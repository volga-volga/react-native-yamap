import * as React from 'react';
import { ViewProps } from 'react-native';

declare module 'react-native-yamap';

export interface Marker {
  id: number,
  lon: number,
  lat: number,
  selected: boolean,
}

export interface Point {
  lat: number,
  lon: number,
}

export interface Route {
  start: Point,
  end: Point,
}

interface Props extends ViewProps {
  onMarkerPress?: (id: string) => void,
  onRouteFound?: (event: Event) => void,
  markers: Marker[],
  center: { lon: number, lat: number, zoom: number },
  route?: Route
  vehicles?: Array<string>

  /** supported vehicle types
   *  bus, railway, tramway, suburban, underground, walk
   */
  routeColors?: { [key: string]: string }
}

declare class YaMap extends React.Component<Props> {
  static init(apiKey: string): void;
}

export default YaMap;
