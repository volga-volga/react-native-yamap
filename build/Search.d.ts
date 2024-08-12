import { BoundingBox, Point } from './interfaces';
import { Address } from './geocoding';
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
export declare enum SearchTypes {
    YMKSearchTypeUnspecified = 0,
    /**
     * Toponyms.
     */
    YMKSearchTypeGeo = 1,
    /**
     * Companies.
     */
    YMKSearchTypeBiz = 2
}
export declare enum SearchTypesSnippets {
    YMKSearchTypeUnspecified = 0,
    /**
     * Toponyms.
     */
    YMKSearchTypeGeo = 1,
    /**
     * Companies.
     */
    YMKSearchTypeBiz = 1
}
export type SearchOptions = {
    disableSpellingCorrection?: boolean;
    geometry?: boolean;
    snippets?: SearchTypesSnippets;
    searchTypes?: SearchTypes;
};
export declare enum GeoFigureType {
    POINT = "POINT",
    BOUNDINGBOX = "BOUNDINGBOX",
    POLYLINE = "POLYLINE",
    POLYGON = "POLYGON"
}
export interface PointParams {
    type: GeoFigureType.POINT;
    value: Point;
}
export interface BoundingBoxParams {
    type: GeoFigureType.BOUNDINGBOX;
    value: BoundingBox;
}
export interface PolylineParams {
    type: GeoFigureType.POLYLINE;
    value: PolylineParams;
}
export interface PolygonParams {
    type: GeoFigureType.POLYGON;
    value: PolygonParams;
}
type FigureParams = PointParams | BoundingBoxParams | PolylineParams | PolygonParams;
type SearchFetcher = (query: string, options?: SearchOptions) => Promise<Array<YamapSearch>>;
type SearchPointFetcher = (point: Point, options?: SearchOptions) => Promise<Address>;
declare const Search: {
    searchText: (query: string, figure?: FigureParams, options?: SearchOptions) => any;
    searchPoint: (point: Point, zoom?: number, options?: SearchOptions) => Promise<Address[]>;
    geocodePoint: SearchPointFetcher;
    geocodeAddress: SearchFetcher;
    resolveURI: SearchFetcher;
    searchByURI: SearchFetcher;
};
export default Search;
