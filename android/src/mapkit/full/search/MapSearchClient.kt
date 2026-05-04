package ru.yamap.search

import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.search.SearchOptions
import ru.yamap.utils.Callback

interface MapSearchClient {
    fun searchPoint(
        point: Point,
        zoom: Int,
        options: SearchOptions,
        onSuccess: Callback<MapSearchItem?>,
        onError: Callback<Throwable?>?
    )

    fun searchAddress(
        text: String,
        geometry: Geometry,
        options: SearchOptions,
        onSuccess: Callback<MapSearchItem?>,
        onError: Callback<Throwable?>?
    )

    fun resolveURI(
        uri: String,
        options: SearchOptions,
        onSuccess: Callback<MapSearchItem?>,
        onError: Callback<Throwable?>?
    )

    fun searchByURI(
        uri: String,
        options: SearchOptions,
        onSuccess: Callback<MapSearchItem?>,
        onError: Callback<Throwable?>?
    )
}
