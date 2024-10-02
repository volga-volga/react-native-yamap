package ru.vvdev.yamap.view

import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.uimanager.events.RCTEventEmitter
import com.yandex.mapkit.geometry.LinearRing
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polygon
import com.yandex.mapkit.map.MapObject
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PolygonMapObject
import ru.vvdev.yamap.models.ReactMapObject

class YamapPolygon(context: Context?) : ViewGroup(context), MapObjectTapListener, ReactMapObject {
    @JvmField
    var polygon: Polygon
    var _points: ArrayList<Point> = ArrayList()
    var innerRings: ArrayList<ArrayList<Point>>? = ArrayList()
    override var rnMapObject: MapObject? = null
    private var fillColor = Color.BLACK
    private var strokeColor = Color.BLACK
    private var zIndex = 1
    private var strokeWidth = 1f
    private var handled = true

    init {
        polygon = Polygon(LinearRing(ArrayList()), ArrayList())
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
    }

    // PROPS
    fun setPolygonPoints(points: ArrayList<Point>?) {
        _points = if ((points != null)) points else ArrayList()
        updatePolygonGeometry()
        updatePolygon()
    }

    fun setPolygonInnerRings(_innerRings: ArrayList<ArrayList<Point>>?) {
        innerRings = _innerRings ?: ArrayList()
        updatePolygonGeometry()
        updatePolygon()
    }

//    fun setInnerRings(_innerRings: ArrayList<ArrayList<Point>>?) {
//        innerRings = _innerRings ?: ArrayList()
//        updatePolygonGeometry()
//        updatePolygon()
//    }

    private fun updatePolygonGeometry() {
        val _rings = ArrayList<LinearRing>()
        if (innerRings != null) {
            for (i in innerRings!!.indices) {
                _rings.add(LinearRing(innerRings!![i]))
            }
        }
        polygon = Polygon(LinearRing(_points), _rings)
    }

    fun setZIndex(_zIndex: Int) {
        zIndex = _zIndex
        updatePolygon()
    }

    fun setStrokeColor(_color: Int) {
        strokeColor = _color
        updatePolygon()
    }

    fun setFillColor(_color: Int) {
        fillColor = _color
        updatePolygon()
    }

    fun setStrokeWidth(width: Float) {
        strokeWidth = width
        updatePolygon()
    }

    private fun updatePolygon() {
        if (rnMapObject != null) {
            (rnMapObject as PolygonMapObject).geometry = polygon
            (rnMapObject as PolygonMapObject).strokeWidth = strokeWidth
            (rnMapObject as PolygonMapObject).strokeColor = strokeColor
            (rnMapObject as PolygonMapObject).fillColor = fillColor
            (rnMapObject as PolygonMapObject).zIndex = zIndex.toFloat()
        }
    }

    fun setPolygonMapObject(obj: MapObject?) {
        rnMapObject = obj as PolygonMapObject?
        rnMapObject!!.addTapListener(this)
        updatePolygon()
    }

    fun setHandled(_handled: Boolean) {
        handled = _handled
    }

//    fun setRnMapObject(obj: MapObject?) {
//        rnMapObject = obj as PolygonMapObject?
//        rnMapObject!!.addTapListener(this)
//        updatePolygon()
//    }

    override fun onMapObjectTap(mapObject: MapObject, point: Point): Boolean {
        val e = Arguments.createMap()
        (context as ReactContext).getJSModule(RCTEventEmitter::class.java).receiveEvent(
            id, "onPress", e
        )

        return handled
    }
}
