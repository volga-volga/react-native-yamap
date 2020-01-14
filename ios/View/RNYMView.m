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

#import "YamapPolygonView.h"
#import "YamapPolylineView.h"
#import "RNYMView.h"


#define ANDROID_COLOR(c) [UIColor colorWithRed:((c>>16)&0xFF)/255.0 green:((c>>8)&0xFF)/255.0 blue:((c)&0xFF)/255.0  alpha:((c>>24)&0xFF)/255.0]

#define UIColorFromRGB(rgbValue) [UIColor colorWithRed:((float)((rgbValue & 0xFF0000) >> 16))/255.0 green:((float)((rgbValue & 0xFF00) >> 8))/255.0 blue:((float)(rgbValue & 0xFF))/255.0 alpha:1.0]

@implementation RNYMView {
    YMKMasstransitSession *masstransitSession;
    YMKMasstransitSession *walkSession;
    YMKMasstransitRouter *masstransitRouter;
    YMKPedestrianRouter *pedestrianRouter;
    YMKMasstransitOptions *masstransitOptions;
    void (^routeHandler)(NSArray<YMKMasstransitRoute *>*, NSError *);

    NSMutableArray* _reactSubviews;

    NSMutableArray *routes;
    NSMutableArray *currentRouteInfo;
    NSMutableArray<YMKRequestPoint *>* lastKnownRoutePoints;
    NSMutableArray<RNMarker *>* lastKnownMarkers;
    NSMutableArray<YMKPlacemarkMapObject*>* placemarkObjects;
    YMKUserLocationView* userLocationView;
    NSMutableDictionary *vehicleColors;
    UIImage* userLocationImage;
    NSArray *acceptVehicleTypes;
}

- (instancetype)init {
    self = [super init];
    _reactSubviews = [[NSMutableArray alloc] init];
    masstransitRouter = [[YMKTransport sharedInstance] createMasstransitRouter];
    pedestrianRouter = [[YMKTransport sharedInstance] createPedestrianRouter];
    masstransitOptions = [YMKMasstransitOptions masstransitOptionsWithAvoidTypes:[[NSArray alloc] init] acceptTypes:[[NSArray alloc] init] timeOptions:[[YMKTimeOptions alloc] init]];
    acceptVehicleTypes = [[NSMutableArray<NSString *> alloc] init];
    routes = [[NSMutableArray alloc] init];
    currentRouteInfo = [[NSMutableArray alloc] init];
    lastKnownRoutePoints = [[NSMutableArray alloc] init];
    placemarkObjects = [[NSMutableArray alloc] init];
    vehicleColors = [[NSMutableDictionary alloc] init];
    [vehicleColors setObject:@"#59ACFF" forKey:@"bus"];
    [vehicleColors setObject:@"#7D60BD" forKey:@"minibus"];
    [vehicleColors setObject:@"#F8634F" forKey:@"railway"];
    [vehicleColors setObject:@"#C86DD7" forKey:@"tramway"];
    [vehicleColors setObject:@"#3023AE" forKey:@"suburban"];
    [vehicleColors setObject:@"#BDCCDC" forKey:@"underground"];
    [vehicleColors setObject:@"#55CfDC" forKey:@"trolleybus"];
    [vehicleColors setObject:@"#2d9da8" forKey:@"walk"];
    __weak RNYMView *weakSelf = self;
    routeHandler = ^(NSArray<YMKMasstransitRoute *> *routes, NSError *error) {
        if (error != nil) return;
        RNYMView *strongSelf = weakSelf;
        if ([routes count] > 0) {
            if ([strongSelf->acceptVehicleTypes containsObject:@"walk"]) {
                [strongSelf processRouteWithRoute:routes[0] index:0];
            } else {
                for (int i = 0; i < [routes count]; i++) {
                    [strongSelf processRouteWithRoute:routes[i] index:i];
                }
            }
            [strongSelf onReceiveNativeEvent:strongSelf->routes];
            strongSelf->routes = [[NSMutableArray alloc] init];
        }
    };
    YMKMapKit* inst = [YMKMapKit sharedInstance];
    YMKUserLocationLayer *userLayer = [inst createUserLocationLayerWithMapWindow: self.mapWindow];
    userLocationView = nil;
    userLocationImage = nil;
    [userLayer setVisibleWithOn:YES];
    [userLayer setObjectListenerWithObjectListener: self];
    return self;
}

