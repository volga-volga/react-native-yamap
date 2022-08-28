package ru.vvdev.yamap;

import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.yandex.mapkit.geometry.Point;

import java.util.Map;

import javax.annotation.Nonnull;

import ru.vvdev.yamap.view.YamapCircle;

public class YamapCircleManager extends ViewGroupManager<YamapCircle> {
    public static final String REACT_CLASS = "YamapCircle";

    YamapCircleManager() { }

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

    @Nonnull
    @Override
    public YamapCircle createViewInstance(@Nonnull ThemedReactContext context) {
        return new YamapCircle(context);
    }

    // PROPS
    @ReactProp(name = "center")
    public void setCenter(YamapCircle view, ReadableMap center) {
        if (center != null) {
            double lon = center.getDouble("lon");
            double lat = center.getDouble("lat");
            Point point = new Point(lat, lon);
            view.setCenter(point);
        }
    }

    @ReactProp(name = "radius")
    public void setRadius(YamapCircle view, float radius) {
        view.setRadius(radius);
    }

    @ReactProp(name = "strokeWidth")
    public void setStrokeWidth(YamapCircle view, float width) {
        view.setStrokeWidth(width);
    }

    @ReactProp(name = "strokeColor")
    public void setStrokeColor(YamapCircle view, int color) {
        view.setStrokeColor(color);
    }

    @ReactProp(name = "fillColor")
    public void setFillColor(YamapCircle view, int color) {
        view.setFillColor(color);
    }

    @ReactProp(name = "zIndex")
    public void setZIndex(YamapCircle view, int zIndex) {
        view.setZIndex(zIndex);
    }
}
