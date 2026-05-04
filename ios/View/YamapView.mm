#import "YamapView.h"

#import <React/UIView+React.h>

#if TARGET_OS_SIMULATOR
#import <mach-o/arch.h>
#endif

#import "ClusteredYamapView.h"
#import "CircleView.h"
#import "MarkerView.h"
#import "PolygonView.h"
#import "PolylineView.h"
#import "../Util/RCTConvert+Yamap.mm"
#import "ImageCacheManager.h"

#ifdef RCT_NEW_ARCH_ENABLED

#import "../Util/NewArchUtils.h"

#import <react/renderer/components/RNYamapPlusSpec/ComponentDescriptors.h>
#import <react/renderer/components/RNYamapPlusSpec/EventEmitters.h>
#import <react/renderer/components/RNYamapPlusSpec/Props.h>
#import <react/renderer/components/RNYamapPlusSpec/RCTComponentViewHelpers.h>

#import "RCTFabricComponentsPlugins.h"

using namespace facebook::react;

@interface YamapView () <YMKUserLocationObjectListener, YMKMapCameraListener, YMKMapLoadedListener, YMKTrafficDelegate, YMKClusterListener, YMKClusterTapListener>

@end

#endif

#import <YandexMapsMobile/YMKMap.h>
#import <YandexMapsMobile/YMKMapKitFactory.h>
#import <YandexMapsMobile/YMKMapObjectCollection.h>
#import <YandexMapsMobile/YMKVisibleRegion.h>
#import <YandexMapsMobile/YMKLogoAlignment.h>
#import <YandexMapsMobile/YMKLogoPadding.h>
#import <YandexMapsMobile/YMKIconStyle.h>
#import <YandexMapsMobile/YMKMapLoadStatistics.h>
#import <YandexMapsMobile/YMKCluster.h>
#import <YandexMapsMobile/YMKClusterizedPlacemarkCollection.h>
#import <YandexMapsMobile/YMKMasstransitSession.h>
#import <YandexMapsMobile/YMKPedestrianRouter.h>

@implementation YamapView {
    YMKMapView *mapView;
    NSMutableArray<UIView*> *_reactSubviews;
    YMKUserLocationView *userLocationView;
    UIImage *userLocationImage;
    NSNumber *userLocationImageScale;
    YMKUserLocationLayer *userLayer;
    YMKTrafficLayer *trafficLayer;
    UIColor *userLocationAccuracyFillColor;
    UIColor *userLocationAccuracyStrokeColor;
    float userLocationAccuracyStrokeWidth;
    Boolean initializedRegion;
    UIColor *clusterColor;
    UIImage* clusImage;
    CGFloat clusterWidth;
    CGFloat clusterHeight;
    UIColor* clusterTextColor;
    double clusterTextSize;
    double clusterTextYOffset;
    double clusterTextXOffset;
    NSMutableArray<YMKPlacemarkMapObject *> *clusterPlacemarks;
    YMKClusterizedPlacemarkCollection *clusterCollection;
    BOOL mapLoaded;
}

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
#if TARGET_OS_SIMULATOR
        const NXArchInfo *archInfo = NXGetLocalArchInfo();
        NSString *cpuArch = [NSString stringWithUTF8String:archInfo->description];
        mapView = [[YMKMapView alloc] initWithFrame:frame vulkanPreferred:[cpuArch hasPrefix:@"ARM64"]];
#else
        mapView = [[YMKMapView alloc] initWithFrame:frame];
#endif

#ifdef RCT_NEW_ARCH_ENABLED

        static const auto defaultProps = std::make_shared<const YamapViewProps>();
        _props = defaultProps;

#endif

        _reactSubviews = [[NSMutableArray alloc] init];
        userLocationImageScale = [NSNumber numberWithFloat:1.f];
        userLocationAccuracyFillColor = nil;
        userLocationAccuracyStrokeColor = nil;
        userLocationAccuracyStrokeWidth = 0.f;
        [mapView.mapWindow.map addCameraListenerWithCameraListener:self];
        [mapView.mapWindow.map addInputListenerWithInputListener:(id<YMKMapInputListener>) self];
        [mapView.mapWindow.map setMapLoadedListenerWithMapLoadedListener:self];
        mapView.mapWindow.map.awesomeModelsEnabled = YES;
        initializedRegion = NO;
        clusterPlacemarks = [[NSMutableArray alloc] init];
        clusterCollection = [mapView.mapWindow.map.mapObjects addClusterizedPlacemarkCollectionWithClusterListener:self];
        clusterColor = UIColor.redColor;
        mapLoaded = NO;
        clusterWidth = 32;
        clusterHeight = 32;
        clusterTextColor = UIColor.blackColor;
        clusterTextSize = 45;
        clusterTextYOffset = 0;
        clusterTextXOffset = 0;
        [self addSubview:mapView];
    }

    return self;
}

#ifdef RCT_NEW_ARCH_ENABLED

+ (ComponentDescriptorProvider)componentDescriptorProvider
{
    return concreteComponentDescriptorProvider<YamapViewComponentDescriptor>();
}

