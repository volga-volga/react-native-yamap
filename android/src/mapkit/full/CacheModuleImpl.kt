package ru.yamap.module

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.UiThreadUtil
import com.facebook.react.bridge.WritableArray
import ru.yamap.cache.MapCacheClient
import ru.yamap.cache.RegionEventsListener
import ru.yamap.cache.YandexMapCacheClient
import ru.yamap.utils.Callback

class CacheModuleImpl(private val reactContext: ReactApplicationContext) {
    private var _suggestClient: MapCacheClient? = null

    fun initManager() {
        getCacheClient().setRegionEventsListener(object : RegionEventsListener {
            override fun onRegionStateChanged(regionId: Int, state: Int) {
                emitRegionStateChanged(regionId, state)
            }

            override fun onRegionProgress(regionId: Int, progress: Float) {
                emitRegionProgress(regionId, progress)
            }
        })
        getCacheClient().initCache()
    }

    fun getRegionsList(promise: Promise) {
        UiThreadUtil.runOnUiThread {
            getCacheClient().searchRegions(
                object : Callback<WritableArray> {
                    override fun invoke(arg: WritableArray) {
                        promise.resolve(arg)
                    }
                },
                object : Callback<Throwable?> {
                    override fun invoke(arg: Throwable?) {
                        promise.reject(
                            ERR_SUGGEST_FAILED,
                            "suggest request: " + arg?.message
                        )
                    }
                })
        }
    }

    fun getRegionState(int: Int, promise: Promise) {
        UiThreadUtil.runOnUiThread {
            getCacheClient().getRegionState(
                int, object : Callback<Number?> {
                    override fun invoke(arg: Number?) {
                        promise.resolve(arg)
                    }
                },
                object : Callback<Throwable?> {
                    override fun invoke(arg: Throwable?) {
                        promise.reject(
                            ERR_SUGGEST_FAILED,
                            "suggest request: " + arg?.message
                        )
                    }
                })
        }
    }

    fun startDownloadRegion(int: Int, promise: Promise) {
        UiThreadUtil.runOnUiThread {
            getCacheClient().startDownloadRegion(
                int, object : Callback<Boolean?> {
                    override fun invoke(arg: Boolean?) {
                        promise.resolve(arg)
                    }
                },
                object : Callback<Throwable?> {
                    override fun invoke(arg: Throwable?) {
                        promise.reject(
                            ERR_SUGGEST_FAILED,
                            "suggest request: " + arg?.message
                        )
                    }
                })
        }
    }

    fun dropRegion(int: Int) {
        getCacheClient().dropRegion(int)
    }

    fun addListener(eventName: String?) {
    }

    fun removeListeners(count: Double) {
    }

    private fun emitRegionStateChanged(regionId: Int, state: Int) {
        if (!reactContext.hasActiveReactInstance()) {
            return
        }

        val params = Arguments.createMap().apply {
            putInt("regionId", regionId)
            putInt("state", state)
        }
        reactContext.emitDeviceEvent(EVENT_REGION_STATE_CHANGED, params)
    }

    private fun emitRegionProgress(regionId: Int, progress: Float) {
        if (!reactContext.hasActiveReactInstance()) {
            return
        }

        val params = Arguments.createMap().apply {
            putInt("regionId", regionId)
            putDouble("progress", progress.toDouble())
        }
        reactContext.emitDeviceEvent(EVENT_REGION_PROGRESS, params)
    }

    private fun getCacheClient(): MapCacheClient {
        if (_suggestClient == null) {
            _suggestClient = YandexMapCacheClient()
        }

        return _suggestClient as MapCacheClient
    }

    companion object {
        const val NAME = "RTNCacheModule"
        private const val EVENT_REGION_STATE_CHANGED = "cacheRegionStateChanged"
        private const val EVENT_REGION_PROGRESS = "cacheRegionProgress"

        private const val ERR_NO_REQUEST_ARG = "YANDEX_SUGGEST_ERR_NO_REQUEST_ARG"
        private const val ERR_SUGGEST_FAILED = "YANDEX_SUGGEST_ERR_SUGGEST_FAILED"
    }
}
