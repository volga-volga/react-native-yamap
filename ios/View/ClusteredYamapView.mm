#import "ClusteredYamapView.h"

#import <React/UIView+React.h>

#ifdef RCT_NEW_ARCH_ENABLED

#import "../Util/NewArchUtils.h"

#import <react/renderer/components/RNYamapPlusSpec/ComponentDescriptors.h>
#import <react/renderer/components/RNYamapPlusSpec/EventEmitters.h>
#import <react/renderer/components/RNYamapPlusSpec/Props.h>
#import <react/renderer/components/RNYamapPlusSpec/RCTComponentViewHelpers.h>

#import "RCTFabricComponentsPlugins.h"

using namespace facebook::react;

@interface ClusteredYamapView ()

@end

#endif

@implementation ClusteredYamapView

- (instancetype)init {
    if (self = [super init]) {

#ifdef RCT_NEW_ARCH_ENABLED

        static const auto defaultProps = std::make_shared<const ClusteredYamapViewProps>();
        _props = defaultProps;

#endif

    }

    return self;
}

#ifdef RCT_NEW_ARCH_ENABLED

+ (ComponentDescriptorProvider)componentDescriptorProvider
{
    return concreteComponentDescriptorProvider<ClusteredYamapViewComponentDescriptor>();
}

- (void)updateProps:(const Props::Shared &)props oldProps:(const Props::Shared &)oldProps {
    const auto &oldViewProps = *std::static_pointer_cast<ClusteredYamapViewProps const>(_props);
    const auto &newViewProps = *std::static_pointer_cast<ClusteredYamapViewProps const>(props);

    if (oldViewProps.clusterColor != newViewProps.clusterColor) {
        [super setClusterColor:[NSNumber numberWithInt:newViewProps.clusterColor]];
    }

    if (oldViewProps.clusterSize.width != newViewProps.clusterSize.width || oldViewProps.clusterSize.height != newViewProps.clusterSize.height) {
        NSMutableDictionary *sizes = [[NSMutableDictionary alloc] init];
        [sizes setValue:[NSNumber numberWithDouble:newViewProps.clusterSize.width] forKey:@"width"];
        [sizes setValue:[NSNumber numberWithDouble:newViewProps.clusterSize.height] forKey:@"height"];
        [super setClusterSize:sizes];
    }

    if (oldViewProps.clusterTextColor != newViewProps.clusterTextColor) {
        [super setClusterTextColor:[NSNumber numberWithInt:newViewProps.clusterTextColor]];
    }

    if (oldViewProps.clusterTextSize != newViewProps.clusterTextSize) {
        [super setClusterTextSize:newViewProps.clusterTextSize];
    }

    if (oldViewProps.clusterTextYOffset != newViewProps.clusterTextYOffset) {
        [super setClusterTextYOffset:newViewProps.clusterTextYOffset];
    }

    if (oldViewProps.clusterTextXOffset != newViewProps.clusterTextXOffset) {
        [super setClusterTextXOffset:newViewProps.clusterTextXOffset];
    }

    NSMutableArray<YMKPoint *> *points = [[NSMutableArray alloc] init];
    for (int i = 0; i < newViewProps.clusteredMarkers.size(); i++) {
        ClusteredYamapViewClusteredMarkersStruct pointStruct = newViewProps.clusteredMarkers.at(i);
        [points addObject:[YMKPoint pointWithLatitude:pointStruct.lat longitude:pointStruct.lon]];
    }

    if (oldViewProps.clusterIcon != newViewProps.clusterIcon) {
        [super setClusterIcon:[NSString stringWithCString:newViewProps.clusterIcon.c_str() encoding:[NSString defaultCStringEncoding]] points:points];
    } else if (![NewArchUtils yamapClusteredMarkersEquals:oldViewProps.clusteredMarkers markers2:newViewProps.clusteredMarkers]) {
        [super setClusteredMarkers:points];
    }

    [super updateProps:props oldProps:oldProps];
}

- (void)prepareForRecycle
{
    [super prepareForRecycle];

    static const auto defaultProps = std::make_shared<const ClusteredYamapViewProps>();
    _props = defaultProps;
}

#endif

@end
