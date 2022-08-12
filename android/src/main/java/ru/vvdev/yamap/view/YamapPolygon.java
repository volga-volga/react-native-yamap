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

import ru.vvdev.yamap.models.ReactMapObject;

public class YamapPolygon extends ViewGroup implements MapObjectTapListener, ReactMapObject {
    public Polygon polygon;
    public ArrayList<Point> _points = new ArrayList<>();
    ArrayList<ArrayList<Point>> innerRings = new ArrayList<>();
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

    // PROPS
    public void setPolygonPoints(ArrayList<Point> points) {
        _points = (points !=  null) ? points :new ArrayList<Point>();
        updatePolygonGeometry();
        updatePolygon();
    }

    public void setInnerRings(ArrayList<ArrayList<Point>> _innerRings) {
        innerRings = _innerRings != null ? _innerRings : new ArrayList<ArrayList<Point>>();
        updatePolygonGeometry();
        updatePolygon();
    }

    private void updatePolygonGeometry() {
        ArrayList<LinearRing> _rings = new ArrayList<>();
        if (innerRings != null) {
            for (int i = 0; i < innerRings.size(); ++i) {
                _rings.add(new LinearRing(innerRings.get(i)));
            }
        }
        polygon = new Polygon(new LinearRing(_points), _rings);
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

    public void setMapObject(MapObject obj) {
        mapObject = (PolygonMapObject) obj;
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
