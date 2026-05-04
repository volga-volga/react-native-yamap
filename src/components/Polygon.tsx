import React, {type FC, useMemo} from 'react';
import {type OmitEx, processColorsToNative} from '../utils';
import PolygonNativeComponent, {type PolygonNativeProps} from '../spec/PolygonNativeComponent';

export type PolygonProps = OmitEx<PolygonNativeProps, 'fillColor' | 'strokeColor' | 'zI'> & {
  fillColor?: string;
  strokeColor?: string;
  zIndex?: number;
}

export const Polygon: FC<PolygonProps> = ({zIndex, ...props}) => {
  const nativeProps = useMemo(() =>
    processColorsToNative(props, ['fillColor', 'strokeColor']),
    [props]
  );

  return <PolygonNativeComponent
    zI={zIndex}
    {...nativeProps}
  />;
};
