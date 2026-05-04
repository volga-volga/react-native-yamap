// eslint-disable-next-line @react-native/no-deep-imports
import codegenNativeComponent from 'react-native/Libraries/Utilities/codegenNativeComponent';
import type {
  BubblingEventHandler,
  Double,
  Float,
  Int32,
} from 'react-native/Libraries/Types/CodegenTypes';
import type {ViewProps} from 'react-native';

interface Point {
  lat: Double;
  lon: Double;
}

export interface PolylineNativeProps extends ViewProps {
  strokeColor?: Int32;
  outlineColor?: Int32;
  strokeWidth?: Float;
  outlineWidth?: Float;
  dashLength?: Float;
  dashOffset?: Float;
  gapLength?: Float;
  onPress?: BubblingEventHandler<undefined>;
  points: Point[];
  handled?: boolean;
  zI?: Float;
}

export default codegenNativeComponent<PolylineNativeProps>('PolylineView');
