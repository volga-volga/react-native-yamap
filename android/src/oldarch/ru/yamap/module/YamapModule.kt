package ru.yamap.module

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod

class YamapModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    private val implementation = YamapModuleImpl(reactContext)

    override fun getName() = YamapModuleImpl.NAME

    @ReactMethod
    fun init(apiKey: String?, promise: Promise) {
        implementation.init(apiKey, promise)
    }

    @ReactMethod
    fun setLocale(locale: String?, promise: Promise) {
        implementation.setLocale(locale, promise)
    }

    @ReactMethod
    fun getLocale(promise: Promise) {
        implementation.getLocale(promise)
    }

    @ReactMethod
    fun resetLocale(promise: Promise) {
        implementation.resetLocale(promise)
    }
}
