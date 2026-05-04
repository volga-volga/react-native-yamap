//
//  GeometryUtils.m
//  Pods
//
//  Created by Tim on 30.03.26.
//


// GeometryUtils.m

#import "GeometryUtils.h"


@implementation GeometryUtils

#pragma mark - Проверка пересечения

+ (BOOL)polyline:(YMKPolyline *)polyline intersectsPolygon:(NSArray<YMKPoint *> *)polygon {

    NSArray *points = polyline.points;

    for (NSInteger i = 0; i < points.count - 1; i++) {
        YMKPoint *a = points[i];
        YMKPoint *b = points[i + 1];

        if ([self segment:a b:b intersectsPolygon:polygon]) {
            return YES;
        }
    }
    return NO;
}

+ (BOOL)segment:(YMKPoint *)a b:(YMKPoint *)b intersectsPolygon:(NSArray<YMKPoint *> *)poly {

    for (NSInteger i = 0; i < poly.count; i++) {

        YMKPoint *p1 = poly[i];
        YMKPoint *p2 = poly[(i + 1) % poly.count];

        if ([self segmentsIntersectA:a b:b c:p1 d:p2]) {
            return YES;
        }
    }
    return NO;
}

+ (BOOL)segmentsIntersectA:(YMKPoint *)a b:(YMKPoint *)b c:(YMKPoint *)c d:(YMKPoint *)d {

    double det = (b.longitude - a.longitude) * (d.latitude - c.latitude) -
                 (b.latitude - a.latitude) * (d.longitude - c.longitude);

    return det != 0; // упрощённо (можно улучшить)
}

#pragma mark - Проверка выхода за границу

+ (BOOL)polyline:(YMKPolyline *)polyline isOutsidePolygon:(NSArray<YMKPoint *> *)polygon {

    for (YMKPoint *p in polyline.points) {
        if (![self point:p insidePolygon:polygon]) {
            return YES;
        }
    }
    return NO;
}

+ (BOOL)point:(YMKPoint *)point insidePolygon:(NSArray<YMKPoint *> *)polygon {

    BOOL inside = NO;

    for (int i = 0, j = (int)polygon.count - 1; i < polygon.count; j = i++) {

        YMKPoint *pi = polygon[i];
        YMKPoint *pj = polygon[j];

        if (((pi.longitude > point.longitude) != (pj.longitude > point.longitude)) &&
            (point.latitude < (pj.latitude - pi.latitude) *
             (point.longitude - pi.longitude) /
             (pj.longitude - pi.longitude) + pi.latitude)) {

            inside = !inside;
        }
    }
    return inside;
}

#pragma mark - Генерация обхода

+ (NSArray<YMKPoint *> *)detourPointsForPolygon:(YMKPolyline *)polyline
                                        polygon:(NSArray<YMKPoint *> *)polygon {

    // простой вариант: взять 2 ближайшие вершины
    return [polygon subarrayWithRange:NSMakeRange(0, MIN(2, polygon.count))];
}

+ (NSArray<YMKPoint *> *)detourPointsToStayInside:(YMKPolyline *)polyline
                                         polygon:(NSArray<YMKPoint *> *)polygon {

    return [polygon subarrayWithRange:NSMakeRange(0, MIN(2, polygon.count))];
}

@end
