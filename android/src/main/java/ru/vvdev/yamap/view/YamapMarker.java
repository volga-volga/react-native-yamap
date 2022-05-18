package ru.vvdev.yamap.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.view.View;

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
import com.yandex.runtime.image.ImageProvider;

import java.util.ArrayList;

import ru.vvdev.yamap.models.ReactMapObject;
import ru.vvdev.yamap.utils.Callback;
import ru.vvdev.yamap.utils.ImageLoader;

public class YamapMarker extends ReactViewGroup implements MapObjectTapListener, ReactMapObject {
    public Point point;
    private int zIndex = 1;
    private float scale = 1;
    private Boolean visible = true;
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

    // props
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
                    ImageLoader.DownloadImageBitmap(getContext(), iconSource, new Callback<Bitmap>() {
                        @Override
                        public void invoke(Bitmap bitmap) {
                            try {
                                if (mapObject != null) {
                                    mapObject.setIcon(ImageProvider.fromBitmap(bitmap));
                                    mapObject.setIconStyle(iconStyle);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
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

    @Override
    public boolean onMapObjectTap(@NonNull MapObject mapObject, @NonNull Point point) {
        WritableMap e = Arguments.createMap();
        ((ReactContext) getContext()).getJSModule(RCTEventEmitter.class).receiveEvent(getId(), "onPress", e);
        return false;
    }
}
