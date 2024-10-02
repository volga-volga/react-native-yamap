#ifndef YamapPolygonView_h
#define YamapPolygonView_h
#import <React/RCTComponent.h>
@import YandexMapsMobile;

@interface YamapPolygonView: UIView<YMKMapObjectTapListener>

@property (nonatomic, copy) RCTBubblingEventBlock onPress;

// PROPS
- (void)setFillColor:(UIColor*)color;
- (void)setStrokeColor:(UIColor*)color;
- (void)setStrokeWidth:(NSNumber*)width;
- (void)setZIndex:(NSNumber*)_zIndex;
- (void)setPolygonPoints:(NSArray<YMKPoint*>*)_points;
- (void)setInnerRings:(NSArray<NSArray<YMKPoint*>*>*)_innerRings;
- (NSMutableArray<YMKPoint*>*)getPoints;
- (YMKPolygon*)getPolygon;
- (YMKPolygonMapObject*)getMapObject;
- (void)setMapObject:(YMKPolygonMapObject*)mapObject;
- (void)setHandled:(BOOL)_handled;

@end

#endif /* YamapPoligonView */
