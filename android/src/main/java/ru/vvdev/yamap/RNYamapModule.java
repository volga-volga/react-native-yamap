package ru.vvdev.yamap;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.transport.TransportFactory;
import com.yandex.runtime.i18n.I18nManagerFactory;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import static com.facebook.react.bridge.UiThreadUtil.runOnUiThread;

public class RNYamapModule extends ReactContextBaseJavaModule {
    private static final String REACT_CLASS = "yamap";

    private ReactApplicationContext getContext() {
        return reactContext;
    }

    private static ReactApplicationContext reactContext = null;

    RNYamapModule(ReactApplicationContext context) {
        super(context);
        reactContext = context;
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    public Map<String, Object> getConstants() {
        return new HashMap<>();
    }

    @ReactMethod
    public void init(final String apiKey, final Promise promise) {
        runOnUiThread(new Thread(new Runnable() {
            @Override
            public void run() {
                Throwable apiKeyException = null;
                try {
                    // In case when android application reloads during development
                    // MapKitFactory is already initialized
                    // And setting api key leads to crash
                    try {
                        MapKitFactory.setApiKey(apiKey);
                    } catch (Throwable exception) {
                        apiKeyException = exception;
                    }

                    MapKitFactory.initialize(reactContext);
                    TransportFactory.initialize(reactContext);
                    MapKitFactory.getInstance().onStart();
                    promise.resolve(null);
                } catch (Exception exception) {
                    if (apiKeyException != null) {
                        promise.reject(apiKeyException);
                        return;
                    }
                    promise.reject(exception);
                }
            }
        }));
    }

    @ReactMethod
    public void setLocale(final String locale, final Callback successCb, final Callback errorCb) {
        runOnUiThread(new Thread(new Runnable() {
            @Override
            public void run() {
                I18nManagerFactory.setLocale(locale);
                successCb.invoke();
            }
        }));
    }

    @ReactMethod
    public void getLocale(final Callback successCb, final Callback errorCb) {
        runOnUiThread(new Thread(new Runnable() {
            @Override
            public void run() {
                String locale = I18nManagerFactory.getLocale();
                successCb.invoke(locale);
            }
        }));
    }

    @ReactMethod
    public void resetLocale(final Callback successCb, final Callback errorCb) {
        runOnUiThread(new Thread(new Runnable() {
            @Override
            public void run() {
                I18nManagerFactory.setLocale(null);
                successCb.invoke();
            }
        }));
    }

    private static void emitDeviceEvent(String eventName, @Nullable WritableMap eventData) {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, eventData);
    }
}
