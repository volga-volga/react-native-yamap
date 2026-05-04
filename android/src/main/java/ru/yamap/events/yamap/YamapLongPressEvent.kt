package ru.yamap.events.yamap

import com.facebook.react.uimanager.events.Event
import com.yandex.mapkit.geometry.Point
import ru.yamap.utils.PointUtil

class YamapLongPressEvent(surfaceId: Int, viewId: Int, private val point: Point) : Event<YamapLongPressEvent>(surfaceId, viewId) {
    override fun getEventName() = EVENT_NAME

    override fun getCoalescingKey(): Short = 0

    override fun getEventData() = PointUtil.pointToJsPoint(point)

    companion object {
        const val EVENT_NAME = "topMapLongPress"
    }
}
