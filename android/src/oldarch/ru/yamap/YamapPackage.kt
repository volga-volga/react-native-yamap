package ru.yamap

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager
import ru.yamap.module.SearchModule
import ru.yamap.module.SuggestsModule
import ru.yamap.module.TransportModule
import ru.yamap.module.YamapModule
import ru.yamap.view.YamapViewManager
import ru.yamap.view.ClusteredYamapViewManager
import ru.yamap.view.PolygonViewManager
import ru.yamap.view.PolylineViewManager
import ru.yamap.view.MarkerViewManager
import ru.yamap.view.CircleViewManager

class YamapPackage : ReactPackage {

    override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> =
        listOf(
            TransportModule(reactContext),
            SearchModule(reactContext),
            SuggestsModule(reactContext),
            YamapModule(reactContext)
        )

    override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> =
        listOf(
            YamapViewManager(),
            ClusteredYamapViewManager(),
            PolygonViewManager(),
            PolylineViewManager(),
            MarkerViewManager(),
            CircleViewManager()
        )
}
