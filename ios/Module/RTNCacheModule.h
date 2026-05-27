#ifdef RCT_NEW_ARCH_ENABLED

#import <RNYamapPlusSpec/RNYamapPlusSpec.h>
@interface RTNCacheModule : NativeCacheModuleSpecBase <NativeCacheModuleSpec>

#else

#import <React/RCTEventEmitter.h>
@interface RTNCacheModule : RCTEventEmitter <RCTBridgeModule>

#endif

@end
