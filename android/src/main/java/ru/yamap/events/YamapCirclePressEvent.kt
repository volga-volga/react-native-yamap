package ru.yamap.events

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.events.Event

class YamapCirclePressEvent(surfaceId: Int, viewId: Int) : Event<YamapCirclePressEvent>(surfaceId, viewId) {
    override fun getEventName() = EVENT_NAME

    override fun getCoalescingKey(): Short = 0

    override fun getEventData(): WritableMap = Arguments.createMap()

    companion object {
        const val EVENT_NAME = "topPress"
    }
}
