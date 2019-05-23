package ru.vvdev.yamap;

import android.app.AlertDialog;
import android.content.Context;
import android.support.annotation.NonNull;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.IconStyle;
import com.yandex.mapkit.map.MapObject;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.MapObjectTapListener;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.user_location.UserLocationLayer;
import com.yandex.runtime.image.ImageProvider;

import java.util.ArrayList;

public class YaMapView extends MapView {
    private ImageProvider selectedMarker;
    private ImageProvider marker;

    public YaMapView(Context context, ImageProvider selectedMarker, ImageProvider marker) {
        super(context);
        this.selectedMarker = selectedMarker;
        this.marker = marker;
        UserLocationLayer userLocationLayer = this.getMap().getUserLocationLayer();
        userLocationLayer.setEnabled(true);
        userLocationLayer.setHeadingEnabled(true);
    }

    public void setCenter(Point location, float zoom) {
        getMap().move(
                new CameraPosition(location, zoom, 0.0f, 0.0f),
                new Animation(Animation.Type.SMOOTH, 2),
                null);
    }
    public void setMarkers(ArrayList<RNMarker> markers) {
        MapObjectCollection objects = getMap().getMapObjects();
        objects.clear();
        for (final RNMarker marker : markers) {
            PlacemarkMapObject placemark = objects.addPlacemark(new Point(marker.lat, marker.lon));
            placemark.setIcon(marker.isSelected ? selectedMarker : this.marker);
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
}
