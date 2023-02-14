package ru.vvdev.yamap.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.view.View;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.yandex.mapkit.ScreenPoint;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.RequestPoint;
import com.yandex.mapkit.RequestPointType;
import com.yandex.mapkit.directions.DirectionsFactory;
import com.yandex.mapkit.directions.driving.DrivingOptions;
import com.yandex.mapkit.directions.driving.DrivingRoute;
import com.yandex.mapkit.directions.driving.DrivingRouter;
import com.yandex.mapkit.directions.driving.DrivingSection;
import com.yandex.mapkit.directions.driving.DrivingSession;
import com.yandex.mapkit.directions.driving.VehicleOptions;
import com.yandex.mapkit.geometry.BoundingBox;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.geometry.Polyline;
import com.yandex.mapkit.geometry.SubpolylineHelper;
import com.yandex.mapkit.layers.ObjectEvent;
import com.yandex.mapkit.map.CameraListener;
import com.yandex.mapkit.map.MapLoadStatistics;
import com.yandex.mapkit.map.MapLoadedListener;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.CameraUpdateReason;
import com.yandex.mapkit.map.CircleMapObject;
import com.yandex.mapkit.map.ClusterizedPlacemarkCollection;
import com.yandex.mapkit.map.IconStyle;
import com.yandex.mapkit.map.InputListener;
import com.yandex.mapkit.map.MapObject;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.map.PolygonMapObject;
import com.yandex.mapkit.map.PolylineMapObject;
import com.yandex.mapkit.map.VisibleRegion;
import com.yandex.mapkit.map.MapType;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.logo.Alignment;
import com.yandex.mapkit.logo.Padding;
import com.yandex.mapkit.logo.HorizontalAlignment;
import com.yandex.mapkit.logo.VerticalAlignment;
import com.yandex.mapkit.transport.TransportFactory;
import com.yandex.mapkit.transport.masstransit.FilterVehicleTypes;
import com.yandex.mapkit.transport.masstransit.MasstransitRouter;
import com.yandex.mapkit.transport.masstransit.PedestrianRouter;
import com.yandex.mapkit.transport.masstransit.Route;
import com.yandex.mapkit.transport.masstransit.RouteStop;
import com.yandex.mapkit.transport.masstransit.Section;
import com.yandex.mapkit.transport.masstransit.SectionMetadata;
import com.yandex.mapkit.transport.masstransit.Session;
import com.yandex.mapkit.transport.masstransit.TimeOptions;
import com.yandex.mapkit.transport.masstransit.TransitOptions;
import com.yandex.mapkit.transport.masstransit.Transport;
import com.yandex.mapkit.transport.masstransit.Weight;
import com.yandex.mapkit.user_location.UserLocationLayer;
import com.yandex.mapkit.user_location.UserLocationObjectListener;
import com.yandex.mapkit.user_location.UserLocationView;
import com.yandex.runtime.Error;
import com.yandex.runtime.image.ImageProvider;
import com.yandex.mapkit.traffic.TrafficLayer;
import com.yandex.mapkit.traffic.TrafficListener;
import com.yandex.mapkit.traffic.TrafficLevel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ru.vvdev.yamap.models.ReactMapObject;
import ru.vvdev.yamap.utils.Callback;
import ru.vvdev.yamap.utils.ImageLoader;
import ru.vvdev.yamap.utils.RouteManager;

