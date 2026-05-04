package ru.yamap.module

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import ru.yamap.NativeYamapModuleSpec

class YamapModule(reactContext: ReactApplicationContext) : NativeYamapModuleSpec(reactContext) {

    private val implementation = YamapModuleImpl(reactContext)

    override fun getName() = YamapModuleImpl.NAME

    override fun init(apiKey: String?, promise: Promise) {
        implementation.init(apiKey, promise)
    }

    override fun setLocale(locale: String?, promise: Promise) {
        implementation.setLocale(locale, promise)
    }

    override fun getLocale(promise: Promise) {
        implementation.getLocale(promise)
    }

    override fun resetLocale(promise: Promise) {
        implementation.resetLocale(promise)
    }
}
