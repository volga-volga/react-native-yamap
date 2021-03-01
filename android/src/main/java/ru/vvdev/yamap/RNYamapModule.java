package ru.vvdev.yamap;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.transport.TransportFactory;
import com.yandex.runtime.Error;
import com.yandex.runtime.i18n.I18nManagerFactory;
import com.yandex.runtime.i18n.LocaleListener;

import java.util.HashMap;
import java.util.Locale;
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
    public void init(final String apiKey) {
        runOnUiThread(new Thread(new Runnable() {
            @Override
            public void run() {
                MapKitFactory.setApiKey(apiKey);
                MapKitFactory.initialize(getContext());
                MapKitFactory.getInstance().onStart();
            }
        }));
    }

    @ReactMethod
    public void setLocale(final String locale) {
        runOnUiThread(new Thread(new Runnable() {
            @Override
            public void run() {
                I18nManagerFactory.setLocale(locale);
            }
        }));
    }

    @ReactMethod
    public void getLocale(final Callback successCb, final Callback errorCb) {
        runOnUiThread(new Thread(new Runnable() {
            @Override
            public void run() {
                I18nManagerFactory.getLocale(new LocaleListener() {
                    @Override
                    public void onLocaleReceived(@androidx.annotation.Nullable String s) {
                        successCb.invoke(s);
                    }
                });
            }
        }));
    }

    @ReactMethod
    public void resetLocale() {
        runOnUiThread(new Thread(new Runnable() {
            @Override
            public void run() {
                MapKitFactory.setLocale(Locale.getDefault().getLanguage());
            }
        }));
    }

    private static void emitDeviceEvent(String eventName, @Nullable WritableMap eventData) {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, eventData);
    }
}
