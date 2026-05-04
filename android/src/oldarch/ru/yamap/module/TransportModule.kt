package ru.yamap.module

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableArray

class TransportModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    private val implementation = TransportModuleImpl()

    override fun getName() = TransportModuleImpl.NAME

    @ReactMethod
    fun findRoutes(points: ReadableArray, vehicles: ReadableArray, promise: Promise?) {
        implementation.findRoutes(points, vehicles, promise)
    }
}
