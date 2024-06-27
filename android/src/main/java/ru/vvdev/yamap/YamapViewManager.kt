package ru.vvdev.yamap

import android.view.View
import com.facebook.infer.annotation.Assertions
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.common.MapBuilder
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewGroupManager
import com.facebook.react.uimanager.annotations.ReactProp
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import ru.vvdev.yamap.view.YamapView
import javax.annotation.Nonnull

class YamapViewManager internal constructor() : ViewGroupManager<YamapView>() {
    override fun getName(): String {
        return REACT_CLASS
    }

    override fun getExportedCustomDirectEventTypeConstants(): Map<String, Any>? {
        return MapBuilder.builder<String, Any>()
            .build()
    }

    override fun getExportedCustomBubblingEventTypeConstants(): MutableMap<String, Any>? {
        return MapBuilder.builder<String, Any>()
            .put(
                "routes",
                MapBuilder.of("phasedRegistrationNames", MapBuilder.of("bubbled", "onRouteFound"))
            )
            .put(
                "cameraPosition",
                MapBuilder.of(
                    "phasedRegistrationNames",
                    MapBuilder.of("bubbled", "onCameraPositionReceived")
                )
            )
            .put(
                "cameraPositionChange",
                MapBuilder.of(
                    "phasedRegistrationNames",
                    MapBuilder.of("bubbled", "onCameraPositionChange")
                )
            )
            .put(
                "cameraPositionChangeEnd",
                MapBuilder.of(
                    "phasedRegistrationNames",
                    MapBuilder.of("bubbled", "onCameraPositionChangeEnd")
                )
            )
            .put(
                "visibleRegion",
                MapBuilder.of(
                    "phasedRegistrationNames",
                    MapBuilder.of("bubbled", "onVisibleRegionReceived")
                )
            )
            .put(
                "onMapPress",
                MapBuilder.of("phasedRegistrationNames", MapBuilder.of("bubbled", "onMapPress"))
            )
            .put(
                "onMapLongPress",
                MapBuilder.of("phasedRegistrationNames", MapBuilder.of("bubbled", "onMapLongPress"))
            )
            .put(
                "onMapLoaded",
                MapBuilder.of("phasedRegistrationNames", MapBuilder.of("bubbled", "onMapLoaded"))
            )
            .put(
                "screenToWorldPoints",
                MapBuilder.of(
                    "phasedRegistrationNames",
                    MapBuilder.of("bubbled", "onScreenToWorldPointsReceived")
                )
            )
            .put(
                "worldToScreenPoints",
                MapBuilder.of(
                    "phasedRegistrationNames",
                    MapBuilder.of("bubbled", "onWorldToScreenPointsReceived")
                )
            )
            .build()
    }

    override fun getCommandsMap(): Map<String, Int>? {
        val map: MutableMap<String, Int> = MapBuilder.newHashMap()
        map["setCenter"] = SET_CENTER
        map["fitAllMarkers"] = FIT_ALL_MARKERS
        map["findRoutes"] = FIND_ROUTES
        map["setZoom"] = SET_ZOOM
        map["getCameraPosition"] = GET_CAMERA_POSITION
        map["getVisibleRegion"] = GET_VISIBLE_REGION
        map["setTrafficVisible"] = SET_TRAFFIC_VISIBLE
        map["fitMarkers"] = FIT_MARKERS
        map["getScreenPoints"] = GET_SCREEN_POINTS
        map["getWorldPoints"] = GET_WORLD_POINTS

        return map
    }

