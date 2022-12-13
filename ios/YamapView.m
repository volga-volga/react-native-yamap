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

- (NSArray<NSString*>*)supportedEvents {
    return @[
        @"onRouteFound",
        @"onCameraPositionReceived",
        @"onVisibleRegionReceived",
        @"onCameraPositionChange",
        @"onCameraPositionChangeEnd",
        @"onMapPress",
        @"onMapLongPress",
        @"onMapLoaded",
        @"onWorldToScreenPointsReceived",
        @"onScreenToWorldPointsReceived"
    ];
}

- (instancetype)init {
    self = [super init];

    return self;
}

+ (BOOL)requiresMainQueueSetup {
    return YES;
}

- (UIView*_Nullable)view {
    RNYMView *map = [[RNYMView alloc] init];

    return map;
}

- (void)setCenterForMap:(RNYMView*)map center:(NSDictionary*)_center zoom:(float)zoom azimuth:(float)azimuth tilt:(float)tilt duration:(float)duration animation:(int)animation {
    YMKPoint *center = [RCTConvert YMKPoint:_center];
    YMKCameraPosition *pos = [YMKCameraPosition cameraPositionWithTarget:center zoom:zoom azimuth:azimuth tilt:tilt];
    [map setCenter:pos withDuration:duration withAnimation:animation];
}

// PROPS
RCT_EXPORT_VIEW_PROPERTY(onRouteFound, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onCameraPositionReceived, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onVisibleRegionReceived, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onCameraPositionChange, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onCameraPositionChangeEnd, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onMapPress, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onMapLongPress, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onMapLoaded, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onWorldToScreenPointsReceived, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onScreenToWorldPointsReceived, RCTBubblingEventBlock)

RCT_CUSTOM_VIEW_PROPERTY(userLocationAccuracyFillColor, NSNumber, RNYMView) {
    [view setUserLocationAccuracyFillColor:[RCTConvert UIColor:json]];
}

RCT_CUSTOM_VIEW_PROPERTY(userLocationAccuracyStrokeColor, NSNumber, RNYMView) {
    [view setUserLocationAccuracyStrokeColor:[RCTConvert UIColor:json]];
}

RCT_CUSTOM_VIEW_PROPERTY(userLocationAccuracyStrokeWidth, NSNumber, RNYMView) {
    [view setUserLocationAccuracyStrokeWidth:[json floatValue]];
}

RCT_CUSTOM_VIEW_PROPERTY(userLocationIcon, NSString, RNYMView) {
    if (json && view) {
        [view setUserLocationIcon:json];
    }
}

RCT_CUSTOM_VIEW_PROPERTY(userLocationIconScale, NSNumber, RNYMView) {
    if (json && view) {
        [view setUserLocationIconScale:json];
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
		[view.mapWindow.map setMapStyleWithStyle:json];
	}
}

RCT_CUSTOM_VIEW_PROPERTY(zoomGesturesEnabled, BOOL, RNYMView) {
    if (view) {
        view.mapWindow.map.zoomGesturesEnabled = json ? [json boolValue] : YES;
    }
}

RCT_CUSTOM_VIEW_PROPERTY(scrollGesturesEnabled, BOOL, RNYMView) {
    if (view) {
        view.mapWindow.map.scrollGesturesEnabled = json ? [json boolValue] : YES;
    }
}

RCT_CUSTOM_VIEW_PROPERTY(tiltGesturesEnabled, BOOL, RNYMView) {
    if (view) {
        view.mapWindow.map.tiltGesturesEnabled = json ? [json boolValue] : YES;
    }
}

RCT_CUSTOM_VIEW_PROPERTY(rotateGesturesEnabled, BOOL, RNYMView) {
    if (view) {
        view.mapWindow.map.rotateGesturesEnabled = json ? [json boolValue] : YES;
    }
}

RCT_CUSTOM_VIEW_PROPERTY(fastTapEnabled, BOOL, RNYMView) {
    if (view) {
        view.mapWindow.map.fastTapEnabled = json ? [json boolValue] : YES;
    }
}

RCT_CUSTOM_VIEW_PROPERTY(mapType, NSString, RNYMView) {
    if (view) {
        [view setMapType:json];
    }
}

RCT_CUSTOM_VIEW_PROPERTY(initialRegion, NSDictionary, RNYMView) {
    if (json && view) {
        [view setInitialRegion:json];
    }
}

RCT_CUSTOM_VIEW_PROPERTY(maxFps, NSNumber, RNYMView) {
    if (json && view) {
        [view setMaxFps:[json floatValue]];
    }
}

RCT_CUSTOM_VIEW_PROPERTY(interactive, BOOL, RNYMView) {
    if (json && view) {
        [view setInteractive:[json boolValue]];
    }
}

RCT_CUSTOM_VIEW_PROPERTY(logoPosition, BOOL, RNYMView) {
    if (json && view) {
        [view setLogoPosition:json];
    }
}

RCT_CUSTOM_VIEW_PROPERTY(logoPadding, BOOL, RNYMView) {
    if (json && view) {
        [view setLogoPadding:json];
    }
}

// REF
RCT_EXPORT_METHOD(fitAllMarkers:(nonnull NSNumber *)reactTag) {
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *, UIView *> *viewRegistry) {
        RNYMView *view = (RNYMView *)viewRegistry[reactTag];

        if (!view || ![view isKindOfClass:[RNYMView class]]) {
            RCTLogError(@"Cannot find NativeView with tag #%@", reactTag);
            return;
        }

        [view fitAllMarkers];
    }];
}

