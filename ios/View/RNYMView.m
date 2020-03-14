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
#import <YandexMapKitDirections/YMKDrivingRouter.h>
#import <YandexMapKitDirections/YMKDrivingSession.h>
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
#import "YamapPolylineView.h"
#import "YamapMarkerView.h"
#import "RNYMView.h"


#define ANDROID_COLOR(c) [UIColor colorWithRed:((c>>16)&0xFF)/255.0 green:((c>>8)&0xFF)/255.0 blue:((c)&0xFF)/255.0  alpha:((c>>24)&0xFF)/255.0]

#define UIColorFromRGB(rgbValue) [UIColor colorWithRed:((float)((rgbValue & 0xFF0000) >> 16))/255.0 green:((float)((rgbValue & 0xFF00) >> 8))/255.0 blue:((float)(rgbValue & 0xFF))/255.0 alpha:1.0]

@implementation RNYMView {
    YMKMasstransitSession *masstransitSession;
    YMKMasstransitSession *walkSession;
    YMKMasstransitRouter *masstransitRouter;
    YMKDrivingRouter* drivingRouter;
    YMKDrivingSession* drivingSession;
    YMKPedestrianRouter *pedestrianRouter;
    YMKMasstransitOptions *masstransitOptions;
    YMKMasstransitSessionRouteHandler routeHandler;
    NSMutableArray<UIView*>* _reactSubviews;
    NSMutableArray *routes;
    NSMutableArray *currentRouteInfo;
    NSMutableArray<YMKRequestPoint *>* lastKnownRoutePoints;
    YMKUserLocationView* userLocationView;
    NSMutableDictionary *vehicleColors;
    UIImage* userLocationImage;
    NSArray *acceptVehicleTypes;
}

- (instancetype)init {
    self = [super init];
    _reactSubviews = [[NSMutableArray alloc] init];
    masstransitRouter = [[YMKTransport sharedInstance] createMasstransitRouter];
    drivingRouter = [[YMKDirections sharedInstance] createDrivingRouter];
    pedestrianRouter = [[YMKTransport sharedInstance] createPedestrianRouter];
    masstransitOptions = [YMKMasstransitOptions masstransitOptionsWithAvoidTypes:[[NSArray alloc] init] acceptTypes:[[NSArray alloc] init] timeOptions:[[YMKTimeOptions alloc] init]];
    acceptVehicleTypes = [[NSMutableArray<NSString *> alloc] init];
    routes = [[NSMutableArray alloc] init];
    currentRouteInfo = [[NSMutableArray alloc] init];
    lastKnownRoutePoints = [[NSMutableArray alloc] init];
    vehicleColors = [[NSMutableDictionary alloc] init];
    [vehicleColors setObject:@"#59ACFF" forKey:@"bus"];
    [vehicleColors setObject:@"#7D60BD" forKey:@"minibus"];
    [vehicleColors setObject:@"#F8634F" forKey:@"railway"];
    [vehicleColors setObject:@"#C86DD7" forKey:@"tramway"];
    [vehicleColors setObject:@"#3023AE" forKey:@"suburban"];
    [vehicleColors setObject:@"#BDCCDC" forKey:@"underground"];
    [vehicleColors setObject:@"#55CfDC" forKey:@"trolleybus"];
    [vehicleColors setObject:@"#2d9da8" forKey:@"walk"];
    YMKMapKit* inst = [YMKMapKit sharedInstance];
    YMKUserLocationLayer *userLayer = [inst createUserLocationLayerWithMapWindow: self.mapWindow];
    userLocationView = nil;
    userLocationImage = nil;
    [userLayer setVisibleWithOn:YES];
    [userLayer setObjectListenerWithObjectListener: self];
    return self;
}

