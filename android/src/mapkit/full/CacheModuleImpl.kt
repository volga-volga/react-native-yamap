package ru.yamap.module

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.UiThreadUtil
import com.facebook.react.bridge.WritableArray
import ru.yamap.cache.MapCacheClient
import ru.yamap.cache.YandexMapCacheClient
import ru.yamap.utils.Callback

class CacheModuleImpl {
    private var _suggestClient: MapCacheClient? = null

    fun initManager() {
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

    private fun getCacheClient(): MapCacheClient {
        if (_suggestClient == null) {
            _suggestClient = YandexMapCacheClient()
        }

        return _suggestClient as MapCacheClient
    }

    companion object {
        const val NAME = "RTNCacheModule"

        private const val ERR_NO_REQUEST_ARG = "YANDEX_SUGGEST_ERR_NO_REQUEST_ARG"
        private const val ERR_SUGGEST_FAILED = "YANDEX_SUGGEST_ERR_SUGGEST_FAILED"
    }
}
