#import "RTNCacheModule.h"

#ifdef USE_YANDEX_MAPS_FULL
#import <YandexMapsMobile/YMKMapKitFactory.h>
#import <YandexMapsMobile/YMKOfflineCacheManager.h>
#import <YandexMapsMobile/YMKOfflineCacheRegion.h>

@interface RTNRegionListUpdatesListener : NSObject <YMKOfflineMapRegionListUpdatesListener>
@end

@implementation RTNRegionListUpdatesListener
- (void)onListUpdated {
}
@end

#endif

@implementation RTNCacheModule

NSString *ERR_CACHE_FAILED = @"CACHE_FAILED";

#ifdef USE_YANDEX_MAPS_FULL
static YMKOfflineCacheManager *cacheManager = nil;
static RTNRegionListUpdatesListener *regionListUpdatesListener = nil;

- (void)initCacheManager {
    if (cacheManager == nil) {
        cacheManager = [YMKMapKit sharedInstance].offlineCacheManager;
        [cacheManager enableAutoUpdateWithEnable:YES];
        regionListUpdatesListener = [RTNRegionListUpdatesListener new];
        [cacheManager addRegionListUpdatesListenerWithRegionListUpdatesListener:regionListUpdatesListener];
    }
}

- (NSDictionary *)pointToJSON:(YMKPoint *)point {
    return @{
        @"lat": @(point.latitude),
        @"lon": @(point.longitude),
    };
}

- (void)initManager:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject {
    [self initCacheManager];
    resolve(nil);
}

- (void)searchRegionsImpl:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject {
    [self initCacheManager];

    NSArray<YMKOfflineCacheRegion *> *regions = [cacheManager regions];
    NSMutableArray<NSDictionary *> *regionsToPass = [NSMutableArray new];

    for (YMKOfflineCacheRegion *region in regions) {
        NSMutableDictionary *regionToPass = [NSMutableDictionary new];
        regionToPass[@"id"] = @(region.id);
        regionToPass[@"name"] = region.name;
        regionToPass[@"center"] = [self pointToJSON:region.center];
        regionToPass[@"size"] = @(region.size.value);
        regionToPass[@"releaseTime"] = region.releaseTime;
        regionToPass[@"parentId"] = region.parentId;
        regionToPass[@"description"] = region.description;
        [regionsToPass addObject:regionToPass];
    }

    resolve(regionsToPass);
}

- (void)getRegionInfoImpl:(nonnull NSNumber *)regionId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject {
    [self initCacheManager];
    YMKOfflineCacheRegionState regionState = [cacheManager getStateWithRegionId:regionId.unsignedIntegerValue];
    resolve(@(regionState));
}

- (void)startDownloadRegionImpl:(nonnull NSNumber *)regionId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject {
    [self initCacheManager];
    BOOL mayBeOutOfSpace = [cacheManager mayBeOutOfAvailableSpaceWithRegionId:regionId.unsignedIntegerValue];
    if (mayBeOutOfSpace) {
        resolve(@(NO));
    } else {
        resolve(@(YES));
    }
}

- (void)stopDownloadRegionImpl:(nonnull NSNumber *)regionId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject {
    [self initCacheManager];
    [cacheManager stopDownloadWithRegionId:regionId.unsignedIntegerValue];
    resolve(nil);
}

- (void)pauseDownloadRegionImpl:(nonnull NSNumber *)regionId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject {
    [self initCacheManager];
    [cacheManager pauseDownloadWithRegionId:regionId.unsignedIntegerValue];
    resolve(nil);
}

- (void)dropRegionImpl:(nonnull NSNumber *)regionId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject {
    [self initCacheManager];
    [cacheManager dropWithRegionId:regionId.unsignedIntegerValue];
    resolve(nil);
}

#else

- (void)initImpl:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject {
    reject(ERR_CACHE_FAILED, @"CACHE module is not available in Lite version", nil);
}

