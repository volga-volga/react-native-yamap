package ru.vvdev.yamap.view;

import android.content.Context;
import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import ru.vvdev.yamap.models.ReactMapObject;
import ru.vvdev.yamap.utils.RouteManager;

public class YamapView extends com.yandex.mapkit.mapview.MapView {
    // default colors for known vehicles
    // "underground" actually get color considering with his own branch"s color
    private final static Map<String, String> DEFAULT_VEHICLE_COLORS = new HashMap<String, String>() {{
        put("bus", "#59ACFF");
        put("railway", "#F8634F");
        put("tramway", "#C86DD7");
        put("suburban", "#3023AE");
        put("underground", "#BDCCDC");
        put("trolleybus", "#55CfDC");
        put("walk", "#333333");
    }};
    private String userLocationIcon = "";
    private Bitmap userLocationBitmap = null;

    private RouteManager routeMng = new RouteManager();
//    private MasstransitRouter masstransitRouter = TransportFactory.getInstance().createMasstransitRouter();
//    private DrivingRouter drivingRouter;
//    private PedestrianRouter pedestrianRouter = TransportFactory.getInstance().createPedestrianRouter();
//    private UserLocationLayer userLocationLayer = null;
    private int userLocationAccuracyFillColor = 0;
    private int userLocationAccuracyStrokeColor = 0;
    private float userLocationAccuracyStrokeWidth = 0.f;
    private List<ReactMapObject> childs = new ArrayList<>();

    // location

    public YamapView(Context context) {
        super(context);
    }
}
