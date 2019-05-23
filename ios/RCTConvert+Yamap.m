#import <React/RCTConvert.h>

#import <YandexMapKit/YMKMapKitFactory.h>

@interface RCTConvert(Yamap)

+ (YMKPoint *)YMKPoint:(id)json;

@end

@implementation RCTConvert (Yamap)

+ (YMKPoint *)YMKPoint:(id)json {
    json = [self NSDictionary:json];
    YMKPoint *target = [YMKPoint pointWithLatitude:[self double:json[@"lat"]] longitude:[self double:json[@"lon"]]];
    return target;
}

+ (float)Zoom:(id)json {
    json = [self NSDictionary:json];
    return [self float:json[@"zoom"]];
}

@end