-(NSDictionary*) convertDrivingRouteSection:(YMKDrivingRoute*) route withSection:(YMKDrivingSection*) section {
    int routeIndex = 0;
    YMKDrivingWeight* routeWeight = route.metadata.weight;
    NSMutableDictionary *routeMetadata = [[NSMutableDictionary alloc] init];
    NSMutableDictionary *routeWeightData = [[NSMutableDictionary alloc] init];
    NSMutableDictionary *sectionWeightData = [[NSMutableDictionary alloc] init];
    NSMutableDictionary *transports = [[NSMutableDictionary alloc] init];
    NSMutableArray *stops = [[NSMutableArray alloc] init];
    [routeWeightData setObject:routeWeight.time.text forKey:@"time"];
    [routeWeightData setObject:routeWeight.timeWithTraffic.text forKey:@"timeWithTraffic"];
    [routeWeightData setObject:@(routeWeight.distance.value) forKey:@"distance"];
    [sectionWeightData setObject:section.metadata.weight.time.text forKey:@"time"];
    [sectionWeightData setObject:section.metadata.weight.timeWithTraffic.text forKey:@"timeWithTraffic"];
    [sectionWeightData setObject:@(section.metadata.weight.distance.value) forKey:@"distance"];
    [routeMetadata setObject:sectionWeightData forKey:@"sectionInfo"];
    [routeMetadata setObject:routeWeightData forKey:@"routeInfo"];
    [routeMetadata setObject:@(routeIndex) forKey:@"routeIndex"];
    [routeMetadata setObject:stops forKey:@"stops"];
    [routeMetadata setObject:UIColor.darkGrayColor forKey:@"sectionColor"];
    if (section.metadata.weight.distance.value == 0) {
        [routeMetadata setObject:@"waiting" forKey:@"type"];
    } else {
        [routeMetadata setObject:@"car" forKey:@"type"];
    }
    NSMutableDictionary *wTransports = [[NSMutableDictionary alloc] init];
    for (NSString *key in transports) {
        [wTransports setObject:[transports valueForKey:key] forKey:key];
    }
    [routeMetadata setObject:wTransports forKey:@"transports"];
    NSMutableArray* points = [[NSMutableArray alloc] init];
    YMKPolyline* subpolyline = YMKMakeSubpolyline(route.geometry, section.geometry);
    for (int i = 0; i < [subpolyline.points count]; ++i) {
        YMKPoint* point = [subpolyline.points objectAtIndex:i];
        NSMutableDictionary* jsonPoint = [[NSMutableDictionary alloc] init];
        [jsonPoint setValue:[NSNumber numberWithDouble:point.latitude] forKey:@"lat"];
        [jsonPoint setValue:[NSNumber numberWithDouble:point.longitude] forKey:@"lon"];
        [points addObject:jsonPoint];
    }
    [routeMetadata setValue:points forKey:@"points"];
    return routeMetadata;
}
-(NSDictionary*) convertRouteSection:(YMKMasstransitRoute*) route withSection:(YMKMasstransitSection*) section {
    int routeIndex = 0;
    YMKMasstransitWeight* routeWeight = route.metadata.weight;
    YMKMasstransitSectionMetadataSectionData *data = section.metadata.data;
    NSMutableDictionary *routeMetadata = [[NSMutableDictionary alloc] init];
    NSMutableDictionary *routeWeightData = [[NSMutableDictionary alloc] init];
    NSMutableDictionary *sectionWeightData = [[NSMutableDictionary alloc] init];
    NSMutableDictionary *transports = [[NSMutableDictionary alloc] init];
    NSMutableArray *stops = [[NSMutableArray alloc] init];
    [routeWeightData setObject:routeWeight.time.text forKey:@"time"];
    [routeWeightData setObject:@(routeWeight.transfersCount) forKey:@"transferCount"];
    [routeWeightData setObject:@(routeWeight.walkingDistance.value) forKey:@"walkingDistance"];
    [sectionWeightData setObject:section.metadata.weight.time.text forKey:@"time"];
    [sectionWeightData setObject:@(section.metadata.weight.transfersCount) forKey:@"transferCount"];
    [sectionWeightData setObject:@(section.metadata.weight.walkingDistance.value) forKey:@"walkingDistance"];
    [routeMetadata setObject:sectionWeightData forKey:@"sectionInfo"];
    [routeMetadata setObject:routeWeightData forKey:@"routeInfo"];
    [routeMetadata setObject:@(routeIndex) forKey:@"routeIndex"];
    for (YMKMasstransitRouteStop *stop in section.stops) {
        [stops addObject:stop.stop.name];
    }
    [routeMetadata setObject:stops forKey:@"stops"];
    if (data.transports != nil) {
        for (YMKMasstransitTransport *transport in data.transports) {
            for (NSString *type in transport.line.vehicleTypes) {
                if ([type isEqual: @"suburban"]) continue;
                if (transports[type] != nil) {
                    NSMutableArray *list = transports[type];
                    if (list != nil) {
                        [list addObject:transport.line.name];
                        [transports setObject:list forKey:type];
                    }
                } else {
                    NSMutableArray *list = [[NSMutableArray alloc] init];
                    [list addObject:transport.line.name];
                    [transports setObject:list forKey:type];
                }
                [routeMetadata setObject:type forKey:@"type"];
                UIColor *color;
                if (transport.line.style != nil) {
                    color = UIColorFromRGB([transport.line.style.color integerValue]);
                } else {
                    if ([vehicleColors valueForKey:type] != nil) {
                        color = [RNYMView colorFromHexString:vehicleColors[type]];
                    } else {
                        color = UIColor.blackColor;
                    }
                }
                [routeMetadata setObject:[RNYMView hexStringFromColor:color] forKey:@"sectionColor"];
            }
        }
    } else {
        [routeMetadata setObject:UIColor.darkGrayColor forKey:@"sectionColor"];
        if (section.metadata.weight.walkingDistance.value == 0) {
            [routeMetadata setObject:@"waiting" forKey:@"type"];
        } else {
            [routeMetadata setObject:@"walk" forKey:@"type"];
        }
    }
    NSMutableDictionary *wTransports = [[NSMutableDictionary alloc] init];
    for (NSString *key in transports) {
        [wTransports setObject:[transports valueForKey:key] forKey:key];
    }
    [routeMetadata setObject:wTransports forKey:@"transports"];
    NSMutableArray* points = [[NSMutableArray alloc] init];
    YMKPolyline* subpolyline = YMKMakeSubpolyline(route.geometry, section.geometry);
    for (int i = 0; i < [subpolyline.points count]; ++i) {
        YMKPoint* point = [subpolyline.points objectAtIndex:i];
        NSMutableDictionary* jsonPoint = [[NSMutableDictionary alloc] init];
        [jsonPoint setValue:[NSNumber numberWithDouble:point.latitude] forKey:@"lat"];
        [jsonPoint setValue:[NSNumber numberWithDouble:point.longitude] forKey:@"lon"];
        [points addObject:jsonPoint];
    }
    [routeMetadata setValue:points forKey:@"points"];
    return routeMetadata;
}