- (void)updateProps:(const Props::Shared &)props oldProps:(const Props::Shared &)oldProps {
    const auto &oldViewProps = *std::static_pointer_cast<YamapViewProps const>(_props);
    const auto &newViewProps = *std::static_pointer_cast<YamapViewProps const>(props);

    BOOL needUpdateUserIcon = false;

    if (![NewArchUtils yamapInitialRegionsEquals:oldViewProps.initialRegion initialRegion2:newViewProps.initialRegion]) {
        YMKPoint *center = [YMKPoint pointWithLatitude:newViewProps.initialRegion.lat longitude:newViewProps.initialRegion.lon];
        YMKCameraPosition *cameraPosition = [YMKCameraPosition cameraPositionWithTarget:center zoom:newViewProps.initialRegion.zoom azimuth:newViewProps.initialRegion.azimuth tilt:newViewProps.initialRegion.tilt];
        [mapView.mapWindow.map moveWithCameraPosition:cameraPosition];
    }

    if (oldViewProps.mapType != newViewProps.mapType) {
        mapView.mapWindow.map.mapType = [self mapTypeWithJsEnum:newViewProps.mapType];
    }

    if (oldViewProps.nightMode != newViewProps.nightMode) {
        [self setNightMode:newViewProps.nightMode];
    }

    if (oldViewProps.showUserPosition != newViewProps.showUserPosition) {
        [self setShowUserPosition:newViewProps.showUserPosition];
    }

    if (oldViewProps.userLocationAccuracyFillColor != newViewProps.userLocationAccuracyFillColor) {
        userLocationAccuracyFillColor = [RCTConvert UIColor:[NSNumber numberWithInt:newViewProps.userLocationAccuracyFillColor]];
        needUpdateUserIcon = true;
    }

    if (oldViewProps.userLocationAccuracyStrokeColor != newViewProps.userLocationAccuracyStrokeColor) {
        userLocationAccuracyStrokeColor = [RCTConvert UIColor:[NSNumber numberWithInt:newViewProps.userLocationAccuracyStrokeColor]];
        needUpdateUserIcon = true;
    }

    if (oldViewProps.userLocationAccuracyStrokeWidth != newViewProps.userLocationAccuracyStrokeWidth) {
        userLocationAccuracyStrokeWidth = newViewProps.userLocationAccuracyStrokeWidth;
        needUpdateUserIcon = true;
    }

    if (oldViewProps.userLocationIconScale != newViewProps.userLocationIconScale) {
        userLocationImageScale = [NSNumber numberWithFloat:newViewProps.userLocationIconScale];
        needUpdateUserIcon = true;
    }

    if (oldViewProps.userLocationIcon != newViewProps.userLocationIcon) {
        needUpdateUserIcon = false;
        [self setUserLocationIcon:[NSString stringWithCString:newViewProps.userLocationIcon.c_str() encoding:[NSString defaultCStringEncoding]]];
    }

    if (oldViewProps.mapStyle != newViewProps.mapStyle) {
        [mapView.mapWindow.map setMapStyleWithStyle:[NSString stringWithCString:newViewProps.mapStyle.c_str() encoding:[NSString defaultCStringEncoding]]];
    }

    if (oldViewProps.scrollGesturesDisabled != newViewProps.scrollGesturesDisabled) {
        mapView.mapWindow.map.scrollGesturesEnabled = !newViewProps.scrollGesturesDisabled;
    }

    if (oldViewProps.zoomGesturesDisabled != newViewProps.zoomGesturesDisabled) {
        mapView.mapWindow.map.zoomGesturesEnabled = !newViewProps.zoomGesturesDisabled;
    }

    if (oldViewProps.tiltGesturesDisabled != newViewProps.tiltGesturesDisabled) {
        mapView.mapWindow.map.tiltGesturesEnabled = !newViewProps.tiltGesturesDisabled;
    }

    if (oldViewProps.rotateGesturesDisabled != newViewProps.rotateGesturesDisabled) {
        mapView.mapWindow.map.rotateGesturesEnabled = !newViewProps.rotateGesturesDisabled;
    }

    if (oldViewProps.fastTapDisabled != newViewProps.fastTapDisabled) {
        mapView.mapWindow.map.fastTapEnabled = !newViewProps.fastTapDisabled;
    }

    if (oldViewProps.followUser != newViewProps.followUser) {
        [self setFollowUser:newViewProps.followUser];
    }

    if (oldViewProps.interactiveDisabled != newViewProps.interactiveDisabled) {
        [mapView setNoninteractive:newViewProps.interactiveDisabled];
    }

    if (oldViewProps.logoPosition.vertical != newViewProps.logoPosition.vertical || oldViewProps.logoPosition.horizontal != newViewProps.logoPosition.horizontal) {
        YMKLogoHorizontalAlignment horizontalAlignment = YMKLogoHorizontalAlignmentRight;
        YMKLogoVerticalAlignment verticalAlignment = YMKLogoVerticalAlignmentBottom;

        if (newViewProps.logoPosition.horizontal == YamapViewHorizontal::Left) {
            horizontalAlignment = YMKLogoHorizontalAlignmentLeft;
        } else if (newViewProps.logoPosition.horizontal == YamapViewHorizontal::Center) {
            horizontalAlignment = YMKLogoHorizontalAlignmentCenter;
        }

        if (newViewProps.logoPosition.vertical == YamapViewVertical::Top) {
            verticalAlignment = YMKLogoVerticalAlignmentTop;
        }

        [mapView.mapWindow.map.logo setAlignmentWithAlignment:[YMKLogoAlignment alignmentWithHorizontalAlignment:horizontalAlignment verticalAlignment:verticalAlignment]];
    }

    if (oldViewProps.logoPadding.vertical != newViewProps.logoPadding.vertical || oldViewProps.logoPadding.horizontal != newViewProps.logoPadding.horizontal) {
        [mapView.mapWindow.map.logo setPaddingWithPadding:[YMKLogoPadding paddingWithHorizontalPadding:newViewProps.logoPadding.horizontal verticalPadding:newViewProps.logoPadding.vertical]];
    }

    if (needUpdateUserIcon) {
        [self updateUserIcon];
    }

    _props = std::static_pointer_cast<const ViewProps>(props);
}

