export default class Query {
  constructor(data) {
    this._data = JSON.parse(JSON.stringify(data));
  }

  toQueryString() {
    let res = '';
    for (const key in this._data) {
      const AMPERSAND = res.length > 0 ? '&' : '';
      res = `${res}${AMPERSAND}${encodeURIComponent(key)}=${encodeURIComponent(
        this._data[key],
      )}`;
    }
    return res;
  }
}
