package ru.yamap.view

import com.facebook.react.bridge.ReadableMap
import ru.yamap.events.YamapCirclePressEvent
import ru.yamap.utils.PointUtil

class CircleViewManagerImpl() {

    // PROPS
    fun setCenter(view: CircleView, center: ReadableMap?) {
        center?.let {
            val point = PointUtil.readableMapToPoint(it)
            view.setCenter(point)
        }
    }

    fun setRadius(view: CircleView, radius: Float) {
        view.setRadius(radius)
    }

    fun setStrokeWidth(view: CircleView, width: Float) {
        view.setStrokeWidth(width)
    }

    fun setStrokeColor(view: CircleView, color: Int) {
        view.setStrokeColor(color)
    }

    fun setFillColor(view: CircleView, color: Int) {
        view.setFillColor(color)
    }

    fun setZI(view: CircleView, zIndex: Float) {
        view.setZIndex(zIndex)
    }

    fun setHandled(view: CircleView, handled: Boolean) {
        view.setHandled(handled)
    }

    companion object {
        const val NAME = "CircleView"

        val exportedCustomBubblingEventTypeConstants = mapOf(
            YamapCirclePressEvent.EVENT_NAME to
                    mapOf("phasedRegistrationNames" to mapOf("bubbled" to "onPress"))
        )
    }
}
