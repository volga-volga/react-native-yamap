#import "RTNSearchModule.h"

#ifdef USE_YANDEX_MAPS_FULL

#import "../Util/RCTConvert+Yamap.mm"

#import <YandexMapsMobile/YMKSearch.h>
#import <YandexMapsMobile/YMKSearchResponse.h>
#import <YandexMapsMobile/YMKSearchToponymObjectMetadata.h>
#import <YandexMapsMobile/YMKUriObjectMetadata.h>

#endif

@implementation RTNSearchModule

NSString *ERR_NO_REQUEST_ARG = @"ERR_NO_REQUEST_ARG";
NSString *ERR_SEARCH_FAILED = @"ERR_SEARCH_FAILED";

- (dispatch_queue_t)methodQueue {
    return dispatch_get_main_queue();
}

#ifdef USE_YANDEX_MAPS_FULL

- (instancetype)init
{
    YMKPoint *southWestPoint = [YMKPoint pointWithLatitude:-90.0 longitude:-180.0];
    YMKPoint *northEastPoint = [YMKPoint pointWithLatitude:90.0 longitude:180.0];
    _defaultBoundingBox = [YMKBoundingBox boundingBoxWithSouthWest:southWestPoint northEast:northEastPoint];
    return [super init];
}

- (void) initSearchManager {
    if (_searchManager == nil) {
        self->_searchManager = [[YMKSearchFactory instance] createSearchManagerWithSearchManagerType:YMKSearchManagerTypeOnline];
    }
}

- (YMKSearchSnippet) searchSnippetWithString:(NSString*) str {
    if ([str isEqual:@"PHOTOS"]) {
        return YMKSearchSnippetPhotos;
    }

    if ([str isEqual:@"BUSINESSRATING1X"]) {
        return YMKSearchSnippetBusinessRating1x;
    }

    if ([str isEqual:@"PANORAMAS"]) {
        return YMKSearchSnippetPanoramas;
    }

    return YMKSearchSnippetNone;
}

- (YMKSearchType) searchTypeWithString:(NSString*) str {
    if ([str isEqual:@"GEO"]) {
        return YMKSearchTypeGeo;
    }

    if ([str isEqual:@"BIZ"]) {
        return YMKSearchTypeBiz;
    }

    return YMKSearchTypeNone;
}


- (YMKGeometry *) getGeometry:(NSDictionary *) figure {
    if (![figure isKindOfClass:[NSDictionary class]]) {
        return [YMKGeometry geometryWithBoundingBox:_defaultBoundingBox];
    }
    if ([figure[@"type"] isEqual:@"POINT"]) {
        return [YMKGeometry geometryWithPoint:[RCTConvert YMKPoint:figure[@"value"]]];
    }
    if ([figure[@"type"] isEqual:@"BOUNDINGBOX"]) {
        YMKPoint *southWest = [RCTConvert YMKPoint:figure[@"value"][@"southWest"]];
        YMKPoint *northEast = [RCTConvert YMKPoint:figure[@"value"][@"northEast"]];
        return [YMKGeometry geometryWithBoundingBox:[YMKBoundingBox boundingBoxWithSouthWest:southWest northEast:northEast]];
    }
    if ([figure[@"type"] isEqual:@"POLYLINE"]) {
        NSArray<YMKPoint*> *points = [RCTConvert YMKPointArray:figure[@"value"]];
        return [YMKGeometry geometryWithPolyline:[YMKPolyline polylineWithPoints:points]];
    }
    if ([figure[@"type"] isEqual:@"POLYGON"]) {
        NSArray<YMKPoint*> *points = [RCTConvert YMKPointArray:figure[@"value"]];
        return [YMKGeometry geometryWithPolygon:[YMKPolygon polygonWithOuterRing:[YMKLinearRing linearRingWithPoints:points] innerRings:@[]]];
    }
    return [YMKGeometry geometryWithBoundingBox:_defaultBoundingBox];
}

