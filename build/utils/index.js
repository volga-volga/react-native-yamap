"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.guid = exports.processColorProps = void 0;
var react_native_1 = require("react-native");
function processColorProps(props, name) {
    if (props[name]) {
        /* eslint-disable no-param-reassign */
        // @ts-ignore
        props[name] = react_native_1.processColor(props[name]);
        /* eslint-enable no-param-reassign */
    }
}
exports.processColorProps = processColorProps;
function guid() {
    function s4() {
        return Math.floor((1 + Math.random()) * 0x10000)
            .toString(16)
            .substring(1);
    }
    return "" + s4() + s4() + "-" + s4() + "-" + s4() + "-" + s4() + "-" + s4() + s4() + s4();
}
exports.guid = guid;
//# sourceMappingURL=index.js.map