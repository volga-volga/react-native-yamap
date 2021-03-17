#ifndef YamapMarkerView_h
#define YamapMarkerView_h
#import <React/RCTComponent.h>
@import YandexMapsMobile;

@class RCTBridge;

@interface YamapMarkerView: UIView<YMKMapObjectTapListener, RCTComponent>

@property (nonatomic, copy) RCTBubblingEventBlock onPress;

// props
-(void) setZIndex:(NSNumber*) _zIndex;
-(void) setScale:(NSNumber*) _scale;
-(void) setSource:(NSString*) _source;
-(void) setPoint:(YMKPoint*) _points;
-(void) setAnchor:(NSValue*) _anchor;

-(YMKPoint*) getPoint;
-(YMKPlacemarkMapObject*) getMapObject;
-(void) setMapObject:(YMKPlacemarkMapObject*) mapObject;

@end

#endif /* YamapMarkerView_h */
