#import "RTNSuggestsModule.h"

#ifdef USE_YANDEX_MAPS_FULL

#import "../Util/RCTConvert+Yamap.mm"

#import <YandexMapsMobile/YMKSearch.h>
#import <YandexMapsMobile/YMKSearchSuggestSession.h>
#import <YandexMapsMobile/YMKSuggestResponse.h>

#endif

@implementation RTNSuggestsModule

NSString *ERR_SUGGEST_FAILED = @"ERR_SUGGEST_FAILED";

- (dispatch_queue_t)methodQueue {
    return dispatch_get_main_queue();
}

#ifdef USE_YANDEX_MAPS_FULL

- (instancetype) init {
    YMKPoint *southWestPoint = [YMKPoint pointWithLatitude:-90.0 longitude:-180.0];
    YMKPoint *northEastPoint = [YMKPoint pointWithLatitude:90.0 longitude:180.0];
    _defaultBoundingBox = [YMKBoundingBox boundingBoxWithSouthWest:southWestPoint northEast:northEastPoint];
    _defaultSuggestOptions = [YMKSuggestOptions suggestOptionsWithSuggestTypes:YMKSuggestTypeUnspecified userPosition:nil suggestWords:false strictBounds:false];
    return [super init];
}

- (YMKSearchSuggestSession*) getSuggestClient {
    if (_suggestClient) {
        return _suggestClient;
    }

    if (_searchManager == nil) {
        _searchManager = [[YMKSearchFactory instance] createSearchManagerWithSearchManagerType:YMKSearchManagerTypeOnline];
    }

    _suggestClient = [self->_searchManager createSuggestSession];

    return _suggestClient;
}

- (void) suggestImpl:(nonnull NSString*) searchQuery options:(YMKSuggestOptions*) options boundingBox:(YMKBoundingBox*) boundingBox resolver:(RCTPromiseResolveBlock) resolve rejecter:(RCTPromiseRejectBlock) reject {
    YMKSearchSuggestSession *session = [self getSuggestClient];

    [session suggestWithText:searchQuery window:boundingBox suggestOptions:options responseHandler:^(YMKSuggestResponse * _Nullable suggest, NSError * _Nullable error) {
        if (error) {
            reject(ERR_SUGGEST_FAILED, @"suggest error:", error);
            return;
        }

        NSMutableArray *suggestsToPass = [NSMutableArray new];

        if (suggest) {
            for (YMKSuggestItem *suggestItem in suggest.items) {
                NSMutableDictionary *suggestToPass = [NSMutableDictionary new];
                [suggestToPass setObject:suggestItem.title.text forKey:@"title"];
                if (suggestItem.subtitle) {
                    [suggestToPass setObject:suggestItem.subtitle.text forKey:@"subtitle"];
                }
                if (suggestItem.uri) {
                    [suggestToPass setObject:suggestItem.uri forKey:@"uri"];
                }
                [suggestsToPass addObject:suggestToPass];
            }
        }

        resolve(suggestsToPass);
    }];
}

- (YMKSuggestType) suggestTypeWithString:(NSString*) str {
    if ([str isEqual:@"GEO"]) {
        return YMKSuggestTypeGeo;
    }

    if ([str isEqual:@"BIZ"]) {
        return YMKSuggestTypeBiz;
    }

    if ([str isEqual:@"TRANSIT"]) {
        return YMKSuggestTypeTransit;
    }

    return YMKSuggestTypeUnspecified;
}

- (void)resetImpl:(RCTPromiseResolveBlock) resolve  {
    if (_suggestClient) {
        [_suggestClient reset];
        _suggestClient = nil;
    }

    resolve(@[]);
}

#endif

#ifdef RCT_NEW_ARCH_ENABLED

// New architecture

- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:(const facebook::react::ObjCTurboModule::InitParams &)params {
    return std::make_shared<facebook::react::NativeSuggestsModuleSpecJSI>(params);
}

- (void)suggest:(nonnull NSString *)query resolve:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {

#ifdef USE_YANDEX_MAPS_FULL

    [self suggestImpl:query options:_defaultSuggestOptions boundingBox:_defaultBoundingBox resolver:resolve rejecter:reject];

#else

    reject(@"SUGGESTS_FAILED", @"SUGGESTS module is not available in Lite version", nil);

#endif

}

