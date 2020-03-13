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
    'trolleybus',
    'tramway',
    'minibus',
    'suburban',
    'underground',
    'ferry',
    'cable',
    'funicular',
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

  findRoutes(points, vehicles, cb) {
    const cbId = CallbacksManager.addCallback(cb);
    const args =
      Platform.OS === 'ios'
        ? [{points, vehicles, id: cbId}]
        : [points, vehicles, cbId];
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this),
      this.getCommand('findRoutes'),
      args,
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
        // todo: сделать один пропс на обе платформы
        onRoutesFound={this.processRoute}
        onRouteFound={this.processRoute}
        userLocationIcon={this.resolveImageUri(this.props.userLocationIcon)}
      />
    );
  }
}
