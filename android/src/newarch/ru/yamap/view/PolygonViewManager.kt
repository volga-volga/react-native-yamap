package ru.yamap.view

import com.facebook.react.bridge.ReadableArray
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewGroupManager
import com.facebook.react.viewmanagers.PolygonViewManagerDelegate
import com.facebook.react.viewmanagers.PolygonViewManagerInterface

class PolygonViewManager : ViewGroupManager<PolygonView>(),  PolygonViewManagerInterface<PolygonView> {

    private val implementation = PolygonViewManagerImpl()
    private val delegate = PolygonViewManagerDelegate(this)

    override fun getDelegate() = delegate

    override fun getName() = PolygonViewManagerImpl.NAME

    override fun getExportedCustomBubblingEventTypeConstants() =
        PolygonViewManagerImpl.exportedCustomBubblingEventTypeConstants

    override fun createViewInstance(context: ThemedReactContext) = PolygonView(context)

    // PROPS
    override fun setPoints(view: PolygonView, jsPoints: ReadableArray?) {
        implementation.setPoints(view, jsPoints)
    }

    override fun setInnerRings(view: PolygonView, jsRings: ReadableArray?) {
        implementation.setInnerRings(view, jsRings)
    }

    override fun setStrokeWidth(view: PolygonView, width: Float) {
        implementation.setStrokeWidth(view, width)
    }

    override fun setStrokeColor(view: PolygonView, color: Int) {
        implementation.setStrokeColor(view, color)
    }

    override fun setFillColor(view: PolygonView, color: Int) {
        implementation.setFillColor(view, color)
    }

    override fun setZI(view: PolygonView, zIndex: Float) {
        implementation.setZI(view, zIndex)
    }

    override fun setHandled(view: PolygonView, handled: Boolean) {
        implementation.setHandled(view, handled)
    }
}
