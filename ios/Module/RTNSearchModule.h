#ifdef USE_YANDEX_MAPS_FULL

#import <YandexMapsMobile/YMKSearchManager.h>

#endif

#ifdef RCT_NEW_ARCH_ENABLED

#import <RNYamapPlusSpec/RNYamapPlusSpec.h>
@interface RTNSearchModule : NativeSearchModuleSpecBase <NativeSearchModuleSpec>

#else

#import <React/RCTBridgeModule.h>
@interface RTNSearchModule : NSObject <RCTBridgeModule>

#endif

#ifdef USE_YANDEX_MAPS_FULL

@property YMKSearchManager *searchManager;
@property YMKBoundingBox *defaultBoundingBox;
@property YMKSearchSession *searchSession;

#endif

@end
