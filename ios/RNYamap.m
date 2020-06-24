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

RCT_EXPORT_METHOD(setLocale: (NSString *) locale successCallback:(RCTResponseSenderBlock)successCb errorCallback:(RCTResponseSenderBlock) errorCb) {
    [YRTI18nManagerFactory setLocaleWithLanguage:locale localeUpdateDelegate:^(NSError * _Nullable error) {
        if (error == nil) {
            successCb(@[]);
        } else {
            NSString* errMsg = [error localizedDescription];
            errorCb(@[errMsg]);
        }
    }];
}

RCT_EXPORT_METHOD(resetLocale:(RCTResponseSenderBlock)successCb errorCallback:(RCTResponseSenderBlock) errorCb) {
    [YRTI18nManagerFactory resetLocaleWithLocaleResetDelegate:^(NSError * _Nullable error) {
        if (error == nil) {
            successCb(@[]);
        } else {
            NSString* errMsg = [error localizedDescription];
            errorCb(@[errMsg]);
        }
    }];
}

RCT_EXPORT_METHOD(getLocale:(RCTResponseSenderBlock)successCb errorCallback:(RCTResponseSenderBlock) errorCb) {
    [YRTI18nManagerFactory getLocaleWithLocaleDelegate:^(NSString * _Nullable locale, NSError * _Nullable error) {
        if (error == nil) {
            successCb(@[locale]);
        } else {
            NSString* errMsg = [error localizedDescription];
            errorCb(@[errMsg]);
        }
    }];
}

RCT_EXPORT_MODULE()

@end
