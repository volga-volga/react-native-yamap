//
//  Untitled.mm
//  Pods
//
//  Created by Tim on 11.02.26.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import <YandexMapsMobile/YRTDataProviderWithId.h>

#pragma mark - MapModel

@interface MapModel : NSObject<YRTDataProviderWithId>

@property (nonatomic, strong, readonly) NSString *modelId;
@property (nonatomic, strong, readonly) NSData *data;

- (instancetype)initWithId:(NSString *)modelId data:(NSData *)data;

@end

@implementation MapModel

- (instancetype)initWithId:(NSString *)modelId data:(NSData *)data {
    self = [super init];
    if (self) {
        _modelId = modelId;
        _data = data ?: [NSData data];
    }
    return self;
}

- (nonnull NSData *)load { 
    return self.data ?: [NSData data];
}

- (nonnull NSString *)providerId {
    return self.modelId;
}

@end

#pragma mark - ModelCacheManager

typedef void(^ModelCallback)(MapModel * _Nullable model);

@interface ModelCacheManager : NSObject

+ (instancetype)shared;
- (void)getModel:(NSString *)source completion:(ModelCallback)completion;
- (NSData * _Nullable)getModelSync:(NSString *)source error:(NSError **)error;

@end

@implementation ModelCacheManager {
    NSMutableDictionary<NSString *, MapModel *> *_modelCache;
}

+ (instancetype)shared {
    static ModelCacheManager *instance;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        instance = [[ModelCacheManager alloc] init];
    });
    return instance;
}

- (instancetype)init {
    self = [super init];
    if (self) {
        _modelCache = [NSMutableDictionary dictionary];
    }
    return self;
}

#pragma mark - Synchronous Loading

- (NSData * _Nullable)getModelSync:(NSString *)source error:(NSError **)error {
    
    // 🌐 URL загрузка
    if ([source hasPrefix:@"http://"] || [source hasPrefix:@"https://"]) {
        NSURL *url = [NSURL URLWithString:source];
        return [NSData dataWithContentsOfURL:url options:0 error:error];
    }
    
    // 🧾 Base64 data
    if ([source containsString:@"data:model"]) {
        NSRange commaRange = [source rangeOfString:@","];
        if (commaRange.location != NSNotFound) {
            NSString *base64String = [source substringFromIndex:commaRange.location + 1];
            return [[NSData alloc] initWithBase64EncodedString:base64String options:NSDataBase64DecodingIgnoreUnknownCharacters];
        }
    }
    
    // 📦 Bundle resources
    NSString *path = [[NSBundle mainBundle] pathForResource:source ofType:nil];
    if (path) {
        return [NSData dataWithContentsOfFile:path options:0 error:error];
    }
    
    if (error) {
        *error = [NSError errorWithDomain:@"ModelCacheManager"
                                     code:404
                                 userInfo:@{NSLocalizedDescriptionKey: [NSString stringWithFormat:@"Model not found: %@", source]}];
    }
    return nil;
}

#pragma mark - Asynchronous Loading

- (void)getModel:(NSString *)source completion:(ModelCallback)completion {
    
    MapModel *cached = _modelCache[source];
    if (cached) {
        completion(cached);
        return;
    }
    
    dispatch_async(dispatch_get_global_queue(QOS_CLASS_USER_INITIATED, 0), ^{
        NSError *error = nil;
        NSData *data = [self getModelSync:source error:&error];
        MapModel *model = nil;
        if (data) {
            model = [[MapModel alloc] initWithId:source data:data];
            @synchronized (self) {
                self->_modelCache[source] = model;
            }
        } else {
            NSLog(@"Error loading model: %@", error.localizedDescription);
        }
        dispatch_async(dispatch_get_main_queue(), ^{
            completion(model);
        });
    });
}

@end
