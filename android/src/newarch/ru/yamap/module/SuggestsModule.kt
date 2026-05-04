package ru.yamap.module

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableMap
import ru.yamap.NativeSuggestsModuleSpec

class SuggestsModule(reactContext: ReactApplicationContext) : NativeSuggestsModuleSpec(reactContext) {

    private val implementation = SuggestsModuleImpl()

    override fun getName() = SuggestsModuleImpl.NAME

    override fun suggest(text: String?, promise: Promise) {
        implementation.suggest(text, promise)
    }

    override fun suggestWithOptions(text: String?, options: ReadableMap?, promise: Promise) {
        implementation.suggestWithOptions(text, options, promise)
    }

    override fun reset(promise: Promise) {
        implementation.reset(promise)
    }
}
