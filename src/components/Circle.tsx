import React, {type FC, useMemo} from 'react';
import {type OmitEx, processColorsToNative} from '../utils';
import CircleNativeComponent, {type CircleNativeProps} from '../spec/CircleNativeComponent';

export type CircleProps = OmitEx<CircleNativeProps, 'fillColor' | 'strokeColor' | 'zI'> & {
  fillColor?: string;
  strokeColor?: string;
  zIndex?: number;
}

export const Circle: FC<CircleProps> = ({zIndex, ...props}) => {
  const nativeProps = useMemo(() =>
    processColorsToNative(props, ['fillColor', 'strokeColor']),
    [props]
  );

  return <CircleNativeComponent
    zI={zIndex}
    {...nativeProps}
  />;
};
