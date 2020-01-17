package ru.vvdev.yamap.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.View;

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
import com.yandex.mapkit.geometry.BoundingBox;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.geometry.Polyline;
import com.yandex.mapkit.geometry.SubpolylineHelper;
import com.yandex.mapkit.layers.ObjectEvent;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.map.PolygonMapObject;
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
import com.yandex.mapkit.user_location.UserLocationObjectListener;
import com.yandex.mapkit.user_location.UserLocationView;
import com.yandex.runtime.Error;
import com.yandex.runtime.image.ImageProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import ru.vvdev.yamap.models.ReactMapObject;
import ru.vvdev.yamap.utils.Callback;
import ru.vvdev.yamap.utils.ImageLoader;

public class YamapView extends MapView implements Session.RouteListener, UserLocationObjectListener {

    // default colors for known vehicles
    // "underground" actually get color considering with his own branch"s color
    private final static Map<String, String> DEFAULT_VEHICLE_COLORS = new HashMap<String, String>() {{
        put("bus", "#59ACFF");
        put("railway", "#F8634F");
        put("tramway", "#C86DD7");
        put("suburban", "#3023AE");
        put("underground", "#BDCCDC");
        put("trolleybus", "#55CfDC");
        put("walk", "#333333");
    }};
    private Map<String, String> vehicleColors = DEFAULT_VEHICLE_COLORS;

    private String userLocationIcon = "";
    private Bitmap userLocationBitmap = null;

    private ArrayList<String> acceptVehicleTypes = new ArrayList<>();
    private ArrayList<RequestPoint> lastKnownRoutePoints = new ArrayList<>();
    private MasstransitOptions masstransitOptions = new MasstransitOptions(new ArrayList<String>(), acceptVehicleTypes, new TimeOptions());
    private Session walkSession;
    private Session transportSession;

    WritableArray currentRouteInfo = Arguments.createArray();
    WritableArray routes = Arguments.createArray();

    private MasstransitRouter masstransitRouter = TransportFactory.getInstance().createMasstransitRouter();
    private PedestrianRouter pedestrianRouter = TransportFactory.getInstance().createPedestrianRouter();

    private List<ReactMapObject> childs = new ArrayList<>();

    // location
    private UserLocationView userLocationView = null;

    public YamapView(Context context) {
        super(context);
        initUserLocationLayer();
    }

    private void initUserLocationLayer() {
        UserLocationLayer userLocationLayer = getMap().getUserLocationLayer();
        userLocationLayer.setObjectListener(this);
        userLocationLayer.setEnabled(true);
        userLocationLayer.setHeadingEnabled(true);
    }

    // ref methods
    public void setCenter(Point location, float zoom) {
        // todo[0]: добавить параметры анимации
        getMap().move(new CameraPosition(location, zoom, 0.0F, 0.0F), new Animation(Animation.Type.SMOOTH, 1.8F), null);
    }

    public void fitAllMarkers() {
        ArrayList<Point> lastKnownMarkers = new ArrayList<>();
        for (int i = 0; i < childs.size(); ++i) {
            ReactMapObject obj = childs.get(i);
            if (obj instanceof YamapMarker) {
                YamapMarker marker = (YamapMarker) obj;
                lastKnownMarkers.add(marker.point);
            }
        }
        // todo[0]: добавить параметры анимации и дефолтного зума (для одного маркера)
        if (lastKnownMarkers.size() == 0) {
            return;
        }
        if (lastKnownMarkers.size() == 1) {
            Point center = new Point(lastKnownMarkers.get(0).getLatitude(), lastKnownMarkers.get(0).getLongitude());
            getMap().move(new CameraPosition(center, 15, 0, 0));
            return;
        }
        double minLon = lastKnownMarkers.get(0).getLongitude();
        double maxLon = lastKnownMarkers.get(0).getLongitude();
        double minLat = lastKnownMarkers.get(0).getLatitude();
        double maxLat = lastKnownMarkers.get(0).getLatitude();
        for (int i = 0; i < lastKnownMarkers.size(); i++) {
            if (lastKnownMarkers.get(i).getLongitude() > maxLon) {
                maxLon = lastKnownMarkers.get(i).getLongitude();
            }
            if (lastKnownMarkers.get(i).getLongitude() < minLon) {
                minLon = lastKnownMarkers.get(i).getLongitude();
            }
            if (lastKnownMarkers.get(i).getLatitude() > maxLat) {
                maxLat = lastKnownMarkers.get(i).getLatitude();
            }
            if (lastKnownMarkers.get(i).getLatitude() < minLat) {
                minLat = lastKnownMarkers.get(i).getLatitude();
            }
        }
        Point southWest = new Point(minLat, minLon);
        Point northEast = new Point(maxLat, maxLon);

        BoundingBox boundingBox = new BoundingBox(southWest, northEast);
        CameraPosition cameraPosition = getMap().cameraPosition(boundingBox);
        cameraPosition = new CameraPosition(cameraPosition.getTarget(), cameraPosition.getZoom() - 0.8f, cameraPosition.getAzimuth(), cameraPosition.getTilt());
        getMap().move(cameraPosition, new Animation(Animation.Type.SMOOTH, 0.7f), null);
    }

    // props
    public void setUserLocationIcon(final String iconSource) {
        // todo[0]: можно устанавливать разные иконки на покой и движение. Дополнительно можно устанавливать стиль иконки, например scale
        userLocationIcon = iconSource;
        ImageLoader.DownloadImageBitmap(getContext(), iconSource, new Callback<Bitmap>() {
            @Override
            public void invoke(Bitmap bitmap) {
                if (iconSource.equals(userLocationIcon)) {
                    userLocationBitmap = bitmap;
                    updateUserLocationIcon();
                }
            }
        });
    }

