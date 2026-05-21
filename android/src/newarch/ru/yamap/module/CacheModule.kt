package ru.yamap.module

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import ru.yamap.NativeCacheModuleSpec

class CacheModule(reactContext: ReactApplicationContext) : NativeCacheModuleSpec(reactContext) {
    private val implementation = CacheModuleImpl()

    override fun getName() = CacheModuleImpl.NAME

    override fun initManager(promise: Promise?) {
        implementation.initManager()
    }

    override fun searchRegions(promise: Promise?) {
        implementation.getRegionsList(promise!!)
    }

    override fun getRegionInfo(
        regionId: Double,
        promise: Promise?
    ) {
        implementation.getRegionState(regionId.toInt(), promise!!)
    }

    override fun startDownloadRegion(
        regionId: Double,
        promise: Promise?
    ) {
        implementation.startDownloadRegion(regionId.toInt(), promise!!)

    }

    override fun stopDownloadRegion(
        regionId: Double,
        promise: Promise?
    ) {
        TODO("Not yet implemented")
    }

    override fun pauseDownloadRegion(
        regionId: Double,
        promise: Promise?
    ) {
        TODO("Not yet implemented")
    }

    override fun dropRegion(regionId: Double, promise: Promise?) {
        implementation.dropRegion(regionId.toInt())
    }

}
