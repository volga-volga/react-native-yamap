#ifndef RCT_NEW_ARCH_ENABLED

#import <React/RCTUIManager.h>

#import "View/ClusteredYamapView.h"
#import "Util/RCTConvert+Yamap.mm"

#import <YandexMapsMobile/YMKMap.h>

@interface ClusteredYamapViewManager : RCTViewManager
@end

@implementation ClusteredYamapViewManager

RCT_EXPORT_MODULE(ClusteredYamapView)

- (NSArray<NSString *> *)supportedEvents {
    return @[
        @"onRouteFound",
        @"onCameraPositionReceived",
        @"onVisibleRegionReceived",
        @"onCameraPositionChange",
        @"onMapPress",
        @"onMapLongPress",
        @"onCameraPositionChangeEnd",
        @"onMapLoaded",
        @"onWorldToScreenPointsReceived",
        @"onScreenToWorldPointsReceived"
    ];
}

- (UIView *)view {
    return [[ClusteredYamapView alloc] init];
}

- (void)setCenterForMap:(ClusteredYamapView*)map center:(NSDictionary*)_center zoom:(float)zoom azimuth:(float)azimuth tilt:(float)tilt duration:(float)duration animation:(int)animation {
    YMKPoint *center = [RCTConvert YMKPoint:_center];
    YMKCameraPosition *pos = [YMKCameraPosition cameraPositionWithTarget:center zoom:zoom azimuth:azimuth tilt:tilt];
    [map setCenter:pos withDuration:duration withAnimation:animation];
}

// props
RCT_EXPORT_VIEW_PROPERTY(onCameraPositionReceived, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onVisibleRegionReceived, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onCameraPositionChange, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onMapPress, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onMapLongPress, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onCameraPositionChangeEnd, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onMapLoaded, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onWorldToScreenPointsReceived, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onScreenToWorldPointsReceived, RCTBubblingEventBlock)

RCT_EXPORT_VIEW_PROPERTY(initialRegion, NSDictionary)
RCT_EXPORT_VIEW_PROPERTY(userLocationAccuracyFillColor, NSNumber)
RCT_EXPORT_VIEW_PROPERTY(clusterColor, NSNumber)
RCT_EXPORT_VIEW_PROPERTY(clusteredMarkers, NSArray<YMKPoint*>*)
RCT_EXPORT_VIEW_PROPERTY(userLocationAccuracyStrokeColor, NSNumber)
RCT_EXPORT_VIEW_PROPERTY(userLocationAccuracyStrokeWidth, float)
RCT_EXPORT_VIEW_PROPERTY(userLocationIcon, NSString)
RCT_EXPORT_VIEW_PROPERTY(userLocationIconScale, NSNumber)
RCT_EXPORT_VIEW_PROPERTY(showUserPosition, BOOL)
RCT_EXPORT_VIEW_PROPERTY(followUser, BOOL)
RCT_EXPORT_VIEW_PROPERTY(nightMode, BOOL)
RCT_EXPORT_VIEW_PROPERTY(mapStyle, NSString)
RCT_EXPORT_VIEW_PROPERTY(mapType, NSString)
RCT_EXPORT_VIEW_PROPERTY(interactiveDisabled, BOOL)
RCT_EXPORT_VIEW_PROPERTY(logoPosition, NSDictionary)
RCT_EXPORT_VIEW_PROPERTY(logoPadding, NSDictionary)
RCT_EXPORT_VIEW_PROPERTY(zoomGesturesDisabled, BOOL)
RCT_EXPORT_VIEW_PROPERTY(scrollGesturesDisabled, BOOL)
RCT_EXPORT_VIEW_PROPERTY(tiltGesturesDisabled, BOOL)
RCT_EXPORT_VIEW_PROPERTY(rotateGesturesDisabled, BOOL)
RCT_EXPORT_VIEW_PROPERTY(fastTapDisabled, BOOL)
RCT_CUSTOM_VIEW_PROPERTY(clusterIcon, NSString, ClusteredYamapView) {
    [view setClusterIcon:json points:nil];
}
RCT_EXPORT_VIEW_PROPERTY(clusterSize, NSDictionary)
RCT_EXPORT_VIEW_PROPERTY(clusterTextColor, NSNumber)
RCT_EXPORT_VIEW_PROPERTY(clusterTextSize, NSNumber)
RCT_EXPORT_VIEW_PROPERTY(clusterTextXOffset, NSNumber)
RCT_EXPORT_VIEW_PROPERTY(clusterTextYOffset, NSNumber)

