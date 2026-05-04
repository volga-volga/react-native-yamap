//// ParkRouter.m
//
//#import "ParkRouter.h"
//#import "GeometryUtils.h"
//#import <YandexMapsMobile/YMKPedestrianRouter.h>
//
//
//@interface ParkRouter () <YMKSessionRouteListener>
//
//@property (nonatomic, strong) YMKPedestrianRouter *router;
//@property (nonatomic, copy) void (^completion)(YMKRoute *);
//@property (nonatomic, strong) NSArray<NSArray<YMKPoint *> *> *forbidden;
//@property (nonatomic, strong) NSArray<YMKPoint *> *outer;
//@property (nonatomic, strong) YMKPoint *start;
//@property (nonatomic, strong) YMKPoint *end;
//
//@end
//
//@implementation ParkRouter
//
//- (instancetype)initWithMapView:(YMKMapView *)mapView {
//    self = [super init];
//    if (self) {
//        _router = [[YMKDirectionsFactory instance] createPedestrianRouter];
//    }
//    return self;
//}
//
//- (void)buildRouteFrom:(YMKPoint *)start
//                   to:(YMKPoint *)end
//        outerBoundary:(NSArray<YMKPoint *> *)outerPolygon
//       forbiddenZones:(NSArray<NSArray<YMKPoint *> *> *)forbiddenPolygons
//           completion:(void (^)(YMKRoute * _Nullable route))completion {
//
//    self.start = start;
//    self.end = end;
//    self.outer = outerPolygon;
//    self.forbidden = forbiddenPolygons;
//    self.completion = completion;
//
//    [self requestRouteWithPoints:@[
//        [YMKRequestPoint requestPointWithPoint:start type:YMKRequestPointTypeWaypoint pointContext:nil],
//        [YMKRequestPoint requestPointWithPoint:end type:YMKRequestPointTypeWaypoint pointContext:nil]
//    ]];
//}
//
//- (void)requestRouteWithPoints:(NSArray<YMKRequestPoint *> *)points {
//
//    [self.router requestRoutesWithPoints:points
//                            timeOptions:[[YMKTimeOptions alloc] init]
//                          routeHandler:self];
//}
//
//#pragma mark - YMKSessionRouteListener
//
//- (void)onRoutes:(NSArray<YMKRoute *> *)routes {
//
//    YMKRoute *route = routes.firstObject;
//    if (!route) {
//        self.completion(nil);
//        return;
//    }
//
//    YMKPolyline *polyline = route.geometry;
//
//    // 1. Проверка выхода за границу парка
//    if ([GeometryUtils polyline:polyline isOutsidePolygon:self.outer]) {
//
//        NSArray<YMKPoint *> *fixPoints =
//        [GeometryUtils detourPointsToStayInside:polyline polygon:self.outer];
//
//        [self rebuildWithDetours:fixPoints];
//        return;
//    }
//
//    // 2. Проверка пересечения запрещённых зон
//    for (NSArray<YMKPoint *> *zone in self.forbidden) {
//
//        if ([GeometryUtils polyline:polyline intersectsPolygon:zone]) {
//
//            NSArray<YMKPoint *> *detours =
//            [GeometryUtils detourPointsForPolygon:polyline polygon:zone];
//
//            [self rebuildWithDetours:detours];
//            return;
//        }
//    }
//
//    // Всё ок
//    self.completion(route);
//}
//
//- (void)onRoutesError:(NSError *)error {
//    self.completion(nil);
//}
//
//#pragma mark - Rebuild
//
//- (void)rebuildWithDetours:(NSArray<YMKPoint *> *)detours {
//
//    NSMutableArray *points = [NSMutableArray array];
//
//    [points addObject:[YMKRequestPoint requestPointWithPoint:self.start
//                                                        type:YMKRequestPointTypeWaypoint
//                                                pointContext:nil]];
//
//    for (YMKPoint *p in detours) {
//        [points addObject:[YMKRequestPoint requestPointWithPoint:p
//                                                            type:YMKRequestPointTypeWaypoint
//                                                    pointContext:nil]];
//    }
//
//    [points addObject:[YMKRequestPoint requestPointWithPoint:self.end
//                                                        type:YMKRequestPointTypeWaypoint
//                                                pointContext:nil]];
//
//    [self requestRouteWithPoints:points];
//}
//
//@end
