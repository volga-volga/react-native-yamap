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
    return @[@"onRouteFound", @"onCameraPositionReceived", @"onCameraPositionChange"];
}

- (instancetype)init {
    self = [super init];
    return self;
}

- (UIView *_Nullable)view {
    RNYMView* map = [[RNYMView alloc] init];
    return map;
}

-(void) setCenterForMap: (RNYMView*) map center:(NSDictionary*) _center zoom:(float) zoom azimuth:(float) azimuth tilt:(float) tilt duration:(float) duration animation:(int) animation {
    YMKPoint *center = [RCTConvert YMKPoint:_center];
    YMKCameraPosition* pos = [YMKCameraPosition cameraPositionWithTarget:center zoom:zoom azimuth:azimuth tilt:tilt];
    [map setCenter: pos withDuration: duration withAnimation: animation];
}

// props
RCT_EXPORT_VIEW_PROPERTY(onRouteFound, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onCameraPositionReceived, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onCameraPositionChange, RCTBubblingEventBlock)

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
		[view.mapWindow.map setMapStyleWithStyle:json];
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

RCT_EXPORT_METHOD(getCameraPosition:(nonnull NSNumber*) reactTag _id:(NSString*_Nonnull) _id ) {
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *,UIView *> *viewRegistry) {
        RNYMView *view = (RNYMView*) viewRegistry[reactTag];
        if (!view || ![view isKindOfClass:[RNYMView class]]) {
            RCTLogError(@"Cannot find NativeView with tag #%@", reactTag);
            return;
        }
        [view emitCameraPositionToJS:_id];
    }];
}

@end
