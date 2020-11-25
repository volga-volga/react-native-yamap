#ifndef RNYMView_h
#define RNYMView_h
#import <React/RCTComponent.h>
#import <YandexMapKit/YMKMapView.h>

@class RCTBridge;

@interface RNYMView: YMKMapView<YMKUserLocationObjectListener, YMKMapCameraListener, RCTComponent>

@property (nonatomic, copy) RCTBubblingEventBlock _Nullable onRouteFound;
@property (nonatomic, copy) RCTBubblingEventBlock _Nullable onCameraPositionReceived;
@property (nonatomic, copy) RCTBubblingEventBlock _Nullable onVisibleRegionReceived;
@property (nonatomic, copy) RCTBubblingEventBlock _Nullable onCameraPositionChange;
@property (nonatomic, copy) RCTBubblingEventBlock _Nullable onMapPress;
@property (nonatomic, copy) RCTBubblingEventBlock _Nullable onMapLongPress;

// ref
-(void) emitCameraPositionToJS:(NSString*_Nonnull) _id;
-(void) emitVisibleRegionToJS:(NSString*_Nonnull) _id;
-(void) setCenter:(YMKCameraPosition*_Nonnull) position withDuration:(float) duration withAnimation:(int) animation;
-(void) setZoom:(float) zoom withDuration:(float) duration withAnimation:(int) animation;
-(void) fitAllMarkers;
-(void) findRoutes:(NSArray<YMKRequestPoint*>*_Nonnull) points vehicles:(NSArray<NSString*>*_Nonnull) vehicles withId:(NSString*_Nonnull)_id;

// props
-(void) setNightMode:(BOOL)nightMode;
-(void) setListenUserLocation:(BOOL)listen;
-(void) setUserLocationIcon:(NSString*_Nullable) iconSource;

-(void) setUserLocationAccuracyFillColor: (UIColor*_Nullable) color;
-(void) setUserLocationAccuracyStrokeColor: (UIColor*_Nullable) color;
-(void) setUserLocationAccuracyStrokeWidth: (float) width;

@end

#endif /* RNYMView_h */
