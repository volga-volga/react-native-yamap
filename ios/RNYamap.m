#import "RNYamap.h"
@import YandexMapsMobile;

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

+ (BOOL)requiresMainQueueSetup
{
    return YES;
}

- (void)initWithKey:(NSString *) apiKey {
    [YMKMapKit setApiKey: apiKey];
    [[YMKMapKit sharedInstance] onStart];
}

- (dispatch_queue_t)methodQueue{
    return dispatch_get_main_queue();
}

RCT_EXPORT_METHOD(init: (NSString *) apiKey
          resolver:(RCTPromiseResolveBlock)resolve
          rejecter:(RCTPromiseRejectBlock)reject) {
    @try {
        [self initWithKey: apiKey];
        resolve(nil);
    } @catch (NSException *exception) {
        NSError *error = nil;
        if (exception.userInfo.count > 0) {
            error = [NSError errorWithDomain:NSCocoaErrorDomain code:0 userInfo:exception.userInfo];
        }
        reject(exception.name, exception.reason ?: @"Error initiating YMKMapKit", error);
    }
}

RCT_EXPORT_METHOD(setLocale: (NSString *) locale successCallback:(RCTResponseSenderBlock)successCb errorCallback:(RCTResponseSenderBlock) errorCb) {
    [YRTI18nManagerFactory setLocaleWithLocale:locale];
    successCb(@[]);
}

RCT_EXPORT_METHOD(resetLocale:(RCTResponseSenderBlock)successCb errorCallback:(RCTResponseSenderBlock) errorCb) {
    [YRTI18nManagerFactory setLocaleWithLocale:nil];
    successCb(@[]);
}

RCT_EXPORT_METHOD(getLocale:(RCTResponseSenderBlock)successCb errorCallback:(RCTResponseSenderBlock) errorCb) {
    NSString * locale = [YRTI18nManagerFactory getLocale];
    successCb(@[locale]);
}

RCT_EXPORT_MODULE()

@end
