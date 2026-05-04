package ru.yamap.module

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReadableArray

class TransportModuleImpl {

    fun findRoutes(jsPoints: ReadableArray, jsVehicles: ReadableArray?, promise: Promise?) {
        promise?.reject(ERR_TRANSPORT_FAILED, "findRoutes: $ERR_TRANSPORT_DESCRIPTION")
    }

    companion object {
        const val NAME = "RTNTransportModule"

        private const val ERR_TRANSPORT_FAILED = "TRANSPORT_FAILED"
        private const val ERR_TRANSPORT_DESCRIPTION = "TRANSPORT module is not available in Lite version"
    }
}
