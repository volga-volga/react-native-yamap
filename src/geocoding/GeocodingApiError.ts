export class GeocodingApiError extends Error {
  readonly yandexResponse: any;

  constructor(response: any) {
    super('api error');
    this.yandexResponse = response;
  }
}