    public void setRouteColors(ReadableMap colors) {
        // todo[1]: RERENDER!!!
        // todo[1]: ставить дефолтный если не передан конкретный транспорт. Логично делать в js
        if (colors == null) {
            vehicleColors = DEFAULT_VEHICLE_COLORS;
            return;
        }
        ReadableMapKeySetIterator iterator = colors.keySetIterator();
        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            ReadableType type = colors.getType(key);
            if (type != ReadableType.String) {
                throw new IllegalArgumentException("Color prop for \"" + key + "\" should have a String type");
            }
            vehicleColors.put(key, colors.getString(key));
        }
    }

    public void setAcceptVehicleTypes(ArrayList<String> _acceptVehicleTypes) {
        acceptVehicleTypes = _acceptVehicleTypes;
        removeAllSections();
        if (acceptVehicleTypes.isEmpty()) {
            onRoutesFound(Arguments.createArray());
            return;
        }
        if (!lastKnownRoutePoints.isEmpty()) {
            requestRoute(lastKnownRoutePoints);
        }
    }

    public void removeRoute() {
        lastKnownRoutePoints = null;
        removeAllSections();
    }

    public void requestRoute(@Nonnull ArrayList<RequestPoint> points) {
        // todo[1] - все равно не надежно - мог произойти запрос маршрута, затем запрос нового, пока старый еще не найден. Тогда будут найдены и отрисованы оба маршрута
        // todo[2] - нужно делать через ref. Запрос маршрута -> проброс найденых вариантов в js -> запрос из js нарисовать маршруты по id. Удалять аналогично
        lastKnownRoutePoints = points;
        removeAllSections();
        if (acceptVehicleTypes.size() > 0) {
            if (acceptVehicleTypes.contains("walk")) {
                walkSession = pedestrianRouter.requestRoutes(points, new TimeOptions(), this);
                return;
            }
            transportSession = masstransitRouter.requestRoutes(points, masstransitOptions.setAcceptTypes(acceptVehicleTypes), this);
        }
    }

    @Override
    public void onMasstransitRoutes(@Nonnull List<Route> routes) {
        if (routes.size() > 0) {
            if (acceptVehicleTypes.contains("walk")) {
                processRoute(routes.get(0), 0);
            } else {
                for (int i = 0; i < routes.size(); i++) {
                    processRoute(routes.get(i), i);
                }
            }
            onRoutesFound(this.routes);
            this.routes = Arguments.createArray();
        }
    }

    private void processRoute(Route route, int index) {
        // You need to check the routes and draw this route only if
        // there is at least one transport belonging to the acceptVehicleTypes list
        boolean isRouteBelongToAcceptedVehicleList = false;
        boolean isWalkRoute = true;

        for (Section section : route.getSections()) {
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
            for (Section section : route.getSections()) {
                drawSection(section, SubpolylineHelper.subpolyline(route.getGeometry(),
                        section.getGeometry()), route.getMetadata().getWeight(), index);
            }

            this.routes.pushArray(currentRouteInfo);
            currentRouteInfo = Arguments.createArray();
        }
    }

    @Override
    public void onMasstransitRoutesError(@Nonnull Error error) {
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
        // todo: удалять только секции
        // todo: вынести clear в отдельный метод, чтобы чистить одновременно
        getMap().getMapObjects().clear();
    }

    public void onRoutesFound(WritableArray routes) {
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

    // children
    public void addFeature(View child, int index) {
        if (child instanceof YamapPolygon) {
            YamapPolygon _child = (YamapPolygon) child;
            PolygonMapObject obj = getMap().getMapObjects().addPolygon(_child.polygon);
            _child.setMapObject(obj);
            childs.add(_child);
        } else if (child instanceof YamapPolyline) {
            YamapPolyline _child = (YamapPolyline) child;
            PolylineMapObject obj = getMap().getMapObjects().addPolyline(_child.polyline);
            _child.setMapObject(obj);
            childs.add(_child);
        } else if (child instanceof YamapMarker) {
            YamapMarker _child = (YamapMarker) child;
            PlacemarkMapObject obj = getMap().getMapObjects().addPlacemark(_child.point);
            _child.setMapObject(obj);
            childs.add(_child);
        }
    }

    public void removeChild(int index) {
        if (index < childs.size()) {
            ReactMapObject child = childs.remove(index);
            getMap().getMapObjects().remove(child.getMapObject());
        }
    }

    // location listener implementation
    @Override
    public void onObjectAdded(@Nonnull UserLocationView _userLocationView) {
        userLocationView = _userLocationView;
        updateUserLocationIcon();
    }
    @Override
    public void onObjectRemoved(@Nonnull UserLocationView userLocationView) {
    }

    @Override
    public void onObjectUpdated(@Nonnull UserLocationView _userLocationView, @Nonnull ObjectEvent objectEvent) {
        userLocationView = _userLocationView;
        updateUserLocationIcon();
    }

    private void updateUserLocationIcon() {
        if (userLocationView != null && userLocationBitmap != null) {
            PlacemarkMapObject pin = userLocationView.getPin();
            PlacemarkMapObject arrow = userLocationView.getArrow();
            if (userLocationBitmap != null) {
                pin.setIcon(ImageProvider.fromBitmap(userLocationBitmap));
                arrow.setIcon(ImageProvider.fromBitmap(userLocationBitmap));
            }
        }
    }
}
