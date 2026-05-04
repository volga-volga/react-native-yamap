#import "PolylineView.h"

#import <YandexMapsMobile/YMKLineStyle.h>
#import "../Util/RCTConvert+Yamap.mm"

#ifdef RCT_NEW_ARCH_ENABLED

#import "../Util/NewArchUtils.h"

#import <react/renderer/components/RNYamapPlusSpec/ComponentDescriptors.h>
#import <react/renderer/components/RNYamapPlusSpec/EventEmitters.h>
#import <react/renderer/components/RNYamapPlusSpec/Props.h>
#import <react/renderer/components/RNYamapPlusSpec/RCTComponentViewHelpers.h>

#import "RCTFabricComponentsPlugins.h"

using namespace facebook::react;

@interface PolylineView () <RCTPolylineViewViewProtocol, YMKMapObjectTapListener>

@end

#endif

@implementation PolylineView {
    NSMutableArray<YMKPoint*> *_points;
    YMKPolylineMapObject* mapObject;
    YMKPolyline* polyline;
    UIColor* strokeColor;
    UIColor* outlineColor;
    float strokeWidth;
    float dashLength;
    float dashOffset;
    float gapLength;
    float outlineWidth;
    float zIndex;
    bool handled;

}

- (instancetype)init {
    if (self = [super init]) {

#ifdef RCT_NEW_ARCH_ENABLED

        static const auto defaultProps = std::make_shared<const PolylineViewProps>();
        _props = defaultProps;

#endif

        strokeColor = UIColor.blackColor;
        outlineColor = UIColor.blackColor;
        zIndex = 1;
        strokeWidth = 1;
        dashLength = 1;
        gapLength = 0;
        outlineWidth = 0;
        dashOffset = 0;
        _points = [[NSMutableArray alloc] init];
        handled = NO;
    }

    return self;
}

#ifdef RCT_NEW_ARCH_ENABLED

+ (ComponentDescriptorProvider)componentDescriptorProvider
{
    return concreteComponentDescriptorProvider<PolylineViewComponentDescriptor>();
}

- (void)updateProps:(const Props::Shared &)props oldProps:(const Props::Shared &)oldProps {
    const auto &oldViewProps = *std::static_pointer_cast<PolylineViewProps const>(_props);
    const auto &newViewProps = *std::static_pointer_cast<PolylineViewProps const>(props);

    if (![NewArchUtils polylinePointsEquals:oldViewProps.points points2:newViewProps.points]) {
        std::vector<PolylineViewPointsStruct> newPointsStruct = newViewProps.points;
        NSMutableArray<YMKPoint*> *newPointsArray = [[NSMutableArray alloc] init];

        for (int i = 0; i < newPointsStruct.size(); i++) {
            PolylineViewPointsStruct newPointStruct = newPointsStruct.at(i);
            [newPointsArray addObject:[YMKPoint pointWithLatitude:newPointStruct.lat longitude:newPointStruct.lon]];
        }

        _points = newPointsArray;
        polyline = [YMKPolyline polylineWithPoints:newPointsArray];
    }

    if (oldViewProps.strokeColor != newViewProps.strokeColor) {
        strokeColor = [RCTConvert UIColor:[NSNumber numberWithInt:newViewProps.strokeColor]];
    }

    if (oldViewProps.strokeWidth != newViewProps.strokeWidth) {
        strokeWidth = newViewProps.strokeWidth;
    }

    if (oldViewProps.outlineColor != newViewProps.outlineColor) {
        outlineColor = [RCTConvert UIColor:[NSNumber numberWithInt:newViewProps.outlineColor]];
    }

    if (oldViewProps.outlineWidth != newViewProps.outlineWidth) {
        outlineWidth = newViewProps.outlineWidth;
    }

    if (oldViewProps.dashLength != newViewProps.dashLength) {
        dashLength = newViewProps.dashLength;
    }

    if (oldViewProps.dashOffset != newViewProps.dashOffset) {
        dashOffset = newViewProps.dashOffset;
    }

    if (oldViewProps.zI != newViewProps.zI) {
        zIndex = newViewProps.zI;
    }

    if (oldViewProps.gapLength != newViewProps.gapLength) {
        gapLength = newViewProps.gapLength;
    }

    if (oldViewProps.handled != newViewProps.handled) {
        handled = newViewProps.handled;
    }

    [self updatePolyline];
}

