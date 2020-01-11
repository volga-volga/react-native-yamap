package ru.vvdev.yamap.view;

import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.yandex.mapkit.geometry.LinearRing;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.geometry.Polygon;
import com.yandex.mapkit.map.MapObject;
import com.yandex.mapkit.map.MapObjectTapListener;
import com.yandex.mapkit.map.PolygonMapObject;

import java.util.ArrayList;

public class YamapPolygon extends ViewGroup implements MapObjectTapListener {
    public Polygon polygon;
    public ArrayList<Point> _points;
    private PolygonMapObject mapObject;
    private int fillColor = Color.BLACK;
    private int strokeColor = Color.BLACK;
    private int zIndex = 1;
    private float strokeWidth = 1.f;

    public YamapPolygon(Context context) {
        super(context);
        polygon = new Polygon(new LinearRing(new ArrayList<Point>()), new ArrayList<LinearRing>());
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
    }

    // props
    public void setPolygonPoints(ArrayList<Point> points) {
        _points = points;
        polygon = new Polygon(new LinearRing(_points), new ArrayList<LinearRing>());
        updatePolygon();
    }

    public void setZIndex(int _zIndex) {
        zIndex = _zIndex;
        updatePolygon();
    }

    public void setStrokeColor(int _color) {
        strokeColor = _color;
        updatePolygon();
    }

    public void setFillColor(int _color) {
        fillColor = _color;
        updatePolygon();
    }

    public void setStrokeWidth(float width) {
        strokeWidth = width;
        updatePolygon();
    }

    private void updatePolygon() {
        if (mapObject != null) {
            mapObject.setGeometry(polygon);
            mapObject.setStrokeWidth(strokeWidth);
            mapObject.setStrokeColor(strokeColor);
            mapObject.setFillColor(fillColor);
            mapObject.setZIndex(zIndex);
        }
    }

    public void setMapObject(PolygonMapObject obj) {
        mapObject = obj;
        mapObject.addTapListener(this);
        updatePolygon();
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
