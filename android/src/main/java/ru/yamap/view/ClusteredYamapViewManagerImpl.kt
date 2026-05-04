package ru.yamap.view

import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.uimanager.ThemedReactContext
import com.yandex.mapkit.MapKitFactory
import java.time.ZoneOffset

class ClusteredYamapViewManagerImpl() {

    fun setClusteredMarkers(view: ClusteredYamapView, points: ReadableArray?) {
        points?.let {
            @Suppress("UNCHECKED_CAST")
            view.setClusteredMarkers(it.toArrayList() as ArrayList<HashMap<String, Double>>)
        }
    }

    fun setClusterColor(view: ClusteredYamapView, color: Int) {
        view.setClustersColor(color)
    }


    fun setClusterIcon(view: ClusteredYamapView, source: String?) {
        source?.let {
            view.setClusterIcon(it)
        }
    }

    fun setClusterSize(view: ClusteredYamapView, size: ReadableMap?) {
        size?.let {
            view.setClusterSize(it)
        }
    }

    fun setClusterTextSize(view: ClusteredYamapView, size: Float) {
        view.setClusterTextSize(size)
    }

    fun setClusterTextYOffset(view: ClusteredYamapView, offset: Int) {
        view.setClusterTextYOffset(offset)
    }

    fun setClusterTextXOffset(view: ClusteredYamapView, offset: Int) {
        view.setClusterTextXOffset(offset)
    }

    fun setClusterTextColor(view: ClusteredYamapView, color: Int) {
        view.setClusterTextColor(color)
    }

    fun createViewInstance(context: ThemedReactContext): ClusteredYamapView {
        val view = ClusteredYamapView(context)
        MapKitFactory.getInstance().onStart()
        view.onStart()
        return view
    }

    companion object {
        const val NAME = "ClusteredYamapView"
    }
}
