// eslint-disable-next-line @react-native/no-deep-imports
import codegenNativeCommands from 'react-native/Libraries/Utilities/codegenNativeCommands';
// eslint-disable-next-line @react-native/no-deep-imports
import {type Double} from 'react-native/Libraries/Types/CodegenTypes';
import {type YamapComponentType} from '../YamapNativeComponent';
import {type ClusteredYamapComponentType} from '../ClusteredYamapNativeComponent';
import {Animation, type ScreenPoint} from '../../interfaces';
import type {Point} from '../../';

export interface YamapNativeCommands {
  setCenter: (
    viewRef: React.ElementRef<YamapComponentType | ClusteredYamapComponentType>,
    args: Array<Readonly<{
      duration: Double;
      center: Point,
      zoom: Double;
      azimuth: Double;
      tilt: Double;
      animation: Animation
    }>>,
  ) => void;
  fitAllMarkers: (
    viewRef: React.ElementRef<YamapComponentType | ClusteredYamapComponentType>,
    args: Array<Readonly<{
      duration?: number,
      animation?: Animation,
    }>>,
  ) => void;
  fitMarkers: (
    viewRef: React.ElementRef<YamapComponentType | ClusteredYamapComponentType>,
    args: Array<Readonly<{
      points: Point[],
      duration?: number,
      animation?: Animation,
    }>>,
  ) => void;
  setZoom: (
    viewRef: React.ElementRef<YamapComponentType | ClusteredYamapComponentType>,
    args: Array<Readonly<{
      zoom: Double
      duration: Double,
      animation: Animation,
    }>>,
  ) => void;
  getCameraPosition: (
    viewRef: React.ElementRef<YamapComponentType | ClusteredYamapComponentType>,
    args: Array<Readonly<{
      id: string,
    }>>,
  ) => void;
  getVisibleRegion: (
    viewRef: React.ElementRef<YamapComponentType | ClusteredYamapComponentType>,
    args: Array<Readonly<{
      id: string,
    }>>,
  ) => void;
  setTrafficVisible: (
    viewRef: React.ElementRef<YamapComponentType | ClusteredYamapComponentType>,
    args: Array<Readonly<{
      isVisible: boolean,
    }>>,
  ) => void;
  getScreenPoints: (
    viewRef: React.ElementRef<YamapComponentType | ClusteredYamapComponentType>,
    args: Array<Readonly<{
      points: Point[]
      id: string
    }>>,
  ) => void;
  getWorldPoints: (
    viewRef: React.ElementRef<YamapComponentType | ClusteredYamapComponentType>,
    args: Array<Readonly<{
      points: ScreenPoint[]
      id: string
    }>>,
  ) => void;
  findParkRoute: (
    viewRef: React.ElementRef<YamapComponentType | ClusteredYamapComponentType>,
    args: Array<Readonly<{
      start: Point
      end: Point
      zones: Point[][]
    }>>,
  ) => void;

  getUserPosition: (
    viewRef: React.ElementRef<YamapComponentType | ClusteredYamapComponentType>,
    args: Array<Readonly<{
      id: string,
    }>>,
  ) => void;
}

export const Commands = codegenNativeCommands<YamapNativeCommands>({
  supportedCommands: [
    'setCenter',
    'fitAllMarkers',
    'fitMarkers',
    'setZoom',
    'getCameraPosition',
    'getVisibleRegion',
    'setTrafficVisible',
    'getScreenPoints',
    'getWorldPoints',
    'getUserPosition',
    'findParkRoute',
  ],
});
