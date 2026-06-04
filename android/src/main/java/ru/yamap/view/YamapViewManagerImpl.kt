package ru.yamap.view

import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.uimanager.ThemedReactContext
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import ru.yamap.events.yamap.CameraPositionChangeEndEvent
import ru.yamap.events.yamap.CameraPositionChangeEvent
import ru.yamap.events.yamap.FindRoutesEvent
import ru.yamap.events.yamap.GetCameraPositionEvent
import ru.yamap.events.yamap.GetScreenToWorldPointsEvent
import ru.yamap.events.yamap.GetVisibleRegionEvent
import ru.yamap.events.yamap.GetWorldToScreenPointsEvent
import ru.yamap.events.yamap.MapLoadedEvent
import ru.yamap.events.yamap.YamapLongPressEvent
import ru.yamap.events.yamap.YamapPressEvent
import ru.yamap.utils.PointUtil

class YamapViewManagerImpl() {

    fun receiveCommand(view: YamapView, commandType: String, argsArr: ReadableArray?) {
        val args = argsArr?.getArray(0)?.getMap(0) ?: return

        when (commandType) {
            "setCenter" -> setCenter(
                view,
                args.getMap("center"),
                args.getDouble("zoom").toFloat(),
                args.getDouble("azimuth").toFloat(),
                args.getDouble("tilt").toFloat(),
                args.getDouble("duration").toFloat(),
                args.getInt("animation")
            )
            "fitAllMarkers" -> fitAllMarkers(
                view,
                args.getDouble("duration").toFloat(),
                args.getInt("animation")
            )
            "fitMarkers" -> fitMarkers(
                view,
                args.getArray("points"),
                args.getDouble("duration").toFloat(),
                args.getInt("animation")
            )
            "setZoom" -> view.setZoom(
                args.getDouble("zoom").toFloat(),
                args.getDouble("duration").toFloat(),
                args.getInt("animation")
            )
            "getCameraPosition" -> view.emitCameraPositionToJS(args.getString("id"))
            "getVisibleRegion" -> view.emitVisibleRegionToJS(args.getString("id"))
            "setTrafficVisible" -> view.setTrafficVisible(args.getBoolean("isVisible"))
            "getScreenPoints" -> view.emitWorldToScreenPoints(
                args.getArray("points"),
                args.getString("id")
            )
            "getWorldPoints" -> view.emitScreenToWorldPoints(
                args.getArray("points"),
                args.getString("id")
            )
            "getUserPosition" -> view.emitGetUserPosition(
                args.getString("id")
            )

            else -> throw IllegalArgumentException(
                String.format(
                    "Unsupported command %d received by %s.",
                    commandType,
                    javaClass.simpleName
                )
            )
        }
    }

    fun createViewInstance(context: ThemedReactContext): YamapView {
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
        center?.let {
            val point = PointUtil.readableMapToPoint(it)
            val position = CameraPosition(point, zoom, azimuth, tilt)
            view.setCenter(position, duration, animation)
        }
    }

    private fun fitAllMarkers(view: YamapView, duration: Float, animation: Int) {
        view.fitAllMarkers(duration, animation)
    }

    private fun fitMarkers(view: YamapView, jsPoints: ReadableArray?, duration: Float, animation: Int) {
        jsPoints?.let {
            val points = PointUtil.jsPointsToPoints(it)
            view.fitMarkers(points, duration, animation)
        }
    }

    // PROPS
    fun setUserLocationIcon(view: YamapView, icon: String?) {
        icon?.let {
            view.setUserLocationIcon(it)
        }
    }

    fun setUserLocationIconScale(view: YamapView, scale: Float) {
        view.setUserLocationIconScale(scale)
    }

    fun setUserLocationAccuracyFillColor(view: YamapView, color: Int) {
        view.setUserLocationAccuracyFillColor(color)
    }

    fun setUserLocationAccuracyStrokeColor(view: YamapView, color: Int) {
        view.setUserLocationAccuracyStrokeColor(color)
    }

