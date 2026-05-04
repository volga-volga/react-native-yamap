#ifndef ImageCacheManager_h
#define ImageCacheManager_h

@interface ImageCacheManager : NSObject;

+ (id _Nonnull) instance;

- (void)getWithSource: (NSString*_Nonnull) source completion:(void (NS_SWIFT_SENDABLE ^_Nonnull)(UIImage * _Nonnull image))completion;

@end

#endif
