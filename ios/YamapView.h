#import <React/RCTViewManager.h>
#import "RNYMView.h"
#import "RNMarker.h"

@interface YamapView : RCTViewManager<YMKMapObjectTapListener, YMKUserLocationObjectListener>

@property RNYMView * _Nonnull map;
@property (nonatomic) NSMutableArray<RNMarker*> * _Nullable markers;
@property (nonatomic) YMKUserLocationView* _Nullable userLocationView;
@property (nonatomic) NSMutableDictionary<NSString*, YMKPlacemarkMapObject*> * _Nullable markersDict;
@property (nonatomic) UIImage* _Nullable userLocationImage;

@end