- (NSMutableDictionary *) convertSearchResponse:(YMKSearchResponse *) search {
    NSMutableDictionary *searchToPass = [[NSMutableDictionary alloc] init];
    NSArray<YMKGeoObjectCollectionItem *> *geoCollectionObjects = [[search collection] children];

    NSObject *metadata = (NSObject *)[[[geoCollectionObjects firstObject].obj metadataContainer] getItemOfClass:[YMKSearchToponymObjectMetadata class]];

    if (metadata != nil) {
        searchToPass[@"formatted"] = [[metadata valueForKey:@"address"] valueForKey:@"formattedAddress"];
        searchToPass[@"country_code"] = [[metadata valueForKey:@"address"] valueForKey:@"countryCode"];
    }

    YMKPoint *point = [[[[geoCollectionObjects firstObject].obj geometry] firstObject] point];
    searchToPass[@"point"] = [RCTConvert pointJsonWithPoint:point];

    NSMutableArray *components = [[NSMutableArray alloc] init];

    for (YMKGeoObjectCollectionItem *geoCollectionObject in geoCollectionObjects) {
        NSMutableDictionary *component = [[NSMutableDictionary alloc] init];
        component[@"name"] = geoCollectionObject.obj.name;
        NSObject *metadata = (NSObject *)[[geoCollectionObject.obj metadataContainer] getItemOfClass:[YMKSearchToponymObjectMetadata class]];
        if (metadata != nil) {
            NSArray<YMKSearchAddressComponent *> *addresseDict = (NSArray<YMKSearchAddressComponent *> *)[[metadata valueForKey:@"address"] components];
            component[@"kind"] = [NSNumber numberWithInt:[[[[addresseDict objectAtIndex:[addresseDict count] - 1] kinds] firstObject] intValue]];
        }
        [components addObject:component];
    }

    searchToPass[@"Components"] = components;

    NSObject *uriMetadata = (NSObject *)[[[geoCollectionObjects firstObject].obj metadataContainer] getItemOfClass:[YMKUriObjectMetadata class]];
    if (uriMetadata) {
        searchToPass[@"uri"] =[[[uriMetadata valueForKey:@"uris"] firstObject] valueForKey:@"value"];
    }

    return searchToPass;
}

- (void)searchByAddressImpl:(nonnull NSString*) searchQuery figure:(NSDictionary*)figure searchOptions:(YMKSearchOptions*) searchOptions resolver:(RCTPromiseResolveBlock) resolve rejecter:(RCTPromiseRejectBlock) reject  {
    [self initSearchManager];

    YMKGeometry* geometry = [self getGeometry: figure];

    _searchSession = [self->_searchManager submitWithText:searchQuery geometry:geometry searchOptions:searchOptions responseHandler:^(YMKSearchResponse * _Nullable response, NSError * _Nullable error) {
        if (error) {
            reject(ERR_SEARCH_FAILED,  @"searchByAddress error:", error);
            return;
        }

        resolve([self convertSearchResponse:response]);
    }];
}

- (void)searchByPointImpl:(nonnull YMKPoint*) point zoom:(nonnull NSNumber*) zoom searchOptions:(YMKSearchOptions*) searchOptions resolver:(RCTPromiseResolveBlock) resolve rejecter:(RCTPromiseRejectBlock) reject {
    [self initSearchManager];

    _searchSession = [self->_searchManager submitWithPoint:point zoom:zoom searchOptions:searchOptions responseHandler:^(YMKSearchResponse * _Nullable response, NSError * _Nullable error) {
        if (error) {
            reject(ERR_SEARCH_FAILED,  @"searchByPoint error:", error);
            return;
        }

        resolve([self convertSearchResponse:response]);
    }];
}

