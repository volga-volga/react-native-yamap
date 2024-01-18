//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.yandex.mapkit.mapview;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.Surface;
import android.widget.RelativeLayout;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.map.MapWindow;
import com.yandex.mapkit.map.internal.MapWindowBinding;
import com.yandex.runtime.view.GraphicsAPIType;
import com.yandex.runtime.view.PlatformGLTextureView;
import com.yandex.runtime.view.PlatformView;
import com.yandex.runtime.view.PlatformViewFactory;
import com.yandex.runtime.view.PlatformVulkanSurfaceView;

public class MapView extends RelativeLayout {
    private PlatformView platformView;
    private MapWindowBinding mapWindow;

    public MapView(Context context) {
        this(context, (AttributeSet)null, 0);
    }

    public MapView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (!this.isInEditMode()) {
            MapKitFactory.initialize(context);
            this.platformView = PlatformViewFactory.getPlatformView(context, PlatformViewFactory.convertAttributeSet(context, attrs));
            this.mapWindow = (MapWindowBinding)MapKitFactory.getInstance().createMapWindow(this.platformView);
            this.addView(this.platformView.getView(), new RelativeLayout.LayoutParams(-1, -1));
        }

    }

    public MapWindow getMapWindow() {
        return this.mapWindow;
    }

    /** @deprecated */
    public Map getMap() {
        return this.mapWindow.getMap();
    }

    public void setNoninteractive(boolean is) {
        this.platformView.setNoninteractive(is);
    }

    public void onStop() {
        this.platformView.pause();
        this.platformView.stop();
    }

    public void onStart() {
        this.platformView.start();
        this.platformView.resume();
    }

    public Bitmap getScreenshot() {
        if (this.platformView instanceof PlatformGLTextureView) {
            PlatformGLTextureView textureView = (PlatformGLTextureView)this.platformView;
            return textureView.getBitmap();
        } else {
            return null;
        }
    }

    public void onMemoryWarning() {
        this.platformView.onMemoryWarning();
    }

    public void addSurface(int id, Surface surface) {
        this.platformView.addSurface(id, surface);
    }

    public void removeSurface(int id) {
        this.platformView.removeSurface(id);
    }

    public GraphicsAPIType getGraphicsAPI() {
        return this.platformView instanceof PlatformVulkanSurfaceView ? GraphicsAPIType.VULKAN : GraphicsAPIType.OPEN_GL;
    }
}
