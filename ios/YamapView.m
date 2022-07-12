#import <React/RCTViewManager.h>
#import <MapKit/MapKit.h>
#import <math.h>
#import "Converter/RCTConvert+Yamap.m"
#import "YamapView.h"
#import "RNYamap.h"
#import "View/RNYMView.h"

#ifndef MAX
#import <NSObjCRuntime.h>
#endif

@implementation YamapView

RCT_EXPORT_MODULE()

- (NSArray<NSString *> *)supportedEvents {
    return @[@"onRouteFound", @"onCameraPositionReceived", @"onVisibleRegionReceived", @"onCameraPositionChange", @"onMapPress", @"onMapLongPress"];
}

- (instancetype)init {
    self = [super init];
    return self;
}
+ (BOOL)requiresMainQueueSetup {
    return YES;
}

- (UIView *_Nullable)view {
    return [[RNYMView alloc] init];
}

- (void)setCenterForMap: (RNYMView*) map center:(NSDictionary*) _center zoom:(float) zoom azimuth:(float) azimuth tilt:(float) tilt duration:(float) duration animation:(int) animation {
    YMKPoint *center = [RCTConvert YMKPoint:_center];
    YMKCameraPosition* pos = [YMKCameraPosition cameraPositionWithTarget:center zoom:zoom azimuth:azimuth tilt:tilt];
    [map setCenter: pos withDuration: duration withAnimation: animation];
}

+ (BOOL)requiresMainQueueSetup
 {
     return YES;
 }

// props
RCT_EXPORT_VIEW_PROPERTY(onRouteFound, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onCameraPositionReceived, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onVisibleRegionReceived, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onCameraPositionChange, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onMapPress, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onMapLongPress, RCTBubblingEventBlock)

RCT_CUSTOM_VIEW_PROPERTY(userLocationAccuracyFillColor, NSNumber, RNYMView) {
    [view setUserLocationAccuracyFillColor: [RCTConvert UIColor:json]];
}


RCT_CUSTOM_VIEW_PROPERTY(userLocationAccuracyStrokeColor, NSNumber, RNYMView) {
    [view setUserLocationAccuracyStrokeColor: [RCTConvert UIColor:json]];
}

RCT_CUSTOM_VIEW_PROPERTY(withClusters, BOOL, RNYMView) {
    if (view) {
        [view setClusters: json ? [json boolValue] : NO];
    }
}

RCT_CUSTOM_VIEW_PROPERTY(clusterColor, NSNumber, RNYMView) {
    [view setClusterColor: [RCTConvert UIColor:json]];
}


RCT_CUSTOM_VIEW_PROPERTY(userLocationAccuracyStrokeWidth, NSNumber, RNYMView) {
    [view setUserLocationAccuracyStrokeWidth: [json floatValue]];
}

RCT_CUSTOM_VIEW_PROPERTY(userLocationIcon, NSString, RNYMView) {
    if (json && view) {
        [view setUserLocationIcon: json];
    }
}

RCT_CUSTOM_VIEW_PROPERTY(showUserPosition, BOOL, RNYMView) {
    if (view) {
        [view setListenUserLocation: json ? [json boolValue] : NO];
    }
}

RCT_CUSTOM_VIEW_PROPERTY(nightMode, BOOL, RNYMView) {
    if (view) {
        [view setNightMode: json ? [json boolValue]: NO];
    }
}

RCT_CUSTOM_VIEW_PROPERTY(mapStyle, NSString, RNYMView) {
	if (json && view) {
		[view.map.mapWindow.map setMapStyleWithStyle:json];
	}
}

RCT_CUSTOM_VIEW_PROPERTY(zoomGesturesEnabled, BOOL, RNYMView) {
    if (view) {
        view.map.mapWindow.map.zoomGesturesEnabled = json ? [json boolValue] : YES;
    }
}

RCT_CUSTOM_VIEW_PROPERTY(scrollGesturesEnabled, BOOL, RNYMView) {
    if (view) {
        view.map.mapWindow.map.scrollGesturesEnabled = json ? [json boolValue] : YES;
    }
}

RCT_CUSTOM_VIEW_PROPERTY(tiltGesturesEnabled, BOOL, RNYMView) {
    if (view) {
        view.map.mapWindow.map.tiltGesturesEnabled = json ? [json boolValue] : YES;
    }
}

RCT_CUSTOM_VIEW_PROPERTY(rotateGesturesEnabled, BOOL, RNYMView) {
    if (view) {
        view.map.mapWindow.map.rotateGesturesEnabled = json ? [json boolValue] : YES;
    }
}

RCT_CUSTOM_VIEW_PROPERTY(clusteredMap, BOOL, RNYMView) {
    if (view) {
        [view setClusterMode: json ? [json boolValue] : NO];
    }
}

RCT_CUSTOM_VIEW_PROPERTY(fastTapEnabled, BOOL, RNYMView) {
    if (view) {
        view.mapWindow.map.fastTapEnabled = json ? [json boolValue] : YES;
    }
}