- (void)searchRegionsImpl:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject {
    reject(ERR_CACHE_FAILED, @"CACHE module is not available in Lite version", nil);
}

- (void)getRegionInfoImpl:(nonnull NSNumber *)regionId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject {
    reject(ERR_CACHE_FAILED, @"CACHE module is not available in Lite version", nil);
}

- (void)startDownloadRegionImpl:(nonnull NSNumber *)regionId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject {
    reject(ERR_CACHE_FAILED, @"CACHE module is not available in Lite version", nil);
}

- (void)stopDownloadRegionImpl:(nonnull NSNumber *)regionId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject {
    reject(ERR_CACHE_FAILED, @"CACHE module is not available in Lite version", nil);
}

- (void)pauseDownloadRegionImpl:(nonnull NSNumber *)regionId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject {
    reject(ERR_CACHE_FAILED, @"CACHE module is not available in Lite version", nil);
}

- (void)dropRegionImpl:(nonnull NSNumber *)regionId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject {
    reject(ERR_CACHE_FAILED, @"CACHE module is not available in Lite version", nil);
}

#endif

- (dispatch_queue_t)methodQueue {
    return dispatch_get_main_queue();
}

#ifdef RCT_NEW_ARCH_ENABLED

- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:(const facebook::react::ObjCTurboModule::InitParams &)params {
    return std::make_shared<facebook::react::NativeCacheModuleSpecJSI>(params);
}

- (void)initManager:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
    [self initManager:resolve rejecter:reject];
}

- (void)searchRegions:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
    [self searchRegionsImpl:resolve rejecter:reject];
}

- (void)getRegionInfo:(double)regionId resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
    NSNumber *regionIdNumber = @(regionId);
    [self getRegionInfoImpl:regionIdNumber resolver:resolve rejecter:reject];
}

- (void)startDownloadRegion:(double)regionId resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
    NSNumber *regionIdNumber = @(regionId);
    [self startDownloadRegionImpl:regionIdNumber resolver:resolve rejecter:reject];
}

- (void)stopDownloadRegion:(double)regionId resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
    NSNumber *regionIdNumber = @(regionId);
    [self stopDownloadRegionImpl:regionIdNumber resolver:resolve rejecter:reject];
}

- (void)pauseDownloadRegion:(double)regionId resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
    NSNumber *regionIdNumber = @(regionId);
    [self pauseDownloadRegionImpl:regionIdNumber resolver:resolve rejecter:reject];
}

- (void)dropRegion:(double)regionId resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
    NSNumber *regionIdNumber = @(regionId);
    [self dropRegionImpl:regionIdNumber resolver:resolve rejecter:reject];
}

#else

RCT_EXPORT_METHOD(init:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [self initImpl:resolve rejecter:reject];
}

RCT_EXPORT_METHOD(searchRegions:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [self searchRegionsImpl:resolve rejecter:reject];
}

RCT_EXPORT_METHOD(getRegionInfo:(nonnull NSNumber *)regionId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [self getRegionInfoImpl:regionId resolver:resolve rejecter:reject];
}

RCT_EXPORT_METHOD(startDownloadRegion:(nonnull NSNumber *)regionId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [self startDownloadRegionImpl:regionId resolver:resolve rejecter:reject];
}

RCT_EXPORT_METHOD(stopDownloadRegion:(nonnull NSNumber *)regionId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [self stopDownloadRegionImpl:regionId resolver:resolve rejecter:reject];
}

RCT_EXPORT_METHOD(pauseDownloadRegion:(nonnull NSNumber *)regionId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [self pauseDownloadRegionImpl:regionId resolver:resolve rejecter:reject];
}

RCT_EXPORT_METHOD(dropRegion:(nonnull NSNumber *)regionId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [self dropRegionImpl:regionId resolver:resolve rejecter:reject];
}

#endif

RCT_EXPORT_MODULE()

@end
