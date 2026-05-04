#ifdef RCT_NEW_ARCH_ENABLED

#import <RNYamapPlusSpec/RNYamapPlusSpec.h>
@interface RTNYamapModule : NativeYamapModuleSpecBase <NativeYamapModuleSpec>

#else

#import <React/RCTBridgeModule.h>
@interface RTNYamapModule : NSObject <RCTBridgeModule>

#endif

@end
