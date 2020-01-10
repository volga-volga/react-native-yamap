import Query from './Query';

export default class Geocoder {
  static API_KEY = '';

  static init(apiKey) {
    Geocoder.API_KEY = apiKey;
  }

  static async requestWithQuery(query) {
    const res = await fetch(
      'https://geocode-maps.yandex.ru/1.x?' + query.toQueryString(),
      {
        method: 'get',
        headers: {
          'content-type': 'application/json',
          accept: 'application/json',
        },
      },
    );
    return res.json();
  }

  static async geocode(geocode, kind, results, skip, lang) {
    const query = new Query({
      apikey: this.API_KEY,
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

  static reverseGeocode(geocode, kind, results, skip, lang, rspn, ll, spn, bbox) {
    const query = new Query({
      apikey: this.API_KEY,
      geocode,
      format: 'json',
      results,
      skip,
      lang,
      rspn,
      spn: spn ? `${spn[0]},${spn[1]}` : undefined,
      bbox: bbox
        ? `${bbox[0].lat},${bbox[0].lon}-${bbox[1].lat},${bbox[1].lon}`
        : undefined,
    });
    return Geocoder.requestWithQuery(query);
  }

  static async addressToGeo(address: string) {
    const {response} = await Geocoder.reverseGeocode(address);
    if (
      response.GeoObjectCollection &&
      response.GeoObjectCollection.featureMember &&
      response.GeoObjectCollection.featureMember.length > 0
    ) {
      const obj = Object.values(
        response.GeoObjectCollection.featureMember[0],
      )[0];
      if (obj.Point) {
        const [lon, lat] = obj.Point.pos.split(' ').map(Number);
        return {lon, lat};
      }
    }
    return null;
  }

  static async geoToAddress(geo: Point) {
    const {response} = await Geocoder.geocode(geo);
    if (
      response.GeoObjectCollection &&
      response.GeoObjectCollection.featureMember &&
      response.GeoObjectCollection.featureMember.length > 0
    ) {
      const obj = Object.values(
        response.GeoObjectCollection.featureMember[0],
      )[0];
      return obj.metaDataProperty.GeocoderMetaData.Address;
    }
    return null;
  }
}