-(void)processRouteWithRoute:(YMKMasstransitRoute *)route index:(int) index {
    BOOL isRouteBelongToAcceptedVehicleList = false;
    BOOL isWalkRoute = true;
    for (YMKMasstransitSection *section in route.sections) {
        if (section.metadata.data.transports != nil) {
            isWalkRoute = false;
            for (YMKMasstransitTransport *transport in section.metadata.data.transports) {
                for (NSString *type in transport.line.vehicleTypes) {
                    if ([self->acceptVehicleTypes containsObject:type]) {
                        isRouteBelongToAcceptedVehicleList = true;
                        break;
                    }
                }
            }
        }
    }
    if (isRouteBelongToAcceptedVehicleList || isWalkRoute) {
        for (YMKMasstransitSection *section in route.sections) {
            [self drawSection:section withGeometry:YMKMakeSubpolyline(route.geometry, section.geometry) withWeight:route.metadata.weight withIndex:index];
        }
        [self->routes addObject:self->currentRouteInfo];
        self->currentRouteInfo = [[NSMutableArray alloc] init];
    }
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

-(void)drawSection:(YMKMasstransitSection *) section withGeometry:(YMKPolyline *) geometry withWeight:(YMKMasstransitWeight *) routeWeight withIndex:(int) routeIndex {
    if ([acceptVehicleTypes count] == 0) {
        [self removeAllSections];
        return;
    }
    YMKMapObjectCollection *objects = self.mapWindow.map.mapObjects;
    YMKMasstransitSectionMetadataSectionData *data = section.metadata.data;
    YMKPolylineMapObject *polylineMapObject = [[objects addCollection] addPolylineWithPolyline:geometry];
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
                [polylineMapObject setStrokeColor:color];
            }
        }
    } else {
        [self setDashPolyline:polylineMapObject];
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
    [self->currentRouteInfo addObject:routeMetadata];
}

-(void) setDashPolyline:(YMKPolylineMapObject *)polylineMapObject {
    [polylineMapObject setDashLength:8.0];
    [polylineMapObject setGapLength:11.0];
    [polylineMapObject setStrokeWidth:2.0];
    [polylineMapObject setStrokeColor:[RNYMView colorFromHexString:[vehicleColors valueForKey:@"walk"]]];
}


-(void)onReceiveNativeEvent:(NSMutableArray *)routes {
    if (self.onRouteFound) self.onRouteFound(@{@"routes": routes});
}

-(void) removeAllSections {
    [self.mapWindow.map.mapObjects clear];
    [placemarkObjects removeAllObjects];
    if (lastKnownMarkers != nil) [self setMarkers:lastKnownMarkers];
}

// ref
-(void) setCenter:(YMKPoint*) center withZoom:(float) zoom {
    [self.mapWindow.map moveWithCameraPosition:[YMKCameraPosition cameraPositionWithTarget:center zoom:zoom azimuth:0 tilt:0]];
}

