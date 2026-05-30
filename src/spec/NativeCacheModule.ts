// eslint-disable-next-line @react-native/no-deep-imports
import type {Double, EventEmitter} from 'react-native/Libraries/Types/CodegenTypes';
import {type TurboModule, TurboModuleRegistry} from 'react-native';

export interface Region {
  id: Double,
  name: string,
  parentId?: Double,
  releaseTime: Double,
  size: Double,
}

interface Spec extends TurboModule {
  initManager(): Promise<void>
  allowUseCellularNetwork(useCellular: boolean): Promise<void>
  searchRegions(): Promise<Region[]>
  getRegionInfo(regionId: Double): Promise<void>
  startDownloadRegion(regionId: Double): Promise<void>
  stopDownloadRegion(regionId: Double): Promise<void>
  pauseDownloadRegion(regionId: Double): Promise<void>
  dropRegion(regionId: Double): Promise<void>
  readonly onRegionStateChanged: EventEmitter<{regionId: Double, state: Double}>
  readonly onRegionProgress: EventEmitter<{regionId: Double, progress: Double}>
}

export default TurboModuleRegistry.getEnforcing<Spec>('RTNCacheModule');
