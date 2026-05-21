package ru.yamap.cache

import com.facebook.react.bridge.WritableArray
import ru.yamap.utils.Callback

interface MapCacheClient {

    fun initCache()
    fun searchRegions(onSuccess: Callback<WritableArray>?,
                      onError: Callback<Throwable?>?)
    fun getRegionState(id: Int, onSuccess: Callback<Number?>?,
                      onError: Callback<Throwable?>?)
    fun startDownloadRegion(id: Number, onSuccess: Callback<Boolean?>?,
                            onError: Callback<Throwable?>?)
    fun stopDownloadRegion(id: Number)
    fun pauseDownloadRegion(id: Number)
    fun dropRegion(id: Number)

}
