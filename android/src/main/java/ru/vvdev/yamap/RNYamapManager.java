package ru.vvdev.yamap;

import android.graphics.Color;
import android.view.View;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;

import com.facebook.react.uimanager.annotations.ReactProp;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.RequestPoint;
import com.yandex.mapkit.RequestPointType;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.layers.ObjectEvent;
import com.yandex.mapkit.map.IconStyle;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.user_location.UserLocationLayer;
import com.yandex.mapkit.user_location.UserLocationObjectListener;
import com.yandex.mapkit.user_location.UserLocationView;
import com.yandex.runtime.image.ImageProvider;

import java.util.ArrayList;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RNYamapManager extends SimpleViewManager<View> implements UserLocationObjectListener {
    public static final String REACT_CLASS = "YamapView";

    private YaMapView view;

    private ThemedReactContext reactContext;

    private ImageProvider userLocationPin;
    private ImageProvider userLocationArrow;
    private ImageProvider selectedMarker;
    private ImageProvider marker;

    RNYamapManager(@Nullable ImageProvider userLocationPin, @Nullable ImageProvider userLocationArrow, @Nullable ImageProvider selectedMarker,
                   @Nullable ImageProvider marker) {
        this.userLocationPin = userLocationPin;
        this.userLocationArrow = userLocationArrow;
        this.selectedMarker = selectedMarker;
        this.marker = marker;
    }

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
    public View createViewInstance(@Nonnull ThemedReactContext context) {
        reactContext = context;
        view = new YaMapView(context, selectedMarker, marker);
        MapKitFactory.getInstance().onStart();
        view.onStart();
        UserLocationLayer userLocationLayer = view.getMap().getUserLocationLayer();
        userLocationLayer.setEnabled(true);
        userLocationLayer.setHeadingEnabled(true);
        userLocationLayer.setObjectListener(this);

        return view;
    }

    @ReactProp(name = "center")
    public void setCenter(View _view, ReadableMap center) {
        view.setCenter(new Point(center.getDouble("lat"), center.getDouble("lon")), (float) center.getDouble("zoom"));
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
            throw new IllegalArgumentException("Prop \"route\" cannot be empty, null or undefined");
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

//                routePoints.add(new RequestPoint(new Point(startLat, startLon), RequestPointType.WAYPOINT, null));
//                routePoints.add(new RequestPoint(new Point(endLat, endLon), RequestPointType.WAYPOINT, null));

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
    public void onObjectAdded(@Nonnull UserLocationView userLocationView) {
        PlacemarkMapObject pin = userLocationView.getPin();
        PlacemarkMapObject arrow = userLocationView.getArrow();
        if (userLocationPin != null) {
            pin.setIcon(userLocationPin);
        }
        if (userLocationArrow != null) {
            arrow.setIcon(userLocationArrow);
        }
        pin.setIconStyle(new IconStyle().setScale(0.0f));
        arrow.setIconStyle(new IconStyle().setScale(0.9f));
        userLocationView.getAccuracyCircle().setFillColor(Color.TRANSPARENT);

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
    public void onObjectUpdated(@Nonnull UserLocationView userLocationView, @Nonnull ObjectEvent objectEvent) {
    }
}
