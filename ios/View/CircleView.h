#ifndef CircleView_h
#define CircleView_h

#ifdef RCT_NEW_ARCH_ENABLED

#import <React/RCTViewComponentView.h>

#else

#import <React/RCTComponent.h>

#endif

#import <YandexMapsMobile/YMKCircle.h>
#import <YandexMapsMobile/YMKPolygon.h>

NS_ASSUME_NONNULL_BEGIN

#ifdef RCT_NEW_ARCH_ENABLED

@interface CircleView: RCTViewComponentView

#else

@interface CircleView: UIView<YMKMapObjectTapListener>

@property (nonatomic, copy) RCTBubblingEventBlock onPress;

// PROPS
- (void)setCircleCenter:(YMKPoint*)center;

#endif

- (YMKCircle*)getCircle;
- (YMKPolygonMapObject*)getMapObject;
- (void)setMapObject:(YMKCircleMapObject*)mapObject;

@end

NS_ASSUME_NONNULL_END

#endif /* CircleView_h */
