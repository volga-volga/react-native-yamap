import {type TurboModule, TurboModuleRegistry} from 'react-native';
import {Double} from 'react-native/Libraries/Types/CodegenTypes';
import type {RoutesFoundState, Vehicles} from "../interfaces";

interface Point {
  lat: Double;
  lon: Double;
}

interface Spec extends TurboModule {
  findRoutes(points: Point[], vehicles: Vehicles[]): Promise<RoutesFoundState>
  findParkRoutes(start: Point, end: Point, zones: Point[][]): Promise<RoutesFoundState>
}

export default TurboModuleRegistry.getEnforcing<Spec>('RTNTransportModule');
