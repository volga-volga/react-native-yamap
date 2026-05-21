#ifdef RCT_NEW_ARCH_ENABLED

#import <RNYamapPlusSpec/RNYamapPlusSpec.h>
@interface RTNCacheModule : NativeCacheModuleSpecBase <NativeCacheModuleSpec>

#else

#import <React/RCTBridgeModule.h>
@interface RTNCacheModule : NSObject <RCTBridgeModule>

#endif

@end
