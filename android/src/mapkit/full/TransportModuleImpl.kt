package ru.yamap.module

import android.graphics.Color
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.UiThreadUtil.runOnUiThread
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.WritableNativeArray
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.DrivingOptions
import com.yandex.mapkit.directions.driving.DrivingRoute
import com.yandex.mapkit.directions.driving.DrivingRouterType
import com.yandex.mapkit.directions.driving.DrivingSection
import com.yandex.mapkit.directions.driving.DrivingSession.DrivingRouteListener
import com.yandex.mapkit.directions.driving.VehicleOptions
import com.yandex.mapkit.geometry.SubpolylineHelper
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
import com.yandex.runtime.Error
import ru.yamap.utils.PointUtil
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot

class TransportModuleImpl {
    fun findRoutes(jsPoints: ReadableArray, jsVehicles: ReadableArray?, promise: Promise?) {
        val points = PointUtil.jsPointsToPoints(jsPoints)

        val vehicles = ArrayList<String>()

        if (jsVehicles != null) {
            for (i in 0 until jsVehicles.size()) {
                jsVehicles.getString(i)?.let { vehicles.add(it) }
            }
        }

        if (vehicles.size == 1 && vehicles[0] == "car") {
            val listener: DrivingRouteListener = object : DrivingRouteListener {
                override fun onDrivingRoutes(routes: List<DrivingRoute>) {
                    val jsonRoutes = Arguments.createArray()
                    for (i in routes.indices) {
                        val _route = routes[i]
                        val jsonRoute = Arguments.createMap()
                        val sections = Arguments.createArray()
                        for (section in _route.sections) {
                            val jsonSection = convertDrivingRouteSection(_route, section, i)
                            sections.pushMap(jsonSection)
                        }
                        jsonRoute.putArray("sections", sections)
                        jsonRoutes.pushMap(jsonRoute)
                    }

                    val eventData = Arguments.createMap()
                    eventData.putArray("routes", jsonRoutes)
                    eventData.putString("status", "success")
                    promise?.resolve(eventData)
                }

                override fun onDrivingRoutesError(error: Error) {
                    promise?.reject("onDrivingRoutesError", error.toString())
                }
            }
            val _points = ArrayList<RequestPoint>()
            for (i in points.indices) {
                val point = points[i]
                val _p = RequestPoint(point, RequestPointType.WAYPOINT, null, null, null)
                _points.add(_p)
            }

            runOnUiThread {
                val drivingRouter = DirectionsFactory.getInstance().createDrivingRouter(DrivingRouterType.ONLINE)
                drivingRouter.requestRoutes(
                    _points,
                    DrivingOptions().setRoutesCount(1),
                    VehicleOptions(),
                    listener
                )
            }
            return
        }
        val _points = buildList {
            for (i in points.indices) {
                val point = points[i]
                add(RequestPoint(point, RequestPointType.WAYPOINT, null, null, null))
            }
        }
        val listener = object : Session.RouteListener {
            override fun onMasstransitRoutes(routes: List<Route>) {
                val jsonRoutes = Arguments.createArray()
                for (i in routes.indices) {
                    val _route = routes[i]
                    val jsonRoute = Arguments.createMap()
                    val sections = Arguments.createArray()
                    for (section in _route.sections) {
                        val jsonSection = convertRouteSection(
                            _route,
                            section,
                            _route.metadata.weight,
                            i
                        )
                        sections.pushMap(jsonSection)
                    }
                    jsonRoute.putArray("sections", sections)
                    jsonRoutes.pushMap(jsonRoute)
                }

                val eventData = Arguments.createMap()
                eventData.putArray("routes", jsonRoutes)
                eventData.putString("status", "success")
                promise?.resolve(eventData)
            }

            override fun onMasstransitRoutesError(error: Error) {
                promise?.reject("onMasstransitRoutesError", error.toString())
            }
        }
        if (vehicles.isEmpty()) {
            runOnUiThread {
                val pedestrianRouter = TransportFactory.getInstance().createPedestrianRouter()
                pedestrianRouter.requestRoutes(
                    _points,
                    TimeOptions(null, null),
                    RouteOptions(FitnessOptions(false, false)),
                    listener
                )
            }
            return
        }
        val transitOptions = TransitOptions(FilterVehicleTypes.NONE.value, TimeOptions())
        runOnUiThread {
            val masstransitRouter = TransportFactory.getInstance().createMasstransitRouter()
            masstransitRouter.requestRoutes(
                _points,
                transitOptions,
                RouteOptions(),
                listener
            )
        }
    }