public class YamapView extends MapView implements UserLocationObjectListener, CameraListener, InputListener, TrafficListener, MapLoadedListener {
    private final static Map<String, String> DEFAULT_VEHICLE_COLORS = new HashMap<String, String>() {{
        put("bus", "#59ACFF");
        put("railway", "#F8634F");
        put("tramway", "#C86DD7");
        put("suburban", "#3023AE");
        put("underground", "#BDCCDC");
        put("trolleybus", "#55CfDC");
        put("walk", "#333333");
    }};
    private String userLocationIcon = "";
    private float userLocationIconScale = 1.f;
    private Bitmap userLocationBitmap = null;
    private RouteManager routeMng = new RouteManager();
    private MasstransitRouter masstransitRouter = TransportFactory.getInstance().createMasstransitRouter();
    private DrivingRouter drivingRouter;
    private ClusterizedPlacemarkCollection clusterCollection;
    private PedestrianRouter pedestrianRouter = TransportFactory.getInstance().createPedestrianRouter();
    private UserLocationLayer userLocationLayer = null;
    private int userLocationAccuracyFillColor = 0;
    private int userLocationAccuracyStrokeColor = 0;
    private float userLocationAccuracyStrokeWidth = 0.f;
    private TrafficLayer trafficLayer = null;
    private float maxFps = 60;
    static private HashMap<String, ImageProvider> icons = new HashMap<>();