- (void)handleCommand:(const NSString *)commandName args:(const NSArray *)args {
    if (!_eventEmitter) {
        return;
    }

    if ([commandName isEqual:@"setCenter"]) {
        NSNumber *duration = args[0][0][@"duration"];
        NSNumber *animation = args[0][0][@"animation"];
        NSNumber *zoom = args[0][0][@"zoom"];
        NSNumber *azimuth = args[0][0][@"azimuth"];
        NSNumber *tilt = args[0][0][@"tilt"];
        YMKPoint *center = [RCTConvert YMKPoint:args[0][0][@"center"]];
        YMKCameraPosition *cameraPosition = [YMKCameraPosition cameraPositionWithTarget:center zoom:[zoom floatValue] azimuth:[azimuth floatValue] tilt:[tilt floatValue]];
        [self setCenter:cameraPosition withDuration:[duration floatValue] withAnimation:[animation intValue]];
    } else if ([commandName isEqual:@"fitMarkers"]) {
        NSArray *points = [RCTConvert YMKPointArray:args[0][0][@"points"]];
        NSNumber *duration = args[0][0][@"duration"];
        NSNumber *animation = args[0][0][@"animation"];
        [self fitMarkers:points duration:[duration floatValue] animation:[animation intValue]];
    } else if ([commandName isEqual:@"fitAllMarkers"]) {
        NSNumber *duration = args[0][0][@"duration"];
        NSNumber *animation = args[0][0][@"animation"];
        [self fitAllMarkers:[duration floatValue] animation:[animation intValue]];
    } else if ([commandName isEqual:@"setTrafficVisible"]) {
        BOOL isVisible = [args[0][0][@"isVisible"] boolValue];
        [self setTrafficVisible:isVisible];
    } else if ([commandName isEqual:@"setZoom"]) {
        NSNumber *zoom = args[0][0][@"zoom"];
        NSNumber *duration = args[0][0][@"duration"];
        NSNumber *animation = args[0][0][@"animation"];
        [self setZoom:[zoom floatValue] withDuration:[duration floatValue] withAnimation:[animation intValue]];
    } else if ([commandName isEqual:@"getUserPosition"]) {
        std::string id = std::string([args[0][0][@"id"] UTF8String]);
        YMKCameraPosition *position = [userLayer cameraPosition];
        std::dynamic_pointer_cast<const YamapViewEventEmitter>(_eventEmitter)->onCameraPositionReceived({
            .id = id,
            .point = {
                .lat = position.target.latitude,
                .lon = position.target.longitude,
            },
            .azimuth = position.azimuth,
            .reason = "APPLICATION",
            .tilt = position.tilt,
            .zoom = position.zoom
        });
    } else if ([commandName isEqual:@"getCameraPosition"]) {
        std::string id = std::string([args[0][0][@"id"] UTF8String]);
        YMKCameraPosition *position = mapView.mapWindow.map.cameraPosition;

        if ([self isKindOfClass:[ClusteredYamapView class]]) {
            std::dynamic_pointer_cast<const ClusteredYamapViewEventEmitter>(_eventEmitter)->onCameraPositionReceived({
                .id = id,
                .point = {
                    .lat = position.target.latitude,
                    .lon = position.target.longitude,
                },
                .azimuth = position.azimuth,
                .reason = "APPLICATION",
                .tilt = position.tilt,
                .zoom = position.zoom
            });
        } else {
            std::dynamic_pointer_cast<const YamapViewEventEmitter>(_eventEmitter)->onCameraPositionReceived({
                .id = id,
                .point = {
                    .lat = position.target.latitude,
                    .lon = position.target.longitude,
                },
                .azimuth = position.azimuth,
                .reason = "APPLICATION",
                .tilt = position.tilt,
                .zoom = position.zoom
            });
        }
    } else if ([commandName isEqual:@"getVisibleRegion"]) {
        std::string id = std::string([args[0][0][@"id"] UTF8String]);
        YMKVisibleRegion *region = mapView.mapWindow.map.visibleRegion;

        if ([self isKindOfClass:[ClusteredYamapView class]]) {
            std::dynamic_pointer_cast<const ClusteredYamapViewEventEmitter>(_eventEmitter)->onVisibleRegionReceived({
                .id = id,
                .bottomLeft = {
                  .lat = region.bottomLeft.latitude,
                  .lon = region.bottomLeft.longitude
                },
                .bottomRight = {
                  .lat = region.bottomRight.latitude,
                  .lon = region.bottomRight.longitude
                },
                .topLeft = {
                  .lat = region.topLeft.latitude,
                  .lon = region.topLeft.longitude
                },
                .topRight = {
                  .lat = region.topRight.latitude,
                  .lon = region.topRight.longitude
                }
            });
        } else {
            std::dynamic_pointer_cast<const YamapViewEventEmitter>(_eventEmitter)->onVisibleRegionReceived({
                .id = id,
                .bottomLeft = {
                  .lat = region.bottomLeft.latitude,
                  .lon = region.bottomLeft.longitude
                },
                .bottomRight = {
                  .lat = region.bottomRight.latitude,
                  .lon = region.bottomRight.longitude
                },
                .topLeft = {
                  .lat = region.topLeft.latitude,
                  .lon = region.topLeft.longitude
                },
                .topRight = {
                  .lat = region.topRight.latitude,
                  .lon = region.topRight.longitude
                }
            });
        }
    } else if ([commandName isEqual:@"getScreenPoints"]) {
        std::string id = std::string([args[0][0][@"id"] UTF8String]);
        NSArray *worldPoints = args[0][0][@"points"];

        if ([self isKindOfClass:[ClusteredYamapView class]]) {
            std::vector<ClusteredYamapViewEventEmitter::OnWorldToScreenPointsReceivedScreenPoints> screenPoints;
            for (int i = 0; i < [worldPoints count]; ++i) {
                NSDictionary *worldPoint = [worldPoints objectAtIndex:i];
                YMKScreenPoint *screenPoint = [mapView.mapWindow worldToScreenWithWorldPoint:[RCTConvert YMKPoint:worldPoint]];
                screenPoints.push_back({
                    .x = screenPoint.x,
                    .y = screenPoint.y
                });
            }

            std::dynamic_pointer_cast<const ClusteredYamapViewEventEmitter>(_eventEmitter)->onWorldToScreenPointsReceived({
                .id = id,
                .screenPoints = screenPoints
            });
        } else {
            std::vector<YamapViewEventEmitter::OnWorldToScreenPointsReceivedScreenPoints> screenPoints;
            for (int i = 0; i < [worldPoints count]; ++i) {
                NSDictionary *worldPoint = [worldPoints objectAtIndex:i];
                YMKScreenPoint *screenPoint = [mapView.mapWindow worldToScreenWithWorldPoint:[RCTConvert YMKPoint:worldPoint]];
                screenPoints.push_back({
                    .x = screenPoint.x,
                    .y = screenPoint.y
                });
            }

            std::dynamic_pointer_cast<const YamapViewEventEmitter>(_eventEmitter)->onWorldToScreenPointsReceived({
                .id = id,
                .screenPoints = screenPoints
            });
        }
    } else if ([commandName isEqual:@"getWorldPoints"]) {
        std::string id = std::string([args[0][0][@"id"] UTF8String]);
        NSArray *screenPoints = args[0][0][@"points"];

        if ([self isKindOfClass:[ClusteredYamapView class]]) {
            std::vector<ClusteredYamapViewEventEmitter::OnScreenToWorldPointsReceivedWorldPoints> worldPoints;
            for (int i = 0; i < [screenPoints count]; ++i) {
                NSDictionary *screenPoint = [screenPoints objectAtIndex:i];
                YMKPoint *worldPoint = [mapView.mapWindow screenToWorldWithScreenPoint:[RCTConvert YMKScreenPoint:screenPoint]];
                worldPoints.push_back({
                    .lat = worldPoint.latitude,
                    .lon = worldPoint.longitude
                });
            }

            std::dynamic_pointer_cast<const ClusteredYamapViewEventEmitter>(_eventEmitter)->onScreenToWorldPointsReceived({
                .id = id,
                .worldPoints = worldPoints
            });
        } else {
            std::vector<YamapViewEventEmitter::OnScreenToWorldPointsReceivedWorldPoints> worldPoints;
            for (int i = 0; i < [screenPoints count]; ++i) {
                NSDictionary *screenPoint = [screenPoints objectAtIndex:i];
                YMKPoint *worldPoint = [mapView.mapWindow screenToWorldWithScreenPoint:[RCTConvert YMKScreenPoint:screenPoint]];
                worldPoints.push_back({
                    .lat = worldPoint.latitude,
                    .lon = worldPoint.longitude
                });
            }

            std::dynamic_pointer_cast<const YamapViewEventEmitter>(_eventEmitter)->onScreenToWorldPointsReceived({
                .id = id,
                .worldPoints = worldPoints
            });
        }
    }
}

- (void)prepareForRecycle
{
    [super prepareForRecycle];

    [self removeAllSections];

    static const auto defaultProps = std::make_shared<const YamapViewProps>();
    _props = defaultProps;
}

- (YMKMapType)mapTypeWithJsEnum:(YamapViewMapType)jsMapType {
    if (jsMapType == YamapViewMapType::None) {
        return YMKMapTypeNone;
    } else if (jsMapType == YamapViewMapType::Raster) {
        return YMKMapTypeMap;
    }

    return YMKMapTypeVectorMap;
}

#endif

- (void)removeAllSections {
    [mapView.mapWindow.map.mapObjects clear];
}

// REF
- (void)setCenter:(YMKCameraPosition *)position withDuration:(float)duration withAnimation:(int)animation {
    if (duration > 0) {
        YMKAnimationType anim = animation == 0 ? YMKAnimationTypeSmooth : YMKAnimationTypeLinear;
        [mapView.mapWindow.map moveWithCameraPosition:position animation:[YMKAnimation animationWithType:anim duration: duration] cameraCallback:^(BOOL completed) {}];
    } else {
        [mapView.mapWindow.map moveWithCameraPosition:position];
    }
}

- (void)setZoom:(float)zoom withDuration:(float)duration withAnimation:(int)animation {
    YMKCameraPosition *prevPosition = mapView.mapWindow.map.cameraPosition;
    YMKCameraPosition *position = [YMKCameraPosition cameraPositionWithTarget:prevPosition.target zoom:zoom azimuth:prevPosition.azimuth tilt:prevPosition.tilt];
    [self setCenter:position withDuration:duration withAnimation:animation];
}

