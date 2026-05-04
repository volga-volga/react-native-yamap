package ru.yamap

import com.facebook.react.BaseReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.model.ReactModuleInfo
import com.facebook.react.module.model.ReactModuleInfoProvider
import com.facebook.react.uimanager.ViewManager
import ru.yamap.module.SearchModule
import ru.yamap.module.SearchModuleImpl
import ru.yamap.module.SuggestsModule
import ru.yamap.module.SuggestsModuleImpl
import ru.yamap.module.TransportModule
import ru.yamap.module.TransportModuleImpl
import ru.yamap.module.YamapModule
import ru.yamap.module.YamapModuleImpl
import ru.yamap.view.YamapViewManager
import ru.yamap.view.ClusteredYamapViewManager
import ru.yamap.view.PolygonViewManager
import ru.yamap.view.PolylineViewManager
import ru.yamap.view.MarkerViewManager
import ru.yamap.view.CircleViewManager

class YamapPackage : BaseReactPackage() {

    override fun getModule(name: String, reactContext: ReactApplicationContext): NativeModule? =
        when (name) {
            TransportModuleImpl.NAME -> TransportModule(reactContext)
            SearchModuleImpl.NAME -> SearchModule(reactContext)
            SuggestsModuleImpl.NAME -> SuggestsModule(reactContext)
            YamapModuleImpl.NAME -> YamapModule(reactContext)

            else -> null
        }

    override fun getReactModuleInfoProvider() = ReactModuleInfoProvider {
        mapOf(
            TransportModuleImpl.NAME to ReactModuleInfo(
                name = TransportModuleImpl.NAME,
                className = TransportModuleImpl.NAME,
                canOverrideExistingModule = false,
                needsEagerInit = false,
                isCxxModule = false,
                isTurboModule = true
            ),
            SearchModuleImpl.NAME to ReactModuleInfo(
                name = SearchModuleImpl.NAME,
                className = SearchModuleImpl.NAME,
                canOverrideExistingModule = false,
                needsEagerInit = false,
                isCxxModule = false,
                isTurboModule = true
            ),
            SuggestsModuleImpl.NAME to ReactModuleInfo(
                name = SuggestsModuleImpl.NAME,
                className = SuggestsModuleImpl.NAME,
                canOverrideExistingModule = false,
                needsEagerInit = false,
                isCxxModule = false,
                isTurboModule = true
            ),
            YamapModuleImpl.NAME to ReactModuleInfo(
                name = YamapModuleImpl.NAME,
                className = YamapModuleImpl.NAME,
                canOverrideExistingModule = false,
                needsEagerInit = false,
                isCxxModule = false,
                isTurboModule = true
            ),
        )
    }

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
