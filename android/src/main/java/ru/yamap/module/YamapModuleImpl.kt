package ru.yamap.module

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.UiThreadUtil
import com.yandex.mapkit.MapKitFactory
import com.yandex.runtime.i18n.I18nManagerFactory

class YamapModuleImpl(reactContext: ReactApplicationContext) {

    private val reactApplicationContext = reactContext

    fun init(apiKey: String?, promise: Promise) {
        UiThreadUtil.runOnUiThread(Thread(Runnable {
            var apiKeyException: Throwable? = null
            try {
                // In case when android application reloads during development
                // MapKitFactory is already initialized
                // And setting api key leads to crash
                try {
                    MapKitFactory.setApiKey(apiKey!!)
                } catch (exception: Throwable) {
                    apiKeyException = exception
                }

                MapKitFactory.initialize(reactApplicationContext)
                MapKitFactory.getInstance().onStart()
                promise.resolve(null)
            } catch (exception: Exception) {
                if (apiKeyException != null) {
                    promise.reject(apiKeyException)
                    return@Runnable
                }
                promise.reject(exception)
            }
        }))
    }

    fun setLocale(locale: String?, promise: Promise) {
        UiThreadUtil.runOnUiThread(Thread {
            try {
                MapKitFactory.setLocale(locale)
                promise.resolve(null)
            } catch (e: Throwable) {
                promise.reject("101", e.message)
            }
        })
    }

    fun getLocale(promise: Promise) {
        UiThreadUtil.runOnUiThread(Thread {
            try {
                val locale = I18nManagerFactory.getLocale()
                promise.resolve(locale)
            } catch (e: Throwable) {
                promise.reject("102", e.message)
            }
        })
    }

    fun resetLocale(promise: Promise) {
        UiThreadUtil.runOnUiThread(Thread {
            try {
                I18nManagerFactory.setLocale(null)
                promise.resolve(null)
            } catch (e: Throwable) {
                promise.reject("103", e.message)
            }
        })
    }

    companion object {
        const val NAME = "RTNYamapModule"
    }
}
