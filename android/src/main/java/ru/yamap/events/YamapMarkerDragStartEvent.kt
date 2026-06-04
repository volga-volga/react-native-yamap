package ru.yamap.events

import com.facebook.react.uimanager.events.Event
import com.yandex.mapkit.geometry.Point
import ru.yamap.utils.PointUtil

class YamapMarkerDragStartEvent(
    surfaceId: Int,
    viewId: Int,
    private val point: Point
) : Event<YamapMarkerDragStartEvent>(surfaceId, viewId) {
    override fun getEventName() = EVENT_NAME

    override fun getCoalescingKey(): Short = 0

    override fun getEventData() = PointUtil.pointToJsPoint(point)

    companion object {
        const val EVENT_NAME = "topDragStart"
    }
}
