package ru.vvdev.yamap.suggest

import YandexSearchRNArgsHelper
import android.content.Context
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.UiThreadUtil
import com.yandex.mapkit.geometry.Point
import ru.vvdev.yamap.utils.Callback

class RNYandexSuggestModule(reactContext: ReactApplicationContext?) :
    ReactContextBaseJavaModule(reactContext) {
    private var suggestClient: MapSuggestClient? = null
    private val argsHelper = YandexSuggestRNArgsHelper()
    private val searchArgsHelper = YandexSearchRNArgsHelper()

    override fun getName(): String {
        return "YamapSuggests"
    }

    @ReactMethod
    fun suggest(text: String?, promise: Promise) {
        if (text == null) {
            promise.reject(ERR_NO_REQUEST_ARG, "suggest request: text arg is not provided")
            return
        }

        UiThreadUtil.runOnUiThread {
            getSuggestClient(reactApplicationContext).suggest(text,
                object : Callback<List<MapSuggestItem?>?> {
                    override fun invoke(arg: List<MapSuggestItem?>?) {
                        promise.resolve(argsHelper.createSuggestsMapFrom(arg))
                    }
                },
                object : Callback<Throwable?> {
                    override fun invoke(arg: Throwable?) {
                        promise.reject(ERR_SUGGEST_FAILED, "suggest request: " + arg?.message)
                    }
                }
            )
        }
    }

    @ReactMethod
    fun geoToAddress(markerPoint: ReadableMap?, promise: Promise) {
        if (markerPoint != null) {
            val lon = markerPoint.getDouble("lon")
            val lat = markerPoint.getDouble("lat")
            val point = com.yandex.mapkit.geometry.Point(lat, lon)
            UiThreadUtil.runOnUiThread {
                getSuggestClient(reactApplicationContext).suggestPoint(point,
                    object : Callback<MapSearchItem?> {
                        override fun invoke(arg: MapSearchItem?) {
                            promise.resolve(searchArgsHelper.createSearchMapFrom(arg))
                        }
                    },
                    object : Callback<Throwable?> {
                        override fun invoke(arg: Throwable?) {
                            promise.reject(ERR_SUGGEST_FAILED, "suggest request: " + arg?.message)
                        }
                    }
                )
            }
        } else {
            promise.reject(ERR_NO_REQUEST_ARG, "suggest request: text arg is not provided")
            return
        }
    }

    @ReactMethod
    fun addressToGeo(text: String?, promise: Promise) {
        if (text != null) {
            UiThreadUtil.runOnUiThread {
                getSuggestClient(reactApplicationContext).suggestAddress(text,
                    object : Callback<Point?> {
                        override fun invoke(arg: Point?) {
                            val resultPoint = Arguments.createMap()
                            arg?.latitude?.let { resultPoint.putDouble("lat", it) }
                            arg?.longitude?.let { resultPoint.putDouble("lon", it) }
                            promise.resolve(resultPoint)
                        }
                    },
                    object : Callback<Throwable?> {
                        override fun invoke(arg: Throwable?) {
                            promise.reject(ERR_SUGGEST_FAILED, "suggest request: " + arg?.message)
                        }
                    }
                )
            }
        } else {
            promise.reject(ERR_NO_REQUEST_ARG, "suggest request: text arg is not provided")
            return
        }
    }

    @ReactMethod
    fun suggestWithOptions(text: String?, options: ReadableMap?, promise: Promise) {
        if (text == null) {
            promise.reject(ERR_NO_REQUEST_ARG, "suggest request: text arg is not provided")
            return
        }

        UiThreadUtil.runOnUiThread {
            getSuggestClient(reactApplicationContext).suggest(text, options,
                object : Callback<List<MapSuggestItem?>?> {
                    override fun invoke(arg: List<MapSuggestItem?>?) {
                        promise.resolve(argsHelper.createSuggestsMapFrom(arg))
                    }
                },
                object : Callback<Throwable?> {
                    override fun invoke(arg: Throwable?) {
                        promise.reject(ERR_SUGGEST_FAILED, "suggest request: " + arg?.message)
                    }
                }
            )
        }
    }

    @ReactMethod
    fun reset() {
        UiThreadUtil.runOnUiThread { getSuggestClient(reactApplicationContext).resetSuggest() }
    }

    private fun getSuggestClient(context: Context): MapSuggestClient {
        if (suggestClient == null) {
            suggestClient = YandexMapSuggestClient(context)
        }

        return suggestClient as MapSuggestClient
    }

    companion object {
        private const val ERR_NO_REQUEST_ARG = "YANDEX_SUGGEST_ERR_NO_REQUEST_ARG"
        private const val ERR_SUGGEST_FAILED = "YANDEX_SUGGEST_ERR_SUGGEST_FAILED"
    }
}