- (void)addressToGeoImpl:(nonnull NSString *)address resolve:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
    [self initSearchManager];

    YMKSearchOptions *searchOptions = [[YMKSearchOptions alloc] init];

    _searchSession = [_searchManager submitWithText:address geometry:[YMKGeometry geometryWithBoundingBox:_defaultBoundingBox] searchOptions:searchOptions responseHandler: ^(YMKSearchResponse * _Nullable response, NSError * _Nullable error) {
        if (error) {
            reject(ERR_SEARCH_FAILED,  @"addressToGeo error:", error);
            return;
        }

        NSArray<YMKGeoObjectCollectionItem *> *geoCollectionObjects = [[response collection] children];
        NSObject *item = (NSObject *)[[[geoCollectionObjects firstObject].obj metadataContainer] getItemOfClass:[YMKSearchToponymObjectMetadata class]];
        if (item != nil) {
            YMKPoint *point = [item valueForKey:@"balloonPoint"];
            resolve([RCTConvert pointJsonWithPoint:point]);
        }
    }];
}

- (void)geoToAddressImpl:(YMKPoint *)point resolve:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
    [self initSearchManager];
    YMKSearchOptions *searchOptions = [[YMKSearchOptions alloc] init];

    _searchSession = [_searchManager submitWithPoint:point zoom:@10 searchOptions:searchOptions responseHandler:^(YMKSearchResponse * _Nullable response, NSError * _Nullable error) {
        if (error) {
            reject(ERR_SEARCH_FAILED,  @"geoToAddress error:", error);
            return;
        }

        resolve([self convertSearchResponse:response]);
    }];
}

- (void)searchByURIImpl:(nonnull NSString *)query searchOptions:(YMKSearchOptions*)searchOptions resolve:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
    [self initSearchManager];

    _searchSession = [_searchManager searchByURIWithUri:query searchOptions:searchOptions responseHandler:^(YMKSearchResponse * _Nullable response, NSError * _Nullable error) {
        if (error) {
            reject(ERR_SEARCH_FAILED,  @"searchByURI error:", error);
            return;
        }

        resolve([self convertSearchResponse:response]);
    }];
}

- (void)resolveURIImpl:(nonnull NSString *)query searchOptions:(YMKSearchOptions*)searchOptions resolve:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
    [self initSearchManager];

    _searchSession = [self->_searchManager resolveURIWithUri:query searchOptions:searchOptions responseHandler:^(YMKSearchResponse * _Nullable response, NSError * _Nullable error) {
        if (error) {
            reject(ERR_SEARCH_FAILED,  @"resolveURI error:", error);
            return;
        }

        resolve([self convertSearchResponse:response]);
    }];
}

#else

#endif

#ifdef RCT_NEW_ARCH_ENABLED

// New architecture

#ifdef USE_YANDEX_MAPS_FULL

- (YMKSearchOptions *) searchOptionsWithStruct:(JS::NativeSearchModule::SearchOptions &) options {
    YMKSearchOptions *searchOptions = [[YMKSearchOptions alloc] init];

    std::optional<bool> disableSpellingCorrection = options.disableSpellingCorrection();
    if (disableSpellingCorrection) {
        searchOptions.disableSpellingCorrection = *disableSpellingCorrection;
    }

    std::optional<bool> geometry = options.geometry();
    if (geometry) {
        searchOptions.geometry = *geometry;
    }

    std::optional<facebook::react::LazyVector<NSString *>> snippets = options.snippets();
    if (snippets) {
        FB::LazyVector<NSString *, id> values = snippets.value();
        for (int i=0; i<values.size(); i++) {
            searchOptions.snippets = searchOptions.snippets | [self searchSnippetWithString:values.at(i)];
        }
    }
    
    std::optional<facebook::react::LazyVector<NSString *>> searchTypes = options.searchTypes();
    if (searchTypes) {
        FB::LazyVector<NSString *, id> values = searchTypes.value();
        for (int i=0; i<values.size(); i++) {
            searchOptions.searchTypes = searchOptions.searchTypes | [self searchTypeWithString:values.at(i)];
        }
    }

    return searchOptions;
}

#endif

- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:(const facebook::react::ObjCTurboModule::InitParams &)params {
    return std::make_shared<facebook::react::NativeSearchModuleSpecJSI>(params);
}

- (void)searchByAddress:(nonnull NSString *)query figure:(nonnull NSDictionary *)figure options:(JS::NativeSearchModule::SearchOptions &)options resolve:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {

#ifdef USE_YANDEX_MAPS_FULL

    YMKSearchOptions *searchOptions = [self searchOptionsWithStruct: options];
    [self searchByAddressImpl:query figure:figure searchOptions:searchOptions resolver:resolve rejecter:reject];

#else

    reject(@"SEARCH_FAILED", @"SEARCH module is not available in Lite version", nil);

#endif

}

- (void)searchByPoint:(JS::NativeSearchModule::Point &)point zoom:(nonnull NSNumber *)zoom options:(JS::NativeSearchModule::SearchOptions &)options resolve:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {

#ifdef USE_YANDEX_MAPS_FULL

    YMKPoint *searchPoint = [YMKPoint pointWithLatitude:point.lat() longitude:point.lon()];
    YMKSearchOptions *searchOptions = [self searchOptionsWithStruct: options];
    [self searchByPointImpl:searchPoint zoom:zoom searchOptions:searchOptions resolver:resolve rejecter:reject];

#else

    reject(@"SEARCH_FAILED", @"SEARCH module is not available in Lite version", nil);

#endif

}

- (void)addressToGeo:(nonnull NSString *)address resolve:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {

#ifdef USE_YANDEX_MAPS_FULL

    [self addressToGeoImpl:address resolve:resolve reject:reject];

#else

    reject(@"SEARCH_FAILED", @"SEARCH module is not available in Lite version", nil);

#endif

}

- (void)geoToAddress:(JS::NativeSearchModule::Point &)point resolve:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {

#ifdef USE_YANDEX_MAPS_FULL

    YMKPoint *searchPoint = [YMKPoint pointWithLatitude:point.lat() longitude:point.lon()];
    [self geoToAddressImpl:searchPoint resolve:resolve reject:reject];

#else

    reject(@"SEARCH_FAILED", @"SEARCH module is not available in Lite version", nil);

#endif

}

- (void)searchByURI:(nonnull NSString *)query options:(JS::NativeSearchModule::SearchOptions &)options resolve:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {

#ifdef USE_YANDEX_MAPS_FULL

    YMKSearchOptions *searchOptions = [self searchOptionsWithStruct: options];
    [self searchByURIImpl:query searchOptions:searchOptions resolve:resolve reject:reject];

#else

    reject(@"SEARCH_FAILED", @"SEARCH module is not available in Lite version", nil);

#endif

}

- (void)resolveURI:(nonnull NSString *)query options:(JS::NativeSearchModule::SearchOptions &)options resolve:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {

#ifdef USE_YANDEX_MAPS_FULL

    YMKSearchOptions *searchOptions = [self searchOptionsWithStruct: options];
    [self resolveURIImpl:query searchOptions:searchOptions resolve:resolve reject:reject];

#else

    reject(@"SEARCH_FAILED", @"SEARCH module is not available in Lite version", nil);

#endif

}

#else

// Old architecture


#ifdef USE_YANDEX_MAPS_FULL

- (YMKSearchOptions *) searchOptionsWithDictionary:(NSDictionary *) options {
    YMKSearchOptions *searchOptions = [[YMKSearchOptions alloc] init];

    if ([options isKindOfClass:[NSDictionary class]]) {
        for (NSString* key in options) {
            if ([key isEqual:@"disableSpellingCorrection"]) {
                searchOptions.disableSpellingCorrection = options[key];
            } else if ([key isEqual:@"geometry"]) {
                searchOptions.geometry = options[key];
            } else if ([key isEqual:@"snippets"]) {
                NSArray<NSString *> *values = options[key];
                for (NSString *value in values) {
                    searchOptions.snippets = searchOptions.snippets | [self searchSnippetWithString:value];
                }
            } else if ([key isEqual:@"searchTypes"]) {
                NSArray<NSString *> *values = options[key];
                for (NSString *value in values) {
                    searchOptions.searchTypes = searchOptions.searchTypes | [self searchTypeWithString:value];
                }
            }
        }
    }
    
    return searchOptions;
}