- (void)prepareForRecycle
{
  [super prepareForRecycle];
  strokeColor = UIColor.blackColor;
  outlineColor = UIColor.blackColor;
  zIndex = 1;
  strokeWidth = 1;
  dashLength = 1;
  gapLength = 0;
  outlineWidth = 0;
  dashOffset = 0;
  handled = NO;
}

#else

- (void)setStrokeColor:(NSNumber *)color {
    strokeColor = [RCTConvert UIColor:color];
    [self updatePolyline];
}

- (void)setStrokeWidth:(NSNumber *)width {
    strokeWidth = [width floatValue];
    [self updatePolyline];
}

- (void)setOutlineWidth:(NSNumber *)width {
    outlineWidth = [width floatValue];
    [self updatePolyline];
}

- (void)setDashLength:(NSNumber *)length {
    dashLength = [length floatValue];
    [self updatePolyline];
}

- (void)setDashOffset:(NSNumber *)offset {
    dashOffset = [offset floatValue];
    [self updatePolyline];
}

- (void)setGapLength:(NSNumber *)length {
    gapLength = [length floatValue];
    [self updatePolyline];
}

- (void)setOutlineColor:(NSNumber *)color {
    outlineColor = [RCTConvert UIColor:color];
    [self updatePolyline];
}

- (void)setZI:(NSNumber *)_zIndex {
    zIndex = [_zIndex floatValue];
    [self updatePolyline];
}

- (void)setPoints:(NSMutableArray<YMKPoint*>*)points {
    _points = points;
    polyline = [YMKPolyline polylineWithPoints:points];
    [self updatePolyline];
}

- (void)setHandled:(BOOL)_handled {
    handled = _handled;
}

#endif

- (void)updatePolyline {
    if (mapObject != nil && [mapObject isValid]) {
        [mapObject setGeometry:polyline];
        [mapObject setZIndex:zIndex];
        [mapObject setStrokeColorWithColor:strokeColor];

        YMKLineStyle * style = [YMKLineStyle new];
        [style setStrokeWidth:strokeWidth];
        [style setDashLength:dashLength];
        [style setGapLength:gapLength];
        [style setDashOffset:dashOffset];
        [style setOutlineWidth:outlineWidth];
        [style setOutlineColor:outlineColor];
        [mapObject setStyle:style];
    }
}

- (BOOL)onMapObjectTapWithMapObject:(nonnull YMKMapObject*)mapObject point:(nonnull YMKPoint*)point {

#ifdef RCT_NEW_ARCH_ENABLED

    if (_eventEmitter) {
        std::dynamic_pointer_cast<const PolylineViewEventEmitter>(_eventEmitter)->onPress({});
    }

#else

    if (self.onPress)
        self.onPress(@{});

#endif

    return handled;
}

- (NSMutableArray<YMKPoint*>*)getPoints {
    return _points;
}

- (YMKPolyline*)getPolyline {
    return polyline;
}

- (YMKPolylineMapObject*)getMapObject {
    return mapObject;
}

- (void)setMapObject:(YMKPolylineMapObject*)_mapObject {
    mapObject = _mapObject;
    [mapObject addTapListenerWithTapListener:self];
    [self updatePolyline];
}

#ifdef RCT_NEW_ARCH_ENABLED

Class<RCTComponentViewProtocol> PolylineViewCls(void)
{
    return PolylineView.class;
}

#endif

@end