// ref
RCT_EXPORT_METHOD(fitAllMarkers:(nonnull NSNumber*) reactTag argsArr:(NSArray*)argsArr) {
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *,UIView *> *viewRegistry) {
        ClusteredYamapView *view = (ClusteredYamapView*) viewRegistry[reactTag];

        NSDictionary* args = argsArr.firstObject;
        NSNumber *duration = args[@"duration"];
        NSNumber *animation = args[@"animation"];
        [view fitAllMarkers:[duration floatValue] animation:[animation intValue]];
    }];
}

RCT_EXPORT_METHOD(fitMarkers:(nonnull NSNumber *)reactTag argsArr:(NSArray*)argsArr) {
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *, UIView*> *viewRegistry) {
        YamapView *view = (YamapView *)viewRegistry[reactTag];

        NSDictionary* args = argsArr.firstObject;
        NSArray<YMKPoint *> *points = [RCTConvert YMKPointArray:args[@"points"]];
        NSNumber *duration = args[@"duration"];
        NSNumber *animation = args[@"animation"];
        [view fitMarkers: points duration:[duration floatValue] animation:[animation intValue]];
    }];
}

RCT_EXPORT_METHOD(setCenter:(nonnull NSNumber*) reactTag argsArr:(NSArray*)argsArr) {
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *,UIView *> *viewRegistry) {
        ClusteredYamapView *view = (ClusteredYamapView*) viewRegistry[reactTag];

        NSDictionary* args = argsArr.firstObject;
        [self setCenterForMap: view center:args[@"center"] zoom:[args[@"zoom"] floatValue] azimuth:[args[@"azimuth"] floatValue] tilt: [args[@"tilt"] floatValue] duration: [args[@"duration"] floatValue] animation: [args[@"animation"] intValue]];
    }];
}

RCT_EXPORT_METHOD(setZoom:(nonnull NSNumber*) reactTag argsArr:(NSArray*)argsArr) {
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *,UIView *> *viewRegistry) {
        ClusteredYamapView *view = (ClusteredYamapView*) viewRegistry[reactTag];

        NSDictionary* args = argsArr.firstObject;
        [view setZoom:[args[@"zoom"] floatValue] withDuration:[args[@"duration"] floatValue] withAnimation:[args[@"animation"] intValue]];
    }];
}

RCT_EXPORT_METHOD(getCameraPosition:(nonnull NSNumber*) reactTag argsArr:(NSArray*)argsArr) {
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *,UIView *> *viewRegistry) {
        ClusteredYamapView *view = (ClusteredYamapView*) viewRegistry[reactTag];

        NSDictionary* args = argsArr.firstObject;
        [view emitCameraPositionToJS:args[@"id"]];
    }];
}

RCT_EXPORT_METHOD(getVisibleRegion:(nonnull NSNumber*) reactTag argsArr:(NSArray*)argsArr) {
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *,UIView *> *viewRegistry) {
        ClusteredYamapView *view = (ClusteredYamapView*) viewRegistry[reactTag];

        NSDictionary* args = argsArr.firstObject;
        [view emitVisibleRegionToJS:args[@"id"]];
    }];
}

RCT_EXPORT_METHOD(getScreenPoints:(nonnull NSNumber *)reactTag argsArr:(NSArray*)argsArr) {
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *, UIView *> *viewRegistry) {
        YamapView *view = (YamapView *)viewRegistry[reactTag];

        NSDictionary* args = argsArr.firstObject;
        NSArray<YMKPoint *> *mapPoints = [RCTConvert YMKPointArray:args[@"points"]];
        [view emitWorldToScreenPoint:mapPoints withId:args[@"id"]];
    }];
}

RCT_EXPORT_METHOD(getWorldPoints:(nonnull NSNumber *)reactTag argsArr:(NSArray*)argsArr) {
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *, UIView *> *viewRegistry) {
        YamapView *view = (YamapView *)viewRegistry[reactTag];

        NSDictionary* args = argsArr.firstObject;
        NSArray<YMKScreenPoint *> *screenPoints = [RCTConvert ScreenPoints:args[@"points"]];
        [view emitScreenToWorldPoint:screenPoints withId:args[@"id"]];
    }];
}

RCT_EXPORT_METHOD(setTrafficVisible:(nonnull NSNumber *)reactTag argsArr:(NSArray*)argsArr) {
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *, UIView *> *viewRegistry) {
        YamapView *view = (YamapView *)viewRegistry[reactTag];

        NSDictionary* args = argsArr.firstObject;
        [view setTrafficVisible:args[@"isVisible"]];
    }];
}

@end

#endif
