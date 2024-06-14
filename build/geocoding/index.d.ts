import { Point } from '../interfaces';
export declare type ObjectKind = 'house' | 'street' | 'metro' | 'district' | 'locality';
export declare type Lang = 'ru_RU' | 'uk_UA' | 'be_BY' | 'en_RU' | 'en_US' | 'tr_TR';
export declare type YandexGeoResponse = any;
export interface Address {
    country_code: string;
    formatted: string;
    postal_code: string;
    Components: {
        kind: string;
        name: string;
    }[];
}
export declare class Geocoder {
    static API_KEY: string;
    static init(apiKey: string): void;
    private static requestWithQuery;
    private static getFirst;
    static geocode(geocode: Point, kind?: ObjectKind, results?: number, skip?: number, lang?: Lang): Promise<YandexGeoResponse>;
    static reverseGeocode(geocode: string, kind?: ObjectKind, results?: number, skip?: number, lang?: Lang, rspn?: 0 | 1, ll?: Point, spn?: [number, number], bbox?: [Point, Point]): Promise<YandexGeoResponse>;
    static addressToGeo(address: string): Promise<Point | undefined>;
    static geoToAddress(geo: Point): Promise<Address | undefined>;
}
