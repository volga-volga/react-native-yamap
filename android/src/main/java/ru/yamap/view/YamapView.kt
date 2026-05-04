package ru.yamap.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PointF
import android.view.MotionEvent
import android.view.View
import android.view.ViewParent
import androidx.core.graphics.createBitmap
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.UiThreadUtil.runOnUiThread
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.UIManagerHelper
import com.facebook.react.uimanager.UIManagerHelper.getSurfaceId
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.ScreenPoint
import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.logo.Alignment
import com.yandex.mapkit.logo.HorizontalAlignment
import com.yandex.mapkit.logo.Padding
import com.yandex.mapkit.logo.VerticalAlignment
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CameraUpdateReason
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.MapLoadStatistics
import com.yandex.mapkit.map.MapLoadedListener
import com.yandex.mapkit.map.MapType
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.traffic.TrafficLayer
import com.yandex.mapkit.traffic.TrafficLevel
import com.yandex.mapkit.traffic.TrafficListener
import com.yandex.mapkit.transport.TransportFactory
import com.yandex.mapkit.transport.masstransit.Route
import com.yandex.mapkit.transport.masstransit.RouteOptions
import com.yandex.mapkit.transport.masstransit.Session
import com.yandex.mapkit.transport.masstransit.TimeOptions
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.image.ImageProvider
import ru.yamap.events.yamap.CameraPositionChangeEndEvent
import ru.yamap.events.yamap.CameraPositionChangeEvent
import ru.yamap.events.yamap.GetCameraPositionEvent
import ru.yamap.events.yamap.GetScreenToWorldPointsEvent
import ru.yamap.events.yamap.GetVisibleRegionEvent
import ru.yamap.events.yamap.GetWorldToScreenPointsEvent
import ru.yamap.events.yamap.MapLoadedEvent
import ru.yamap.events.yamap.YamapLongPressEvent
import ru.yamap.events.yamap.YamapPressEvent
import ru.yamap.models.ReactMapObject
import ru.yamap.utils.ImageCacheManager
import ru.yamap.utils.PointUtil
import javax.annotation.Nonnull

