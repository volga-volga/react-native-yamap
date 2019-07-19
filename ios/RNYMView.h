#ifndef RNYMView_h
#define RNYMView_h
#import <React/RCTComponent.h>

#import <YandexMapKit/YMKMapView.h>

@interface RNYMView: YMKMapView

@property (nonatomic, copy) RCTBubblingEventBlock onMarkerPress;
@property (nonatomic, copy) RCTBubblingEventBlock onRouteFound;

@end

#endif /* RNYMView_h */
