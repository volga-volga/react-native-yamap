package ru.vvdev.yamap.view;

import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.yandex.mapkit.geometry.Circle;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CircleMapObject;
import com.yandex.mapkit.map.MapObject;
import com.yandex.mapkit.map.MapObjectTapListener;

import ru.vvdev.yamap.models.ReactMapObject;

public class YamapCircle extends ViewGroup implements MapObjectTapListener, ReactMapObject {
    public Circle circle;

    private CircleMapObject mapObject;
    private int fillColor = Color.BLACK;
    private int strokeColor = Color.BLACK;
    private int zIndex = 1;
    private float strokeWidth = 1.f;
    private Point center = new Point(0, 0);
    private float radius = 0;

    public YamapCircle(Context context) {
        super(context);
        circle = new Circle(center, radius);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
    }

    // PROPS
    public void setCenter(Point point) {
        center = point;
        updateGeometry();
        updateCircle();
    }

    public void setRadius(float _radius) {
        radius = _radius;
        updateGeometry();
        updateCircle();
    }

    private void updateGeometry() {
        circle = new Circle(center, radius);
    }

    public void setZIndex(int _zIndex) {
        zIndex = _zIndex;
        updateCircle();
    }

    public void setStrokeColor(int _color) {
        strokeColor = _color;
        updateCircle();
    }

    public void setFillColor(int _color) {
        fillColor = _color;
        updateCircle();
    }

    public void setStrokeWidth(float width) {
        strokeWidth = width;
        updateCircle();
    }

    private void updateCircle() {
        if (mapObject != null) {
            mapObject.setGeometry(circle);
            mapObject.setStrokeWidth(strokeWidth);
            mapObject.setStrokeColor(strokeColor);
            mapObject.setFillColor(fillColor);
            mapObject.setZIndex(zIndex);
        }
    }

    public void setMapObject(MapObject obj) {
        mapObject = (CircleMapObject) obj;
        mapObject.addTapListener(this);
        updateCircle();
    }

    public MapObject getMapObject() {
        return mapObject;
    }

    @Override
    public boolean onMapObjectTap(@NonNull MapObject mapObject, @NonNull Point point) {
        WritableMap e = Arguments.createMap();
        ((ReactContext) getContext()).getJSModule(RCTEventEmitter.class).receiveEvent(getId(), "onPress", e);

        return false;
    }
}
