import React from 'react';
import {
  Platform,
  requireNativeComponent,
  NativeModules,
  PermissionsAndroid,
  InteractionManager,
  UIManager,
  findNodeHandle,
} from 'react-native';
import resolveAssetSource from 'react-native/Libraries/Image/resolveAssetSource.js';
import CallbacksManager from '../utils/CallbacksManager';

const {yamap} = NativeModules;

const YaMapNative = requireNativeComponent('YamapView');

export default class YaMap extends React.Component {
  map = React.createRef();

  static ALL_VEHICLES = [
    'bus',
    'railway',
    'tramway',
    'suburban',
    'underground',
    'trolleybus',
  ];

  static init(apiKey) {
    yamap.init(apiKey);
  }

  componentDidMount() {
    // todo: вынести в пропсы
    InteractionManager.runAfterInteractions(() => {
      PermissionsAndroid.request(
        PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
        {
          title: 'Запрос доступа',
          message: 'Приложению требуется доступ к геолокации',
        },
      );
    });
  }

  getCommand(cmd) {
    if (Platform.OS === 'ios') {
      return UIManager.getViewManagerConfig('YamapView').Commands[cmd];
    } else {
      return cmd;
    }
  }

  // drawRoute(routeId) {
  //   UIManager.dispatchViewManagerCommand(
  //     findNodeHandle(this),
  //     this.getCommand('drawRoute'),
  //     [routeId],
  //   );
  // }
  //
  // removeRoute(routeId) {
  //   UIManager.dispatchViewManagerCommand(
  //     findNodeHandle(this),
  //     this.getCommand('removeRoute'),
  //     [routeId],
  //   );
  // }

  findRoutes(points, vehicles, cb) {
    const cbId = CallbacksManager.addCallback(cb);
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this),
      this.getCommand('findRoutes'),
      [points, vehicles, cbId],
    );
  }

  processRoute(event) {
    CallbacksManager.call(event.nativeEvent.id, event);
  }

  fitAllMarkers() {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this),
      this.getCommand('fitAllMarkers'),
      [],
    );
  }

  setCenter(center) {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this),
      this.getCommand('setCenter'),
      [center],
    );
  }

  resolveImageUri(img) {
    return img ? resolveAssetSource(img).uri : '';
  }

  render() {
    return (
      <YaMapNative
        {...this.props}
        ref={this.map}
        onRoutesFound={this.processRoute}
        userLocationIcon={this.resolveImageUri(this.props.userLocationIcon)}
      />
    );
  }
}
