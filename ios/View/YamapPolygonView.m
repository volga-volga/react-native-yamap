#import <React/RCTComponent.h>

#import <MapKit/MapKit.h>
@import YandexMapsMobile;

#ifndef MAX
#import <NSObjCRuntime.h>
#endif

#import "YamapPolygonView.h"


#define ANDROID_COLOR(c) [UIColor colorWithRed:((c>>16)&0xFF)/255.0 green:((c>>8)&0xFF)/255.0 blue:((c)&0xFF)/255.0  alpha:((c>>24)&0xFF)/255.0]

#define UIColorFromRGB(rgbValue) [UIColor colorWithRed:((float)((rgbValue & 0xFF0000) >> 16))/255.0 green:((float)((rgbValue & 0xFF00) >> 8))/255.0 blue:((float)(rgbValue & 0xFF))/255.0 alpha:1.0]

@implementation YamapPolygonView {
    NSMutableArray<YMKPoint*>* _points;
    NSArray<NSArray<YMKPoint*>*>* innerRings;
    YMKPolygonMapObject* mapObject;
    YMKPolygon* polygon;
    UIColor* fillColor;
    UIColor* strokeColor;
    NSNumber* strokeWidth;
    NSNumber* zIndex;
    BOOL handled;
}

- (instancetype)init {
    self = [super init];
    fillColor = UIColor.blackColor;
    strokeColor = UIColor.blackColor;
    zIndex = [[NSNumber alloc] initWithInt:1];
    handled = YES;
    strokeWidth = [[NSNumber alloc] initWithInt:1];
    _points = [[NSMutableArray alloc] init];
    innerRings = [[NSMutableArray alloc] init];
    polygon = [YMKPolygon polygonWithOuterRing:[YMKLinearRing linearRingWithPoints:@[]] innerRings:@[]];

    return self;
}

- (void)updatePolygon {
    if (mapObject != nil) {
        [mapObject setGeometry:polygon];
        [mapObject setZIndex:[zIndex floatValue]];
        [mapObject setFillColor:fillColor];
        [mapObject setStrokeColor:strokeColor];
        [mapObject setStrokeWidth:[strokeWidth floatValue]];
    }
}

- (void)setFillColor:(UIColor*)color {
    fillColor = color;
    [self updatePolygon];
}

- (void)setStrokeColor:(UIColor*)color {
    strokeColor = color;
    [self updatePolygon];
}

- (void)setStrokeWidth:(NSNumber*)width {
    strokeWidth = width;
    [self updatePolygon];
}

- (void)setZIndex:(NSNumber*)_zIndex {
    zIndex = _zIndex;
    [self updatePolygon];
}

- (void)updatePolygonGeometry {
    YMKLinearRing* ring = [YMKLinearRing linearRingWithPoints:_points];
    NSMutableArray<YMKLinearRing*>* _innerRings = [[NSMutableArray alloc] init];

    for (int i = 0; i < [innerRings count]; ++i) {
        YMKLinearRing* iRing = [YMKLinearRing linearRingWithPoints:[innerRings objectAtIndex:i]];
        [_innerRings addObject:iRing];
    }
    polygon = [YMKPolygon polygonWithOuterRing:ring innerRings:_innerRings];
}

- (void)setPolygonPoints:(NSMutableArray<YMKPoint*>*)points {
    _points = points;
    [self updatePolygonGeometry];
    [self updatePolygon];
}

- (void)setInnerRings:(NSArray<NSArray<YMKPoint*>*>*)_innerRings {
    innerRings = _innerRings;
    [self updatePolygonGeometry];
    [self updatePolygon];
}

- (void)setHandled:(BOOL)_handled {
    handled = _handled;
}

- (void)setMapObject:(YMKPolygonMapObject *)_mapObject {
    mapObject = _mapObject;
    [mapObject addTapListenerWithTapListener:self];
    [self updatePolygon];
}

- (BOOL)onMapObjectTapWithMapObject:(nonnull YMKMapObject*)mapObject point:(nonnull YMKPoint*)point {
    if (self.onPress)
        self.onPress(@{});

    return handled;
}

- (NSMutableArray<YMKPoint*>*)getPoints {
    return _points;
}

- (YMKPolygon*)getPolygon {
    return polygon;
}

- (YMKPolygonMapObject*)getMapObject {
    return mapObject;
}

@end
