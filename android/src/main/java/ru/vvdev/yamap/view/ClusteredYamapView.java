package ru.vvdev.yamap.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.CameraUpdateReason;
import com.yandex.mapkit.map.Cluster;
import com.yandex.mapkit.map.ClusterListener;
import com.yandex.mapkit.map.ClusterTapListener;
import com.yandex.mapkit.map.ClusterizedPlacemarkCollection;
import com.yandex.mapkit.map.IconStyle;
import com.yandex.mapkit.map.MapObject;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.runtime.image.ImageProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClusteredYamapView extends YamapView implements ClusterListener, ClusterTapListener {
    private ClusterizedPlacemarkCollection clusterCollection;
    private int clusterColor = 0;
    private HashMap<String, PlacemarkMapObject> placemarksMap = new HashMap();
    private ArrayList<Point> pointsList = new ArrayList<>();

    public ClusteredYamapView(Context context) {
        super(context);
        clusterCollection = getMap().getMapObjects().addClusterizedPlacemarkCollection(this);
    }

    public void setClusteredMarkers(ArrayList<Object> points) {
        clusterCollection.clear();
        placemarksMap.clear();
        ArrayList<Point> pt = new ArrayList<>();
        for (int i = 0; i<points.size(); i++) {
            HashMap<String, Double> point = (HashMap<String, Double>) points.get(i);
            pt.add(new Point(point.get("lat"), point.get("lon")));
        }
        List<PlacemarkMapObject> placemarks = clusterCollection.addPlacemarks(pt, new TextImageProvider(""), new IconStyle());
        pointsList = pt;
        for (int i = 0; i<placemarks.size(); i++) {
            PlacemarkMapObject placemark = placemarks.get(i);
            placemarksMap.put("" + placemark.getGeometry().getLatitude() + placemark.getGeometry().getLongitude(), placemark);
            Object child = getChildAt(i);
            if (child instanceof YamapMarker) {
                ((YamapMarker)child).setMapObject(placemark);
            }
        }
        clusterCollection.clusterPlacemarks(50, 12);
    }

    public void setClustersColor(int color) {
        clusterColor = color;
        updateUserMarkersColor();
    }

    private void updateUserMarkersColor() {
        clusterCollection.clear();
        List<PlacemarkMapObject> placemarks =  clusterCollection.addPlacemarks(pointsList, new TextImageProvider(Integer.toString(pointsList.size())), new IconStyle());
        for (int i = 0; i<placemarks.size(); i++) {
            PlacemarkMapObject placemark = placemarks.get(i);
            placemarksMap.put("" + placemark.getGeometry().getLatitude() + placemark.getGeometry().getLongitude(), placemark);
            Object child = getChildAt(i);
            if (child instanceof YamapMarker) {
                ((YamapMarker)child).setMapObject(placemark);
            }
        }
        clusterCollection.clusterPlacemarks(50, 12);
    }

    @Override
    public void addFeature(View child, int index) {
        YamapMarker marker = (YamapMarker) child;
        PlacemarkMapObject placemark = placemarksMap.get("" + marker.point.getLatitude() + marker.point.getLongitude());
        if (placemark!=null) {
            marker.setMapObject(placemark);
        }
    }

    @Override
    public void removeChild(int index) {
        if (getChildAt(index) instanceof YamapMarker) {
            final YamapMarker child = (YamapMarker) getChildAt(index);
            if (child == null) return;
            final MapObject mapObject = child.getMapObject();
            if (mapObject == null || !mapObject.isValid()) return;
            clusterCollection.remove(mapObject);
            placemarksMap.remove("" + child.point.getLatitude() + child.point.getLongitude());
        }
    }

    @Override
    public void onClusterAdded(@NonNull Cluster cluster) {
        cluster.getAppearance().setIcon(new TextImageProvider(Integer.toString(cluster.getSize())));
        cluster.addClusterTapListener(this);
    }

    @Override
    public boolean onClusterTap(@NonNull Cluster cluster) {
        ArrayList<Point> points = new ArrayList<>();
        for (PlacemarkMapObject placemark : cluster.getPlacemarks()) {
            points.add(placemark.getGeometry());
        }
        fitMarkers(points);
        return true;
    }

    private class TextImageProvider extends ImageProvider {
        private static final float FONT_SIZE = 45;
        private static final float MARGIN_SIZE = 9;
        private static final float STROKE_SIZE = 9;

        @Override
        public String getId() {
            return "text_" + text;
        }

        private final String text;

        @Override
        public Bitmap getImage() {
            Paint textPaint = new Paint();
            textPaint.setTextSize(FONT_SIZE);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setStyle(Paint.Style.FILL);
            textPaint.setAntiAlias(true);

            float widthF = textPaint.measureText(text);
            Paint.FontMetrics textMetrics = textPaint.getFontMetrics();
            float heightF = Math.abs(textMetrics.bottom) + Math.abs(textMetrics.top);
            float textRadius = (float) Math.sqrt(widthF * widthF + heightF * heightF) / 2;
            float internalRadius = textRadius + MARGIN_SIZE;
            float externalRadius = internalRadius + STROKE_SIZE;

            int width = (int) (2 * externalRadius + 0.5);

            Bitmap bitmap = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);

            Paint backgroundPaint = new Paint();
            backgroundPaint.setAntiAlias(true);
            backgroundPaint.setColor(clusterColor);
            canvas.drawCircle(width / 2, width / 2, externalRadius, backgroundPaint);

            backgroundPaint.setColor(Color.WHITE);
            canvas.drawCircle(width / 2, width / 2, internalRadius, backgroundPaint);

            canvas.drawText(
                    text,
                    width / 2,
                    width / 2 - (textMetrics.ascent + textMetrics.descent) / 2,
                    textPaint);

            return bitmap;
        }

        public TextImageProvider(String text) {
            this.text = text;
        }
    }
}
