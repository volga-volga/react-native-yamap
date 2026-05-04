package ru.yamap.view

import com.facebook.react.bridge.ReadableMap
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewGroupManager
import com.facebook.react.uimanager.annotations.ReactProp

class CircleViewManager : ViewGroupManager<CircleView>() {

    private val implementation = CircleViewManagerImpl()

    override fun getName() = CircleViewManagerImpl.NAME

    override fun getExportedCustomBubblingEventTypeConstants() =
        CircleViewManagerImpl.exportedCustomBubblingEventTypeConstants

    override fun createViewInstance(context: ThemedReactContext) = CircleView(context)

    // PROPS
    @ReactProp(name = "center")
    fun setCenter(view: CircleView, center: ReadableMap?) {
        implementation.setCenter(view, center)
    }

    @ReactProp(name = "radius")
    fun setRadius(view: CircleView, radius: Float) {
        implementation.setRadius(view, radius)
    }

    @ReactProp(name = "strokeWidth")
    fun setStrokeWidth(view: CircleView, width: Float) {
        implementation.setStrokeWidth(view, width)
    }

    @ReactProp(name = "strokeColor")
    fun setStrokeColor(view: CircleView, color: Int) {
        implementation.setStrokeColor(view, color)
    }

    @ReactProp(name = "fillColor")
    fun setFillColor(view: CircleView, color: Int) {
        implementation.setFillColor(view, color)
    }

    @ReactProp(name = "zI")
    fun setZI(view: CircleView, zIndex: Float) {
        implementation.setZI(view, zIndex)
    }

    @ReactProp(name = "handled")
    fun setHandled(view: CircleView, handled: Boolean) {
        implementation.setHandled(view, handled)
    }
}
