// eslint-disable-next-line @react-native/no-deep-imports
import codegenNativeComponent, {type NativeComponentType} from 'react-native/Libraries/Utilities/codegenNativeComponent';
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
import {type YamapNativeCommands} from './commands/yamap';

interface MapLoaded {
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

interface InitialRegion {
  lat: Double;
  lon: Double;
  zoom?: Double;
  azimuth?: Double;
  tilt?: Double;
}

interface YandexLogoPosition {
  horizontal?: WithDefault<'left' | 'center' | 'right', 'left'>;
  vertical?: WithDefault<'top' | 'bottom', 'bottom'>;
}

interface YandexLogoPadding {
  horizontal?: Double;
  vertical?: Double;
}

interface Point {
  lat: Double;
  lon: Double;
}

interface CameraPositionResponse {
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

type VisibleRegionResponse = {
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

type ScreenPointsResponse = {
  id: string;
  screenPoints: {
    x: Double;
    y: Double;
  }[]
}

type WorldPointsResponse = {
  id: string;
  worldPoints: {
    lat: Double;
    lon: Double;
  }[]
}

export interface YandexClusterSizes {
  width?: Double;
  height?: Double;
}

export interface ClusteredYamapNativeProps extends ViewProps {
  userLocationIconScale?: Float;
  showUserPosition?: boolean;
  nightMode?: boolean;
  mapStyle?: string;
  mapType?: WithDefault<'none' | 'raster' | 'vector', 'none'>;
  onCameraPositionChange?: DirectEventHandler<CameraPositionResponse>;
  onCameraPositionChangeEnd?: DirectEventHandler<CameraPositionResponse>;
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

  onCameraPositionReceived: DirectEventHandler<CameraPositionResponse>;
  onVisibleRegionReceived: DirectEventHandler<VisibleRegionResponse>;
  onWorldToScreenPointsReceived: DirectEventHandler<ScreenPointsResponse>;
  onScreenToWorldPointsReceived: DirectEventHandler<WorldPointsResponse>;

  clusteredMarkers: Point[];
  clusterColor?: Int32;
  clusterIcon?: string | undefined;
  clusterSize?: YandexClusterSizes;
  clusterTextSize?: Float;
  clusterTextYOffset?: Int32;
  clusterTextXOffset?: Int32;
  clusterTextColor?: Int32;
}

export type ClusteredYamapNativeRef = Component<ClusteredYamapNativeProps, {}, any> & Readonly<NativeMethods>
export type ClusteredYamapComponentType = NativeComponentType<ClusteredYamapNativeProps> & Readonly<YamapNativeCommands>;

require('./commands/yamap');

export default codegenNativeComponent<ClusteredYamapNativeProps>('ClusteredYamapView');
