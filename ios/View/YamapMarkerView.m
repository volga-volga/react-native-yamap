#import <React/RCTComponent.h>
#import <React/UIView+React.h>

#import <MapKit/MapKit.h>
@import YandexMapsMobile;

#ifndef MAX
#import <NSObjCRuntime.h>
#endif

#import "YamapMarkerView.h"

#define ANDROID_COLOR(c) [UIColor colorWithRed:((c>>16)&0xFF)/255.0 green:((c>>8)&0xFF)/255.0 blue:((c)&0xFF)/255.0  alpha:((c>>24)&0xFF)/255.0]

#define UIColorFromRGB(rgbValue) [UIColor colorWithRed:((float)((rgbValue & 0xFF0000) >> 16))/255.0 green:((float)((rgbValue & 0xFF00) >> 8))/255.0 blue:((float)(rgbValue & 0xFF))/255.0 alpha:1.0]

#define YAMAP_FRAMES_PER_SECOND 25

@implementation YamapMarkerView {
    YMKPoint* _point;
    YMKPlacemarkMapObject* mapObject;
    NSNumber* zIndex;
    NSNumber* scale;
    NSNumber* rotated;
    NSString* source;
    NSString* lastSource;
    NSValue* anchor;
    NSNumber* visible;
    NSMutableArray<UIView*>* _reactSubviews;
    UIView* _childView;
}

- (instancetype)init {
    self = [super init];
    zIndex = [[NSNumber alloc] initWithInt:1];
    scale = [[NSNumber alloc] initWithInt:1];
    rotated = [[NSNumber alloc] initWithInt:0];
    visible = [[NSNumber alloc] initWithInt:1];
    _reactSubviews = [[NSMutableArray alloc] init];
    source = @"";
    lastSource = @"";

    return self;
}

- (void)updateMarker {
    if (mapObject != nil && mapObject.valid) {
        [mapObject setGeometry:_point];
        [mapObject setZIndex:[zIndex floatValue]];
        YMKIconStyle* iconStyle = [[YMKIconStyle alloc] init];
        [iconStyle setScale:scale];
        [iconStyle setVisible:visible];
        if (anchor) {
          [iconStyle setAnchor:anchor];
        }
        [iconStyle setRotationType:rotated];
		if ([_reactSubviews count] == 0) {
			if (![source isEqual:@""]) {
				if (![source isEqual:lastSource]) {
					[mapObject setIconWithImage:[self resolveUIImage:source]];
					lastSource = source;
				}
			}
		}
        [mapObject setIconStyleWithStyle:iconStyle];
	}
}


- (void)updateClusterMarker {
    if (mapObject != nil && mapObject.valid) {
        [mapObject setGeometry:_point];
        [mapObject setZIndex:[zIndex floatValue]];
        YMKIconStyle* iconStyle = [[YMKIconStyle alloc] init];
        [iconStyle setScale:scale];
        [iconStyle setVisible:visible];
        if (anchor) {
          [iconStyle setAnchor:anchor];
        }
        [iconStyle setRotationType:rotated];
        if ([_reactSubviews count] == 0) {
            if (![source isEqual:@""]) {
                    [mapObject setIconWithImage:[self resolveUIImage:source]];
                    lastSource = source;
            }
        }
        [mapObject setIconStyleWithStyle:iconStyle];
    }
}

- (void)setScale:(NSNumber*)_scale {
    scale = _scale;
    [self updateMarker];
}
- (void)setRotated:(NSNumber*) _rotated {
    rotated = _rotated;
    [self updateMarker];
}

- (void)setZIndex:(NSNumber*)_zIndex {
    zIndex = _zIndex;
    [self updateMarker];
}

- (void)setVisible:(NSNumber*)_visible {
    visible = _visible;
    [self updateMarker];
}

- (void)setPoint:(YMKPoint*)point {
    _point = point;
    [self updateMarker];
}

- (UIImage*)resolveUIImage:(NSString*)uri {
    UIImage *icon;

    if ([uri rangeOfString:@"http://"].location == NSNotFound && [uri rangeOfString:@"https://"].location == NSNotFound) {
        if ([uri rangeOfString:@"file://"].location != NSNotFound){
            NSString* file = [uri substringFromIndex:8];
            icon = [UIImage imageWithData:[NSData dataWithContentsOfURL:[NSURL fileURLWithPath:file]]];
        } else {
            icon = [UIImage imageNamed:uri];
        }
    } else {
        icon = [UIImage imageWithData:[NSData dataWithContentsOfURL:[NSURL URLWithString:uri]]];
    }

    return icon;
}

- (void)setSource:(NSString*)_source {
    source = _source;
    [self updateMarker];
}

