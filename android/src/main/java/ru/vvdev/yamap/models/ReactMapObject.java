package ru.vvdev.yamap.models;

import com.yandex.mapkit.map.MapObject;

public interface ReactMapObject {
    MapObject getMapObject();
    void setMapObject(MapObject obj);
}