    override fun receiveCommand(
        view: YamapView,
        commandType: String,
        args: ReadableArray?
    ) {
        Assertions.assertNotNull(view)
        Assertions.assertNotNull(args)

        when (commandType) {
            "setCenter" -> setCenter(
                castToYaMapView(view),
                args!!.getMap(0),
                args.getDouble(1).toFloat(),
                args.getDouble(2).toFloat(),
                args.getDouble(3).toFloat(),
                args.getDouble(4).toFloat(),
                args.getInt(5)
            )

            "fitAllMarkers" -> fitAllMarkers(view)
            "fitMarkers" -> if (args != null) {
                fitMarkers(view, args.getArray(0))
            }

            "findRoutes" -> if (args != null) {
                findRoutes(view, args.getArray(0), args.getArray(1), args.getString(2))
            }

            "setZoom" -> if (args != null) {
                view.setZoom(
                    args.getDouble(0).toFloat(),
                    args.getDouble(1).toFloat(),
                    args.getInt(2)
                )
            }

            "getCameraPosition" -> if (args != null) {
                view.emitCameraPositionToJS(args.getString(0))
            }

            "getVisibleRegion" -> if (args != null) {
                view.emitVisibleRegionToJS(args.getString(0))
            }

            "setTrafficVisible" -> if (args != null) {
                view.setTrafficVisible(args.getBoolean(0))
            }

            "getScreenPoints" -> if (args != null) {
                view.emitWorldToScreenPoints(args.getArray(0), args.getString(1))
            }

            "getWorldPoints" -> if (args != null) {
                view.emitScreenToWorldPoints(args.getArray(0), args.getString(1))
            }

            else -> throw IllegalArgumentException(
                String.format(
                    "Unsupported command %d received by %s.",
                    commandType,
                    javaClass.simpleName
                )
            )
        }
    }

    private fun castToYaMapView(view: View): YamapView {
        return view as YamapView
    }

    @Nonnull
    public override fun createViewInstance(@Nonnull context: ThemedReactContext): YamapView {
        val view = YamapView(context)
        MapKitFactory.getInstance().onStart()
        view.onStart()

        return view
    }

    private fun setCenter(
        view: YamapView,
        center: ReadableMap?,
        zoom: Float,
        azimuth: Float,
        tilt: Float,
        duration: Float,
        animation: Int
    ) {
        if (center != null) {
            val centerPosition = Point(center.getDouble("lat"), center.getDouble("lon"))
            val pos = CameraPosition(centerPosition, zoom, azimuth, tilt)
            view.setCenter(pos, duration, animation)
        }
    }

    private fun fitAllMarkers(view: View) {
        castToYaMapView(view).fitAllMarkers()
    }

    private fun fitMarkers(view: View, jsPoints: ReadableArray?) {
        if (jsPoints != null) {
            val points = ArrayList<Point?>()

            for (i in 0 until jsPoints.size()) {
                val point = jsPoints.getMap(i)
                if (point != null) {
                    points.add(Point(point.getDouble("lat"), point.getDouble("lon")))
                }
            }

            castToYaMapView(view).fitMarkers(points)
        }
    }

    private fun findRoutes(
        view: View,
        jsPoints: ReadableArray?,
        jsVehicles: ReadableArray?,
        id: String
    ) {
        if (jsPoints != null) {
            val points = ArrayList<Point?>()

            for (i in 0 until jsPoints.size()) {
                val point = jsPoints.getMap(i)
                if (point != null) {
                    points.add(Point(point.getDouble("lat"), point.getDouble("lon")))
                }
            }

            val vehicles = ArrayList<String>()

            if (jsVehicles != null) {
                for (i in 0 until jsVehicles.size()) {
                    vehicles.add(jsVehicles.getString(i))
                }
            }

            castToYaMapView(view).findRoutes(points, vehicles, id)
        }
    }

    // PROPS
    @ReactProp(name = "userLocationIcon")
    fun setUserLocationIcon(view: View, icon: String?) {
        if (icon != null) {
            castToYaMapView(view).setUserLocationIcon(icon)
        }
    }

    @ReactProp(name = "userLocationIconScale")
    fun setUserLocationIconScale(view: View, scale: Float) {
        castToYaMapView(view).setUserLocationIconScale(scale)
    }

    @ReactProp(name = "userLocationAccuracyFillColor")
    fun setUserLocationAccuracyFillColor(view: View, color: Int) {
        castToYaMapView(view).setUserLocationAccuracyFillColor(color)
    }

    @ReactProp(name = "userLocationAccuracyStrokeColor")
    fun setUserLocationAccuracyStrokeColor(view: View, color: Int) {
        castToYaMapView(view).setUserLocationAccuracyStrokeColor(color)
    }

