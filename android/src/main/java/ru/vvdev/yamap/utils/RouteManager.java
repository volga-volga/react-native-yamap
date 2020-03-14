package ru.vvdev.yamap.utils;

import com.yandex.mapkit.map.MapObject;
import com.yandex.mapkit.transport.masstransit.Route;

import java.util.ArrayList;
import java.util.HashMap;

// todo: не используется
public class RouteManager {
    private HashMap<String, Route> data = new HashMap<>();
    private HashMap<String, ArrayList<MapObject>> mapObjects = new HashMap<>();

    public static String generateId() {
        return java.util.UUID.randomUUID().toString();
    }

    public void saveRoute(Route route, String id) {
        data.put(id, route);
    }

    public void putRouteMapObjects(String id, ArrayList<MapObject> objects) {
        mapObjects.put(id, objects);
    }

    public Route getRoute(String id) {
        return data.get(id);
    }

    public ArrayList<MapObject> getRouteMapObjects(String id) {
        return mapObjects.get(id);
    }
}
