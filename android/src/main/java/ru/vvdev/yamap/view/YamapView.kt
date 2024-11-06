package ru.vvdev.yamap.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PointF
import android.view.MotionEvent
import android.view.View
import android.view.ViewParent
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.WritableNativeArray
import com.facebook.react.uimanager.events.RCTEventEmitter
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.ScreenPoint
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.DrivingOptions
import com.yandex.mapkit.directions.driving.DrivingRoute
import com.yandex.mapkit.directions.driving.DrivingRouter
import com.yandex.mapkit.directions.driving.DrivingRouterType
import com.yandex.mapkit.directions.driving.DrivingSection
import com.yandex.mapkit.directions.driving.DrivingSession.DrivingRouteListener
import com.yandex.mapkit.directions.driving.VehicleOptions
import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.geometry.SubpolylineHelper
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
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.MapType
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.map.VisibleRegion
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.traffic.TrafficLayer
import com.yandex.mapkit.traffic.TrafficLevel
import com.yandex.mapkit.traffic.TrafficListener
import com.yandex.mapkit.transport.TransportFactory
import com.yandex.mapkit.transport.masstransit.FilterVehicleTypes
import com.yandex.mapkit.transport.masstransit.FitnessOptions
import com.yandex.mapkit.transport.masstransit.Route
import com.yandex.mapkit.transport.masstransit.RouteOptions
import com.yandex.mapkit.transport.masstransit.Section
import com.yandex.mapkit.transport.masstransit.Session
import com.yandex.mapkit.transport.masstransit.TimeOptions
import com.yandex.mapkit.transport.masstransit.TransitOptions
import com.yandex.mapkit.transport.masstransit.Transport
import com.yandex.mapkit.transport.masstransit.Weight
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.Error
import com.yandex.runtime.image.ImageProvider
import ru.vvdev.yamap.models.ReactMapObject
import ru.vvdev.yamap.utils.Callback
import ru.vvdev.yamap.utils.ImageLoader.DownloadImageBitmap
import ru.vvdev.yamap.utils.RouteManager
import java.util.Objects
import javax.annotation.Nonnull


