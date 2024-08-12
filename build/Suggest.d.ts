import { BoundingBox, Point } from './interfaces';
export type YamapSuggest = {
    title: string;
    subtitle?: string;
    uri?: string;
};
export type YamapCoords = {
    lon: number;
    lat: number;
};
export type YamapSuggestWithCoords = YamapSuggest & Partial<YamapCoords>;
export declare enum SuggestTypes {
    YMKSuggestTypeUnspecified = 0,
    /**
     * Toponyms.
     */
    YMKSuggestTypeGeo = 1,
    /**
     * Companies.
     */
    YMKSuggestTypeBiz = 2,
    /**
     * Mass transit routes.
     */
    YMKSuggestTypeTransit = 4
}
export type SuggestOptions = {
    userPosition?: Point;
    boundingBox?: BoundingBox;
    suggestWords?: boolean;
    suggestTypes?: SuggestTypes[];
};
type SuggestFetcher = (query: string, options?: SuggestOptions) => Promise<Array<YamapSuggest>>;
type SuggestWithCoordsFetcher = (query: string, options?: SuggestOptions) => Promise<Array<YamapSuggestWithCoords>>;
type SuggestResetter = () => Promise<void>;
type LatLonGetter = (suggest: YamapSuggest) => YamapCoords | undefined;
declare const Suggest: {
    suggest: SuggestFetcher;
    suggestWithCoords: SuggestWithCoordsFetcher;
    reset: SuggestResetter;
    getCoordsFromSuggest: LatLonGetter;
};
export default Suggest;
