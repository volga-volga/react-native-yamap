#ifdef RCT_NEW_ARCH_ENABLED

#import "NewArchUtils.h"

@implementation NewArchUtils;

+ (BOOL)polylinePointsEquals:(std::vector<PolylineViewPointsStruct>)points1 points2:(std::vector<PolylineViewPointsStruct>)points2 {
    if (points1.size() != points2.size()) {
        return false;
    }

    for (int i = 0; i < points1.size(); i++) {
        PolylineViewPointsStruct p1 = points1.at(i);
        PolylineViewPointsStruct p2 = points2.at(i);

        if ((p1.lat != p2.lat) || (p1.lon != p2.lon)){
            return false;
        }
    }

    return true;
}

+ (BOOL)polygonPointsEquals:(std::vector<PolygonViewPointsStruct>)points1 points2:(std::vector<PolygonViewPointsStruct>)points2 {
    if (points1.size() != points2.size()) {
        return false;
    }

    for (int i = 0; i < points1.size(); i++) {
        PolygonViewPointsStruct p1 = points1.at(i);
        PolygonViewPointsStruct p2 = points2.at(i);

        if ((p1.lat != p2.lat) || (p1.lon != p2.lon)){
            return false;
        }
    }

    return true;
}

+ (BOOL)polygonInnerRingsEquals:(std::vector<std::vector<PolygonViewInnerRingsStruct>>)innerRings1 innerRings2:(std::vector<std::vector<PolygonViewInnerRingsStruct>>)innerRings2 {
    if (innerRings1.size() != innerRings2.size()) {
        return false;
    }
    
    for (int i = 0; i < innerRings1.size(); i++) {
        std::vector<PolygonViewInnerRingsStruct> innerRing1 = innerRings1.at(i);
        std::vector<PolygonViewInnerRingsStruct> innerRing2 = innerRings2.at(i);
        
        if (innerRing1.size() != innerRing2.size()) {
            return false;
        }

        for (int j = 0; j < innerRing1.size(); j++) {
            PolygonViewInnerRingsStruct p1 = innerRing1.at(i);
            PolygonViewInnerRingsStruct p2 = innerRing1.at(i);

            if ((p1.lat != p2.lat) || (p1.lon != p2.lon)){
                return false;
            }
        }
    }
    
    return true;
}

+ (BOOL)yamapInitialRegionsEquals:(YamapViewInitialRegionStruct)initialRegion1 initialRegion2:(YamapViewInitialRegionStruct)initialRegion2 {
    if (
        initialRegion1.azimuth != initialRegion2.azimuth ||
        initialRegion1.lat != initialRegion2.lat ||
        initialRegion1.lon != initialRegion2.lon ||
        initialRegion1.tilt != initialRegion2.tilt ||
        initialRegion1.zoom != initialRegion2.zoom
    ) {
        return false;
    }
    
    return true;
}

+ (BOOL)yamapClusteredMarkersEquals:(std::vector<ClusteredYamapViewClusteredMarkersStruct>)markers1 markers2:(std::vector<ClusteredYamapViewClusteredMarkersStruct>)markers2 {
    if (markers1.size() != markers2.size()) {
        return false;
    }
    
    for (int i = 0; i < markers1.size(); i++) {
        ClusteredYamapViewClusteredMarkersStruct m1 = markers1.at(i);
        ClusteredYamapViewClusteredMarkersStruct m2 = markers2.at(i);
        
        if (m1.lat != m2.lat || m1.lon != m2.lon) {
            return false;
        }
    }

    return true;
}

@end

#endif
