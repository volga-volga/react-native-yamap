package ru.vvdev.yamap

import com.facebook.react.bridge.ReadableMap
import com.facebook.react.common.MapBuilder
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewGroupManager
import com.facebook.react.uimanager.annotations.ReactProp
import com.yandex.mapkit.geometry.Point
import ru.vvdev.yamap.view.YamapCircle
import javax.annotation.Nonnull

class YamapCircleManager internal constructor() : ViewGroupManager<YamapCircle>() {
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

    @Nonnull
    public override fun createViewInstance(@Nonnull context: ThemedReactContext): YamapCircle {
        return YamapCircle(context)
    }

    // PROPS
    @ReactProp(name = "center")
    fun setCenter(view: YamapCircle, center: ReadableMap?) {
        if (center != null) {
            val lon = center.getDouble("lon")
            val lat = center.getDouble("lat")
            val point = Point(lat, lon)
            view.setCenter(point)
        }
    }

    @ReactProp(name = "radius")
    fun setRadius(view: YamapCircle, radius: Float) {
        view.setRadius(radius)
    }

    @ReactProp(name = "strokeWidth")
    fun setStrokeWidth(view: YamapCircle, width: Float) {
        view.setStrokeWidth(width)
    }

    @ReactProp(name = "strokeColor")
    fun setStrokeColor(view: YamapCircle, color: Int) {
        view.setStrokeColor(color)
    }

    @ReactProp(name = "fillColor")
    fun setFillColor(view: YamapCircle, color: Int) {
        view.setFillColor(color)
    }

    @ReactProp(name = "zIndex")
    fun setZIndex(view: YamapCircle, zIndex: Int) {
        view.setZIndex(zIndex)
    }

    companion object {
        const val REACT_CLASS: String = "YamapCircle"
    }
}
