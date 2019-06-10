package ru.vvdev.yamap;

import android.support.annotation.Nullable;
import android.util.Log;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.transport.TransportFactory;

import java.util.HashMap;
import java.util.Map;

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
                MapKitFactory.initialize(reactContext);
                MapKitFactory.getInstance().onStart();
                TransportFactory.initialize(reactContext);
            }
        }));
    }

    private static void emitDeviceEvent(String eventName, @Nullable WritableMap eventData) {
        Log.e("YUI", eventName);
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, eventData);
    }
}
