// eslint-disable-next-line @react-native/no-deep-imports
import codegenNativeComponent from 'react-native/Libraries/Utilities/codegenNativeComponent';
import type {
  BubblingEventHandler,
  Double,
  Float,
  Int32,
} from 'react-native/Libraries/Types/CodegenTypes';
import {type ViewProps} from 'react-native';

interface Point {
  lat: Double;
  lon: Double;
}

export interface PolygonNativeProps extends ViewProps {
  fillColor?: Int32;
  strokeColor?: Int32;
  strokeWidth?: Float;
  onPress?: BubblingEventHandler<undefined>;
  points: Point[];
  innerRings?: (Point[])[];
  handled?: boolean;
  zI?: Float;
}

export default codegenNativeComponent<PolygonNativeProps>('PolygonView');
