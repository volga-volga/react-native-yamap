#import <React/RCTComponent.h>

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

#import "YamapPolygonView.h"


#define ANDROID_COLOR(c) [UIColor colorWithRed:((c>>16)&0xFF)/255.0 green:((c>>8)&0xFF)/255.0 blue:((c)&0xFF)/255.0  alpha:((c>>24)&0xFF)/255.0]

#define UIColorFromRGB(rgbValue) [UIColor colorWithRed:((float)((rgbValue & 0xFF0000) >> 16))/255.0 green:((float)((rgbValue & 0xFF00) >> 8))/255.0 blue:((float)(rgbValue & 0xFF))/255.0 alpha:1.0]

@implementation YamapPolygonView {
    NSMutableArray<YMKPoint*> * _points;
    YMKPolygonMapObject* mapObject;
    YMKPolygon* polygon;
    UIColor* fillColor;
    UIColor* strokeColor;
    NSNumber* strokeWidth;
    NSNumber* zIndex;
}

- (instancetype)init {
    self = [super init];
    fillColor = UIColor.blackColor;
    strokeColor = UIColor.blackColor;
    zIndex =  [[NSNumber alloc] initWithInt:1];
    strokeWidth =  [[NSNumber alloc] initWithInt:1];
    _points = [[NSMutableArray alloc] init];
    polygon = [YMKPolygon polygonWithOuterRing:[YMKLinearRing linearRingWithPoints:@[]] innerRings:@[]];
    return self;
}

-(void) updatePolygon {
    if (mapObject != nil) {
        [mapObject setGeometry:polygon];
        [mapObject setZIndex:[zIndex floatValue]];
        [mapObject setFillColor:fillColor];
        [mapObject setStrokeColor:strokeColor];
        [mapObject setStrokeWidth:[strokeWidth floatValue]];
    }
}

-(void) setFillColor:(UIColor*) color {
    fillColor = color;
    [self updatePolygon];
}
-(void) setStrokeColor:(UIColor*) color {
    strokeColor = color;
    [self updatePolygon];
}
-(void) setStrokeWidth:(NSNumber*) width {
    strokeWidth = width;
    [self updatePolygon];
}
-(void) setZIndex:(NSNumber*) _zIndex {
    zIndex = _zIndex;
    [self updatePolygon];
}
-(void) setPolygonPoints:(NSMutableArray<YMKPoint*>*) points {
    _points = points;
    YMKLinearRing* ring = [YMKLinearRing linearRingWithPoints:points];
    polygon = [YMKPolygon polygonWithOuterRing:ring innerRings:@[]];
    [self updatePolygon];
}

-(void) setMapObject:(YMKPolygonMapObject *)_mapObject {
    mapObject = _mapObject;
    [mapObject addTapListenerWithTapListener:self];
    [self updatePolygon];
}
// object tap listener
- (BOOL)onMapObjectTapWithMapObject:(nonnull YMKMapObject *)mapObject point:(nonnull YMKPoint *)point {
    if (self.onPress) self.onPress(@{});
    return YES;
}

-(NSMutableArray<YMKPoint*>*) getPoints {
    return _points;
}

-(YMKPolygon*) getPolygon {
    return polygon;
}

-(YMKPolygonMapObject*) getMapObject {
    return mapObject;
}

@end
