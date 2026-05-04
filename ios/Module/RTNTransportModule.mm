#import "RTNTransportModule.h"

#ifdef USE_YANDEX_MAPS_FULL

#import "ColorUtil.h"
#import "ParkRouter.h"
#import "../Util/RCTConvert+Yamap.mm"

#import <YandexMapsMobile/YMKTransport.h>
#import <YandexMapsMobile/YMKDirections.h>
#import <YandexMapsMobile/YMKTransitOptions.h>
#import <YandexMapsMobile/YMKMasstransitRoute.h>
#import <YandexMapsMobile/YMKDrivingRoute.h>
#import <YandexMapsMobile/YMKSubpolylineHelper.h>
#import <YandexMapsMobile/YMKDrivingVehicleOptions.h>

#define UIColorFromRGB(rgbValue) [UIColor colorWithRed:((float)((rgbValue & 0xFF0000) >> 16))/255.0 green:((float)((rgbValue & 0xFF00) >> 8))/255.0 blue:((float)(rgbValue & 0xFF))/255.0 alpha:1.0]

#endif

@implementation RTNTransportModule

#ifdef USE_YANDEX_MAPS_FULL

YMKMasstransitSession *masstransitSession;
YMKMasstransitSession *walkSession;
YMKDrivingSession *drivingSession;
YMKTransitOptions *transitOptions;
YMKMasstransitSessionRouteHandler routeHandler;
YMKRouteOptions *_routeOptions;
YMKTimeOptions *_timeOptions;

NSMutableDictionary *vehicleColors;

- (instancetype)init
{
    self = [super init];
    if (self) {
        _timeOptions = [[YMKTimeOptions alloc] init];
        transitOptions = [YMKTransitOptions transitOptionsWithAvoid:YMKFilterVehicleTypesNone timeOptions:_timeOptions];
        _routeOptions = [YMKRouteOptions routeOptionsWithFitnessOptions:[YMKFitnessOptions fitnessOptionsWithAvoidSteep:false avoidStairs:false]];
        vehicleColors = [[NSMutableDictionary alloc] init];
        [vehicleColors setObject:@"#59ACFF" forKey:@"bus"];
        [vehicleColors setObject:@"#7D60BD" forKey:@"minibus"];
        [vehicleColors setObject:@"#F8634F" forKey:@"railway"];
        [vehicleColors setObject:@"#C86DD7" forKey:@"tramway"];
        [vehicleColors setObject:@"#3023AE" forKey:@"suburban"];
        [vehicleColors setObject:@"#BDCCDC" forKey:@"underground"];
        [vehicleColors setObject:@"#55CfDC" forKey:@"trolleybus"];
        [vehicleColors setObject:@"#2d9da8" forKey:@"walk"];
    }
    return self;
}

- (dispatch_queue_t)methodQueue {
    return dispatch_get_main_queue();
}

