#import "RTNTransportModule.h"
#import <math.h>

#ifdef USE_YANDEX_MAPS_FULL

#import "ColorUtil.h"
#import "../Util/RCTConvert+Yamap.mm"

#import <YandexMapsMobile/YMKTransport.h>
#import <YandexMapsMobile/YMKDirections.h>
#import <YandexMapsMobile/YMKTransitOptions.h>
#import <YandexMapsMobile/YMKMasstransitRoute.h>
#import <YandexMapsMobile/YMKDrivingRoute.h>
#import <YandexMapsMobile/YMKSubpolylineHelper.h>
#import <YandexMapsMobile/YMKDrivingVehicleOptions.h>
#import <YandexMapsMobile/YMKRequestPoint.h>
#import <YandexMapsMobile/YMKOptions.h>

#define UIColorFromRGB(rgbValue) [UIColor colorWithRed:((float)((rgbValue & 0xFF0000) >> 16))/255.0 green:((float)((rgbValue & 0xFF00) >> 8))/255.0 blue:((float)(rgbValue & 0xFF))/255.0 alpha:1.0]

#endif

@implementation RTNTransportModule

#ifdef USE_YANDEX_MAPS_FULL

static const NSInteger kMaxParkRouteIterations = 10;
static const double kParkRouteBufferMeters = 5.0;
static const double kDetourEpsilonDegrees = 1e-6;

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
            [response setValue:@"success" forKey:@"status"];
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
    YMKPoint *startPoint = [YMKPoint pointWithLatitude:start.lat() longitude:start.lon()];
    YMKPoint *endPoint = [YMKPoint pointWithLatitude:end.lat() longitude:end.lon()];
    NSArray<NSArray<YMKPoint *> *> *parsedZones = [self parseZones:zones];

    if ([parsedZones count] == 0) {
        reject(@"findParkRoutesError", @"zones should contain at least one polygon", nil);
        return;
    }

    [self requestParkRouteFrom:startPoint
                           end:endPoint
                         zones:parsedZones
                       detours:@[]
                     iteration:0
                       resolve:resolve
                        reject:reject];
}

- (void)requestParkRouteFrom:(YMKPoint *)startPoint
                         end:(YMKPoint *)endPoint
                       zones:(NSArray<NSArray<YMKPoint *> *> *)zones
                     detours:(NSArray<YMKPoint *> *)detours
                   iteration:(NSInteger)iteration
                     resolve:(RCTPromiseResolveBlock)resolve
                      reject:(RCTPromiseRejectBlock)reject {
    if (iteration >= kMaxParkRouteIterations) {
        reject(@"findParkRoutesError", @"failed to build route around restricted zones", nil);
        return;
    }

    NSMutableArray<YMKRequestPoint *> *requestPoints = [[NSMutableArray alloc] init];
    [requestPoints addObject:[YMKRequestPoint requestPointWithPoint:startPoint
                                                               type:YMKRequestPointTypeWaypoint
                                                       pointContext:nil
                                               drivingArrivalPointId:nil
                                                       indoorLevelId:nil]];
    for (YMKPoint *detour in detours) {
        [requestPoints addObject:[YMKRequestPoint requestPointWithPoint:detour
                                                                   type:YMKRequestPointTypeWaypoint
                                                           pointContext:nil
                                                   drivingArrivalPointId:nil
                                                           indoorLevelId:nil]];
    }
    [requestPoints addObject:[YMKRequestPoint requestPointWithPoint:endPoint
                                                               type:YMKRequestPointTypeWaypoint
                                                       pointContext:nil
                                               drivingArrivalPointId:nil
                                                       indoorLevelId:nil]];

    YMKPedestrianRouter *pedestrianRouter = [[YMKTransportFactory instance] createPedestrianRouter];
    walkSession = [pedestrianRouter requestRoutesWithPoints:requestPoints
                                                timeOptions:_timeOptions
                                               routeOptions:_routeOptions
                                               routeHandler:^(NSArray<YMKMasstransitRoute *> *routes, NSError *error) {
        if (error != nil) {
            reject(@"findParkRoutesError", error.userInfo.description, error);
            return;
        }

        YMKMasstransitRoute *route = routes.firstObject;
        if (route == nil) {
            reject(@"findParkRoutesError", @"empty route response", nil);
            return;
        }

        NSArray<YMKPoint *> *intersectedZone = [self firstIntersectedZoneForRoute:route zones:zones];
        if (intersectedZone == nil) {
            NSMutableDictionary *response = [[NSMutableDictionary alloc] init];
            [response setValue:@"success" forKey:@"status"];

            NSMutableArray *jsonRoutes = [[NSMutableArray alloc] init];
            NSMutableDictionary *jsonRoute = [[NSMutableDictionary alloc] init];
            [jsonRoute setValue:@"0" forKey:@"id"];

            NSMutableArray *sections = [[NSMutableArray alloc] init];
            for (YMKMasstransitSection *section in route.sections) {
                NSDictionary *jsonSection = [self convertRouteSection:route withSection:section];
                [sections addObject:jsonSection];
            }
            [jsonRoute setValue:sections forKey:@"sections"];
            [jsonRoutes addObject:jsonRoute];
            [response setValue:jsonRoutes forKey:@"routes"];
            resolve(response);
            return;
        }

        YMKPoint *detour = [self chooseDetourPointForZone:intersectedZone endPoint:endPoint existingDetours:detours];
        if (detour == nil) {
            reject(@"findParkRoutesError", @"failed to build detour outside restricted zone", nil);
            return;
        }

        NSMutableArray<YMKPoint *> *nextDetours = [detours mutableCopy];
        [nextDetours addObject:detour];

        [self requestParkRouteFrom:startPoint
                               end:endPoint
                             zones:zones
                           detours:nextDetours
                         iteration:iteration + 1
                           resolve:resolve
                            reject:reject];
    }];
}