- (void)setMapType:(NSString *)type {
    if ([type isEqual:@"none"]) {
        mapView.mapWindow.map.mapType = YMKMapTypeNone;
    } else if ([type isEqual:@"raster"]) {
        mapView.mapWindow.map.mapType = YMKMapTypeMap;
    } else {
        mapView.mapWindow.map.mapType = YMKMapTypeVectorMap;
    }
}

- (void)setTrafficVisible:(BOOL)isVisible {
    YMKMapKit *inst = [YMKMapKit sharedInstance];

    if (trafficLayer == nil) {
        trafficLayer = [inst createTrafficLayerWithMapWindow:mapView.mapWindow];
    }

    if (isVisible) {
        [trafficLayer setTrafficVisibleWithOn:YES];
        [trafficLayer addTrafficListenerWithTrafficListener:self];
    } else {
        [trafficLayer setTrafficVisibleWithOn:NO];
        [trafficLayer removeTrafficListenerWithTrafficListener:self];
    }
}

#ifndef RCT_NEW_ARCH_ENABLED

- (void)setInitialRegion:(NSDictionary *)initialParams {
    if (initializedRegion) return;
    if ([initialParams valueForKey:@"lat"] == nil || [initialParams valueForKey:@"lon"] == nil) return;

    float initialZoom = 10.f;
    float initialAzimuth = 0.f;
    float initialTilt = 0.f;

    if ([initialParams valueForKey:@"zoom"] != nil) initialZoom = [initialParams[@"zoom"] floatValue];

    if ([initialParams valueForKey:@"azimuth"] != nil) initialTilt = [initialParams[@"azimuth"] floatValue];

    if ([initialParams valueForKey:@"tilt"] != nil) initialTilt = [initialParams[@"tilt"] floatValue];

    YMKPoint *initialRegionCenter = [RCTConvert YMKPoint:@{@"lat" : [initialParams valueForKey:@"lat"], @"lon" : [initialParams valueForKey:@"lon"]}];
    YMKCameraPosition *initialRegionPosition = [YMKCameraPosition cameraPositionWithTarget:initialRegionCenter zoom:initialZoom azimuth:initialAzimuth tilt:initialTilt];
    [mapView.mapWindow.map moveWithCameraPosition:initialRegionPosition];
    initializedRegion = YES;
}

- (NSDictionary *)cameraPositionToJSON:(YMKCameraPosition *)position reason:(YMKCameraUpdateReason)reason finished:(BOOL)finished {
    return @{
        @"azimuth": [NSNumber numberWithFloat:position.azimuth],
        @"tilt": [NSNumber numberWithFloat:position.tilt],
        @"zoom": [NSNumber numberWithFloat:position.zoom],
        @"point": @{
            @"lat": [NSNumber numberWithDouble:position.target.latitude],
            @"lon": [NSNumber numberWithDouble:position.target.longitude]
        },
        @"reason": reason == 0 ? @"GESTURES" : @"APPLICATION",
        @"finished": @(finished)
    };
}

- (NSDictionary *)worldPointToJSON:(YMKPoint *)point {
    return @{
        @"lat": [NSNumber numberWithDouble:point.latitude],
        @"lon": [NSNumber numberWithDouble:point.longitude]
    };
}

- (NSDictionary *)screenPointToJSON:(YMKScreenPoint *)point {
    return @{
        @"x": [NSNumber numberWithFloat:point.x],
        @"y": [NSNumber numberWithFloat:point.y]
    };
}

- (NSDictionary *)visibleRegionToJSON:(YMKVisibleRegion *)region {
    return @{
        @"bottomLeft": @{
            @"lat": [NSNumber numberWithDouble:region.bottomLeft.latitude],
            @"lon": [NSNumber numberWithDouble:region.bottomLeft.longitude]
        },
        @"bottomRight": @{
            @"lat": [NSNumber numberWithDouble:region.bottomRight.latitude],
            @"lon": [NSNumber numberWithDouble:region.bottomRight.longitude]
        },
        @"topLeft": @{
            @"lat": [NSNumber numberWithDouble:region.topLeft.latitude],
            @"lon": [NSNumber numberWithDouble:region.topLeft.longitude]
        },
        @"topRight": @{
            @"lat": [NSNumber numberWithDouble:region.topRight.latitude],
            @"lon": [NSNumber numberWithDouble:region.topRight.longitude]
        }
    };
}

- (void)emitCameraPositionToJS:(NSString *)_id {
    YMKCameraPosition *position = mapView.mapWindow.map.cameraPosition;
    NSDictionary *cameraPosition = [self cameraPositionToJSON:position reason:YMKCameraUpdateReasonApplication finished:YES];
    NSMutableDictionary *response = [NSMutableDictionary dictionaryWithDictionary:cameraPosition];
    [response setValue:_id forKey:@"id"];

    if (self.onCameraPositionReceived) {
        self.onCameraPositionReceived(response);
    }
}

- (void)emitVisibleRegionToJS:(NSString *)_id {
    YMKVisibleRegion *region = mapView.mapWindow.map.visibleRegion;
    NSDictionary *visibleRegion = [self visibleRegionToJSON:region];
    NSMutableDictionary *response = [NSMutableDictionary dictionaryWithDictionary:visibleRegion];
    [response setValue:_id forKey:@"id"];

    if (self.onVisibleRegionReceived) {
        self.onVisibleRegionReceived(response);
    }
}

- (void)emitWorldToScreenPoint:(NSArray<YMKPoint *> *)worldPoints withId:(NSString *)_id {
    NSMutableArray *screenPoints = [[NSMutableArray alloc] init];

    for (int i = 0; i < [worldPoints count]; ++i) {
        YMKScreenPoint *screenPoint = [mapView.mapWindow worldToScreenWithWorldPoint:[worldPoints objectAtIndex:i]];
        [screenPoints addObject:[self screenPointToJSON:screenPoint]];
    }

    NSMutableDictionary *response = [[NSMutableDictionary alloc] init];
    [response setValue:_id forKey:@"id"];
    [response setValue:screenPoints forKey:@"screenPoints"];

    if (self.onWorldToScreenPointsReceived) {
        self.onWorldToScreenPointsReceived(response);
    }
}

- (void)emitScreenToWorldPoint:(NSArray<YMKScreenPoint *> *)screenPoints withId:(NSString *)_id {
    NSMutableArray *worldPoints = [[NSMutableArray alloc] init];

    for (int i = 0; i < [screenPoints count]; ++i) {
        YMKPoint *worldPoint = [mapView.mapWindow screenToWorldWithScreenPoint:[screenPoints objectAtIndex:i]];
        [worldPoints addObject:[self worldPointToJSON:worldPoint]];
    }

    NSMutableDictionary *response = [[NSMutableDictionary alloc] init];
    [response setValue:_id forKey:@"id"];
    [response setValue:worldPoints forKey:@"worldPoints"];

    if (self.onScreenToWorldPointsReceived) {
        self.onScreenToWorldPointsReceived(response);
    }
}

#endif

