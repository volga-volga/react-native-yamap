#ifndef NewArchUtils_h
#define NewArchUtils_h

#ifdef RCT_NEW_ARCH_ENABLED

#import <react/renderer/components/RNYamapPlusSpec/Props.h>

using namespace facebook::react;

@interface NewArchUtils : NSObject

+ (BOOL)polylinePointsEquals:(std::vector<PolylineViewPointsStruct>)points1 points2:(std::vector<PolylineViewPointsStruct>)points2;
+ (BOOL)polygonPointsEquals:(std::vector<PolygonViewPointsStruct>)points1 points2:(std::vector<PolygonViewPointsStruct>)points2;
+ (BOOL)polygonInnerRingsEquals:(std::vector<std::vector<PolygonViewInnerRingsStruct>>)innerRings1 innerRings2:(std::vector<std::vector<PolygonViewInnerRingsStruct>>)innerRings2;
+ (BOOL)yamapInitialRegionsEquals:(YamapViewInitialRegionStruct)initialRegion1 initialRegion2:(YamapViewInitialRegionStruct)initialRegion2;
+ (BOOL)yamapClusteredMarkersEquals:(std::vector<ClusteredYamapViewClusteredMarkersStruct>)markers1 markers2:(std::vector<ClusteredYamapViewClusteredMarkersStruct>)markers2;

@end

#endif
#endif