RCT_EXPORT_METHOD(fitMarkers:(nonnull NSNumber *)reactTag json:(id)json) {
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *, UIView*> *viewRegistry) {
        RNYMView *view = (RNYMView *)viewRegistry[reactTag];

        if (!view || ![view isKindOfClass:[RNYMView class]]) {
            RCTLogError(@"Cannot find NativeView with tag #%@", reactTag);
            return;
        }

        NSArray<YMKPoint *> *points = [RCTConvert Points:json];
        [view fitMarkers: points];
    }];
}

RCT_EXPORT_METHOD(findRoutes:(nonnull NSNumber *)reactTag json:(NSDictionary *)json) {
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *, UIView *> *viewRegistry) {
        RNYMView *view = (RNYMView *)viewRegistry[reactTag];

        if (!view || ![view isKindOfClass:[RNYMView class]]) {
            RCTLogError(@"Cannot find NativeView with tag #%@", reactTag);
            return;
        }

        NSArray<YMKPoint *> *points = [RCTConvert Points:json[@"points"]];
        NSMutableArray<YMKRequestPoint *> *requestPoints = [[NSMutableArray alloc] init];

        for (int i = 0; i < [points count]; ++i) {
            YMKRequestPoint *requestPoint = [YMKRequestPoint requestPointWithPoint:[points objectAtIndex:i] type:YMKRequestPointTypeWaypoint pointContext:nil];
            [requestPoints addObject:requestPoint];
        }

        NSArray<NSString *> *vehicles = [RCTConvert Vehicles:json[@"vehicles"]];
        [view findRoutes: requestPoints vehicles: vehicles withId:json[@"id"]];
    }];
}

RCT_EXPORT_METHOD(setCenter:(nonnull NSNumber *)reactTag center:(NSDictionary *_Nonnull)center zoom:(NSNumber *_Nonnull)zoom azimuth:(NSNumber *_Nonnull)azimuth tilt:(NSNumber *_Nonnull)tilt duration:(NSNumber *_Nonnull)duration animation:(NSNumber *_Nonnull)animation) {
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *, UIView *> *viewRegistry) {
        RNYMView *view = (RNYMView *)viewRegistry[reactTag];

        if (!view || ![view isKindOfClass:[RNYMView class]]) {
            RCTLogError(@"Cannot find NativeView with tag #%@", reactTag);
            return;
        }

        [self setCenterForMap:view center:center zoom:[zoom floatValue] azimuth:[azimuth floatValue] tilt:[tilt floatValue] duration:[duration floatValue] animation:[animation intValue]];
    }];
}

RCT_EXPORT_METHOD(setZoom:(nonnull NSNumber *)reactTag zoom:(NSNumber *_Nonnull)zoom duration:(NSNumber *_Nonnull)duration animation:(NSNumber *_Nonnull)animation) {
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *, UIView *> *viewRegistry) {
        RNYMView *view = (RNYMView *)viewRegistry[reactTag];

        if (!view || ![view isKindOfClass:[RNYMView class]]) {
            RCTLogError(@"Cannot find NativeView with tag #%@", reactTag);
            return;
        }

        [view setZoom:[zoom floatValue] withDuration:[duration floatValue] withAnimation:[animation intValue]];
    }];
}

RCT_EXPORT_METHOD(getCameraPosition:(nonnull NSNumber *)reactTag _id:(NSString *_Nonnull)_id) {
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *, UIView *> *viewRegistry) {
        RNYMView *view = (RNYMView *)viewRegistry[reactTag];

        if (!view || ![view isKindOfClass:[RNYMView class]]) {
            RCTLogError(@"Cannot find NativeView with tag #%@", reactTag);
            return;
        }

        [view emitCameraPositionToJS:_id];
    }];
}

RCT_EXPORT_METHOD(getVisibleRegion:(nonnull NSNumber *)reactTag _id:(NSString *_Nonnull)_id) {
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *, UIView *> *viewRegistry) {
        RNYMView *view = (RNYMView *)viewRegistry[reactTag];

        if (!view || ![view isKindOfClass:[RNYMView class]]) {
            RCTLogError(@"Cannot find NativeView with tag #%@", reactTag);
            return;
        }

        [view emitVisibleRegionToJS:_id];
    }];
}

RCT_EXPORT_METHOD(setTrafficVisible:(nonnull NSNumber *)reactTag traffic:(BOOL)traffic) {
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *, UIView *> *viewRegistry) {
        RNYMView *view = (RNYMView *)viewRegistry[reactTag];

        if (!view || ![view isKindOfClass:[RNYMView class]]) {
            RCTLogError(@"Cannot find NativeView with tag #%@", reactTag);
            return;
        }

        [view setTrafficVisible:traffic];
    }];
}

RCT_EXPORT_METHOD(getScreenPoints:(nonnull NSNumber *)reactTag json:(id)json _id:(NSString *_Nonnull)_id) {
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *, UIView *> *viewRegistry) {
        RNYMView *view = (RNYMView *)viewRegistry[reactTag];

        if (!view || ![view isKindOfClass:[RNYMView class]]) {
            RCTLogError(@"Cannot find NativeView with tag #%@", reactTag);
            return;
        }

        NSArray<YMKPoint *> *mapPoints = [RCTConvert Points:json];
        [view emitWorldToScreenPoint:mapPoints withId:_id];
    }];
}

RCT_EXPORT_METHOD(getWorldPoints:(nonnull NSNumber *)reactTag json:(id)json _id:(NSString *_Nonnull)_id) {
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *, UIView *> *viewRegistry) {
        RNYMView *view = (RNYMView *)viewRegistry[reactTag];

        if (!view || ![view isKindOfClass:[RNYMView class]]) {
            RCTLogError(@"Cannot find NativeView with tag #%@", reactTag);
            return;
        }

        NSArray<YMKScreenPoint *> *screenPoints = [RCTConvert ScreenPoints:json];
        [view emitScreenToWorldPoint:screenPoints withId:_id];
    }];
}

@end