- (void)findRoutesImpl:(nonnull NSArray *)points vehicles:(nonnull NSArray *)vehicles resolve:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
    
    NSArray<YMKPoint *> *_points = [RCTConvert YMKPointArray:points];
    NSMutableArray<YMKRequestPoint *> *requestPoints = [[NSMutableArray alloc] init];
    
    for (int i = 0; i < [_points count]; ++i) {
        YMKRequestPoint *requestPoint = [YMKRequestPoint requestPointWithPoint:[_points objectAtIndex:i] type:YMKRequestPointTypeWaypoint pointContext:nil drivingArrivalPointId:nil indoorLevelId:nil];
        [requestPoints addObject:requestPoint];
    }

    if ([vehicles count] == 1 && [[vehicles objectAtIndex:0] isEqualToString:@"car"]) {
        YMKDrivingOptions *drivingOptions = [[YMKDrivingOptions alloc] init];
        YMKDrivingVehicleOptions *vehicleOptions = [[YMKDrivingVehicleOptions alloc] init];
        
        YMKDrivingRouter *drivingRouter = [[YMKDirectionsFactory instance] createDrivingRouterWithType: YMKDrivingRouterTypeCombined];
        drivingSession = [drivingRouter requestRoutesWithPoints:requestPoints drivingOptions:drivingOptions vehicleOptions:vehicleOptions routeHandler:^(NSArray<YMKDrivingRoute *> *routes, NSError *error) {
            if (error != nil) {
                reject(@"drivingRouter requestRoutesWithPoints error", error.userInfo.description, error);
                return;
            }
            
            NSMutableDictionary* response = [[NSMutableDictionary alloc] init];
            [response setValue:@"status" forKey:@"success"];
            NSMutableArray* jsonRoutes = [[NSMutableArray alloc] init];
            
            for (int i = 0; i < [routes count]; ++i) {
                YMKDrivingRoute *_route = [routes objectAtIndex:i];
                NSMutableDictionary *jsonRoute = [[NSMutableDictionary alloc] init];
                [jsonRoute setValue:[NSString stringWithFormat:@"%d", i] forKey:@"id"];
                NSMutableArray* sections = [[NSMutableArray alloc] init];
                NSArray<YMKDrivingSection *> *_sections = [_route sections];
                for (int j = 0; j < [_sections count]; ++j) {
                    NSDictionary *jsonSection = [self convertDrivingRouteSection:_route withSection: [_sections objectAtIndex:j]];
                    [sections addObject:jsonSection];
                }
                [jsonRoute setValue:sections forKey:@"sections"];
                [jsonRoutes addObject:jsonRoute];
            }
            
            [response setValue:jsonRoutes forKey:@"routes"];
            resolve(response);
        }];
        return;
    }
    
    YMKMasstransitSessionRouteHandler _routeHandler = ^(NSArray<YMKMasstransitRoute *> *routes, NSError *error) {
        if (error != nil) {
            reject(@"_routeHandler error", error.userInfo.description, error);
            return;
        }
        NSMutableDictionary* response = [[NSMutableDictionary alloc] init];
        [response setValue:@"success" forKey:@"status"];
        NSMutableArray *jsonRoutes = [[NSMutableArray alloc] init];
        for (int i = 0; i < [routes count]; ++i) {
            YMKMasstransitRoute *_route = [routes objectAtIndex:i];
            NSMutableDictionary *jsonRoute = [[NSMutableDictionary alloc] init];
            
            [jsonRoute setValue:[NSString stringWithFormat:@"%d", i] forKey:@"id"];
            NSMutableArray *sections = [[NSMutableArray alloc] init];
            NSArray<YMKMasstransitSection *> *_sections = [_route sections];
            for (int j = 0; j < [_sections count]; ++j) {
                NSDictionary *jsonSection = [self convertRouteSection:_route withSection: [_sections objectAtIndex:j]];
                [sections addObject:jsonSection];
            }
            [jsonRoute setValue:sections forKey:@"sections"];
            [jsonRoutes addObject:jsonRoute];
        }
        [response setValue:jsonRoutes forKey:@"routes"];
        resolve(response);
        return;
    };
    
    if ([vehicles count] == 0) {
        YMKPedestrianRouter *pedestrianRouter = [[YMKTransportFactory instance] createPedestrianRouter];
        walkSession = [pedestrianRouter requestRoutesWithPoints:requestPoints timeOptions:_timeOptions routeOptions:_routeOptions routeHandler:_routeHandler];
        return;
    }
    
    YMKTransitOptions *_transitOptions = [YMKTransitOptions transitOptionsWithAvoid:YMKFilterVehicleTypesNone timeOptions:_timeOptions];
    YMKMasstransitRouter *masstransitRouter = [[YMKTransportFactory instance] createMasstransitRouter];
    masstransitSession = [masstransitRouter requestRoutesWithPoints:requestPoints transitOptions:_transitOptions routeOptions:_routeOptions routeHandler:_routeHandler];
}


