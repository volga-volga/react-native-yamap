package ru.yamap.view

import com.facebook.react.bridge.ReadableArray
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewGroupManager
import com.facebook.react.uimanager.annotations.ReactProp

class PolylineViewManager : ViewGroupManager<PolylineView>() {

    private val implementation = PolylineViewManagerImpl()

    override fun getName() = PolylineViewManagerImpl.NAME

    override fun getExportedCustomBubblingEventTypeConstants() =
        PolylineViewManagerImpl.exportedCustomBubblingEventTypeConstants

    override fun createViewInstance(context: ThemedReactContext) = PolylineView(context)

    // PROPS
    @ReactProp(name = "points")
    fun setPoints(view: PolylineView, jsPoints: ReadableArray?) {
        implementation.setPoints(view, jsPoints)
    }

    @ReactProp(name = "strokeWidth")
    fun setStrokeWidth(view: PolylineView, width: Float) {
        implementation.setStrokeWidth(view, width)
    }

    @ReactProp(name = "strokeColor")
    fun setStrokeColor(view: PolylineView, color: Int) {
        implementation.setStrokeColor(view, color)
    }

    @ReactProp(name = "zI")
    fun setZI(view: PolylineView, zIndex: Float) {
        implementation.setZI(view, zIndex)
    }

    @ReactProp(name = "dashLength")
    fun setDashLength(view: PolylineView, length: Float) {
        implementation.setDashLength(view, length)
    }

    @ReactProp(name = "dashOffset")
    fun setDashOffset(view: PolylineView, offset: Float) {
        implementation.setDashOffset(view, offset)
    }

    @ReactProp(name = "gapLength")
    fun setGapLength(view: PolylineView, length: Float) {
        implementation.setGapLength(view, length)
    }

    @ReactProp(name = "outlineWidth")
    override fun setOutlineWidth(view: PolylineView, width: Float) {
        implementation.setOutlineWidth(view, width)
    }

    @ReactProp(name = "outlineColor")
    fun setOutlineColor(view: PolylineView, color: Int) {
        implementation.setOutlineColor(view, color)
    }

    @ReactProp(name = "handled")
    fun setHandled(view: PolylineView, handled: Boolean) {
        implementation.setHandled(view, handled)
    }
}
