package ru.vvdev.yamap;

import android.Manifest;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.Arrays;
import java.util.List;

public class RNYamapPackage implements ReactPackage {
    public RNYamapPackage() {
    }

    @NonNull
    @Override
    public List<NativeModule> createNativeModules(@NonNull ReactApplicationContext reactContext) {
        return Arrays.<NativeModule>asList(new RNYamapModule(reactContext));
    }

    @NonNull
    public List<ViewManager> createViewManagers(@NonNull ReactApplicationContext reactContext) {
        return Arrays.<ViewManager>asList(
                new YamapViewManager(),
                new YamapPolygonManager(),
                new YamapPolylineManager(),
                new YamapCircleManager(),
                new YamapMarkerManager()
        );
    }
}
