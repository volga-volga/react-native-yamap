package ru.vvdev.yamap;

import androidx.annotation.NonNull;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;
import ru.vvdev.yamap.suggest.RNYandexSuggestModule;

import java.util.Arrays;
import java.util.List;

public class RNYamapPackage implements ReactPackage {
    public RNYamapPackage() {
    }

    @NonNull
    @Override
    public List<NativeModule> createNativeModules(@NonNull ReactApplicationContext reactContext) {
        return Arrays.<NativeModule>asList(new RNYamapModule(reactContext), new RNYandexSuggestModule(reactContext));
    }

    @NonNull
    public List<ViewManager> createViewManagers(@NonNull ReactApplicationContext reactContext) {
        return Arrays.<ViewManager>asList(
                new YamapViewManager(),
                new ClusteredYamapViewManager(),
                new YamapPolygonManager(),
                new YamapPolylineManager(),
                new YamapMarkerManager(),
                new YamapCircleManager()
        );
    }
}
