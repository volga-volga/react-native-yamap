package ru.vvdev.yamap.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yandex.mapkit.Animation;
import com.yandex.mapkit.geometry.BoundingBox;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.Cluster;
import com.yandex.mapkit.map.ClusterListener;
import com.yandex.mapkit.map.ClusterTapListener;
import com.yandex.mapkit.map.ClusterizedPlacemarkCollection;
import com.yandex.mapkit.map.IconStyle;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.map.MapObject;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.map.internal.PlacemarkMapObjectBinding;
import com.yandex.runtime.image.ImageProvider;
import com.yandex.runtime.ui_view.ViewProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import ru.vvdev.yamap.models.ReactMapObject;

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

    private void fitClusterMarkers(List<PlacemarkMapObject> points) {
        ArrayList<Point> lastKnownMarkers = new ArrayList<Point>();
        for (int i = 0; i < points.size(); ++i) {
            lastKnownMarkers.add(points.get(i).getGeometry());
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
        CameraPosition oldCameraPosition = getMap().getCameraPosition();
        CameraPosition cameraPosition = getMap().cameraPosition(calculateBoundingBox(lastKnownMarkers));
        cameraPosition = new CameraPosition(cameraPosition.getTarget(), cameraPosition.getZoom() - 0.8f, cameraPosition.getAzimuth(), cameraPosition.getTilt());
        if (cameraPosition.getZoom()-oldCameraPosition.getZoom()>1) {
            getMap().move(cameraPosition, new Animation(Animation.Type.SMOOTH, 0.7f), null);
        } else {
            cameraPosition = new CameraPosition(cameraPosition.getTarget(), (float) Math.ceil(cameraPosition.getZoom()), cameraPosition.getAzimuth(), cameraPosition.getTilt());
            getMap().move(cameraPosition, new Animation(Animation.Type.SMOOTH, 0.7f), null);
        }
    }

    @Override
    public boolean onClusterTap(@NonNull Cluster cluster) {
        fitClusterMarkers(cluster.getPlacemarks());
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
