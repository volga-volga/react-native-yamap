package ru.yamap.view

import com.facebook.react.bridge.ReadableArray
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewGroupManager
import com.facebook.react.uimanager.annotations.ReactProp

class PolygonViewManager : ViewGroupManager<PolygonView>() {

    private val implementation = PolygonViewManagerImpl()

    override fun getName() = PolygonViewManagerImpl.NAME

    override fun getExportedCustomBubblingEventTypeConstants() =
        PolygonViewManagerImpl.exportedCustomBubblingEventTypeConstants

    override fun createViewInstance(context: ThemedReactContext) = PolygonView(context)

    // PROPS
    @ReactProp(name = "points")
    fun setPoints(view: PolygonView, jsPoints: ReadableArray?) {
        implementation.setPoints(view, jsPoints)
    }

    @ReactProp(name = "innerRings")
    fun setInnerRings(view: PolygonView, jsRings: ReadableArray?) {
        implementation.setInnerRings(view, jsRings)
    }

    @ReactProp(name = "strokeWidth")
    fun setStrokeWidth(view: PolygonView, width: Float) {
        implementation.setStrokeWidth(view, width)
    }

    @ReactProp(name = "strokeColor")
    fun setStrokeColor(view: PolygonView, color: Int) {
        implementation.setStrokeColor(view, color)
    }

    @ReactProp(name = "fillColor")
    fun setFillColor(view: PolygonView, color: Int) {
        implementation.setFillColor(view, color)
    }

    @ReactProp(name = "zI")
    fun setZI(view: PolygonView, zIndex: Float) {
        implementation.setZI(view, zIndex)
    }

    @ReactProp(name = "handled")
    fun setHandled(view: PolygonView, handled: Boolean) {
        implementation.setHandled(view, handled)
    }
}
