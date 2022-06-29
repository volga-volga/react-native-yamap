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
declare type SuggestFetcher = (query: string) => Promise<Array<YamapSuggest>>;
declare type SuggestWithCoordsFetcher = (query: string) => Promise<Array<YamapSuggestWithCoords>>;
declare type SuggestResetter = () => Promise<void>;
declare type LatLonGetter = (suggest: YamapSuggest) => YamapCoords | undefined;
declare const Suggest: {
    suggest: SuggestFetcher;
    suggestWithCoords: SuggestWithCoordsFetcher;
    reset: SuggestResetter;
    getCoordsFromSuggest: LatLonGetter;
};
export default Suggest;