- (void)setMapObject:(YMKPlacemarkMapObject *)_mapObject {
    mapObject = _mapObject;
    [mapObject addTapListenerWithTapListener:self];
    [self updateMarker];
}

- (void)setClusterMapObject:(YMKPlacemarkMapObject *)_mapObject {
    mapObject = _mapObject;
    [mapObject addTapListenerWithTapListener:self];
    [self updateClusterMarker];
}

// object tap listener
- (BOOL)onMapObjectTapWithMapObject:(nonnull YMKMapObject*)_mapObject point:(nonnull YMKPoint*)point {
    if (self.onPress)
        self.onPress(@{});

    return YES;
}

- (YMKPoint*)getPoint {
    return _point;
}

- (void)setAnchor:(NSValue*)_anchor {
    anchor = _anchor;
}

- (YMKPlacemarkMapObject*)getMapObject {
    return mapObject;
}

- (void)setChildView {
    if ([_reactSubviews count] > 0) {
        _childView = [_reactSubviews objectAtIndex:0];
        if (_childView != nil) {
            [_childView setOpaque:false];
            YRTViewProvider* v = [[YRTViewProvider alloc] initWithUIView:_childView];
            if (v != nil) {
                if (mapObject.isValid) {
                    [mapObject setViewWithView:v];
                    [self updateMarker];
                }
            }
        }
    } else {
        _childView = nil;
    }
}

- (void)didUpdateReactSubviews {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self setChildView];
    });
}

- (void)insertReactSubview:(UIView*)subview atIndex:(NSInteger)atIndex {
    [_reactSubviews insertObject:subview atIndex: atIndex];
    [super insertReactSubview:subview atIndex:atIndex];
}

- (void)removeReactSubview:(UIView*)subview {
    [_reactSubviews removeObject:subview];
    [super removeReactSubview: subview];
}

- (void)moveAnimationLoop:(NSInteger)frame withTotalFrames:(NSInteger)totalFrames withDeltaLat:(double)deltaLat withDeltaLon:(double)deltaLon {
    @try  {
        YMKPlacemarkMapObject *placemark = [self getMapObject];
        YMKPoint* p = placemark.geometry;
        placemark.geometry = [YMKPoint pointWithLatitude:p.latitude + deltaLat/totalFrames
                                            longitude:p.longitude + deltaLon/totalFrames];

        if (frame < totalFrames) {
            dispatch_after(dispatch_time(DISPATCH_TIME_NOW, NSEC_PER_SEC / YAMAP_FRAMES_PER_SECOND), dispatch_get_main_queue(), ^{
                [self moveAnimationLoop: frame+1 withTotalFrames:totalFrames withDeltaLat:deltaLat withDeltaLon:deltaLon];
            });
        }
    } @catch (NSException *exception) {
        NSLog(@"Reason: %@ ",exception.reason);
    }
}

- (void)rotateAnimationLoop:(NSInteger)frame withTotalFrames:(NSInteger)totalFrames withDelta:(double)delta {
    @try  {
        YMKPlacemarkMapObject *placemark = [self getMapObject];
        [placemark setDirection:placemark.direction+(delta / totalFrames)];

        if (frame < totalFrames) {
            dispatch_after(dispatch_time(DISPATCH_TIME_NOW, NSEC_PER_SEC / YAMAP_FRAMES_PER_SECOND), dispatch_get_main_queue(), ^{
                [self rotateAnimationLoop: frame+1 withTotalFrames:totalFrames withDelta:delta];
            });
        }
    } @catch (NSException *exception) {
        NSLog(@"Reason: %@ ",exception.reason);
    }
}

- (void)animatedMoveTo:(YMKPoint*)point withDuration:(float)duration {
    @try  {
        YMKPlacemarkMapObject* placemark = [self getMapObject];
        YMKPoint* p = placemark.geometry;
        double deltaLat = point.latitude - p.latitude;
        double deltaLon = point.longitude - p.longitude;
        [self moveAnimationLoop: 0 withTotalFrames:[@(duration / YAMAP_FRAMES_PER_SECOND) integerValue] withDeltaLat:deltaLat withDeltaLon:deltaLon];
    } @catch (NSException *exception) {
        NSLog(@"Reason: %@ ",exception.reason);
    }
}

- (void)animatedRotateTo:(float)angle withDuration:(float)duration {
    @try  {
        YMKPlacemarkMapObject* placemark = [self getMapObject];
        double delta = angle - placemark.direction;
        [self rotateAnimationLoop: 0 withTotalFrames:[@(duration / YAMAP_FRAMES_PER_SECOND) integerValue] withDelta:delta];
    } @catch (NSException *exception) {
        NSLog(@"Reason: %@ ",exception.reason);
    }
}

@synthesize reactTag;

@end
