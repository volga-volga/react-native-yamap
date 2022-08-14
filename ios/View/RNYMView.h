#ifndef RNYMView_h
#define RNYMView_h
#import <React/RCTComponent.h>

#import <MapKit/MapKit.h>
@import YandexMapsMobile;

@class RCTBridge;

@interface RNYMView: YMKMapView<YMKUserLocationObjectListener, YMKMapCameraListener, RCTComponent, YMKClusterListener, YMKClusterTapListener>

@property (nonatomic, assign) CGRect mapFrame;
@property (nonatomic, copy) RCTBubblingEventBlock _Nullable onRouteFound;
@property (nonatomic, copy) RCTBubblingEventBlock _Nullable onCameraPositionReceived;
@property (nonatomic, copy) RCTBubblingEventBlock _Nullable onVisibleRegionReceived;
@property (nonatomic, copy) RCTBubblingEventBlock _Nullable onCameraPositionChange;
@property (nonatomic, copy) RCTBubblingEventBlock _Nullable onCameraPositionChangeEnd;
@property (nonatomic, copy) RCTBubblingEventBlock _Nullable onMapPress;
@property (nonatomic, copy) RCTBubblingEventBlock _Nullable onMapLongPress;

// REF
- (void)emitCameraPositionToJS:(NSString*_Nonnull)_id;
- (void)emitVisibleRegionToJS:(NSString*_Nonnull)_id;
- (void)setCenter:(YMKCameraPosition*_Nonnull)position withDuration:(float)duration withAnimation:(int)animation;
- (void)setZoom:(float)zoom withDuration:(float)duration withAnimation:(int)animation;
- (void)setMapType:(NSString*_Nullable)type;
- (void)setInitialRegion:(NSDictionary*_Nullable)type;
- (void)fitAllMarkers;
- (void)fitMarkers: (NSArray<YMKPoint*>*_Nonnull)points;
- (void)findRoutes:(NSArray<YMKRequestPoint*>*_Nonnull)points vehicles:(NSArray<NSString*>*_Nonnull)vehicles withId:(NSString*_Nonnull)_id;

// PROPS
- (void)setNightMode:(BOOL)nightMode;
- (void)setClusters:(BOOL)userClusters;
- (void)setListenUserLocation:(BOOL)listen;
- (void)setUserLocationIcon:(NSString*_Nullable)iconSource;
- (void)setUserLocationAccuracyFillColor:(UIColor*_Nullable)color;
- (void)setClusterColor:(UIColor*_Nullable)color;
- (void)setUserLocationAccuracyStrokeColor:(UIColor*_Nullable)color;
- (void)setUserLocationAccuracyStrokeWidth:(float)width;

@end

#endif /* RNYMView_h */
