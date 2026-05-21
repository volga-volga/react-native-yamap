package ru.yamap.cache

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableMap
import com.yandex.mapkit.offline_cache.Region

class YandexCacheRNArgsHelper {
    fun createCacheRegionMapFrom(data: List<Region?>?): WritableArray {
        val result = Arguments.createArray()

        if (data != null) {
            for (i in data.indices) {
                result.pushMap(data[i]?.let { createCacheRegionFrom(it) })
            }
        }

        return result
    }

    private fun createCacheRegionFrom(data: Region): WritableMap {
        val result = Arguments.createMap()
        result.putInt("id", data.id)
        result.putString("name", data.name)
        result.putDouble("size", data.size.value)
        result.putLong("releaseTime", data.releaseTime)
        if (data.parentId!=null) {
            result.putInt("parentId", data.parentId!!)
        } else {
            result.putNull("parentId")
        }
        return result
    }
}
