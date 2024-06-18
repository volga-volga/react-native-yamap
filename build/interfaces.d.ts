export interface Point {
    lat: number;
    lon: number;
}
export interface BoundingBox {
    southWest: Point;
    northEast: Point;
}
export interface ScreenPoint {
    x: number;
    y: number;
}
export interface MapLoaded {
    renderObjectCount: number;
    curZoomModelsLoaded: number;
    curZoomPlacemarksLoaded: number;
    curZoomLabelsLoaded: number;
    curZoomGeometryLoaded: number;
    tileMemoryUsage: number;
    delayedGeometryLoaded: number;
    fullyAppeared: number;
    fullyLoaded: number;
}
export interface InitialRegion {
    lat: number;
    lon: number;
    zoom?: number;
    azimuth?: number;
    tilt?: number;
}
export declare type MasstransitVehicles = 'bus' | 'trolleybus' | 'tramway' | 'minibus' | 'suburban' | 'underground' | 'ferry' | 'cable' | 'funicular';
export declare type Vehicles = MasstransitVehicles | 'walk' | 'car';
export declare type MapType = 'none' | 'raster' | 'vector';
export interface DrivingInfo {
    time: string;
    timeWithTraffic: string;
    distance: number;
}
export interface MasstransitInfo {
    time: string;
    transferCount: number;
    walkingDistance: number;
}
export interface RouteInfo<T extends (DrivingInfo | MasstransitInfo)> {
    id: string;
    sections: {
        points: Point[];
        sectionInfo: T;
        routeInfo: T;
        routeIndex: number;
        stops: any[];
        type: string;
        transports?: any;
        sectionColor?: string;
    }[];
}
export interface RoutesFoundEvent<T extends (DrivingInfo | MasstransitInfo)> {
    nativeEvent: {
        status: 'success' | 'error';
        id: string;
        routes: RouteInfo<T>[];
    };
}
export interface CameraPosition {
    zoom: number;
    tilt: number;
    azimuth: number;
    point: Point;
    finished: boolean;
}
export declare type VisibleRegion = {
    bottomLeft: Point;
    bottomRight: Point;
    topLeft: Point;
    topRight: Point;
};
export declare enum Animation {
    SMOOTH = 0,
    LINEAR = 1
}
export declare type YandexLogoPosition = {
    horizontal?: 'left' | 'center' | 'right';
    vertical?: 'top' | 'bottom';
};
export declare type YandexLogoPadding = {
    horizontal?: number;
    vertical?: number;
};
