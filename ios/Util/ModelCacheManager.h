//
//  ModelCacheManager.h
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

#pragma mark - MapModel

@interface MapModel : NSObject

@property (nonatomic, strong, readonly) NSString *modelId;
@property (nonatomic, strong, readonly) NSData *data;

- (instancetype)initWithId:(NSString *)modelId data:(nullable NSData *)data;

@end

#pragma mark - ModelCacheManager

typedef void(^ModelCallback)(MapModel * _Nullable model);

@interface ModelCacheManager : NSObject

+ (instancetype)shared;

/// Асинхронная загрузка модели
- (void)getModel:(NSString *)source completion:(ModelCallback)completion;

/// Синхронная загрузка модели
- (nullable NSData *)getModelSync:(NSString *)source error:(NSError **)error;

@end

NS_ASSUME_NONNULL_END
