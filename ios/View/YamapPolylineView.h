#ifndef YamapPolylineView_h
#define YamapPolylineView_h
#import <React/RCTComponent.h>
@import YandexMapsMobile;

@interface YamapPolylineView: UIView<YMKMapObjectTapListener>

@property (nonatomic, copy) RCTBubblingEventBlock onPress;

// PROPS
- (void)setOutlineColor:(UIColor*)color;
- (void)setStrokeColor:(UIColor*)color;
- (void)setStrokeWidth:(NSNumber*)width;
- (void)setDashLength:(NSNumber*)length;
- (void)setGapLength:(NSNumber*)length;
- (void)setDashOffset:(NSNumber*)offset;
- (void)setOutlineWidth:(NSNumber*)width;
- (void)setZIndex:(NSNumber*)_zIndex;
- (void)setPolylinePoints:(NSArray<YMKPoint*>*)_points;
- (NSMutableArray<YMKPoint*>*)getPoints;
- (YMKPolyline*)getPolyline;
- (YMKPolylineMapObject*)getMapObject;
- (void)setMapObject:(YMKPolylineMapObject*)mapObject;
- (void)setHandled:(BOOL)_handled;

@end

#endif /* YamapPolylineView */
