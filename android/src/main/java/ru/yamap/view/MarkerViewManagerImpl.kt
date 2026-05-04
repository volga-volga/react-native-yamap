package ru.yamap.view

import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import ru.yamap.events.YamapMarkerPressEvent
import ru.yamap.utils.PointUtil

class MarkerViewManagerImpl() {

    // PROPS
    fun setPoint(view: MarkerView, jsPoint: ReadableMap?) {
        jsPoint?.let {
            val point = PointUtil.readableMapToPoint(it)
            view.setPoint(point)
        }
    }

    fun setZI(view: MarkerView, zIndex: Float) {
        view.setZIndex(zIndex)
    }

    fun setScale(view: MarkerView, scale: Float) {
        view.setScale(scale)
    }

    fun setHandled(view: MarkerView, handled: Boolean) {
        view.setHandled(handled)
    }

    fun setRotated(view: MarkerView, rotated: Boolean) {
        view.setRotated(rotated)
    }

    fun setVisible(view: MarkerView, visible: Boolean) {
        view.setVisible(visible)
    }

    fun setSource(view: MarkerView, source: String?) {
        if (source != null) {
            view.setIconSource(source)
        }
    }

    fun setSource3d(view: MarkerView, source: String?) {
        if (source != null) {
            view.setIcon3dSource(source)
        }
    }

    fun setAnchor(view: MarkerView, anchor: ReadableMap?) {
        val pointF = PointUtil.jsPointToPointF(anchor)
        view.setAnchor(pointF)
    }

    fun receiveCommand(view: MarkerView, commandType: String, argsArr: ReadableArray?) {
        val args = argsArr?.getArray(0)?.getMap(0) ?: return

        when (commandType) {
            "animatedMoveTo" -> {
                val jsPoint = args.getMap("coords") ?: return
                val moveDuration = args.getDouble("duration")
                val point = PointUtil.readableMapToPoint(jsPoint)
                view.animatedMoveTo(point, moveDuration.toFloat())
            }

            "animatedRotateTo" -> {
                val angle = args.getDouble("angle")
                val rotateDuration = args.getDouble("duration")
                view.animatedRotateTo(angle.toFloat(), rotateDuration.toFloat())
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
        const val NAME = "MarkerView"

        val exportedCustomBubblingEventTypeConstants = mapOf(
            YamapMarkerPressEvent.EVENT_NAME to
                    mapOf("phasedRegistrationNames" to mapOf("bubbled" to "onPress"))
        )
    }
}
