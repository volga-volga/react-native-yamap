package ru.yamap.module

import android.graphics.Color
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReadableArray
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
    }
}
