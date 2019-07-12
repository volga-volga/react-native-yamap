package ru.vvdev.yamap;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.jni.MapIteratorHelper;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.RequestPoint;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.geometry.Polyline;
import com.yandex.mapkit.geometry.SubpolylineHelper;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.IconStyle;
import com.yandex.mapkit.map.MapObject;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.MapObjectTapListener;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.map.PolylineMapObject;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.transport.TransportFactory;
import com.yandex.mapkit.transport.masstransit.MasstransitOptions;
import com.yandex.mapkit.transport.masstransit.MasstransitRouter;
import com.yandex.mapkit.transport.masstransit.PedestrianRouter;
import com.yandex.mapkit.transport.masstransit.Route;
import com.yandex.mapkit.transport.masstransit.RouteStop;
import com.yandex.mapkit.transport.masstransit.Section;
import com.yandex.mapkit.transport.masstransit.SectionMetadata;
import com.yandex.mapkit.transport.masstransit.Session;
import com.yandex.mapkit.transport.masstransit.TimeOptions;
import com.yandex.mapkit.transport.masstransit.Transport;
import com.yandex.mapkit.transport.masstransit.Weight;
import com.yandex.mapkit.user_location.UserLocationLayer;
import com.yandex.runtime.Error;
import com.yandex.runtime.image.ImageProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YaMapView extends MapView implements Session.RouteListener {

    // default colors for known vehicles
    // "underground" actually get color considering with his own branch"s color
    private Map<String, String> vehicleColors = new HashMap<String, String>() {{
        put("bus", "#59ACFF");
        put("railway", "#F8634F");
        put("tramway", "#C86DD7");
        put("suburban", "#3023AE");
        put("underground", "#BDCCDC");
        put("trolleybus", "#55CfDC");
        put("walk", "#333333");
    }};

    private ImageProvider selectedMarkerIcon;
    private ImageProvider markerIcon;

    private ArrayList<String> acceptVehicleTypes = new ArrayList<>();
    private ArrayList<RequestPoint> lastKnownRoutePoints = new ArrayList<>();
    private ArrayList<RNMarker> lastKnownMarkers = new ArrayList<>();
    private MasstransitOptions masstransitOptions = new MasstransitOptions(new ArrayList<String>(), acceptVehicleTypes, new TimeOptions());
    private Session walkSession;
    private Session transportSession;

    WritableArray currentRouteInfo = Arguments.createArray();
    WritableArray routes = Arguments.createArray();

    private MasstransitRouter masstransitRouter = TransportFactory.getInstance().createMasstransitRouter();
    private PedestrianRouter pedestrianRouter = TransportFactory.getInstance().createPedestrianRouter();

    public YaMapView(Context context, @Nullable ImageProvider selectedMarkerIcon, @Nullable ImageProvider markerIcon) {
        super(context);
        this.selectedMarkerIcon = selectedMarkerIcon;
        this.markerIcon = markerIcon;
        UserLocationLayer userLocationLayer = this.getMap().getUserLocationLayer();
        userLocationLayer.setEnabled(true);
        userLocationLayer.setHeadingEnabled(true);
    }

    public void setRouteColors(ReadableMap colors) {

        if (colors == null) {
            return;
        }

        ReadableMapKeySetIterator iterator = colors.keySetIterator();
        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();

            if (!Arrays.asList("bus", "railway", "trolleybus", "tramway", "suburban",
                    "underground", "walk", "minibus").contains(key)) {
                throw new IllegalArgumentException("Only 'bus' | 'railway' | 'trolleybus' 'tramway' | 'suburban' | 'underground' | 'walk' | trolleybus vehicle types are supported");
            }

            ReadableType type = colors.getType(key);
            if (type != ReadableType.String) {
                throw new IllegalArgumentException("Color prop for \"" + key + "\" should have a String type");
            }

            vehicleColors.put(key, colors.getString(key));
        }
    }

    public void setAcceptVehicleTypes(ArrayList<String> acceptVehicleTypes) {
        this.acceptVehicleTypes = acceptVehicleTypes;
        setMarkers(lastKnownMarkers);

        if (acceptVehicleTypes.isEmpty()) {
            onReceiveNativeEvent(Arguments.createArray());
            return;
        }

        if (!lastKnownRoutePoints.isEmpty()) {
            if (acceptVehicleTypes.contains("walk")) {
                if (walkSession != null) {
                    walkSession.retry(this);
                } else {
                    walkSession = pedestrianRouter.requestRoutes(lastKnownRoutePoints, new TimeOptions(), this);
                }
            } else {
                if (transportSession != null) {
                    transportSession.retry(this);
                } else {
                    transportSession = masstransitRouter.requestRoutes(lastKnownRoutePoints, masstransitOptions.setAcceptTypes(acceptVehicleTypes), this);
                }
            }
        }
    }

    public void setCenter(Point location, float zoom) {
        getMap().move(new CameraPosition(location, zoom, 0.0F, 0.0F), new Animation(Animation.Type.SMOOTH, 0.8F), null);
        if (!lastKnownRoutePoints.isEmpty()) {
            removeAllSections();
            requestRoute(lastKnownRoutePoints);
        }
    }

    public void requestRoute(@NonNull ArrayList<RequestPoint> points) {
        lastKnownRoutePoints = points;
        if (acceptVehicleTypes.contains("walk")) {
            walkSession = pedestrianRouter.requestRoutes(points, new TimeOptions(), this);
            return;
        }
        transportSession = masstransitRouter.requestRoutes(points, masstransitOptions.setAcceptTypes(acceptVehicleTypes), this);
    }

    public void setMarkers(ArrayList<RNMarker> markers) {
        lastKnownMarkers = markers;
        MapObjectCollection objects = getMap().getMapObjects();
        objects.clear();
        for (final RNMarker marker : markers) {
            PlacemarkMapObject placemark = objects.addPlacemark(new Point(marker.lat, marker.lon));
            if (selectedMarkerIcon != null && this.markerIcon != null) {
                placemark.setIcon(marker.isSelected ? selectedMarkerIcon : this.markerIcon);
            }
            placemark.setIconStyle(new IconStyle().setScale(0.3f));
            placemark.addTapListener(new MapObjectTapListener() {
                @Override
                public boolean onMapObjectTap(@NonNull MapObject mapObject, @NonNull Point point) {
                    final Context context = getContext();
                    if (context instanceof ReactContext) {
                        WritableMap e = Arguments.createMap();
                        e.putString("id", marker.id);
                        ((ReactContext) context).getJSModule(RCTEventEmitter.class).receiveEvent(getId(), "onMarkerPress", e);
                    }
                    return true;
                }
            });
        }
    }

    @Override
    public void onMasstransitRoutes(@NonNull List<Route> routes) {
        if (routes.size() > 0) {
            // You need to check the routes and draw this route only if
            // there is at least one transport belonging to the acceptVehicleTypes list
            for (int i = 0; i < routes.size(); i++) {
                boolean isRouteBelongToAcceptedVehicleList = false;
                boolean isWalkRoute = true;
                for (Section section : routes.get(i).getSections()) {
                    if (section.getMetadata().getData().getTransports() != null) {
                        isWalkRoute = false;
                        for (Transport transport : section.getMetadata().getData().getTransports()) {
                            for (String type : transport.getLine().getVehicleTypes()) {
                                if (acceptVehicleTypes.contains(type)) {
                                    isRouteBelongToAcceptedVehicleList = true;
                                    break;
                                }
                            }
                        }
                    }
                }

                if (isRouteBelongToAcceptedVehicleList || isWalkRoute) {
                    for (Section section : routes.get(i).getSections()) {
                        drawSection(section, SubpolylineHelper.subpolyline(routes.get(i).getGeometry(), section.getGeometry()), routes.get(i).getMetadata().getWeight(), i);
                    }

                    this.routes.pushArray(currentRouteInfo);
                    currentRouteInfo = Arguments.createArray();
                }
            }
            onReceiveNativeEvent(this.routes);
            this.routes = Arguments.createArray();
        }

    }

    @Override
    public void onMasstransitRoutesError(@NonNull Error error) {
        // todo: implement error handling
        // f.e: emit error event to js
    }

    private void drawSection(final Section section, Polyline geometry, Weight routeWeight, int routeIndex) {

        if (acceptVehicleTypes.isEmpty()) {
            removeAllSections();
            return;
        }

        SectionMetadata.SectionData data = section.getMetadata().getData();
        PolylineMapObject polylineMapObject = getMap().getMapObjects().addCollection().addPolyline(geometry);

        WritableMap routeMetadata = Arguments.createMap();
        WritableMap routeWeightData = Arguments.createMap();
        WritableMap sectionWeightData = Arguments.createMap();

        Map<String, ArrayList<String>> transports = new HashMap<>();

        routeWeightData.putString("time", routeWeight.getTime().getText());
        routeWeightData.putInt("transferCount", routeWeight.getTransfersCount());
        routeWeightData.putDouble("walkingDistance", routeWeight.getWalkingDistance().getValue());

        sectionWeightData.putString("time", section.getMetadata().getWeight().getTime().getText());
        sectionWeightData.putInt("transferCount", section.getMetadata().getWeight().getTransfersCount());
        sectionWeightData.putDouble("walkingDistance", section.getMetadata().getWeight().getWalkingDistance().getValue());

        routeMetadata.putMap("sectionInfo", sectionWeightData);
        routeMetadata.putMap("routeInfo", routeWeightData);
        routeMetadata.putInt("routeIndex", routeIndex);

        final WritableArray stops = new WritableNativeArray();

        for (RouteStop stop : section.getStops()) {
            stops.pushString(stop.getStop().getName());
        }

        routeMetadata.putArray("stops", stops);

        if (data.getTransports() != null) {
            for (Transport transport : data.getTransports()) {
                Log.e("transport", transport.getLine().getName());

                for (String type : transport.getLine().getVehicleTypes()) {

                    if (type.equals("suburban")) continue;

                    if (transports.get(type) != null) {
                        ArrayList<String> list = transports.get(type);
                        if (list != null) {
                            list.add(transport.getLine().getName());
                            transports.put(type, list);
                        }
                    } else {
                        ArrayList<String> list = new ArrayList<>();
                        list.add(transport.getLine().getName());
                        transports.put(type, list);
                    }

                    routeMetadata.putString("type", type);

                    int color;
                    if (transportHasStyle(transport)) {
                        color = transport.getLine().getStyle().getColor() | 0xFF000000;
                    } else {
                        if (vehicleColors.containsKey(type)) {
                            color = Color.parseColor(vehicleColors.get(type));
                        } else {
                            color = Color.BLACK;
                        }
                    }

                    routeMetadata.putString("sectionColor", formatColor(color));

                    polylineMapObject.setStrokeColor(color);
                }
            }
        } else {
            setDashPolyline(polylineMapObject);
            routeMetadata.putString("sectionColor", formatColor(Color.DKGRAY));
            if (section.getMetadata().getWeight().getWalkingDistance().getValue() == 0) {
                routeMetadata.putString("type", "waiting");
            } else {
                routeMetadata.putString("type", "walk");
            }
        }

        WritableMap wTransports = Arguments.createMap();
        for (Map.Entry<String, ArrayList<String>> entry : transports.entrySet()) {
            wTransports.putArray(entry.getKey(), Arguments.fromList(entry.getValue()));
        }

        routeMetadata.putMap("transports", wTransports);
        currentRouteInfo.pushMap(routeMetadata);
    }

    private void removeAllSections() {
        getMap().getMapObjects().clear();
        setMarkers(lastKnownMarkers);
    }

    public void onReceiveNativeEvent(WritableArray routes) {
        WritableMap event = Arguments.createMap();
        event.putArray("routes", routes);
        ReactContext reactContext = (ReactContext) getContext();
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(getId(), "routes", event);
    }

    private boolean transportHasStyle(Transport transport) {
        return transport.getLine().getStyle() != null;
    }

    private void setDashPolyline(PolylineMapObject polylineMapObject) {
        polylineMapObject.setDashLength(8f);
        polylineMapObject.setGapLength(11f);
        polylineMapObject.setStrokeColor(Color.parseColor(vehicleColors.get("walk")));
        polylineMapObject.setStrokeWidth(2f);
    }

    private String formatColor(int color) {
        return String.format("#%06X", (0xFFFFFF & color));
    }
}
