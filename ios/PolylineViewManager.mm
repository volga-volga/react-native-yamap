#ifndef RCT_NEW_ARCH_ENABLED

#import <React/RCTViewManager.h>

#import "View/PolylineView.h"

@interface PolylineViewManager : RCTViewManager
@end

@implementation PolylineViewManager

RCT_EXPORT_MODULE(PolylineView)

- (UIView *)view {
    return [[PolylineView alloc] init];
}

// PROPS
RCT_EXPORT_VIEW_PROPERTY(onPress, RCTBubblingEventBlock)

RCT_EXPORT_VIEW_PROPERTY(points, NSArray<YMKPoint>)
RCT_EXPORT_VIEW_PROPERTY(outlineColor, NSNumber)
RCT_EXPORT_VIEW_PROPERTY(strokeColor, NSNumber)
RCT_EXPORT_VIEW_PROPERTY(strokeWidth, NSNumber)
RCT_EXPORT_VIEW_PROPERTY(dashLength, NSNumber)
RCT_EXPORT_VIEW_PROPERTY(gapLength, NSNumber)
RCT_EXPORT_VIEW_PROPERTY(dashOffset, NSNumber)
RCT_EXPORT_VIEW_PROPERTY(outlineWidth, NSNumber)
RCT_EXPORT_VIEW_PROPERTY(zI, NSNumber)
RCT_EXPORT_VIEW_PROPERTY(handled, BOOL)

@end

#endif
