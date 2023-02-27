#import "YamapSuggests.h"

@import YandexMapsMobile;

@implementation YamapSuggests {
    YMKSearchManager* searchManager;
    YMKSearchSuggestSession* suggestClient;
    YMKBoundingBox* defaultBoundingBox;
    YMKSuggestOptions* suggestOptions;
}

- (id)init {
    self = [super init];

    YMKPoint* southWestPoint = [YMKPoint pointWithLatitude:-90.0 longitude:-180.0];
    YMKPoint* northEastPoint = [YMKPoint pointWithLatitude:90.0 longitude:180.0];
    defaultBoundingBox = [YMKBoundingBox boundingBoxWithSouthWest:southWestPoint northEast:northEastPoint];
    suggestOptions = [YMKSuggestOptions suggestOptionsWithSuggestTypes: YMKSuggestTypeGeo userPosition:nil suggestWords:true];

    return self;
}

+ (BOOL)requiresMainQueueSetup {
    return YES;
}

// TODO: Этот метод можно вынести в отдельный файл утилей, но пока в этом нет необходимости.
void runOnMainQueueWithoutDeadlocking(void (^block)(void)) {
    if ([NSThread isMainThread]) {
        block();
    } else {
        dispatch_sync(dispatch_get_main_queue(), block);
    }
}

NSString* ERR_NO_REQUEST_ARG = @"YANDEX_SUGGEST_ERR_NO_REQUEST_ARG";
NSString* ERR_SUGGEST_FAILED = @"YANDEX_SUGGEST_ERR_SUGGEST_FAILED";
NSString* YandexSuggestErrorDomain = @"YandexSuggestErrorDomain";

- (YMKSearchSuggestSession*_Nonnull)getSuggestClient {
    if (suggestClient) {
        return suggestClient;
    }

    if (!searchManager) {
        runOnMainQueueWithoutDeadlocking(^{
            self->searchManager = [[YMKSearch sharedInstance] createSearchManagerWithSearchManagerType:YMKSearchSearchManagerTypeOnline];
        });
    }

    runOnMainQueueWithoutDeadlocking(^{
        self->suggestClient = [self->searchManager createSuggestSession];
    });

    return suggestClient;
}

-(void)suggestHandler: (nonnull NSString*) searchQuery options:(YMKSuggestOptions*) options boundingBox:(nonnull YMKBoundingBox*) boundingBox resolver:(RCTPromiseResolveBlock) resolve rejecter:(RCTPromiseRejectBlock) reject {
	@try {
		YMKSearchSuggestSession* session = [self getSuggestClient];

		dispatch_async(dispatch_get_main_queue(), ^{
			[session suggestWithText:searchQuery
												window:boundingBox
								suggestOptions:options
							 responseHandler:^(NSArray<YMKSuggestItem *> * _Nullable suggestList, NSError * _Nullable error) {
				if (error) {
					reject(ERR_SUGGEST_FAILED, [NSString stringWithFormat:@"search request: %@", searchQuery], error);
					return;
				}

				NSMutableArray *suggestsToPass = [NSMutableArray new];

				for (YMKSuggestItem* suggest in suggestList) {
					NSMutableDictionary *suggestToPass = [NSMutableDictionary new];

					[suggestToPass setValue:[[suggest title] text] forKey:@"title"];
					[suggestToPass setValue:[[suggest subtitle] text] forKey:@"subtitle"];
					[suggestToPass setValue:[suggest uri] forKey:@"uri"];

					[suggestsToPass addObject:suggestToPass];
				}

				resolve(suggestsToPass);
			}];
		});
	}
	@catch ( NSException *error ) {
		reject(ERR_NO_REQUEST_ARG, [NSString stringWithFormat:@"search request: %@", searchQuery], nil);
	}
}

-(NSError * _Nonnull)makeErrorWithText: (nonnull NSString*) descriptionText {
	NSDictionary *errorDictionary = @{ NSLocalizedDescriptionKey : descriptionText };
  NSError *errorObject = [[NSError alloc] initWithDomain:YandexSuggestErrorDomain code:0 userInfo:errorDictionary];
	return errorObject;
}

-(YMKPoint *)mapPoint: (nonnull NSDictionary*) fromDictionary withKey:(nonnull NSString*) pointKey error:(NSError **) outError {
	NSDictionary *pointDictionary = fromDictionary[pointKey];
	if(![pointDictionary isKindOfClass: [NSDictionary class]]){
		*outError = [self makeErrorWithText:[NSString stringWithFormat:@"search request: %@ is not an Object", pointKey]];
		return nil;
	}
	if(pointDictionary[@"lat"] == nil || pointDictionary[@"lon"] == nil){
		*outError = [self makeErrorWithText:[NSString stringWithFormat:@"search request: lon and lat cannot be empty in %@", pointKey]];
		return nil;
	}

	NSNumber *lat =  pointDictionary[@"lat"];
	NSNumber *lon =  pointDictionary[@"lon"];

	if(![lat isKindOfClass: [NSNumber class]] || ![lon isKindOfClass:[NSNumber class]]){
		*outError = [self makeErrorWithText:[NSString stringWithFormat:@"search request: lat or lon is not a Number in %@", pointKey]];
		return nil;
	}

	YMKPoint	*point = [YMKPoint pointWithLatitude:[lat doubleValue] longitude:[lon doubleValue]];
	return point;
}