#endif

RCT_EXPORT_METHOD(searchByAddress:(nonnull NSString*) query figure:(NSDictionary*)figure options:(NSDictionary*) options resolver:(RCTPromiseResolveBlock) resolve rejecter:(RCTPromiseRejectBlock) reject) {

#ifdef USE_YANDEX_MAPS_FULL

    YMKSearchOptions *searchOptions = [self searchOptionsWithDictionary: options];
    [self searchByAddressImpl:query figure:figure searchOptions:searchOptions resolver:resolve rejecter:reject];

#else

    reject(@"SEARCH_FAILED", @"SEARCH module is not available in Lite version", nil);

#endif

}

RCT_EXPORT_METHOD(searchByPoint:(nonnull NSDictionary*) point zoom:(nonnull NSNumber*) zoom options:(NSDictionary*) options resolver:(RCTPromiseResolveBlock) resolve rejecter:(RCTPromiseRejectBlock) reject) {

#ifdef USE_YANDEX_MAPS_FULL

    YMKPoint *searchPoint = [RCTConvert YMKPoint:point];
    YMKSearchOptions *searchOptions = [self searchOptionsWithDictionary: options];
    [self searchByPointImpl:searchPoint zoom:zoom searchOptions:searchOptions resolver:resolve rejecter:reject];

#else

    reject(@"SEARCH_FAILED", @"SEARCH module is not available in Lite version", nil);

#endif

}

RCT_EXPORT_METHOD(addressToGeo:(nonnull NSString*) address resolver:(RCTPromiseResolveBlock) resolve rejecter:(RCTPromiseRejectBlock) reject) {

#ifdef USE_YANDEX_MAPS_FULL

    [self addressToGeoImpl:address resolve:resolve reject:reject];

#else

    reject(@"SEARCH_FAILED", @"SEARCH module is not available in Lite version", nil);

#endif

}

RCT_EXPORT_METHOD(geoToAddress:(nonnull NSDictionary*) point resolver:(RCTPromiseResolveBlock) resolve rejecter:(RCTPromiseRejectBlock) reject) {

#ifdef USE_YANDEX_MAPS_FULL

    YMKPoint *searchPoint = [RCTConvert YMKPoint:point];
    [self geoToAddressImpl:searchPoint resolve:resolve reject:reject];

#else

    reject(@"SEARCH_FAILED", @"SEARCH module is not available in Lite version", nil);

#endif

}

RCT_EXPORT_METHOD(searchByURI:(nonnull NSString*) query options:(NSDictionary*) options resolver:(RCTPromiseResolveBlock) resolve rejecter:(RCTPromiseRejectBlock) reject) {

#ifdef USE_YANDEX_MAPS_FULL

    YMKSearchOptions *searchOptions = [self searchOptionsWithDictionary: options];
    [self searchByURIImpl:query searchOptions:searchOptions resolve:resolve reject:reject];

#else

    reject(@"SEARCH_FAILED", @"SEARCH module is not available in Lite version", nil);

#endif

}

RCT_EXPORT_METHOD(resolveURI:(nonnull NSString*) query options:(NSDictionary*) options resolver:(RCTPromiseResolveBlock) resolve rejecter:(RCTPromiseRejectBlock) reject) {

#ifdef USE_YANDEX_MAPS_FULL

    YMKSearchOptions *searchOptions = [self searchOptionsWithDictionary: options];
    [self resolveURIImpl:query searchOptions:searchOptions resolve:resolve reject:reject];
    
#else

    reject(@"SEARCH_FAILED", @"SEARCH module is not available in Lite version", nil);

#endif

}

#endif

RCT_EXPORT_MODULE()

@end
