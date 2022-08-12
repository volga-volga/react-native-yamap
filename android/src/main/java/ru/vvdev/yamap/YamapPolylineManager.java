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

import ru.vvdev.yamap.view.YamapPolyline;

public class YamapPolylineManager extends ViewGroupManager<YamapPolyline> {
    public static final String REACT_CLASS = "YamapPolyline";

    YamapPolylineManager() {
    }

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

    private YamapPolyline castToPolylineView(View view) {
        return (YamapPolyline) view;
    }

    @Nonnull
    @Override
    public YamapPolyline createViewInstance(@Nonnull ThemedReactContext context) {
        return new YamapPolyline(context);
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
            castToPolylineView(view).setPolygonPoints(parsed);
        }
    }

    @ReactProp(name = "strokeWidth")
    public void setStrokeWidth(View view, float width) {
        castToPolylineView(view).setStrokeWidth(width);
    }

    @ReactProp(name = "strokeColor")
    public void setStrokeColor(View view, int color) {
        castToPolylineView(view).setStrokeColor(color);
    }

    @ReactProp(name = "zIndex")
    public void setZIndex(View view, int zIndex) {
        castToPolylineView(view).setZIndex(zIndex);
    }

    @ReactProp(name = "dashLength")
    public void setDashLength(View view, int length) {
        castToPolylineView(view).setDashLength(length);
    }

    @ReactProp(name = "dashOffset")
    public void setDashOffset(View view, int offset) {
        castToPolylineView(view).setDashOffset(offset);
    }

    @ReactProp(name = "gapLength")
    public void setGapLength(View view, int length) {
        castToPolylineView(view).setGapLength(length);
    }

    @ReactProp(name = "outlineWidth")
    public void setOutlineWidth(View view, int width) {
        castToPolylineView(view).setOutlineWidth(width);
    }

    @ReactProp(name = "outlineColor")
    public void setOutlineColor(View view, int color) {
        castToPolylineView(view).setOutlineColor(color);
    }
}
