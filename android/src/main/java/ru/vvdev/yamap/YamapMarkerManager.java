package ru.vvdev.yamap;

import android.graphics.PointF;
import android.view.View;

import androidx.annotation.NonNull;

import com.facebook.infer.annotation.Assertions;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.yandex.mapkit.geometry.Point;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ru.vvdev.yamap.view.YamapMarker;
import ru.vvdev.yamap.view.YamapView;

public class YamapMarkerManager extends ViewGroupManager<YamapMarker> {
    public static final String REACT_CLASS = "YamapMarker";

    YamapMarkerManager() {}

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

    private YamapMarker castToMarkerView(View view) {
        return (YamapMarker) view;
    }

    @Nonnull
    @Override
    public YamapMarker createViewInstance(@Nonnull ThemedReactContext context) {
        return new YamapMarker(context);
    }

    // PROPS
    @ReactProp(name = "point")
    public void setPoint(View view, ReadableMap markerPoint) {
        if (markerPoint != null) {
            double lon = markerPoint.getDouble("lon");
            double lat = markerPoint.getDouble("lat");
            Point point = new Point(lat, lon);
            castToMarkerView(view).setPoint(point);
        }
    }

    @ReactProp(name = "zIndex")
    public void setZIndex(View view, int zIndex) {
        castToMarkerView(view).setZIndex(zIndex);
    }

    @ReactProp(name = "scale")
    public void setScale(View view, float scale) {
        castToMarkerView(view).setScale(scale);
    }

    @ReactProp(name = "rotated")
    public void setRotated(View view, Boolean rotated) {
        castToMarkerView(view).setRotated(rotated != null ? rotated : true);
    }

    @ReactProp(name = "visible")
    public void setVisible(View view, Boolean visible) {
        castToMarkerView(view).setVisible(visible != null ? visible : true);
    }

    @ReactProp(name = "source")
    public void setSource(View view, String source) {
        if (source != null) {
            castToMarkerView(view).setIconSource(source);
        }
    }

    @ReactProp(name = "anchor")
    public void setAnchor(View view, ReadableMap anchor) {
        castToMarkerView(view).setAnchor(anchor != null ? new PointF((float) anchor.getDouble("x"), (float) anchor.getDouble("y")) : null);
    }

    @Override
    public void addView(YamapMarker parent, View child, int index) {
        parent.addChildView(child, index);
        super.addView(parent, child, index);
    }

    @Override
    public void removeViewAt(YamapMarker parent, int index) {
        parent.removeChildView(index);
        super.removeViewAt(parent, index);
    }

    @Override
    public void receiveCommand(
            @NonNull YamapMarker view,
            String commandType,
            @Nullable ReadableArray args) {
        switch (commandType) {
            case "animatedMoveTo":
                ReadableMap markerPoint = args.getMap(0);
                int moveDuration = args.getInt(1);
                float lon = (float) markerPoint.getDouble("lon");
                float lat = (float) markerPoint.getDouble("lat");
                Point point = new Point(lat, lon);
                castToMarkerView(view).animatedMoveTo(point, moveDuration);
                return;
            case "animatedRotateTo":
                int angle = args.getInt(0);
                int rotateDuration = args.getInt(1);
                castToMarkerView(view).animatedRotateTo(angle, rotateDuration);
                return;
            default:
                throw new IllegalArgumentException(String.format(
                        "Unsupported command %d received by %s.",
                        commandType,
                        getClass().getSimpleName()));
        }
    }
}
