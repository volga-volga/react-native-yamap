#ifndef RNYMView_h
#define RNYMView_h

#import <YandexMapKit/YMKMapView.h>

@interface RNYMView: YMKMapView

@property (nonatomic, copy) RCTBubblingEventBlock onMarkerPress;

@end

#endif /* RNYMView_h */