    fun findParkRoutes(start: ReadableMap, end: ReadableMap, jsZones: ReadableArray, promise: Promise?) {
        val startPoint = PointUtil.readableMapToPoint(start)
        val endPoint = PointUtil.readableMapToPoint(end)
        val zones = parseZones(jsZones)

        if (zones.isEmpty()) {
            promise?.reject("findParkRoutesError", "zones should contain at least one polygon")
            return
        }

        requestParkRoute(
            startPoint = startPoint,
            endPoint = endPoint,
            zones = zones,
            detours = emptyList(),
            iteration = 0,
            promise = promise
        )
    }

    private fun requestParkRoute(
        startPoint: com.yandex.mapkit.geometry.Point,
        endPoint: com.yandex.mapkit.geometry.Point,
        zones: List<List<com.yandex.mapkit.geometry.Point>>,
        detours: List<com.yandex.mapkit.geometry.Point>,
        iteration: Int,
        promise: Promise?
    ) {
        if (iteration >= MAX_PARK_ROUTE_ITERATIONS) {
            promise?.reject("findParkRoutesError", "failed to build route around restricted zones")
            return
        }

        val requestPoints = ArrayList<RequestPoint>()
        requestPoints.add(RequestPoint(startPoint, RequestPointType.WAYPOINT, null, null, null))
        detours.forEach {
            requestPoints.add(RequestPoint(it, RequestPointType.WAYPOINT, null, null, null))
        }
        requestPoints.add(RequestPoint(endPoint, RequestPointType.WAYPOINT, null, null, null))

        val listener = object : Session.RouteListener {
            override fun onMasstransitRoutes(routes: List<Route>) {
                val route = routes.firstOrNull()
                if (route == null) {
                    promise?.reject("findParkRoutesError", "empty route response")
                    return
                }

                val intersectedZone = firstIntersectedZone(route, zones)
                if (intersectedZone == null) {
                    val sections = Arguments.createArray()
                    route.sections.forEach { section ->
                        sections.pushMap(convertRouteSection(route, section, route.metadata.weight, 0))
                    }
                    val routePayload = Arguments.createMap()
                    routePayload.putArray("sections", sections)
                    val routesPayload = Arguments.createArray()
                    routesPayload.pushMap(routePayload)
                    val eventData = Arguments.createMap()
                    eventData.putArray("routes", routesPayload)
                    eventData.putString("status", "success")
                    promise?.resolve(eventData)
                    return
                }

                val detour = chooseDetourPoint(intersectedZone, endPoint, detours)
                if (detour == null) {
                    promise?.reject("findParkRoutesError", "failed to build detour outside restricted zone")
                    return
                }

                requestParkRoute(
                    startPoint = startPoint,
                    endPoint = endPoint,
                    zones = zones,
                    detours = detours + detour,
                    iteration = iteration + 1,
                    promise = promise
                )
            }

            override fun onMasstransitRoutesError(error: Error) {
                promise?.reject("findParkRoutesError", error.toString())
            }
        }

        runOnUiThread {
            val pedestrianRouter = TransportFactory.getInstance().createPedestrianRouter()
            pedestrianRouter.requestRoutes(
                requestPoints,
                TimeOptions(null, null),
                RouteOptions(FitnessOptions(false, false)),
                listener
            )
        }
    }

