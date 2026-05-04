#ifndef RCT_NEW_ARCH_ENABLED

#import <React/RCTUIManager.h>

#import "View/MarkerView.h"
#import "Util/RCTConvert+Yamap.mm"

@interface MarkerViewManager : RCTViewManager
@end

@implementation MarkerViewManager

RCT_EXPORT_MODULE(MarkerView)

- (NSArray<NSString*>*)supportedEvents {
    return @[@"onPress"];
}

- (UIView *)view {
    return [[MarkerView alloc] init];
}

// PROPS
RCT_EXPORT_VIEW_PROPERTY(onPress, RCTBubblingEventBlock)

RCT_EXPORT_VIEW_PROPERTY(point, YMKPoint)
RCT_EXPORT_VIEW_PROPERTY(scale, NSNumber)
RCT_EXPORT_VIEW_PROPERTY(rotated, BOOL)
RCT_EXPORT_VIEW_PROPERTY(visible, BOOL)
RCT_EXPORT_VIEW_PROPERTY(handled, BOOL)
RCT_CUSTOM_VIEW_PROPERTY(anchor, NSDictionary, MarkerView) {
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
RCT_EXPORT_VIEW_PROPERTY(zI, NSNumber)
RCT_EXPORT_VIEW_PROPERTY(source, NSString)

// REF
RCT_EXPORT_METHOD(animatedMoveTo:(nonnull NSNumber*)reactTag argsArr:(NSArray*)argsArr) {
    @try  {
        [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber*, UIView*> *viewRegistry) {
            MarkerView* view = (MarkerView*)viewRegistry[reactTag];

            NSDictionary* args = argsArr.firstObject;
            YMKPoint* point = [RCTConvert YMKPoint:args[@"coords"]];
            [view animatedMoveTo:point withDuration:[args[@"duration"] floatValue]];
        }];
    } @catch (NSException *exception) {
        NSLog(@"Marker animatedMoveTo error: %@ ",exception.reason);
    }
}

RCT_EXPORT_METHOD(animatedRotateTo:(nonnull NSNumber*)reactTag argsArr:(NSArray*)argsArr) {
    @try  {
        [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber*, UIView*> *viewRegistry) {
            MarkerView* view = (MarkerView*)viewRegistry[reactTag];

            NSDictionary* args = argsArr.firstObject;
            [view animatedRotateTo:[args[@"angle"] floatValue] withDuration:[args[@"duration"] floatValue]];
        }];
    } @catch (NSException *exception) {
        NSLog(@"Marker animatedRotateTo error: %@ ",exception.reason);
    }
}

@end

#endif
