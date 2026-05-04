package ru.yamap.view

import android.view.View
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewGroupManager
import com.facebook.react.uimanager.annotations.ReactProp

class YamapViewManager : ViewGroupManager<YamapView>() {

    private val implementation = YamapViewManagerImpl()

    override fun getName() = YamapViewManagerImpl.NAME

    override fun getExportedCustomBubblingEventTypeConstants() =
        YamapViewManagerImpl.exportedCustomBubblingEventTypeConstants

    override fun getExportedCustomDirectEventTypeConstants() =
        YamapViewManagerImpl.exportedCustomDirectEventTypeConstants

    override fun getCommandsMap() = YamapViewManagerImpl.commandsMap

    override fun receiveCommand(view: YamapView, commandType: String, argsArr: ReadableArray?) =
        implementation.receiveCommand(view, commandType, argsArr)

    override fun createViewInstance(context: ThemedReactContext) =
        implementation.createViewInstance(context)

    // PROPS
    @ReactProp(name = "userLocationIcon")
    fun setUserLocationIcon(view: YamapView, icon: String?) {
        implementation.setUserLocationIcon(view, icon)
    }

    @ReactProp(name = "userLocationIconScale")
    fun setUserLocationIconScale(view: YamapView, scale: Float) {
        implementation.setUserLocationIconScale(view, scale)
    }

    @ReactProp(name = "userLocationAccuracyFillColor")
    fun setUserLocationAccuracyFillColor(view: YamapView, color: Int) {
        implementation.setUserLocationAccuracyFillColor(view, color)
    }

    @ReactProp(name = "userLocationAccuracyStrokeColor")
    fun setUserLocationAccuracyStrokeColor(view: YamapView, color: Int) {
        implementation.setUserLocationAccuracyStrokeColor(view, color)
    }

    @ReactProp(name = "userLocationAccuracyStrokeWidth")
    fun setUserLocationAccuracyStrokeWidth(view: YamapView, width: Float) {
        implementation.setUserLocationAccuracyStrokeWidth(view, width)
    }

    @ReactProp(name = "showUserPosition")
    fun setShowUserPosition(view: YamapView, show: Boolean) {
        implementation.setShowUserPosition(view, show)
    }

    @ReactProp(name = "nightMode")
    fun setNightMode(view: YamapView, nightMode: Boolean) {
        implementation.setNightMode(view, nightMode)
    }

    @ReactProp(name = "scrollGesturesDisabled")
    fun setScrollGesturesDisabled(view: YamapView, value: Boolean) {
        implementation.setScrollGesturesDisabled(view, value)
    }

    @ReactProp(name = "rotateGesturesDisabled")
    fun setRotateGesturesDisabled(view: YamapView, value: Boolean) {
        implementation.setRotateGesturesDisabled(view, value)
    }

    @ReactProp(name = "zoomGesturesDisabled")
    fun setZoomGesturesDisabled(view: YamapView, value: Boolean) {
        implementation.setZoomGesturesDisabled(view, value)
    }

    @ReactProp(name = "tiltGesturesDisabled")
    fun setTiltGesturesDisabled(view: YamapView, value: Boolean) {
        implementation.setTiltGesturesDisabled(view, value)
    }

    @ReactProp(name = "fastTapDisabled")
    fun setFastTapDisabled(view: YamapView, value: Boolean) {
        implementation.setFastTapDisabled(view, value)
    }

    @ReactProp(name = "mapStyle")
    fun setMapStyle(view: YamapView, style: String?) {
        implementation.setMapStyle(view, style)
    }

    @ReactProp(name = "mapType")
    fun setMapType(view: YamapView, type: String?) {
        implementation.setMapType(view, type)
    }

    @ReactProp(name = "initialRegion")
    fun setInitialRegion(view: YamapView, params: ReadableMap?) {
        implementation.setInitialRegion(view, params)
    }

    @ReactProp(name = "interactiveDisabled")
    fun setInteractiveDisabled(view: YamapView, value: Boolean) {
        implementation.setInteractiveDisabled(view, value)
    }

    @ReactProp(name = "logoPosition")
    fun setLogoPosition(view: YamapView, params: ReadableMap?) {
        implementation.setLogoPosition(view, params)
    }

    @ReactProp(name = "logoPadding")
    fun setLogoPadding(view: YamapView, params: ReadableMap?) {
        implementation.setLogoPadding(view, params)
    }

    @ReactProp(name = "followUser")
    fun setFollowUser(view: YamapView, value: Boolean) {
        implementation.setFollowUser(view, value)
    }

    override fun addView(parent: YamapView, child: View, index: Int) {
        parent.addFeature(child, index)
        super.addView(parent, child, index)
    }

    override fun removeViewAt(parent: YamapView, index: Int) {
        parent.removeChild(index)
        super.removeViewAt(parent, index)
    }
}
