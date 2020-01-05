#import <React/RCTViewManager.h>
#import <MapKit/MapKit.h>
#import <math.h>
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
#import "Converter/RCTConvert+Yamap.m"
#import "Models/RNMarker.h"
#import "YamapView.h"
#import "RNYamap.h"
#import "View/RNYMView.h"

#ifndef MAX
#import <NSObjCRuntime.h>
#endif

#define ANDROID_COLOR(c) [UIColor colorWithRed:((c>>16)&0xFF)/255.0 green:((c>>8)&0xFF)/255.0 blue:((c)&0xFF)/255.0  alpha:((c>>24)&0xFF)/255.0]

#define UIColorFromRGB(rgbValue) [UIColor colorWithRed:((float)((rgbValue & 0xFF0000) >> 16))/255.0 green:((float)((rgbValue & 0xFF00) >> 8))/255.0 blue:((float)(rgbValue & 0xFF))/255.0 alpha:1.0]

@implementation YamapView {
    YMKMasstransitSession *masstransitSession;
    YMKMasstransitSession *walkSession;
    YMKMasstransitRouter *masstransitRouter;
    YMKPedestrianRouter *pedestrianRouter;
    YMKMasstransitOptions *masstransitOptions;
    void (^routeHandler)(NSArray<YMKMasstransitRoute *>*, NSError *);

    NSMutableArray *routes;
    NSMutableArray *currentRouteInfo;
    NSMutableArray<YMKRequestPoint *> *lastKnownRoutePoints;
    NSMutableArray<RNMarker *> *lastKnownMarkers;
    YMKUserLocationView* userLocationView;
    NSMutableDictionary *vehicleColors;
    UIImage* userLocationImage;
    NSArray *acceptVehicleTypes;
}

@synthesize map;

RCT_EXPORT_MODULE()

- (NSArray<NSString *> *)supportedEvents {
    return @[@"onMarkerPress", @"onRouteFound"];
}

