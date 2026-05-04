#ifndef MarkerView_h
#define MarkerView_h

#ifdef RCT_NEW_ARCH_ENABLED

#import <React/RCTViewComponentView.h>

#else

#import <React/RCTComponent.h>

#endif

#import <YandexMapsMobile/YMKPlacemark.h>

NS_ASSUME_NONNULL_BEGIN

#ifdef RCT_NEW_ARCH_ENABLED

@interface MarkerView: RCTViewComponentView

#else

@interface MarkerView: UIView<YMKMapObjectTapListener, RCTComponent>

@property (nonatomic, copy) RCTBubblingEventBlock onPress;

// REF
- (void)animatedMoveTo:(YMKPoint*)point withDuration:(float)duration;
- (void)animatedRotateTo:(float)angle withDuration:(float)duration;

- (void)setAnchor:(NSValue*)anchor;

#endif

- (YMKPoint*)getPoint;
- (YMKPlacemarkMapObject*)getMapObject;
- (void)setMapObject:(YMKPlacemarkMapObject*)mapObject;
- (void)setClusterMapObject:(YMKPlacemarkMapObject*)mapObject;

@end

NS_ASSUME_NONNULL_END

#endif /* MarkerView_h */
