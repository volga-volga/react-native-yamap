package ru.yamap.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
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
import androidx.core.graphics.createBitmap
import com.facebook.react.bridge.ReadableMap
import ru.yamap.utils.ImageCacheManager

class ClusteredYamapView(context: Context?) : YamapView(context), ClusterListener,
    ClusterTapListener {
    private val clusterCollection = mapWindow.map.mapObjects.addClusterizedPlacemarkCollection(this)
    private var clusterColor = 0
    private val placemarksMap = HashMap<String?, PlacemarkMapObject?>()
    private var pointsList = ArrayList<Point>()
    private var clusterIconSource = ""
    private var clusterWidth = 32
    private var clusterHeight = 32
    private var clusterTextSize = FONT_SIZE
    private var clusterTextColor = Color.BLACK
    private var clusterTextXOffset = 0
    private var clusterTextYOffset = 0

    fun setClusterTextSize(size: Float) {
        clusterTextSize = size
    }

    fun setClusterTextColor(color: Int) {
        clusterTextColor = color
    }

    fun setClusterTextXOffset(offset: Int) {
        clusterTextXOffset = offset
    }

    fun setClusterTextYOffset(offset: Int) {
        clusterTextYOffset = offset
    }

    fun setClusteredMarkers(points: ArrayList<HashMap<String, Double>>) {
        clusterCollection.clear()
        placemarksMap.clear()
        val pt = ArrayList<Point>()
        for (i in points.indices) {
            val point = points[i]
            pt.add(Point(point["lat"]!!, point["lon"]!!))
        }
        val placemarks = clusterCollection.addPlacemarks(pt, TextImageProvider(""), IconStyle())
        pointsList = pt
        for (i in placemarks.indices) {
            val placemark = placemarks[i]
            placemarksMap["" + placemark.geometry.latitude + placemark.geometry.longitude] =
                placemark
            val child: Any? = getChildAt(i)
            if (child != null && child is MarkerView) {
                child.setMarkerMapObject(placemark)
            }
        }
        clusterCollection.clusterPlacemarks(50.0, 12)
    }

    fun setClusterIcon(source: String) {
        clusterIconSource = source
    }

    fun setClusterSize(params: ReadableMap?) {
        clusterWidth =
            if (params != null && params.hasKey("width") && !params.isNull("width")) params.getInt("width") else clusterWidth;
        clusterHeight =
            if (params != null && params.hasKey("height") && !params.isNull("height")) params.getInt(
                "height"
            ) else clusterWidth;
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
            if (child != null && child is MarkerView) {
                child.setMarkerMapObject(placemark)
            }
        }
        clusterCollection.clusterPlacemarks(50.0, 12)
    }

    override fun addFeature(child: View?, index: Int) {
        val marker = child as MarkerView?
        val placemark = placemarksMap["" + marker!!.point!!.latitude + marker.point!!.longitude]
        if (placemark != null) {
            marker.setMarkerMapObject(placemark)
        }
    }

    override fun removeChild(index: Int) {
        val child = getChildAt(index)
        if (child is MarkerView) {
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
        val points = ArrayList<Point>()
        for (placemark in cluster.placemarks) {
            points.add(placemark.geometry)
        }
        fitMarkers(points, 0.7f, 0)
        return true
    }

    private inner class TextImageProvider(private val text: String) : ImageProvider() {
        override fun getId(): String {
            return "text_$text"
        }

        override fun getImage(): Bitmap {
            val textPaint = Paint()
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.style = Paint.Style.FILL
            textPaint.isAntiAlias = true
            textPaint.textSize = clusterTextSize
            textPaint.color = clusterTextColor

            val textMetrics = textPaint.fontMetrics

            if (clusterIconSource != "" && clusterWidth != 0 && clusterHeight != 0) {
                val clusterBitmap = ImageCacheManager.getBitmapSync(context, clusterIconSource)
                val bitmap = createBitmap(clusterWidth, clusterHeight);
                val canvas = Canvas(bitmap);

                canvas.drawBitmap(
                    clusterBitmap,
                    null,
                    Rect(0, 0, clusterWidth, clusterHeight),
                    null
                );

                val currentTypeFace = textPaint.typeface
                textPaint.typeface = Typeface.create(currentTypeFace, Typeface.BOLD)

                canvas.drawText(
                    text,
                    (clusterWidth / 2 + clusterTextXOffset).toFloat(),
                    (clusterHeight / 2 - (textMetrics.ascent + textMetrics.descent) / 2 + clusterTextYOffset).toFloat(),
                    textPaint
                );

                return bitmap
            } else {
                val widthF = textPaint.measureText(text)
                val heightF =
                    (abs(textMetrics.bottom.toDouble()) + abs(textMetrics.top.toDouble())).toFloat()
                val textRadius = sqrt((widthF * widthF + heightF * heightF).toDouble())
                    .toFloat() / 2
                val internalRadius = textRadius + Companion.MARGIN_SIZE
                val externalRadius = internalRadius + Companion.STROKE_SIZE

                val width = (2 * externalRadius + 0.5).toInt()

                val bitmap = createBitmap(width, width)
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
    }
    companion object {
        private const val FONT_SIZE = 45f
        private const val MARGIN_SIZE = 9f
        private const val STROKE_SIZE = 9f
    }
}
