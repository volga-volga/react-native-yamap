package ru.vvdev.yamap

import android.graphics.PointF
import android.view.View
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.common.MapBuilder
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewGroupManager
import com.facebook.react.uimanager.annotations.ReactProp
import com.yandex.mapkit.geometry.Point
import ru.vvdev.yamap.view.YamapMarker
import javax.annotation.Nonnull

class YamapMarkerManager internal constructor() : ViewGroupManager<YamapMarker>() {
    override fun getName(): String {
        return REACT_CLASS
    }

    override fun getExportedCustomDirectEventTypeConstants(): Map<String, Any>? {
        return MapBuilder.builder<String, Any>()
            .put("onPress", MapBuilder.of("registrationName", "onPress"))
            .build()
    }

    override fun getExportedCustomBubblingEventTypeConstants(): MutableMap<String, Any>? {
        return MapBuilder.builder<String, Any>()
            .build()
    }

    private fun castToMarkerView(view: View): YamapMarker {
        return view as YamapMarker
    }

    @Nonnull
    public override fun createViewInstance(@Nonnull context: ThemedReactContext): YamapMarker {
        return YamapMarker(context)
    }

    // PROPS
    @ReactProp(name = "point")
    fun setPoint(view: View, markerPoint: ReadableMap?) {
        if (markerPoint != null) {
            val lon = markerPoint.getDouble("lon")
            val lat = markerPoint.getDouble("lat")
            val point = Point(lat, lon)
            castToMarkerView(view).setPoint(point)
        }
    }

    @ReactProp(name = "zIndex")
    fun setZIndex(view: View, zIndex: Int) {
        castToMarkerView(view).setZIndex(zIndex)
    }

    @ReactProp(name = "scale")
    fun setScale(view: View, scale: Float) {
        castToMarkerView(view).setScale(scale)
    }

    @ReactProp(name = "handled")
    fun setHandled(view: View, handled: Boolean?) {
        castToMarkerView(view).setHandled(handled ?: true)
    }

    @ReactProp(name = "rotated")
    fun setRotated(view: View, rotated: Boolean?) {
        castToMarkerView(view).setRotated(rotated ?: true)
    }

    @ReactProp(name = "visible")
    fun setVisible(view: View, visible: Boolean?) {
        castToMarkerView(view).setVisible(visible ?: true)
    }

    @ReactProp(name = "source")
    fun setSource(view: View, source: String?) {
        if (source != null) {
            castToMarkerView(view).setIconSource(source)
        }
    }

    @ReactProp(name = "anchor")
    fun setAnchor(view: View, anchor: ReadableMap?) {
        castToMarkerView(view).setAnchor(
            if (anchor != null) PointF(
                anchor.getDouble("x").toFloat(),
                anchor.getDouble("y").toFloat()
            ) else null
        )
    }

    override fun addView(parent: YamapMarker, child: View, index: Int) {
        parent.addChildView(child, index)
        super.addView(parent, child, index)
    }

    override fun removeViewAt(parent: YamapMarker, index: Int) {
        parent.removeChildView(index)
        super.removeViewAt(parent, index)
    }

    override fun receiveCommand(
        view: YamapMarker,
        commandType: String,
        args: ReadableArray?
    ) {
        when (commandType) {
            "animatedMoveTo" -> {
                val markerPoint = args!!.getMap(0)
                val moveDuration = args.getInt(1)
                val lon = markerPoint.getDouble("lon").toFloat()
                val lat = markerPoint.getDouble("lat").toFloat()
                val point = Point(lat.toDouble(), lon.toDouble())
                castToMarkerView(view).animatedMoveTo(point, moveDuration.toFloat())
                return
            }

            "animatedRotateTo" -> {
                val angle = args!!.getInt(0)
                val rotateDuration = args.getInt(1)
                castToMarkerView(view).animatedRotateTo(angle.toFloat(), rotateDuration.toFloat())
                return
            }

            else -> throw IllegalArgumentException(
                String.format(
                    "Unsupported command %d received by %s.",
                    commandType,
                    javaClass.simpleName
                )
            )
        }
    }

    companion object {
        const val REACT_CLASS: String = "YamapMarker"
    }
}
