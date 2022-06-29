interface InData {
    [key: string]: string | number | boolean | undefined | null;
}
export default class Query {
    private _data;
    constructor(data: InData);
    toQueryString(): string;
}
export {};
