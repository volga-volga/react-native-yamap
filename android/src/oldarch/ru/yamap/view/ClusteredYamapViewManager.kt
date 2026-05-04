package ru.yamap.view

import android.view.View
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewGroupManager
import com.facebook.react.uimanager.annotations.ReactProp

class ClusteredYamapViewManager : ViewGroupManager<ClusteredYamapView>() {

    private val implementation = YamapViewManagerImpl()
    private val clusteredImplementation = ClusteredYamapViewManagerImpl()

    override fun getName() = ClusteredYamapViewManagerImpl.NAME

    override fun getExportedCustomBubblingEventTypeConstants() =
        YamapViewManagerImpl.exportedCustomBubblingEventTypeConstants

    override fun getCommandsMap() = YamapViewManagerImpl.commandsMap

    override fun receiveCommand(
        view: ClusteredYamapView,
        commandType: String,
        argsArr: ReadableArray?
    ) {
        implementation.receiveCommand(view, commandType, argsArr)
    }

    @ReactProp(name = "clusteredMarkers")
    fun setClusteredMarkers(view: ClusteredYamapView, points: ReadableArray) {
        clusteredImplementation.setClusteredMarkers(view, points)
    }

    @ReactProp(name = "clusterColor")
    fun setClusterColor(view: ClusteredYamapView, color: Int) {
        clusteredImplementation.setClusterColor(view, color)
    }

    @ReactProp(name = "clusterIcon")
    fun setClusterIcon(view: ClusteredYamapView, source: String?) {
        clusteredImplementation.setClusterIcon(view, source)
    }

    @ReactProp(name = "clusterSize")
    fun setClusterSize(view: ClusteredYamapView, size: ReadableMap?) {
        clusteredImplementation.setClusterSize(view, size)
    }

    @ReactProp(name = "clusterTextSize")
    fun setClusterTextSize(view: ClusteredYamapView, size: Float) {
        clusteredImplementation.setClusterTextSize(view, size)
    }

    @ReactProp(name = "clusterTextYOffset")
    fun setClusterTextYOffset(view: ClusteredYamapView, offset: Int) {
        clusteredImplementation.setClusterTextYOffset(view, offset)
    }

    @ReactProp(name = "clusterTextXOffset")
    fun setClusterTextXOffset(view: ClusteredYamapView, offset: Int) {
        clusteredImplementation.setClusterTextXOffset(view, offset)
    }

    @ReactProp(name = "clusterTextColor")
    fun setClusterTextColor(view: ClusteredYamapView, color: Int) {
        clusteredImplementation.setClusterTextColor(view, color)
    }

    override fun createViewInstance(context: ThemedReactContext) =
        clusteredImplementation.createViewInstance(context)

    // PROPS
    @ReactProp(name = "userLocationIcon")
    fun setUserLocationIcon(view: ClusteredYamapView, icon: String?) {
        implementation.setUserLocationIcon(view, icon)
    }

    @ReactProp(name = "userLocationIconScale")
    fun setUserLocationIconScale(view: ClusteredYamapView, scale: Float) {
        implementation.setUserLocationIconScale(view, scale)
    }

    @ReactProp(name = "userLocationAccuracyFillColor")
    fun setUserLocationAccuracyFillColor(view: ClusteredYamapView, color: Int) {
        implementation.setUserLocationAccuracyFillColor(view, color)
    }

    @ReactProp(name = "userLocationAccuracyStrokeColor")
    fun setUserLocationAccuracyStrokeColor(view: ClusteredYamapView, color: Int) {
        implementation.setUserLocationAccuracyStrokeColor(view, color)
    }

    @ReactProp(name = "userLocationAccuracyStrokeWidth")
    fun setUserLocationAccuracyStrokeWidth(view: ClusteredYamapView, width: Float) {
        implementation.setUserLocationAccuracyStrokeWidth(view, width)
    }

    @ReactProp(name = "showUserPosition")
    fun setShowUserPosition(view: ClusteredYamapView, show: Boolean) {
        implementation.setShowUserPosition(view, show)
    }

    @ReactProp(name = "followUser")
    fun setFollowUser(view: ClusteredYamapView, follow: Boolean) {
        implementation.setFollowUser(view, follow)
    }

    @ReactProp(name = "nightMode")
    fun setNightMode(view: ClusteredYamapView, nightMode: Boolean) {
        implementation.setNightMode(view, nightMode)
    }

    @ReactProp(name = "scrollGesturesDisabled")
    fun setScrollGesturesDisabled(view: ClusteredYamapView, value: Boolean) {
        implementation.setScrollGesturesDisabled(view, value)
    }

    @ReactProp(name = "rotateGesturesDisabled")
    fun setRotateGesturesDisabled(view: ClusteredYamapView, value: Boolean) {
        implementation.setRotateGesturesDisabled(view, value)
    }

    @ReactProp(name = "zoomGesturesDisabled")
    fun setZoomGesturesDisabled(view: ClusteredYamapView, value: Boolean) {
        implementation.setZoomGesturesDisabled(view, value)
    }

    @ReactProp(name = "tiltGesturesDisabled")
    fun setTiltGesturesDisabled(view: ClusteredYamapView, value: Boolean) {
        implementation.setTiltGesturesDisabled(view, value)
    }

    @ReactProp(name = "fastTapDisabled")
    fun setFastTapDisabled(view: ClusteredYamapView, value: Boolean) {
        implementation.setFastTapDisabled(view, value)
    }

    @ReactProp(name = "mapStyle")
    fun setMapStyle(view: ClusteredYamapView, style: String?) {
        implementation.setMapStyle(view, style)
    }

    @ReactProp(name = "mapType")
    fun setMapType(view: ClusteredYamapView, type: String?) {
        implementation.setMapType(view, type)
    }

    @ReactProp(name = "initialRegion")
    fun setInitialRegion(view: ClusteredYamapView, params: ReadableMap?) {
        implementation.setInitialRegion(view, params)
    }

    @ReactProp(name = "interactiveDisabled")
    fun setInteractiveDisabled(view: ClusteredYamapView, value: Boolean) {
        implementation.setInteractiveDisabled(view, value)
    }

    @ReactProp(name = "logoPosition")
    fun setLogoPosition(view: ClusteredYamapView, params: ReadableMap?) {
        implementation.setLogoPosition(view, params)
    }

    @ReactProp(name = "logoPadding")
    fun setLogoPadding(view: ClusteredYamapView, params: ReadableMap?) {
        implementation.setLogoPadding(view, params)
    }

    override fun addView(parent: ClusteredYamapView, child: View, index: Int) {
        parent.addFeature(child, index)
        super.addView(parent, child, index)
    }

    override fun removeViewAt(parent: ClusteredYamapView, index: Int) {
        parent.removeChild(index)
        super.removeViewAt(parent, index)
    }
}
