#import <React/RCTComponent.h>
#import <React/UIView+React.h>

#import <MapKit/MapKit.h>
#import <YandexMapKit/YMKMapKitFactory.h>
#import <YandexMapKit/YMKMapView.h>
#import <YandexMapKit/YMKBoundingBox.h>
#import <YandexMapKit/YMKCameraPosition.h>
#import <YandexMapKit/YMKCircle.h>
#import <YandexMapKit/YMKPolyline.h>
#import <YandexMapKit/YMKPolylineMapObject.h>
#import <YandexMapKit/YMKMap.h>
#import <YandexMapKit/YMKMapObjectCollection.h>
#import <YandexMapKit/YMKGeoObjectCollection.h>
#import <YandexMapKit/YMKSubpolylineHelper.h>
#import <YandexMapKit/YMKPlacemarkMapObject.h>
#import <YandexMapKitTransport/YMKMasstransitSession.h>
#import <YandexMapKitTransport/YMKMasstransitRouter.h>
#import <YandexMapKitTransport/YMKPedestrianRouter.h>
#import <YandexMapKitTransport/YMKMasstransitRouteStop.h>
#import <YandexMapKitTransport/YMKMasstransitOptions.h>
#import <YandexMapKitTransport/YMKMasstransitSection.h>
#import <YandexMapKitTransport/YMKMasstransitSectionMetadata.h>
#import <YandexMapKitTransport/YMKMasstransitTransport.h>
#import <YandexMapKitTransport/YMKMasstransitWeight.h>
#import <YandexMapKitTransport/YMKTimeOptions.h>

#ifndef MAX
#import <NSObjCRuntime.h>
#endif

#import "YamapMarkerView.h"

#define ANDROID_COLOR(c) [UIColor colorWithRed:((c>>16)&0xFF)/255.0 green:((c>>8)&0xFF)/255.0 blue:((c)&0xFF)/255.0  alpha:((c>>24)&0xFF)/255.0]

#define UIColorFromRGB(rgbValue) [UIColor colorWithRed:((float)((rgbValue & 0xFF0000) >> 16))/255.0 green:((float)((rgbValue & 0xFF00) >> 8))/255.0 blue:((float)(rgbValue & 0xFF))/255.0 alpha:1.0]

@implementation YamapMarkerView {
    YMKPoint* _point;
    YMKPlacemarkMapObject* mapObject;
    NSNumber* zIndex;
    NSNumber* scale;
    NSString* source;
    NSString* lastSource;
    NSMutableArray<UIView*>* _reactSubviews;
    UIView* _childView;
}

- (instancetype)init {
    self = [super init];
    zIndex =  [[NSNumber alloc] initWithInt:1];
    scale =  [[NSNumber alloc] initWithInt:1];
    _reactSubviews = [[NSMutableArray alloc] init];
    source = @"";
    lastSource = @"";
    return self;
}

-(void) updateMarker {
    if (mapObject != nil) {
        [mapObject setGeometry:_point];
        [mapObject setZIndex:[zIndex floatValue]];
		if ([_reactSubviews count] == 0) {
			YMKIconStyle* iconStyle = [[YMKIconStyle alloc] init];
			[iconStyle setScale:scale];
			if (![source isEqual:@""]) {
				if (![source isEqual:lastSource]) {
					[mapObject setIconWithImage:[self resolveUIImage:source]];
					lastSource = source;
				}
			}
			[mapObject setIconStyleWithStyle:iconStyle];
		}
	}
}

-(void) setScale:(NSNumber*) _scale {
    scale = _scale;
    [self updateMarker];
}
-(void) setZIndex:(NSNumber*) _zIndex {
    zIndex = _zIndex;
    [self updateMarker];
}

-(void) setPoint:(YMKPoint*) point {
    _point = point;
    [self updateMarker];
}

-(UIImage*) resolveUIImage:(NSString*) uri {
    UIImage *icon;
    if ([uri rangeOfString:@"http://"].location == NSNotFound && [uri rangeOfString:@"https://"].location == NSNotFound) {
        if ([uri rangeOfString:@"file://"].location != NSNotFound){
            NSString *file = [uri substringFromIndex:8];
            icon = [UIImage imageWithData:[NSData dataWithContentsOfURL:[NSURL fileURLWithPath:file]]];
        } else {
            icon = [UIImage imageNamed:uri];
        }
    } else {
        icon = [UIImage imageWithData:[NSData dataWithContentsOfURL:[NSURL URLWithString:uri]]];
    }
    return icon;
}

-(void) setSource:(NSString*) _source {
    source = _source;
    [self updateMarker];
}
-(void) setMapObject:(YMKPlacemarkMapObject *)_mapObject {
    mapObject = _mapObject;
    [mapObject addTapListenerWithTapListener:self];
    [self updateMarker];
}

// object tap listener
- (BOOL)onMapObjectTapWithMapObject:(nonnull YMKMapObject *)_mapObject point:(nonnull YMKPoint *)point {
    if (self.onPress) self.onPress(@{});
    return YES;
}

-(YMKPoint*) getPoint {
    return _point;
}

-(YMKPlacemarkMapObject*) getMapObject {
    return mapObject;
}

-(void) setChildView {
    if ([_reactSubviews count] > 0) {
        _childView = [_reactSubviews objectAtIndex:0];
        if (_childView != nil) {
            [_childView setOpaque:false];
            YRTViewProvider* v = [[YRTViewProvider alloc] initWithUIView:_childView];
            if (v != nil) {
				if (mapObject.isValid) {
					 [mapObject setViewWithView:v];
				}
            }
        }
    } else {
        _childView = nil;
    }
}

-(void) didUpdateReactSubviews {
    // todo: Если вызывать сразу то frame имеет размеры 0. В идеале делать подписку на событие
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, 0.5 * NSEC_PER_SEC), dispatch_get_main_queue(), ^{
        [self setChildView];
    });
}
- (void)insertReactSubview:(UIView*) subview atIndex:(NSInteger)atIndex {
    [_reactSubviews insertObject:subview atIndex: atIndex];
    [super insertReactSubview:subview atIndex:atIndex];
}

- (void)removeReactSubview:(UIView*) subview {
    [_reactSubviews removeObject:subview];
    [super removeReactSubview: subview];
}

@synthesize reactTag;

@end
