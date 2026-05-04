package ru.yamap.view

import android.view.View
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewGroupManager
import com.facebook.react.uimanager.annotations.ReactProp

class MarkerViewManager : ViewGroupManager<MarkerView>() {

    private val implementation = MarkerViewManagerImpl()

    override fun getName() = MarkerViewManagerImpl.NAME

    override fun getExportedCustomBubblingEventTypeConstants() =
        MarkerViewManagerImpl.exportedCustomBubblingEventTypeConstants

    public override fun createViewInstance(context: ThemedReactContext) = MarkerView(context)

    // PROPS
    @ReactProp(name = "point")
    fun setPoint(view: MarkerView, jsPoint: ReadableMap?) {
        implementation.setPoint(view, jsPoint)
    }

    @ReactProp(name = "zI")
    fun setZI(view: MarkerView, zIndex: Float) {
        implementation.setZI(view, zIndex)
    }

    @ReactProp(name = "scale")
    fun setScale(view: MarkerView, scale: Float) {
        implementation.setScale(view, scale)
    }

    @ReactProp(name = "handled")
    fun setHandled(view: MarkerView, handled: Boolean) {
        implementation.setHandled(view, handled)
    }

    @ReactProp(name = "rotated")
    fun setRotated(view: MarkerView, rotated: Boolean) {
        implementation.setRotated(view, rotated)
    }

    @ReactProp(name = "visible")
    fun setVisible(view: MarkerView, visible: Boolean) {
        implementation.setVisible(view, visible)
    }

    @ReactProp(name = "source")
    fun setSource(view: MarkerView, source: String?) {
        implementation.setSource(view, source)
    }

    @ReactProp(name = "anchor")
    fun setAnchor(view: MarkerView, anchor: ReadableMap?) {
        implementation.setAnchor(view, anchor)
    }

    override fun addView(parent: MarkerView, child: View, index: Int) {
        parent.addChildView(child, index)
        super.addView(parent, child, index)
    }

    override fun removeViewAt(parent: MarkerView, index: Int) {
        parent.removeChildView(index)
        super.removeViewAt(parent, index)
    }

    override fun receiveCommand(view: MarkerView, commandType: String, argsArr: ReadableArray?) {
        implementation.receiveCommand(view, commandType, argsArr)
    }
}
