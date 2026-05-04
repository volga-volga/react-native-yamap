package ru.yamap.module

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReadableMap

class SearchModuleImpl {

    fun searchByAddress(searchQuery: String?, figure: ReadableMap?, options: ReadableMap?, promise: Promise) {
        promise.reject(ERR_SEARCH_FAILED, "searchByAddress: $ERR_SEARCH_DESCRIPTION")
    }

    fun resolveURI(searchQuery: String?, options: ReadableMap?, promise: Promise) {
        promise.reject(ERR_SEARCH_FAILED, "resolveURI: $ERR_SEARCH_DESCRIPTION")
    }

    fun searchByURI(searchQuery: String?, options: ReadableMap?, promise: Promise) {
        promise.reject(ERR_SEARCH_FAILED, "searchByURI: $ERR_SEARCH_DESCRIPTION")
    }

    fun searchByPoint(jsPoint: ReadableMap?, zoom: Double?, options: ReadableMap?, promise: Promise) {
        promise.reject(ERR_SEARCH_FAILED, "searchByPoint: $ERR_SEARCH_DESCRIPTION")
    }

    fun geoToAddress(jsPoint: ReadableMap?, promise: Promise) {
        promise.reject(ERR_SEARCH_FAILED, "geoToAddress: $ERR_SEARCH_DESCRIPTION")
    }

    fun addressToGeo(text: String?, promise: Promise) {
        promise.reject(ERR_SEARCH_FAILED, "addressToGeo: $ERR_SEARCH_DESCRIPTION")
    }

    companion object {
        const val NAME = "RTNSearchModule"

        private const val ERR_SEARCH_FAILED = "SEARCH_FAILED"
        private const val ERR_SEARCH_DESCRIPTION = "SEARCH module is not available in Lite version"
    }
}
