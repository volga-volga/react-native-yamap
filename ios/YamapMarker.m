#import <React/RCTViewManager.h>
#import <MapKit/MapKit.h>
#import <math.h>
#import "YamapMarker.h"
#import "RNYamap.h"

#import "View/YamapMarkerView.h"
#import "View/RNYMView.h"

#import "Converter/RCTConvert+Yamap.m"

#ifndef MAX
#import <NSObjCRuntime.h>
#endif

@implementation YamapMarker

RCT_EXPORT_MODULE()

- (NSArray<NSString *> *)supportedEvents {
    return @[@"onPress"];
}

- (instancetype)init {
    self = [super init];
    return self;
}

- (UIView *_Nullable)view {
    return [[YamapMarkerView alloc] init];
}

+ (BOOL)requiresMainQueueSetup
 {
     return YES;
 }

// props
RCT_EXPORT_VIEW_PROPERTY(onPress, RCTBubblingEventBlock)

RCT_CUSTOM_VIEW_PROPERTY (point, YMKPoint, YamapMarkerView) {
    if (json != nil) {
        [view setPoint: [RCTConvert YMKPoint:json]];
    }
}

RCT_CUSTOM_VIEW_PROPERTY(scale, NSNumber, YamapMarkerView) {
    [view setScale: json];
}

RCT_CUSTOM_VIEW_PROPERTY(sectionType, NSString, YamapMarkerView) {
    [view setSectionType: json];
}

RCT_CUSTOM_VIEW_PROPERTY(anchor, NSDictionary, YamapMarkerView) {
    CGPoint point;
    if (json) {
        CGFloat x = [[json valueForKey:@"x"] doubleValue];
        CGFloat y = [[json valueForKey:@"y"] doubleValue];
        point = CGPointMake(x, y);
    } else {
        point = CGPointMake(0.5, 0.5);
    }
    [view setAnchor: [NSValue valueWithCGPoint:point]];
}

RCT_CUSTOM_VIEW_PROPERTY(zIndex, NSNumber, YamapMarkerView) {
    [view setZIndex: json];
}

RCT_CUSTOM_VIEW_PROPERTY(source, NSString, YamapMarkerView) {
    [view setSource: json];
}

@end
