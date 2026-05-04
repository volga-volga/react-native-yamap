//
//  GeometryUtils.h
//  Pods
//
//  Created by Tim on 30.03.26.
//


// GeometryUtils.h

#import <YandexMapsMobile/YMKPolyline.h>
#import <YandexMapsMobile/YMKPoint.h>

@interface GeometryUtils : NSObject

+ (BOOL)polyline:(YMKPolyline *)polyline intersectsPolygon:(NSArray<YMKPoint *> *)polygon;
+ (BOOL)polyline:(YMKPolyline *)polyline isOutsidePolygon:(NSArray<YMKPoint *> *)polygon;

+ (NSArray<YMKPoint *> *)detourPointsForPolygon:(YMKPolyline *)polyline
                                        polygon:(NSArray<YMKPoint *> *)polygon;

+ (NSArray<YMKPoint *> *)detourPointsToStayInside:(YMKPolyline *)polyline
                                         polygon:(NSArray<YMKPoint *> *)polygon;

@end
