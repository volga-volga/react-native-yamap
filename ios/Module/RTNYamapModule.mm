#import "RTNYamapModule.h"

#import <YandexMapsMobile/YMKMapKitFactory.h>
#import <YandexMapsMobile/YRTI18nManager.h>

@implementation RTNYamapModule

- (dispatch_queue_t)methodQueue {
    return dispatch_get_main_queue();
}

- (void)initImpl:(NSString *) apiKey resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject {
    @try {
        [YMKMapKit setApiKey: apiKey];
        [[YMKMapKit sharedInstance] onStart];
        resolve(nil);
    } @catch (NSException *exception) {
        NSError *error = nil;
        if (exception.userInfo.count > 0) {
            error = [NSError errorWithDomain:NSCocoaErrorDomain code:0 userInfo:exception.userInfo];
        }
        reject(exception.name, exception.reason ?: @"Error initiating YMKMapKit", error);
    }
}

- (void)setLocaleImpl:(NSString *) locale resolver:(RCTPromiseResolveBlock)resolve {
    [YRTI18nManagerFactory setLocaleWithLocale:locale];
    resolve(nil);
}

- (void)getLocaleImpl:(nonnull RCTPromiseResolveBlock)resolve {
    NSString *locale = [YRTI18nManagerFactory getLocale];
    resolve(locale);
}

#ifdef RCT_NEW_ARCH_ENABLED

// New architecture

- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:(const facebook::react::ObjCTurboModule::InitParams &)params {
    return std::make_shared<facebook::react::NativeYamapModuleSpecJSI>(params);
}

- (void)init:(nonnull NSString *)apiKey resolve:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
    [self initImpl:apiKey resolver:resolve rejecter:reject];
}

- (void)setLocale:(nonnull NSString *)locale resolve:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
    [self setLocaleImpl:locale resolver:resolve];
}

- (void)resetLocale:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
    [self setLocaleImpl:nil resolver:resolve];
}

- (void)getLocale:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
    [self getLocaleImpl:resolve];
}

#else

// Old architecture

RCT_EXPORT_METHOD(init: (NSString *) apiKey resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [self initImpl:apiKey resolver:resolve rejecter:reject];
}

RCT_EXPORT_METHOD(setLocale: (NSString *) locale resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [self setLocaleImpl:locale resolver:resolve];
}

RCT_EXPORT_METHOD(resetLocale:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [self setLocaleImpl:nil resolver:resolve];
}

RCT_EXPORT_METHOD(getLocale:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [self getLocaleImpl:resolve];
}

#endif

RCT_EXPORT_MODULE()

@end
