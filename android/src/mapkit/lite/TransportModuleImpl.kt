package ru.yamap.module

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap

class TransportModuleImpl {

    fun findRoutes(jsPoints: ReadableArray, jsVehicles: ReadableArray?, promise: Promise?) {
        promise?.reject(ERR_TRANSPORT_FAILED, "findRoutes: $ERR_TRANSPORT_DESCRIPTION")
    }

    fun findParkRoutes(start: ReadableMap, end: ReadableMap, zones: ReadableArray, promise: Promise?) {
        promise?.reject(ERR_TRANSPORT_FAILED, "findParkRoutes: $ERR_TRANSPORT_DESCRIPTION")
    }

    companion object {
        const val NAME = "RTNTransportModule"

        private const val ERR_TRANSPORT_FAILED = "TRANSPORT_FAILED"
        private const val ERR_TRANSPORT_DESCRIPTION = "TRANSPORT module is not available in Lite version"
    }
}
