interface InData {
  [key: string]: string | number | boolean | undefined | null;
}

interface ParsedData {
  [key: string]: string | number | boolean;
}

export default class Query {
  private _data: ParsedData;

  constructor(data: InData) {
    this._data = JSON.parse(JSON.stringify(data));
  }

  public toQueryString(): string {
    let res = '';

    for (const key in this._data) {
      const AMPERSAND = res.length > 0 ? '&' : '';
      res = `${res}${AMPERSAND}${encodeURIComponent(key)}=${encodeURIComponent(this._data[key])}`;
    }

    return res;
  }
}
