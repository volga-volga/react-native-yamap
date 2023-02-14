#import <React/RCTComponent.h>

#import <MapKit/MapKit.h>
@import YandexMapsMobile;

#ifndef MAX
#import <NSObjCRuntime.h>
#endif

#import "YamapPolylineView.h"

@implementation YamapPolylineView {
    NSMutableArray<YMKPoint*> * _points;
    YMKPolylineMapObject* mapObject;
    YMKPolyline* polyline;
    UIColor* strokeColor;
    UIColor* outlineColor;
    NSNumber* strokeWidth;
    NSNumber* dashLength;
    NSNumber* dashOffset;
    NSNumber* gapLength;
    NSNumber* outlineWidth;
    NSNumber* zIndex;
}

- (instancetype)init {
    self = [super init];
    strokeColor = UIColor.blackColor;
    outlineColor = UIColor.blackColor;
    zIndex =  [[NSNumber alloc] initWithInt:1];
    strokeWidth =  [[NSNumber alloc] initWithInt:1];
    dashLength = [[NSNumber alloc] initWithInt:1];
    gapLength =  [[NSNumber alloc] initWithInt:0];
    outlineWidth =  [[NSNumber alloc] initWithInt:0];
    dashOffset =  [[NSNumber alloc] initWithInt:0];
    _points = [[NSMutableArray alloc] init];
    polyline = [YMKPolyline polylineWithPoints:_points];

    return self;
}

- (void)updatePolyline {
    if (mapObject != nil) {
        [mapObject setGeometry:polyline];
        [mapObject setZIndex:[zIndex floatValue]];
        [mapObject setStrokeColorWithColor:strokeColor];
        [mapObject setStrokeWidth:[strokeWidth floatValue]];
        [mapObject setDashLength:[dashLength floatValue]];
        [mapObject setGapLength:[gapLength floatValue]];
        [mapObject setDashOffset:[dashOffset floatValue]];
        [mapObject setOutlineWidth:[outlineWidth floatValue]];
        [mapObject setOutlineColor:outlineColor];
    }
}

- (void)setStrokeColor:(UIColor*)color {
    strokeColor = color;
    [self updatePolyline];
}

- (void)setStrokeWidth:(NSNumber*)width {
    strokeWidth = width;
    [self updatePolyline];
}

- (void)setOutlineWidth:(NSNumber*)width {
    outlineWidth = width;
    [self updatePolyline];
}

- (void)setDashLength:(NSNumber*)length {
    dashLength = length;
    [self updatePolyline];
}

- (void)setDashOffset:(NSNumber*)offset {
    dashOffset = offset;
    [self updatePolyline];
}

- (void)setGapLength:(NSNumber*)length {
    gapLength = length;
    [self updatePolyline];
}

- (void)setOutlineColor:(UIColor*)color {
    outlineColor = color;
    [self updatePolyline];
}

- (void)setZIndex:(NSNumber*)_zIndex {
    zIndex = _zIndex;
    [self updatePolyline];
}

- (void)setPolylinePoints:(NSMutableArray<YMKPoint*>*)points {
    _points = points;
    polyline = [YMKPolyline polylineWithPoints:points];
    [self updatePolyline];
}

- (void)setMapObject:(YMKPolylineMapObject*)_mapObject {
    mapObject = _mapObject;
    [mapObject addTapListenerWithTapListener:self];
    [self updatePolyline];
}

- (BOOL)onMapObjectTapWithMapObject:(nonnull YMKMapObject*)mapObject point:(nonnull YMKPoint*)point {
    if (self.onPress)
        self.onPress(@{});

    return YES;
}

- (NSMutableArray<YMKPoint*>*)getPoints {
    return _points;
}

- (YMKPolyline*)getPolyline {
    return polyline;
}

- (YMKPolylineMapObject*)getMapObject {
    return mapObject;
}

@end
