package ru.vvdev.yamap.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
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
import android.graphics.Color
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Path
import android.graphics.Shader
import android.graphics.BitmapShader
import android.graphics.BitmapFactory


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
    private var similarMarkersCount: Int? = null

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

    fun setSimilarMarkersCount(count: Int?) {
        similarMarkersCount = count
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
        // Проверяем, нужно ли отрисовывать текст
        if (similarMarkersCount != null && similarMarkersCount!! > 1) {
            drawTextMarker(similarMarkersCount!!)
            } else { // если нет, то текущая логика
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
                    if (iconSource != "") { // если есть ссылка на ресурс отрисовывем картинку
                        iconSource?.let {
                            DownloadImageBitmap(context, it, object : Callback<Bitmap?> {
                                override fun invoke(arg: Bitmap?) {
                                    try {
                                        if (arg != null) {
                                            val scaledBitmap = Bitmap.createScaledBitmap(arg, 140, 140, true)
                                            val icon = createRoundedMarkerImage(scaledBitmap)
                                            // val icon = createRoundedMarkerImage(arg)
                                            (rnMapObject as PlacemarkMapObject).setIcon(ImageProvider.fromBitmap(icon))
                                            (rnMapObject as PlacemarkMapObject).setIconStyle(iconStyle)
                                        }
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
    }
    // Создаем Bitmap для текстового маркера
    private fun drawTextMarker(count: Int) {
        val textBitmap = Bitmap.createBitmap(140, 140, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(textBitmap)
    
        // Рисуем белый фон с закругленными углами
        val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        val radius = 80f // Радиус закругления
        canvas.drawRoundRect(RectF(0f, 0f, textBitmap.width.toFloat(), textBitmap.height.toFloat()), radius, radius, backgroundPaint)
    
        // Настраиваем Paint для текста
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 60f
            textAlign = Paint.Align.CENTER
        }
    
        // Отрисовываем текст в центре
        canvas.drawText(count.toString(), (textBitmap.width / 2).toFloat(), (textBitmap.height / 2).toFloat() - (textPaint.descent() + textPaint.ascent()) / 2, textPaint)
    
        // Рисуем черную обводку вокруг закругленного фона
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 10f // Толщина обводки
        }

        // Рисуем обводку вокруг закругленного фона
        canvas.drawRoundRect(RectF(10f, 10f, (textBitmap.width - 10f).toFloat(), (textBitmap.height - 10f).toFloat()), radius, radius, borderPaint)
    
        // Устанавливаем иконку маркера как текстовый Bitmap
        (rnMapObject as PlacemarkMapObject).setIcon(ImageProvider.fromBitmap(textBitmap))
    }

    // Создаем Bitmap для изображения
    private fun createRoundedMarkerImage(markerImage: Bitmap?): Bitmap? {
        if (markerImage == null) return null
    
        val radius = 80f // Радиус закругления
        val borderWidth = 10f // Ширина границы
        val borderColor = Color.WHITE // Цвет границы
    
        // Создаем BitmapShader из изображения маркера
        val shader = BitmapShader(markerImage, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        val paint = Paint(ANTI_ALIAS_FLAG)
        paint.shader = shader // Устанавливаем Shader для Paint
    
        // Создаем новый Bitmap для закругленного изображения
        val roundedBitmap = Bitmap.createBitmap(
            140 + (borderWidth * 2).toInt(), // Ширина с учётом границы
            140 + (borderWidth * 2).toInt(), // Высота с учётом границы
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(roundedBitmap)
    
        // Рисуем закругленный фон (с учётом отступов для границы)
        canvas.drawRoundRect(
            RectF(borderWidth, borderWidth, 
                   (markerImage.width + borderWidth).toFloat(), 
                   (markerImage.height + borderWidth).toFloat()), 
            radius, radius, paint
        )
    
        // Рисуем белую границу
        val borderPaint = Paint(ANTI_ALIAS_FLAG)
        borderPaint.color = borderColor
        borderPaint.style = Paint.Style.STROKE // Устанавливаем стиль рисования: только обводка
        borderPaint.strokeWidth = borderWidth // Устанавливаем толщину обводки
    
        // Рисуем границу вокруг закругленного изображения
        canvas.drawRoundRect(
            RectF(borderWidth, borderWidth, 
                   (markerImage.width + borderWidth).toFloat(), 
                   (markerImage.height + borderWidth).toFloat()), 
            radius, radius, borderPaint
        )
    
        return roundedBitmap
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
