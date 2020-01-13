#ifndef YamapPolygonView_h
#define YamapPolygonView_h
#import <React/RCTComponent.h>
#import <YandexMapKit/YMKPolygonMapObject.h>
#import <YandexMapKit/YMKPoint.h>

@interface YamapPolygonView: UIView<YMKMapObjectTapListener>

@property (nonatomic, copy) RCTBubblingEventBlock onPress;

@property (nonatomic) NSMutableArray<YMKPoint*> * _points;
@property (nonatomic) YMKPolygonMapObject* mapObject;
@property (nonatomic) YMKPolygon* polygon;
@property (nonatomic) UIColor* fillColor;
@property (nonatomic) UIColor* strokeColor;
@property (nonatomic) NSNumber* strokeWidth;
@property (nonatomic) NSNumber* zIndex;

// props
-(void) setFillColor:(UIColor*) color;
-(void) setStrokeColor:(UIColor*) color;
-(void) setStrokeWidth:(NSNumber*) width;
-(void) setZIndex:(NSNumber*) _zIndex;
-(void) setPolygonPoints:(YMKPoint*) _points;

-(NSMutableArray<YMKPoint*>*) getPoints;
-(YMKPolygon*) getPolygon;
-(YMKPolygonMapObject*) getMapObject;

@end

#endif /* YamapPoligonView */
