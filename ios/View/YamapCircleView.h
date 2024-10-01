#ifndef YamapCircleView_h
#define YamapCircleView_h
#import <React/RCTComponent.h>
@import YandexMapsMobile;

@interface YamapCircleView: UIView<YMKMapObjectTapListener>

@property (nonatomic, copy) RCTBubblingEventBlock onPress;

// PROPS
- (void)setFillColor:(UIColor*)color;
- (void)setStrokeColor:(UIColor*)color;
- (void)setStrokeWidth:(NSNumber*)width;
- (void)setZIndex:(NSNumber*)_zIndex;
- (void)setCircleCenter:(YMKPoint*)center;
- (void)setRadius:(float)radius;
- (YMKCircle*)getCircle;
- (YMKPolygonMapObject*)getMapObject;
- (void)setMapObject:(YMKCircleMapObject*)mapObject;
- (void)setHandled:(BOOL)_handled;

@end

#endif /* YamapCircleView */