RCT_CUSTOM_VIEW_PROPERTY(mapType, NSString, RNYMView) {
    if (view) {
        [view setMapType: json];
    }
}

// ref
RCT_EXPORT_METHOD(fitAllMarkers:(nonnull NSNumber*) reactTag) {
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *,UIView *> *viewRegistry) {
        RNYMView *view = (RNYMView*) viewRegistry[reactTag];
        if (!view || ![view isKindOfClass:[RNYMView class]]) {
            RCTLogError(@"Cannot find NativeView with tag #%@", reactTag);
            return;
        }
        [view fitAllMarkers];
    }];
}
RCT_EXPORT_METHOD(fitMarkers:(nonnull NSNumber*) reactTag json:(id) json) {
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *,UIView *> *viewRegistry) {
        RNYMView *view = (RNYMView*) viewRegistry[reactTag];
        if (!view || ![view isKindOfClass:[RNYMView class]]) {
            RCTLogError(@"Cannot find NativeView with tag #%@", reactTag);
            return;
        }
        NSArray<YMKPoint*>* points = [RCTConvert Points:json];
        [view fitMarkers: points];
    }];
}

RCT_EXPORT_METHOD(findRoutes:(nonnull NSNumber*) reactTag json:(NSDictionary*) json) {
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *,UIView *> *viewRegistry) {
        RNYMView *view = (RNYMView*) viewRegistry[reactTag];
        if (!view || ![view isKindOfClass:[RNYMView class]]) {
            RCTLogError(@"Cannot find NativeView with tag #%@", reactTag);
            return;
        }
        NSArray<YMKPoint*>* points = [RCTConvert Points:json[@"points"]];
        NSMutableArray<YMKRequestPoint*>* requestPoints = [[NSMutableArray alloc] init];
        for (int i = 0; i < [points count]; ++i) {
            YMKRequestPoint * requestPoint = [YMKRequestPoint requestPointWithPoint:[points objectAtIndex:i] type: YMKRequestPointTypeWaypoint pointContext:nil];
            [requestPoints addObject:requestPoint];
        }
        NSArray<NSString*>* vehicles = [RCTConvert Vehicles:json[@"vehicles"]];
        [view findRoutes: requestPoints vehicles: vehicles withId:json[@"id"]];
    }];
}

RCT_EXPORT_METHOD(setCenter:(nonnull NSNumber*) reactTag center:(NSDictionary*_Nonnull) center zoom:(NSNumber*_Nonnull) zoom azimuth:(NSNumber*_Nonnull) azimuth tilt:(NSNumber*_Nonnull) tilt duration: (NSNumber*_Nonnull) duration animation:(NSNumber*_Nonnull) animation) {
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *,UIView *> *viewRegistry) {
        RNYMView *view = (RNYMView*) viewRegistry[reactTag];
        if (!view || ![view isKindOfClass:[RNYMView class]]) {
            RCTLogError(@"Cannot find NativeView with tag #%@", reactTag);
            return;
        }
        [self setCenterForMap: view center:center zoom: [zoom floatValue] azimuth: [azimuth floatValue] tilt: [tilt floatValue] duration: [duration floatValue] animation: [animation intValue]];
    }];
}

RCT_EXPORT_METHOD(setZoom:(nonnull NSNumber*) reactTag zoom:(NSNumber*_Nonnull) zoom duration:(NSNumber*_Nonnull) duration animation:(NSNumber*_Nonnull) animation) {
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *,UIView *> *viewRegistry) {
        RNYMView *view = (RNYMView*) viewRegistry[reactTag];
        if (!view || ![view isKindOfClass:[RNYMView class]]) {
            RCTLogError(@"Cannot find NativeView with tag #%@", reactTag);
            return;
        }
        [view setZoom: [zoom floatValue] withDuration:[duration floatValue] withAnimation:[animation intValue]];
    }];
}

RCT_EXPORT_METHOD(getCameraPosition:(nonnull NSNumber*) reactTag _id:(NSString*_Nonnull) _id) {
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *,UIView *> *viewRegistry) {
        RNYMView *view = (RNYMView*) viewRegistry[reactTag];
        if (!view || ![view isKindOfClass:[RNYMView class]]) {
            RCTLogError(@"Cannot find NativeView with tag #%@", reactTag);
            return;
        }
        [view emitCameraPositionToJS:_id];
    }];
}

RCT_EXPORT_METHOD(getVisibleRegion:(nonnull NSNumber*) reactTag _id:(NSString*_Nonnull) _id) {
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *,UIView *> *viewRegistry) {
        RNYMView *view = (RNYMView*) viewRegistry[reactTag];
        if (!view || ![view isKindOfClass:[RNYMView class]]) {
            RCTLogError(@"Cannot find NativeView with tag #%@", reactTag);
            return;
        }
        [view emitVisibleRegionToJS:_id];
    }];
}

@end
