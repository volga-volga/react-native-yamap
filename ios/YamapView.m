#import <React/RCTViewManager.h>
#import <YandexMapKit/YMKMapKitFactory.h>
#import <YandexMapKit/YMKMapView.h>
#import <YandexMapKit/YMKCameraPosition.h>
#import <YandexMapKit/YMKMap.h>
#import <YandexMapKit/YMKMapObjectCollection.h>
#import <YandexMapKit/YMKPlacemarkMapObject.h>
#import "RCTConvert+Yamap.m"
#import "RNMarker.h"
#import "RNYMView.h"

@interface YamapView : RCTViewManager<YMKMapObjectTapListener, YMKUserLocationObjectListener>
@property RNYMView *map;
@property (nonatomic) NSMutableArray<RNMarker*> *markers;
@property (nonatomic) NSMutableDictionary<NSString*, YMKPlacemarkMapObject*> *markers_dict;
@end

@implementation YamapView
RCT_EXPORT_MODULE()


- (NSArray<NSString *> *)supportedEvents
{
    return @[@"onMarkerPress"];
}

RCT_EXPORT_VIEW_PROPERTY(onMarkerPress, RCTBubblingEventBlock)
- (void)onObjectAddedWithView:(nonnull YMKUserLocationView *)view {
    [view.pin setIconWithImage:[UIImage imageNamed:@"base_location"]];
    YMKIconStyle *selectedStyle = [[YMKIconStyle alloc] init];
    selectedStyle.scale = [[NSNumber alloc] initWithDouble:0.5];
    [view.pin setIconStyleWithStyle:selectedStyle];
}
- (void)onObjectRemovedWithView:(nonnull YMKUserLocationView *)view {}
- (void)onObjectUpdatedWithView:(nonnull YMKUserLocationView *)view
                          event:(nonnull YMKObjectEvent *)event {}
- (BOOL)onMapObjectTapWithMapObject:(nonnull YMKMapObject *)mapObject
                              point:(nonnull YMKPoint *)point {
    // очень тупой способ определения на какой маркер ткнули - просто нахожу ближайший
    double minDistance = 10000;
    int minDistanceIndex = -1;
    for (int i = 0; i < self.markers.count; ++i) {
        double dist = (self.markers[i].lat - point.latitude)*(self.markers[i].lat - point.latitude) + (self.markers[i].lon - point.longitude)*(self.markers[i].lon - point.longitude);
        if (dist < minDistance) {
            minDistance = dist;
            minDistanceIndex = i;
        }
    }
    if (self.map.onMarkerPress) {
        self.map.onMarkerPress(@{
          @"id": self.markers[minDistanceIndex]._id
        });
    }
    return YES;
}

- (UIView *)view {
    self.markers = nil;
    self.markers_dict = nil;
    self.map = [[RNYMView alloc] init];
    YMKUserLocationLayer *userLayer = self.map.mapWindow.map.userLocationLayer;
    [userLayer setEnabled:YES];
    [userLayer setObjectListenerWithObjectListener: self];
    return self.map;
}

RCT_CUSTOM_VIEW_PROPERTY (markers, NSArray<YMKPoint>, YMKMapView) {
    unsigned long count = [json count];
    YMKMapObjectCollection *objects = self.map.mapWindow.map.mapObjects;
    NSMutableArray<RNMarker*> *arr = [[NSMutableArray<RNMarker*> alloc] initWithCapacity:count];
    UIImage *selected = [UIImage imageNamed:@"selected"];
    UIImage *normal = [UIImage imageNamed:@"normal"];
    YMKIconStyle *selectedStyle = [[YMKIconStyle alloc] init];
    selectedStyle.scale = [[NSNumber alloc] initWithDouble:0.5];
    if (!self.markers_dict) {
        self.markers_dict = [[NSMutableDictionary alloc] init];
    }
    for (unsigned long i = 0; i < count; ++i) {
        RNMarker *marker = [[RNMarker alloc] initWithJson:json[i]];
        YMKPlacemarkMapObject *foo = [self.markers_dict valueForKey:marker._id];
        if (!foo) {
            YMKPoint *point = [YMKPoint pointWithLatitude:marker.lat longitude:marker.lon];
            foo = [objects addPlacemarkWithPoint:point];
            [self.markers_dict setValue:foo forKey:marker._id];
        }
        YMKPlacemarkMapObject *a = foo; //[objects addPlacemarkWithPoint:point];
        arr[i] = marker;
        if (selected && normal) {
            [a setIconWithImage:marker.isSelected ? selected : normal];
            [a setIconStyleWithStyle:selectedStyle];
        }
        [a addTapListenerWithTapListener:self];
    }
    self.markers = arr;
    NSArray<NSString*> *a = [self.markers_dict allKeys];
    for (int i = 0; i < [a count]; ++i) {
        NSString *key = a[i];
        Boolean exist = NO;
        for (unsigned long i = 0; i < count; ++i) {
            if (self.markers[i]._id == key) {
                exist = YES;
                break;
            }
        }
        if (!exist) {
            YMKPlacemarkMapObject *a = [self.markers_dict valueForKey:key];
            [self.markers_dict removeObjectForKey:key];
            [objects removeWithMapObject:a];
        }
    }
}

RCT_CUSTOM_VIEW_PROPERTY(center, YMKPoint, YMKMapView) {
    if (json) {
        YMKPoint *center = [RCTConvert YMKPoint:json];
        [self.map.mapWindow.map moveWithCameraPosition:[YMKCameraPosition cameraPositionWithTarget:center
                                                                                              zoom:11
                                                                                           azimuth:0
                                                                                              tilt:0]];
    }
}

@end
