import type {Point} from "../";

export interface BoundingBox {
  southWest: Point;
  northEast: Point;
}

export type WorldPointsCallback = (result: {worldPoints: Point[]}) => void

export interface ScreenPoint {
  x: number
  y: number
}

export type ScreenPointsCallback = (result: {screenPoints: ScreenPoint[]}) => void

export type MasstransitVehicles =
  | 'bus'
  | 'trolleybus'
  | 'tramway'
  | 'minibus'
  | 'suburban'
  | 'underground'
  | 'ferry'
  | 'cable'
  | 'funicular'

export type Vehicles = MasstransitVehicles | 'walk' | 'car'

export const ALL_MASSTRANSIT_VEHICLES: Vehicles[] = [
  'bus',
  'trolleybus',
  'tramway',
  'minibus',
  'suburban',
  'underground',
  'ferry',
  'cable',
  'funicular',
]

export interface CameraPosition {
  point: Point
  azimuth: number
  finished: boolean
  reason: 'APPLICATION' | 'GESTURES'
  tilt: number
  zoom: number
}

export type CameraPositionCallback = (position: CameraPosition) => void

export type UserPositionCallback = (userPosition: Point) => void

export type VisibleRegion = {
  bottomLeft: Point
  bottomRight: Point
  topLeft: Point
  topRight: Point
}

export type VisibleRegionCallback = (visibleRegion: VisibleRegion) => void

export enum Animation {
  SMOOTH,
  LINEAR,
}

export enum AddressKind {
  UNKNOWN,
  COUNTRY,
  REGION,
  PROVINCE,
  AREA,
  LOCALITY,
  DISTRICT,
  STREET,
  HOUSE,
  ENTRANCE,
  LEVEL,
  APARTMENT,
  ROUTE,
  STATION,
  METRO_STATION,
  RAILWAY_STATION,
  VEGETATION,
  HYDRO,
  AIRPORT,
  OTHER,
}

export interface Address {
  Components: Array<{name: string; kind: AddressKind}>
  country_code: string
  formatted: string
  point: Point
  uri: string
}

export interface RoutesFoundState {
  id: string;
  status: 'success' | 'error';
  routes: {
    id: string;
    sections: {
      points: {lat: number, lon: number}[];
      sectionInfo: {
        time: string;
        timeWithTraffic?: string;
        distance?: number;
        transferCount?: number;
        walkingDistance?: number;
      };
      routeInfo: {
        time: string;
        timeWithTraffic?: string;
        distance?: number;
        transferCount?: number;
        walkingDistance?: number;
      };
      routeIndex: number;
      stops: string[];
      type: string;
      transports: {type: string[]};
      sectionColor?: string;
    }[];
  }[];
}
