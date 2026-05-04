package ru.yamap.events.yamap

import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.events.Event

class GetWorldToScreenPointsEvent(surfaceId: Int, viewId: Int, private val eventData: WritableMap?)
    : Event<GetWorldToScreenPointsEvent>(surfaceId, viewId) {

    override fun getEventName() = EVENT_NAME

    override fun getCoalescingKey(): Short = 0

    override fun getEventData() = eventData

    companion object {
        const val EVENT_NAME = "topWorldToScreenPointsReceived"
    }
}
