package ru.vvdev.yamap.models;

public class RNMarker {
    public double lon;
    public double lat;
    public String id;
    public Integer zIndex;
    public String uri;
    public RNMarker(double _lon, double _lat, String _id, Integer _zIndex, String _uri) {
        lon = _lon;
        lat = _lat;
        id = _id;
        zIndex = _zIndex;
        uri = _uri;
    }
}
