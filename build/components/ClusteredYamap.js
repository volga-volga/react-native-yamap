"use strict";
var __extends = (this && this.__extends) || (function () {
    var extendStatics = function (d, b) {
        extendStatics = Object.setPrototypeOf ||
            ({ __proto__: [] } instanceof Array && function (d, b) { d.__proto__ = b; }) ||
            function (d, b) { for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p]; };
        return extendStatics(d, b);
    };
    return function (d, b) {
        extendStatics(d, b);
        function __() { this.constructor = d; }
        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
})();
var __assign = (this && this.__assign) || function () {
    __assign = Object.assign || function(t) {
        for (var s, i = 1, n = arguments.length; i < n; i++) {
            s = arguments[i];
            for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p))
                t[p] = s[p];
        }
        return t;
    };
    return __assign.apply(this, arguments);
};
var __rest = (this && this.__rest) || function (s, e) {
    var t = {};
    for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p) && e.indexOf(p) < 0)
        t[p] = s[p];
    if (s != null && typeof Object.getOwnPropertySymbols === "function")
        for (var i = 0, p = Object.getOwnPropertySymbols(s); i < p.length; i++) {
            if (e.indexOf(p[i]) < 0 && Object.prototype.propertyIsEnumerable.call(s, p[i]))
                t[p[i]] = s[p[i]];
        }
    return t;
};
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.ClusteredYamap = void 0;
var react_1 = __importDefault(require("react"));
var react_native_1 = require("react-native");
// @ts-ignore
var resolveAssetSource_1 = __importDefault(require("react-native/Libraries/Image/resolveAssetSource"));
var CallbacksManager_1 = __importDefault(require("../utils/CallbacksManager"));
var interfaces_1 = require("../interfaces");
var utils_1 = require("../utils");
var Yamap_1 = require("./Yamap");
var NativeYamapModule = react_native_1.NativeModules.yamap;
var YaMapNativeComponent = react_native_1.requireNativeComponent('ClusteredYamapView');
var ClusteredYamap = /** @class */ (function (_super) {
    __extends(ClusteredYamap, _super);
    function ClusteredYamap() {
        var _this = _super !== null && _super.apply(this, arguments) || this;
        // @ts-ignore
        _this.map = react_1.default.createRef();
        return _this;
    }
    ClusteredYamap.init = function (apiKey) {
        return NativeYamapModule.init(apiKey);
    };
    ClusteredYamap.setLocale = function (locale) {
        return new Promise(function (resolve, reject) {
            NativeYamapModule.setLocale(locale, function () { return resolve(); }, function (err) { return reject(new Error(err)); });
        });
    };
    ClusteredYamap.getLocale = function () {
        return new Promise(function (resolve, reject) {
            NativeYamapModule.getLocale(function (locale) { return resolve(locale); }, function (err) { return reject(new Error(err)); });
        });
    };
    ClusteredYamap.resetLocale = function () {
        return new Promise(function (resolve, reject) {
            NativeYamapModule.resetLocale(function () { return resolve(); }, function (err) { return reject(new Error(err)); });
        });
    };
    ClusteredYamap.prototype.findRoutes = function (points, vehicles, callback) {
        this._findRoutes(points, vehicles, callback);
    };
    ClusteredYamap.prototype.findMasstransitRoutes = function (points, callback) {
        this._findRoutes(points, Yamap_1.YaMap.ALL_MASSTRANSIT_VEHICLES, callback);
    };
    ClusteredYamap.prototype.findPedestrianRoutes = function (points, callback) {
        this._findRoutes(points, [], callback);
    };
    ClusteredYamap.prototype.findDrivingRoutes = function (points, callback) {
        this._findRoutes(points, ['car'], callback);
    };
    ClusteredYamap.prototype.fitAllMarkers = function () {
        react_native_1.UIManager.dispatchViewManagerCommand(react_native_1.findNodeHandle(this), this.getCommand('fitAllMarkers'), []);
    };
    ClusteredYamap.prototype.setTrafficVisible = function (isVisible) {
        react_native_1.UIManager.dispatchViewManagerCommand(react_native_1.findNodeHandle(this), this.getCommand('setTrafficVisible'), [isVisible]);
    };
    ClusteredYamap.prototype.fitMarkers = function (points) {
        react_native_1.UIManager.dispatchViewManagerCommand(react_native_1.findNodeHandle(this), this.getCommand('fitMarkers'), [points]);
    };
    ClusteredYamap.prototype.setCenter = function (center, zoom, azimuth, tilt, duration, animation) {
        if (zoom === void 0) { zoom = center.zoom || 10; }
        if (azimuth === void 0) { azimuth = 0; }
        if (tilt === void 0) { tilt = 0; }
        if (duration === void 0) { duration = 0; }
        if (animation === void 0) { animation = interfaces_1.Animation.SMOOTH; }
        react_native_1.UIManager.dispatchViewManagerCommand(react_native_1.findNodeHandle(this), this.getCommand('setCenter'), [center, zoom, azimuth, tilt, duration, animation]);
    };
    ClusteredYamap.prototype.setZoom = function (zoom, duration, animation) {
        if (duration === void 0) { duration = 0; }
        if (animation === void 0) { animation = interfaces_1.Animation.SMOOTH; }
        react_native_1.UIManager.dispatchViewManagerCommand(react_native_1.findNodeHandle(this), this.getCommand('setZoom'), [zoom, duration, animation]);
    };
    ClusteredYamap.prototype.getCameraPosition = function (callback) {
        var cbId = CallbacksManager_1.default.addCallback(callback);
        react_native_1.UIManager.dispatchViewManagerCommand(react_native_1.findNodeHandle(this), this.getCommand('getCameraPosition'), [cbId]);
    };
    ClusteredYamap.prototype.getVisibleRegion = function (callback) {
        var cbId = CallbacksManager_1.default.addCallback(callback);
        react_native_1.UIManager.dispatchViewManagerCommand(react_native_1.findNodeHandle(this), this.getCommand('getVisibleRegion'), [cbId]);
    };
    ClusteredYamap.prototype.getScreenPoints = function (points, callback) {
        var cbId = CallbacksManager_1.default.addCallback(callback);
        react_native_1.UIManager.dispatchViewManagerCommand(react_native_1.findNodeHandle(this), this.getCommand('getScreenPoints'), [points, cbId]);
    };
    ClusteredYamap.prototype.getWorldPoints = function (points, callback) {
        var cbId = CallbacksManager_1.default.addCallback(callback);
        react_native_1.UIManager.dispatchViewManagerCommand(react_native_1.findNodeHandle(this), this.getCommand('getWorldPoints'), [points, cbId]);
    };
    ClusteredYamap.prototype._findRoutes = function (points, vehicles, callback) {
        var cbId = CallbacksManager_1.default.addCallback(callback);
        var args = react_native_1.Platform.OS === 'ios' ? [{ points: points, vehicles: vehicles, id: cbId }] : [points, vehicles, cbId];
        react_native_1.UIManager.dispatchViewManagerCommand(react_native_1.findNodeHandle(this), this.getCommand('findRoutes'), args);
    };
    ClusteredYamap.prototype.getCommand = function (cmd) {
        return react_native_1.Platform.OS === 'ios' ? react_native_1.UIManager.getViewManagerConfig('ClusteredYamapView').Commands[cmd] : cmd;
    };
    ClusteredYamap.prototype.processRoute = function (event) {
        var _a = event.nativeEvent, id = _a.id, routes = __rest(_a, ["id"]);
        CallbacksManager_1.default.call(id, routes);
    };
    ClusteredYamap.prototype.processCameraPosition = function (event) {
        var _a = event.nativeEvent, id = _a.id, point = __rest(_a, ["id"]);
        CallbacksManager_1.default.call(id, point);
    };
    ClusteredYamap.prototype.processVisibleRegion = function (event) {
        var _a = event.nativeEvent, id = _a.id, visibleRegion = __rest(_a, ["id"]);
        CallbacksManager_1.default.call(id, visibleRegion);
    };
    ClusteredYamap.prototype.processWorldToScreenPointsReceived = function (event) {
        var _a = event.nativeEvent, id = _a.id, screenPoints = __rest(_a, ["id"]);
        CallbacksManager_1.default.call(id, screenPoints);
    };
    ClusteredYamap.prototype.processScreenToWorldPointsReceived = function (event) {
        var _a = event.nativeEvent, id = _a.id, worldPoints = __rest(_a, ["id"]);
        CallbacksManager_1.default.call(id, worldPoints);
    };
    ClusteredYamap.prototype.resolveImageUri = function (img) {
        return img ? resolveAssetSource_1.default(img).uri : '';
    };
    ClusteredYamap.prototype.getProps = function () {
        var props = __assign(__assign({}, this.props), { clusteredMarkers: this.props.clusteredMarkers.map(function (mark) { return mark.point; }), children: this.props.clusteredMarkers.map(this.props.renderMarker), onRouteFound: this.processRoute, onCameraPositionReceived: this.processCameraPosition, onVisibleRegionReceived: this.processVisibleRegion, onWorldToScreenPointsReceived: this.processWorldToScreenPointsReceived, onScreenToWorldPointsReceived: this.processScreenToWorldPointsReceived, userLocationIcon: this.props.userLocationIcon ? this.resolveImageUri(this.props.userLocationIcon) : undefined });
        utils_1.processColorProps(props, 'clusterColor');
        utils_1.processColorProps(props, 'userLocationAccuracyFillColor');
        utils_1.processColorProps(props, 'userLocationAccuracyStrokeColor');
        return props;
    };
    ClusteredYamap.prototype.render = function () {
        return (react_1.default.createElement(YaMapNativeComponent, __assign({}, this.getProps(), { ref: this.map })));
    };
    ClusteredYamap.defaultProps = {
        showUserPosition: true,
        clusterColor: 'red',
        maxFps: 60
    };
    ClusteredYamap.ALL_MASSTRANSIT_VEHICLES = [
        'bus',
        'trolleybus',
        'tramway',
        'minibus',
        'suburban',
        'underground',
        'ferry',
        'cable',
        'funicular',
    ];
    return ClusteredYamap;
}(react_1.default.Component));
exports.ClusteredYamap = ClusteredYamap;
//# sourceMappingURL=ClusteredYamap.js.map