-(void) fitAllMarkers {
    if ([lastKnownMarkers count] == 1) {
        YMKPoint *center = [YMKPoint pointWithLatitude:lastKnownMarkers[0].lat longitude:lastKnownMarkers[0].lon];
        [self.mapWindow.map moveWithCameraPosition:[YMKCameraPosition cameraPositionWithTarget:center zoom:15 azimuth:0 tilt:0]];
        return;
    }
    double minLon = lastKnownMarkers[0].lon, maxLon = lastKnownMarkers[0].lon;
    double minLat  = lastKnownMarkers[0].lat, maxLat = lastKnownMarkers[0].lat;
    for (int i = 0; i < [lastKnownMarkers count]; i++) {
        if (lastKnownMarkers[i].lon > maxLon) maxLon = lastKnownMarkers[i].lon;
        if (lastKnownMarkers[i].lon < minLon) minLon = lastKnownMarkers[i].lon;
        if (lastKnownMarkers[i].lat > maxLat) maxLat = lastKnownMarkers[i].lat;
        if (lastKnownMarkers[i].lat < minLat) minLat = lastKnownMarkers[i].lat;
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
//    YMKCircle *circle = [YMKCircle circleWithCenter:rectCenter radius:distance];
//    [map.mapWindow.map.mapObjects addCircleWithCircle:circle strokeColor:UIColor.redColor strokeWidth:5 fillColor:UIColor.clearColor];
    YMKCameraPosition *cameraPosition = [self.mapWindow.map cameraPositionWithBoundingBox:boundingBox];
    cameraPosition = [YMKCameraPosition cameraPositionWithTarget:cameraPosition.target zoom:zoom azimuth:cameraPosition.azimuth tilt:cameraPosition.tilt];
    [self.mapWindow.map moveWithCameraPosition:cameraPosition animationType:[YMKAnimation animationWithType:YMKAnimationTypeSmooth duration:1.0] cameraCallback:^(BOOL completed){}];
}

-(void) actualizePlacemark:(YMKPlacemarkMapObject*) placemark withMarker:(RNMarker*) marker {
    [placemark setGeometry:[YMKPoint pointWithLatitude:marker.lat longitude:marker.lon]];
    if (![marker.uri isEqualToString:@""]) {
        UIImage *icon = [self resolveUIImage: marker.uri];
        if (icon != nil) {
            [placemark setIconWithImage:icon];
        }
    }
    YMKIconStyle *style = [[YMKIconStyle alloc] init];
    style.zIndex = [NSNumber numberWithInt:marker.zIndex];
    [placemark setIconStyleWithStyle:style];
}

// props
-(void) setMarkers:(NSMutableArray<RNMarker *> *) markerList {
    YMKMapObjectCollection *objects = self.mapWindow.map.mapObjects;
    lastKnownMarkers = markerList;
    NSMutableArray<NSNumber*>* statuses = [[NSMutableArray alloc] init];
    NSNumber* _false = [[NSNumber alloc] initWithInt:0];
    NSNumber* _true = [[NSNumber alloc] initWithInt:1];
    for (int i = 0; i < markerList.count; ++i) {
        [statuses insertObject:_false atIndex:i];
    }
    for (int i = 0; i < placemarkObjects.count; ++i) {
        YMKPlacemarkMapObject* obj = [placemarkObjects objectAtIndex:i];
        if (obj != nil) {
            NSDictionary* json = obj.userData;
            NSString* _id =  [json valueForKey:@"id"];
            bool removed = true;
            for (int j = 0; j < markerList.count; ++j) {
                RNMarker* marker = [markerList objectAtIndex:j];
                if ([marker._id isEqual:_id]) {
                    removed = false;
                    [statuses replaceObjectAtIndex:j withObject:_true];
                    [self actualizePlacemark:obj withMarker:marker];
                }
            }
            if (removed) {
                [objects removeWithMapObject:obj];
                [placemarkObjects removeObject:obj];
                --i;
            }
        }
    }
    for (int i = 0; i < markerList.count; ++i) {
        NSNumber* status = [statuses objectAtIndex:i];
        if ([status isEqual:_false]) {
            RNMarker* marker = [markerList objectAtIndex:i];
            YMKPlacemarkMapObject *placemark = [objects addPlacemarkWithPoint:[YMKPoint pointWithLatitude:marker.lat longitude:marker.lon]];
            [placemarkObjects addObject:placemark];
            [placemark setUserData:@{@"id": marker._id}];
            [placemark addTapListenerWithTapListener:self];
            [self actualizePlacemark:placemark withMarker:marker];
        }
    }
}

-(void) clearRoute {
    lastKnownRoutePoints = nil;
    [self removeAllSections];
}

-(void) setRouteWithStart:(YMKRequestPoint*) start end:(YMKRequestPoint*) end {
    [self requestRoute:[NSMutableArray arrayWithObjects:start, end, nil]];
}

-(void) setAcceptedVehicleTypes:(NSArray*) _acceptVehicleTypes {
    acceptVehicleTypes = _acceptVehicleTypes;
    [self removeAllSections];
    if ([acceptVehicleTypes count] == 0) {
        [self onReceiveNativeEvent: [[NSMutableArray alloc] init]];
        return;
    }
    if ([lastKnownRoutePoints count] > 0) {
        [self requestRoute:lastKnownRoutePoints];
    }
}

-(void) setVehicleColors:(NSDictionary*) _vehicleColors {
    for(NSString *key in _vehicleColors) {
        [vehicleColors setValue:[_vehicleColors valueForKey:key] forKey:key];
    }
}

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

-(void) requestRoute:(NSMutableArray<YMKRequestPoint *> *) points {
    lastKnownRoutePoints = points;
    if ([acceptVehicleTypes count] > 0) {
        if ([acceptVehicleTypes containsObject:@"walk"]) {
            walkSession = [pedestrianRouter requestRoutesWithPoints:points timeOptions:[[YMKTimeOptions alloc] init] routeHandler:routeHandler];
            return;
        }
        masstransitSession = [masstransitRouter requestRoutesWithPoints:points masstransitOptions:masstransitOptions routeHandler:routeHandler];
    } else {
        [self clearRoute];
    }
}

// object tap listener
- (BOOL)onMapObjectTapWithMapObject:(nonnull YMKMapObject *)mapObject point:(nonnull YMKPoint *)point {
    if (self.onMarkerPress) self.onMarkerPress(@{@"id": [mapObject.userData valueForKey:@"id"]});
    return YES;
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

- (void)insertReactSubview:(id<RCTComponent>)subview atIndex:(NSInteger)atIndex {
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
    } else {
        NSArray<id<RCTComponent>> *childSubviews = [subview reactSubviews];
        for (int i = 0; i < childSubviews.count; i++) {
          [self insertReactSubview:(UIView *)childSubviews[i] atIndex:atIndex];
        }
    }
    [_reactSubviews insertObject:(UIView *)subview atIndex:(NSUInteger) atIndex];
    [super insertReactSubview:subview atIndex:atIndex];
}

- (void)removeReactSubview:(id<RCTComponent>)subview {
    if ([subview isKindOfClass:[YamapPolygonView class]]) {
        YMKMapObjectCollection *objects = self.mapWindow.map.mapObjects;
        YamapPolygonView* polygon = (YamapPolygonView*) subview;
        [objects removeWithMapObject:[polygon getMapObject]];
    } else if ([subview isKindOfClass:[YamapPolylineView class]]) {
        YMKMapObjectCollection *objects = self.mapWindow.map.mapObjects;
        YamapPolylineView* polyline = (YamapPolylineView*) subview;
        [objects removeWithMapObject:[polyline getMapObject]];
    } else {
        NSArray<id<RCTComponent>> *childSubviews = [subview reactSubviews];
        for (int i = 0; i < childSubviews.count; i++) {
          [self removeReactSubview:(UIView *)childSubviews[i]];
        }
    }
    [_reactSubviews removeObject:(UIView *)subview];
    [super removeReactSubview: subview];
}

@end
