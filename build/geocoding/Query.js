"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var Query = /** @class */ (function () {
    function Query(data) {
        this._data = JSON.parse(JSON.stringify(data));
    }
    Query.prototype.toQueryString = function () {
        var res = '';
        for (var key in this._data) {
            var AMPERSAND = res.length > 0 ? '&' : '';
            res = "" + res + AMPERSAND + encodeURIComponent(key) + "=" + encodeURIComponent(this._data[key]);
        }
        return res;
    };
    return Query;
}());
exports.default = Query;
//# sourceMappingURL=Query.js.map