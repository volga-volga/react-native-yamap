import NativeTransportModule from '../spec/NativeTransportModule';
import {ALL_MASSTRANSIT_VEHICLES} from "../interfaces";
import type {Point} from "../";

export const Transport = {
  findRoutes: NativeTransportModule.findRoutes,
  findMasstransitRoutes: (points: Point[]) => NativeTransportModule.findRoutes(points, ALL_MASSTRANSIT_VEHICLES),
  findPedestrianRoutes: (points: Point[]) => NativeTransportModule.findRoutes(points, []),
  findDrivingRoutes: (points: Point[]) => NativeTransportModule.findRoutes(points, ['car']),
  findParkRoutes: (start: Point, end: Point, zones: Point[][]) => NativeTransportModule.findParkRoutes(start, end, zones),
};
