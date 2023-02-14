import React from 'react';
import { requireNativeComponent } from 'react-native';
import { processColorProps } from '../utils';
import { Point } from '../interfaces';

export interface PolygonProps {
  fillColor?: string;
  strokeColor?: string;
  strokeWidth?: number;
  zIndex?: number;
  onPress?: () => void;
  points: Point[];
  innerRings?: (Point[])[];
  children?: undefined;
}

const NativePolygonComponent = requireNativeComponent<PolygonProps>('YamapPolygon');

export class Polygon extends React.Component<PolygonProps> {
  static defaultProps = {
    innerRings: []
  };

  render() {
    const props = { ...this.props };

    processColorProps(props, 'fillColor' as keyof PolygonProps);
    processColorProps(props, 'strokeColor' as keyof PolygonProps);

    return <NativePolygonComponent {...props} />;
  }
}
