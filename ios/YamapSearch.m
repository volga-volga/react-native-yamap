#import "React/RCTBridgeModule.h"
#import "React/RCTViewManager.h"
#import <UIKit/UIKit.h>
#import "RNYamap-Bridging-Header.h"

@interface RCT_EXTERN_MODULE(YamapSearch, NSObject)

RCT_EXTERN_METHOD(searchByAddress:(nonnull NSString*) searchQuery                figure:(id)figure options:(NSDictionary*) options resolver:(RCTPromiseResolveBlock) resolve rejecter:(RCTPromiseRejectBlock) reject)

RCT_EXTERN_METHOD(searchByPoint:(nonnull NSDictionary*) point zoom:(nonnull NSNumber*) zoom options:(NSDictionary*) options resolver:(RCTPromiseResolveBlock) resolve rejecter:(RCTPromiseRejectBlock) reject)

RCT_EXTERN_METHOD(addressToGeo:(nonnull NSString*) searchQuery resolver:(RCTPromiseResolveBlock) resolve rejecter:(RCTPromiseRejectBlock) reject)

RCT_EXTERN_METHOD(geoToAddress:(nonnull NSDictionary*) point resolver:(RCTPromiseResolveBlock) resolve rejecter:(RCTPromiseRejectBlock) reject)

RCT_EXTERN_METHOD(searchByURI:(nonnull NSString*) searchUri options:(NSDictionary*) options resolver:(RCTPromiseResolveBlock) resolve rejecter:(RCTPromiseRejectBlock) reject)

RCT_EXTERN_METHOD(searchByURI:(nonnull NSString*) searchUri options:(NSDictionary*) options resolver:(RCTPromiseResolveBlock) resolve rejecter:(RCTPromiseRejectBlock) reject)

@end

