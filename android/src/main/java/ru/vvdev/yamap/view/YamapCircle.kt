package ru.vvdev.yamap.view

import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.uimanager.events.RCTEventEmitter
import com.yandex.mapkit.geometry.Circle
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CircleMapObject
import com.yandex.mapkit.map.MapObject
import com.yandex.mapkit.map.MapObjectTapListener
import ru.vvdev.yamap.models.ReactMapObject

class YamapCircle(context: Context?) : ViewGroup(context), MapObjectTapListener, ReactMapObject {
    @JvmField
    var circle: Circle

    override var rnMapObject: MapObject? = null
    private var handled = true
    private var fillColor = Color.BLACK
    private var strokeColor = Color.BLACK
    private var zIndex = 1
    private var strokeWidth = 1f
    private var center = Point(0.0, 0.0)
    private var radius = 0f

    init {
        circle = Circle(center, radius)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
    }

    // PROPS
    fun setCenter(point: Point) {
        center = point
        updateGeometry()
        updateCircle()
    }

    fun setRadius(_radius: Float) {
        radius = _radius
        updateGeometry()
        updateCircle()
    }

    private fun updateGeometry() {
        circle = Circle(center, radius)
    }

    fun setZIndex(_zIndex: Int) {
        zIndex = _zIndex
        updateCircle()
    }

    fun setHandled(_handled: Boolean) {
        handled = _handled
    }

    fun setStrokeColor(_color: Int) {
        strokeColor = _color
        updateCircle()
    }

    fun setFillColor(_color: Int) {
        fillColor = _color
        updateCircle()
    }

    fun setStrokeWidth(width: Float) {
        strokeWidth = width
        updateCircle()
    }

    private fun updateCircle() {
        if (rnMapObject != null) {
            (rnMapObject as CircleMapObject).geometry = circle
            (rnMapObject as CircleMapObject).strokeWidth = strokeWidth
            (rnMapObject as CircleMapObject).strokeColor = strokeColor
            (rnMapObject as CircleMapObject).fillColor = fillColor
            (rnMapObject as CircleMapObject).zIndex = zIndex.toFloat()
        }
    }

    fun setCircleMapObject(obj: MapObject?) {
        rnMapObject = obj as CircleMapObject?
        rnMapObject!!.addTapListener(this)
        updateCircle()
    }

//    fun setRnMapObject(obj: MapObject?) {
//        rnMapObject = obj as CircleMapObject?
//        rnMapObject!!.addTapListener(this)
//        updateCircle()
//    }

    override fun onMapObjectTap(mapObject: MapObject, point: Point): Boolean {
        val e = Arguments.createMap()
        (context as ReactContext).getJSModule(RCTEventEmitter::class.java).receiveEvent(
            id, "onPress", e
        )

        return handled
    }
}
