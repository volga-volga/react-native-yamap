package ru.vvdev.yamap;

import android.arch.lifecycle.LifecycleRegistry;
import android.graphics.Color;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.view.View;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;

import com.facebook.react.uimanager.annotations.ReactProp;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.layers.ObjectEvent;
import com.yandex.mapkit.location.LocationManager;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.CompositeIcon;
import com.yandex.mapkit.map.IconStyle;
import com.yandex.mapkit.map.MapObject;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.map.RotationType;
import com.yandex.mapkit.user_location.UserLocationLayer;
import com.yandex.mapkit.user_location.UserLocationObjectListener;
import com.yandex.mapkit.user_location.UserLocationView;
import com.yandex.runtime.image.ImageProvider;

import java.util.ArrayList;
import java.util.Map;

import javax.annotation.Nonnull;

public class RNYamapManager extends SimpleViewManager<View> implements UserLocationObjectListener {
    public static final String REACT_CLASS = "YamapView";

    private YaMapView view;

    private ThemedReactContext reactContext;

    private ImageProvider userLocation;
    private ImageProvider selectedMarker;
    private ImageProvider marker;

    public RNYamapManager(ImageProvider userLocation, ImageProvider selectedMarker, ImageProvider marker) {
        this.userLocation = userLocation;
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
                .put("onMarkerPress",
                        MapBuilder.of("registrationName", "onMarkerPress"))
                .build();
    }

    @Nonnull
    @Override
    public View createViewInstance(@Nonnull ThemedReactContext context){
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
        view.setCenter(new Point(center.getDouble("lat"), center.getDouble("lon")));
    }

    @ReactProp(name = "markers")
    public void setMarkers(View _view, ReadableArray markers) {
        ArrayList<RNMarker> parsed = new ArrayList<>();
        for (int i = 0; i < markers.size(); ++i) {
            ReadableMap markerMap = markers.getMap(i);
            double lon = markerMap.getDouble("lon");
            double lat = markerMap.getDouble("lat");
            String id = markerMap.getString("id");
            boolean isSelected = markerMap.getBoolean("selected");
            RNMarker marker = new RNMarker(lon, lat, id, isSelected);
            parsed.add(marker);
        }
        view.setMarkers(parsed);
    }

    @Override
    public void onObjectAdded(@NonNull UserLocationView userLocationView) {
        YaMapView mapView = view;
        PlacemarkMapObject pin = userLocationView.getPin();
        pin.setIcon(userLocation);
        pin.setIconStyle(new IconStyle().setScale(0.3f));
        userLocationView.getAccuracyCircle().setFillColor(Color.TRANSPARENT);

    }

    @Override
    public void onObjectRemoved(@NonNull UserLocationView userLocationView) {
    }

    @Override
    public void onObjectUpdated(@NonNull UserLocationView userLocationView, @NonNull ObjectEvent objectEvent) {
    }
}
