#ifndef RCT_NEW_ARCH_ENABLED

#import <React/RCTViewManager.h>

#import "View/CircleView.h"
#import "Util/RCTConvert+Yamap.mm"

@interface CircleViewManager : RCTViewManager
@end

@implementation CircleViewManager

RCT_EXPORT_MODULE(CircleView)

- (NSArray<NSString*>*)supportedEvents {
    return @[@"onPress"];
}

- (UIView *)view {
    return [[CircleView alloc] init];
}

// PROPS
RCT_EXPORT_VIEW_PROPERTY(onPress, RCTBubblingEventBlock)

RCT_EXPORT_VIEW_PROPERTY(radius, float)
RCT_EXPORT_VIEW_PROPERTY(fillColor, NSNumber)
RCT_EXPORT_VIEW_PROPERTY(strokeColor, NSNumber)
RCT_EXPORT_VIEW_PROPERTY(strokeWidth, NSNumber)
RCT_EXPORT_VIEW_PROPERTY(zI, NSNumber)
RCT_EXPORT_VIEW_PROPERTY(handled, BOOL)

RCT_CUSTOM_VIEW_PROPERTY(center, YMKPoint, CircleView) {
    [view setCircleCenter: [RCTConvert YMKPoint:json]];
}

@end

#endif
