#ifndef RNYMView_h
#define RNYMView_h
#import <React/RCTComponent.h>
#import <YandexMapKit/YMKMapView.h>

#import "RNMarker.h"

@interface RNYMView: YMKMapView<YMKMapObjectTapListener, YMKUserLocationObjectListener>

@property (nonatomic, copy) RCTBubblingEventBlock onMarkerPress;
@property (nonatomic, copy) RCTBubblingEventBlock onRouteFound;

@property (nonatomic) NSMutableArray<RNMarker*> * _Nullable markers;
@property (nonatomic) YMKUserLocationView* _Nullable userLocationView;
@property (nonatomic) NSMutableDictionary<NSString*, YMKPlacemarkMapObject*> * _Nullable markersDict;
@property (nonatomic) UIImage* _Nullable userLocationImage;

-(void) onStart;

// ref
-(void) setCenter:(YMKPoint*) center withZoom:(float) zoom;
-(void) fitAllMarkers;

// props
-(void) setMarkers:(NSMutableArray<RNMarker*>*) markerList;
-(void) clearRoute;
-(void) setRouteWithStart:(YMKRequestPoint*) start end:(YMKRequestPoint*) end;
-(void) setAcceptedVehicleTypes:(NSArray*) acceptVehicleTypes;
-(void) setVehicleColors:(NSDictionary*) _vehicleColors;
-(void) setUserLocationIcon:(NSString*) iconSource;

@end

#endif /* RNYMView_h */
