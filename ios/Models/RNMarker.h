#import <Foundation/Foundation.h>


@interface RNMarker : NSObject
@property NSString *_id;
@property double lon;
@property double lat;
@property int zIndex;
@property NSString* uri;
- (id) initWithJson: (NSDictionary*) json;
@end

