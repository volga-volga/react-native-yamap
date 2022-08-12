#import <React/RCTComponent.h>

#import <MapKit/MapKit.h>
@import YandexMapsMobile;

#ifndef MAX
#import <NSObjCRuntime.h>
#endif

#import "YamapCircleView.h"


#define ANDROID_COLOR(c) [UIColor colorWithRed:((c>>16)&0xFF)/255.0 green:((c>>8)&0xFF)/255.0 blue:((c)&0xFF)/255.0  alpha:((c>>24)&0xFF)/255.0]

#define UIColorFromRGB(rgbValue) [UIColor colorWithRed:((float)((rgbValue & 0xFF0000) >> 16))/255.0 green:((float)((rgbValue & 0xFF00) >> 8))/255.0 blue:((float)(rgbValue & 0xFF))/255.0 alpha:1.0]

@implementation YamapCircleView {
    YMKPoint* center;
    float radius;
    YMKCircleMapObject* mapObject;
    YMKCircle* circle;
    UIColor* fillColor;
    UIColor* strokeColor;
    NSNumber* strokeWidth;
    NSNumber* zIndex;
}

- (instancetype)init {
    self = [super init];
    fillColor = UIColor.blackColor;
    strokeColor = UIColor.blackColor;
    zIndex = [[NSNumber alloc] initWithInt:1];
    strokeWidth = [[NSNumber alloc] initWithInt:1];
    center = [YMKPoint pointWithLatitude:0 longitude:0];
    radius = 0.f;
    circle = [YMKCircle circleWithCenter:center radius:radius];

    return self;
}

- (void)updateCircle {
    if (mapObject != nil) {
        [mapObject setGeometry:circle];
        [mapObject setZIndex:[zIndex floatValue]];
        [mapObject setFillColor:fillColor];
        [mapObject setStrokeColor:strokeColor];
        [mapObject setStrokeWidth:[strokeWidth floatValue]];
    }
}

- (void)setFillColor:(UIColor*)color {
    fillColor = color;
    [self updateCircle];
}

- (void)setStrokeColor:(UIColor*)color {
    strokeColor = color;
    [self updateCircle];
}

- (void)setStrokeWidth:(NSNumber*)width {
    strokeWidth = width;
    [self updateCircle];
}

- (void)setZIndex:(NSNumber*)_zIndex {
    zIndex = _zIndex;
    [self updateCircle];
}

- (void)updateGeometry {
    if (center) {
        circle = [YMKCircle circleWithCenter:center radius:radius];
    }
}

- (void)setCircleCenter:(YMKPoint*)point {
    center = point;
    [self updateGeometry];
    [self updateCircle];
}

- (void)setRadius:(float)_radius {
    radius = _radius;
    [self updateGeometry];
    [self updateCircle];
}

- (void)setMapObject:(YMKCircleMapObject*)_mapObject {
    mapObject = _mapObject;
    [mapObject addTapListenerWithTapListener:self];
    [self updateCircle];
}

- (YMKCircle*)getCircle {
    return circle;
}

- (BOOL)onMapObjectTapWithMapObject:(nonnull YMKMapObject*)mapObject point:(nonnull YMKPoint*)point {
    if (self.onPress)
        self.onPress(@{});

    return YES;
}

- (YMKCircleMapObject*)getMapObject {
    return mapObject;
}

@end