- (void)onCameraPositionChangedWithMap:(nonnull YMKMap*)map
                        cameraPosition:(nonnull YMKCameraPosition*)cameraPosition
                    cameraUpdateReason:(YMKCameraUpdateReason)cameraUpdateReason
                              finished:(BOOL)finished {

#ifdef RCT_NEW_ARCH_ENABLED

    if (!_eventEmitter) {
        return;
    }

    if ([self isKindOfClass:[ClusteredYamapView class]]) {
        std::dynamic_pointer_cast<const ClusteredYamapViewEventEmitter>(_eventEmitter)->onCameraPositionChange({
            .point = {
                .lat = cameraPosition.target.latitude,
                .lon = cameraPosition.target.longitude,
            },
            .azimuth = cameraPosition.azimuth,
            .finished = finished,
            .reason = cameraUpdateReason == YMKCameraUpdateReasonGestures ? "GESTURES" : "APPLICATION",
            .tilt = cameraPosition.tilt,
            .zoom = cameraPosition.zoom,
        });
    } else {
        std::dynamic_pointer_cast<const YamapViewEventEmitter>(_eventEmitter)->onCameraPositionChange({
            .point = {
                .lat = cameraPosition.target.latitude,
                .lon = cameraPosition.target.longitude,
            },
            .azimuth = cameraPosition.azimuth,
            .finished = finished,
            .reason = cameraUpdateReason == YMKCameraUpdateReasonGestures ? "GESTURES" : "APPLICATION",
            .tilt = cameraPosition.tilt,
            .zoom = cameraPosition.zoom,
        });
    }

    if (finished) {
        if ([self isKindOfClass:[ClusteredYamapView class]]) {
            std::dynamic_pointer_cast<const ClusteredYamapViewEventEmitter>(_eventEmitter)->onCameraPositionChangeEnd({
                .point = {
                    .lat = cameraPosition.target.latitude,
                    .lon = cameraPosition.target.longitude,
                },
                .azimuth = cameraPosition.azimuth,
                .finished = finished,
                .reason = cameraUpdateReason == YMKCameraUpdateReasonGestures ? "GESTURES" : "APPLICATION",
                .tilt = cameraPosition.tilt,
                .zoom = cameraPosition.zoom,
            });
        } else {
            std::dynamic_pointer_cast<const YamapViewEventEmitter>(_eventEmitter)->onCameraPositionChangeEnd({
                .point = {
                    .lat = cameraPosition.target.latitude,
                    .lon = cameraPosition.target.longitude,
                },
                .azimuth = cameraPosition.azimuth,
                .finished = finished,
                .reason = cameraUpdateReason == YMKCameraUpdateReasonGestures ? "GESTURES" : "APPLICATION",
                .tilt = cameraPosition.tilt,
                .zoom = cameraPosition.zoom,
            });
        }
    }

#else

    if (self.onCameraPositionChange) {
        self.onCameraPositionChange([self cameraPositionToJSON:cameraPosition reason:cameraUpdateReason finished:finished]);
    }

    if (self.onCameraPositionChangeEnd && finished) {
        self.onCameraPositionChangeEnd([self cameraPositionToJSON:cameraPosition reason:cameraUpdateReason finished:finished]);
    }

#endif

}

- (void)setNightMode:(BOOL)nightMode {
    [mapView.mapWindow.map setNightModeEnabled:nightMode];
}

- (void)setShowUserPosition:(BOOL)listen {
    YMKMapKit *inst = [YMKMapKit sharedInstance];

    if (userLayer == nil) {
        userLayer = [inst createUserLocationLayerWithMapWindow:mapView.mapWindow];
    }

    if (listen) {
        [userLayer setVisibleWithOn:YES];
        [userLayer setObjectListenerWithObjectListener:self];
    } else {
        [userLayer setVisibleWithOn:NO];
        [userLayer setObjectListenerWithObjectListener:nil];
    }
}

- (void)setFollowUser:(BOOL)follow {
    if (userLayer == nil) {
        [self setShowUserPosition: follow];
    }

    if (follow) {
        [userLayer setAnchorWithAnchorNormal:CGPointMake(0.5 * mapView.mapWindow.width, 0.5 * mapView.mapWindow.height) anchorCourse:CGPointMake(0.5 * mapView.mapWindow.width, 0.83 * mapView.mapWindow.height )];
        [userLayer setAutoZoomEnabled:YES];
    } else {
        [userLayer setAutoZoomEnabled:NO];
        [userLayer resetAnchor];
    }
}


- (void)getUserPosition:(NSString *)_id {
    if (userLayer == nil) {
        [self setShowUserPosition: YES];
    }
    
    [userLayer cameraPosition];
}

- (void)fitAllMarkers:(float)duration animation:(int)animation {
    NSMutableArray<YMKPoint *> *lastKnownMarkers = [[NSMutableArray alloc] init];

    for (int i = 0; i < [_reactSubviews count]; ++i) {
        UIView *view = [_reactSubviews objectAtIndex:i];

        if ([view isKindOfClass:[MarkerView class]]) {
            MarkerView *marker = (MarkerView *)view;
            [lastKnownMarkers addObject:[marker getPoint]];
        }
    }

    [self fitMarkers:lastKnownMarkers duration:duration animation:animation];
}

- (NSArray<YMKPoint *> *)mapPlacemarksToPoints:(NSArray<YMKPlacemarkMapObject *> *)placemarks {
    NSMutableArray<YMKPoint *> *points = [[NSMutableArray alloc] init];

    for (int i = 0; i < [placemarks count]; ++i) {
        [points addObject:[[placemarks objectAtIndex:i] geometry]];
    }

    return points;
}

- (YMKBoundingBox *)calculateBoundingBox:(NSArray<YMKPoint *> *) points {
    double minLon = [points[0] longitude], maxLon = [points[0] longitude];
    double minLat = [points[0] latitude], maxLat = [points[0] latitude];

    for (int i = 0; i < [points count]; i++) {
        if ([points[i] longitude] > maxLon) maxLon = [points[i] longitude];
        if ([points[i] longitude] < minLon) minLon = [points[i] longitude];
        if ([points[i] latitude] > maxLat) maxLat = [points[i] latitude];
        if ([points[i] latitude] < minLat) minLat = [points[i] latitude];
    }

    YMKPoint *southWest = [YMKPoint pointWithLatitude:minLat longitude:minLon];
    YMKPoint *northEast = [YMKPoint pointWithLatitude:maxLat longitude:maxLon];
    YMKBoundingBox *boundingBox = [YMKBoundingBox boundingBoxWithSouthWest:southWest northEast:northEast];
    return boundingBox;
}

- (void)fitMarkers:(NSArray<YMKPoint *> *)points duration:(float)duration animation:(int)animation {
    if ([points count] == 0) {
        return;
    }

    YMKAnimation *anim = [YMKAnimation animationWithType:animation == 0 ? YMKAnimationTypeSmooth : YMKAnimationTypeLinear duration:duration];

    if ([points count] == 1) {
        YMKPoint *center = [points objectAtIndex:0];
        [mapView.mapWindow.map moveWithCameraPosition:[YMKCameraPosition cameraPositionWithTarget:center zoom:15 azimuth:0 tilt:0] animation:anim cameraCallback:^(BOOL completed){}];
        return;
    }
    YMKCameraPosition *cameraPosition = [mapView.mapWindow.map cameraPositionWithGeometry:[YMKGeometry geometryWithBoundingBox:[self calculateBoundingBox:points]]];
    cameraPosition = [YMKCameraPosition cameraPositionWithTarget:cameraPosition.target zoom:cameraPosition.zoom - 0.8f azimuth:cameraPosition.azimuth tilt:cameraPosition.tilt];
    [mapView.mapWindow.map moveWithCameraPosition:cameraPosition animation:anim cameraCallback:^(BOOL completed){}];
}