    private fun parseZones(jsZones: ReadableArray): List<List<com.yandex.mapkit.geometry.Point>> {
        val zones = ArrayList<List<com.yandex.mapkit.geometry.Point>>()
        for (i in 0 until jsZones.size()) {
            val zoneArray = jsZones.getArray(i) ?: continue
            val zone = PointUtil.jsPointsToPoints(zoneArray)
            if (zone.size >= 3) {
                zones.add(zone)
            }
        }
        return zones
    }

    private fun firstIntersectedZone(
        route: Route,
        zones: List<List<com.yandex.mapkit.geometry.Point>>
    ): List<com.yandex.mapkit.geometry.Point>? {
        val routePoints = route.geometry.points
        if (routePoints.size < 2) {
            return null
        }
        return zones.firstOrNull { zone -> polylineIntersectsPolygon(routePoints, zone) }
    }

    private fun polylineIntersectsPolygon(
        polyline: List<com.yandex.mapkit.geometry.Point>,
        polygon: List<com.yandex.mapkit.geometry.Point>
    ): Boolean {
        for (i in 0 until polyline.size - 1) {
            if (segmentIntersectsPolygon(polyline[i], polyline[i + 1], polygon)) {
                return true
            }
        }
        return false
    }

    private fun segmentIntersectsPolygon(
        a: com.yandex.mapkit.geometry.Point,
        b: com.yandex.mapkit.geometry.Point,
        polygon: List<com.yandex.mapkit.geometry.Point>
    ): Boolean {
        if (isPointInsidePolygon(a, polygon) || isPointInsidePolygon(b, polygon)) {
            return true
        }
        for (i in polygon.indices) {
            val c = polygon[i]
            val d = polygon[(i + 1) % polygon.size]
            if (segmentsIntersect(a, b, c, d)) {
                return true
            }
        }
        return false
    }

    private fun segmentsIntersect(
        a: com.yandex.mapkit.geometry.Point,
        b: com.yandex.mapkit.geometry.Point,
        c: com.yandex.mapkit.geometry.Point,
        d: com.yandex.mapkit.geometry.Point
    ): Boolean {
        val o1 = orientation(a, b, c)
        val o2 = orientation(a, b, d)
        val o3 = orientation(c, d, a)
        val o4 = orientation(c, d, b)

        if (o1 * o2 < 0 && o3 * o4 < 0) {
            return true
        }

        val eps = 1e-9
        if (abs(o1) < eps && onSegment(a, c, b)) return true
        if (abs(o2) < eps && onSegment(a, d, b)) return true
        if (abs(o3) < eps && onSegment(c, a, d)) return true
        if (abs(o4) < eps && onSegment(c, b, d)) return true
        return false
    }

    private fun orientation(
        a: com.yandex.mapkit.geometry.Point,
        b: com.yandex.mapkit.geometry.Point,
        c: com.yandex.mapkit.geometry.Point
    ): Double {
        return (b.longitude - a.longitude) * (c.latitude - a.latitude) -
                (b.latitude - a.latitude) * (c.longitude - a.longitude)
    }

    private fun onSegment(
        a: com.yandex.mapkit.geometry.Point,
        b: com.yandex.mapkit.geometry.Point,
        c: com.yandex.mapkit.geometry.Point
    ): Boolean {
        return b.longitude <= maxOf(a.longitude, c.longitude) &&
                b.longitude >= minOf(a.longitude, c.longitude) &&
                b.latitude <= maxOf(a.latitude, c.latitude) &&
                b.latitude >= minOf(a.latitude, c.latitude)
    }

    private fun isPointInsidePolygon(
        point: com.yandex.mapkit.geometry.Point,
        polygon: List<com.yandex.mapkit.geometry.Point>
    ): Boolean {
        var intersects = false
        var j = polygon.size - 1
        for (i in polygon.indices) {
            val pi = polygon[i]
            val pj = polygon[j]
            val condition = (pi.latitude > point.latitude) != (pj.latitude > point.latitude)
            if (condition) {
                val xIntersection =
                    (pj.longitude - pi.longitude) * (point.latitude - pi.latitude) /
                            (pj.latitude - pi.latitude + 1e-12) + pi.longitude
                if (point.longitude < xIntersection) {
                    intersects = !intersects
                }
            }
            j = i
        }
        return intersects
    }

