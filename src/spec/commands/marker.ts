import type {Double, Float} from 'react-native/Libraries/Types/CodegenTypes';
// eslint-disable-next-line @react-native/no-deep-imports
import codegenNativeCommands from 'react-native/Libraries/Utilities/codegenNativeCommands';
import {type MarkerComponentType} from '../MarkerNativeComponent';
import {type Point} from "../../";

export interface MarkerNativeCommands {
  animatedMoveTo: (
    viewRef: React.ElementRef<MarkerComponentType>,
    args: Array<Readonly<{coords: Point, duration: Double}>>
  ) => void;
  animatedRotateTo: (
    viewRef: React.ElementRef<MarkerComponentType>,
    args: Array<Readonly<{angle: Float, duration: Double}>>
  ) => void;
}

export const Commands = codegenNativeCommands<MarkerNativeCommands>({
  supportedCommands: [
    'animatedMoveTo',
    'animatedRotateTo',
  ],
});
