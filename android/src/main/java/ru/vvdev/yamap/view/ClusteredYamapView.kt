package ru.vvdev.yamap.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.Cluster
import com.yandex.mapkit.map.ClusterListener
import com.yandex.mapkit.map.ClusterTapListener
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.runtime.image.ImageProvider
import kotlin.math.abs
import kotlin.math.sqrt

class ClusteredYamapView(context: Context?) : YamapView(context), ClusterListener,
    ClusterTapListener {
    private val clusterCollection = mapWindow.map.mapObjects.addClusterizedPlacemarkCollection(this)
    private var clusterColor = 0
    private val placemarksMap: HashMap<String?, PlacemarkMapObject?> = HashMap<String?, PlacemarkMapObject?>()
    private var pointsList = ArrayList<Point>()

    fun setClusteredMarkers(points: ArrayList<Any>) {
        clusterCollection.clear()
        placemarksMap.clear()
        val pt = ArrayList<Point>()
        for (i in points.indices) {
            val point = points[i] as HashMap<String, Double>
            pt.add(Point(point["lat"]!!, point["lon"]!!))
        }
        val placemarks = clusterCollection.addPlacemarks(pt, TextImageProvider(""), IconStyle())
        pointsList = pt
        for (i in placemarks.indices) {
            val placemark = placemarks[i]
            placemarksMap["" + placemark.geometry.latitude + placemark.geometry.longitude] =
                placemark
            val child: Any? = getChildAt(i)
            if (child != null && child is YamapMarker) {
                child.setMarkerMapObject(placemark)
            }
        }
        clusterCollection.clusterPlacemarks(50.0, 12)
    }

    fun setClustersColor(color: Int) {
        clusterColor = color
        updateUserMarkersColor()
    }

    private fun updateUserMarkersColor() {
        clusterCollection.clear()
        val placemarks = clusterCollection.addPlacemarks(
            pointsList,
            TextImageProvider(pointsList.size.toString()),
            IconStyle()
        )
        for (i in placemarks.indices) {
            val placemark = placemarks[i]
            placemarksMap["" + placemark.geometry.latitude + placemark.geometry.longitude] =
                placemark
            val child: Any? = getChildAt(i)
            if (child != null && child is YamapMarker) {
                child.setMarkerMapObject(placemark)
            }
        }
        clusterCollection.clusterPlacemarks(50.0, 12)
    }

    override fun addFeature(child: View?, index: Int) {
        val marker = child as YamapMarker?
        val placemark = placemarksMap["" + marker!!.point!!.latitude + marker.point!!.longitude]
        if (placemark != null) {
            marker.setMarkerMapObject(placemark)
        }
    }

    override fun removeChild(index: Int) {
        if (getChildAt(index) is YamapMarker) {
            val child = getChildAt(index) as YamapMarker ?: return
            val mapObject = child.rnMapObject
            if (mapObject == null || !mapObject.isValid) return
            clusterCollection.remove(mapObject)
            placemarksMap.remove("" + child.point!!.latitude + child.point!!.longitude)
        }
    }

    override fun onClusterAdded(cluster: Cluster) {
        cluster.appearance.setIcon(TextImageProvider(cluster.size.toString()))
        cluster.addClusterTapListener(this)
    }

    override fun onClusterTap(cluster: Cluster): Boolean {
        val points = ArrayList<Point?>()
        for (placemark in cluster.placemarks) {
            points.add(placemark.geometry)
        }
        fitMarkers(points)
        return true
    }

    private inner class TextImageProvider(private val text: String) : ImageProvider() {
        override fun getId(): String {
            return "text_$text"
        }

        override fun getImage(): Bitmap {
            val textPaint = Paint()
            textPaint.textSize = Companion.FONT_SIZE
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.style = Paint.Style.FILL
            textPaint.isAntiAlias = true

            val widthF = textPaint.measureText(text)
            val textMetrics = textPaint.fontMetrics
            val heightF =
                (abs(textMetrics.bottom.toDouble()) + abs(textMetrics.top.toDouble())).toFloat()
            val textRadius = sqrt((widthF * widthF + heightF * heightF).toDouble())
                .toFloat() / 2
            val internalRadius = textRadius + Companion.MARGIN_SIZE
            val externalRadius = internalRadius + Companion.STROKE_SIZE

            val width = (2 * externalRadius + 0.5).toInt()

            val bitmap = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            val backgroundPaint = Paint()
            backgroundPaint.isAntiAlias = true
            backgroundPaint.color = clusterColor
            canvas.drawCircle(
                (width / 2).toFloat(),
                (width / 2).toFloat(),
                externalRadius,
                backgroundPaint
            )

            backgroundPaint.color = Color.WHITE
            canvas.drawCircle(
                (width / 2).toFloat(),
                (width / 2).toFloat(),
                internalRadius,
                backgroundPaint
            )

            canvas.drawText(
                text,
                (width / 2).toFloat(),
                width / 2 - (textMetrics.ascent + textMetrics.descent) / 2,
                textPaint
            )

            return bitmap
        }
    }
    companion object {
        private const val FONT_SIZE = 45f
        private const val MARGIN_SIZE = 9f
        private const val STROKE_SIZE = 9f
    }
}
