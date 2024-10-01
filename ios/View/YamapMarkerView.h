#ifndef YamapMarkerView_h
#define YamapMarkerView_h
#import <React/RCTComponent.h>
@import YandexMapsMobile;

@class RCTBridge;

@interface YamapMarkerView: UIView<YMKMapObjectTapListener, RCTComponent>

@property (nonatomic, copy) RCTBubblingEventBlock onPress;

// PROPS
- (void)setZIndex:(NSNumber*)_zIndex;
- (void)setScale:(NSNumber*)_scale;
- (void)setRotated:(NSNumber*)_rotation;
- (void)setSource:(NSString*)_source;
- (void)setPoint:(YMKPoint*)_points;
- (void)setAnchor:(NSValue*)_anchor;
- (void)setVisible:(NSNumber*)_visible;
- (void)setHandled:(BOOL)_visible;

// REF
- (void)animatedMoveTo:(YMKPoint*)point withDuration:(float)duration;
- (void)animatedRotateTo:(float)angle withDuration:(float)duration;
- (YMKPoint*)getPoint;
- (YMKPlacemarkMapObject*)getMapObject;
- (void)setMapObject:(YMKPlacemarkMapObject*)mapObject;
- (void)setClusterMapObject:(YMKPlacemarkMapObject*)mapObject;

@end

#endif /* YamapMarkerView_h */
