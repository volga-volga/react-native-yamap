// eslint-disable-next-line @react-native/no-deep-imports
import codegenNativeComponent, {type NativeComponentType} from 'react-native/Libraries/Utilities/codegenNativeComponent';
import type {BubblingEventHandler, Double, Float} from 'react-native/Libraries/Types/CodegenTypes';
import {type ViewProps} from 'react-native';
import {type MarkerNativeCommands} from './commands/marker';

interface Point {
  lat: Double;
  lon: Double;
}

type Anchor = {
  x: Double;
  y: Double;
}

export interface MarkerNativeProps extends ViewProps {
  scale?: Float;
  rotated?: boolean;
  onPress?: BubblingEventHandler<undefined>;
  onDragStart?: BubblingEventHandler<Point>;
  onDrag?: BubblingEventHandler<Point>;
  onDragEnd?: BubblingEventHandler<Point>;
  point: Point;
  anchor?: Anchor;
  visible?: boolean;
  handled?: boolean;
  draggable?: boolean;
  source?: string;
  source3d?: string;
  zI?: Float;
  direction?: Float;
}

export type MarkerComponentType = NativeComponentType<MarkerNativeProps> & Readonly<MarkerNativeCommands>;

require('./commands/marker');

export default codegenNativeComponent<MarkerNativeProps>('MarkerView');