    @ReactProp(name = "userLocationAccuracyStrokeWidth")
    fun setUserLocationAccuracyStrokeWidth(view: View, width: Float) {
        castToYaMapView(view).setUserLocationAccuracyStrokeWidth(width)
    }

    @ReactProp(name = "showUserPosition")
    fun setShowUserPosition(view: View, show: Boolean?) {
        castToYaMapView(view).setShowUserPosition(show!!)
    }

    @ReactProp(name = "nightMode")
    fun setNightMode(view: View, nightMode: Boolean?) {
        castToYaMapView(view).setNightMode(nightMode ?: false)
    }

    @ReactProp(name = "scrollGesturesEnabled")
    fun setScrollGesturesEnabled(view: View, scrollGesturesEnabled: Boolean) {
        castToYaMapView(view).setScrollGesturesEnabled(scrollGesturesEnabled == true)
    }

    @ReactProp(name = "rotateGesturesEnabled")
    fun setRotateGesturesEnabled(view: View, rotateGesturesEnabled: Boolean) {
        castToYaMapView(view).setRotateGesturesEnabled(rotateGesturesEnabled == true)
    }

    @ReactProp(name = "zoomGesturesEnabled")
    fun setZoomGesturesEnabled(view: View, zoomGesturesEnabled: Boolean) {
        castToYaMapView(view).setZoomGesturesEnabled(zoomGesturesEnabled == true)
    }

    @ReactProp(name = "tiltGesturesEnabled")
    fun setTiltGesturesEnabled(view: View, tiltGesturesEnabled: Boolean) {
        castToYaMapView(view).setTiltGesturesEnabled(tiltGesturesEnabled == true)
    }

    @ReactProp(name = "fastTapEnabled")
    fun setFastTapEnabled(view: View, fastTapEnabled: Boolean) {
        castToYaMapView(view).setFastTapEnabled(fastTapEnabled == true)
    }

    @ReactProp(name = "mapStyle")
    fun setMapStyle(view: View, style: String?) {
        if (style != null) {
            castToYaMapView(view).setMapStyle(style)
        }
    }

    @ReactProp(name = "mapType")
    fun setMapType(view: View, type: String?) {
        if (type != null) {
            castToYaMapView(view).setMapType(type)
        }
    }

    @ReactProp(name = "initialRegion")
    fun setInitialRegion(view: View, params: ReadableMap?) {
        if (params != null) {
            castToYaMapView(view).setInitialRegion(params)
        }
    }

    @ReactProp(name = "maxFps")
    fun setMaxFps(view: View, maxFps: Float) {
        castToYaMapView(view).setMaxFps(maxFps)
    }

    @ReactProp(name = "interactive")
    fun setInteractive(view: View, interactive: Boolean) {
        castToYaMapView(view).setInteractive(interactive)
    }

    @ReactProp(name = "logoPosition")
    fun setLogoPosition(view: View, params: ReadableMap?) {
        if (params != null) {
            castToYaMapView(view).setLogoPosition(params)
        }
    }

    @ReactProp(name = "logoPadding")
    fun setLogoPadding(view: View, params: ReadableMap?) {
        if (params != null) {
            castToYaMapView(view).setLogoPadding(params)
        }
    }

    override fun addView(parent: YamapView, child: View, index: Int) {
        parent.addFeature(child, index)
        super.addView(parent, child, index)
    }

    override fun removeViewAt(parent: YamapView, index: Int) {
        parent.removeChild(index)
        super.removeViewAt(parent, index)
    }

    companion object {
        const val REACT_CLASS: String = "YamapView"

        private const val SET_CENTER = 1
        private const val FIT_ALL_MARKERS = 2
        private const val FIND_ROUTES = 3
        private const val SET_ZOOM = 4
        private const val GET_CAMERA_POSITION = 5
        private const val GET_VISIBLE_REGION = 6
        private const val SET_TRAFFIC_VISIBLE = 7
        private const val FIT_MARKERS = 8
        private const val GET_SCREEN_POINTS = 9
        private const val GET_WORLD_POINTS = 10
    }
}