RCT_EXPORT_METHOD(suggest:(nonnull NSString*) searchQuery resolver:(RCTPromiseResolveBlock) resolve rejecter:(RCTPromiseRejectBlock) reject {
	[self suggestHandler:searchQuery options:self->suggestOptions boundingBox:self->defaultBoundingBox resolver:resolve rejecter:reject];
})

RCT_EXPORT_METHOD(suggestWithOptions:(nonnull NSString*) searchQuery options:(NSDictionary *) options resolver:(RCTPromiseResolveBlock) resolve rejecter:(RCTPromiseRejectBlock) reject {
	NSArray *suggestTypes = options[@"suggestTypes"];
	NSDictionary *boxDictionary = options[@"boundingBox"];
	YMKSuggestType suggestType = YMKSuggestTypeGeo;
	YMKBoundingBox *boundingBox = self->defaultBoundingBox;

	YMKSuggestOptions *opt = [[YMKSuggestOptions alloc] init];

	if(options[@"suggestWords"] != nil){
		NSNumber *suggestWords = options[@"suggestWords"];
		if(![suggestWords isKindOfClass:[NSNumber class]]){
			reject(ERR_NO_REQUEST_ARG, [NSString stringWithFormat:@"search request: suggestWords must be a Boolean"], nil);
			return;
		}
		[opt setSuggestWords:suggestWords.boolValue];
	}

	if(suggestTypes != nil){
		if(![suggestTypes isKindOfClass: [NSArray class]]){
			reject(ERR_NO_REQUEST_ARG, [NSString stringWithFormat:@"search request: suggestTypes is not an Array"], nil);
			return;
		}

		suggestType = YMKSuggestTypeUnspecified;

		for(int i = 0; i < [suggestTypes count]; i++){
			NSNumber *value = suggestTypes[i];
			if(![value isKindOfClass: [NSNumber class]]){
				reject(ERR_NO_REQUEST_ARG, [NSString stringWithFormat:@"search request: one or more suggestTypes is not a Number"], nil);
				return;
			}
			suggestType = suggestType | [value unsignedLongValue];
		}
	}

	[opt setSuggestTypes:suggestType];

	if(options[@"userPosition"] != nil){
		NSError *pointError;
		YMKPoint *userPoint = [self mapPoint:options withKey:@"userPosition" error:&pointError];
		if(!userPoint){
			reject(ERR_NO_REQUEST_ARG, [pointError localizedDescription], nil);
			return;
		}

		[opt setUserPosition:userPoint];
	}

	if(boxDictionary != nil){
		if(![boxDictionary isKindOfClass: [NSDictionary class]]){
			reject(ERR_NO_REQUEST_ARG, [NSString stringWithFormat:@"search request: boundingBox is not an Object"], nil);
			return;
		}
		if(boxDictionary[@"southWest"] == nil || boxDictionary[@"northEast"] == nil){
			reject(ERR_NO_REQUEST_ARG, [NSString stringWithFormat:@"search request: southWest and northEast cannot be empty"], nil);
			return;
		}

    NSError *boxError;
		YMKPoint *southWest = [self mapPoint:boxDictionary withKey:@"southWest" error:&boxError];
		if(!southWest){
			reject(ERR_NO_REQUEST_ARG, [boxError localizedDescription], nil);
			return;
		}
		YMKPoint *northEast = [self mapPoint:boxDictionary withKey:@"northEast" error:&boxError];
		if(!northEast){
			reject(ERR_NO_REQUEST_ARG, [boxError localizedDescription], nil);
			return;
		}

		boundingBox = [YMKBoundingBox boundingBoxWithSouthWest:southWest northEast:northEast];
	}

	[self suggestHandler:searchQuery options:opt boundingBox:boundingBox resolver:resolve rejecter:reject];
})

RCT_EXPORT_METHOD(reset: (RCTPromiseResolveBlock) resolve rejecter:(RCTPromiseRejectBlock) reject {
    @try {
        if (suggestClient) {
          dispatch_async(dispatch_get_main_queue(),^{
						[self->suggestClient reset];
					});
        }

        resolve(@[]);
    }
    @catch(NSException *error) {
        reject(@"ERROR", @"Error during reset suggestions", nil);
    }
})

RCT_EXPORT_MODULE();

@end
