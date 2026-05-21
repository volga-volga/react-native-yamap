package ru.yamap.cache

import com.facebook.react.bridge.UiThreadUtil
import com.facebook.react.bridge.WritableArray
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.offline_cache.OfflineCacheManager
import com.yandex.mapkit.offline_cache.Region
import com.yandex.mapkit.offline_cache.RegionListUpdatesListener
import ru.yamap.utils.Callback

class YandexMapCacheClient : MapCacheClient {
    private var _cacheManager: OfflineCacheManager? = null
    private val _argsHelper = YandexCacheRNArgsHelper()
    private var regionsList: List<Region>? = null

    private val listener = RegionListUpdatesListener {
        UiThreadUtil.runOnUiThread {
            regionsList = _cacheManager?.regions()
        }
    }

    override fun initCache() {
        UiThreadUtil.runOnUiThread {
            _cacheManager = MapKitFactory.getInstance().offlineCacheManager
            _cacheManager?.addRegionListUpdatesListener(listener)
        }
    }

    override fun searchRegions(
        onSuccess: Callback<WritableArray>?,
        onError: Callback<Throwable?>?
    ) {
        UiThreadUtil.runOnUiThread {
            if (regionsList != null) {
                onSuccess!!.invoke(_argsHelper.createCacheRegionMapFrom(regionsList))
            }
        }
    }

    override fun getRegionState(
        id: Int,
        onSuccess: Callback<Number?>?,
        onError: Callback<Throwable?>?
    ) {
        UiThreadUtil.runOnUiThread {
            val regionState = _cacheManager?.getState(id)
            onSuccess!!.invoke(regionState?.ordinal)
        }
    }

    override fun startDownloadRegion(
        id: Number,
        onSuccess: Callback<Boolean?>?,
        onError: Callback<Throwable?>?
    ) {
        UiThreadUtil.runOnUiThread {
            val isEmpty = _cacheManager?.mayBeOutOfAvailableSpace(id as Int)
            if (isEmpty == true) {
                onSuccess!!.invoke(false)
            } else {
                _cacheManager?.startDownload(id as Int);
                onSuccess!!.invoke(true)
            }
        }
    }

    override fun stopDownloadRegion(id: Number) {
        TODO("Not yet implemented")
    }

    override fun pauseDownloadRegion(id: Number) {
        TODO("Not yet implemented")
    }

    override fun dropRegion(id: Number) {
        UiThreadUtil.runOnUiThread {
            _cacheManager?.drop(id as Int)
        }
    }

}