- (void)suggestWithOptions:(nonnull NSString *)query options:(JS::NativeSuggestsModule::SuggestOptions &)options resolve:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {

#ifdef USE_YANDEX_MAPS_FULL

    YMKSuggestOptions *suggestOptions = [[YMKSuggestOptions alloc] init];

    std::optional<bool> suggestWords = options.suggestWords();
    if (suggestWords) {
        suggestOptions.suggestWords = *suggestWords;
    }

    std::optional<facebook::react::LazyVector<NSString *>> suggestTypes = options.suggestTypes();

    if (suggestTypes) {
        FB::LazyVector<NSString *, id> values = suggestTypes.value();
        for (int i=0; i<values.size(); i++) {
            suggestOptions.suggestTypes = suggestOptions.suggestTypes | [self suggestTypeWithString:values.at(i)];
        }
    }

    std::optional<JS::NativeSuggestsModule::Point> userPosition = options.userPosition();
    if (userPosition) {
        JS::NativeSuggestsModule::Point value = userPosition.value();
        suggestOptions.userPosition = [YMKPoint pointWithLatitude:value.lat() longitude:value.lon()];
    }

    YMKBoundingBox *boundingBox = _defaultBoundingBox;
    std::optional<JS::NativeSuggestsModule::BoundingBox> boundingBoxJS = options.boundingBox();
    if (boundingBoxJS) {
        YMKPoint *southWest = [YMKPoint pointWithLatitude:boundingBoxJS.value().southWest().lat() longitude:boundingBoxJS.value().southWest().lon()];
        YMKPoint *northEast = [YMKPoint pointWithLatitude:boundingBoxJS.value().northEast().lat() longitude:boundingBoxJS.value().northEast().lon()];

        boundingBox = [YMKBoundingBox boundingBoxWithSouthWest:southWest northEast:northEast];
    }

    [self suggestImpl:query options:suggestOptions boundingBox:boundingBox resolver:resolve rejecter:reject];

#else

    reject(@"SUGGESTS_FAILED", @"SUGGESTS module is not available in Lite version", nil);

#endif

}

- (void)reset:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {

#ifdef USE_YANDEX_MAPS_FULL

    [self resetImpl:resolve];

#else

    reject(@"SUGGESTS_FAILED", @"SUGGESTS module is not available in Lite version", nil);

#endif

}

#else

// Old architecture

RCT_EXPORT_METHOD(suggest:(nonnull NSString*) query resolver:(RCTPromiseResolveBlock) resolve rejecter:(RCTPromiseRejectBlock) reject) {

#ifdef USE_YANDEX_MAPS_FULL

    [self suggestImpl:query options:_defaultSuggestOptions boundingBox:_defaultBoundingBox resolver:resolve rejecter:reject];

#else

    reject(@"SUGGESTS_FAILED", @"SUGGESTS module is not available in Lite version", nil);

#endif

}

RCT_EXPORT_METHOD(suggestWithOptions:(nonnull NSString*) query options:(NSDictionary *) options resolver:(RCTPromiseResolveBlock) resolve rejecter:(RCTPromiseRejectBlock) reject) {

#ifdef USE_YANDEX_MAPS_FULL

    YMKSuggestOptions *suggestOptions = [[YMKSuggestOptions alloc] init];

    id suggestWords = [options valueForKey:@"suggestWords"];
    if (suggestWords != nil) {
        suggestOptions.suggestWords = suggestWords;
    }

    id suggestTypes = [options valueForKey:@"suggestTypes"];
    if (suggestTypes != nil) {
        for (NSString *suggestType in suggestTypes) {
            suggestOptions.suggestTypes = suggestOptions.suggestTypes | [self suggestTypeWithString:suggestType];
        }
    }

    id userPosition = [options valueForKey:@"userPosition"];
    if (userPosition != nil) {
        suggestOptions.userPosition = [RCTConvert YMKPoint:userPosition];
    }

    YMKBoundingBox *boundingBox = _defaultBoundingBox;
    id boxDictionary = [options valueForKey:@"boundingBox"];
    if (userPosition != nil) {
        id southWest = [boxDictionary valueForKey:@"southWest"];
        id northEast = [boxDictionary valueForKey:@"northEast"];

        boundingBox = [YMKBoundingBox boundingBoxWithSouthWest:[RCTConvert YMKPoint:southWest] northEast:[RCTConvert YMKPoint:northEast]];
    }

    [self suggestImpl:query options:suggestOptions boundingBox:boundingBox resolver:resolve rejecter:reject];

#else

    reject(@"SUGGESTS_FAILED", @"SUGGESTS module is not available in Lite version", nil);

#endif

}

RCT_EXPORT_METHOD(reset: (RCTPromiseResolveBlock) resolve rejecter:(RCTPromiseRejectBlock) reject) {

#ifdef USE_YANDEX_MAPS_FULL

    [self resetImpl:resolve];

#else

    reject(@"SUGGESTS_FAILED", @"SUGGESTS module is not available in Lite version", nil);

#endif

}

#endif

RCT_EXPORT_MODULE()

@end