- (void)setLogoPosition:(NSDictionary *)logoPosition {
    YMKLogoHorizontalAlignment horizontalAlignment = YMKLogoHorizontalAlignmentRight;
    YMKLogoVerticalAlignment verticalAlignment = YMKLogoVerticalAlignmentBottom;

    if ([[logoPosition valueForKey:@"horizontal"] isEqual:@"left"]) {
        horizontalAlignment = YMKLogoHorizontalAlignmentLeft;
    } else if ([[logoPosition valueForKey:@"horizontal"] isEqual:@"center"]) {
        horizontalAlignment = YMKLogoHorizontalAlignmentCenter;
    }

    if ([[logoPosition valueForKey:@"vertical"] isEqual:@"top"]) {
        verticalAlignment = YMKLogoVerticalAlignmentTop;
    }

    [mapView.mapWindow.map.logo setAlignmentWithAlignment:[YMKLogoAlignment alignmentWithHorizontalAlignment:horizontalAlignment verticalAlignment:verticalAlignment]];
}
- (void)setMapStyle:(NSString *)mapStyle {
    [mapView.mapWindow.map setMapStyleWithStyle:mapStyle];
}

- (void)setLogoPadding:(NSDictionary *)logoPadding {
    NSUInteger horizontalPadding = [logoPadding valueForKey:@"horizontal"] != nil ? [RCTConvert NSUInteger:logoPadding[@"horizontal"]] : 0;
    NSUInteger verticalPadding = [logoPadding valueForKey:@"vertical"] != nil ? [RCTConvert NSUInteger:logoPadding[@"vertical"]] : 0;

    YMKLogoPadding *padding = [YMKLogoPadding paddingWithHorizontalPadding:horizontalPadding verticalPadding:verticalPadding];
    [mapView.mapWindow.map.logo setPaddingWithPadding:padding];
}

// PROPS
- (void)setUserLocationIcon:(NSString *)source {
    [[ImageCacheManager instance] getWithSource:source completion:^(UIImage *image) {
        self->userLocationImage = image;
        [self updateUserIcon];
    }];
}

#ifndef RCT_NEW_ARCH_ENABLED

- (void)setUserLocationIconScale:(NSNumber *)iconScale {
    userLocationImageScale = iconScale;
    [self updateUserIcon];
}

- (void)setUserLocationAccuracyFillColor:(NSNumber *)color {
    userLocationAccuracyFillColor = [RCTConvert UIColor:color];
    [self updateUserIcon];
}

- (void)setUserLocationAccuracyStrokeColor:(NSNumber *)color {
    userLocationAccuracyStrokeColor = [RCTConvert UIColor:color];
    [self updateUserIcon];
}

- (void)setUserLocationAccuracyStrokeWidth:(float)width {
    userLocationAccuracyStrokeWidth = width;
    [self updateUserIcon];
}

- (void)setMapStyle:(NSString *)style {
    [mapView.mapWindow.map setMapStyleWithStyle:style];
}

- (void)setZoomGesturesDisabled:(BOOL)value {
    mapView.mapWindow.map.zoomGesturesEnabled = !value;
}

- (void)setScrollGesturesDisabled:(BOOL)value {
    mapView.mapWindow.map.scrollGesturesEnabled = !value;
}

- (void)setTiltGesturesDisabled:(BOOL)value {
    mapView.mapWindow.map.tiltGesturesEnabled = !value;
}

- (void)setRotateGesturesDisabled:(BOOL)value {
    mapView.mapWindow.map.rotateGesturesEnabled = !value;
}

- (void)setFastTapDisabled:(BOOL)value {
    mapView.mapWindow.map.fastTapEnabled = !value;
}

#endif

