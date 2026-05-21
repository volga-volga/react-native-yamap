import NativeCacheModule from '../spec/NativeCacheModule';

export const Cache = {
  init: NativeCacheModule.initManager,
  searchRegions: NativeCacheModule.searchRegions,
  getRegionInfo: (regionId: number) => NativeCacheModule.getRegionInfo(regionId),
  startDownloadRegion: (regionId: number) => NativeCacheModule.startDownloadRegion(regionId),
  stopDownloadRegion: (regionId: number) => NativeCacheModule.stopDownloadRegion(regionId),
  pauseDownloadRegion: (regionId: number) => NativeCacheModule.pauseDownloadRegion(regionId),
  dropRegion: (regionId: number) => NativeCacheModule.dropRegion(regionId),
};
