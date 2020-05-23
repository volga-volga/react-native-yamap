import React from 'react';
import { requireNativeComponent } from 'react-native';
import { processColorProps } from '../utils';
import { Point } from '../interfaces';

const NativePolylineComponent = requireNativeComponent('YamapPolyline');

export interface PolylineProps {
  strokeColor?: string;
  outlineColor?: string;
  strokeWidth?: number;
  outlineWidth?: number;
  dashLength?: number;
  dashOffset?: number;
  gapLength?: number;
  zIndex?: number;
  onPress?: () => void;
  points: Point[];
}

export class Polyline extends React.Component<PolylineProps> {
  render() {
    const props = { ...this.props };
    processColorProps(props, 'fillColor' as keyof PolylineProps);
    processColorProps(props, 'strokeColor' as keyof PolylineProps);
    processColorProps(props, 'outlineColor' as keyof PolylineProps);
    return <NativePolylineComponent {...props} />;
  }
}
