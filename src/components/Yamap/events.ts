import {type NativeSyntheticEvent} from "react-native";
import {CallbacksManager} from '../../utils';
import type {
  CameraPositionNativeResponse,
  VisibleRegionNativeResponse,
  ScreenPointsNativeResponse,
  WorldPointsNativeResponse
} from '../../spec/YamapNativeComponent';

export const onCameraPositionReceived = (event: NativeSyntheticEvent<CameraPositionNativeResponse>) => {
  const { id, ...point } = event.nativeEvent;
  CallbacksManager.call(id, point);
};

export const onVisibleRegionReceived = (event: NativeSyntheticEvent<VisibleRegionNativeResponse>) => {
  const { id, ...visibleRegion } = event.nativeEvent;
  CallbacksManager.call(id, visibleRegion);
};

export const onWorldToScreenPointsReceived = (event: NativeSyntheticEvent<ScreenPointsNativeResponse>) => {
  const { id, ...screenPoints } = event.nativeEvent;
  CallbacksManager.call(id, screenPoints);
};

export const onScreenToWorldPointsReceived = (event: NativeSyntheticEvent<WorldPointsNativeResponse>) => {
  const { id, ...worldPoints } = event.nativeEvent;
  CallbacksManager.call(id, worldPoints);
};
