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
  onMarkerPress?: (id: string) => void,
  onRouteFound?: (event: Event) => void,
  markers?: Marker[],
  center?: { lon: number, lat: number, zoom: number },
  route?: Route
  vehicles?: Array<Vehiles>

  /** supported vehicle types
   *  bus, railway, tramway, suburban, trolleybus, underground, walk
   */
  routeColors?: { [key: string]: string }
}

declare class YaMap extends React.Component<Props> {
  static init(apiKey: string): void;
}

export default YaMap;