open class YamapView(context: Context?) : MapView(context), UserLocationObjectListener,
    CameraListener, InputListener, TrafficListener, MapLoadedListener {
    private var mViewParent: ViewParent? = null
    private var userLocationIcon = ""
    private var userLocationIconScale = 1f
    private var userLocationBitmap: Bitmap? = null
    private var userLocationLayer: UserLocationLayer? = null
    private var userLocationAccuracyFillColor = 0
    private var userLocationAccuracyStrokeColor = 0
    private var userLocationAccuracyStrokeWidth = 0f
    private var trafficLayer: TrafficLayer? = null
    private var initializedRegion = false
    private var userLocationView: UserLocationView? = null

    private val pedestrianRouter = TransportFactory.getInstance().createPedestrianRouter()

    init {
        mapWindow.map.addCameraListener(this)
        mapWindow.map.addInputListener(this)
        mapWindow.map.setMapLoadedListener(this)
        map.cameraBounds.setMinZoomPreference(15f)  // минимальный зум
        map.cameraBounds.setMaxZoomPreference(20f)  // максимальный зум
    }

    // REF
    fun setCenter(position: CameraPosition?, duration: Float, animation: Int) {
        if (duration > 0) {
            val anim = if (animation == 0) Animation.Type.SMOOTH else Animation.Type.LINEAR
            mapWindow.map.move(position!!, Animation(anim, duration), null)
        } else {
            mapWindow.map.move(position!!)
        }
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> if (null == mViewParent) {
                parent.requestDisallowInterceptTouchEvent(true)
            } else {
                mViewParent!!.requestDisallowInterceptTouchEvent(true)
            }

            MotionEvent.ACTION_UP -> if (null == mViewParent) {
                parent.requestDisallowInterceptTouchEvent(false)
            } else {
                mViewParent!!.requestDisallowInterceptTouchEvent(false)
            }

            else -> {}
        }
        return super.onInterceptTouchEvent(event)
    }

    private fun positionToJSON(
        position: CameraPosition,
        reason: CameraUpdateReason,
        finished: Boolean
    ): WritableMap {
        val cameraPosition = Arguments.createMap()
        val point = position.target
        cameraPosition.putDouble("azimuth", position.azimuth.toDouble())
        cameraPosition.putDouble("tilt", position.tilt.toDouble())
        cameraPosition.putDouble("zoom", position.zoom.toDouble())
        val target = Arguments.createMap()
        target.putDouble("lat", point.latitude)
        target.putDouble("lon", point.longitude)
        cameraPosition.putMap("point", target)
        cameraPosition.putString("reason", reason.toString())
        cameraPosition.putBoolean("finished", finished)

        return cameraPosition
    }

    private fun screenPointToJSON(screenPoint: ScreenPoint?): WritableMap {
        val result = Arguments.createMap()

        result.putDouble("x", screenPoint!!.x.toDouble())
        result.putDouble("y", screenPoint.y.toDouble())

        return result
    }

    private fun worldPointToJSON(worldPoint: Point?): WritableMap {
        return PointUtil.pointToJsPoint(worldPoint)
    }

    fun emitCameraPositionToJS(getCameraPositionId: String?) {
        val position = mapWindow.map.cameraPosition
        val eventData =
            positionToJSON(position, CameraUpdateReason.valueOf("APPLICATION"), true)
        eventData.putString("id", getCameraPositionId)

        val eventDispatcher = UIManagerHelper.getEventDispatcherForReactTag(context as ThemedReactContext, id)
        eventDispatcher?.dispatchEvent(GetCameraPositionEvent(
            getSurfaceId(context),
            id,
            eventData
        ))
    }

    fun emitVisibleRegionToJS(getVisibleRegionId: String?) {
        val eventDispatcher = UIManagerHelper.getEventDispatcherForReactTag(context as ThemedReactContext, id)
        eventDispatcher?.dispatchEvent(GetVisibleRegionEvent(
            getSurfaceId(context),
            id,
            mapWindow.map.visibleRegion,
            getVisibleRegionId
        ))
    }

    fun emitWorldToScreenPoints(worldPoints: ReadableArray?, getWorldToScreenPointsId: String?) {
        val screenPoints = Arguments.createArray()

        if (worldPoints !== null && worldPoints.size() > 0) {
            for (i in 0 until worldPoints.size()) {
                worldPoints.getMap(i)?.let {
                    val worldPoint = PointUtil.readableMapToPoint(it)
                    val screenPoint = mapWindow.worldToScreen(worldPoint)
                    screenPoints.pushMap(screenPointToJSON(screenPoint))
                }
            }
        }

        val eventData = Arguments.createMap()
        eventData.putString("id", getWorldToScreenPointsId)
        eventData.putArray("screenPoints", screenPoints)

        val eventDispatcher = UIManagerHelper.getEventDispatcherForReactTag(context as ThemedReactContext, id)
        eventDispatcher?.dispatchEvent(GetWorldToScreenPointsEvent(
            getSurfaceId(context),
            id,
            eventData
        ))
    }

    fun emitGetUserPosition(getWorldToScreenPointsId: String?) {

        if (userLocationLayer == null) {
            userLocationLayer = MapKitFactory.getInstance().createUserLocationLayer(mapWindow)
        }

        val userPosition = userLocationLayer?.cameraPosition()
        val eventData = Arguments.createMap()
        eventData.putString("id", getWorldToScreenPointsId)
        eventData.putMap("point", PointUtil.pointToJsPoint(userPosition!!.target))
        eventData.putDouble("zoom", userPosition.zoom.toDouble())
        eventData.putDouble("tilt", userPosition.tilt.toDouble())
        eventData.putDouble("azimuth", userPosition.azimuth.toDouble())
        eventData.putDouble("azimuth", userPosition.azimuth.toDouble())

        val eventDispatcher = UIManagerHelper.getEventDispatcherForReactTag(context as ThemedReactContext, id)
        eventDispatcher?.dispatchEvent(GetWorldToScreenPointsEvent(
            getSurfaceId(context),
            id,
            eventData
        ))
    }

    fun emitScreenToWorldPoints(screenPoints: ReadableArray?, getScreenToWorldPointsId: String?) {
        val worldPoints = Arguments.createArray()

        if (screenPoints !== null && screenPoints.size() > 0) {
            for (i in 0 until screenPoints.size()) {
                screenPoints.getMap(i)?.let {
                    val screenPoint =
                        ScreenPoint(it.getDouble("x").toFloat(), it.getDouble("y").toFloat())
                    val worldPoint = mapWindow.screenToWorld(screenPoint)
                    worldPoints.pushMap(worldPointToJSON(worldPoint))
                }
            }
        }

        val eventData = Arguments.createMap()
        eventData.putString("id", getScreenToWorldPointsId)
        eventData.putArray("worldPoints", worldPoints)

        val eventDispatcher = UIManagerHelper.getEventDispatcherForReactTag(context as ThemedReactContext, id)
        eventDispatcher?.dispatchEvent(GetScreenToWorldPointsEvent(
            getSurfaceId(context),
            id,
            eventData
        ))
    }

    fun setZoom(zoom: Float?, duration: Float, animation: Int) {
        val prevPosition = mapWindow.map.cameraPosition
        val position =
            CameraPosition(prevPosition.target, zoom!!, prevPosition.azimuth, prevPosition.tilt)
        setCenter(position, duration, animation)
    }

    fun fitAllMarkers(duration: Float, animation: Int) {
        val points = ArrayList<Point>()
        for (i in 0 until childCount) {
            val obj: Any = getChildAt(i)
            if (obj is MarkerView) {
                obj.point?.let { points.add(it) }
            }
        }
        fitMarkers(points, duration, animation)
    }

    private fun calculateBoundingBox(points: ArrayList<Point>): BoundingBox {
        var minLon = points[0].longitude
        var maxLon = points[0].longitude
        var minLat = points[0].latitude
        var maxLat = points[0].latitude

        for (i in points.indices) {
            if (points[i].longitude > maxLon) {
                maxLon = points[i].longitude
            }

            if (points[i].longitude < minLon) {
                minLon = points[i].longitude
            }

            if (points[i].latitude > maxLat) {
                maxLat = points[i].latitude
            }

            if (points[i].latitude < minLat) {
                minLat = points[i].latitude
            }
        }

        val southWest = Point(minLat, minLon)
        val northEast = Point(maxLat, maxLon)

        val boundingBox = BoundingBox(southWest, northEast)
        return boundingBox
    }

    fun fitMarkers(points: ArrayList<Point>, duration: Float, animation: Int) {
        if (points.isEmpty()) {
            return
        }

        val anim = Animation(
            if (animation == 0) Animation.Type.SMOOTH else Animation.Type.LINEAR,
            duration
        )

        if (points.size == 1) {
            val center = Point(
                points[0].latitude, points[0].longitude
            )
            mapWindow.map.move(
                CameraPosition(center, 15f, 0f, 0f),
                anim,
                null
            )
            return
        }
        var cameraPosition = mapWindow.map.cameraPosition(Geometry.fromBoundingBox(calculateBoundingBox(points)))
        cameraPosition = CameraPosition(
            cameraPosition.target,
            cameraPosition.zoom - 0.8f,
            cameraPosition.azimuth,
            cameraPosition.tilt
        )
        mapWindow.map.move(cameraPosition, anim, null)
    }

    // PROPS
    fun setUserLocationIcon(iconSource: String) {
        // todo[0]: можно устанавливать разные иконки на покой и движение. Дополнительно можно устанавливать стиль иконки, например scale
        userLocationIcon = iconSource
        ImageCacheManager.getImage(context, iconSource, fun (image: Bitmap?) {
            userLocationBitmap = image
            updateUserLocationIcon()
        })
    }

    fun setUserLocationIconScale(scale: Float) {
        userLocationIconScale = scale
        updateUserLocationIcon()
    }

    fun setUserLocationAccuracyFillColor(color: Int) {
        userLocationAccuracyFillColor = color
        updateUserLocationIcon()
    }

    fun setUserLocationAccuracyStrokeColor(color: Int) {
        userLocationAccuracyStrokeColor = color
        updateUserLocationIcon()
    }

    fun setUserLocationAccuracyStrokeWidth(width: Float) {
        userLocationAccuracyStrokeWidth = width
        updateUserLocationIcon()
    }

    fun setMapStyle(style: String?) {
        if (style != null) {
            mapWindow.map.setMapStyle(style)
        }
    }

    fun setMapType(type: String?) {
        if (type != null) {
            mapWindow.map.mapType = when (type) {
                "none" -> MapType.NONE
                "raster" -> MapType.MAP
                else -> MapType.VECTOR_MAP
            }
        }
    }

    fun setInitialRegion(params: ReadableMap?) {
        if (initializedRegion) return
        if ((!params!!.hasKey("lat") || params.isNull("lat")) || (!params.hasKey("lon") && params.isNull(
                "lon"
            ))
        ) return

        var initialRegionZoom = 10f
        var initialRegionAzimuth = 0f
        var initialRegionTilt = 0f

        if (params.hasKey("zoom") && !params.isNull("zoom")) initialRegionZoom =
            params.getDouble("zoom").toFloat()

        if (params.hasKey("azimuth") && !params.isNull("azimuth")) initialRegionAzimuth =
            params.getDouble("azimuth").toFloat()

        if (params.hasKey("tilt") && !params.isNull("tilt")) initialRegionTilt =
            params.getDouble("tilt").toFloat()

        val initialPosition = PointUtil.readableMapToPoint(params)

        val initialCameraPosition = CameraPosition(
            initialPosition,
            initialRegionZoom,
            initialRegionAzimuth,
            initialRegionTilt
        )
        setCenter(initialCameraPosition, 0f, 0)
        initializedRegion = true
    }

    fun setLogoPosition(params: ReadableMap?) {
        var horizontalAlignment = HorizontalAlignment.RIGHT
        var verticalAlignment = VerticalAlignment.BOTTOM

        if (params!!.hasKey("horizontal") && !params.isNull("horizontal")) {
            when (params.getString("horizontal")) {
                "left" -> horizontalAlignment = HorizontalAlignment.LEFT
                "center" -> horizontalAlignment = HorizontalAlignment.CENTER
                else -> {}
            }
        }

        if (params.hasKey("vertical") && !params.isNull("vertical")) {
            when (params.getString("vertical")) {
                "top" -> verticalAlignment = VerticalAlignment.TOP
                else -> {}
            }
        }

        mapWindow.map.logo.setAlignment(Alignment(horizontalAlignment, verticalAlignment))
    }

    fun setLogoPadding(params: ReadableMap?) {
        val horizontalPadding =
            if ((params!!.hasKey("horizontal") && !params.isNull("horizontal"))) params.getInt("horizontal") else 0
        val verticalPadding =
            if ((params.hasKey("vertical") && !params.isNull("vertical"))) params.getInt("vertical") else 0
        mapWindow.map.logo.setPadding(Padding(horizontalPadding, verticalPadding))
    }

    fun setInteractiveDisabled(value: Boolean) {
        setNoninteractive(value)
    }

    fun setNightMode(nightMode: Boolean?) {
        mapWindow.map.isNightModeEnabled = nightMode!!
    }

    fun setScrollGesturesDisabled(value: Boolean) {
        mapWindow.map.isScrollGesturesEnabled = !value
    }

    fun setZoomGesturesDisabled(value: Boolean) {
        mapWindow.map.isZoomGesturesEnabled = !value
    }

    fun setRotateGesturesDisabled(value: Boolean) {
        mapWindow.map.isRotateGesturesEnabled = !value
    }

    fun setFastTapDisabled(value: Boolean) {
        mapWindow.map.isFastTapEnabled = !value
    }

    fun setTiltGesturesDisabled(value: Boolean) {
        mapWindow.map.isTiltGesturesEnabled = !value
    }

    fun setTrafficVisible(isVisible: Boolean) {
        if (trafficLayer == null) {
            trafficLayer = MapKitFactory.getInstance().createTrafficLayer(mapWindow)
        }

        if (isVisible) {
            trafficLayer!!.addTrafficListener(this)
            trafficLayer!!.isTrafficVisible = true
        } else {
            trafficLayer!!.isTrafficVisible = false
            trafficLayer!!.removeTrafficListener(this)
        }
    }

    fun setShowUserPosition(show: Boolean) {
        if (userLocationLayer == null) {
            userLocationLayer = MapKitFactory.getInstance().createUserLocationLayer(mapWindow)
        }

        if (show) {
            userLocationLayer!!.setObjectListener(this)
            userLocationLayer!!.isVisible = true
            userLocationLayer!!.isHeadingModeActive = true
        } else {
            userLocationLayer!!.isVisible = false
            userLocationLayer!!.isHeadingModeActive = false
            userLocationLayer!!.setObjectListener(null)
        }
    }

    fun setFollowUser(follow: Boolean) {
        if (userLocationLayer == null) {
            setShowUserPosition(true)
        }

        if (follow) {
            userLocationLayer!!.isAutoZoomEnabled = true
            userLocationLayer!!.setAnchor(
                PointF((width * 0.5).toFloat(), (height * 0.5).toFloat()),
                PointF((width * 0.5).toFloat(), (height * 0.83).toFloat())
            )
        } else {
            userLocationLayer!!.isAutoZoomEnabled = false
            userLocationLayer!!.resetAnchor()
        }
    }

    // CHILDREN
    open fun addFeature(child: View?, index: Int) {
        if (child is PolygonView) {
            val obj = mapWindow.map.mapObjects.addPolygon(child.polygon)
            child.setPolygonMapObject(obj)
        } else if (child is PolylineView) {
            val obj = mapWindow.map.mapObjects.addPolyline(child.polyline)
            child.setPolylineMapObject(obj)
        } else if (child is MarkerView) {
            val obj = mapWindow.map.mapObjects.addPlacemark()
            obj.setIcon(ImageProvider.fromBitmap(createBitmap(1, 1)))
            obj.geometry = child.point!!
            child.setMarkerMapObject(obj)
        } else if (child is CircleView) {
            val obj = mapWindow.map.mapObjects.addCircle(child.circle)
            child.setCircleMapObject(obj)
        }
    }

    open fun removeChild(index: Int) {
        val child = getChildAt(index)
        if (child is ReactMapObject) {
            val mapObject = child.rnMapObject
            if (mapObject == null || !mapObject.isValid) return

            mapWindow.map.mapObjects.remove(mapObject)
        }
    }

    // location listener implementation
    override fun onObjectAdded(@Nonnull view: UserLocationView) {
        userLocationView = view
        updateUserLocationIcon()
    }

    override fun onObjectRemoved(@Nonnull userLocationView: UserLocationView) {
    }

    override fun onObjectUpdated(
        @Nonnull view: UserLocationView,
        @Nonnull objectEvent: ObjectEvent
    ) {
        userLocationView = view
        updateUserLocationIcon()
    }

    private fun updateUserLocationIcon() {
        if (userLocationView === null) return

        val userIconStyle = IconStyle()
        userIconStyle.setScale(userLocationIconScale)

        val pin = userLocationView!!.pin
        val arrow = userLocationView!!.arrow

        userLocationBitmap?.let {
            pin.setIcon(ImageProvider.fromBitmap(it), userIconStyle)
            arrow.setIcon(ImageProvider.fromBitmap(it), userIconStyle)
        }

        val circle = userLocationView!!.accuracyCircle
        if (userLocationAccuracyFillColor != 0) {
            circle.fillColor = userLocationAccuracyFillColor
        }
        if (userLocationAccuracyStrokeColor != 0) {
            circle.strokeColor = userLocationAccuracyStrokeColor
        }
        circle.strokeWidth = userLocationAccuracyStrokeWidth
    }

    override fun onCameraPositionChanged(
        map: com.yandex.mapkit.map.Map,
        cameraPosition: CameraPosition,
        reason: CameraUpdateReason,
        finished: Boolean
    ) {
        val positionStart = positionToJSON(cameraPosition, reason, finished)

        val eventDispatcher = UIManagerHelper.getEventDispatcherForReactTag(context as ThemedReactContext, id)
        eventDispatcher?.dispatchEvent(CameraPositionChangeEvent(
            getSurfaceId(context),
            id,
            positionStart
        ))

        if (finished) {
            val positionFinish = positionToJSON(cameraPosition, reason, finished)

            eventDispatcher?.dispatchEvent(CameraPositionChangeEndEvent(
                getSurfaceId(context),
                id,
                positionFinish
            ))
        }
    }

    override fun onMapTap(map: com.yandex.mapkit.map.Map, point: Point) {
        val eventDispatcher = UIManagerHelper.getEventDispatcherForReactTag(context as ThemedReactContext, id)
        eventDispatcher?.dispatchEvent(YamapPressEvent(
            getSurfaceId(context),
            id,
            point
        ))
    }

    override fun onMapLongTap(map: com.yandex.mapkit.map.Map, point: Point) {
        val eventDispatcher = UIManagerHelper.getEventDispatcherForReactTag(context as ThemedReactContext, id)
        eventDispatcher?.dispatchEvent(YamapLongPressEvent(
            getSurfaceId(context),
            id,
            point
        ))
    }

    override fun onMapLoaded(statistics: MapLoadStatistics) {
        val eventDispatcher = UIManagerHelper.getEventDispatcherForReactTag(context as ThemedReactContext, id)
        eventDispatcher?.dispatchEvent(MapLoadedEvent(
            getSurfaceId(context),
            id,
            statistics
        ))
    }

    override fun onTrafficChanged(trafficLevel: TrafficLevel?) {
    }

    override fun onTrafficLoading() {
    }

    override fun onTrafficExpired() {
    }
}
