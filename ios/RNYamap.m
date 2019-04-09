#import "RNYamap.h"
#import <YandexMapKit/YMKMapKitFactory.h>

@implementation yamap

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

RCT_EXPORT_METHOD(init: (NSString *) apiKey)
{
    // Пусто потому что: инициализировать надо в AppDelegate.m и дважды инициализировать нельзя, но метод нужен (вызывается в js)
}

RCT_EXPORT_MODULE()

@end
