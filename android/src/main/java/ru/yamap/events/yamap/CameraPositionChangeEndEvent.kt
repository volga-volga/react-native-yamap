package ru.yamap.events.yamap

import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.events.Event

class CameraPositionChangeEndEvent(surfaceId: Int, viewId: Int, private val cameraPosition: WritableMap?)
    : Event<CameraPositionChangeEndEvent>(surfaceId, viewId) {

    override fun getEventName() = EVENT_NAME

    override fun getCoalescingKey(): Short = 0

    override fun getEventData() = cameraPosition

    companion object {
        const val EVENT_NAME = "topCameraPositionChangeEnd"
    }
}
