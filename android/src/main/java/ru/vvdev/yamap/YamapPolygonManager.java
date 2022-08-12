package ru.vvdev.yamap;

import android.view.View;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.yandex.mapkit.geometry.Point;

import java.util.ArrayList;
import java.util.Map;

import javax.annotation.Nonnull;

import ru.vvdev.yamap.view.YamapPolygon;

public class YamapPolygonManager extends ViewGroupManager<YamapPolygon> {
    public static final String REACT_CLASS = "YamapPolygon";

    YamapPolygonManager() { }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    public Map<String, Object> getExportedCustomDirectEventTypeConstants() {
        return MapBuilder.<String, Object>builder()
                .put("onPress", MapBuilder.of("registrationName", "onPress"))
                .build();
    }

    public Map getExportedCustomBubblingEventTypeConstants() {
        return MapBuilder.builder()
                .build();
    }

    private YamapPolygon castToPolygonView(View view) {
        return (YamapPolygon) view;
    }

    @Nonnull
    @Override
    public YamapPolygon createViewInstance(@Nonnull ThemedReactContext context) {
        return new YamapPolygon(context);
    }

    // PROPS
    @ReactProp(name = "points")
    public void setPoints(View view, ReadableArray points) {
        if (points != null) {
            ArrayList<Point> parsed = new ArrayList<>();
            for (int i = 0; i < points.size(); ++i) {
                ReadableMap markerMap = points.getMap(i);
                if (markerMap != null) {
                    double lon = markerMap.getDouble("lon");
                    double lat = markerMap.getDouble("lat");
                    Point point = new Point(lat, lon);
                    parsed.add(point);
                }
            }
            castToPolygonView(view).setPolygonPoints(parsed);
        }
    }

    @ReactProp(name = "innerRings")
    public void setInnerRings(View view, ReadableArray _rings) {
        ArrayList<ArrayList<Point>> rings = new ArrayList<>();
        if (_rings != null) {
            for (int j = 0; j < _rings.size(); ++j) {
                ReadableArray points = _rings.getArray(j);
                if (points != null) {
                    ArrayList<Point> parsed = new ArrayList<>();
                    for (int i = 0; i < points.size(); ++i) {
                        ReadableMap markerMap = points.getMap(i);
                        if (markerMap != null) {
                            double lon = markerMap.getDouble("lon");
                            double lat = markerMap.getDouble("lat");
                            Point point = new Point(lat, lon);
                            parsed.add(point);
                        }
                    }
                    rings.add(parsed);
                }
            }
        }
        castToPolygonView(view).setInnerRings(rings);
    }

    @ReactProp(name = "strokeWidth")
    public void setStrokeWidth(View view, float width) {
        castToPolygonView(view).setStrokeWidth(width);
    }

    @ReactProp(name = "strokeColor")
    public void setStrokeColor(View view, int color) {
        castToPolygonView(view).setStrokeColor(color);
    }

    @ReactProp(name = "fillColor")
    public void setFillColor(View view, int color) {
        castToPolygonView(view).setFillColor(color);
    }

    @ReactProp(name = "zIndex")
    public void setZIndex(View view, int zIndex) {
        castToPolygonView(view).setZIndex(zIndex);
    }
}
