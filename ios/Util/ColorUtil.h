#ifndef ColorUtil_h
#define ColorUtil_h

@interface ColorUtil : NSObject;

+ (UIColor *)colorFromHexString:(NSString *)hexString;
+ (NSString *)hexStringFromColor:(UIColor *)color;

@end

#endif
