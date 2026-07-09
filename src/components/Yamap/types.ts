import {type ImageSourcePropType} from 'react-native';
import type {
  Animation,
  BoundingBox,
  CameraPositionCallback,
  ScreenPoint,
  ScreenPointsCallback, UserPositionCallback,
  VisibleRegionCallback,
  WorldPointsCallback,
} from '../../interfaces';
import {type OmitEx} from '../../utils';
import {type YamapNativeProps} from '../../spec/YamapNativeComponent';
import type {Point} from "../";

export type YamapProps = OmitEx<YamapNativeProps,
  'userLocationAccuracyFillColor' |
  'userLocationAccuracyStrokeColor' |
  'userLocationIcon' |
  'onCameraPositionReceived' |
  'onVisibleRegionReceived' |
  'onWorldToScreenPointsReceived' |
  'onScreenToWorldPointsReceived'
> & {
  userLocationAccuracyFillColor?: string;
  userLocationAccuracyStrokeColor?: string;
  userLocationIcon?: ImageSourcePropType;
}

export type YamapRef = {
  setCenter: (
    center: Point,
    zoom?: number,
    azimuth?: number,
    tilt?: number,
    duration?: number,
    animation?: Animation
  ) => void;
  findParkRoute: (start: Point, end: Point, zones: Point[][]) => void,
  fitAllMarkers: (duration?: number, animation?: Animation) => void;
  fitMarkers: (points: Point[], duration?: number, animation?: Animation) => void;
  setZoom: (zoom: number, duration?: number, animation?: Animation) => void;
  getUserPosition: (callback: CameraPositionCallback) => void;
  getCameraPosition: (callback: CameraPositionCallback) => void;
  getVisibleRegion: (callback: VisibleRegionCallback) => void;
  setTrafficVisible: (isVisible: boolean) => void;
  getScreenPoints: (points: Point[], callback: ScreenPointsCallback) => void;
  getWorldPoints: (points: ScreenPoint[], callback: WorldPointsCallback) => void;
  setLatLngBounds: (bounds: BoundingBox | null) => void;
};
