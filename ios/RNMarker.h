#import <Foundation/Foundation.h>


@interface RNMarker : NSObject
@property NSString *_id;
@property double lon;
@property double lat;
@property Boolean isSelected;
- (id) initWithJson: (NSDictionary*) json;
@end

