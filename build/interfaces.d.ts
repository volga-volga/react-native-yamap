export interface Point {
    lat: number;
    lon: number;
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
export declare enum Animation {
    SMOOTH = 0,
    LINEAR = 1
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
