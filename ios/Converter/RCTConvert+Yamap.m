#import <React/RCTConvert.h>
#import <Foundation/Foundation.h>
#import <YandexMapKit/YMKMapKitFactory.h>
#import "RNMarker.h"

@interface RCTConvert(Yamap)

+ (YMKPoint *)YMKPoint:(id)json;

@end

@implementation RCTConvert (Yamap)

+ (YMKPoint *)YMKPoint:(id)json {
    json = [self NSDictionary:json];
    YMKPoint *target = [YMKPoint pointWithLatitude:[self double:json[@"lat"]] longitude:[self double:json[@"lon"]]];
    return target;
}

+ (NSArray *)Vehicles:(id)json {
    return [self NSArray:json];
}

+ (NSDictionary *)RouteColors:(id)json {
    return [self NSDictionary:json];
}

+ (NSMutableArray<RNMarker *> *)Markers:(id)json {
    NSArray *parsedArray = [self NSArray:json];
    NSMutableArray *result = [[NSMutableArray alloc] init];
    for (NSDictionary *jsonMarker in parsedArray) {
        [result addObject:[[RNMarker alloc] initWithJson:jsonMarker]];
    }
    return result;
}

+ (NSMutableArray<YMKPoint *> *)Points:(id)json {
    NSArray *parsedArray = [self NSArray:json];
    NSMutableArray *result = [[NSMutableArray alloc] init];
    for (NSDictionary *jsonMarker in parsedArray) {
        double lat = [[jsonMarker valueForKey:@"lat"] doubleValue];
        double lon = [[jsonMarker valueForKey:@"lon"] doubleValue];
        YMKPoint *point = [YMKPoint pointWithLatitude:lat longitude:lon];
        [result addObject:point];
    }
    return result;
}

+(NSMutableDictionary *)RouteDict:(id)json {
    json = [self NSDictionary:json];
    NSMutableDictionary* route = [[NSMutableDictionary alloc] init];

    [route setObject:[YMKPoint pointWithLatitude:[self double:json[@"start"][@"lat"]] longitude:[self double:json[@"start"][@"lon"]]] forKey:@"start"];
    [route setObject:[YMKPoint pointWithLatitude:[self double:json[@"end"][@"lat"]] longitude:[self double:json[@"end"][@"lon"]]] forKey:@"end"];

    return route;
}

+ (float)Zoom:(id)json {
    json = [self NSDictionary:json];
    return [self float:json[@"zoom"]];
}

@end
