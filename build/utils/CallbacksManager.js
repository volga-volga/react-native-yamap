"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var index_1 = require("./index");
var CallbacksManager = /** @class */ (function () {
    function CallbacksManager() {
    }
    CallbacksManager.addCallback = function (callback) {
        var id = (0, index_1.guid)();
        CallbacksManager.callbacks[id] = function () {
            var args = [];
            for (var _i = 0; _i < arguments.length; _i++) {
                args[_i] = arguments[_i];
            }
            callback.apply(void 0, args);
            delete CallbacksManager.callbacks[id];
        };
        return id;
    };
    CallbacksManager.call = function (id) {
        var _a;
        var args = [];
        for (var _i = 1; _i < arguments.length; _i++) {
            args[_i - 1] = arguments[_i];
        }
        if (CallbacksManager.callbacks[id]) {
            (_a = CallbacksManager.callbacks)[id].apply(_a, args);
        }
    };
    CallbacksManager.callbacks = {};
    return CallbacksManager;
}());
exports.default = CallbacksManager;
//# sourceMappingURL=CallbacksManager.js.map