-(void) findRoutes:(NSArray<YMKRequestPoint*>*) _points vehicles:(NSArray<NSString*>*) vehicles withId:(NSString*)_id {
    __weak RNYMView *weakSelf = self;
    if ([vehicles count] == 1 && [[vehicles objectAtIndex:0] isEqualToString:@"car"]) {
        YMKDrivingDrivingOptions* options = [[YMKDrivingDrivingOptions alloc] init];
        drivingSession = [drivingRouter requestRoutesWithPoints:_points drivingOptions:options routeHandler:^(NSArray<YMKDrivingRoute *> *routes, NSError *error) {
            RNYMView *strongSelf = weakSelf;
            if (error != nil) {
                [strongSelf onReceiveNativeEvent: @{@"id": _id, @"status": @"error"}];
                return;
            }
            NSMutableDictionary* response = [[NSMutableDictionary alloc] init];
            [response setValue:_id forKey:@"id"];
            [response setValue:@"status" forKey:@"success"];
            NSMutableArray* jsonRoutes = [[NSMutableArray alloc] init];
            for (int i = 0; i < [routes count]; ++i) {
                YMKDrivingRoute* _route = [routes objectAtIndex:i];
                NSMutableDictionary* jsonRoute = [[NSMutableDictionary alloc] init];
                [jsonRoute setValue:[NSString stringWithFormat:@"%d", i] forKey:@"id"];
                NSMutableArray* sections = [[NSMutableArray alloc] init];
                NSArray<YMKDrivingSection *>* _sections = [_route sections];
                for (int j = 0; j < [_sections count]; ++j) {
                    NSDictionary* jsonSection = [self convertDrivingRouteSection:_route withSection: [_sections objectAtIndex:j]];
                    [sections addObject:jsonSection];
                }
                [jsonRoute setValue:sections forKey:@"sections"];
                [jsonRoutes addObject:jsonRoute];
            }
            [response setValue:jsonRoutes forKey:@"routes"];
            [strongSelf onReceiveNativeEvent: response];
        }];
        return;
    }
    YMKMasstransitSessionRouteHandler _routeHandler = ^(NSArray<YMKMasstransitRoute *> *routes, NSError *error) {
        RNYMView *strongSelf = weakSelf;
        if (error != nil) {
            [strongSelf onReceiveNativeEvent: @{@"id": _id, @"status": @"error"}];
            return;
        }
        NSMutableDictionary* response = [[NSMutableDictionary alloc] init];
        [response setValue:_id forKey:@"id"];
        [response setValue:@"status" forKey:@"success"];
        NSMutableArray* jsonRoutes = [[NSMutableArray alloc] init];
        for (int i = 0; i < [routes count]; ++i) {
            YMKMasstransitRoute* _route = [routes objectAtIndex:i];
            NSMutableDictionary* jsonRoute = [[NSMutableDictionary alloc] init];

            [jsonRoute setValue:[NSString stringWithFormat:@"%d", i] forKey:@"id"];
            NSMutableArray* sections = [[NSMutableArray alloc] init];
            NSArray<YMKMasstransitSection *>* _sections = [_route sections];
            for (int j = 0; j < [_sections count]; ++j) {
                NSDictionary* jsonSection = [self convertRouteSection:_route withSection: [_sections objectAtIndex:j]];
                [sections addObject:jsonSection];
            }
            [jsonRoute setValue:sections forKey:@"sections"];
            [jsonRoutes addObject:jsonRoute];
        }
        [response setValue:jsonRoutes forKey:@"routes"];
        [strongSelf onReceiveNativeEvent: response];
    };
    if ([vehicles count] == 0) {
        walkSession = [pedestrianRouter requestRoutesWithPoints:_points timeOptions:[[YMKTimeOptions alloc] init] routeHandler:_routeHandler];
        return;
    }
    YMKMasstransitOptions* _masstransitOptions =[YMKMasstransitOptions masstransitOptionsWithAvoidTypes:[[NSArray alloc] init] acceptTypes:vehicles timeOptions:[[YMKTimeOptions alloc] init]];
    masstransitSession = [masstransitRouter requestRoutesWithPoints:_points masstransitOptions:_masstransitOptions routeHandler:_routeHandler];
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

-(void)onReceiveNativeEvent:(NSDictionary *)response {
    if (self.onRouteFound) self.onRouteFound(response);
}

-(void) removeAllSections {
    [self.mapWindow.map.mapObjects clear];
}

// ref
-(void) setCenter:(YMKPoint*) center withZoom:(float) zoom {
    [self.mapWindow.map moveWithCameraPosition:[YMKCameraPosition cameraPositionWithTarget:center zoom:zoom azimuth:0 tilt:0]];
}

-(void) fitAllMarkers {
    NSMutableArray<YMKPoint*>* lastKnownMarkers = [[NSMutableArray alloc] init];
    for (int i = 0; i < [_reactSubviews count]; ++i) {
        UIView* view = [_reactSubviews objectAtIndex:i];
        if ([view isKindOfClass:[YamapMarkerView class]]) {
            YamapMarkerView* marker = (YamapMarkerView*) view;
            [lastKnownMarkers addObject:[marker getPoint]];
        }
    }
    if ([lastKnownMarkers count] == 0) {
        return;
    }
    if ([lastKnownMarkers count] == 1) {
        YMKPoint *center = [lastKnownMarkers objectAtIndex:0];
        [self.mapWindow.map moveWithCameraPosition:[YMKCameraPosition cameraPositionWithTarget:center zoom:15 azimuth:0 tilt:0]];
        return;
    }
    double minLon = [lastKnownMarkers[0] longitude], maxLon = [lastKnownMarkers[0] longitude];
    double minLat = [lastKnownMarkers[0] latitude], maxLat = [lastKnownMarkers[0] latitude];
    for (int i = 0; i < [lastKnownMarkers count]; i++) {
        if ([lastKnownMarkers[i] longitude] > maxLon) maxLon = [lastKnownMarkers[i] longitude];
        if ([lastKnownMarkers[i] longitude] < minLon) minLon = [lastKnownMarkers[i] longitude];
        if ([lastKnownMarkers[i] latitude] > maxLat) maxLat = [lastKnownMarkers[i] latitude];
        if ([lastKnownMarkers[i] latitude] < minLat) minLat = [lastKnownMarkers[i] latitude];
    }
    YMKPoint *southWest = [YMKPoint pointWithLatitude:minLat longitude:minLon];
    YMKPoint *northEast = [YMKPoint pointWithLatitude:maxLat longitude:maxLon];
    YMKPoint *rectCenter = [YMKPoint pointWithLatitude:(minLat + maxLat) / 2 longitude:(minLon + maxLon) / 2];
    CLLocation *centerP = [[CLLocation alloc] initWithLatitude:northEast.latitude longitude:northEast.longitude];
    CLLocation *edgeP = [[CLLocation alloc] initWithLatitude:rectCenter.latitude longitude:rectCenter.longitude];
    CLLocationDistance distance = [centerP distanceFromLocation:edgeP];
    double scale = (distance/2)/140;
    int zoom = (int) (16 - log(scale) / log(2));
    YMKBoundingBox *boundingBox = [YMKBoundingBox boundingBoxWithSouthWest:southWest northEast:northEast];
    YMKCameraPosition *cameraPosition = [self.mapWindow.map cameraPositionWithBoundingBox:boundingBox];
    cameraPosition = [YMKCameraPosition cameraPositionWithTarget:cameraPosition.target zoom:zoom azimuth:cameraPosition.azimuth tilt:cameraPosition.tilt];
    [self.mapWindow.map moveWithCameraPosition:cameraPosition animationType:[YMKAnimation animationWithType:YMKAnimationTypeSmooth duration:1.0] cameraCallback:^(BOOL completed){}];
}

// props
-(void) setUserLocationIcon:(NSString*) iconSource {
    userLocationImage = [self resolveUIImage: iconSource];
    [self updateUserIcon];
}

-(void) updateUserIcon {
    if (userLocationView != nil && userLocationImage) {
        [userLocationView.pin setIconWithImage: userLocationImage];
        [userLocationView.arrow setIconWithImage: userLocationImage];
    }
}

// user location listener implementation
- (void)onObjectAddedWithView:(nonnull YMKUserLocationView *)view {
    userLocationView = view;
    [self updateUserIcon];
}

- (void)onObjectRemovedWithView:(nonnull YMKUserLocationView *)view {
}

- (void)onObjectUpdatedWithView:(nonnull YMKUserLocationView *)view event:(nonnull YMKObjectEvent *)event {
}

// utils
+(UIColor *) colorFromHexString:(NSString*) hexString {
    unsigned rgbValue = 0;
    NSScanner *scanner = [NSScanner scannerWithString:hexString];
    [scanner setScanLocation:1];
    [scanner scanHexInt:&rgbValue];
    return [UIColor colorWithRed:((rgbValue & 0xFF0000) >> 16)/255.0 green:((rgbValue & 0xFF00) >> 8)/255.0 blue:(rgbValue & 0xFF)/255.0 alpha:1.0];
}

+(NSString*) hexStringFromColor:(UIColor *) color {
    const CGFloat *components = CGColorGetComponents(color.CGColor);
    CGFloat r = components[0];
    CGFloat g = components[1];
    CGFloat b = components[2];
    return [NSString stringWithFormat:@"#%02lX%02lX%02lX", lroundf(r * 255), lroundf(g * 255), lroundf(b * 255)];
}

// children
-(void)addSubview:(UIView *)view {
    [super addSubview:view];
}

- (void)insertReactSubview:(UIView<RCTComponent>*)subview atIndex:(NSInteger)atIndex {
    if ([subview isKindOfClass:[YamapPolygonView class]]) {
        YMKMapObjectCollection *objects = self.mapWindow.map.mapObjects;
        YamapPolygonView* polygon = (YamapPolygonView*) subview;
        YMKPolygonMapObject* obj = [objects addPolygonWithPolygon:[polygon getPolygon]];
        [polygon setMapObject:obj];
    } else if ([subview isKindOfClass:[YamapPolylineView class]]) {
        YMKMapObjectCollection *objects = self.mapWindow.map.mapObjects;
        YamapPolylineView* polyline = (YamapPolylineView*) subview;
        YMKPolylineMapObject* obj = [objects addPolylineWithPolyline:[polyline getPolyline]];
        [polyline setMapObject:obj];
    } else if ([subview isKindOfClass:[YamapMarkerView class]]) {
        YMKMapObjectCollection *objects = self.mapWindow.map.mapObjects;
        YamapMarkerView* marker = (YamapMarkerView*) subview;
        YMKPlacemarkMapObject* obj = [objects addPlacemarkWithPoint:[marker getPoint]];
        [marker setMapObject:obj];
    } else {
        NSArray<id<RCTComponent>> *childSubviews = [subview reactSubviews];
        for (int i = 0; i < childSubviews.count; i++) {
            [self insertReactSubview:(UIView *)childSubviews[i] atIndex:atIndex];
        }
    }
    [_reactSubviews insertObject:subview atIndex:atIndex];
    [super insertReactSubview:subview atIndex:atIndex];
}

- (void)removeReactSubview:(UIView<RCTComponent>*)subview {
    if ([subview isKindOfClass:[YamapPolygonView class]]) {
        YMKMapObjectCollection *objects = self.mapWindow.map.mapObjects;
        YamapPolygonView* polygon = (YamapPolygonView*) subview;
        [objects removeWithMapObject:[polygon getMapObject]];
    } else if ([subview isKindOfClass:[YamapPolylineView class]]) {
        YMKMapObjectCollection *objects = self.mapWindow.map.mapObjects;
        YamapPolylineView* polyline = (YamapPolylineView*) subview;
        [objects removeWithMapObject:[polyline getMapObject]];
    } else if ([subview isKindOfClass:[YamapMarkerView class]]) {
        YMKMapObjectCollection *objects = self.mapWindow.map.mapObjects;
        YamapMarkerView* marker = (YamapMarkerView*) subview;
        [objects removeWithMapObject:[marker getMapObject]];
    } else {
        NSArray<id<RCTComponent>> *childSubviews = [subview reactSubviews];
        for (int i = 0; i < childSubviews.count; i++) {
            [self removeReactSubview:(UIView *)childSubviews[i]];
        }
    }
    [_reactSubviews removeObject:subview];
    [super removeReactSubview: subview];
}

@end
