import React, {forwardRef, useMemo, useRef} from 'react';
import {PixelRatio} from "react-native";
import {getImageUri, processColorsToNative} from '../../utils';
import {
  onCameraPositionReceived,
  onScreenToWorldPointsReceived,
  onVisibleRegionReceived,
  onWorldToScreenPointsReceived,
} from '../Yamap/events';
import {type ClusteredYamapProps} from './types';
import ClusteredYamapNativeComponent, {type ClusteredYamapNativeRef} from '../../spec/ClusteredYamapNativeComponent';
import {useYamap} from '../../hooks/useYamap';
import {type YamapRef} from '../Yamap/types';
import {Commands} from '../../spec/commands/yamap';

export const ClusteredYamap = forwardRef<YamapRef, ClusteredYamapProps>(({
    clusterColor = 'red',
    ...props
  }, ref) => {

  const nativeRef = useRef<ClusteredYamapNativeRef | null>(null);

  useYamap(nativeRef, ref, Commands);

  const nativeProps = useMemo(() =>
    processColorsToNative({
      ...props,
      onCameraPositionReceived,
      onVisibleRegionReceived,
      onWorldToScreenPointsReceived,
      onScreenToWorldPointsReceived,
      userLocationIcon: getImageUri(props.userLocationIcon),
      clusterIcon: getImageUri(props.clusterIcon),
      clusterColor,
      clusteredMarkers: props.clusteredMarkers.map(mark => mark.point),
      clusterSize: props.clusterSize ? {
        width:  props.clusterSize.width
          ? PixelRatio.getPixelSizeForLayoutSize(props.clusterSize.width)
          : props.clusterSize.width,
        height: props.clusterSize.height
          ? PixelRatio.getPixelSizeForLayoutSize(props.clusterSize.height)
          : props.clusterSize.height,
      } : props.clusterSize,
      clusterTextSize: props.clusterTextSize
        ? PixelRatio.getPixelSizeForLayoutSize(props.clusterTextSize)
        : props.clusterTextSize,
      clusterTextYOffset: props.clusterTextYOffset
        ? PixelRatio.getPixelSizeForLayoutSize(props.clusterTextYOffset)
        : props.clusterTextYOffset,
      clusterTextXOffset: props.clusterTextXOffset
        ? PixelRatio.getPixelSizeForLayoutSize(props.clusterTextXOffset)
        : props.clusterTextXOffset,
      children: props.clusteredMarkers.map(props.renderMarker),
    }, [
      'clusterColor',
      'userLocationAccuracyFillColor',
      'userLocationAccuracyStrokeColor',
      'clusterTextColor'
    ]),
    [clusterColor, props]
  );

  return (
    <ClusteredYamapNativeComponent
      {...nativeProps}
      ref={nativeRef}
    />
  );
});
