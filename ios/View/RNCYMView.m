#import <React/RCTComponent.h>
#import <React/UIView+React.h>

#import <MapKit/MapKit.h>
@import YandexMapsMobile;

#ifndef MAX
#import <NSObjCRuntime.h>
#endif

#import "RNCYMView.h"
#import <YamapMarkerView.h>

#define ANDROID_COLOR(c) [UIColor colorWithRed:((c>>16)&0xFF)/255.0 green:((c>>8)&0xFF)/255.0 blue:((c)&0xFF)/255.0  alpha:((c>>24)&0xFF)/255.0]

#define UIColorFromRGB(rgbValue) [UIColor colorWithRed:((float)((rgbValue & 0xFF0000) >> 16))/255.0 green:((float)((rgbValue & 0xFF00) >> 8))/255.0 blue:((float)(rgbValue & 0xFF))/255.0 alpha:1.0]

@implementation RNCYMView {
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
    YMKUserLocationLayer *userLayer;
    UIColor* userLocationAccuracyFillColor;
    UIColor* userLocationAccuracyStrokeColor;
    float userLocationAccuracyStrokeWidth;
    YMKClusterizedPlacemarkCollection *clusterCollection;
    UIColor* clusterColor;
    NSArray<YMKRequestPoint*> *clusteredMarkers;
    BOOL userClusters;
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
    userLocationAccuracyFillColor = nil;
    userLocationAccuracyStrokeColor = nil;
    clusterColor=nil;
    userClusters=NO;
    userLocationAccuracyStrokeWidth = 0.f;
    [self.mapWindow.map addCameraListenerWithCameraListener:self];
    [self.mapWindow.map addInputListenerWithInputListener:(id<YMKMapInputListener>) self];
    clusterCollection = [self.mapWindow.map.mapObjects addClusterizedPlacemarkCollectionWithClusterListener:self];
    return self;
}

- (NSDictionary*)convertDrivingRouteSection:(YMKDrivingRoute*) route withSection:(YMKDrivingSection*) section {
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

- (NSDictionary*)convertRouteSection:(YMKMasstransitRoute*) route withSection:(YMKMasstransitSection*) section {
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
                        color = [RNCYMView colorFromHexString:vehicleColors[type]];
                    } else {
                        color = UIColor.blackColor;
                    }
                }
                [routeMetadata setObject:[RNCYMView hexStringFromColor:color] forKey:@"sectionColor"];
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

