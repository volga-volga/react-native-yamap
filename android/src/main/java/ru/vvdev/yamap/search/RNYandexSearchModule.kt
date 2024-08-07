package ru.vvdev.yamap.search

import android.content.Context
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.UiThreadUtil
import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.geometry.LinearRing
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polygon
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.search.SearchOptions
import com.yandex.mapkit.search.SearchType
import com.yandex.mapkit.search.Snippet
import ru.vvdev.yamap.utils.Callback

class RNYandexSearchModule(reactContext: ReactApplicationContext?) :
    ReactContextBaseJavaModule(reactContext) {
    private var searchClient: MapSearchClient? = null
    private val searchArgsHelper = YandexSearchRNArgsHelper()

    override fun getName(): String {
        return "YamapSearch"
    }

    private fun getGeometry(figure: ReadableMap?): Geometry {
        if (figure?.getMap("value")!=null && figure.getString("type") !=null) {
            return when (figure.getString("type")) {
                "POINT" -> {
                   Geometry.fromPoint(Point(figure.getMap("value")!!.getDouble("lat"), figure.getMap("value")!!.getDouble("lon")));
                }

                "BOUNDINGBOX" -> {
                    val southWest = Point(figure.getMap("value")!!.getMap("southWest")!!.getDouble("lat"), figure.getMap("value")!!.getMap("southWest")!!.getDouble("lon"));
                    val northEast = Point(figure.getMap("value")!!.getMap("northEast")!!.getDouble("lat"), figure.getMap("value")!!.getMap("northEast")!!.getDouble("lon"));
                    Geometry.fromBoundingBox(BoundingBox(southWest, northEast));
                }

                "POLYLINE" -> {
                    val polylinePoints: ArrayList<Point> = ArrayList()
                    val points = figure.getMap("value")!!.getArray("points");
                    for (i in 0 until points!!.size()) {
                        val markerMap = points.getMap(i)
                        if (markerMap != null) {
                            val lon = markerMap.getDouble("lon")
                            val lat = markerMap.getDouble("lat")
                            val point = Point(lat, lon)
                            polylinePoints.add(point)
                        }
                    }
                    Geometry.fromPolyline(Polyline(polylinePoints))
                }

                "POLYGON" -> {
                    val polygonPoints: ArrayList<Point> = ArrayList()
                    val points = figure.getMap("value")!!.getArray("points");
                    for (i in 0 until points!!.size()) {
                        val markerMap = points.getMap(i)
                        if (markerMap != null) {
                            val lon = markerMap.getDouble("lon")
                            val lat = markerMap.getDouble("lat")
                            val point = Point(lat, lon)
                            polygonPoints.add(point)
                        }
                    }
                    Geometry.fromPolygon(Polygon(LinearRing(polygonPoints), ArrayList()));
                }

                else -> Geometry.fromPoint(Point(0.0, 0.0))
            }
        }
        return Geometry.fromPoint(Point(0.0, 0.0))
    }

    private fun getSearchOptions(options: ReadableMap?): SearchOptions {
        var searchOptions: SearchOptions? = null
        if (options!=null) {
            searchOptions = SearchOptions().apply {
                searchTypes = if (options.hasKey("searchTypes")) options.getInt("searchTypes") else SearchType.NONE.value
                snippets = if (options.hasKey("snippets")) options.getInt("snippets") else Snippet.NONE.value
                geometry = if (options.hasKey("geometry")) options.getBoolean("geometry") else false
                disableSpellingCorrection = if (options.hasKey("disableSpellingCorrection")) options.getBoolean("disableSpellingCorrection") else false
            }
        } else {
            searchOptions = SearchOptions();
        }
        return searchOptions;
    }

    @ReactMethod
    fun searchByAddress(searchQuery: String?, figure: ReadableMap?, options: ReadableMap?, promise: Promise) {
        if (searchQuery != null) {
            val searchOptions = getSearchOptions(options)
            UiThreadUtil.runOnUiThread {
                getSearchClient(reactApplicationContext).searchAddress(searchQuery, getGeometry(figure), searchOptions,
                    object : Callback<MapSearchItem?> {
                        override fun invoke(arg: MapSearchItem?) {
                            promise.resolve(searchArgsHelper.createSearchMapFrom(arg))
                        }
                    },
                    object : Callback<Throwable?> {
                        override fun invoke(arg: Throwable?) {
                            promise.reject(ERR_SEARCH_FAILED, "search request: " + arg?.message)
                        }
                    }
                )
            }
        } else {
            promise.reject(ERR_NO_REQUEST_ARG, "search request: text arg is not provided")
            return
        }
    }

    @ReactMethod
    fun resolveURI(searchQuery: String?, options: ReadableMap?, promise: Promise) {
        if (searchQuery != null) {
            val searchOptions = getSearchOptions(options)
            UiThreadUtil.runOnUiThread {
                getSearchClient(reactApplicationContext).resolveURI(searchQuery, searchOptions,
                    object : Callback<MapSearchItem?> {
                        override fun invoke(arg: MapSearchItem?) {
                            promise.resolve(searchArgsHelper.createSearchMapFrom(arg))
                        }
                    },
                    object : Callback<Throwable?> {
                        override fun invoke(arg: Throwable?) {
                            promise.reject(ERR_SEARCH_FAILED, "search request: " + arg?.message)
                        }
                    }
                )
            }
        } else {
            promise.reject(ERR_NO_REQUEST_ARG, "search request: text arg is not provided")
            return
        }
    }

    @ReactMethod
    fun searchByURI(searchQuery: String?, options: ReadableMap?, promise: Promise) {
        if (searchQuery != null) {
            val searchOptions = getSearchOptions(options)
            UiThreadUtil.runOnUiThread {
                getSearchClient(reactApplicationContext).searchByURI(searchQuery, searchOptions,
                    object : Callback<MapSearchItem?> {
                        override fun invoke(arg: MapSearchItem?) {
                            promise.resolve(searchArgsHelper.createSearchMapFrom(arg))
                        }
                    },
                    object : Callback<Throwable?> {
                        override fun invoke(arg: Throwable?) {
                            promise.reject(ERR_SEARCH_FAILED, "search request: " + arg?.message)
                        }
                    }
                )
            }
        } else {
            promise.reject(ERR_NO_REQUEST_ARG, "search request: text arg is not provided")
            return
        }
    }

    @ReactMethod
    fun searchByPoint(markerPoint: ReadableMap?, zoom: Double?, options: ReadableMap?, promise: Promise) {
        if (markerPoint != null) {
            val lon = markerPoint.getDouble("lon")
            val lat = markerPoint.getDouble("lat")
            val point = Point(lat, lon)
            UiThreadUtil.runOnUiThread {
                getSearchClient(reactApplicationContext).searchPoint(point, (zoom?.toInt() ?: 10), getSearchOptions(options),
                    object : Callback<MapSearchItem?> {
                        override fun invoke(arg: MapSearchItem?) {
                            promise.resolve(searchArgsHelper.createSearchMapFrom(arg))
                        }
                    },
                    object : Callback<Throwable?> {
                        override fun invoke(arg: Throwable?) {
                            promise.reject(ERR_SEARCH_FAILED, "search request: " + arg?.message)
                        }
                    }
                )
            }
        } else {
            promise.reject(ERR_NO_REQUEST_ARG, "search request: text arg is not provided")
            return
        }
    }

    @ReactMethod
    fun geoToAddress(markerPoint: ReadableMap?, promise: Promise) {
        if (markerPoint != null) {
            val lon = markerPoint.getDouble("lon")
            val lat = markerPoint.getDouble("lat")
            val point = Point(lat, lon)
            UiThreadUtil.runOnUiThread {
                getSearchClient(reactApplicationContext).searchPoint(point, 10, getSearchOptions(null),
                    object : Callback<MapSearchItem?> {
                        override fun invoke(arg: MapSearchItem?) {
                            promise.resolve(searchArgsHelper.createSearchMapFrom(arg))
                        }
                    },
                    object : Callback<Throwable?> {
                        override fun invoke(arg: Throwable?) {
                            promise.reject(ERR_SEARCH_FAILED, "search request: " + arg?.message)
                        }
                    }
                )
            }
        } else {
            promise.reject(ERR_NO_REQUEST_ARG, "search request: text arg is not provided")
            return
        }
    }

    @ReactMethod
    fun addressToGeo(text: String?, promise: Promise) {
        if (text != null) {
            UiThreadUtil.runOnUiThread {
                getSearchClient(reactApplicationContext).searchAddress(text, Geometry.fromPoint(Point(0.0, 0.0)), SearchOptions(),
                    object : Callback<MapSearchItem?> {
                        override fun invoke(arg: MapSearchItem?) {
                            val resultPoint = Arguments.createMap()
                            arg?.point?.latitude?.let { resultPoint.putDouble("lat", it) }
                            arg?.point?.longitude?.let { resultPoint.putDouble("lon", it) }
                            promise.resolve(resultPoint)
                        }
                    },
                    object : Callback<Throwable?> {
                        override fun invoke(arg: Throwable?) {
                            promise.reject(ERR_SEARCH_FAILED, "search request: " + arg?.message)
                        }
                    }
                )
            }
        } else {
            promise.reject(ERR_NO_REQUEST_ARG, "search request: text arg is not provided")
            return
        }
    }

    private fun getSearchClient(context: Context): MapSearchClient {
        if (searchClient == null) {
            searchClient = YandexMapSearchClient(context)
        }

        return searchClient as MapSearchClient
    }

    companion object {
        private const val ERR_NO_REQUEST_ARG = "YANDEX_SEARCH_ERR_NO_REQUEST_ARG"
        private const val ERR_SEARCH_FAILED = "YANDEX_SEARCH_ERR_SEARCH_FAILED"
    }
}
