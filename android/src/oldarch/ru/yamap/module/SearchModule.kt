package ru.yamap.module

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap

class SearchModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    private val implementation = SearchModuleImpl()

    override fun getName() = SearchModuleImpl.NAME

    @ReactMethod
    fun searchByAddress(searchQuery: String?, figure: ReadableMap?, options: ReadableMap?, promise: Promise) {
        implementation.searchByAddress(searchQuery, figure, options, promise)
    }

    @ReactMethod
    fun resolveURI(searchQuery: String?, options: ReadableMap?, promise: Promise) {
        implementation.resolveURI(searchQuery, options, promise)
    }

    @ReactMethod
    fun searchByURI(searchQuery: String?, options: ReadableMap?, promise: Promise) {
        implementation.searchByURI(searchQuery, options, promise)
    }

    @ReactMethod
    fun searchByPoint(jsPoint: ReadableMap?, zoom: Double?, options: ReadableMap?, promise: Promise) {
        implementation.searchByPoint(jsPoint, zoom, options, promise)
    }

    @ReactMethod
    fun geoToAddress(jsPoint: ReadableMap?, promise: Promise) {
        implementation.geoToAddress(jsPoint, promise)
    }

    @ReactMethod
    fun addressToGeo(text: String?, promise: Promise) {
        implementation.addressToGeo(text, promise)
    }
}
