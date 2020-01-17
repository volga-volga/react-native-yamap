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

interface MarkerProps {
  children?: React.ReactElement;
  zIndex?: number;
  scale?: number;
  onPress?: () => void;
  point: Point;
  source?: ImageSource;
}

export class Marker extends React.Component<MarkerProps> {
}

interface PolylineProps {
  strokeColor?: string;
  outlineColor?: string;
  strokeWidth?: number;
  outlineWidth?: number;
  dashLength?: number;
  dashOffset?: number;
  gapLength?: number;
  zIndex?: number;
  onPress?: () => void;
  points: Point[];
}

export class Polyline extends React.Component<PolylineProps> {
}

interface PolygonProps {
  fillColor?: string;
  strokeColor?: string;
  strokeWidth?: number;
  zIndex?: number;
  onPress?: () => void;
  points: Point[];
  innerRings: (Point[])[];
}

export class Polygon extends React.Component<PolygonProps> {
}

export default YaMap;

export interface Address {
  country_code: string;
  formatted: string;
  postal_code: string;
  Components: {kind: string, name: string}[];
}

export type ObjectKind = 'house' | 'street' | 'metro' | 'district' | 'locality';
export type Lang = 'ru_RU' | 'uk_UA' | 'be_BY' | 'en_RU' | 'en_US' | 'tr_TR';
export type YandexGeoResponse = any;

export class Geocoder {
  static init(apiKey: string): void;

  static geocode(geocode: Point, kind?: ObjectKind, results?: number, skip?: number, lang?: Lang): Promise<YandexGeoResponse>;

  static reverseGeocode(geocode: string, kind?: ObjectKind, results?: number, skip?: number, lang?: Lang, rspn?: 0 | 1, ll?: Point, spn?: [number, number], bbox?: [Point, Point]): Promise<YandexGeoResponse>;

  static addressToGeo(address: string): Promise<Point | null>

  static getToAddress(geo: Point): Promise<Address | null>
}