- (void)findParkRoutesImpl:(JS::NativeTransportModule::Point &)start
                       end:(JS::NativeTransportModule::Point &)end
                     zones:(NSArray *)zones
                   resolve:(RCTPromiseResolveBlock)resolve
                    reject:(RCTPromiseRejectBlock)reject {
//    ParkRouter *router = [[ParkRouter alloc] initWithMapView:self.mapView];
//
//    [router buildRouteFrom:startPoint
//                        to:endPoint
//             outerBoundary:parkPolygon
//            forbiddenZones:@[lakePolygon, flowerZone]
//                completion:^(YMKRoute *route) {
//
//        if (route) {
//            [self.mapView.map.mapObjects addPolylineWithPolyline:route.geometry];
//        }
//    }];
}


- (NSDictionary*)convertDrivingRouteSection:(YMKDrivingRoute*)route withSection:(YMKDrivingSection*)section {
    int routeIndex = 0;
    YMKDrivingWeight *routeWeight = route.metadata.weight;
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
    YMKPolyline* subpolyline = [YMKSubpolylineHelper subpolylineWithPolyline:route.geometry subpolyline:section.geometry];
    
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

- (NSDictionary *)convertRouteSection:(YMKMasstransitRoute *)route withSection:(YMKMasstransitSection *)section {
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
        [stops addObject:stop.metadata.stop.name];
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
                        color = [ColorUtil colorFromHexString:vehicleColors[type]];
                    } else {
                        color = UIColor.blackColor;
                    }
                }
                [routeMetadata setObject:[ColorUtil hexStringFromColor:color] forKey:@"sectionColor"];
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
    NSMutableArray *points = [[NSMutableArray alloc] init];
    YMKPolyline* subpolyline = [YMKSubpolylineHelper subpolylineWithPolyline:route.geometry subpolyline:section.geometry];
    
    for (int i = 0; i < [subpolyline.points count]; ++i) {
        YMKPoint *point = [subpolyline.points objectAtIndex:i];
        NSMutableDictionary *jsonPoint = [[NSMutableDictionary alloc] init];
        [jsonPoint setValue:[NSNumber numberWithDouble:point.latitude] forKey:@"lat"];
        [jsonPoint setValue:[NSNumber numberWithDouble:point.longitude] forKey:@"lon"];
        [points addObject:jsonPoint];
    }
    
    [routeMetadata setValue:points forKey:@"points"];
    
    return routeMetadata;
}

#else

- (void)findRoutesImpl:(nonnull NSArray *)points vehicles:(nonnull NSArray *)vehicles resolve:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
    reject(@"TRANSPORT_FAILED", @"TRANSPORT module is not available in Lite version", nil);
}

#endif

#ifdef RCT_NEW_ARCH_ENABLED

// New architecture

- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:(const facebook::react::ObjCTurboModule::InitParams &)params {
    return std::make_shared<facebook::react::NativeTransportModuleSpecJSI>(params);
}

- (void)findRoutes:(nonnull NSArray *)points vehicles:(nonnull NSArray *)vehicles resolve:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
    [self findRoutesImpl:points vehicles:vehicles resolve:resolve reject:reject];
}

- (void)findParkRoutes:(JS::NativeTransportModule::Point &)start
                   end:(JS::NativeTransportModule::Point &)end
                 zones:(NSArray *)zones
               resolve:(RCTPromiseResolveBlock)resolve
                reject:(RCTPromiseRejectBlock)reject {
    [self findParkRoutesImpl:start end:end zones:zones resolve:resolve reject:reject];
}

#else

// Old architecture

RCT_EXPORT_METHOD(findRoutes:(nonnull NSArray *)points vehicles:(nonnull NSArray *)vehicles resolve:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject) {
    [self findRoutesImpl:points vehicles:vehicles resolve:resolve reject:reject];
}

RCT_EXPORT_METHOD(findParkRoutes:(JS::NativeTransportModule::Point &)start
                  end:(JS::NativeTransportModule::Point &)end
                zones:(NSArray *)zones
              resolve:(RCTPromiseResolveBlock)resolve
               reject:(RCTPromiseRejectBlock)reject) {
    [self findParkRoutesImpl:start end:end zones:zones resolve:resolve reject:reject];
}

#endif


RCT_EXPORT_MODULE()

@end
