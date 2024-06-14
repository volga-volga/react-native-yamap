#ifndef RNCYMView_h
#define RNCYMView_h
#import <React/RCTComponent.h>

#import <MapKit/MapKit.h>
#import <RNYMView.h>
@import YandexMapsMobile;

@class RCTBridge;

@interface RNCYMView: RNYMView<YMKClusterListener, YMKClusterTapListener>

- (void)setClusterColor:(UIColor*_Nullable)color;
- (void)setClusteredMarkers:(NSArray<YMKRequestPoint*>*_Nonnull)points;
- (void)setInitialRegion:(NSDictionary *_Nullable)initialRegion;

@end

#endif /* RNYMView_h */
