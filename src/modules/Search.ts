import NativeSearchModule, {type FigureParams, type Point, type SearchOptions} from '../spec/NativeSearchModule';

export const Search = {
  searchText: (query: string, figure?: FigureParams, options?: SearchOptions) =>
    NativeSearchModule.searchByAddress(query, figure, options || {}),
  searchPoint: (point: Point, zoom?: number, options?: SearchOptions) =>
    NativeSearchModule.searchByPoint(point, zoom || 15, options || {}),
  geocodePoint: (point: Point) =>
    NativeSearchModule.geoToAddress(point),
  geocodeAddress: (address: string) =>
    NativeSearchModule.addressToGeo(address),
  resolveURI: (query: string, options?: SearchOptions) =>
    NativeSearchModule.resolveURI(query, options || {}),
  searchByURI: (query: string, options?: SearchOptions) =>
    NativeSearchModule.searchByURI(query, options || {}),
};
