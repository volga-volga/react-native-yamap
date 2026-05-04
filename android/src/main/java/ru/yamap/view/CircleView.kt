package ru.yamap.view

import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.UIManagerHelper
import com.facebook.react.uimanager.UIManagerHelper.getSurfaceId
import com.yandex.mapkit.geometry.Circle
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CircleMapObject
import com.yandex.mapkit.map.MapObject
import com.yandex.mapkit.map.MapObjectTapListener
import ru.yamap.events.YamapCirclePressEvent
import ru.yamap.models.ReactMapObject

class CircleView(context: Context?) : ViewGroup(context), MapObjectTapListener, ReactMapObject {
    @JvmField
    var circle: Circle

    override var rnMapObject: MapObject? = null
    private var _handled = false
    private var _fillColor = Color.BLACK
    private var _strokeColor = Color.BLACK
    private var _zIndex = 1f
    private var _strokeWidth = 0f
    private var _center = Point(0.0, 0.0)
    private var _radius = 0f

    init {
        circle = Circle(_center, _radius)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
    }

    // PROPS
    fun setCenter(point: Point) {
        _center = point
        updateGeometry()
        updateCircle()
    }

    fun setRadius(radius: Float) {
        _radius = radius
        updateGeometry()
        updateCircle()
    }

    private fun updateGeometry() {
        circle = Circle(_center, _radius)
    }

    fun setZIndex(zIndex: Float) {
        _zIndex = zIndex
        updateCircle()
    }

    fun setHandled(handled: Boolean) {
        _handled = handled
    }

    fun setStrokeColor(color: Int) {
        _strokeColor = color
        updateCircle()
    }

    fun setFillColor(color: Int) {
        _fillColor = color
        updateCircle()
    }

    fun setStrokeWidth(width: Float) {
        _strokeWidth = width
        updateCircle()
    }

    private fun updateCircle() {
        if (rnMapObject != null) {
            (rnMapObject as CircleMapObject).geometry = circle
            (rnMapObject as CircleMapObject).strokeWidth = _strokeWidth
            (rnMapObject as CircleMapObject).strokeColor = _strokeColor
            (rnMapObject as CircleMapObject).fillColor = _fillColor
            (rnMapObject as CircleMapObject).zIndex = _zIndex
        }
    }

    fun setCircleMapObject(obj: MapObject?) {
        rnMapObject = obj as CircleMapObject?
        rnMapObject!!.addTapListener(this)
        updateCircle()
    }

    override fun onMapObjectTap(mapObject: MapObject, point: Point): Boolean {
        val eventDispatcher = UIManagerHelper.getEventDispatcherForReactTag(context as ThemedReactContext, id)
        eventDispatcher?.dispatchEvent(YamapCirclePressEvent(getSurfaceId(context), id))

        return _handled
    }
}
