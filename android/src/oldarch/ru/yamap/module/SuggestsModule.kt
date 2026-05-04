package ru.yamap.module

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap

class SuggestsModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    private val implementation = SuggestsModuleImpl()

    override fun getName() = SuggestsModuleImpl.NAME

    @ReactMethod
    fun suggest(text: String?, promise: Promise) {
        implementation.suggest(text, promise)
    }

    @ReactMethod
    fun suggestWithOptions(text: String?, options: ReadableMap?, promise: Promise) {
        implementation.suggestWithOptions(text, options, promise)
    }

    @ReactMethod
    fun reset(promise: Promise) {
        implementation.reset(promise)
    }
}