- (void)updateUserIcon {
    if (userLocationView == nil) {
        return;
    }

    if (userLocationImage) {
        YMKIconStyle *userIconStyle = [[YMKIconStyle alloc] init];
        [userIconStyle setScale:userLocationImageScale];

        [userLocationView.pin setIconWithImage:userLocationImage style:userIconStyle];
        [userLocationView.arrow setIconWithImage:userLocationImage style:userIconStyle];
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

- (void)onObjectAddedWithView:(nonnull YMKUserLocationView *)view {
    userLocationView = view;
    [self updateUserIcon];
}

- (void)onObjectRemovedWithView:(nonnull YMKUserLocationView *)view {
}

- (void)onObjectUpdatedWithView:(nonnull YMKUserLocationView *)view event:(nonnull YMKObjectEvent *)event {
    userLocationView = view;
    [self updateUserIcon];
}

- (void)onMapTapWithMap:(nonnull YMKMap *)map
                  point:(nonnull YMKPoint *)point {

#ifdef RCT_NEW_ARCH_ENABLED

    if (!_eventEmitter) {
        return;
    }

    if ([self isKindOfClass:[ClusteredYamapView class]]) {
        std::dynamic_pointer_cast<const ClusteredYamapViewEventEmitter>(_eventEmitter)->onMapPress({.lat = point.latitude, .lon = point.longitude});
    } else {
        std::dynamic_pointer_cast<const YamapViewEventEmitter>(_eventEmitter)->onMapPress({.lat = point.latitude, .lon = point.longitude});
    }

#else

    if (self.onMapPress) {
        NSDictionary *data = @{
            @"lat": [NSNumber numberWithDouble:point.latitude],
            @"lon": [NSNumber numberWithDouble:point.longitude]
        };
        self.onMapPress(data);
    }

#endif

}

- (void)onMapLongTapWithMap:(nonnull YMKMap *)map
                      point:(nonnull YMKPoint *)point {

#ifdef RCT_NEW_ARCH_ENABLED

    if (!_eventEmitter) {
        return;
    }

    if ([self isKindOfClass:[ClusteredYamapView class]]) {
        std::dynamic_pointer_cast<const ClusteredYamapViewEventEmitter>(_eventEmitter)->onMapLongPress({.lat = point.latitude, .lon = point.longitude});
    } else {
        std::dynamic_pointer_cast<const YamapViewEventEmitter>(_eventEmitter)->onMapLongPress({.lat = point.latitude, .lon = point.longitude});
    }

#else

    if (self.onMapLongPress) {
        NSDictionary *data = @{
            @"lat": [NSNumber numberWithDouble:point.latitude],
            @"lon": [NSNumber numberWithDouble:point.longitude]
        };
        self.onMapLongPress(data);
    }

#endif

}

#ifndef RCT_NEW_ARCH_ENABLED

- (void)insertReactSubview:(UIView<RCTComponent> *)subview atIndex:(NSInteger)index {
    [self insertSubview:subview atIndex:index];
    [super insertReactSubview:subview atIndex:index];
}

#endif

- (void)insertSubview:(UIView *)subview atIndex:(NSInteger)index {
    if ([subview isKindOfClass:[PolygonView class]]) {
        YMKMapObjectCollection *objects = mapView.mapWindow.map.mapObjects;
        PolygonView *polygon = (PolygonView *) subview;
        YMKPolygonMapObject *obj = [objects addPolygonWithPolygon:[polygon getPolygon]];
        [polygon setMapObject:obj];
    } else if ([subview isKindOfClass:[PolylineView class]]) {
        YMKMapObjectCollection *objects = mapView.mapWindow.map.mapObjects;
        PolylineView *polyline = (PolylineView*) subview;
        YMKPolylineMapObject *obj = [objects addPolylineWithPolyline:[polyline getPolyline]];
        [polyline setMapObject:obj];
    } else if ([subview isKindOfClass:[MarkerView class]]) {
        MarkerView *marker = (MarkerView *) subview;
        if ([self isKindOfClass:[ClusteredYamapView class]]) {
            if (index<[clusterPlacemarks count]) {
                [marker setClusterMapObject:[clusterPlacemarks objectAtIndex:index]];
            }
        } else {
            YMKMapObjectCollection *objects = mapView.mapWindow.map.mapObjects;
            YMKPlacemarkMapObject *obj = [objects addPlacemark];
            [obj setIconWithImage:[[UIImage alloc] init]];
            [obj setGeometry:[marker getPoint]];
            [marker setMapObject:obj];
        }
    } else if ([subview isKindOfClass:[CircleView class]]) {
        YMKMapObjectCollection *objects = mapView.mapWindow.map.mapObjects;
        CircleView *circle = (CircleView*) subview;
        YMKCircleMapObject *obj = [objects addCircleWithCircle:[circle getCircle]];
        [circle setMapObject:obj];
    } else {
        NSArray<id<RCTComponent>> *childSubviews = [subview reactSubviews];
        for (int i = 0; i < childSubviews.count; i++) {
            [self insertSubview:(UIView *)childSubviews[i] atIndex:index];
        }
    }

    [_reactSubviews insertObject:subview atIndex:index];
    [super insertSubview:subview atIndex:index];
}

- (void)removeReactSubview:(UIView *)subview {
    if ([subview isKindOfClass:[PolygonView class]]) {
        YMKMapObjectCollection *objects = mapView.mapWindow.map.mapObjects;
        PolygonView *polygon = (PolygonView *) subview;
        [objects removeWithMapObject:[polygon getMapObject]];
    } else if ([subview isKindOfClass:[PolylineView class]]) {
        YMKMapObjectCollection *objects = mapView.mapWindow.map.mapObjects;
        PolylineView *polyline = (PolylineView *) subview;
        [objects removeWithMapObject:[polyline getMapObject]];
    } else if ([subview isKindOfClass:[MarkerView class]]) {
        YMKMapObjectCollection *objects = mapView.mapWindow.map.mapObjects;
        MarkerView *marker = (MarkerView *) subview;
        YMKPlacemarkMapObject *mapObject = [marker getMapObject];
        if ([self isKindOfClass:[ClusteredYamapView class]]) {
            [clusterPlacemarks removeObject:mapObject];
        } else {
            [objects removeWithMapObject:mapObject];
        }
    } else if ([subview isKindOfClass:[CircleView class]]) {
        YMKMapObjectCollection *objects = mapView.mapWindow.map.mapObjects;
        CircleView *circle = (CircleView *) subview;
        [objects removeWithMapObject:[circle getMapObject]];
    } else {
        NSArray<id<RCTComponent>> *childSubviews = [subview reactSubviews];
        for (int i = 0; i < childSubviews.count; i++) {
            [self removeReactSubview:(UIView *)childSubviews[i]];
        }
    }

    [_reactSubviews removeObject:subview];
    [super removeReactSubview: subview];
}

#ifdef RCT_NEW_ARCH_ENABLED

- (void)mountChildComponentView:(UIView<RCTComponentViewProtocol> *)childComponentView index:(NSInteger)index
{
    [self insertSubview:childComponentView atIndex:index];
}

- (void)unmountChildComponentView:(UIView<RCTComponentViewProtocol> *)childComponentView index:(NSInteger)index
{
    [self removeReactSubview:childComponentView];
}

#endif

- (void)setClusterColor: (NSNumber*) color {
    clusterColor = [RCTConvert UIColor:color];
}

- (void)setClusterIcon:(NSString *)source points:(NSArray<YMKPoint *> * _Nullable)points {
    [[ImageCacheManager instance] getWithSource:source completion:^(UIImage *image) {
        self->clusImage = image;
        if (points != nil) {
            [self setClusteredMarkers:points];
        }
    }];
}

- (void)setClusterSize:(NSDictionary *)sizes {
    clusterWidth = [sizes valueForKey:@"width"] != nil ? [RCTConvert NSUInteger:sizes[@"width"]] : 0;
    clusterHeight = [sizes valueForKey:@"height"] != nil ? [RCTConvert NSUInteger:sizes[@"height"]] : 0;
}

- (void)setClusterTextColor:(NSNumber *)color {
    clusterTextColor = [RCTConvert UIColor:color];
}

- (void)setClusterTextSize:(double)size {
    clusterTextSize = size;
}

- (void)setClusterTextYOffset:(double)offset {
    clusterTextYOffset = offset;
}

- (void)setClusterTextXOffset:(double)offset {
    clusterTextXOffset = offset;
}

- (void)setClusteredMarkers:(NSArray<YMKPoint*>*) markers {
    [clusterPlacemarks removeAllObjects];
    if (![clusterCollection isValid]) {
        clusterCollection = [mapView.mapWindow.map.mapObjects addClusterizedPlacemarkCollectionWithClusterListener:self];
    }
    [clusterCollection clear];
    UIImage *image = [self clusterImage:[NSNumber numberWithFloat:[markers count]]];
    NSArray<YMKPlacemarkMapObject *>* newPlacemarks = [clusterCollection addPlacemarksWithPoints:markers image:image style:[YMKIconStyle new]];
    [clusterPlacemarks addObjectsFromArray:newPlacemarks];
    for (int i=0; i<[clusterPlacemarks count]; i++) {
        if (i<[_reactSubviews count]) {
            UIView *subview = [_reactSubviews objectAtIndex:i];
            if ([subview isKindOfClass:[MarkerView class]]) {
                MarkerView *marker = (MarkerView*) subview;
                [marker setClusterMapObject:[clusterPlacemarks objectAtIndex:i]];
            }
        }
    }

#ifdef RCT_NEW_ARCH_ENABLED

    [self clusterPlacemarks];

#else

    if (mapLoaded) {
        [self clusterPlacemarks];
    }

#endif

}

-(UIImage*)clusterImage:(NSNumber*) clusterSize {
    NSString *text = [clusterSize stringValue];
    UIFont *font = [UIFont systemFontOfSize:clusterTextSize];
    CGSize size = [text sizeWithAttributes:@{NSFontAttributeName:font}];

    // This function returns a newImage, based on image, that has been:
    // - scaled to fit in (CGRect) rect
    // - and cropped within a circle of radius: rectWidth/2

    //Create the bitmap graphics context
    if(clusImage && clusterWidth != 0 && clusterHeight != 0) {
        UIGraphicsBeginImageContextWithOptions(CGSizeMake(clusterWidth, clusterHeight), NO, 1.0);
        [clusImage drawInRect:CGRectMake(0, 0, clusterWidth, clusterHeight)];
        [text drawInRect:CGRectMake(clusterWidth / 2  - size.width/2 + clusterTextXOffset, clusterHeight / 2 - size.height/2 + clusterTextYOffset, size.width, size.height) withAttributes:@{NSFontAttributeName: font, NSForegroundColorAttributeName: clusterTextColor }];
    } else {
        float MARGIN_SIZE = 9;
        float STROKE_SIZE = 9;

        float textRadius = sqrt(size.height * size.height + size.width * size.width) / 2;
        float internalRadius = textRadius + MARGIN_SIZE;
        float externalRadius = internalRadius + STROKE_SIZE;

        UIGraphicsBeginImageContextWithOptions(CGSizeMake(externalRadius*2, externalRadius*2), NO, 1.0);
        CGContextRef context = UIGraphicsGetCurrentContext();
        CGContextSetFillColorWithColor(context, [clusterColor CGColor]);
        CGContextFillEllipseInRect(context, CGRectMake(0, 0, externalRadius*2, externalRadius*2));
        CGContextSetFillColorWithColor(context, [UIColor.whiteColor CGColor]);
        CGContextFillEllipseInRect(context, CGRectMake(STROKE_SIZE, STROKE_SIZE, internalRadius*2, internalRadius*2));
        [text drawInRect:CGRectMake(externalRadius - size.width/2, externalRadius - size.height/2, size.width, size.height) withAttributes:@{NSFontAttributeName: font, NSForegroundColorAttributeName: clusterTextColor }];
    }

    UIImage *newImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();

    return newImage;
}

- (void) clusterPlacemarks {
    if ([clusterPlacemarks count] > 0) {
        [clusterCollection clusterPlacemarksWithClusterRadius:50 minZoom:12];
    }
}

- (void)onMapLoadedWithStatistics:(YMKMapLoadStatistics*)statistics {

#ifdef RCT_NEW_ARCH_ENABLED

    if (!_eventEmitter) {
        return;
    }

    if ([self isKindOfClass:[ClusteredYamapView class]]) {
        std::dynamic_pointer_cast<const ClusteredYamapViewEventEmitter>(_eventEmitter)->onMapLoaded({
            .renderObjectCount = [[NSNumber numberWithLong:statistics.renderObjectCount] doubleValue],
            .curZoomModelsLoaded = statistics.curZoomModelsLoaded,
            .curZoomPlacemarksLoaded = statistics.curZoomPlacemarksLoaded,
            .curZoomLabelsLoaded = statistics.curZoomLabelsLoaded,
            .curZoomGeometryLoaded = statistics.curZoomGeometryLoaded,
            .tileMemoryUsage = [[NSNumber numberWithUnsignedLong:statistics.tileMemoryUsage] doubleValue],
            .delayedGeometryLoaded = statistics.delayedGeometryLoaded,
            .fullyAppeared = statistics.fullyAppeared,
            .fullyLoaded = statistics.fullyLoaded,
        });
    } else {
        std::dynamic_pointer_cast<const YamapViewEventEmitter>(_eventEmitter)->onMapLoaded({
            .renderObjectCount = [[NSNumber numberWithLong:statistics.renderObjectCount] doubleValue],
            .curZoomModelsLoaded = statistics.curZoomModelsLoaded,
            .curZoomPlacemarksLoaded = statistics.curZoomPlacemarksLoaded,
            .curZoomLabelsLoaded = statistics.curZoomLabelsLoaded,
            .curZoomGeometryLoaded = statistics.curZoomGeometryLoaded,
            .tileMemoryUsage = [[NSNumber numberWithUnsignedLong:statistics.tileMemoryUsage] doubleValue],
            .delayedGeometryLoaded = statistics.delayedGeometryLoaded,
            .fullyAppeared = statistics.fullyAppeared,
            .fullyLoaded = statistics.fullyLoaded,
        });
    }

#else

    if (self.onMapLoaded) {
        NSDictionary *data = @{
            @"renderObjectCount": @(statistics.renderObjectCount),
            @"curZoomModelsLoaded": @(statistics.curZoomModelsLoaded),
            @"curZoomPlacemarksLoaded": @(statistics.curZoomPlacemarksLoaded),
            @"curZoomLabelsLoaded": @(statistics.curZoomLabelsLoaded),
            @"curZoomGeometryLoaded": @(statistics.curZoomGeometryLoaded),
            @"tileMemoryUsage": @(statistics.tileMemoryUsage),
            @"delayedGeometryLoaded": @(statistics.delayedGeometryLoaded),
            @"fullyAppeared": @(statistics.fullyAppeared),
            @"fullyLoaded": @(statistics.fullyLoaded),
        };
        self.onMapLoaded(data);
    }

    [self clusterPlacemarks];

#endif

    mapLoaded = YES;
}

- (void)setInteractiveDisabled:(BOOL)interactiveDisabled {
    [mapView setNoninteractive:interactiveDisabled];
}

- (void)onTrafficLoading {
}

- (void)onTrafficChangedWithTrafficLevel:(nullable YMKTrafficLevel *)trafficLevel {
}

- (void)onTrafficExpired {
}

- (YMKMapView *)getMapView {
    return mapView;
}

- (void)onClusterAddedWithCluster:(nonnull YMKCluster *)cluster {
    NSNumber *myNum = @([cluster size]);
    [[cluster appearance] setIconWithImage:[self clusterImage:myNum]];
    [cluster addClusterTapListenerWithClusterTapListener:self];
}

- (BOOL)onClusterTapWithCluster:(nonnull YMKCluster *)cluster {
    NSMutableArray<YMKPoint*>* lastKnownMarkers = [[NSMutableArray alloc] init];
    for (YMKPlacemarkMapObject *placemark in [cluster placemarks]) {
        [lastKnownMarkers addObject:[placemark geometry]];
    }
    [self fitMarkers:lastKnownMarkers duration:0.7 animation:0];
    return YES;
}

- (void)findRoutes:(NSArray<YMKRequestPoint *> *)_points {
//    __weak YamapView *weakSelf = self;
//
//    YMKMasstransitSessionRouteHandler _routeHandler = ^(NSArray<YMKMasstransitRoute *> *routes, NSError *error) {
//        YamapView *strongSelf = weakSelf;
//        if (error != nil) {
//            [self onReceiveNativeEvent: @{@"id": _id, @"status": @"error"}];
//            return;
//        }
//        NSMutableDictionary* response = [[NSMutableDictionary alloc] init];
//        [response setValue:_id forKey:@"id"];
//        [response setValue:@"status" forKey:@"success"];
//        NSMutableArray *jsonRoutes = [[NSMutableArray alloc] init];
//        for (int i = 0; i < [routes count]; ++i) {
//            YMKMasstransitRoute *_route = [routes objectAtIndex:i];
//            NSMutableDictionary *jsonRoute = [[NSMutableDictionary alloc] init];
//
//            [jsonRoute setValue:[NSString stringWithFormat:@"%d", i] forKey:@"id"];
//            NSMutableArray *sections = [[NSMutableArray alloc] init];
//            NSArray<YMKMasstransitSection *> *_sections = [_route sections];
//            for (int j = 0; j < [_sections count]; ++j) {
//                NSDictionary *jsonSection = [self convertRouteSection:_route withSection: [_sections objectAtIndex:j]];
//                [sections addObject:jsonSection];
//            }
//            [jsonRoute setValue:sections forKey:@"sections"];
//            [jsonRoutes addObject:jsonRoute];
//        }
//        [response setValue:jsonRoutes forKey:@"routes"];
//        [strongSelf onReceiveNativeEvent: response];
//    };
//    walkSession = [pedestrianRouter requestRoutesWithPoints:_points timeOptions:[[YMKTimeOptions alloc] init] routeOptions:routeOptions routeHandler:_routeHandler];
}

@end
