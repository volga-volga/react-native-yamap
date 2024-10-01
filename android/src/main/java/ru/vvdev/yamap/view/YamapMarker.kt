package ru.vvdev.yamap.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PointF
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.view.animation.LinearInterpolator
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.uimanager.events.RCTEventEmitter
import com.facebook.react.views.view.ReactViewGroup
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.MapObject
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.map.RotationType
import com.yandex.runtime.image.ImageProvider
import ru.vvdev.yamap.models.ReactMapObject
import ru.vvdev.yamap.utils.Callback
import ru.vvdev.yamap.utils.ImageLoader.DownloadImageBitmap

class YamapMarker(context: Context?) : ReactViewGroup(context), MapObjectTapListener,
    ReactMapObject {
    @JvmField
    var point: Point? = null
    private var zIndex = 1
    private var scale = 1f
    private var visible = true
    private var handled = true
    private var rotated = false
    private val YAMAP_FRAMES_PER_SECOND = 25
    private var markerAnchor: PointF? = null
    private var iconSource: String? = null
    private var _childView: View? = null
    override var rnMapObject: MapObject? = null
    private val childs = ArrayList<View>()

    private val childLayoutListener =
        OnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom -> updateMarker() }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
    }

    // PROPS
    fun setPoint(_point: Point?) {
        point = _point
        updateMarker()
    }

    fun setZIndex(_zIndex: Int) {
        zIndex = _zIndex
        updateMarker()
    }

    fun setScale(_scale: Float) {
        scale = _scale
        updateMarker()
    }

    fun setHandled(_handled: Boolean) {
        handled = _handled
    }

    fun setRotated(_rotated: Boolean) {
        rotated = _rotated
        updateMarker()
    }

    fun setVisible(_visible: Boolean) {
        visible = _visible
        updateMarker()
    }

    fun setIconSource(source: String?) {
        iconSource = source
        updateMarker()
    }

    fun setAnchor(anchor: PointF?) {
        markerAnchor = anchor
        updateMarker()
    }

    private fun updateMarker() {
        if (rnMapObject != null && rnMapObject!!.isValid) {
            val iconStyle = IconStyle()
            iconStyle.setScale(scale)
            iconStyle.setRotationType(if (rotated) RotationType.ROTATE else RotationType.NO_ROTATION)
            iconStyle.setVisible(visible)
            if (markerAnchor != null) {
                iconStyle.setAnchor(markerAnchor)
            }
            (rnMapObject as PlacemarkMapObject).geometry = point!!
            (rnMapObject as PlacemarkMapObject).zIndex = zIndex.toFloat()
            (rnMapObject as PlacemarkMapObject).setIconStyle(iconStyle)

            if (_childView != null) {
                try {
                    val b = Bitmap.createBitmap(
                        _childView!!.width, _childView!!.height, Bitmap.Config.ARGB_8888
                    )
                    val c = Canvas(b)
                    _childView!!.draw(c)
                    (rnMapObject as PlacemarkMapObject).setIcon(ImageProvider.fromBitmap(b))
                    (rnMapObject as PlacemarkMapObject).setIconStyle(iconStyle)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (childs.size == 0) {
                if (iconSource != "") {
                    iconSource?.let {
                        DownloadImageBitmap(context, it, object : Callback<Bitmap?> {
                            override fun invoke(arg: Bitmap?) {
                                try {
                                    val icon = ImageProvider.fromBitmap(arg)
                                    (rnMapObject as PlacemarkMapObject).setIcon(icon)
                                    (rnMapObject as PlacemarkMapObject).setIconStyle(iconStyle)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        })
                    }
                }
            }
        }
    }

    fun setMarkerMapObject(obj: MapObject?) {
        rnMapObject = obj as PlacemarkMapObject?
        rnMapObject!!.addTapListener(this)
        updateMarker()
    }

    fun setChildView(view: View?) {
        if (view == null) {
            _childView!!.removeOnLayoutChangeListener(childLayoutListener)
            _childView = null
            updateMarker()
            return
        }
        _childView = view
        _childView!!.addOnLayoutChangeListener(childLayoutListener)
    }

    fun addChildView(view: View, index: Int) {
        childs.add(index, view)
        setChildView(childs[0])
    }

    fun removeChildView(index: Int) {
        childs.removeAt(index)
        setChildView(if (childs.size > 0) childs[0] else null)
    }

    fun moveAnimationLoop(lat: Double, lon: Double) {
        (rnMapObject as PlacemarkMapObject).geometry = Point(lat, lon)
    }

    fun rotateAnimationLoop(delta: Float) {
        (rnMapObject as PlacemarkMapObject).direction = delta
    }

    fun animatedMoveTo(point: Point, duration: Float) {
        val p = (rnMapObject as PlacemarkMapObject).geometry
        val startLat = p.latitude
        val startLon = p.longitude
        val deltaLat = point.latitude - startLat
        val deltaLon = point.longitude - startLon
        val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
        valueAnimator.setDuration(duration.toLong())
        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.addUpdateListener { animation ->
            try {
                val v = animation.animatedFraction
                moveAnimationLoop(startLat + v * deltaLat, startLon + v * deltaLon)
            } catch (ex: Exception) {
                // I don't care atm..
            }
        }
        valueAnimator.start()
    }

    fun animatedRotateTo(angle: Float, duration: Float) {
        val placemark = (rnMapObject as PlacemarkMapObject)
        val startDirection = placemark.direction
        val delta = angle - placemark.direction
        val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
        valueAnimator.setDuration(duration.toLong())
        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.addUpdateListener { animation ->
            try {
                val v = animation.animatedFraction
                rotateAnimationLoop(startDirection + v * delta)
            } catch (ex: Exception) {
                // I don't care atm..
            }
        }
        valueAnimator.start()
    }

    override fun onMapObjectTap(mapObject: MapObject, point: Point): Boolean {
        val e = Arguments.createMap()
        (context as ReactContext).getJSModule(RCTEventEmitter::class.java).receiveEvent(
            id, "onPress", e
        )

        return handled
    }
}
