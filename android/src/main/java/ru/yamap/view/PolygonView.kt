package ru.yamap.view

import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.UIManagerHelper
import com.facebook.react.uimanager.UIManagerHelper.getSurfaceId
import com.yandex.mapkit.geometry.LinearRing
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polygon
import com.yandex.mapkit.map.MapObject
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PolygonMapObject
import ru.yamap.events.YamapPolygonPressEvent
import ru.yamap.models.ReactMapObject

class PolygonView(context: Context?) : ViewGroup(context), MapObjectTapListener, ReactMapObject {
    @JvmField
    var polygon: Polygon
    private var _points: ArrayList<Point> = ArrayList()
    private var _innerRings: ArrayList<ArrayList<Point>>? = ArrayList()
    override var rnMapObject: MapObject? = null
    private var _fillColor = Color.BLACK
    private var _strokeColor = Color.BLACK
    private var _zIndex = 1f
    private var _strokeWidth = 0f
    private var _handled = false

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

    fun setPolygonInnerRings(innerRings: ArrayList<ArrayList<Point>>?) {
        _innerRings = innerRings ?: ArrayList()
        updatePolygonGeometry()
        updatePolygon()
    }

    private fun updatePolygonGeometry() {
        val rings = ArrayList<LinearRing>()
        if (_innerRings != null) {
            for (i in _innerRings!!.indices) {
                rings.add(LinearRing(_innerRings!![i]))
            }
        }
        polygon = Polygon(LinearRing(_points), rings)
    }

    fun setZIndex(zIndex: Float) {
        _zIndex = zIndex
        updatePolygon()
    }

    fun setStrokeColor(color: Int) {
        _strokeColor = color
        updatePolygon()
    }

    fun setFillColor(color: Int) {
        _fillColor = color
        updatePolygon()
    }

    fun setStrokeWidth(width: Float) {
        _strokeWidth = width
        updatePolygon()
    }

    private fun updatePolygon() {
        if (rnMapObject != null) {
            (rnMapObject as PolygonMapObject).geometry = polygon
            (rnMapObject as PolygonMapObject).strokeWidth = _strokeWidth
            (rnMapObject as PolygonMapObject).strokeColor = _strokeColor
            (rnMapObject as PolygonMapObject).fillColor = _fillColor
            (rnMapObject as PolygonMapObject).zIndex = _zIndex
        }
    }

    fun setPolygonMapObject(obj: MapObject?) {
        rnMapObject = obj as PolygonMapObject?
        rnMapObject!!.addTapListener(this)
        updatePolygon()
    }

    fun setHandled(handled: Boolean) {
        _handled = handled
    }

    override fun onMapObjectTap(mapObject: MapObject, point: Point): Boolean {
        val eventDispatcher = UIManagerHelper.getEventDispatcherForReactTag(context as ThemedReactContext, id)
        eventDispatcher?.dispatchEvent(YamapPolygonPressEvent(getSurfaceId(context), id))

        return _handled
    }
}
