#import <React/RCTViewManager.h>
#import "RNYMView.h"
#import "RNMarker.h"

@interface YamapView : RCTViewManager<YMKMapObjectTapListener, YMKUserLocationObjectListener>

@property RNYMView * _Nonnull map;
@property (nonatomic) NSMutableArray<RNMarker*> * _Nullable markers;
@property (nonatomic) NSMutableDictionary<NSString*, YMKPlacemarkMapObject*> * _Nullable markersDict;

@end
