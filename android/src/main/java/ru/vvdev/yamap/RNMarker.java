package ru.vvdev.yamap;

public class RNMarker {
    public double lon;
    public double lat;
    public String id;
    public boolean isSelected;
    public RNMarker(double _lon, double _lat, String _id, boolean _isSelected) {
        lon = _lon;
        lat = _lat;
        id = _id;
        isSelected = _isSelected;
    }
}
