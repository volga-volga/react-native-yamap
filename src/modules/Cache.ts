import NativeCacheModule from '../spec/NativeCacheModule';
import {NativeEventEmitter, Platform, type EmitterSubscription} from 'react-native';

const EVENT_REGION_STATE_CHANGED = 'cacheRegionStateChanged';
const EVENT_REGION_PROGRESS = 'cacheRegionProgress';

export interface CacheRegionStateChangedEvent {
  regionId: number,
  state: number,
}

export interface CacheRegionProgressEvent {
  regionId: number,
  progress: number,
}

const cacheEventsEmitter = new NativeEventEmitter(NativeCacheModule as any);

export const Cache = {
  init: NativeCacheModule.initManager,
  searchRegions: NativeCacheModule.searchRegions,
  getRegionInfo: (regionId: number) => NativeCacheModule.getRegionInfo(regionId),
  startDownloadRegion: (regionId: number) => NativeCacheModule.startDownloadRegion(regionId),
  stopDownloadRegion: (regionId: number) => NativeCacheModule.stopDownloadRegion(regionId),
  pauseDownloadRegion: (regionId: number) => NativeCacheModule.pauseDownloadRegion(regionId),
  dropRegion: (regionId: number) => NativeCacheModule.dropRegion(regionId),
  subscribeOnRegionStateChanged: (listener: (event: CacheRegionStateChangedEvent) => void) => {
    if (Platform.OS === 'ios' && typeof NativeCacheModule.onRegionStateChanged === 'function') {
      return NativeCacheModule.onRegionStateChanged(listener as never);
    }
    return cacheEventsEmitter.addListener(EVENT_REGION_STATE_CHANGED, listener);
  },
  subscribeOnRegionProgress: (listener: (event: CacheRegionProgressEvent) => void) => {
    if (Platform.OS === 'ios' && typeof NativeCacheModule.onRegionProgress === 'function') {
      return NativeCacheModule.onRegionProgress(listener as never);
    }
    return cacheEventsEmitter.addListener(EVENT_REGION_PROGRESS, listener);
  },
};
