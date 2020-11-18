package ru.vvdev.yamap;

import android.view.LayoutInflater;
import android.view.View;

import com.facebook.infer.annotation.Assertions;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.VisibleRegion;

import java.util.ArrayList;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import androidx.annotation.NonNull;
import ru.vvdev.yamap.view.YamapView;

public class YamapViewManager extends ViewGroupManager<YamapView> {
    public static final String REACT_CLASS = "YamapView";

    private static final int SET_CENTER = 1;
    private static final int FIT_ALL_MARKERS = 2;
    private static final int FIND_ROUTES = 3;
    private static final int SET_ZOOM = 4;
    private static final int GET_CAMERA_POSITION = 5;
    private static final int GET_VISIBLE_REGION = 6;

    YamapViewManager() {
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    public Map<String, Object> getExportedCustomDirectEventTypeConstants() {
        return MapBuilder.<String, Object>builder()
                .build();
    }

    public Map getExportedCustomBubblingEventTypeConstants() {
        return MapBuilder.builder()
                .put("routes", MapBuilder.of("phasedRegistrationNames", MapBuilder.of("bubbled", "onRouteFound")))
                .put("cameraPosition", MapBuilder.of("phasedRegistrationNames", MapBuilder.of("bubbled", "onCameraPositionReceived")))
                .put("cameraPositionChanged", MapBuilder.of("phasedRegistrationNames", MapBuilder.of("bubbled", "onCameraPositionChange")))
                .put("onMapPress", MapBuilder.of("phasedRegistrationNames", MapBuilder.of("bubbled", "onMapPress")))
                .put("onMapLongPress", MapBuilder.of("phasedRegistrationNames", MapBuilder.of("bubbled", "onMapLongPress")))
                .put("visibleRegion", MapBuilder.of("phasedRegistrationNames", MapBuilder.of("bubbled", "onVisibleRegionReceived")))
                .build();
    }

    @Override
    public Map<String, Integer> getCommandsMap() {
        return MapBuilder.of(
                "setCenter",
                SET_CENTER,
                "fitAllMarkers",
                FIT_ALL_MARKERS,
                "findRoutes",
                FIND_ROUTES,
                "setZoom",
                SET_ZOOM,
                "getCameraPosition",
                GET_CAMERA_POSITION,
                "getVisibleRegion",
                GET_VISIBLE_REGION);
    }

    @Override
    public void receiveCommand(
            @NonNull YamapView view,
            String commandType,
            @Nullable ReadableArray args) {
        Assertions.assertNotNull(view);
        Assertions.assertNotNull(args);
        switch (commandType) {
            case "setCenter":
                setCenter(castToYaMapView(view), args.getMap(0), (float) args.getDouble(1), (float) args.getDouble(2), (float) args.getDouble(3), (float) args.getDouble(4), args.getInt(5));
                return;
            case "fitAllMarkers":
                fitAllMarkers(view);
                return;
            case "findRoutes":
                if (args != null) {
                    findRoutes(view, args.getArray(0), args.getArray(1), args.getString(2));
                }
                return;
            case "setZoom":
                if (args != null) {
                    view.setZoom((float) args.getDouble(0), (float) args.getDouble(1), args.getInt(2));
                }
                return;
            case "getCameraPosition":
                if (args != null) {
                    view.emitCameraPositionToJS(args.getString(0));
                }
                return;
            case "getVisibleRegion":
                if (args != null) {
                    view.emitVisibleRegionToJS(args.getString(0));
                }
                return;
            default:
                throw new IllegalArgumentException(String.format(
                        "Unsupported command %d received by %s.",
                        commandType,
                        getClass().getSimpleName()));
        }
    }

    private YamapView castToYaMapView(View view) {
        return (YamapView) view;
    }

    @Nonnull
    @Override
    public YamapView createViewInstance(@Nonnull ThemedReactContext context) {
        YamapView view = (YamapView) LayoutInflater.from(context).inflate(R.layout.map_layout, null);
        MapKitFactory.getInstance().onStart();
        view.onStart();
        return view;
    }

    private void setCenter(YamapView view, ReadableMap center, float zoom, float azimuth, float tilt, float duration, int animation) {
        if (center != null) {
            Point centerPosition = new Point(center.getDouble("lat"), center.getDouble("lon"));
            CameraPosition pos = new CameraPosition(centerPosition, zoom, azimuth, tilt);
            view.setCenter(pos, duration, animation);
        }
    }

    private void fitAllMarkers(View view) {
        castToYaMapView(view).fitAllMarkers();
    }

    private void findRoutes(View view, ReadableArray jsPoints, ReadableArray jsVehicles, String id) {
        if (jsPoints != null) {
            ArrayList<Point> points = new ArrayList<>();
            for (int i = 0; i < jsPoints.size(); ++i) {
                ReadableMap point = jsPoints.getMap(i);
                if (point != null) {
                    points.add(new Point(point.getDouble("lat"), point.getDouble("lon")));
                }
            }
            ArrayList<String> vehicles = new ArrayList<>();
            if (jsVehicles != null) {
                for (int i = 0; i < jsVehicles.size(); ++i) {
                    vehicles.add(jsVehicles.getString(i));
                }
            }
            castToYaMapView(view).findRoutes(points, vehicles, id);
        }
    }

    // props
    @ReactProp(name = "userLocationIcon")
    public void setUserLocationIcon(View view, String icon) {
        if (icon != null) {
            castToYaMapView(view).setUserLocationIcon(icon);
        }
    }

    @ReactProp(name = "userLocationAccuracyFillColor")
    public void setUserLocationAccuracyFillColor(View view, int color) {
        castToYaMapView(view).setUserLocationAccuracyFillColor(color);
    }

    @ReactProp(name = "userLocationAccuracyStrokeColor")
    public void setUserLocationAccuracyStrokeColor(View view, int color) {
        castToYaMapView(view).setUserLocationAccuracyStrokeColor(color);
    }

    @ReactProp(name = "userLocationAccuracyStrokeWidth")
    public void setUserLocationAccuracyStrokeWidth(View view, float width) {
        castToYaMapView(view).setUserLocationAccuracyStrokeWidth(width);
    }

    @ReactProp(name = "showUserPosition")
    public void setShowUserPosition(View view, Boolean show) {
        castToYaMapView(view).setShowUserPosition(show);
    }

    @ReactProp(name = "nightMode")
    public void setNightMode(View view, Boolean nightMode) {
        castToYaMapView(view).setNightMode(nightMode != null ? nightMode : false);
    }

    @ReactProp(name = "mapStyle")
    public void setMapStyle(View view, String style) {
        if (style != null) {
            castToYaMapView(view).setMapStyle(style);
        }
    }

    @Override
    public void addView(YamapView parent, View child, int index) {
        parent.addFeature(child, index);
        super.addView(parent, child, index);
    }

    @Override
    public void removeViewAt(YamapView parent, int index) {
        parent.removeChild(index);
        super.removeViewAt(parent, index);
    }
}