open class YamapView(context: Context?) : MapView(context), UserLocationObjectListener,
    CameraListener, InputListener, TrafficListener, MapLoadedListener {
    private var mViewParent: ViewParent? = null
    private var userLocationIcon = ""
    private var userLocationIconScale = 1f
    private var userLocationBitmap: Bitmap? = null
    private val routeMng = RouteManager()
    private var routeOptions: RouteOptions = RouteOptions(FitnessOptions(false))
    private val masstransitRouter = TransportFactory.getInstance().createMasstransitRouter()
    private val drivingRouter: DrivingRouter
    private val pedestrianRouter = TransportFactory.getInstance().createPedestrianRouter()
    private var userLocationLayer: UserLocationLayer? = null
    private var userLocationAccuracyFillColor = 0
    private var userLocationAccuracyStrokeColor = 0
    private var userLocationAccuracyStrokeWidth = 0f
    private var trafficLayer: TrafficLayer? = null
    private var maxFps = 60f
    private var initializedRegion = false;

    private var userLocationView: UserLocationView? = null

    init {
        drivingRouter = DirectionsFactory.getInstance().createDrivingRouter(DrivingRouterType.ONLINE)
        mapWindow.map.addCameraListener(this)
        mapWindow.map.addInputListener(this)
        mapWindow.map.setMapLoadedListener(this)
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

    fun setViewParent(viewParent: ViewParent?) { //any ViewGroup
        mViewParent = viewParent
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        when (event.getAction()) {
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
        val result = Arguments.createMap()

        result.putDouble("lat", worldPoint!!.latitude)
        result.putDouble("lon", worldPoint.longitude)

        return result
    }

    private fun visibleRegionToJSON(region: VisibleRegion): WritableMap {
        val result = Arguments.createMap()

        val bl = Arguments.createMap()
        bl.putDouble("lat", region.bottomLeft.latitude)
        bl.putDouble("lon", region.bottomLeft.longitude)
        result.putMap("bottomLeft", bl)

        val br = Arguments.createMap()
        br.putDouble("lat", region.bottomRight.latitude)
        br.putDouble("lon", region.bottomRight.longitude)
        result.putMap("bottomRight", br)

        val tl = Arguments.createMap()
        tl.putDouble("lat", region.topLeft.latitude)
        tl.putDouble("lon", region.topLeft.longitude)
        result.putMap("topLeft", tl)

        val tr = Arguments.createMap()
        tr.putDouble("lat", region.topRight.latitude)
        tr.putDouble("lon", region.topRight.longitude)
        result.putMap("topRight", tr)

        return result
    }

    fun emitCameraPositionToJS(id: String?) {
        val position = mapWindow.map.cameraPosition
        val cameraPosition =
            positionToJSON(position, CameraUpdateReason.valueOf("APPLICATION"), true)
        cameraPosition.putString("id", id)
        val reactContext = context as ReactContext
        reactContext.getJSModule(RCTEventEmitter::class.java)
            .receiveEvent(getId(), "cameraPosition", cameraPosition)
    }

    fun emitVisibleRegionToJS(id: String?) {
        val visibleRegion = mapWindow.map.visibleRegion
        val result = visibleRegionToJSON(visibleRegion)
        result.putString("id", id)
        val reactContext = context as ReactContext
        reactContext.getJSModule(RCTEventEmitter::class.java)
            .receiveEvent(getId(), "visibleRegion", result)
    }

    fun emitWorldToScreenPoints(worldPoints: ReadableArray, id: String?) {
        val screenPoints = Arguments.createArray()

        for (i in 0 until worldPoints.size()) {
            val p = worldPoints.getMap(i)
            val worldPoint = Point(p.getDouble("lat"), p.getDouble("lon"))
            val screenPoint = mapWindow.worldToScreen(worldPoint)
            screenPoints.pushMap(screenPointToJSON(screenPoint))
        }

        val result = Arguments.createMap()
        result.putString("id", id)
        result.putArray("screenPoints", screenPoints)

        val reactContext = context as ReactContext
        reactContext.getJSModule(RCTEventEmitter::class.java)
            .receiveEvent(getId(), "worldToScreenPoints", result)
    }

    fun emitScreenToWorldPoints(screenPoints: ReadableArray, id: String?) {
        val worldPoints = Arguments.createArray()

        for (i in 0 until screenPoints.size()) {
            val p = screenPoints.getMap(i)
            val screenPoint = ScreenPoint(p.getDouble("x").toFloat(), p.getDouble("y").toFloat())
            val worldPoint = mapWindow.screenToWorld(screenPoint)
            worldPoints.pushMap(worldPointToJSON(worldPoint))
        }

        val result = Arguments.createMap()
        result.putString("id", id)
        result.putArray("worldPoints", worldPoints)

        val reactContext = context as ReactContext
        reactContext.getJSModule(RCTEventEmitter::class.java)
            .receiveEvent(getId(), "screenToWorldPoints", result)
    }

    fun setZoom(zoom: Float?, duration: Float, animation: Int) {
        val prevPosition = mapWindow.map.cameraPosition
        val position =
            CameraPosition(prevPosition.target, zoom!!, prevPosition.azimuth, prevPosition.tilt)
        setCenter(position, duration, animation)
    }

    fun findRoutes(points: ArrayList<Point?>, vehicles: ArrayList<String>, id: String?) {
        val self = this
        if (vehicles.size == 1 && vehicles[0] == "car") {
            val listener: DrivingRouteListener = object : DrivingRouteListener {
                override fun onDrivingRoutes(routes: List<DrivingRoute>) {
                    val jsonRoutes = Arguments.createArray()
                    for (i in routes.indices) {
                        val _route = routes[i]
                        val jsonRoute = Arguments.createMap()
                        val id = RouteManager.generateId()
                        jsonRoute.putString("id", id)
                        val sections = Arguments.createArray()
                        for (section in _route.sections) {
                            val jsonSection = convertDrivingRouteSection(_route, section, i)
                            sections.pushMap(jsonSection)
                        }
                        jsonRoute.putArray("sections", sections)
                        jsonRoutes.pushMap(jsonRoute)
                    }
                    self.onRoutesFound(id, jsonRoutes, "success")
                }

                override fun onDrivingRoutesError(error: Error) {
                    self.onRoutesFound(id, Arguments.createArray(), "error")
                }
            }
            val _points = ArrayList<RequestPoint>()
            for (i in points.indices) {
                val point = points[i]
                val _p = RequestPoint(point!!, RequestPointType.WAYPOINT, null, null)
                _points.add(_p)
            }

            drivingRouter.requestRoutes(
                _points,
                DrivingOptions().setRoutesCount(1),
                VehicleOptions(),
                listener
            )
            return
        }
        val _points = ArrayList<RequestPoint>()
        for (i in points.indices) {
            val point = points[i]
            _points.add(RequestPoint(point!!, RequestPointType.WAYPOINT, null, null))
        }
        val listener: Session.RouteListener = object : Session.RouteListener {
            override fun onMasstransitRoutes(routes: List<Route>) {
                val jsonRoutes = Arguments.createArray()
                for (i in routes.indices) {
                    val _route = routes[i]
                    val jsonRoute = Arguments.createMap()
                    val id = RouteManager.generateId()
                    self.routeMng.saveRoute(_route, id)
                    jsonRoute.putString("id", id)
                    val sections = Arguments.createArray()
                    for (section in _route.sections) {
                        val jsonSection = convertRouteSection(
                            _route, section, SubpolylineHelper.subpolyline(
                                _route.geometry,
                                section.geometry
                            ), _route.metadata.weight, i
                        )
                        sections.pushMap(jsonSection)
                    }
                    jsonRoute.putArray("sections", sections)
                    jsonRoutes.pushMap(jsonRoute)
                }
                self.onRoutesFound(id, jsonRoutes, "success")
            }

            override fun onMasstransitRoutesError(error: Error) {
                self.onRoutesFound(id, Arguments.createArray(), "error")
            }
        }
        if (vehicles.size == 0) {
            pedestrianRouter.requestRoutes(_points, TimeOptions(), routeOptions, listener)
            return
        }
        val transitOptions = TransitOptions(FilterVehicleTypes.NONE.value, TimeOptions())
        masstransitRouter.requestRoutes(_points, transitOptions, routeOptions, listener)
    }

    fun fitAllMarkers() {
        val points = ArrayList<Point?>()
        for (i in 0 until childCount) {
            val obj: Any = getChildAt(i)
            if (obj is YamapMarker) {
                points.add(obj.point)
            }
        }
        fitMarkers(points)
    }

    private fun mapPlacemarksToPoints(placemarks: List<PlacemarkMapObject>): ArrayList<Point> {
        val points = ArrayList<Point>()

        for (i in placemarks.indices) {
            points.add(placemarks[i].geometry)
        }

        return points
    }

    fun calculateBoundingBox(points: ArrayList<Point?>): BoundingBox {
        var minLon = points[0]!!.longitude
        var maxLon = points[0]!!.longitude
        var minLat = points[0]!!.latitude
        var maxLat = points[0]!!.latitude

        for (i in points.indices) {
            if (points[i]!!.longitude > maxLon) {
                maxLon = points[i]!!.longitude
            }

            if (points[i]!!.longitude < minLon) {
                minLon = points[i]!!.longitude
            }

            if (points[i]!!.latitude > maxLat) {
                maxLat = points[i]!!.latitude
            }

            if (points[i]!!.latitude < minLat) {
                minLat = points[i]!!.latitude
            }
        }

        val southWest = Point(minLat, minLon)
        val northEast = Point(maxLat, maxLon)

        val boundingBox = BoundingBox(southWest, northEast)
        return boundingBox
    }

    fun fitMarkers(points: ArrayList<Point?>) {
        if (points.size == 0) {
            return
        }
        if (points.size == 1) {
            val center = Point(
                points[0]!!.latitude, points[0]!!.longitude
            )
            mapWindow.map.move(CameraPosition(center, 15f, 0f, 0f))
            return
        }
        var cameraPosition = mapWindow.map.cameraPosition(Geometry.fromBoundingBox(calculateBoundingBox(points)))
        cameraPosition = CameraPosition(
            cameraPosition.target,
            cameraPosition.zoom - 0.8f,
            cameraPosition.azimuth,
            cameraPosition.tilt
        )
        mapWindow.map.move(cameraPosition, Animation(Animation.Type.SMOOTH, 0.7f), null)
    }

    // PROPS
    fun setUserLocationIcon(iconSource: String) {
        // todo[0]: можно устанавливать разные иконки на покой и движение. Дополнительно можно устанавливать стиль иконки, например scale
        userLocationIcon = iconSource
        DownloadImageBitmap(context, iconSource, object : Callback<Bitmap?> {
            override fun invoke(arg: Bitmap?) {
                if (iconSource == userLocationIcon) {
                    userLocationBitmap = arg
                    updateUserLocationIcon()
                }
            }
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
            when (type) {
                "none" -> mapWindow.map.mapType = MapType.NONE
                "raster" -> mapWindow.map.mapType = MapType.MAP
                else -> mapWindow.map.mapType = MapType.VECTOR_MAP
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

        val initialPosition = Point(
            params.getDouble("lat"), params.getDouble("lon")
        )
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

    fun setMaxFps(fps: Float) {
        maxFps = fps
//        mapWindow.setMaxFps(maxFps)
    }

    fun setInteractive(interactive: Boolean) {
        setNoninteractive(!interactive)
    }

    fun setNightMode(nightMode: Boolean?) {
        mapWindow.map.isNightModeEnabled = nightMode!!
    }

    fun setScrollGesturesEnabled(scrollGesturesEnabled: Boolean?) {
        mapWindow.map.isScrollGesturesEnabled = scrollGesturesEnabled!!
    }

    fun setZoomGesturesEnabled(zoomGesturesEnabled: Boolean?) {
        mapWindow.map.isZoomGesturesEnabled = zoomGesturesEnabled!!
    }

    fun setRotateGesturesEnabled(rotateGesturesEnabled: Boolean?) {
        mapWindow.map.isRotateGesturesEnabled = rotateGesturesEnabled!!
    }

    fun setFastTapEnabled(fastTapEnabled: Boolean?) {
        mapWindow.map.isFastTapEnabled = fastTapEnabled!!
    }

    fun setTiltGesturesEnabled(tiltGesturesEnabled: Boolean?) {
        mapWindow.map.isTiltGesturesEnabled = tiltGesturesEnabled!!
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
            userLocationLayer!!.isHeadingEnabled = true
        } else {
            userLocationLayer!!.isVisible = false
            userLocationLayer!!.isHeadingEnabled = false
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

    private fun convertRouteSection(
        route: Route,
        section: Section,
        geometry: Polyline,
        routeWeight: Weight,
        routeIndex: Int
    ): WritableMap {
        val data = section.metadata.data
        val routeMetadata = Arguments.createMap()
        val routeWeightData = Arguments.createMap()
        val sectionWeightData = Arguments.createMap()
        val transports: MutableMap<String, ArrayList<String?>?> = HashMap()
        routeWeightData.putString("time", routeWeight.time.text)
        routeWeightData.putInt("transferCount", routeWeight.transfersCount)
        routeWeightData.putDouble("walkingDistance", routeWeight.walkingDistance.value)
        sectionWeightData.putString("time", section.metadata.weight.time.text)
        sectionWeightData.putInt("transferCount", section.metadata.weight.transfersCount)
        sectionWeightData.putDouble(
            "walkingDistance",
            section.metadata.weight.walkingDistance.value
        )
        routeMetadata.putMap("sectionInfo", sectionWeightData)
        routeMetadata.putMap("routeInfo", routeWeightData)
        routeMetadata.putInt("routeIndex", routeIndex)
        val stops: WritableArray = WritableNativeArray()

        for (stop in section.stops) {
            stops.pushString(stop.metadata.stop.name)
        }

        routeMetadata.putArray("stops", stops)

        if (data.transports != null) {
            for (transport in data.transports!!) {
                for (type in transport.line.vehicleTypes) {
                    if (type == "suburban") continue
                    if (transports[type] != null) {
                        val list = transports[type]
                        if (list != null) {
                            list.add(transport.line.name)
                            transports[type] = list
                        }
                    } else {
                        val list = ArrayList<String?>()
                        list.add(transport.line.name)
                        transports[type] = list
                    }
                    routeMetadata.putString("type", type)
                    var color = Color.BLACK
                    if (transportHasStyle(transport)) {
                        try {
                            color = transport.line.style!!.color!!
                        } catch (ignored: Exception) {
                        }
                    }
                    routeMetadata.putString("sectionColor", formatColor(color))
                }
            }
        } else {
            routeMetadata.putString("sectionColor", formatColor(Color.DKGRAY))
            if (section.metadata.weight.walkingDistance.value == 0.0) {
                routeMetadata.putString("type", "waiting")
            } else {
                routeMetadata.putString("type", "walk")
            }
        }

        val wTransports = Arguments.createMap()

        for ((key, value) in transports) {
            wTransports.putArray(key, Arguments.fromList(value))
        }

        routeMetadata.putMap("transports", wTransports)
        val subpolyline = SubpolylineHelper.subpolyline(route.geometry, section.geometry)
        val linePoints = subpolyline.points
        val jsonPoints = Arguments.createArray()

        for (point in linePoints) {
            val jsonPoint = Arguments.createMap()
            jsonPoint.putDouble("lat", point.latitude)
            jsonPoint.putDouble("lon", point.longitude)
            jsonPoints.pushMap(jsonPoint)
        }

        routeMetadata.putArray("points", jsonPoints)

        return routeMetadata
    }

    private fun convertDrivingRouteSection(
        route: DrivingRoute,
        section: DrivingSection,
        routeIndex: Int
    ): WritableMap {
        val routeWeight = route.metadata.weight
        val routeMetadata = Arguments.createMap()
        val routeWeightData = Arguments.createMap()
        val sectionWeightData = Arguments.createMap()
        routeWeightData.putString("time", routeWeight.time.text)
        routeWeightData.putString("timeWithTraffic", routeWeight.timeWithTraffic.text)
        routeWeightData.putDouble("distance", routeWeight.distance.value)
        sectionWeightData.putString("time", section.metadata.weight.time.text)
        sectionWeightData.putString("timeWithTraffic", section.metadata.weight.timeWithTraffic.text)
        sectionWeightData.putDouble("distance", section.metadata.weight.distance.value)
        routeMetadata.putMap("sectionInfo", sectionWeightData)
        routeMetadata.putMap("routeInfo", routeWeightData)
        routeMetadata.putInt("routeIndex", routeIndex)
        val stops: WritableArray = WritableNativeArray()
        routeMetadata.putArray("stops", stops)
        routeMetadata.putString("sectionColor", formatColor(Color.DKGRAY))

        if (section.metadata.weight.distance.value == 0.0) {
            routeMetadata.putString("type", "waiting")
        } else {
            routeMetadata.putString("type", "car")
        }

        val wTransports = Arguments.createMap()
        routeMetadata.putMap("transports", wTransports)
        val subpolyline = SubpolylineHelper.subpolyline(route.geometry, section.geometry)
        val linePoints = subpolyline.points
        val jsonPoints = Arguments.createArray()

        for (point in linePoints) {
            val jsonPoint = Arguments.createMap()
            jsonPoint.putDouble("lat", point.latitude)
            jsonPoint.putDouble("lon", point.longitude)
            jsonPoints.pushMap(jsonPoint)
        }

        routeMetadata.putArray("points", jsonPoints)

        return routeMetadata
    }

    fun onRoutesFound(id: String?, routes: WritableArray?, status: String?) {
        val event = Arguments.createMap()
        event.putArray("routes", routes)
        event.putString("id", id)
        event.putString("status", status)
        val reactContext = context as ReactContext
        reactContext.getJSModule(RCTEventEmitter::class.java).receiveEvent(getId(), "routes", event)
    }

    private fun transportHasStyle(transport: Transport): Boolean {
        return transport.line.style != null
    }

    private fun formatColor(color: Int): String {
        return String.format("#%06X", (0xFFFFFF and color))
    }

    // CHILDREN
    open fun addFeature(child: View?, index: Int) {
        if (child is YamapPolygon) {
            val _child = child
            val obj = mapWindow.map.mapObjects.addPolygon(_child.polygon)
            _child.setPolygonMapObject(obj)
        } else if (child is YamapPolyline) {
            val _child = child
            val obj = mapWindow.map.mapObjects.addPolyline(_child.polyline)
            _child.setPolylineMapObject(obj)
        } else if (child is YamapMarker) {
            val _child = child
            val obj = mapWindow.map.mapObjects.addPlacemark(_child.point!!)
            _child.setMarkerMapObject(obj)
        } else if (child is YamapCircle) {
            val _child = child
            val obj = mapWindow.map.mapObjects.addCircle(_child.circle)
            _child.setCircleMapObject(obj)
        }
    }

    open fun removeChild(index: Int) {
        if (getChildAt(index) is ReactMapObject) {
            val child = getChildAt(index) as ReactMapObject ?: return
            val mapObject = child.rnMapObject
            if (mapObject == null || !mapObject.isValid) return

            mapWindow.map.mapObjects.remove(mapObject)
        }
    }

    // location listener implementation
    override fun onObjectAdded(@Nonnull _userLocationView: UserLocationView) {
        userLocationView = _userLocationView
        updateUserLocationIcon()
    }

    override fun onObjectRemoved(@Nonnull userLocationView: UserLocationView) {
    }

    override fun onObjectUpdated(
        @Nonnull _userLocationView: UserLocationView,
        @Nonnull objectEvent: ObjectEvent
    ) {
        userLocationView = _userLocationView
        updateUserLocationIcon()
    }

    private fun updateUserLocationIcon() {
        if (userLocationView != null) {
            val userIconStyle = IconStyle()
            userIconStyle.setScale(userLocationIconScale)

            val pin = userLocationView!!.pin
            val arrow = userLocationView!!.arrow
            if (userLocationBitmap != null) {
                pin.setIcon(ImageProvider.fromBitmap(userLocationBitmap), userIconStyle)
                arrow.setIcon(ImageProvider.fromBitmap(userLocationBitmap), userIconStyle)
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
    }

    override fun onCameraPositionChanged(
        map: com.yandex.mapkit.map.Map,
        cameraPosition: CameraPosition,
        reason: CameraUpdateReason,
        finished: Boolean
    ) {
        val positionStart = positionToJSON(cameraPosition, reason, finished)
        val positionFinish = positionToJSON(cameraPosition, reason, finished)
        val reactContext = context as ReactContext
        reactContext.getJSModule(RCTEventEmitter::class.java)
            .receiveEvent(id, "cameraPositionChange", positionStart)

        if (finished) {
            reactContext.getJSModule(RCTEventEmitter::class.java)
                .receiveEvent(id, "cameraPositionChangeEnd", positionFinish)
        }
    }

    override fun onMapTap(map: com.yandex.mapkit.map.Map, point: Point) {
        val data = Arguments.createMap()
        data.putDouble("lat", point.latitude)
        data.putDouble("lon", point.longitude)
        val reactContext = context as ReactContext
        reactContext.getJSModule(RCTEventEmitter::class.java).receiveEvent(id, "onMapPress", data)
    }

    override fun onMapLongTap(map: com.yandex.mapkit.map.Map, point: Point) {
        val data = Arguments.createMap()
        data.putDouble("lat", point.latitude)
        data.putDouble("lon", point.longitude)
        val reactContext = context as ReactContext
        reactContext.getJSModule(RCTEventEmitter::class.java)
            .receiveEvent(id, "onMapLongPress", data)
    }

    override fun onMapLoaded(statistics: MapLoadStatistics) {
        val data = Arguments.createMap()
        data.putInt("renderObjectCount", statistics.renderObjectCount)
        data.putDouble("curZoomModelsLoaded", statistics.curZoomModelsLoaded.toDouble())
        data.putDouble("curZoomPlacemarksLoaded", statistics.curZoomPlacemarksLoaded.toDouble())
        data.putDouble("curZoomLabelsLoaded", statistics.curZoomLabelsLoaded.toDouble())
        data.putDouble("curZoomGeometryLoaded", statistics.curZoomGeometryLoaded.toDouble())
        data.putDouble("tileMemoryUsage", statistics.tileMemoryUsage.toDouble())
        data.putDouble("delayedGeometryLoaded", statistics.delayedGeometryLoaded.toDouble())
        data.putDouble("fullyAppeared", statistics.fullyAppeared.toDouble())
        data.putDouble("fullyLoaded", statistics.fullyLoaded.toDouble())
        val reactContext = context as ReactContext
        reactContext.getJSModule(RCTEventEmitter::class.java).receiveEvent(id, "onMapLoaded", data)
    }

    //trafficListener implementation
    override fun onTrafficChanged(trafficLevel: TrafficLevel?) {
    }

    override fun onTrafficLoading() {
    }

    override fun onTrafficExpired() {
    }

    companion object {
        private val DEFAULT_VEHICLE_COLORS: HashMap<String?, String?> =
            object : HashMap<String?, String?>() {
                init {
                    put("bus", "#59ACFF")
                    put("railway", "#F8634F")
                    put("tramway", "#C86DD7")
                    put("suburban", "#3023AE")
                    put("underground", "#BDCCDC")
                    put("trolleybus", "#55CfDC")
                    put("walk", "#333333")
                }
            }
    }
}
