package ru.yamap.view

import com.facebook.react.bridge.ReadableArray
import ru.yamap.events.YamapPolylinePressEvent
import ru.yamap.utils.PointUtil

class PolylineViewManagerImpl() {

    // PROPS
    fun setPoints(view: PolylineView, jsPoints: ReadableArray?) {
        val points = jsPoints?.let { PointUtil.jsPointsToPoints(it) }
        view.setPolygonPoints(points)
    }

    fun setStrokeWidth(view: PolylineView, width: Float) {
        view.setStrokeWidth(width)
    }

    fun setStrokeColor(view: PolylineView, color: Int) {
        view.setStrokeColor(color)
    }

    fun setZI(view: PolylineView, zIndex: Float) {
        view.setZIndex(zIndex)
    }

    fun setDashLength(view: PolylineView, length: Float) {
        view.setDashLength(length)
    }

    fun setDashOffset(view: PolylineView, offset: Float) {
        view.setDashOffset(offset)
    }

    fun setGapLength(view: PolylineView, length: Float) {
        view.setGapLength(length)
    }

    fun setOutlineWidth(view: PolylineView, width: Float) {
        view.setOutlineWidth(width)
    }

    fun setOutlineColor(view: PolylineView, color: Int) {
        view.setOutlineColor(color)
    }

    fun setHandled(view: PolylineView, handled: Boolean) {
        view.setHandled(handled)
    }

    companion object {
        const val NAME = "PolylineView"

        val exportedCustomBubblingEventTypeConstants = mapOf(
            YamapPolylinePressEvent.EVENT_NAME to
                    mapOf("phasedRegistrationNames" to mapOf("bubbled" to "onPress"))
        )
    }
}
