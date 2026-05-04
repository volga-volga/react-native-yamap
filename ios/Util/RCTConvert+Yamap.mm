#import <React/RCTConvert.h>

#import <YandexMapsMobile/YMKPoint.h>
#import <YandexMapsMobile/YMKScreenTypes.h>

@interface RCTConvert(Yamap)

@end

@implementation RCTConvert(Yamap)

+ (YMKPoint*)YMKPoint:(id)json {
    json = [self NSDictionary:json];
    YMKPoint *target = [YMKPoint pointWithLatitude:[self double:json[@"lat"]] longitude:[self double:json[@"lon"]]];

    return target;
}

+ (YMKScreenPoint*)YMKScreenPoint:(id)json {
    json = [self NSDictionary:json];
    YMKScreenPoint *target = [YMKScreenPoint screenPointWithX:[self float:json[@"x"]] y:[self float:json[@"y"]]];

    return target;
}

+ (NSArray*)Vehicles:(id)json {
    return [self NSArray:json];
}

+ (NSArray<YMKPoint*>*)YMKPointArray:(id)json {
    NSMutableArray* result = [[NSMutableArray alloc] init];

    for (NSDictionary* jsonMarker in json) {
        double lat = [[jsonMarker valueForKey:@"lat"] doubleValue];
        double lon = [[jsonMarker valueForKey:@"lon"] doubleValue];
        YMKPoint *point = [YMKPoint pointWithLatitude:lat longitude:lon];
        [result addObject:point];
    }

    return result;
}

+ (NSArray<NSArray<YMKPoint*>*>*)YMKPointArrayArray:(id)json {
    NSMutableArray* result = [[NSMutableArray alloc] init];

    for (int i = 0; i < [(NSArray *)json count]; ++i) {
        [result addObject:[RCTConvert YMKPointArray:[json objectAtIndex:i]]];
    }

    return result;
}

+ (NSArray<YMKScreenPoint*>*)ScreenPoints:(id)json {
    NSMutableArray* result = [[NSMutableArray alloc] init];

    for (NSDictionary* jsonMarker in json) {
        float x = [[jsonMarker valueForKey:@"x"] floatValue];
        float y = [[jsonMarker valueForKey:@"y"] floatValue];
        YMKScreenPoint *point = [YMKScreenPoint screenPointWithX:x y:y];
        [result addObject:point];
    }

    return result;
}

+ (float)Zoom:(id)json {
    json = [self NSDictionary:json];
    return [self float:json[@"zoom"]];
}

+ (float)Azimuth:(id)json {
    json = [self NSDictionary:json];
    return [self float:json[@"azimuth"]];
}

+ (float)Tilt:(id)json {
    json = [self NSDictionary:json];
    return [self float:json[@"tilt"]];
}

+ (NSDictionary*)pointJsonWithPoint:(YMKPoint*)point {
    NSMutableDictionary *pointJs = [[NSMutableDictionary alloc] init];
    pointJs[@"lat"] = [point valueForKey:@"latitude"];
    pointJs[@"lon"] = [point valueForKey:@"longitude"];

    return pointJs;
}

+ (NSValue*)NSValue:(id)json {
    json = [self NSDictionary:json];
    return [NSValue valueWithCGPoint:CGPointMake([json[@"x"] floatValue], [json[@"y"] floatValue])];
}

@end
