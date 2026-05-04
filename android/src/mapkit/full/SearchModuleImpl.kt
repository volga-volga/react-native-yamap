package ru.yamap.module

import com.facebook.react.bridge.Promise
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
import ru.yamap.search.MapSearchClient
import ru.yamap.search.MapSearchItem
import ru.yamap.search.YandexMapSearchClient
import ru.yamap.search.YandexSearchRNArgsHelper
import ru.yamap.utils.Callback
import ru.yamap.utils.PointUtil

class SearchModuleImpl {
    private var searchClient: MapSearchClient? = null
    private val searchArgsHelper = YandexSearchRNArgsHelper()

    private fun getGeometry(figure: ReadableMap?): Geometry {
        if (figure?.getMap("value")!=null && figure.getString("type") !=null) {
            return when (figure.getString("type")) {
                "POINT" -> {
                    val jsPoint = figure.getMap("value")
                    val point = jsPoint?.let { PointUtil.readableMapToPoint(it) }
                    Geometry.fromPoint(point ?: Point(0.0, 0.0))
                }

                "BOUNDINGBOX" -> {
                    val value = figure.getMap("value") ?:
                    return Geometry.fromPoint(Point(0.0, 0.0))

                    val southWestJsPoint = value.getMap("southWest")
                    val southWestPoint = southWestJsPoint?.let { PointUtil.readableMapToPoint(it) }

                    val northEastJsPoint = value.getMap("northEast")
                    val northEastPoint = northEastJsPoint?.let { PointUtil.readableMapToPoint(it) }

                    Geometry.fromBoundingBox(BoundingBox(
                        southWestPoint ?: Point(0.0, 0.0),
                        northEastPoint ?: Point(0.0, 0.0)
                    ))
                }

                "POLYLINE" -> {
                    val value = figure.getMap("value") ?:
                    return Geometry.fromPoint(Point(0.0, 0.0))

                    val jsPoints = value.getArray("points") ?:
                    return Geometry.fromPoint(Point(0.0, 0.0))

                    val points = PointUtil.jsPointsToPoints(jsPoints)
                    Geometry.fromPolyline(Polyline(points))
                }

                "POLYGON" -> {
                    val value = figure.getMap("value") ?:
                    return Geometry.fromPoint(Point(0.0, 0.0))

                    val jsPoints = value.getArray("points") ?:
                    return Geometry.fromPoint(Point(0.0, 0.0))

                    val points = PointUtil.jsPointsToPoints(jsPoints)
                    Geometry.fromPolygon(Polygon(LinearRing(points), ArrayList()));
                }

                else -> Geometry.fromPoint(Point(0.0, 0.0))
            }
        }
        return Geometry.fromPoint(Point(0.0, 0.0))
    }

    private fun stringToSearchType(str: String): SearchType {
        if (str == "GEO") {
            return SearchType.GEO
        }
        if (str == "BIZ") {
            return SearchType.BIZ
        }
        return SearchType.NONE
    }

    private fun stringToSnippet(str: String): Snippet {
        if (str == "PHOTOS") {
            return Snippet.PHOTOS
        }
        if (str == "PANORAMAS") {
            return Snippet.PANORAMAS
        }
        return Snippet.NONE
    }

    private fun getSearchOptions(options: ReadableMap?): SearchOptions {
        return if (options!=null) {
            SearchOptions().apply {
                searchTypes = SearchType.NONE.value
                if (options.hasKey("searchTypes") && !options.isNull("searchTypes")) {
                    val searchTypesArray = options.getArray("searchTypes")
                    for (i in 0 until searchTypesArray!!.size()) {
                        if (searchTypesArray.getString(i) != null) {
                            val value = stringToSearchType(searchTypesArray.getString(i)!!)
                            searchTypes = searchTypes or value.value
                        }
                    }
                }

                snippets = Snippet.NONE.value
                if (options.hasKey("snippets") && !options.isNull("snippets")) {
                    val snippetsArray = options.getArray("snippets")
                    for (i in 0 until snippetsArray!!.size()) {
                        if (snippetsArray.getString(i) != null) {
                            val value = stringToSnippet(snippetsArray.getString(i)!!)
                            snippets = snippets or value.value
                        }
                    }
                }

                geometry = if (options.hasKey("geometry")) options.getBoolean("geometry") else false
                disableSpellingCorrection = if (options.hasKey("disableSpellingCorrection")) options.getBoolean("disableSpellingCorrection") else false
            }
        } else {
            SearchOptions();
        }
    }

    fun searchByAddress(searchQuery: String?, figure: ReadableMap?, options: ReadableMap?, promise: Promise) {
        if (searchQuery != null) {
            val searchOptions = getSearchOptions(options)
            UiThreadUtil.runOnUiThread {
                getSearchClient().searchAddress(searchQuery, getGeometry(figure), searchOptions,
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

    fun resolveURI(searchQuery: String?, options: ReadableMap?, promise: Promise) {
        if (searchQuery != null) {
            val searchOptions = getSearchOptions(options)
            UiThreadUtil.runOnUiThread {
                getSearchClient().resolveURI(searchQuery, searchOptions,
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

    fun searchByURI(searchQuery: String?, options: ReadableMap?, promise: Promise) {
        if (searchQuery != null) {
            val searchOptions = getSearchOptions(options)
            UiThreadUtil.runOnUiThread {
                getSearchClient().searchByURI(searchQuery, searchOptions,
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

    fun searchByPoint(jsPoint: ReadableMap?, zoom: Double?, options: ReadableMap?, promise: Promise) {
        if (jsPoint === null) {
            promise.reject(ERR_NO_REQUEST_ARG, "search request: text arg is not provided")
            return
        }

        val point = PointUtil.readableMapToPoint(jsPoint)
        UiThreadUtil.runOnUiThread {
            getSearchClient().searchPoint(point, (zoom?.toInt() ?: 10), getSearchOptions(options),
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
    }

    fun geoToAddress(jsPoint: ReadableMap?, promise: Promise) {
        if (jsPoint === null) {
            promise.reject(ERR_NO_REQUEST_ARG, "search request: text arg is not provided")
            return
        }

        val point = PointUtil.readableMapToPoint(jsPoint)
        UiThreadUtil.runOnUiThread {
            getSearchClient().searchPoint(point, 10, getSearchOptions(null),
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
    }

    fun addressToGeo(text: String?, promise: Promise) {
        if (text != null) {
            UiThreadUtil.runOnUiThread {
                getSearchClient().searchAddress(text, Geometry.fromPoint(Point(0.0, 0.0)), SearchOptions(),
                    object : Callback<MapSearchItem?> {
                        override fun invoke(arg: MapSearchItem?) {
                            val jsPoint = PointUtil.pointToJsPoint(arg?.point)
                            promise.resolve(jsPoint)
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

    private fun getSearchClient(): MapSearchClient {
        if (searchClient == null) {
            searchClient = YandexMapSearchClient()
        }

        return searchClient as MapSearchClient
    }

    companion object {
        const val NAME = "RTNSearchModule"

        private const val ERR_NO_REQUEST_ARG = "YANDEX_SEARCH_ERR_NO_REQUEST_ARG"
        private const val ERR_SEARCH_FAILED = "YANDEX_SEARCH_ERR_SEARCH_FAILED"
    }
}
