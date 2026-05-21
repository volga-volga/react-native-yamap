package ru.yamap.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PointF
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.graphics.createBitmap
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.UIManagerHelper
import com.facebook.react.uimanager.UIManagerHelper.getSurfaceId
import com.facebook.react.views.view.ReactViewGroup
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.MapObject
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.ModelStyle
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.map.RotationType
import com.yandex.runtime.DataProviderWithId
import com.yandex.runtime.image.ImageProvider
import ru.yamap.events.YamapMarkerPressEvent
import ru.yamap.models.ReactMapObject
import ru.yamap.utils.Callback
import ru.yamap.utils.ImageCacheManager
import ru.yamap.utils.MapModel
import ru.yamap.utils.ModelCacheManager
import java.io.File


class MarkerView(context: Context?) : ReactViewGroup(context), MapObjectTapListener,
    ReactMapObject {
    @JvmField
    var point: Point? = null
    private var _zIndex = 1f
    private var _scale = 1f
    private var _direction = 0f
    private var _visible = true
    private var _handled = false
    private var _rotated = false
    private var _markerAnchor: PointF? = null
    private var _iconSource: String? = null
    private var _icon3dSource: String? = null
    private var _childView: View? = null
    override var rnMapObject: MapObject? = null
    private val _children = ArrayList<View>()

    // OnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom -> updateMarker() }
    private val childLayoutListener =
        View.OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> updateMarker() }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    }

    // PROPS
    fun setPoint(point: Point?) {
        this.point = point
        updateMarker()
    }

    fun setZIndex(zIndex: Float) {
        _zIndex = zIndex
        updateMarker()
    }

    fun setDirection(direction: Float) {
        _direction = direction
        updateMarker()
    }

    fun setScale(scale: Float) {
        _scale = scale
        updateMarker()
    }

    fun setHandled(handled: Boolean) {
        _handled = handled
    }

    fun setRotated(rotated: Boolean) {
        _rotated = rotated
        updateMarker()
    }

    fun setVisible(visible: Boolean) {
        _visible = visible
        updateMarker()
    }

    fun setIconSource(source: String?) {
        _iconSource = source
        updateMarker()
    }

    fun setIcon3dSource(source: String?) {
        _icon3dSource = source
        updateMarker()
    }

    fun setAnchor(anchor: PointF?) {
        _markerAnchor = anchor
        updateMarker()
    }

    private fun updateMarker() {
        if (rnMapObject != null && rnMapObject!!.isValid) {
            val iconStyle = IconStyle()
            iconStyle.scale = _scale
            iconStyle.rotationType = if (_rotated) RotationType.ROTATE else RotationType.NO_ROTATION
            iconStyle.visible = _visible
            if (_markerAnchor != null) {
                iconStyle.anchor = _markerAnchor
            }
            (rnMapObject as PlacemarkMapObject).geometry = point!!
            (rnMapObject as PlacemarkMapObject).zIndex = _zIndex
            (rnMapObject as PlacemarkMapObject).direction = _direction
            (rnMapObject as PlacemarkMapObject).setIconStyle(iconStyle)

            if (_childView != null) {
                try {
                    val b = createBitmap(_childView!!.width, _childView!!.height)
                    val c = Canvas(b)
                    _childView!!.draw(c)
                    (rnMapObject as PlacemarkMapObject).setIcon(ImageProvider.fromBitmap(b))
                    (rnMapObject as PlacemarkMapObject).setIconStyle(iconStyle)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (_children.size == 0 && _iconSource != "") {
                _iconSource?.let { source ->
                    ImageCacheManager.getImage(context, source, fun(image: Bitmap?) {
                        ImageProvider.fromBitmap(image).let {
                            (rnMapObject as PlacemarkMapObject).setIcon(it)
                            (rnMapObject as PlacemarkMapObject).setIconStyle(iconStyle)
                        }
                    })
                }
            }
            if (_children.size == 0 && _icon3dSource != "") {
                _icon3dSource?.let { source ->
                    ModelCacheManager.getModel(context, source, fun (model: MapModel?) {
                        val newModelStyle = ModelStyle(1f, ModelStyle.UnitType.METER,
                            ModelStyle.RenderMode.USER_MODEL, source)
                        val mod = (rnMapObject as PlacemarkMapObject).useModel()
                        mod.setData(model!!, object : com.yandex.mapkit.map.Callback {
                            override fun onTaskFinished() {
                                println("Success")
                            }
                        })
                        mod.modelStyle = newModelStyle
                    } )
                }

            }
        }
    }

    fun setMarkerMapObject(obj: MapObject?) {
        rnMapObject = obj as PlacemarkMapObject?
        rnMapObject!!.addTapListener(this)
        updateMarker()
    }

    private fun setChildView(view: View?) {
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
        _children.add(index, view)
        setChildView(_children[0])
    }

    fun removeChildView(index: Int) {
        _children.removeAt(index)
        setChildView(if (_children.isNotEmpty()) _children[0] else null)
    }

    private fun moveAnimationLoop(lat: Double, lon: Double) {
        (rnMapObject as PlacemarkMapObject).geometry = Point(lat, lon)
    }

    private fun rotateAnimationLoop(delta: Float) {
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
        val eventDispatcher = UIManagerHelper.getEventDispatcherForReactTag(context as ThemedReactContext, id)
        eventDispatcher?.dispatchEvent(YamapMarkerPressEvent(getSurfaceId(context), id))

        return _handled
    }
}