    fun setUserLocationAccuracyStrokeWidth(view: YamapView, width: Float) {
        view.setUserLocationAccuracyStrokeWidth(width)
    }

    fun setShowUserPosition(view: YamapView, show: Boolean) {
        view.setShowUserPosition(show)
    }

    fun setNightMode(view: YamapView, nightMode: Boolean) {
        view.setNightMode(nightMode)
    }

    fun setScrollGesturesDisabled(view: YamapView, value: Boolean) {
        view.setScrollGesturesDisabled(value)
    }

    fun setRotateGesturesDisabled(view: YamapView, value: Boolean) {
        view.setRotateGesturesDisabled(value)
    }

    fun setZoomGesturesDisabled(view: YamapView, value: Boolean) {
        view.setZoomGesturesDisabled(value)
    }

    fun setTiltGesturesDisabled(view: YamapView, value: Boolean) {
        view.setTiltGesturesDisabled(value)
    }

    fun setFastTapDisabled(view: YamapView, value: Boolean) {
        view.setFastTapDisabled(value)
    }

    fun setMapStyle(view: YamapView, style: String?) {
        style?.let {
            view.setMapStyle(it)
        }
    }

    fun setMapType(view: YamapView, type: String?) {
        type?.let {
            view.setMapType(it)
        }
    }

    fun setInitialRegion(view: YamapView, params: ReadableMap?) {
        params?.let {
            view.setInitialRegion(it)
        }
    }

    fun setInteractiveDisabled(view: YamapView, value: Boolean) {
        view.setInteractiveDisabled(value)
    }

    fun setLogoPosition(view: YamapView, params: ReadableMap?) {
        params?.let {
            view.setLogoPosition(it)
        }
    }

    fun setLogoPadding(view: YamapView, params: ReadableMap?) {
        params?.let {
            view.setLogoPadding(it)
        }
    }

    fun setFollowUser(view: YamapView, value: Boolean) {
        view.setFollowUser(value)
    }

    fun setMinZoom(view: YamapView, value: Float) {
        view.setMinZoom(value)
    }

    fun setMaxZoom(view: YamapView, value: Float) {
        view.setMaxZoom(value)
    }

    companion object {
        const val NAME = "YamapView"

        val exportedCustomBubblingEventTypeConstants = mutableMapOf(
            YamapPressEvent.EVENT_NAME to
                    mapOf("phasedRegistrationNames" to mapOf("bubbled" to "onMapPress")),
            YamapLongPressEvent.EVENT_NAME to
                    mapOf("phasedRegistrationNames" to mapOf("bubbled" to "onMapLongPress")),
        )

        val exportedCustomDirectEventTypeConstants = mutableMapOf(
            FindRoutesEvent.EVENT_NAME to
                    mapOf("registrationName" to "onRouteFound"),
            GetCameraPositionEvent.EVENT_NAME to
                    mapOf("registrationName" to "onCameraPositionReceived"),
            CameraPositionChangeEvent.EVENT_NAME to
                    mapOf("registrationName" to "onCameraPositionChange"),
            CameraPositionChangeEndEvent.EVENT_NAME to
                    mapOf("registrationName" to "onCameraPositionChangeEnd"),
            GetVisibleRegionEvent.EVENT_NAME to
                    mapOf("registrationName" to "onVisibleRegionReceived"),
            MapLoadedEvent.EVENT_NAME to
                    mapOf("registrationName" to "onMapLoaded"),
            GetScreenToWorldPointsEvent.EVENT_NAME to
                    mapOf("registrationName" to "onScreenToWorldPointsReceived"),
            GetWorldToScreenPointsEvent.EVENT_NAME to
                    mapOf("registrationName" to "onWorldToScreenPointsReceived"),
        )

        val commandsMap = mapOf(
            "setCenter" to 1,
            "fitAllMarkers" to 2,
            "setZoom" to 3,
            "getCameraPosition" to 4,
            "getVisibleRegion" to 5,
            "setTrafficVisible" to 6,
            "fitMarkers" to 7,
            "getScreenPoints" to 8,
            "getWorldPoints" to 9,
        )
    }
}
