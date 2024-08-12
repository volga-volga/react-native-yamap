"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.GeoFigureType = exports.SearchTypesSnippets = exports.SearchTypes = void 0;
var react_native_1 = require("react-native");
var YamapSearch = react_native_1.NativeModules.YamapSearch;
var SearchTypes;
(function (SearchTypes) {
    SearchTypes[SearchTypes["YMKSearchTypeUnspecified"] = 0] = "YMKSearchTypeUnspecified";
    /**
     * Toponyms.
     */
    SearchTypes[SearchTypes["YMKSearchTypeGeo"] = 1] = "YMKSearchTypeGeo";
    /**
     * Companies.
     */
    SearchTypes[SearchTypes["YMKSearchTypeBiz"] = 2] = "YMKSearchTypeBiz";
    /**
     * Mass transit routes.
     */
})(SearchTypes = exports.SearchTypes || (exports.SearchTypes = {}));
var SearchTypesSnippets;
(function (SearchTypesSnippets) {
    SearchTypesSnippets[SearchTypesSnippets["YMKSearchTypeUnspecified"] = 0] = "YMKSearchTypeUnspecified";
    /**
     * Toponyms.
     */
    SearchTypesSnippets[SearchTypesSnippets["YMKSearchTypeGeo"] = 1] = "YMKSearchTypeGeo";
    /**
     * Companies.
     */
    SearchTypesSnippets[SearchTypesSnippets["YMKSearchTypeBiz"] = 1] = "YMKSearchTypeBiz";
    /**
     * Mass transit routes.
     */
})(SearchTypesSnippets = exports.SearchTypesSnippets || (exports.SearchTypesSnippets = {}));
var GeoFigureType;
(function (GeoFigureType) {
    GeoFigureType["POINT"] = "POINT";
    GeoFigureType["BOUNDINGBOX"] = "BOUNDINGBOX";
    GeoFigureType["POLYLINE"] = "POLYLINE";
    GeoFigureType["POLYGON"] = "POLYGON";
})(GeoFigureType = exports.GeoFigureType || (exports.GeoFigureType = {}));
var searchText = function (query, figure, options) {
    return YamapSearch.searchByAddress(query, figure, options);
};
var searchPoint = function (point, zoom, options) {
    return YamapSearch.searchByPoint(point, zoom, options);
};
var resolveURI = function (uri, options) {
    return YamapSearch.resolveURI(uri, options);
};
var searchByURI = function (uri, options) {
    return YamapSearch.searchByURI(uri, options);
};
var geocodePoint = function (point) {
    return YamapSearch.geoToAddress(point);
};
var geocodeAddress = function (address) {
    return YamapSearch.addressToGeo(address);
};
var Search = {
    searchText: searchText,
    searchPoint: searchPoint,
    geocodePoint: geocodePoint,
    geocodeAddress: geocodeAddress,
    resolveURI: resolveURI,
    searchByURI: searchByURI
};
exports.default = Search;
//# sourceMappingURL=Search.js.map