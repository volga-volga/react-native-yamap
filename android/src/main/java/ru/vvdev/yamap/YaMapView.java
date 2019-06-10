package ru.vvdev.yamap;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
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
import com.yandex.mapkit.transport.masstransit.Route;
import com.yandex.mapkit.transport.masstransit.Section;
import com.yandex.mapkit.transport.masstransit.SectionMetadata;
import com.yandex.mapkit.transport.masstransit.Session;
import com.yandex.mapkit.transport.masstransit.TimeOptions;
import com.yandex.mapkit.transport.masstransit.Transport;
import com.yandex.mapkit.user_location.UserLocationLayer;
import com.yandex.runtime.Error;
import com.yandex.runtime.image.ImageProvider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class YaMapView extends MapView implements Session.RouteListener {

    private ImageProvider selectedMarker;
    private ImageProvider marker;

    private MapObjectCollection mapObjects;

    public YaMapView(Context context, @Nullable ImageProvider selectedMarker, @Nullable ImageProvider marker) {
        super(context);
        this.selectedMarker = selectedMarker;
        this.marker = marker;
        UserLocationLayer userLocationLayer = this.getMap().getUserLocationLayer();
        userLocationLayer.setEnabled(true);
        userLocationLayer.setHeadingEnabled(true);
    }

    public void setCenter(Point location, float zoom) {
        getMap().move(new CameraPosition(location, zoom, 0.0F, 0.0F), new Animation(Animation.Type.SMOOTH, 0.5F), null);
    }

    public void requestRoute(@NonNull ArrayList<RequestPoint> points) {
        mapObjects = getMap().getMapObjects().addCollection();
        MasstransitRouter mtRouter = TransportFactory.getInstance().createMasstransitRouter();
        MasstransitOptions options = new MasstransitOptions(
                new ArrayList<String>(),
                new ArrayList<String>(),
                new TimeOptions());
        mtRouter.requestRoutes(points, options, this);
    }

    public void setMarkers(ArrayList<RNMarker> markers) {
        MapObjectCollection objects = getMap().getMapObjects();
        objects.clear();
        for (final RNMarker marker : markers) {
            PlacemarkMapObject placemark = objects.addPlacemark(new Point(marker.lat, marker.lon));
            if (selectedMarker != null && this.marker != null) {
                placemark.setIcon(marker.isSelected ? selectedMarker : this.marker);
            }
            placemark.setIconStyle(new IconStyle().setScale(0.3f));
            placemark.addTapListener(new MapObjectTapListener() {
                @Override
                public boolean onMapObjectTap(@NonNull MapObject mapObject, @NonNull Point point) {
                    final Context context = getContext();
                    if (context instanceof ReactContext) {
                        WritableMap e = Arguments.createMap();
                        e.putString("id", marker.id);
                        ((ReactContext) context).getJSModule(RCTEventEmitter.class)
                                .receiveEvent(getId(),
                                        "onMarkerPress", e);
                    }
                    return true;
                }
            });
        }
    }

    @Override
    public void onMasstransitRoutes(@NonNull List<Route> routes) {
        if (routes.size() > 0) {
            for (Route route : routes) {
                for (Section section : route.getSections()) {
                    drawSection(section.getMetadata().getData(), SubpolylineHelper.subpolyline(route.getGeometry(), section.getGeometry()));
                }
            }
        }

    }

    @Override
    public void onMasstransitRoutesError(@NonNull Error error) {
        // todo: implement error handling
        // f.e: emit error event to js
    }

    private void drawSection(SectionMetadata.SectionData data, Polyline geometry) {
        PolylineMapObject polylineMapObject = mapObjects.addPolyline(geometry);

        if (data.getTransports() == null) {
            polylineMapObject.setStrokeColor(0xFF313131);
        } else {
            HashSet<String> knownVehicleTypes = new HashSet<>();
            knownVehicleTypes.add("bus");
            for (Transport transport : data.getTransports()) {
                String sectionVehicleType = getVehicleType(transport, knownVehicleTypes);
                if (sectionVehicleType != null) {
                    if (sectionVehicleType.equals("bus")) {
                        polylineMapObject.setStrokeColor(0xFFF8634F);
                        return;
                    }
                }
            }

            polylineMapObject.setStrokeColor(0xFF59ACFF);
        }
    }

    private String getVehicleType(Transport transport, HashSet<String> knownVehicleTypes) {

        for (String type : transport.getLine().getVehicleTypes()) {
            if (knownVehicleTypes.contains(type)) {
                return type;
            }
        }
        return null;
    }
}
