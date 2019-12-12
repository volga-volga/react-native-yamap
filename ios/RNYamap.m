#import "RNYamap.h"
#import <YandexMapKit/YMKMapKitFactory.h>

@implementation yamap

static NSString * _pinIcon;
static NSString * _arrowIcon;
static NSString * _markerIcon;
static NSString * _selectedMarkerIcon;

@synthesize map;

- (instancetype) init {
    self = [super init];
    if (self) {
        map = [[YamapView alloc] init];
    }

    return self;
}

+ (NSString *)pinIcon {
    return _pinIcon;
}

+ (void)setPinIcon:(NSString *) icon {
    _pinIcon = icon;
}

+ (NSString *)arrowIcon {
    return _arrowIcon;
}

+ (void)setArrowIcon:(NSString *) icon {
    _arrowIcon = icon;
}

+ (NSString *)markerIcon {
    return _markerIcon;
}

+ (void)setMarkerIcon:(NSString *) icon {
    _markerIcon = icon;
}

+ (NSString *)selectedMarkerIcon {
    return _selectedMarkerIcon;
}

+ (void)setSelectedMarkerIcon:(NSString *) icon {
    _selectedMarkerIcon = icon;
}

- (void)initWithKey:(NSString *) apiKey {
    [YMKMapKit setApiKey: apiKey];
}

- (dispatch_queue_t)methodQueue{
    return dispatch_get_main_queue();
}

RCT_EXPORT_METHOD(init: (NSString *) apiKey) {
    yamap *map = [[yamap alloc] init];
    [map initWithKey: apiKey];
}

RCT_EXPORT_MODULE()

@end
