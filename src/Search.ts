import { BoundingBox, Point } from './interfaces';
import { NativeModules } from 'react-native';
import { Address } from './geocoding';

const { YamapSearch } = NativeModules;

export type YamapSearch = {
  title: string;
  subtitle?: string;
  uri?: string;
};
export type YamapCoords = {
  lon: number;
  lat: number;
};
export type YamapSearchWithCoords = YamapSearch & Partial<YamapCoords>;

export enum SearchTypes {
  YMKSearchTypeUnspecified = 0b00,
  /**
   * Toponyms.
   */
  YMKSearchTypeGeo = 0b01,
  /**
   * Companies.
   */
  YMKSearchTypeBiz = 0b01 << 1,
  /**
   * Mass transit routes.
   */
}

export enum SearchTypesSnippets {
  YMKSearchTypeUnspecified = 0b00,
  /**
   * Toponyms.
   */
  YMKSearchTypeGeo = 0b01,
  /**
   * Companies.
   */
  YMKSearchTypeBiz = 0b01 << 32,
  /**
   * Mass transit routes.
   */
}

export type SearchOptions = {
  disableSpellingCorrection?: boolean;
  geometry?: boolean;
  snippets?: SearchTypesSnippets;
  searchTypes?: SearchTypes;
};

export enum GeoFigureType {
  POINT="POINT",
  BOUNDINGBOX="BOUNDINGBOX",
  POLYLINE="POLYLINE",
  POLYGON="POLYGON",
}

export interface PointParams {
  type: GeoFigureType.POINT
  value: Point
}

export interface BoundingBoxParams {
  type: GeoFigureType.BOUNDINGBOX
  value: BoundingBox
}

export interface PolylineParams {
  type: GeoFigureType.POLYLINE
  value: PolylineParams
}

export interface PolygonParams {
  type: GeoFigureType.POLYGON
  value: PolygonParams
}

type FigureParams = PointParams | BoundingBoxParams | PolylineParams | PolygonParams

type SearchFetcher = (query: string, options?: SearchOptions) => Promise<Array<YamapSearch>>;
type SearchPointFetcher = (point: Point, options?: SearchOptions) => Promise<Address>;
const searchText = (query: string, figure?: FigureParams, options?: SearchOptions) => {
  return YamapSearch.searchByAddress(query, figure, options);
}

const searchPoint = (point: Point, zoom?: number, options?: SearchOptions): Promise<Address[]> => {
  return YamapSearch.searchByPoint(point, zoom, options);
}

const resolveURI: SearchFetcher = (uri: string, options) => {
  return YamapSearch.resolveURI(uri, options);
}

const searchByURI: SearchFetcher = (uri: string, options) => {
  return YamapSearch.searchByURI(uri, options);
}

const geocodePoint: SearchPointFetcher = (point: Point) => {
  return YamapSearch.geoToAddress(point);
}

const geocodeAddress: SearchFetcher = (address: string) => {
  return YamapSearch.addressToGeo(address);
}

const Search = {
  searchText,
  searchPoint,
  geocodePoint,
  geocodeAddress,
  resolveURI,
  searchByURI
};

export default Search;