- (NSArray<NSArray<YMKPoint *> *> *)parseZones:(NSArray *)zones {
    NSMutableArray<NSArray<YMKPoint *> *> *parsed = [[NSMutableArray alloc] init];
    for (id rawZone in zones) {
        if (![rawZone isKindOfClass:[NSArray class]]) {
            continue;
        }
        NSArray<YMKPoint *> *zonePoints = [RCTConvert YMKPointArray:(NSArray *)rawZone];
        if ([zonePoints count] >= 3) {
            [parsed addObject:zonePoints];
        }
    }
    return parsed;
}

- (NSArray<YMKPoint *> * _Nullable)firstIntersectedZoneForRoute:(YMKMasstransitRoute *)route
                                                           zones:(NSArray<NSArray<YMKPoint *> *> *)zones {
    NSArray<YMKPoint *> *routePoints = route.geometry.points;
    if ([routePoints count] < 2) {
        return nil;
    }

    for (NSArray<YMKPoint *> *zone in zones) {
        if ([self polyline:routePoints intersectsPolygon:zone]) {
            return zone;
        }
    }
    return nil;
}

- (BOOL)polyline:(NSArray<YMKPoint *> *)polyline intersectsPolygon:(NSArray<YMKPoint *> *)polygon {
    if ([polyline count] < 2 || [polygon count] < 3) {
        return NO;
    }

    for (NSInteger i = 0; i < [polyline count] - 1; i++) {
        if ([self segmentFrom:polyline[i] to:polyline[i + 1] intersectsPolygon:polygon]) {
            return YES;
        }
    }
    return NO;
}

- (BOOL)segmentFrom:(YMKPoint *)a to:(YMKPoint *)b intersectsPolygon:(NSArray<YMKPoint *> *)polygon {
    if ([self isPoint:a insidePolygon:polygon] || [self isPoint:b insidePolygon:polygon]) {
        return YES;
    }

    for (NSInteger i = 0; i < [polygon count]; i++) {
        YMKPoint *c = polygon[i];
        YMKPoint *d = polygon[(i + 1) % [polygon count]];
        if ([self segmentFrom:a to:b intersectsSegmentFrom:c to:d]) {
            return YES;
        }
    }
    return NO;
}

- (BOOL)segmentFrom:(YMKPoint *)a
                 to:(YMKPoint *)b
intersectsSegmentFrom:(YMKPoint *)c
                 to:(YMKPoint *)d {
    double o1 = [self orientationA:a b:b c:c];
    double o2 = [self orientationA:a b:b c:d];
    double o3 = [self orientationA:c b:d c:a];
    double o4 = [self orientationA:c b:d c:b];
    const double eps = 1e-9;

    if (o1 * o2 < 0 && o3 * o4 < 0) {
        return YES;
    }
    if (fabs(o1) < eps && [self point:c onSegmentFrom:a to:b]) return YES;
    if (fabs(o2) < eps && [self point:d onSegmentFrom:a to:b]) return YES;
    if (fabs(o3) < eps && [self point:a onSegmentFrom:c to:d]) return YES;
    if (fabs(o4) < eps && [self point:b onSegmentFrom:c to:d]) return YES;
    return NO;
}

- (double)orientationA:(YMKPoint *)a b:(YMKPoint *)b c:(YMKPoint *)c {
    return (b.longitude - a.longitude) * (c.latitude - a.latitude) -
           (b.latitude - a.latitude) * (c.longitude - a.longitude);
}

