#ifndef PolylineView_h
#define PolylineView_h

#ifdef RCT_NEW_ARCH_ENABLED

#import <React/RCTViewComponentView.h>

#else

#import <React/RCTComponent.h>

#endif

#import <YandexMapsMobile/YMKPolyline.h>

NS_ASSUME_NONNULL_BEGIN

#ifdef RCT_NEW_ARCH_ENABLED

@interface PolylineView: RCTViewComponentView

#else

@interface PolylineView: UIView<YMKMapObjectTapListener>

@property (nonatomic, copy) RCTBubblingEventBlock onPress;

#endif

- (YMKPolyline*)getPolyline;
- (YMKPolylineMapObject*)getMapObject;
- (void)setMapObject:(YMKPolylineMapObject*)mapObject;

@end

NS_ASSUME_NONNULL_END

#endif /* PolylineView_h */