- (instancetype)init {
    self = [super init];
    if (self) {
        masstransitRouter = [[YMKTransport sharedInstance] createMasstransitRouter];
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

        __weak YamapView *weakSelf = self;

        routeHandler = ^(NSArray<YMKMasstransitRoute *> *routes, NSError *error) {
            if (error != nil) return;

            YamapView *strongSelf = weakSelf;

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
    }
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

- (void)onObjectAddedWithView:(nonnull YMKUserLocationView *)view {
    userLocationView = view;
    [self updateUserIcon];
//    if (yamap.pinIcon != nil) [view.pin setIconWithImage: userLocationImage];
//    if (yamap.arrowIcon != nil) [view.arrow setIconWithImage:[UIImage imageNamed:yamap.arrowIcon]];
//
//    YMKIconStyle *arrowStyle = [[YMKIconStyle alloc] init];
//    YMKIconStyle *pinStyle = [[YMKIconStyle alloc] init];
//    arrowStyle.scale = [[NSNumber alloc] initWithDouble:2];
//    pinStyle.scale = [[NSNumber alloc] initWithDouble:0.5];
//    view.accuracyCircle.fillColor = [UIColor colorWithWhite:1
//                                                      alpha:0];
//    [view.pin setIconStyleWithStyle:pinStyle];
//    [view.arrow setIconStyleWithStyle:arrowStyle];
//    [view.accuracyCircle setFillColor:UIColor.clearColor];
}

- (void)onObjectRemovedWithView:(nonnull YMKUserLocationView *)view {

}

- (void)onObjectUpdatedWithView:(nonnull YMKUserLocationView *)view event:(nonnull YMKObjectEvent *)event {

}

- (BOOL)onMapObjectTapWithMapObject:(nonnull YMKMapObject *)mapObject point:(nonnull YMKPoint *)point {
    if (map.onMarkerPress) map.onMarkerPress(@{@"id": [mapObject.userData valueForKey:@"id"]});
    return YES;
}

- (UIView *_Nullable)view {
    map = [[RNYMView alloc] init];
    userLocationView = nil;
    userLocationImage = nil;
    YMKMapKit* inst = [YMKMapKit sharedInstance];
    YMKUserLocationLayer *userLayer = [inst createUserLocationLayerWithMapWindow: map.mapWindow];
    [userLayer setVisibleWithOn:YES];
    [userLayer setObjectListenerWithObjectListener: self];
    return map;
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

-(void)setMarkers:(NSMutableArray<RNMarker *> *)markerList {
    YMKMapObjectCollection *objects = map.mapWindow.map.mapObjects;
    lastKnownMarkers = markerList;
    // todo: реализовать поведение как на андроиде - без очистки всех объектов
    [objects clear];
    for (RNMarker *marker in markerList) {
        YMKPlacemarkMapObject *placemark = [objects addPlacemarkWithPoint:[YMKPoint pointWithLatitude:marker.lat longitude:marker.lon]];
        if (![marker.uri isEqualToString:@""]) {
            UIImage *icon = [self resolveUIImage: marker.uri];
            if (icon != nil) {
                [placemark setIconWithImage:icon];
            }
        }
        [placemark setUserData:@{@"id": marker._id}];
        YMKIconStyle *style = [[YMKIconStyle alloc] init];
        style.zIndex = [NSNumber numberWithInt:marker.zIndex];
        [placemark setIconStyleWithStyle:style];
        [placemark addTapListenerWithTapListener:self];
    }
}

-(void)drawSection:(YMKMasstransitSection *) section withGeometry:(YMKPolyline *) geometry withWeight:(YMKMasstransitWeight *) routeWeight withIndex:(int) routeIndex {
    if ([acceptVehicleTypes count] == 0) {
        [self removeAllSections];
        return;
    }

    YMKMapObjectCollection *objects = map.mapWindow.map.mapObjects;
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
                        color = [YamapView colorFromHexString:vehicleColors[type]];
                    } else {
                        color = UIColor.blackColor;
                    }
                }
                [routeMetadata setObject:[YamapView hexStringFromColor:color] forKey:@"sectionColor"];
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
    [polylineMapObject setStrokeColor:[YamapView colorFromHexString:[vehicleColors valueForKey:@"walk"]]];
}

-(void)onReceiveNativeEvent:(NSMutableArray *)routes {
    if (map.onRouteFound) map.onRouteFound(@{@"routes": routes});
}

-(void)removeAllSections {
    [map.mapWindow.map.mapObjects clear];
    if (lastKnownMarkers != nil) [self setMarkers:lastKnownMarkers];
}

-(void)_fitAllMarkers: (RNYMView*) _map {
    if ([lastKnownMarkers count] == 1) {
        YMKPoint *center = [YMKPoint pointWithLatitude:lastKnownMarkers[0].lat longitude:lastKnownMarkers[0].lon];
        [_map.mapWindow.map moveWithCameraPosition:[YMKCameraPosition cameraPositionWithTarget:center zoom:15 azimuth:0 tilt:0]];
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

    YMKCameraPosition *cameraPosition = [_map.mapWindow.map cameraPositionWithBoundingBox:boundingBox];
    cameraPosition = [YMKCameraPosition cameraPositionWithTarget:cameraPosition.target zoom:zoom azimuth:cameraPosition.azimuth tilt:cameraPosition.tilt];
    [_map.mapWindow.map moveWithCameraPosition:cameraPosition animationType:[YMKAnimation animationWithType:YMKAnimationTypeSmooth duration:1.0] cameraCallback:^(BOOL completed){}];
}

RCT_EXPORT_VIEW_PROPERTY(onMarkerPress, RCTBubblingEventBlock)

RCT_EXPORT_VIEW_PROPERTY(onRouteFound, RCTBubblingEventBlock)

RCT_CUSTOM_VIEW_PROPERTY (markers, NSArray<YMKPoint>, YMKMapView) {
    [self setMarkers: [RCTConvert Markers:json]];
}

-(void)_setCenter: (YMKMapView*) map center:(NSDictionary*) _center {
    YMKPoint *center = [RCTConvert YMKPoint:_center];
    float zoom = [RCTConvert Zoom:_center];
    [self setCenter: center withZoom:zoom];
}

RCT_CUSTOM_VIEW_PROPERTY(route, NSDictionary, YMKMapView) {
    if (json) {
        NSDictionary *routeDict = [RCTConvert RouteDict:json];
        YMKRequestPoint * start = [YMKRequestPoint requestPointWithPoint:[routeDict objectForKey:@"start"] type: YMKRequestPointTypeWaypoint pointContext:nil];
        YMKRequestPoint * end = [YMKRequestPoint requestPointWithPoint:[routeDict objectForKey:@"end"] type: YMKRequestPointTypeWaypoint pointContext:nil];
        [self requestRoute:[NSMutableArray arrayWithObjects:start, end, nil]];
    } else {
        lastKnownRoutePoints = nil;
        [self removeAllSections];
    }
}

-(void) updateUserIcon {
    if (userLocationView != nil && userLocationImage) {
        [userLocationView.pin setIconWithImage: userLocationImage];
        [userLocationView.arrow setIconWithImage: userLocationImage];
    }
}

RCT_CUSTOM_VIEW_PROPERTY(userLocationIcon, NSString, YMKMapView) {
    if (json) {
        userLocationImage = [self resolveUIImage: json];
        [self updateUserIcon];
    }
}

-(void)setCenter: (YMKPoint*) center withZoom:(float) zoom {
    [map.mapWindow.map moveWithCameraPosition:[YMKCameraPosition cameraPositionWithTarget:center zoom:zoom azimuth:0 tilt:0]];
}

-(void) requestRoute:(NSMutableArray<YMKRequestPoint *> *) points {
    if (points != nil) {
        lastKnownRoutePoints = points;
        if ([acceptVehicleTypes count] > 0) {
            if ([acceptVehicleTypes containsObject:@"walk"]) {
                walkSession = [pedestrianRouter requestRoutesWithPoints:points timeOptions:[[YMKTimeOptions alloc] init] routeHandler:routeHandler];
                return;
            }
            masstransitSession = [masstransitRouter requestRoutesWithPoints:points masstransitOptions:masstransitOptions routeHandler:routeHandler];
        }
    }
}

RCT_CUSTOM_VIEW_PROPERTY(routeColors, YMKPoint, YMKMapView) {
    if (json == nil) return;
    NSDictionary *parsed = [RCTConvert RouteColors:json];
    for(NSString *key in parsed) {
        [vehicleColors setValue:[parsed valueForKey:key] forKey:key];
    }
}

RCT_CUSTOM_VIEW_PROPERTY(vehicles, YMKPoint, YMKMapView) {
    if (json) {
        NSArray *parsed = [RCTConvert Vehicles:json];
        acceptVehicleTypes = parsed;
        [self setMarkers:lastKnownMarkers];

        if ([acceptVehicleTypes count] == 0) return;

        if ([lastKnownRoutePoints count] > 0) {
            if ([acceptVehicleTypes containsObject:@"walk"]) {
                walkSession = [pedestrianRouter requestRoutesWithPoints:lastKnownRoutePoints timeOptions:[[YMKTimeOptions alloc] init] routeHandler:routeHandler];
            } else {
                masstransitSession = [masstransitRouter requestRoutesWithPoints:lastKnownRoutePoints masstransitOptions:masstransitOptions routeHandler:routeHandler];
            }
        }
    }
}

+(UIColor *)colorFromHexString:(NSString *)hexString {
    unsigned rgbValue = 0;
    NSScanner *scanner = [NSScanner scannerWithString:hexString];
    [scanner setScanLocation:1];
    [scanner scanHexInt:&rgbValue];
    return [UIColor colorWithRed:((rgbValue & 0xFF0000) >> 16)/255.0 green:((rgbValue & 0xFF00) >> 8)/255.0 blue:(rgbValue & 0xFF)/255.0 alpha:1.0];
}

+(NSString *)hexStringFromColor:(UIColor *)color {
    const CGFloat *components = CGColorGetComponents(color.CGColor);

    CGFloat r = components[0];
    CGFloat g = components[1];
    CGFloat b = components[2];

    return [NSString stringWithFormat:@"#%02lX%02lX%02lX", lroundf(r * 255), lroundf(g * 255), lroundf(b * 255)];
}

RCT_EXPORT_METHOD(fitAllMarkers:(nonnull NSNumber*) reactTag) {
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *,UIView *> *viewRegistry) {
        RNYMView *view = viewRegistry[reactTag];
        if (!view || ![view isKindOfClass:[RNYMView class]]) {
            RCTLogError(@"Cannot find NativeView with tag #%@", reactTag);
            return;
        }
        [self _fitAllMarkers: view];
    }];
}

RCT_EXPORT_METHOD(setCenter:(nonnull NSNumber*) reactTag json:(NSDictionary*) json) {
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *,UIView *> *viewRegistry) {
        RNYMView *view = viewRegistry[reactTag];
        if (!view || ![view isKindOfClass:[RNYMView class]]) {
            RCTLogError(@"Cannot find NativeView with tag #%@", reactTag);
            return;
        }
        [self _setCenter: view center:json];
    }];
}
@end