    private fun chooseDetourPoint(
        zone: List<com.yandex.mapkit.geometry.Point>,
        endPoint: com.yandex.mapkit.geometry.Point,
        existingDetours: List<com.yandex.mapkit.geometry.Point>
    ): com.yandex.mapkit.geometry.Point? {
        val centroid = polygonCentroid(zone)
        val sortedCandidates = zone
            .map { projectOutsideZone(it, centroid, PARK_ROUTE_BUFFER_METERS) }
            .sortedBy { distance(it, endPoint) }

        return sortedCandidates.firstOrNull { candidate ->
            existingDetours.none { distance(it, candidate) < DETOUR_POINT_EPSILON_DEGREES }
        }
    }

    private fun polygonCentroid(points: List<com.yandex.mapkit.geometry.Point>): com.yandex.mapkit.geometry.Point {
        val avgLat = points.sumOf { it.latitude } / points.size
        val avgLon = points.sumOf { it.longitude } / points.size
        return com.yandex.mapkit.geometry.Point(avgLat, avgLon)
    }

    private fun projectOutsideZone(
        point: com.yandex.mapkit.geometry.Point,
        centroid: com.yandex.mapkit.geometry.Point,
        meters: Double
    ): com.yandex.mapkit.geometry.Point {
        val dLat = point.latitude - centroid.latitude
        val dLon = point.longitude - centroid.longitude
        val norm = hypot(dLat, dLon)
        if (norm < 1e-12) {
            return point
        }

        val latMeters = metersToLatitudeDegrees(meters)
        val lonMeters = metersToLongitudeDegrees(meters, point.latitude)
        val scaleLat = (dLat / norm) * latMeters
        val scaleLon = (dLon / norm) * lonMeters
        return com.yandex.mapkit.geometry.Point(point.latitude + scaleLat, point.longitude + scaleLon)
    }

    private fun metersToLatitudeDegrees(meters: Double): Double {
        return meters / 111_320.0
    }

    private fun metersToLongitudeDegrees(meters: Double, atLatitude: Double): Double {
        val latRad = atLatitude * PI / 180.0
        val scale = cos(latRad).coerceAtLeast(1e-6)
        return meters / (111_320.0 * scale)
    }

    private fun distance(
        a: com.yandex.mapkit.geometry.Point,
        b: com.yandex.mapkit.geometry.Point
    ): Double {
        return hypot(a.latitude - b.latitude, a.longitude - b.longitude)
    }

    private fun convertRouteSection(
        route: Route,
        section: Section,
        routeWeight: Weight,
        routeIndex: Int
    ): WritableMap {
        val data = section.metadata.data
        val routeMetadata = Arguments.createMap()
        val routeWeightData = Arguments.createMap()
        val sectionWeightData = Arguments.createMap()
        val transports = HashMap<String, MutableList<String?>>()
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
        val jsPoints = Arguments.createArray()

        for (point in linePoints) {
            val jsPoint = PointUtil.pointToJsPoint(point)
            jsPoints.pushMap(jsPoint)
        }

        routeMetadata.putArray("points", jsPoints)

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
            val jsPoint = PointUtil.pointToJsPoint(point)
            jsonPoints.pushMap(jsPoint)
        }

        routeMetadata.putArray("points", jsonPoints)

        return routeMetadata
    }

    private fun transportHasStyle(transport: Transport): Boolean {
        return transport.line.style != null
    }

    private fun formatColor(color: Int): String {
        return String.format("#%06X", (0xFFFFFF and color))
    }

    companion object {
        const val NAME = "RTNTransportModule"
        private const val MAX_PARK_ROUTE_ITERATIONS = 6
        private const val PARK_ROUTE_BUFFER_METERS = 20.0
        private const val DETOUR_POINT_EPSILON_DEGREES = 1e-6
    }
}
