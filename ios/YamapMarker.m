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

// props
RCT_EXPORT_VIEW_PROPERTY(onPress, RCTBubblingEventBlock)

RCT_CUSTOM_VIEW_PROPERTY (point, YMKPoint, YamapMarkerView) {
    [view setPoint: [RCTConvert YMKPoint:json]];
}

RCT_CUSTOM_VIEW_PROPERTY(scale, NSNumber, YamapMarkerView) {
    [view setScale: json];
}

RCT_CUSTOM_VIEW_PROPERTY(zIndex, NSNumber, YamapMarkerView) {
    [view setZIndex: json];
}

RCT_CUSTOM_VIEW_PROPERTY(source, NSString, YamapMarkerView) {
    [view setSource: json];
}

@end
