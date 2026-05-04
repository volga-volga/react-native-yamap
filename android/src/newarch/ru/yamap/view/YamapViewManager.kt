package ru.yamap.view

import android.view.View
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewGroupManager
import com.facebook.react.viewmanagers.YamapViewManagerDelegate
import com.facebook.react.viewmanagers.YamapViewManagerInterface

class YamapViewManager : ViewGroupManager<YamapView>(), YamapViewManagerInterface<YamapView> {

    private val implementation = YamapViewManagerImpl()
    private val delegate = YamapViewManagerDelegate(this)

    override fun getDelegate() = delegate

    override fun getName() = YamapViewManagerImpl.NAME

    override fun getExportedCustomBubblingEventTypeConstants() =
        YamapViewManagerImpl.exportedCustomBubblingEventTypeConstants

    override fun getExportedCustomDirectEventTypeConstants() =
        YamapViewManagerImpl.exportedCustomDirectEventTypeConstants

    override fun getCommandsMap() = YamapViewManagerImpl.commandsMap

    override fun receiveCommand(view: YamapView, commandType: String, argsArr: ReadableArray?) {
        implementation.receiveCommand(view, commandType, argsArr)
    }

    override fun createViewInstance(context: ThemedReactContext) =
        implementation.createViewInstance(context)

    // PROPS
    override fun setUserLocationIcon(view: YamapView, icon: String?) {
        implementation.setUserLocationIcon(view, icon)
    }

    override fun setUserLocationIconScale(view: YamapView, scale: Float) {
        implementation.setUserLocationIconScale(view, scale)
    }

    override fun setUserLocationAccuracyFillColor(view: YamapView, color: Int) {
        implementation.setUserLocationAccuracyFillColor(view, color)
    }

    override fun setUserLocationAccuracyStrokeColor(view: YamapView, color: Int) {
        implementation.setUserLocationAccuracyStrokeColor(view, color)
    }

    override fun setUserLocationAccuracyStrokeWidth(view: YamapView, width: Float) {
        implementation.setUserLocationAccuracyStrokeWidth(view, width)
    }

    override fun setShowUserPosition(view: YamapView, show: Boolean) {
        implementation.setShowUserPosition(view, show)
    }

    override fun setNightMode(view: YamapView, nightMode: Boolean) {
        implementation.setNightMode(view, nightMode)
    }

    override fun setScrollGesturesDisabled(view: YamapView, value: Boolean) {
        implementation.setScrollGesturesDisabled(view, value)
    }

    override fun setRotateGesturesDisabled(view: YamapView, value: Boolean) {
        implementation.setRotateGesturesDisabled(view, value)
    }

    override fun setZoomGesturesDisabled(view: YamapView, value: Boolean) {
        implementation.setZoomGesturesDisabled(view, value)
    }

    override fun setTiltGesturesDisabled(view: YamapView, value: Boolean) {
        implementation.setTiltGesturesDisabled(view, value)
    }

    override fun setFastTapDisabled(view: YamapView, value: Boolean) {
        implementation.setFastTapDisabled(view, value)
    }

    override fun setMapStyle(view: YamapView, style: String?) {
        implementation.setMapStyle(view, style)
    }

    override fun setMapType(view: YamapView, type: String?) {
        implementation.setMapType(view, type)
    }

    override fun setInitialRegion(view: YamapView, params: ReadableMap?) {
        implementation.setInitialRegion(view, params)
    }

    override fun setInteractiveDisabled(view: YamapView, value: Boolean) {
        implementation.setInteractiveDisabled(view, value)
    }

    override fun setLogoPosition(view: YamapView, params: ReadableMap?) {
        implementation.setLogoPosition(view, params)
    }

    override fun setLogoPadding(view: YamapView, params: ReadableMap?) {
        implementation.setLogoPadding(view, params)
    }

    override fun setFollowUser(view: YamapView, value: Boolean) {
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
