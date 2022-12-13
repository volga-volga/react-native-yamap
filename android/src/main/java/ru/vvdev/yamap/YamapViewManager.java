package ru.vvdev.yamap;

import android.view.View;

import androidx.annotation.NonNull;

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

import java.util.ArrayList;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ru.vvdev.yamap.view.YamapView;

public class YamapViewManager extends ViewGroupManager<YamapView> {
    public static final String REACT_CLASS = "YamapView";

    private static final int SET_CENTER = 1;
    private static final int FIT_ALL_MARKERS = 2;
    private static final int FIND_ROUTES = 3;
    private static final int SET_ZOOM = 4;
    private static final int GET_CAMERA_POSITION = 5;
    private static final int GET_VISIBLE_REGION = 6;
    private static final int SET_TRAFFIC_VISIBLE = 7;
    private static final int FIT_MARKERS = 8;
    private static final int GET_SCREEN_POINTS = 9;
    private static final int GET_WORLD_POINTS = 10;

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
            .put("cameraPositionChange", MapBuilder.of("phasedRegistrationNames", MapBuilder.of("bubbled", "onCameraPositionChange")))
            .put("cameraPositionChangeEnd", MapBuilder.of("phasedRegistrationNames", MapBuilder.of("bubbled", "onCameraPositionChangeEnd")))
            .put("visibleRegion", MapBuilder.of("phasedRegistrationNames", MapBuilder.of("bubbled", "onVisibleRegionReceived")))
            .put("onMapPress", MapBuilder.of("phasedRegistrationNames", MapBuilder.of("bubbled", "onMapPress")))
            .put("onMapLongPress", MapBuilder.of("phasedRegistrationNames", MapBuilder.of("bubbled", "onMapLongPress")))
            .put("onMapLoaded", MapBuilder.of("phasedRegistrationNames", MapBuilder.of("bubbled", "onMapLoaded")))
            .put("screenToWorldPoints", MapBuilder.of("phasedRegistrationNames", MapBuilder.of("bubbled", "onScreenToWorldPointsReceived")))
            .put("worldToScreenPoints", MapBuilder.of("phasedRegistrationNames", MapBuilder.of("bubbled", "onWorldToScreenPointsReceived")))
            .build();
    }

    @Override
    public Map<String, Integer> getCommandsMap() {
        Map<String, Integer> map = MapBuilder.newHashMap();
        map.put("setCenter", SET_CENTER);
        map.put("fitAllMarkers", FIT_ALL_MARKERS);
        map.put("findRoutes", FIND_ROUTES);
        map.put("setZoom", SET_ZOOM);
        map.put("getCameraPosition", GET_CAMERA_POSITION);
        map.put("getVisibleRegion", GET_VISIBLE_REGION);
        map.put("setTrafficVisible", SET_TRAFFIC_VISIBLE);
        map.put("fitMarkers", FIT_MARKERS);
        map.put("getScreenPoints", GET_SCREEN_POINTS);
        map.put("getWorldPoints", GET_WORLD_POINTS);

        return map;
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
                    break;

                case "fitAllMarkers":
                    fitAllMarkers(view);
                    break;

                case "fitMarkers":
                    if (args != null) {
                        fitMarkers(view, args.getArray(0));
                    }
                    break;

                case "findRoutes":
                    if (args != null) {
                        findRoutes(view, args.getArray(0), args.getArray(1), args.getString(2));
                    }
                    break;

                case "setZoom":
                    if (args != null) {
                        view.setZoom((float)args.getDouble(0), (float)args.getDouble(1), args.getInt(2));
                    }
                    break;

                case "getCameraPosition":
                    if (args != null) {
                        view.emitCameraPositionToJS(args.getString(0));
                    }
                    break;

                case "getVisibleRegion":
                    if (args != null) {
                        view.emitVisibleRegionToJS(args.getString(0));
                    }
                    break;
            case "setTrafficVisible":
                if (args != null) {
                    view.setTrafficVisible(args.getBoolean(0));
                }
                break;

                case "getScreenPoints":
                    if (args != null) {
                        view.emitWorldToScreenPoints(args.getArray(0), args.getString(1));
                    }
                    break;

                case "getWorldPoints":
                    if (args != null) {
                        view.emitScreenToWorldPoints(args.getArray(0), args.getString(1));
                    }
                    break;

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
        YamapView view = new YamapView(context);
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

    private void fitMarkers(View view, ReadableArray jsPoints) {
        if (jsPoints != null) {
            ArrayList<Point> points = new ArrayList<>();

            for (int i = 0; i < jsPoints.size(); ++i) {
                ReadableMap point = jsPoints.getMap(i);
                if (point != null) {
                    points.add(new Point(point.getDouble("lat"), point.getDouble("lon")));
                }
            }

            castToYaMapView(view).fitMarkers(points);
        }
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

    // PROPS
    @ReactProp(name = "userLocationIcon")
    public void setUserLocationIcon(View view, String icon) {
        if (icon != null) {
            castToYaMapView(view).setUserLocationIcon(icon);
        }
    }

    @ReactProp(name = "userLocationIconScale")
    public void setUserLocationIconScale(View view, float scale) {
        castToYaMapView(view).setUserLocationIconScale(scale);
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

    @ReactProp(name = "scrollGesturesEnabled")
    public void setScrollGesturesEnabled(View view, Boolean scrollGesturesEnabled) {
        castToYaMapView(view).setScrollGesturesEnabled(scrollGesturesEnabled == true);
    }

    @ReactProp(name = "rotateGesturesEnabled")
    public void setRotateGesturesEnabled(View view, Boolean rotateGesturesEnabled) {
        castToYaMapView(view).setRotateGesturesEnabled(rotateGesturesEnabled == true);
    }

    @ReactProp(name = "zoomGesturesEnabled")
    public void setZoomGesturesEnabled(View view, Boolean zoomGesturesEnabled) {
        castToYaMapView(view).setZoomGesturesEnabled(zoomGesturesEnabled == true);
    }

    @ReactProp(name = "tiltGesturesEnabled")
    public void setTiltGesturesEnabled(View view, Boolean tiltGesturesEnabled) {
        castToYaMapView(view).setTiltGesturesEnabled(tiltGesturesEnabled == true);
    }

    @ReactProp(name = "fastTapEnabled")
    public void setFastTapEnabled(View view, Boolean fastTapEnabled) {
        castToYaMapView(view).setFastTapEnabled(fastTapEnabled == true);
    }

    @ReactProp(name = "mapStyle")
    public void setMapStyle(View view, String style) {
        if (style != null) {
            castToYaMapView(view).setMapStyle(style);
        }
    }

    @ReactProp(name = "mapType")
    public void setMapType(View view, String type) {
        if (type != null) {
            castToYaMapView(view).setMapType(type);
        }
    }

    @ReactProp(name = "initialRegion")
    public void setInitialRegion(View view, ReadableMap params) {
        if (params != null) {
            castToYaMapView(view).setInitialRegion(params);
        }
    }

    @ReactProp(name = "maxFps")
    public void setMaxFps(View view, float maxFps) {
        castToYaMapView(view).setMaxFps(maxFps);
    }

    @ReactProp(name = "interactive")
    public void setInteractive(View view, boolean interactive) {
        castToYaMapView(view).setInteractive(interactive);
    }

    @ReactProp(name = "logoPosition")
    public void setLogoPosition(View view, ReadableMap params) {
        if (params != null) {
            castToYaMapView(view).setLogoPosition(params);
        }
    }

    @ReactProp(name = "logoPadding")
    public void setLogoPadding(View view, ReadableMap params) {
        if (params != null) {
            castToYaMapView(view).setLogoPadding(params);
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
