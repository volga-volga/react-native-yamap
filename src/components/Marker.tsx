import React, {forwardRef, useImperativeHandle, useMemo, useRef} from 'react';
import {type ImageSourcePropType} from 'react-native';
import {getImageUri, type OmitEx} from '../utils';
import MarkerNativeComponent, {type MarkerNativeProps} from '../spec/MarkerNativeComponent';
import {Commands} from '../spec/commands/marker';
import type {Point} from "../";

export type MarkerProps = OmitEx<MarkerNativeProps, 'source' | 'zI'> &  {
  source?: ImageSourcePropType;
  source3d?: ImageSourcePropType;
  zIndex?: number;
}

export interface MarkerRef {
  animatedMoveTo: (coords: Point, duration: number) => void;
  animatedRotateTo: (angle: number, duration: number) => void;
}

export const Marker = forwardRef<MarkerRef, MarkerProps>(({source, source3d, zIndex, visible = true, ...props}, ref) => {
  const nativeRef = useRef(null);

  const imageUri = useMemo(() => getImageUri(source), [source]);

  const image3dUri = useMemo(() => getImageUri(source3d), [source]);

  useImperativeHandle<MarkerRef, any>(ref, () => ({
    animatedMoveTo: (coords: Point, duration: number) =>
      Commands.animatedMoveTo(nativeRef.current!, [{coords, duration}]),
    animatedRotateTo: (angle: number, duration: number) =>
      Commands.animatedRotateTo(nativeRef.current!, [{angle, duration}]),
  }), []);

  return (
    <MarkerNativeComponent
      zI={zIndex}
      {...props}
      ref={nativeRef}
      source={imageUri}
      source3d={image3dUri}
      visible={visible}
      pointerEvents="none"
    />
  );
});
