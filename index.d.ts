import * as React from 'react';
import { ViewProps, ImageSource } from 'react-native';

declare module 'react-native-yamap';

export interface Marker {
  lon: number,
  lat: number,
  id?: number,
  zIndex?: number,
  source?: ImageSource,
}

export interface Point {
  lat: number,
  lon: number,
}

export interface Route {
  start: Point,
  end: Point,
}

export type Vehiles =  'bus' | 'railway' | 'tramway' | 'suburban' | 'trolleybus' | 'underground' | 'walk';

interface Props extends ViewProps {
  userLocationIcon: ImageSource,
  onMarkerPress?: (id: string) => void,
  onRouteFound?: (event: Event) => void,
  markers?: Marker[],
  route?: Route,
  vehicles?: Array<Vehiles>

  /** supported vehicle types
   *  bus, railway, tramway, suburban, trolleybus, underground, walk
   */
  routeColors?: { [key: string]: string }
}

declare class YaMap extends React.Component<Props> {
  static init(apiKey: string): void;
  fitAllMarkers(): void;
  setCenter(center: { lon: number, lat: number, zoom: number }): void;
}

export default YaMap;
