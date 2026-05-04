package ru.yamap.module

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReadableMap

class SuggestsModuleImpl {

    fun suggest(text: String?, promise: Promise) {
        promise.reject(ERR_SUGGEST_FAILED, "suggest: $ERR_SUGGEST_DESCRIPTION")
    }

    fun suggestWithOptions(text: String?, options: ReadableMap?, promise: Promise) {
        promise.reject(ERR_SUGGEST_FAILED, "suggestWithOptions: $ERR_SUGGEST_DESCRIPTION")
    }

    fun reset(promise: Promise) {
        promise.reject(ERR_SUGGEST_FAILED, "reset: $ERR_SUGGEST_DESCRIPTION")
    }

    companion object {
        const val NAME = "RTNSuggestsModule"

        private const val ERR_SUGGEST_FAILED = "SUGGEST_FAILED"
        private const val ERR_SUGGEST_DESCRIPTION = "SUGGEST module is not available in Lite version"
    }
}
