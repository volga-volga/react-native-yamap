#if __has_include("RCTBridgeModule.h")
#import "RCTBridgeModule.h"
#else
#import <React/RCTBridgeModule.h>
#endif
#import "YamapView.h"

@interface yamap : NSObject <RCTBridgeModule>

@property YamapView *map;

-(void) initWithKey:(NSString *) apiKey;
+(NSString *)pinIcon;
+(NSString *)arrowIcon;
+(NSString *)markerIcon;
+(NSString *)selectedMarkerIcon;

+(void)setPinIcon:(NSString *) icon;
+(void)setArrowIcon:(NSString *) icon;
+(void)setMarkerIcon:(NSString *) icon;
+(void)setSelectedMarkerIcon:(NSString *) icon;

@end
