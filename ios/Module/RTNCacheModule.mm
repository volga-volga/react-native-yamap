#import "RTNCacheModule.h"

#ifdef USE_YANDEX_MAPS_FULL
#import <YandexMapsMobile/YMKMapKitFactory.h>
#import <YandexMapsMobile/YMKOfflineCacheManager.h>
#import <YandexMapsMobile/YMKOfflineCacheRegion.h>

@class RTNCacheModule;
@interface RTNCacheModule (RegionUpdates)
- (void)onRegionStateChangedWithRegionId:(NSUInteger)regionId;
- (void)onRegionProgressWithRegionId:(NSUInteger)regionId;
@end

@interface RTNRegionListUpdatesListener : NSObject <YMKOfflineMapRegionListUpdatesListener, YMKOfflineCacheRegionListener>

@property(nonatomic, weak) RTNCacheModule *module;

- (instancetype)initWithModule:(RTNCacheModule *)module;
@end

@implementation RTNRegionListUpdatesListener

- (instancetype)initWithModule:(RTNCacheModule *)module {
    self = [super init];
    if (self) {
        _module = module;
    }
    return self;
}

- (void)onListUpdated {
}

- (void)onRegionStateChangedWithRegionId:(NSUInteger)regionId {
    [_module onRegionStateChangedWithRegionId:regionId];
}

- (void)onRegionProgressWithRegionId:(NSUInteger)regionId {
    [_module onRegionProgressWithRegionId:regionId];
}
@end

#endif

@implementation RTNCacheModule

NSString *ERR_CACHE_FAILED = @"CACHE_FAILED";
NSString *EVENT_REGION_STATE_CHANGED = @"cacheRegionStateChanged";
NSString *EVENT_REGION_PROGRESS = @"cacheRegionProgress";
BOOL hasRegionEventsListeners = NO;

- (void)emitCacheEvent:(NSString *)eventName payload:(NSDictionary *)payload {
#ifdef RCT_NEW_ARCH_ENABLED
    if ([eventName isEqualToString:EVENT_REGION_STATE_CHANGED]) {
        [self emitOnRegionStateChanged:payload];
    } else if ([eventName isEqualToString:EVENT_REGION_PROGRESS]) {
        [self emitOnRegionProgress:payload];
    }
#else
    if (hasRegionEventsListeners) {
        [self sendEventWithName:eventName body:payload];
    }
#endif
}

#ifdef USE_YANDEX_MAPS_FULL
static YMKOfflineCacheManager *cacheManager = nil;
static RTNRegionListUpdatesListener *regionListUpdatesListener = nil;

- (void)initCacheManager {
    if (cacheManager == nil) {
        cacheManager = [YMKMapKit sharedInstance].offlineCacheManager;
        [cacheManager enableAutoUpdateWithEnable:YES];
    }

    if (regionListUpdatesListener == nil) {
        regionListUpdatesListener = [[RTNRegionListUpdatesListener alloc] initWithModule:self];
        [cacheManager addRegionListenerWithRegionListener:regionListUpdatesListener];
        [cacheManager addRegionListUpdatesListenerWithRegionListUpdatesListener:regionListUpdatesListener];
    } else if (regionListUpdatesListener.module != self) {
        regionListUpdatesListener.module = self;
    }
}

- (NSDictionary *)pointToJSON:(YMKPoint *)point {
    return @{
        @"lat": @(point.latitude),
        @"lon": @(point.longitude),
    };
}

- (void)initImpl:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject {
    [self initCacheManager];
    resolve(nil);
}

- (void)allowUseCellularNetworkImpl:(BOOL)useCellular resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject {
    [self initCacheManager];
    [cacheManager allowUseCellularNetworkWithUseCellular:useCellular];
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
        [cacheManager startDownloadWithRegionId:regionId.unsignedIntegerValue];
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

- (void)onRegionStateChangedWithRegionId:(NSUInteger)regionId {
//    [self initCacheManager];
    YMKOfflineCacheRegionState state = [cacheManager getStateWithRegionId:regionId];
    [self emitCacheEvent:EVENT_REGION_STATE_CHANGED payload:@{
        @"regionId": @(regionId),
        @"state": @(state),
    }];
}

- (void)onRegionProgressWithRegionId:(NSUInteger)regionId {
//    [self initCacheManager];
    float progress = [cacheManager getProgressWithRegionId:regionId];
    [self emitCacheEvent:EVENT_REGION_PROGRESS payload:@{
        @"regionId": @(regionId),
        @"progress": @(progress),
    }];
}

#else

- (void)initImpl:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject {
    reject(ERR_CACHE_FAILED, @"CACHE module is not available in Lite version", nil);
}

- (void)allowUseCellularNetworkImpl:(BOOL)useCellular resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject {
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

#ifndef RCT_NEW_ARCH_ENABLED

- (NSArray<NSString *> *)supportedEvents {
    return @[EVENT_REGION_STATE_CHANGED, EVENT_REGION_PROGRESS];
}

- (void)startObserving {
    hasRegionEventsListeners = YES;
}

- (void)stopObserving {
    hasRegionEventsListeners = NO;
}

#endif

#ifdef RCT_NEW_ARCH_ENABLED

- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:(const facebook::react::ObjCTurboModule::InitParams &)params {
    return std::make_shared<facebook::react::NativeCacheModuleSpecJSI>(params);
}

- (void)initManager:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
    [self initImpl:resolve rejecter:reject];
}

- (void)allowUseCellularNetwork:(BOOL)useCellular resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
    [self allowUseCellularNetworkImpl:useCellular resolver:resolve rejecter:reject];
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

RCT_EXPORT_METHOD(initManager:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [self initImpl:resolve rejecter:reject];
}

RCT_EXPORT_METHOD(allowUseCellularNetwork:(BOOL)useCellular resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [self allowUseCellularNetworkImpl:useCellular resolver:resolve rejecter:reject];
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
