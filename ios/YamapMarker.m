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

- (NSArray<NSString*>*)supportedEvents {
    return @[@"onPress"];
}

- (instancetype)init {
    self = [super init];

    return self;
}

+ (BOOL)requiresMainQueueSetup {
    return YES;
}

- (UIView* _Nullable)view {
    return [[YamapMarkerView alloc] init];
}

// PROPS
RCT_EXPORT_VIEW_PROPERTY(onPress, RCTBubblingEventBlock)

RCT_CUSTOM_VIEW_PROPERTY (point, YMKPoint, YamapMarkerView) {
    if (json != nil) {
        [view setPoint: [RCTConvert YMKPoint:json]];
    }
}

RCT_CUSTOM_VIEW_PROPERTY(scale, NSNumber, YamapMarkerView) {
    [view setScale: json];
}

RCT_CUSTOM_VIEW_PROPERTY(rotated, NSNumber, YamapMarkerView) {
    [view setRotated: json];
}

RCT_CUSTOM_VIEW_PROPERTY(visible, NSNumber, YamapMarkerView) {
    [view setVisible: json];
}

RCT_CUSTOM_VIEW_PROPERTY(handled, BOOL, YamapMarkerView) {
    if (json == nil || [json boolValue]) {
        [view setHandled: YES];
    } else {
        [view setHandled: NO];
    }
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

// REF
RCT_EXPORT_METHOD(animatedMoveTo:(nonnull NSNumber*)reactTag json:(NSDictionary*)json duration:(NSNumber*_Nonnull)duration) {
    @try  {
        [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber*, UIView*> *viewRegistry) {
            YamapMarkerView* view = (YamapMarkerView*)viewRegistry[reactTag];

            if (!view || ![view isKindOfClass:[YamapMarkerView class]]) {
                RCTLogError(@"Cannot find NativeView with tag #%@", reactTag);
                return;
            }

            YMKPoint* point = [RCTConvert YMKPoint:json];
            [view animatedMoveTo:point withDuration:[duration floatValue]];
        }];
    } @catch (NSException *exception) {
        NSLog(@"Reason: %@ ",exception.reason);
    }
}

RCT_EXPORT_METHOD(animatedRotateTo:(nonnull NSNumber*)reactTag  angle:(NSNumber*_Nonnull)angle duration:(NSNumber*_Nonnull)duration) {
    @try  {
        [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber*, UIView*> *viewRegistry) {
            YamapMarkerView* view = (YamapMarkerView*)viewRegistry[reactTag];

            if (!view || ![view isKindOfClass:[YamapMarkerView class]]) {
                RCTLogError(@"Cannot find NativeView with tag #%@", reactTag);
                return;
            }

            [view animatedRotateTo:[angle floatValue] withDuration:[duration floatValue]];
        }];
    } @catch (NSException *exception) {
        NSLog(@"Reason: %@ ",exception.reason);
    }
}

@end
