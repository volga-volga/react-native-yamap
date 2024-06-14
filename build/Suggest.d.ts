import { BoundingBox, Point } from './interfaces';
export declare type YamapSuggest = {
    title: string;
    subtitle?: string;
    uri?: string;
};
export declare type YamapCoords = {
    lon: number;
    lat: number;
};
export declare type YamapSuggestWithCoords = YamapSuggest & Partial<YamapCoords>;
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
export declare type SuggestOptions = {
    userPosition?: Point;
    boundingBox?: BoundingBox;
    suggestWords?: boolean;
    suggestTypes?: SuggestTypes[];
};
declare type SuggestFetcher = (query: string, options?: SuggestOptions) => Promise<Array<YamapSuggest>>;
declare type SuggestWithCoordsFetcher = (query: string, options?: SuggestOptions) => Promise<Array<YamapSuggestWithCoords>>;
declare type SuggestResetter = () => Promise<void>;
declare type LatLonGetter = (suggest: YamapSuggest) => YamapCoords | undefined;
declare const Suggest: {
    suggest: SuggestFetcher;
    suggestWithCoords: SuggestWithCoordsFetcher;
    reset: SuggestResetter;
    getCoordsFromSuggest: LatLonGetter;
};
export default Suggest;
