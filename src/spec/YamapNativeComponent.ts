// eslint-disable-next-line @react-native/no-deep-imports
import codegenNativeComponent, {type NativeComponentType} from 'react-native/Libraries/Utilities/codegenNativeComponent';
import {type YamapNativeCommands} from './commands/yamap';
import type {
  BubblingEventHandler,
  DirectEventHandler,
  Double,
  Float,
  Int32,
  WithDefault,
} from 'react-native/Libraries/Types/CodegenTypes';
import type {NativeMethods, ViewProps} from 'react-native';
import {Component} from 'react';

export interface InitialRegion {
  lat: Double;
  lon: Double;
  zoom?: Double;
  azimuth?: Double;
  tilt?: Double;
}

export interface YandexLogoPosition {
  horizontal?: WithDefault<'left' | 'center' | 'right', 'left'>;
  vertical?: WithDefault<'top' | 'bottom', 'bottom'>;
}

export interface YandexLogoPadding {
  horizontal?: Double;
  vertical?: Double;
}

export interface Point {
  lat: Double;
  lon: Double;
}

export interface CameraPositionNativeResponse {
  id: string;
  point: {
    lat: Double;
    lon: Double;
  }
  azimuth: Double;
  finished: boolean;
  reason: string;
  tilt: Double;
  zoom: Double;
}

export interface UserPosition {
}

export type VisibleRegionNativeResponse = {
  id: string;
  bottomLeft: {
    lat: Double;
    lon: Double;
  };
  bottomRight: {
    lat: Double;
    lon: Double;
  };
  topLeft: {
    lat: Double;
    lon: Double;
  };
  topRight: {
    lat: Double;
    lon: Double;
  };
}

export type ScreenPointsNativeResponse = {
  id: string;
  screenPoints: {
    x: Double;
    y: Double;
  }[]
}

export type WorldPointsNativeResponse = {
  id: string;
  worldPoints: {
    lat: Double;
    lon: Double;
  }[]
}

export interface MapLoaded {
  renderObjectCount: Double;
  curZoomModelsLoaded: Double;
  curZoomPlacemarksLoaded: Double;
  curZoomLabelsLoaded: Double;
  curZoomGeometryLoaded: Double;
  tileMemoryUsage: Double;
  delayedGeometryLoaded: Double;
  fullyAppeared: Double;
  fullyLoaded: Double;
}

export interface YamapNativeProps extends ViewProps {
  userLocationIconScale?: Float;
  showUserPosition?: boolean;
  nightMode?: boolean;
  mapStyle?: string;
  mapType?: WithDefault<'none' | 'raster' | 'vector', 'vector'>;
  onCameraPositionChange?: DirectEventHandler<CameraPositionNativeResponse>;
  onCameraPositionChangeEnd?: DirectEventHandler<CameraPositionNativeResponse>;
  onMapPress?: BubblingEventHandler<Point>;
  onMapLongPress?: BubblingEventHandler<Point>;
  onMapLoaded?: DirectEventHandler<MapLoaded>;
  userLocationAccuracyFillColor?: Int32;
  userLocationAccuracyStrokeColor?: Int32;
  userLocationAccuracyStrokeWidth?: Float;
  scrollGesturesDisabled?: boolean;
  zoomGesturesDisabled?: boolean;
  tiltGesturesDisabled?: boolean;
  rotateGesturesDisabled?: boolean;
  fastTapDisabled?: boolean;
  initialRegion?: InitialRegion;
  followUser?: boolean;
  logoPosition?: YandexLogoPosition;
  logoPadding?: YandexLogoPadding;
  userLocationIcon: string | undefined;
  interactiveDisabled?: boolean;

  onCameraPositionReceived: DirectEventHandler<CameraPositionNativeResponse>;
  onVisibleRegionReceived: DirectEventHandler<VisibleRegionNativeResponse>;
  onWorldToScreenPointsReceived: DirectEventHandler<ScreenPointsNativeResponse>;
  onScreenToWorldPointsReceived: DirectEventHandler<WorldPointsNativeResponse>;
}

export type YamapNativeRef = Component<YamapNativeProps, {}, any> & Readonly<NativeMethods>
export type YamapComponentType = NativeComponentType<YamapNativeProps> & Readonly<YamapNativeCommands>;

require('./commands/yamap');

export default codegenNativeComponent<YamapNativeProps>('YamapView');
