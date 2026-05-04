package ru.yamap.module

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import ru.yamap.NativeTransportModuleSpec

class TransportModule(reactContext: ReactApplicationContext) : NativeTransportModuleSpec(reactContext) {

    private val implementation = TransportModuleImpl()

    override fun getName() = TransportModuleImpl.NAME

    override fun findRoutes(points: ReadableArray, vehicles: ReadableArray, promise: Promise?) {
        implementation.findRoutes(points, vehicles, promise)
    }

    override fun findParkRoutes(start: ReadableMap, end: ReadableMap, zones: ReadableArray, promise: Promise) {

    }
}
