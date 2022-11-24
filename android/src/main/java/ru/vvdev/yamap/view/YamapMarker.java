package ru.vvdev.yamap.view;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.os.Looper;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.facebook.react.views.view.ReactViewGroup;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.IconStyle;
import com.yandex.mapkit.map.MapObject;
import com.yandex.mapkit.map.MapObjectTapListener;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.map.RotationType;
import com.yandex.runtime.image.ImageProvider;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;

import ru.vvdev.yamap.models.ReactMapObject;
import ru.vvdev.yamap.utils.Callback;
import ru.vvdev.yamap.utils.ImageLoader;

public class YamapMarker extends ReactViewGroup implements MapObjectTapListener, ReactMapObject {
    public Point point;
    private int zIndex = 1;
    private float scale = 1;
    private Boolean visible = true;
    private Boolean rotated = false;
    private int YAMAP_FRAMES_PER_SECOND = 25;
    private PointF markerAnchor = null;
    private String iconSource;
    private View _childView;
    private PlacemarkMapObject mapObject;
    private ArrayList<View> childs = new ArrayList<>();

    private OnLayoutChangeListener childLayoutListener = new OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            updateMarker();
        }
    };

    public YamapMarker(Context context) {
        super(context);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
    }

    // PROPS
    public void setPoint(Point _point) {
        point = _point;
        updateMarker();
    }

    public void setZIndex(int _zIndex) {
        zIndex = _zIndex;
        updateMarker();
    }

    public void setScale(float _scale) {
        scale = _scale;
        updateMarker();
    }

    public void setRotated(Boolean _rotated) {
        rotated = _rotated;
        updateMarker();
    }

    public void setVisible(Boolean _visible) {
        visible = _visible;
        updateMarker();
    }

    public void setIconSource(String source) {
        iconSource = source;
        updateMarker();
    }

    public void setAnchor(PointF anchor) {
        markerAnchor = anchor;
        updateMarker();
    }

    private void updateMarker() {
        if (mapObject != null && mapObject.isValid()) {
            final IconStyle iconStyle = new IconStyle();
            iconStyle.setScale(scale);
            iconStyle.setRotationType(rotated ? RotationType.ROTATE : RotationType.NO_ROTATION);
            iconStyle.setVisible(visible);
            if (markerAnchor != null) {
                iconStyle.setAnchor(markerAnchor);
            }
            mapObject.setGeometry(point);
            mapObject.setZIndex(zIndex);
            mapObject.setIconStyle(iconStyle);

            if (_childView != null) {
                try {
                    Bitmap b = Bitmap.createBitmap(_childView.getWidth(), _childView.getHeight(), Bitmap.Config.ARGB_8888);
                    Canvas c = new Canvas(b);
                    _childView.draw(c);
                    mapObject.setIcon(ImageProvider.fromBitmap(b));
                    mapObject.setIconStyle(iconStyle);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (childs.size() == 0) {
                if (!iconSource.equals("")) {
                    YamapView parent = (YamapView)getParent();
                    if (parent!=null) {
                        parent.setImage(iconSource, mapObject, iconStyle);
                    }
                }
            }
        }
    }

    public void setMapObject(MapObject obj) {
        mapObject = (PlacemarkMapObject) obj;
        mapObject.addTapListener(this);
        updateMarker();
    }

    public MapObject getMapObject() {
        return mapObject;
    }

    public void setChildView(View view) {
        if (view == null) {
            _childView.removeOnLayoutChangeListener(childLayoutListener);
            _childView = null;
            updateMarker();
            return;
        }
        _childView = view;
        _childView.addOnLayoutChangeListener(childLayoutListener);
    }

    public void addChildView(View view, int index) {
        childs.add(index, view);
        setChildView(childs.get(0));
    }

    public void removeChildView(int index) {
        childs.remove(index);
        setChildView(childs.size() > 0 ? childs.get(0) : null);
    }

    public void moveAnimationLoop(double lat, double lon) {
        PlacemarkMapObject placemark = (PlacemarkMapObject) this.getMapObject();
        placemark.setGeometry(new Point(lat, lon));
    }

    public void rotateAnimationLoop(float delta) {
        PlacemarkMapObject placemark = (PlacemarkMapObject) this.getMapObject();
        placemark.setDirection(delta);
    }

    public void animatedMoveTo(Point point, final float duration) {
        PlacemarkMapObject placemark = (PlacemarkMapObject) this.getMapObject();
        Point p = placemark.getGeometry();
        final double startLat = p.getLatitude();
        final double startLon = p.getLongitude();
        final double deltaLat = point.getLatitude() - startLat;
        final double deltaLon = point.getLongitude() - startLon;
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.setDuration((long) duration);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override public void onAnimationUpdate(ValueAnimator animation) {
                try {
                    float v = animation.getAnimatedFraction();
                    moveAnimationLoop(startLat + v*deltaLat, startLon + v*deltaLon);
                } catch (Exception ex) {
                    // I don't care atm..
                }
            }
        });
        valueAnimator.start();
    }

    public void animatedRotateTo(final float angle, float duration) {
        PlacemarkMapObject placemark = (PlacemarkMapObject) this.getMapObject();
        final float startDirection = placemark.getDirection();
        final float delta = angle - placemark.getDirection();
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.setDuration((long) duration);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override public void onAnimationUpdate(ValueAnimator animation) {
                try {
                    float v = animation.getAnimatedFraction();
                    rotateAnimationLoop(startDirection + v*delta);
                } catch (Exception ex) {
                    // I don't care atm..
                }
            }
        });
        valueAnimator.start();
    }

    @Override
    public boolean onMapObjectTap(@NonNull MapObject mapObject, @NonNull Point point) {
        WritableMap e = Arguments.createMap();
        ((ReactContext) getContext()).getJSModule(RCTEventEmitter.class).receiveEvent(getId(), "onPress", e);

        return false;
    }
}
