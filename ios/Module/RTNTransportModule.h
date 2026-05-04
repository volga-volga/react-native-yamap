#ifdef RCT_NEW_ARCH_ENABLED

#import <RNYamapPlusSpec/RNYamapPlusSpec.h>
@interface RTNTransportModule : NativeTransportModuleSpecBase <NativeTransportModuleSpec>

#else

#import <React/RCTBridgeModule.h>
@interface RTNTransportModule : NSObject <RCTBridgeModule>

#endif

@end
