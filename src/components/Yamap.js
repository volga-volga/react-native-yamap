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

const {yamap} = NativeModules;

const YaMapNative = requireNativeComponent('YamapView');

export default class YaMap extends React.Component {
  map = React.createRef();

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
      return UIManager.YamapView.Commands[cmd];
    }
  }

  drawRoute(route) {
    this.map.current.setNativeProps({route: route});
  }

  clearRoute() {
    this.drawRoute(null);
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
        userLocationIcon={this.resolveImageUri(this.props.userLocationIcon)}
      />
    );
  }
}