- (BOOL)point:(YMKPoint *)p onSegmentFrom:(YMKPoint *)a to:(YMKPoint *)b {
    return p.longitude <= MAX(a.longitude, b.longitude) &&
           p.longitude >= MIN(a.longitude, b.longitude) &&
           p.latitude <= MAX(a.latitude, b.latitude) &&
           p.latitude >= MIN(a.latitude, b.latitude);
}

- (BOOL)isPoint:(YMKPoint *)point insidePolygon:(NSArray<YMKPoint *> *)polygon {
    BOOL inside = NO;
    NSInteger j = [polygon count] - 1;
    for (NSInteger i = 0; i < [polygon count]; i++) {
        YMKPoint *pi = polygon[i];
        YMKPoint *pj = polygon[j];
        BOOL intersectsLatitude = (pi.latitude > point.latitude) != (pj.latitude > point.latitude);
        if (intersectsLatitude) {
            double xIntersection = (pj.longitude - pi.longitude) * (point.latitude - pi.latitude) /
                                   (pj.latitude - pi.latitude + 1e-12) + pi.longitude;
            if (point.longitude < xIntersection) {
                inside = !inside;
            }
        }
        j = i;
    }
    return inside;
}

- (YMKPoint * _Nullable)chooseDetourPointForZone:(NSArray<YMKPoint *> *)zone
                                        endPoint:(YMKPoint *)endPoint
                                  existingDetours:(NSArray<YMKPoint *> *)existingDetours {
    YMKPoint *centroid = [self polygonCentroid:zone];
    NSMutableArray<YMKPoint *> *candidates = [[NSMutableArray alloc] init];
    for (YMKPoint *vertex in zone) {
        [candidates addObject:[self projectPointOutsideZone:vertex centroid:centroid meters:kParkRouteBufferMeters]];
    }

    [candidates sortUsingComparator:^NSComparisonResult(YMKPoint *lhs, YMKPoint *rhs) {
        double ld = [self distanceBetween:lhs and:endPoint];
        double rd = [self distanceBetween:rhs and:endPoint];
        if (ld < rd) return NSOrderedAscending;
        if (ld > rd) return NSOrderedDescending;
        return NSOrderedSame;
    }];

    for (YMKPoint *candidate in candidates) {
        BOOL alreadyUsed = NO;
        for (YMKPoint *existing in existingDetours) {
            if ([self distanceBetween:candidate and:existing] < kDetourEpsilonDegrees) {
                alreadyUsed = YES;
                break;
            }
        }
        if (!alreadyUsed) {
            return candidate;
        }
    }
    return nil;
}

- (YMKPoint *)polygonCentroid:(NSArray<YMKPoint *> *)points {
    double lat = 0;
    double lon = 0;
    for (YMKPoint *point in points) {
        lat += point.latitude;
        lon += point.longitude;
    }
    return [YMKPoint pointWithLatitude:lat / [points count] longitude:lon / [points count]];
}

- (YMKPoint *)projectPointOutsideZone:(YMKPoint *)point centroid:(YMKPoint *)centroid meters:(double)meters {
    double dLat = point.latitude - centroid.latitude;
    double dLon = point.longitude - centroid.longitude;
    double norm = hypot(dLat, dLon);
    if (norm < 1e-12) {
        return point;
    }

    double latDeg = [self metersToLatitudeDegrees:meters];
    double lonDeg = [self metersToLongitudeDegrees:meters atLatitude:point.latitude];
    double projectedLat = point.latitude + (dLat / norm) * latDeg;
    double projectedLon = point.longitude + (dLon / norm) * lonDeg;
    return [YMKPoint pointWithLatitude:projectedLat longitude:projectedLon];
}

- (double)metersToLatitudeDegrees:(double)meters {
    return meters / 111320.0;
}

- (double)metersToLongitudeDegrees:(double)meters atLatitude:(double)latitude {
    double scale = cos(latitude * M_PI / 180.0);
    if (scale < 1e-6) {
        scale = 1e-6;
    }
    return meters / (111320.0 * scale);
}

- (double)distanceBetween:(YMKPoint *)a and:(YMKPoint *)b {
    return hypot(a.latitude - b.latitude, a.longitude - b.longitude);
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

- (void)findParkRoutesImpl:(JS::NativeTransportModule::Point &)start
                       end:(JS::NativeTransportModule::Point &)end
                     zones:(NSArray *)zones
                   resolve:(RCTPromiseResolveBlock)resolve
                    reject:(RCTPromiseRejectBlock)reject {
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
