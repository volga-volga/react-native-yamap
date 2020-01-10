#import "RNMarker.h"


@implementation RNMarker {
}
- (RNMarker*)initWithJson:(NSDictionary *)json {
    self.lon = [[json valueForKey:@"lon"] doubleValue];
    self.lat = [[json valueForKey:@"lat"] doubleValue];
    self._id = [json valueForKey:@"id"];
    self.uri = [json valueForKey:@"source"];
    self.zIndex = [[json valueForKey:@"zIndex"] intValue];
    return self;
}

@end
