package ru.yamap.utils

import android.graphics.PointF
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableMap
import com.yandex.mapkit.geometry.Point

class PointUtil {
    companion object {
        fun readableMapToPoint(jsPoint: ReadableMap): Point {
            return Point(
                jsPoint.getDouble("lat"),
                jsPoint.getDouble("lon")
            )
        }

        fun pointToJsPoint(point: Point?): WritableMap {
            val jsPoint = Arguments.createMap()
            point?.longitude?.let { jsPoint.putDouble("lon", it) }
            point?.latitude?.let { jsPoint.putDouble("lat", it) }
            return jsPoint
        }

        fun jsPointToPointF(jsPoint: ReadableMap?): PointF? {
            if (jsPoint === null) return null

            return PointF(
                jsPoint.getDouble("x").toFloat(),
                jsPoint.getDouble("y").toFloat()
            )
        }

        fun jsPointsToPoints(jsPoints: ReadableArray): ArrayList<Point> {
            val points = ArrayList<Point>()

            for (i in 0 until jsPoints.size()) {
                jsPoints.getMap(i)?.let {
                    points.add(readableMapToPoint(it))
                }
            }

            return points
        }
    }
}
