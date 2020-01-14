#import <React/RCTViewManager.h>
#import <MapKit/MapKit.h>
#import <math.h>
#import "YamapPolygon.h"
#import "RNYamap.h"

#import "View/YamapPolygonView.h"
#import "View/RNYMView.h"

#import "Converter/RCTConvert+Yamap.m"
#import "Models/RNMarker.h"

#ifndef MAX
#import <NSObjCRuntime.h>
#endif

@implementation YamapPolygon

RCT_EXPORT_MODULE()

- (NSArray<NSString *> *)supportedEvents {
    return @[@"onPress"];
}

- (instancetype)init {
    self = [super init];
    return self;
}

- (UIView *_Nullable)view {
    return [[YamapPolygonView alloc] init];
}

// props
RCT_EXPORT_VIEW_PROPERTY(onPress, RCTBubblingEventBlock)

RCT_CUSTOM_VIEW_PROPERTY (points, NSArray<YMKPoint>, YamapPolygonView) {
    [view setPolygonPoints: [RCTConvert Points:json]];
}

RCT_CUSTOM_VIEW_PROPERTY(fillColor, NSNumber, YamapPolygonView) {
    [view setFillColor: [RCTConvert UIColor:json]];
}

RCT_CUSTOM_VIEW_PROPERTY(strokeColor, NSNumber, YamapPolygonView) {
    [view setStrokeColor: [RCTConvert UIColor:json]];
}

RCT_CUSTOM_VIEW_PROPERTY(strokeWidth, NSNumber, YamapPolygonView) {
    [view setStrokeWidth: json];
}

RCT_CUSTOM_VIEW_PROPERTY(zIndex, NSNumber, YamapPolygonView) {
    [view setZIndex: json];
}

@end
