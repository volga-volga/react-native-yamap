package ru.vvdev.yamap;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;
import com.yandex.runtime.image.ImageProvider;

import java.util.Arrays;
import java.util.List;

public class RNYamapPackage implements ReactPackage {
    private ImageProvider userLocation;
    private ImageProvider selectedMarker;
    private ImageProvider marker;

    public RNYamapPackage(ImageProvider userLocation, ImageProvider selectedMarker, ImageProvider marker) {
        this.userLocation = userLocation;
        this.selectedMarker = selectedMarker;
        this.marker = marker;
    }

    public RNYamapPackage() {
    }

    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        return Arrays.<NativeModule>asList(new RNYamapModule(reactContext));
    }

    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        return Arrays.<ViewManager>asList(
                new RNYamapManager(userLocation, selectedMarker, marker)
        );
    }
}
