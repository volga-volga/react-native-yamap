#import "RCTConvert+UIView.h"
#import <React/RCTViewManager.h>
#import <YamapPolylineView.h>

@implementation RCTConvert (UIView)

+ (UIView *)UIView:(id)json {
    if ([json isKindOfClass:[NSDictionary class]]) {
        NSDictionary *dict = (NSDictionary *)json;
        NSString *viewType = dict[@"type"];
        if ([viewType isEqualToString:@"YamapPolylineView"]) {
            return [[YamapPolylineView alloc] init];
        }
        // Handle other view types if needed
    }
    return nil;
}

@end
