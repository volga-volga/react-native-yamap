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
    return @[@"onRouteFound"];
}

- (instancetype)init {
    self = [super init];
    return self;
}

- (UIView *_Nullable)view {
    RNYMView* map = [[RNYMView alloc] init];
    return map;
}

-(void) setCenterForMap: (RNYMView*) map center:(NSDictionary*) _center {
    YMKPoint *center = [RCTConvert YMKPoint:_center];
    float zoom = [RCTConvert Zoom:_center];
    [map setCenter: center withZoom: zoom];
}

// props
RCT_EXPORT_VIEW_PROPERTY(onRouteFound, RCTBubblingEventBlock)

RCT_CUSTOM_VIEW_PROPERTY(userLocationIcon, NSString, RNYMView) {
    if (json && view) {
        [view setUserLocationIcon: json];
    }
}

// ref
RCT_EXPORT_METHOD(fitAllMarkers:(nonnull NSNumber*) reactTag) {
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *,UIView *> *viewRegistry) {
        RNYMView *view = viewRegistry[reactTag];
        if (!view || ![view isKindOfClass:[RNYMView class]]) {
            RCTLogError(@"Cannot find NativeView with tag #%@", reactTag);
            return;
        }
        [view fitAllMarkers];
    }];
}

RCT_EXPORT_METHOD(findRoutes:(nonnull NSNumber*) reactTag json:(NSDictionary*) json) {
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *,UIView *> *viewRegistry) {
        RNYMView *view = viewRegistry[reactTag];
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

RCT_EXPORT_METHOD(setCenter:(nonnull NSNumber*) reactTag json:(NSDictionary*) json) {
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *,UIView *> *viewRegistry) {
        RNYMView *view = viewRegistry[reactTag];
        if (!view || ![view isKindOfClass:[RNYMView class]]) {
            RCTLogError(@"Cannot find NativeView with tag #%@", reactTag);
            return;
        }
        [self setCenterForMap: view center:json];
    }];
}

@end
