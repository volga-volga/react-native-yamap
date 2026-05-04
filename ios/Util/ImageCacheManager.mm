#import "ImageCacheManager.h"

@implementation ImageCacheManager

static NSCache<NSString*, UIImage*> *_imageCache = nil;

static ImageCacheManager *_instance = nil;

+ (id)instance
{
    if (!_instance) {
        _instance = [[ImageCacheManager alloc] init];
    }
    return _instance;
}

- (id)init
{
    if (!_instance) {
        _instance = [super init];
        _imageCache = [[NSCache alloc] init];
    }
    return _instance;
}

- (void)getWithSource:(NSString * _Nonnull)source completion:(void (^ _Nonnull __strong)(UIImage * _Nonnull __strong))completion {
    NSURLSessionDataTask *task = [[NSURLSession sharedSession] dataTaskWithURL:[NSURL URLWithString:source] completionHandler:^(NSData * _Nullable taskData, NSURLResponse * _Nullable response, NSError * _Nullable error) {

        UIImage *cachedImage = [_imageCache objectForKey:source];

        if (cachedImage) {
            dispatch_async(dispatch_get_main_queue(), ^{
                completion(cachedImage);
            });
            return;
        }

        if (error) {
            NSLog(@"Failed fetch data with error: %@", error);
        }

        if (!taskData) {
            NSLog(@"Failed to load image data from URL: %@", source);
            return;
        }

        UIImage *image = [UIImage imageWithData:taskData];
        if (!image) {
            NSLog(@"Failed to create image from loaded data: %@", source);
            return;
        }

        dispatch_async(dispatch_get_main_queue(), ^{
            completion(image);
        });

        [_imageCache setObject:image forKey:source];
    }];
    [task resume];
}

@end
