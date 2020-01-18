#ifndef RNYMView_h
#define RNYMView_h
#import <React/RCTComponent.h>
#import <YandexMapKit/YMKMapView.h>

@class RCTBridge;

@interface RNYMView: YMKMapView<YMKUserLocationObjectListener, RCTComponent>

@property (nonatomic, copy) RCTBubblingEventBlock onRouteFound;

@property (nonatomic) YMKUserLocationView* _Nullable userLocationView;
@property (nonatomic) UIImage* _Nullable userLocationImage;

// ref
-(void) setCenter:(YMKPoint*) center withZoom:(float) zoom;
-(void) fitAllMarkers;

// props
-(void) clearRoute;
-(void) setRouteWithStart:(YMKRequestPoint*) start end:(YMKRequestPoint*) end;
-(void) setAcceptedVehicleTypes:(NSArray*) acceptVehicleTypes;
-(void) setVehicleColors:(NSDictionary*) _vehicleColors;
-(void) setUserLocationIcon:(NSString*) iconSource;

@end

#endif /* RNYMView_h */
