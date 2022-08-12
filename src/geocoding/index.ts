import Query from './Query';
import { GeocodingApiError } from './GeocodingApiError';
import { Point } from '../interfaces';

export type ObjectKind = 'house' | 'street' | 'metro' | 'district' | 'locality';
export type Lang = 'ru_RU' | 'uk_UA' | 'be_BY' | 'en_RU' | 'en_US' | 'tr_TR';

export type YandexGeoResponse = any;

export interface Address {
  country_code: string;
  formatted: string;
  postal_code: string;
  Components: {kind: string, name: string}[];
}

export class Geocoder {
  static API_KEY = '';

  static init(apiKey: string) {
    Geocoder.API_KEY = apiKey;
  }

  private static async requestWithQuery(query: Query) {
    const res = await fetch(
      'https://geocode-maps.yandex.ru/1.x?' + query.toQueryString(),
      {
        method: 'get',
        headers: {
          'content-type': 'application/json',
          accept: 'application/json',
        }
      }
    );

    if (res.status !== 200) {
      throw new GeocodingApiError(res);
    }

    return res.json();
  }

  private static getFirst(response: any): any {
    // @ts-ignore
    return Object.values(response.GeoObjectCollection.featureMember[0])[0];
  }

  static async geocode(geocode: Point, kind?: ObjectKind, results?: number, skip?: number, lang?: Lang): Promise<YandexGeoResponse> {
    const query = new Query({
      apikey: Geocoder.API_KEY,
      geocode: `${geocode.lat},${geocode.lon}`,
      sco: 'latlong',
      kind,
      format: 'json',
      results,
      skip,
      lang,
    });

    return Geocoder.requestWithQuery(query);
  }

  static reverseGeocode(geocode: string, kind?: ObjectKind, results?: number, skip?: number, lang?: Lang, rspn?: 0 | 1, ll?: Point, spn?: [number, number], bbox?: [Point, Point]): Promise<YandexGeoResponse> {
    const query = new Query({
      apikey: Geocoder.API_KEY,
      geocode,
      format: 'json',
      results,
      skip,
      lang,
      rspn,
      ll: ll ? `${ll.lat},${ll.lon}` : undefined,
      spn: spn ? `${spn[0]},${spn[1]}` : undefined,
      bbox: bbox
        ? `${bbox[0].lat},${bbox[0].lon}-${bbox[1].lat},${bbox[1].lon}`
        : undefined
    });

    return Geocoder.requestWithQuery(query);
  }

  static async addressToGeo(address: string): Promise<Point | undefined> {
    const { response } = await Geocoder.reverseGeocode(address);

    if (
      response.GeoObjectCollection
      && response.GeoObjectCollection.featureMember
      && response.GeoObjectCollection.featureMember.length > 0
    ) {
      const obj = Geocoder.getFirst(response);

      if (obj.Point) {
        const [lon, lat] = obj.Point.pos.split(' ').map(Number);
        return { lon, lat };
      }
    }

    return undefined;
  }

  static async geoToAddress(geo: Point): Promise<Address | undefined> {
    const { response } = await Geocoder.geocode(geo);

    if (
      response.GeoObjectCollection
      && response.GeoObjectCollection.featureMember
      && response.GeoObjectCollection.featureMember.length > 0
    ) {
      const obj = Geocoder.getFirst(response);
      return obj.metaDataProperty.GeocoderMetaData.Address;
    }

    return undefined;
  }
}