- (void)findRoutes:(NSArray<YMKRequestPoint*>*) _points vehicles:(NSArray<NSString*>*) vehicles withId:(NSString*)_id {
    __weak RNCYMView *weakSelf = self;
    if ([vehicles count] == 1 && [[vehicles objectAtIndex:0] isEqualToString:@"car"]) {
        YMKDrivingDrivingOptions* drivingOptions = [[YMKDrivingDrivingOptions alloc] init];
        YMKDrivingVehicleOptions* vehicleOptions = [[YMKDrivingVehicleOptions alloc] init];

        drivingSession = [drivingRouter requestRoutesWithPoints:_points drivingOptions:drivingOptions
                                                 vehicleOptions:vehicleOptions routeHandler:^(NSArray<YMKDrivingRoute *> *routes, NSError *error) {
            RNCYMView *strongSelf = weakSelf;
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
        RNCYMView *strongSelf = weakSelf;
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

- (UIImage*)resolveUIImage:(NSString*) uri {
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

- (void)onReceiveNativeEvent:(NSDictionary *)response {
    if (self.onRouteFound) self.onRouteFound(response);
}

- (void)removeAllSections {
    [self.mapWindow.map.mapObjects clear];
}

// ref
- (void)setCenter:(YMKCameraPosition*) position withDuration:(float) duration withAnimation:(int) animation {
    if (duration > 0) {
        YMKAnimationType anim = animation == 0 ? YMKAnimationTypeSmooth : YMKAnimationTypeLinear;
        [self.mapWindow.map moveWithCameraPosition:position animationType:[YMKAnimation animationWithType:anim duration: duration] cameraCallback: ^(BOOL completed) {
         }];
    } else {
        [self.mapWindow.map moveWithCameraPosition:position];
    }
}

- (void)setZoom:(float) zoom withDuration:(float) duration withAnimation:(int) animation {
    YMKCameraPosition* prevPosition = self.mapWindow.map.cameraPosition;
    YMKCameraPosition* position = [YMKCameraPosition cameraPositionWithTarget:prevPosition.target zoom:zoom azimuth:prevPosition.azimuth tilt:prevPosition.tilt];
    [self setCenter:position withDuration:duration withAnimation:animation];
}

- (void)setMapType:(NSString*) type {
    if ([type isEqual:@"none"]) {
        self.mapWindow.map.mapType = YMKMapTypeNone;
    } else if ([type isEqual:@"raster"]) {
        self.mapWindow.map.mapType = YMKMapTypeMap;
    } else {
        self.mapWindow.map.mapType = YMKMapTypeVectorMap;
    }
}

- (NSDictionary*)cameraPositionToJSON:(YMKCameraPosition*) position reason:(YMKCameraUpdateReason) reason finished:(BOOL) finished {
    return @{
        @"azimuth": [NSNumber numberWithFloat:position.azimuth],
        @"tilt": [NSNumber numberWithFloat:position.tilt],
        @"zoom": [NSNumber numberWithFloat:position.zoom],
        @"point": @{
            @"lat": [NSNumber numberWithDouble:position.target.latitude],
            @"lon": [NSNumber numberWithDouble:position.target.longitude],
        },
        @"reason": reason == 0 ? @"GESTURES" : @"APPLICATION",
        @"finished": @(finished)
    };
}

- (NSDictionary*)visibleRegionToJSON:(YMKVisibleRegion*) region {
    return @{
        @"bottomLeft": @{
                @"lat": [NSNumber numberWithDouble:region.bottomLeft.latitude],
                @"lon": [NSNumber numberWithDouble:region.bottomLeft.longitude],
        },
        @"bottomRight": @{
                @"lat": [NSNumber numberWithDouble:region.bottomRight.latitude],
                @"lon": [NSNumber numberWithDouble:region.bottomRight.longitude],
        },
        @"topLeft": @{
                @"lat": [NSNumber numberWithDouble:region.topLeft.latitude],
                @"lon": [NSNumber numberWithDouble:region.topLeft.longitude],
        },
        @"topRight": @{
                @"lat": [NSNumber numberWithDouble:region.topRight.latitude],
                @"lon": [NSNumber numberWithDouble:region.topRight.longitude],
        },
    };
}


- (void)emitCameraPositionToJS:(NSString*) _id {
    YMKCameraPosition* position = self.mapWindow.map.cameraPosition;
    NSDictionary* cameraPosition = [self cameraPositionToJSON:position reason:1 finished:YES];
    NSMutableDictionary *response = [NSMutableDictionary dictionaryWithDictionary:cameraPosition];
    [response setValue:_id forKey:@"id"];
    if (self.onCameraPositionReceived) {
        self.onCameraPositionReceived(response);
    }
}

- (void)emitVisibleRegionToJS:(NSString*) _id {
    YMKVisibleRegion* region = self.mapWindow.map.visibleRegion;
    NSDictionary* visibleRegion = [self visibleRegionToJSON:region];
    NSMutableDictionary *response = [NSMutableDictionary dictionaryWithDictionary:visibleRegion];
    [response setValue:_id forKey:@"id"];
    if (self.onVisibleRegionReceived) {
        self.onVisibleRegionReceived(response);
    }
}

- (void)onCameraPositionChangedWithMap:(nonnull YMKMap *) map
                        cameraPosition:(nonnull YMKCameraPosition *)cameraPosition
                    cameraUpdateReason:(YMKCameraUpdateReason)cameraUpdateReason
                              finished:(BOOL)finished {
    if (self.onCameraPositionChange) {
        self.onCameraPositionChange([self cameraPositionToJSON:cameraPosition reason:cameraUpdateReason finished:finished]);
    }
}

- (void)setNightMode:(BOOL) nightMode {
    [self.mapWindow.map setNightModeEnabled:nightMode];
}


- (void)setClusters:(BOOL) useUserClusters {
    userClusters = useUserClusters;
    [self updateUserMarkers];
}

-(void)updateUserMarkers {
    NSMutableArray<YamapMarkerView*>* lastKnownMarkers = [[NSMutableArray alloc] init];
    for (int i = 0; i < [_reactSubviews count]; ++i) {
        UIView* view = [_reactSubviews objectAtIndex:i];
        if ([view isKindOfClass:[YamapMarkerView class]]) {
            YamapMarkerView* marker = (YamapMarkerView*) view;
            [lastKnownMarkers addObject:marker];
            if (!userClusters) {
                [clusterCollection removeWithPlacemark:[marker getMapObject]];
            } else {
            YMKMapObjectCollection *objects = self.mapWindow.map.mapObjects;
            [objects removeWithMapObject:[marker getMapObject]];
            }
            [_reactSubviews removeObject:marker];
            --i;
        }
    }
        [clusterCollection clear];
    for (int i = 0; i < [lastKnownMarkers count]; ++i) {
        UIView* view = [lastKnownMarkers objectAtIndex:i];
        [self insertReactSubview:view atIndex:[_reactSubviews count]];
    }
}

- (void)setListenUserLocation:(BOOL) listen {
    YMKMapKit* inst = [YMKMapKit sharedInstance];
    if (userLayer == nil) {
        userLayer = [inst createUserLocationLayerWithMapWindow: self.mapWindow];
    }
    if (listen) {
        [userLayer setVisibleWithOn:YES];
        [userLayer setObjectListenerWithObjectListener: self];
    } else {
        [userLayer setVisibleWithOn:NO];
        [userLayer setObjectListenerWithObjectListener: nil];
    }
}

- (void)fitAllMarkers {
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


- (void)fitMarkers:(NSArray<YMKPlacemarkMapObject*>*) _points {
    NSMutableArray<YMKPoint*>* lastKnownMarkers = [[NSMutableArray alloc] init];
    for (int i = 0; i < [_points count]; ++i) {
            [lastKnownMarkers addObject:[[_points objectAtIndex:i] geometry]];
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
- (void)setUserLocationIcon:(NSString*) iconSource {
    userLocationImage = [self resolveUIImage: iconSource];
    [self updateUserIcon];
}

- (void)setUserLocationAccuracyFillColor: (UIColor*) color {
    userLocationAccuracyFillColor = color;
    [self updateUserIcon];
}

- (void)setClusterColor: (UIColor*) color {
    clusterColor = color;
}

- (void)setUserLocationAccuracyStrokeColor: (UIColor*) color {
    userLocationAccuracyStrokeColor = color;
    [self updateUserIcon];
}

- (void)setUserLocationAccuracyStrokeWidth: (float) width {
    userLocationAccuracyStrokeWidth = width;
    [self updateUserIcon];
}

- (void)updateUserIcon {
    if (userLocationView != nil) {
        if (userLocationImage) {
            [userLocationView.pin setIconWithImage: userLocationImage];
            [userLocationView.arrow setIconWithImage: userLocationImage];
        }
        YMKCircleMapObject* circle = userLocationView.accuracyCircle;
        if (userLocationAccuracyFillColor) {
            [circle setFillColor:userLocationAccuracyFillColor];
        }
        if (userLocationAccuracyStrokeColor) {
            [circle setStrokeColor:userLocationAccuracyStrokeColor];
        }
        [circle setStrokeWidth:userLocationAccuracyStrokeWidth];
    }
}

// user location listener implementation
- (void)onObjectAddedWithView:(nonnull YMKUserLocationView *) view {
    userLocationView = view;
    [self updateUserIcon];
}

- (void)onObjectRemovedWithView:(nonnull YMKUserLocationView *) view {
}

- (void)onObjectUpdatedWithView:(nonnull YMKUserLocationView *) view event:(nonnull YMKObjectEvent *) event {
    userLocationView = view;
    [self updateUserIcon];
}

- (void)onMapTapWithMap:(nonnull YMKMap *) map
                  point:(nonnull YMKPoint *) point {
    if (self.onMapPress) {
        NSDictionary* data = @{
            @"lat": [NSNumber numberWithDouble:point.latitude],
            @"lon": [NSNumber numberWithDouble:point.longitude],
        };
        self.onMapPress(data);
    }
}

- (void)onMapLongTapWithMap:(nonnull YMKMap *) map
                      point:(nonnull YMKPoint *) point {
    if (self.onMapLongPress) {
        NSDictionary* data = @{
            @"lat": [NSNumber numberWithDouble:point.latitude],
            @"lon": [NSNumber numberWithDouble:point.longitude],
        };
        self.onMapLongPress(data);
    }
}

// utils
+ (UIColor*)colorFromHexString:(NSString*) hexString {
    unsigned rgbValue = 0;
    NSScanner *scanner = [NSScanner scannerWithString:hexString];
    [scanner setScanLocation:1];
    [scanner scanHexInt:&rgbValue];
    return [UIColor colorWithRed:((rgbValue & 0xFF0000) >> 16)/255.0 green:((rgbValue & 0xFF00) >> 8)/255.0 blue:(rgbValue & 0xFF)/255.0 alpha:1.0];
}

+ (NSString*)hexStringFromColor:(UIColor *) color {
    const CGFloat *components = CGColorGetComponents(color.CGColor);
    CGFloat r = components[0];
    CGFloat g = components[1];
    CGFloat b = components[2];
    return [NSString stringWithFormat:@"#%02lX%02lX%02lX", lroundf(r * 255), lroundf(g * 255), lroundf(b * 255)];
}

// children
- (void)addSubview:(UIView *) view {
    [super addSubview:view];
}

- (void)insertReactSubview:(UIView<RCTComponent>*) subview atIndex:(NSInteger) atIndex {
     if ([subview isKindOfClass:[YamapMarkerView class]]) {
        YMKMapObjectCollection *objects = self.mapWindow.map.mapObjects;
        YamapMarkerView* marker = (YamapMarkerView*) subview;
        if (userClusters) {
            YMKPlacemarkMapObject* obj = [clusterCollection addPlacemarkWithPoint:[marker getPoint]];
            [marker setMapObject:obj];
            [clusterCollection clusterPlacemarksWithClusterRadius:(50) minZoom:(12)];
        } else {
            YMKPlacemarkMapObject* obj = [objects addPlacemarkWithPoint:[marker getPoint]];
            [marker setMapObject:obj];
        }
    }
    [_reactSubviews insertObject:subview atIndex:atIndex];
    [super insertReactSubview:subview atIndex:atIndex];
}

- (void)removeReactSubview:(UIView<RCTComponent>*) subview {
     if ([subview isKindOfClass:[YamapMarkerView class]]) {
        YMKMapObjectCollection *objects = self.mapWindow.map.mapObjects;
        YamapMarkerView* marker = (YamapMarkerView*) subview;
        if (userClusters) {
            [clusterCollection removeWithPlacemark:[marker getMapObject]];
        } else {
        [objects removeWithMapObject:[marker getMapObject]];
        }
    }
    [_reactSubviews removeObject:subview];
    [super removeReactSubview: subview];
}

-(UIImage*)clusterImage:(NSNumber*) clusterSize {
    float FONT_SIZE = 45;
    float MARGIN_SIZE = 9;
    float STROKE_SIZE = 9;
    NSString *text = [clusterSize stringValue];
    UIFont *font = [UIFont systemFontOfSize:FONT_SIZE];
    CGSize size = [text sizeWithFont:font];
    float textRadius = sqrt(size.height * size.height + size.width * size.width) / 2;
    float internalRadius = textRadius + MARGIN_SIZE;
    float externalRadius = internalRadius + STROKE_SIZE;
    UIImage *someImageView = [UIImage alloc];
    // This function returns a newImage, based on image, that has been:
       // - scaled to fit in (CGRect) rect
       // - and cropped within a circle of radius: rectWidth/2

       //Create the bitmap graphics context
       UIGraphicsBeginImageContextWithOptions(CGSizeMake(externalRadius*2, externalRadius*2), NO, 1.0);
    CGContextRef context = UIGraphicsGetCurrentContext();
    CGContextSetFillColorWithColor(context, [clusterColor CGColor]);
    CGContextFillEllipseInRect(context, CGRectMake(0, 0, externalRadius*2, externalRadius*2));
    CGContextSetFillColorWithColor(context, [UIColor.whiteColor CGColor]);
    CGContextFillEllipseInRect(context, CGRectMake(STROKE_SIZE, STROKE_SIZE, internalRadius*2, internalRadius*2));
    [text drawInRect:CGRectMake(externalRadius - size.width/2, externalRadius - size.height/2, externalRadius, externalRadius) withAttributes:@{NSFontAttributeName: font, NSForegroundColorAttributeName: UIColor.blackColor }];
       UIImage *newImage = UIGraphicsGetImageFromCurrentImageContext();
       UIGraphicsEndImageContext();

       return newImage;
}

- (void)onClusterAddedWithCluster:(nonnull YMKCluster *)cluster {
    NSNumber *myNum = @([cluster size]);
    [[cluster appearance] setIconWithImage:[self clusterImage:myNum]];
    [cluster addClusterTapListenerWithClusterTapListener:self];
}

- (BOOL)onClusterTapWithCluster:(nonnull YMKCluster *)cluster {
    [self fitMarkers:[cluster placemarks]];
    return YES;
}


@synthesize reactTag;

@end
