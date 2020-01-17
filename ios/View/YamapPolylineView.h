#ifndef YamapPolylineView_h
#define YamapPolylineView_h
#import <React/RCTComponent.h>
#import <YandexMapKit/YMKPolygonMapObject.h>
#import <YandexMapKit/YMKPoint.h>

@interface YamapPolylineView: UIView<YMKMapObjectTapListener>

@property (nonatomic, copy) RCTBubblingEventBlock onPress;

// props
-(void) setOutlineColor:(UIColor*) color;
-(void) setStrokeColor:(UIColor*) color;
-(void) setStrokeWidth:(NSNumber*) width;
-(void) setDashLength:(NSNumber*) length;
-(void) setGapLength:(NSNumber*) length;
-(void) setDashOffset:(NSNumber*) offset;
-(void) setOutlineWidth:(NSNumber*) width;
-(void) setZIndex:(NSNumber*) _zIndex;
-(void) setPolylinePoints:(NSArray<YMKPoint*>*) _points;

-(NSMutableArray<YMKPoint*>*) getPoints;
-(YMKPolyline*) getPolyline;
-(YMKPolylineMapObject*) getMapObject;
-(void) setMapObject:(YMKPolylineMapObject*) mapObject;

@end

#endif /* YamapPolylineView */