    void setImage(final String iconSource, final PlacemarkMapObject mapObject, final IconStyle iconStyle) {
        if (icons.get(iconSource)==null) {
            ImageLoader.DownloadImageBitmap(getContext(), iconSource, new Callback<Bitmap>() {
                @Override
                public void invoke(Bitmap bitmap) {
                    try {
                        if (mapObject != null) {
                            ImageProvider icon = ImageProvider.fromBitmap(bitmap);
                            icons.put(iconSource, icon);
                            mapObject.setIcon(icon);
                            mapObject.setIconStyle(iconStyle);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            mapObject.setIcon(Objects.requireNonNull(icons.get(iconSource)));
            mapObject.setIconStyle(iconStyle);
        }
    }

    private UserLocationView userLocationView = null;

    public YamapView(Context context) {
        super(context);
        DirectionsFactory.initialize(context);
        drivingRouter = DirectionsFactory.getInstance().createDrivingRouter();
        getMap().addCameraListener(this);
        getMap().addInputListener(this);
        getMap().setMapLoadedListener(this);
    }

    // REF
    public void setCenter(CameraPosition position, float duration, int animation) {
        if (duration > 0) {
            Animation.Type anim = animation == 0 ? Animation.Type.SMOOTH : Animation.Type.LINEAR;
            getMap().move(position, new Animation(anim, duration), null);
        } else {
            getMap().move(position);
        }
    }

    private WritableMap positionToJSON(CameraPosition position, CameraUpdateReason reason, boolean finished) {
        WritableMap cameraPosition = Arguments.createMap();
        Point point = position.getTarget();
        cameraPosition.putDouble("azimuth", position.getAzimuth());
        cameraPosition.putDouble("tilt", position.getTilt());
        cameraPosition.putDouble("zoom", position.getZoom());
        WritableMap target = Arguments.createMap();
        target.putDouble("lat", point.getLatitude());
        target.putDouble("lon", point.getLongitude());
        cameraPosition.putMap("point", target);
        cameraPosition.putString("reason", reason.toString());
        cameraPosition.putBoolean("finished", finished);

        return cameraPosition;
    }

    private WritableMap screenPointToJSON(ScreenPoint screenPoint) {
        WritableMap result = Arguments.createMap();

        result.putDouble("x", (float) screenPoint.getX());
        result.putDouble("y", (float) screenPoint.getY());

        return result;
    }

    private WritableMap worldPointToJSON(Point worldPoint) {
        WritableMap result = Arguments.createMap();

        result.putDouble("lat", worldPoint.getLatitude());
        result.putDouble("lon", worldPoint.getLongitude());

        return result;
    }

    private WritableMap visibleRegionToJSON(VisibleRegion region) {
        WritableMap result = Arguments.createMap();

        WritableMap bl = Arguments.createMap();
        bl.putDouble("lat", region.getBottomLeft().getLatitude());
        bl.putDouble("lon", region.getBottomLeft().getLongitude());
        result.putMap("bottomLeft", bl);

        WritableMap br = Arguments.createMap();
        br.putDouble("lat", region.getBottomRight().getLatitude());
        br.putDouble("lon", region.getBottomRight().getLongitude());
        result.putMap("bottomRight", br);

        WritableMap tl = Arguments.createMap();
        tl.putDouble("lat", region.getTopLeft().getLatitude());
        tl.putDouble("lon", region.getTopLeft().getLongitude());
        result.putMap("topLeft", tl);

        WritableMap tr = Arguments.createMap();
        tr.putDouble("lat", region.getTopRight().getLatitude());
        tr.putDouble("lon", region.getTopRight().getLongitude());
        result.putMap("topRight", tr);

        return result;
    }

    public void emitCameraPositionToJS(String id) {
        CameraPosition position = getMap().getCameraPosition();
        WritableMap cameraPosition = positionToJSON(position, CameraUpdateReason.valueOf("APPLICATION"), true);
        cameraPosition.putString("id", id);
        ReactContext reactContext = (ReactContext) getContext();
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(getId(), "cameraPosition", cameraPosition);
    }

    public void emitVisibleRegionToJS(String id) {
        VisibleRegion visibleRegion = getMap().getVisibleRegion();
        WritableMap result = visibleRegionToJSON(visibleRegion);
        result.putString("id", id);
        ReactContext reactContext = (ReactContext) getContext();
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(getId(), "visibleRegion", result);
    }

    public void emitWorldToScreenPoints(ReadableArray worldPoints, String id) {
        WritableArray screenPoints = Arguments.createArray();

        for (int i = 0; i < worldPoints.size(); ++i) {
            ReadableMap p = worldPoints.getMap(i);
            Point worldPoint = new Point(p.getDouble("lat"), p.getDouble("lon"));
            ScreenPoint screenPoint = getMapWindow().worldToScreen(worldPoint);
            screenPoints.pushMap(screenPointToJSON(screenPoint));
        }

        WritableMap result = Arguments.createMap();
        result.putString("id", id);
        result.putArray("screenPoints", screenPoints);

        ReactContext reactContext = (ReactContext) getContext();
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(getId(), "worldToScreenPoints", result);
    }

    public void emitScreenToWorldPoints(ReadableArray screenPoints, String id) {
        WritableArray worldPoints = Arguments.createArray();

        for (int i = 0; i < screenPoints.size(); ++i) {
            ReadableMap p = screenPoints.getMap(i);
            ScreenPoint screenPoint = new ScreenPoint((float) p.getDouble("x"), (float) p.getDouble("y"));
            Point worldPoint = getMapWindow().screenToWorld(screenPoint);
            worldPoints.pushMap(worldPointToJSON(worldPoint));
        }

        WritableMap result = Arguments.createMap();
        result.putString("id", id);
        result.putArray("worldPoints", worldPoints);

        ReactContext reactContext = (ReactContext) getContext();
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(getId(), "screenToWorldPoints", result);
    }

    public void setZoom(Float zoom, float duration, int animation) {
        CameraPosition prevPosition = getMap().getCameraPosition();
        CameraPosition position = new CameraPosition(prevPosition.getTarget(), zoom, prevPosition.getAzimuth(), prevPosition.getTilt());
        setCenter(position, duration, animation);
    }

    public void findRoutes(ArrayList<Point> points, final ArrayList<String> vehicles, final String id) {
        final YamapView self = this;
        if (vehicles.size() == 1 && vehicles.get(0).equals("car")) {
            DrivingSession.DrivingRouteListener listener = new DrivingSession.DrivingRouteListener() {
                @Override
                public void onDrivingRoutes(@NonNull List<DrivingRoute> routes) {
                    WritableArray jsonRoutes = Arguments.createArray();
                    for (int i = 0; i < routes.size(); ++i) {
                        DrivingRoute _route = routes.get(i);
                        WritableMap jsonRoute = Arguments.createMap();
                        String id = RouteManager.generateId();
                        jsonRoute.putString("id", id);
                        WritableArray sections = Arguments.createArray();
                        for (DrivingSection section : _route.getSections()) {
                            WritableMap jsonSection = convertDrivingRouteSection(_route, section, i);
                            sections.pushMap(jsonSection);
                        }
                        jsonRoute.putArray("sections", sections);
                        jsonRoutes.pushMap(jsonRoute);
                    }
                    self.onRoutesFound(id, jsonRoutes, "success");
                }

                @Override
                public void onDrivingRoutesError(@NonNull Error error) {
                    self.onRoutesFound(id, Arguments.createArray(), "error");
                }
            };
            ArrayList<RequestPoint> _points = new ArrayList<>();
            for (int i = 0; i < points.size(); ++i) {
                Point point = points.get(i);
                RequestPoint _p = new RequestPoint(point, RequestPointType.WAYPOINT, null);
                _points.add(_p);
            }
            drivingRouter.requestRoutes(_points, new DrivingOptions(), new VehicleOptions(), listener);
            return;
        }
        ArrayList<RequestPoint> _points = new ArrayList<>();
        for (int i = 0; i < points.size(); ++i) {
            Point point = points.get(i);
            _points.add(new RequestPoint(point, RequestPointType.WAYPOINT, null));
        }
        Session.RouteListener listener = new Session.RouteListener() {
            @Override
            public void onMasstransitRoutes(@NonNull List<Route> routes) {
                WritableArray jsonRoutes = Arguments.createArray();
                for (int i = 0; i < routes.size(); ++i) {
                    Route _route = routes.get(i);
                    WritableMap jsonRoute = Arguments.createMap();
                    String id = RouteManager.generateId();
                    self.routeMng.saveRoute(_route, id);
                    jsonRoute.putString("id", id);
                    WritableArray sections = Arguments.createArray();
                    for (Section section : _route.getSections()) {
                        WritableMap jsonSection = convertRouteSection(_route, section, SubpolylineHelper.subpolyline(_route.getGeometry(),
                                section.getGeometry()), _route.getMetadata().getWeight(), i);
                        sections.pushMap(jsonSection);
                    }
                    jsonRoute.putArray("sections", sections);
                    jsonRoutes.pushMap(jsonRoute);
                }
                self.onRoutesFound(id, jsonRoutes, "success");
            }

            @Override
            public void onMasstransitRoutesError(@NonNull Error error) {
                self.onRoutesFound(id, Arguments.createArray(), "error");
            }
        };
        if (vehicles.size() == 0) {
            pedestrianRouter.requestRoutes(_points, new TimeOptions(), listener);
            return;
        }
        TransitOptions transitOptions = new TransitOptions(FilterVehicleTypes.NONE.value, new TimeOptions());
        masstransitRouter.requestRoutes(_points, transitOptions, listener);
    }

    public void fitAllMarkers() {
        ArrayList<Point> points = new ArrayList<>();
        for (int i = 0; i < getChildCount(); ++i) {
            Object obj = getChildAt(i);
            if (obj instanceof YamapMarker) {
                YamapMarker marker = (YamapMarker) obj;
                points.add(marker.point);
            }
        }
        fitMarkers(points);
    }

    private ArrayList<Point> mapPlacemarksToPoints(List<PlacemarkMapObject> placemarks) {
        ArrayList<Point> points = new ArrayList<Point>();

        for (int i = 0; i < placemarks.size(); ++i) {
            points.add(placemarks.get(i).getGeometry());
        }

        return points;
    }

    BoundingBox calculateBoundingBox(ArrayList<Point> points) {
        double minLon = points.get(0).getLongitude();
        double maxLon = points.get(0).getLongitude();
        double minLat = points.get(0).getLatitude();
        double maxLat = points.get(0).getLatitude();

        for (int i = 0; i < points.size(); i++) {
            if (points.get(i).getLongitude() > maxLon) {
                maxLon = points.get(i).getLongitude();
            }

            if (points.get(i).getLongitude() < minLon) {
                minLon = points.get(i).getLongitude();
            }

            if (points.get(i).getLatitude() > maxLat) {
                maxLat = points.get(i).getLatitude();
            }

            if (points.get(i).getLatitude() < minLat) {
                minLat = points.get(i).getLatitude();
            }
        }

        Point southWest = new Point(minLat, minLon);
        Point northEast = new Point(maxLat, maxLon);

        BoundingBox boundingBox = new BoundingBox(southWest, northEast);
        return boundingBox;
    }

    public void fitMarkers(ArrayList<Point> points) {
        if (points.size() == 0) {
            return;
        }
        if (points.size() == 1) {
            Point center = new Point(points.get(0).getLatitude(), points.get(0).getLongitude());
            getMap().move(new CameraPosition(center, 15, 0, 0));
            return;
        }
        CameraPosition cameraPosition = getMap().cameraPosition(calculateBoundingBox(points));
        cameraPosition = new CameraPosition(cameraPosition.getTarget(), cameraPosition.getZoom() - 0.8f, cameraPosition.getAzimuth(), cameraPosition.getTilt());
        getMap().move(cameraPosition, new Animation(Animation.Type.SMOOTH, 0.7f), null);
    }

    // PROPS
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

    public void setUserLocationIconScale(float scale) {
        userLocationIconScale = scale;
        updateUserLocationIcon();
    }

    public void setUserLocationAccuracyFillColor(int color) {
        userLocationAccuracyFillColor = color;
        updateUserLocationIcon();
    }

    public void setUserLocationAccuracyStrokeColor(int color) {
        userLocationAccuracyStrokeColor = color;
        updateUserLocationIcon();
    }

    public void setUserLocationAccuracyStrokeWidth(float width) {
        userLocationAccuracyStrokeWidth = width;
        updateUserLocationIcon();
    }

    public void setMapStyle(@Nullable String style) {
        if (style != null) {
            getMap().setMapStyle(style);
        }
    }

    public void setMapType(@Nullable String type) {
        if (type != null) {
            switch (type) {
                case "none":
                    getMap().setMapType(MapType.NONE);
                    break;

                case "raster":
                    getMap().setMapType(MapType.MAP);
                    break;

                default:
                    getMap().setMapType(MapType.VECTOR_MAP);
                    break;
            }
        }
    }

    public void setInitialRegion(@Nullable ReadableMap params) {
        if ((!params.hasKey("lat") || params.isNull("lat")) || (!params.hasKey("lon") && params.isNull("lon")))
            return;

        Float initialRegionZoom = 10.f;
        Float initialRegionAzimuth = 0.f;
        Float initialRegionTilt = 0.f;

        if (params.hasKey("zoom") && !params.isNull("zoom"))
            initialRegionZoom = (float) params.getDouble("zoom");

        if (params.hasKey("azimuth") && !params.isNull("azimuth"))
            initialRegionAzimuth = (float) params.getDouble("azimuth");

        if (params.hasKey("tilt") && !params.isNull("tilt"))
            initialRegionTilt = (float) params.getDouble("tilt");

        Point initialPosition = new Point(params.getDouble("lat"), params.getDouble("lon"));
        CameraPosition initialCameraPosition = new CameraPosition(initialPosition, initialRegionZoom, initialRegionAzimuth, initialRegionTilt);
        setCenter(initialCameraPosition, 0.f, 0);
    }

    public void setLogoPosition(@Nullable ReadableMap params) {
        HorizontalAlignment horizontalAlignment = HorizontalAlignment.RIGHT;
        VerticalAlignment verticalAlignment = VerticalAlignment.BOTTOM;

        if (params.hasKey("horizontal") && !params.isNull("horizontal")) {
            switch (params.getString("horizontal")) {
                case "left":
                    horizontalAlignment = HorizontalAlignment.LEFT;
                    break;

                case "center":
                    horizontalAlignment = HorizontalAlignment.CENTER;
                    break;

                default:
                    break;
            }
        }

        if (params.hasKey("vertical") && !params.isNull("vertical")) {
            switch (params.getString("vertical")) {
                case "top":
                    verticalAlignment = VerticalAlignment.TOP;
                    break;

                default:
                    break;
            }
        }

        getMap().getLogo().setAlignment(new Alignment(horizontalAlignment, verticalAlignment));
    }

    public void setLogoPadding(@Nullable ReadableMap params) {
        int horizontalPadding = (params.hasKey("horizontal") && !params.isNull("horizontal")) ? params.getInt("horizontal") : 0;
        int verticalPadding = (params.hasKey("vertical") && !params.isNull("vertical")) ? params.getInt("vertical") : 0;
        getMap().getLogo().setPadding(new Padding(horizontalPadding, verticalPadding));
    }

    public void setMaxFps(float fps) {
        maxFps = fps;
        getMapWindow().setMaxFps(maxFps);
    }

    public void setInteractive(boolean interactive) {
        setNoninteractive(!interactive);
    }

    public void setNightMode(Boolean nightMode) {
        getMap().setNightModeEnabled(nightMode);
    }

    public void setScrollGesturesEnabled(Boolean scrollGesturesEnabled) {
        getMap().setScrollGesturesEnabled(scrollGesturesEnabled);
    }

    public void setZoomGesturesEnabled(Boolean zoomGesturesEnabled) {
        getMap().setZoomGesturesEnabled(zoomGesturesEnabled);
    }

    public void setRotateGesturesEnabled(Boolean rotateGesturesEnabled) {
        getMap().setRotateGesturesEnabled(rotateGesturesEnabled);
    }

    public void setFastTapEnabled(Boolean fastTapEnabled) {
        getMap().setFastTapEnabled(fastTapEnabled);
    }

    public void setTiltGesturesEnabled(Boolean tiltGesturesEnabled) {
        getMap().setTiltGesturesEnabled(tiltGesturesEnabled);
    }

    public void setTrafficVisible(Boolean isVisible) {
        if (trafficLayer == null) {
            trafficLayer = MapKitFactory.getInstance().createTrafficLayer(getMapWindow());
        }

        if (isVisible) {
            trafficLayer.addTrafficListener(this);
            trafficLayer.setTrafficVisible(true);
        } else {
            trafficLayer.setTrafficVisible(false);
            trafficLayer.addTrafficListener(null);
        }
    }

    public void setShowUserPosition(Boolean show) {
        if (userLocationLayer == null) {
            userLocationLayer = MapKitFactory.getInstance().createUserLocationLayer(getMapWindow());
        }

        if (show) {
            userLocationLayer.setObjectListener(this);
            userLocationLayer.setVisible(true);
            userLocationLayer.setHeadingEnabled(true);
        } else {
            userLocationLayer.setVisible(false);
            userLocationLayer.setHeadingEnabled(false);
            userLocationLayer.setObjectListener(null);
        }
    }

    public void setFollowUser(Boolean follow) {
        if (userLocationLayer == null) {
        setShowUserPosition(true);
        }

        if(follow){
        userLocationLayer.setAutoZoomEnabled(true);
        userLocationLayer.setAnchor(
            new PointF((float)(getWidth() * 0.5), (float)(getHeight() * 0.5)),
            new PointF((float)(getWidth() * 0.5), (float)(getHeight() * 0.83)));
        }else{
        userLocationLayer.setAutoZoomEnabled(false);
        userLocationLayer.resetAnchor();
        }
    }

    private WritableMap convertRouteSection(Route route, final Section section, Polyline geometry, Weight routeWeight, int routeIndex) {
        SectionMetadata.SectionData data = section.getMetadata().getData();
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
            stops.pushString(stop.getMetadata().getStop().getName());
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
                    int color = Color.BLACK;
                    if (transportHasStyle(transport)) {
                        try {
                            color = transport.getLine().getStyle().getColor();
                        } catch (Exception ignored) {
                        }
                    }
                    routeMetadata.putString("sectionColor", formatColor(color));
                }
            }
        } else {
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
        Polyline subpolyline = SubpolylineHelper.subpolyline(route.getGeometry(), section.getGeometry());
        List<Point> linePoints = subpolyline.getPoints();
        WritableArray jsonPoints = Arguments.createArray();

        for (Point point : linePoints) {
            WritableMap jsonPoint = Arguments.createMap();
            jsonPoint.putDouble("lat", point.getLatitude());
            jsonPoint.putDouble("lon", point.getLongitude());
            jsonPoints.pushMap(jsonPoint);
        }

        routeMetadata.putArray("points", jsonPoints);

        return routeMetadata;
    }

    private WritableMap convertDrivingRouteSection(DrivingRoute route, final DrivingSection section, int routeIndex) {
        com.yandex.mapkit.directions.driving.Weight routeWeight = route.getMetadata().getWeight();
        WritableMap routeMetadata = Arguments.createMap();
        WritableMap routeWeightData = Arguments.createMap();
        WritableMap sectionWeightData = Arguments.createMap();
        Map<String, ArrayList<String>> transports = new HashMap<>();
        routeWeightData.putString("time", routeWeight.getTime().getText());
        routeWeightData.putString("timeWithTraffic", routeWeight.getTimeWithTraffic().getText());
        routeWeightData.putDouble("distance", routeWeight.getDistance().getValue());
        sectionWeightData.putString("time", section.getMetadata().getWeight().getTime().getText());
        sectionWeightData.putString("timeWithTraffic", section.getMetadata().getWeight().getTimeWithTraffic().getText());
        sectionWeightData.putDouble("distance", section.getMetadata().getWeight().getDistance().getValue());
        routeMetadata.putMap("sectionInfo", sectionWeightData);
        routeMetadata.putMap("routeInfo", routeWeightData);
        routeMetadata.putInt("routeIndex", routeIndex);
        final WritableArray stops = new WritableNativeArray();
        routeMetadata.putArray("stops", stops);
        routeMetadata.putString("sectionColor", formatColor(Color.DKGRAY));

        if (section.getMetadata().getWeight().getDistance().getValue() == 0) {
            routeMetadata.putString("type", "waiting");
        } else {
            routeMetadata.putString("type", "car");
        }

        WritableMap wTransports = Arguments.createMap();
        routeMetadata.putMap("transports", wTransports);
        Polyline subpolyline = SubpolylineHelper.subpolyline(route.getGeometry(), section.getGeometry());
        List<Point> linePoints = subpolyline.getPoints();
        WritableArray jsonPoints = Arguments.createArray();

        for (Point point : linePoints) {
            WritableMap jsonPoint = Arguments.createMap();
            jsonPoint.putDouble("lat", point.getLatitude());
            jsonPoint.putDouble("lon", point.getLongitude());
            jsonPoints.pushMap(jsonPoint);
        }

        routeMetadata.putArray("points", jsonPoints);

        return routeMetadata;
    }

    public void onRoutesFound(String id, WritableArray routes, String status) {
        WritableMap event = Arguments.createMap();
        event.putArray("routes", routes);
        event.putString("id", id);
        event.putString("status", status);
        ReactContext reactContext = (ReactContext) getContext();
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(getId(), "routes", event);
    }

    private boolean transportHasStyle(Transport transport) {
        return transport.getLine().getStyle() != null;
    }

    private String formatColor(int color) {
        return String.format("#%06X", (0xFFFFFF & color));
    }

    // CHILDREN
    public void addFeature(View child, int index) {
        if (child instanceof YamapPolygon) {
            YamapPolygon _child = (YamapPolygon) child;
            PolygonMapObject obj = getMap().getMapObjects().addPolygon(_child.polygon);
            _child.setMapObject(obj);
        } else if (child instanceof YamapPolyline) {
            YamapPolyline _child = (YamapPolyline) child;
            PolylineMapObject obj = getMap().getMapObjects().addPolyline(_child.polyline);
            _child.setMapObject(obj);
        } else if (child instanceof YamapMarker) {
            YamapMarker _child = (YamapMarker) child;
            PlacemarkMapObject obj = getMap().getMapObjects().addPlacemark(_child.point);
            _child.setMapObject(obj);
        } else if (child instanceof YamapCircle) {
            YamapCircle _child = (YamapCircle) child;
            CircleMapObject obj = getMap().getMapObjects().addCircle(_child.circle, 0, 0.f, 0);
            _child.setMapObject(obj);
        }
    }

    public void removeChild(int index) {
        if (getChildAt(index) instanceof ReactMapObject) {
            final ReactMapObject child = (ReactMapObject) getChildAt(index);
            if (child == null) return;
            final MapObject mapObject = child.getMapObject();
            if (mapObject == null || !mapObject.isValid()) return;

            getMap().getMapObjects().remove(mapObject);
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
        if (userLocationView != null) {
            IconStyle userIconStyle = new IconStyle();
            userIconStyle.setScale(userLocationIconScale);

            PlacemarkMapObject pin = userLocationView.getPin();
            PlacemarkMapObject arrow = userLocationView.getArrow();
            if (userLocationBitmap != null) {
                pin.setIcon(ImageProvider.fromBitmap(userLocationBitmap), userIconStyle);
                arrow.setIcon(ImageProvider.fromBitmap(userLocationBitmap), userIconStyle);
            }
            CircleMapObject circle = userLocationView.getAccuracyCircle();
            if (userLocationAccuracyFillColor != 0) {
                circle.setFillColor(userLocationAccuracyFillColor);
            }
            if (userLocationAccuracyStrokeColor != 0) {
                circle.setStrokeColor(userLocationAccuracyStrokeColor);
            }
            circle.setStrokeWidth(userLocationAccuracyStrokeWidth);
        }
    }

    @Override
    public void onCameraPositionChanged(@NonNull com.yandex.mapkit.map.Map map, @NonNull CameraPosition cameraPosition, CameraUpdateReason reason, boolean finished) {
        WritableMap positionStart = positionToJSON(cameraPosition, reason, finished);
        WritableMap positionFinish = positionToJSON(cameraPosition, reason, finished);
        ReactContext reactContext = (ReactContext) getContext();
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(getId(), "cameraPositionChange", positionStart);

        if (finished) {
            reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(getId(), "cameraPositionChangeEnd", positionFinish);
        }
    }

    @Override
    public void onMapTap(@NonNull com.yandex.mapkit.map.Map map, @NonNull Point point) {
        WritableMap data = Arguments.createMap();
        data.putDouble("lat", point.getLatitude());
        data.putDouble("lon", point.getLongitude());
        ReactContext reactContext = (ReactContext) getContext();
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(getId(), "onMapPress", data);
    }

    @Override
    public void onMapLongTap(@NonNull com.yandex.mapkit.map.Map map, @NonNull Point point) {
        WritableMap data = Arguments.createMap();
        data.putDouble("lat", point.getLatitude());
        data.putDouble("lon", point.getLongitude());
        ReactContext reactContext = (ReactContext) getContext();
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(getId(), "onMapLongPress", data);
    }

    @Override
    public void onMapLoaded(MapLoadStatistics statistics) {
        WritableMap data = Arguments.createMap();
        data.putInt("renderObjectCount",statistics.getRenderObjectCount());
        data.putDouble("curZoomModelsLoaded",statistics.getCurZoomModelsLoaded());
        data.putDouble("curZoomPlacemarksLoaded",statistics.getCurZoomPlacemarksLoaded());
        data.putDouble("curZoomLabelsLoaded",statistics.getCurZoomLabelsLoaded());
        data.putDouble("curZoomGeometryLoaded",statistics.getCurZoomGeometryLoaded());
        data.putDouble("tileMemoryUsage",statistics.getTileMemoryUsage());
        data.putDouble("delayedGeometryLoaded",statistics.getDelayedGeometryLoaded());
        data.putDouble("fullyAppeared",statistics.getFullyAppeared());
        data.putDouble("fullyLoaded",statistics.getFullyLoaded());
        ReactContext reactContext = (ReactContext) getContext();
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(getId(), "onMapLoaded",data);
    }

    //trafficListener implementation
    @Override
    public void onTrafficChanged(@Nullable TrafficLevel trafficLevel) {
    }

    @Override
    public void onTrafficLoading() {
    }

    @Override
    public void onTrafficExpired() {
    }
}
