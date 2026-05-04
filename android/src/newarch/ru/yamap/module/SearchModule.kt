package ru.yamap.module

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableMap
import ru.yamap.NativeSearchModuleSpec

class SearchModule(reactContext: ReactApplicationContext) : NativeSearchModuleSpec(reactContext) {

    private val implementation = SearchModuleImpl()

    override fun getName() = SearchModuleImpl.NAME

    override fun searchByAddress(searchQuery: String?, figure: ReadableMap?, options: ReadableMap?, promise: Promise) {
        implementation.searchByAddress(searchQuery, figure, options, promise)
    }

    override fun resolveURI(searchQuery: String?, options: ReadableMap?, promise: Promise) {
        implementation.resolveURI(searchQuery, options, promise)
    }

    override fun searchByURI(searchQuery: String?, options: ReadableMap?, promise: Promise) {
        implementation.searchByURI(searchQuery, options, promise)
    }

    override fun searchByPoint(jsPoint: ReadableMap?, zoom: Double?, options: ReadableMap?, promise: Promise) {
        implementation.searchByPoint(jsPoint, zoom, options, promise)
    }

    override fun geoToAddress(jsPoint: ReadableMap?, promise: Promise) {
        implementation.geoToAddress(jsPoint, promise)
    }

    override fun addressToGeo(text: String?, promise: Promise) {
        implementation.addressToGeo(text, promise)
    }
}
