#ifdef USE_YANDEX_MAPS_FULL

#import <YandexMapsMobile/YMKSearchManager.h>
#import <YandexMapsMobile/YMKSuggestOptions.h>

#endif

#ifdef RCT_NEW_ARCH_ENABLED

#import <RNYamapPlusSpec/RNYamapPlusSpec.h>
@interface RTNSuggestsModule : NativeSuggestsModuleSpecBase <NativeSuggestsModuleSpec>

#else

#import <React/RCTBridgeModule.h>
@interface RTNSuggestsModule : NSObject <RCTBridgeModule>

#endif

#ifdef USE_YANDEX_MAPS_FULL

@property YMKSearchManager *searchManager;
@property YMKSearchSuggestSession *suggestClient;
@property YMKBoundingBox *defaultBoundingBox;
@property YMKSuggestOptions *defaultSuggestOptions;

#endif

@end
