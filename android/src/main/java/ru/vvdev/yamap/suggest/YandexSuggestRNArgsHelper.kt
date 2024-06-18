package ru.vvdev.yamap.suggest

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableMap

class YandexSuggestRNArgsHelper {
    fun createSuggestsMapFrom(data: List<MapSuggestItem?>?): WritableArray {
        val result = Arguments.createArray()

        if (data != null) {
            for (i in data.indices) {
                result.pushMap(data[i]?.let { createSuggestMapFrom(it) })
            }
        }

        return result
    }

    private fun createSuggestMapFrom(data: MapSuggestItem): WritableMap {
        val result = Arguments.createMap()
        result.putString("title", data.title)
        result.putString("subtitle", data.subtitle)
        result.putString("uri", data.uri)

        return result
    }
}
