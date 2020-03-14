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

- (void)initWithKey:(NSString *) apiKey {
    [YMKMapKit setApiKey: apiKey];
}

- (dispatch_queue_t)methodQueue{
    return dispatch_get_main_queue();
}

RCT_EXPORT_METHOD(init: (NSString *) apiKey) {
    [self initWithKey: apiKey];
}

RCT_EXPORT_MODULE()

@end
