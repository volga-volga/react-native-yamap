package ru.yamap.events.yamap

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.events.Event
import com.yandex.mapkit.map.VisibleRegion
import ru.yamap.utils.PointUtil

class GetVisibleRegionEvent(surfaceId: Int, viewId: Int, private val region: VisibleRegion, private val id: String?)
    : Event<GetVisibleRegionEvent>(surfaceId, viewId) {

    override fun getEventName() = EVENT_NAME

    override fun getCoalescingKey(): Short = 0

    override fun getEventData(): WritableMap? {
        val data = Arguments.createMap()

        data.putMap("bottomLeft", PointUtil.pointToJsPoint(region.bottomLeft))
        data.putMap("bottomRight", PointUtil.pointToJsPoint(region.bottomRight))
        data.putMap("topLeft", PointUtil.pointToJsPoint(region.topLeft))
        data.putMap("topRight", PointUtil.pointToJsPoint(region.topRight))

        data.putString("id", id)

        return data
    }

    companion object {
        const val EVENT_NAME = "topVisibleRegionReceived"
    }
}
