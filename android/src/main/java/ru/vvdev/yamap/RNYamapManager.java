package ru.vvdev.yamap;

import android.graphics.Bitmap;
import android.view.View;

import com.facebook.infer.annotation.Assertions;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.RequestPoint;
import com.yandex.mapkit.RequestPointType;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.layers.ObjectEvent;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.user_location.UserLocationLayer;
import com.yandex.mapkit.user_location.UserLocationObjectListener;
import com.yandex.mapkit.user_location.UserLocationView;
import com.yandex.runtime.image.ImageProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ru.vvdev.yamap.models.RNMarker;
import ru.vvdev.yamap.utils.Callback;
import ru.vvdev.yamap.utils.ImageLoader;
import ru.vvdev.yamap.view.YaMapView;

public class RNYamapManager extends ViewGroupManager<YaMapView> implements UserLocationObjectListener {
    public static final String REACT_CLASS = "YamapView";

    private static final int SET_CENTER = 1;
    private static final int FIT_ALL_MARKERS = 2;
    private static final int TRY_BUILD_ROUTES = 3;

    private YaMapView view;

    private Bitmap userLocationIcon = null;

    private UserLocationView userLocationView = null;

    private ThemedReactContext reactContext;

    RNYamapManager() { }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    public Map<String, Object> getExportedCustomDirectEventTypeConstants() {
        return MapBuilder.<String, Object>builder()
                .put("onMarkerPress", MapBuilder.of("registrationName", "onMarkerPress"))
                .build();
    }

    @Nonnull
    @Override
    public YaMapView createViewInstance(@Nonnull ThemedReactContext context) {
        reactContext = context;
        view = new YaMapView(context);
        MapKitFactory.getInstance().onStart();
        view.onStart();
        UserLocationLayer userLocationLayer = view.getMap().getUserLocationLayer();
        userLocationLayer.setObjectListener(this);
        userLocationLayer.setEnabled(true);
        userLocationLayer.setHeadingEnabled(true);
        return view;
    }

    @ReactProp(name = "userLocationIcon")
    public void setUserLocationIcon(View _view, String icon) {
        ImageLoader.DownloadImageBitmap(reactContext, icon, new Callback<Bitmap>() {
            @Override
            public void invoke(Bitmap bitmap) {
                userLocationIcon = bitmap;
                updateUserLocationIcon();
            }
        });
    }

    private void setCenter(View _view, ReadableMap center) {
        view.setCenter(new Point(center.getDouble("lat"), center.getDouble("lon")), (float) center.getDouble("zoom"));
    }

    private void fitAllMarkers(View _view) {
        view.fitAllMarkers();
    }

    @ReactProp(name = "routeColors")
    public void setRouteColors(View _view, ReadableMap colors) {
        view.setRouteColors(colors);
    }

    @ReactProp(name = "vehicles")
    public void requestRouteWithSpecificVehicles(View _view, ReadableArray vehicles) {
        if (vehicles == null) {
            throw new IllegalArgumentException("Prop \"vehicles\" cannot be empty, null or undefined. It Should bu Array<String>");
        }

        ArrayList<String> parsed = new ArrayList<>();

        for (int i = 0; i < vehicles.size(); ++i) {
            parsed.add(vehicles.getString(i));
        }

        view.setAcceptVehicleTypes(parsed);
    }

    @ReactProp(name = "route")
    public void requestRoute(View _view, ReadableMap points) {

        if (points == null) {
            view.removeAllSections();
            return;
//            throw new IllegalArgumentException("Prop \"route\" cannot be empty, null or undefined");
        }

        ReadableMap start = points.getMap("start");
        ReadableMap end = points.getMap("end");

        if (start != null && end != null) {

            Double startLat = start.getDouble("lat");
            Double startLon = start.getDouble("lon");
            Double endLat = end.getDouble("lat");
            Double endLon = end.getDouble("lon");

            if (startLat != null && startLon != null && endLat != null && endLon != null) {
                ArrayList<RequestPoint> routePoints = new ArrayList<>();

                List<Point> arrival = new ArrayList<>();
                routePoints.add(new RequestPoint(new Point(startLat, startLon), new ArrayList<Point>(), RequestPointType.WAYPOINT));
                routePoints.add(new RequestPoint(new Point(endLat, endLon), new ArrayList<Point>(), RequestPointType.WAYPOINT));

                view.requestRoute(routePoints);
            }
        }
    }

    @ReactProp(name = "markers")
    public void setMarkers(View _view, ReadableArray markers) {
        ArrayList<RNMarker> parsed = new ArrayList<>();
        for (int i = 0; i < markers.size(); ++i) {
            ReadableMap markerMap = markers.getMap(i);
            double lon = markerMap.getDouble("lon");
            double lat = markerMap.getDouble("lat");
            String id = markerMap.getString("id");
            String uri = markerMap.getString("source");
            Integer zIndex = markerMap.getInt("zIndex");
            RNMarker marker = new RNMarker(lon, lat, id, zIndex, uri);
            parsed.add(marker);
        }
        view.setMarkers(parsed);
    }

    @Override
    public void onObjectAdded(@Nonnull UserLocationView _userLocationView) {
        userLocationView = _userLocationView;
        updateUserLocationIcon();
    }

    private void updateUserLocationIcon() {
        if (userLocationView != null && userLocationIcon != null) {
            PlacemarkMapObject pin = userLocationView.getPin();
            PlacemarkMapObject arrow = userLocationView.getArrow();
            if (userLocationIcon != null) {
                pin.setIcon(ImageProvider.fromBitmap(userLocationIcon));
                arrow.setIcon(ImageProvider.fromBitmap(userLocationIcon));
            }
        }
    }

    public Map getExportedCustomBubblingEventTypeConstants() {
        return MapBuilder.builder()
                .put("routes", MapBuilder.of("phasedRegistrationNames", MapBuilder.of("bubbled", "onRouteFound")))
                .build();
    }

    @Override
    public void onObjectRemoved(@Nonnull UserLocationView userLocationView) {
    }

    @Override
    public void onObjectUpdated(@Nonnull UserLocationView _userLocationView, @Nonnull ObjectEvent objectEvent) {
        userLocationView = _userLocationView;
        updateUserLocationIcon();
    }

    @Override
    public Map<String,Integer> getCommandsMap() {
        return MapBuilder.of(
                "setCenter",
                SET_CENTER,
                "fitAllMarkers",
                FIT_ALL_MARKERS,
                "tryBuildRoutes",
                TRY_BUILD_ROUTES);
    }

    @Override
    public void receiveCommand(
            YaMapView view,
            int commandType,
            @Nullable ReadableArray args) {
        Assertions.assertNotNull(view);
        Assertions.assertNotNull(args);
        switch (commandType) {
            case SET_CENTER:
                setCenter(view, args.getMap(0));
                return;
            case FIT_ALL_MARKERS:
                fitAllMarkers(view);
                return;
            default:
                throw new IllegalArgumentException(String.format(
                        "Unsupported command %d received by %s.",
                        commandType,
                        getClass().getSimpleName()));
        }
    }
}
