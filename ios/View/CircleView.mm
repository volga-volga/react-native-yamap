#import "CircleView.h"

#import "../Util/RCTConvert+Yamap.mm"

#ifdef RCT_NEW_ARCH_ENABLED

#import "../Util/NewArchUtils.h"

#import <react/renderer/components/RNYamapPlusSpec/ComponentDescriptors.h>
#import <react/renderer/components/RNYamapPlusSpec/EventEmitters.h>
#import <react/renderer/components/RNYamapPlusSpec/Props.h>
#import <react/renderer/components/RNYamapPlusSpec/RCTComponentViewHelpers.h>

#import "RCTFabricComponentsPlugins.h"

using namespace facebook::react;

@interface CircleView () <RCTCircleViewViewProtocol, YMKMapObjectTapListener>

@end

#endif

@implementation CircleView {
    YMKPoint* center;
    float radius;
    YMKCircleMapObject* mapObject;
    YMKCircle* circle;
    UIColor* fillColor;
    UIColor* strokeColor;
    float strokeWidth;
    float zIndex;
    BOOL handled;
}

- (instancetype)init {
    if (self = [super init]) {
        
#ifdef RCT_NEW_ARCH_ENABLED
        
        static const auto defaultProps = std::make_shared<const PolygonViewProps>();
        _props = defaultProps;
        
#endif
        
        fillColor = UIColor.blackColor;
        strokeColor = UIColor.blackColor;
        zIndex = 1;
        strokeWidth = 0;
        handled = NO;
        center = [YMKPoint pointWithLatitude:0 longitude:0];
        radius = 0;
        circle = [YMKCircle circleWithCenter:center radius:radius];
    }

    return self;
}

#ifdef RCT_NEW_ARCH_ENABLED

+ (ComponentDescriptorProvider)componentDescriptorProvider
{
    return concreteComponentDescriptorProvider<CircleViewComponentDescriptor>();
}

- (void)updateProps:(const Props::Shared &)props oldProps:(const Props::Shared &)oldProps {
    const auto &oldViewProps = *std::static_pointer_cast<CircleViewProps const>(_props);
    const auto &newViewProps = *std::static_pointer_cast<CircleViewProps const>(props);

    if (oldViewProps.center.lat != newViewProps.center.lat || oldViewProps.center.lon != newViewProps.center.lon) {
        center = [YMKPoint pointWithLatitude:newViewProps.center.lat longitude:newViewProps.center.lon];
    }

    if (oldViewProps.radius != newViewProps.radius) {
        radius = newViewProps.radius;
    }
    
    if (oldViewProps.fillColor != newViewProps.fillColor) {
        fillColor = [RCTConvert UIColor:[NSNumber numberWithInt:newViewProps.fillColor]];
    }

    if (oldViewProps.strokeColor != newViewProps.strokeColor) {
        strokeColor = [RCTConvert UIColor:[NSNumber numberWithInt:newViewProps.strokeColor]];
    }

    if (oldViewProps.strokeWidth != newViewProps.strokeWidth) {
        strokeWidth = newViewProps.strokeWidth;
    }

    if (oldViewProps.zI != newViewProps.zI) {
        zIndex = newViewProps.zI;
    }

    if (oldViewProps.handled != newViewProps.handled) {
        handled = newViewProps.handled;
    }

    [self updateGeometry];
    [self updateCircle];
}

- (void)prepareForRecycle
{
  [super prepareForRecycle];
    fillColor = UIColor.blackColor;
    strokeColor = UIColor.blackColor;
    zIndex = 1;
    strokeWidth = 0;
    handled = NO;
    radius = 0;
}

#else

- (void)setFillColor:(NSNumber*)color {
    fillColor = [RCTConvert UIColor:color];
    [self updateCircle];
}

- (void)setStrokeColor:(NSNumber*)color {
    strokeColor = [RCTConvert UIColor:color];
    [self updateCircle];
}

- (void)setStrokeWidth:(NSNumber*)width {
    strokeWidth = [width floatValue];
    [self updateCircle];
}

- (void)setZI:(NSNumber*)zI {
    zIndex = [zI floatValue];
    [self updateCircle];
}

- (void)setHandled:(BOOL)_handled {
    handled = _handled;
}

- (void)setRadius:(float)_radius {
    radius = _radius;
    [self updateGeometry];
    [self updateCircle];
}

- (void)setCircleCenter:(YMKPoint*)point {
    center = point;
    [self updateGeometry];
    [self updateCircle];
}

#endif

- (void)updateGeometry {
    if (center) {
        circle = [YMKCircle circleWithCenter:center radius:radius];
    }
}

- (void)updateCircle {
    if (mapObject != nil && [mapObject isValid]) {
        [mapObject setGeometry:circle];
        [mapObject setZIndex:zIndex];
        [mapObject setFillColor:fillColor];
        [mapObject setStrokeColor:strokeColor];
        [mapObject setStrokeWidth:strokeWidth];
    }
}

- (void)setMapObject:(YMKCircleMapObject*)_mapObject {
    mapObject = _mapObject;
    [mapObject addTapListenerWithTapListener:self];
    [self updateCircle];
}

- (YMKCircle*)getCircle {
    return circle;
}

- (BOOL)onMapObjectTapWithMapObject:(nonnull YMKMapObject*)mapObject point:(nonnull YMKPoint*)point {
#ifdef RCT_NEW_ARCH_ENABLED

    if (_eventEmitter) {
        std::dynamic_pointer_cast<const CircleViewEventEmitter>(_eventEmitter)->onPress({});
    }

#else

    if (self.onPress)
        self.onPress(@{});

#endif

    return handled;
}

- (YMKCircleMapObject*)getMapObject {
    return mapObject;
}

@end
