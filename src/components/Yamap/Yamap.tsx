import React, {forwardRef, useMemo, useRef} from 'react';
import {getImageUri, processColorsToNative} from '../../utils';
import type {YamapProps, YamapRef} from './types';
import {
  onCameraPositionReceived,
  onScreenToWorldPointsReceived,
  onVisibleRegionReceived,
  onWorldToScreenPointsReceived,
} from './events';
import {useYamap} from '../../hooks/useYamap';
import YamapNativeComponent, {type YamapNativeRef} from '../../spec/YamapNativeComponent';
import {Commands} from '../../spec/commands/yamap';

export const Yamap = forwardRef<YamapRef, YamapProps>((props, ref) => {
  const nativeRef = useRef<YamapNativeRef | null>(null);

  useYamap(nativeRef, ref, Commands);

  const nativeProps = useMemo(() =>
    processColorsToNative({
      ...props,
      onCameraPositionReceived,
      onVisibleRegionReceived,
      onWorldToScreenPointsReceived,
      onScreenToWorldPointsReceived,
      userLocationIcon: getImageUri(props.userLocationIcon),
    }, ['userLocationAccuracyFillColor', 'userLocationAccuracyStrokeColor']),
    [props]
  );

  return (
    <YamapNativeComponent
      {...nativeProps}
      ref={nativeRef}
    />
  );
});
