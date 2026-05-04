#ifndef PolygonView_h
#define PolygonView_h

#ifdef RCT_NEW_ARCH_ENABLED

#import <React/RCTViewComponentView.h>

#else

#import <React/RCTComponent.h>

#endif

#import <YandexMapsMobile/YMKPolygon.h>

NS_ASSUME_NONNULL_BEGIN

#ifdef RCT_NEW_ARCH_ENABLED

@interface PolygonView: RCTViewComponentView

#else

@interface PolygonView: UIView<YMKMapObjectTapListener>

@property (nonatomic, copy) RCTBubblingEventBlock onPress;

#endif

- (YMKPolygon*)getPolygon;
- (YMKPolygonMapObject*)getMapObject;
- (void)setMapObject:(YMKPolygonMapObject*)mapObject;

@end

NS_ASSUME_NONNULL_END

#endif /* PolygonView_h */
