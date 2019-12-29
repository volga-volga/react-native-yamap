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

  prepareMarkers = () => {
    if (this.props.markers) {
      return this.props.markers.map((marker, index) => ({
        id: String(marker.id || index),
        zIndex: typeof marker.zIndex === 'number' ? marker.zIndex : 1,
        lon: marker.lon,
        lat: marker.lat,
        source: this.resolveImageUri(marker.source),
      }));
    }
    return undefined;
  };

  onMarkerPress = e => {
    if (this.props.onMarkerPress) {
      this.props.onMarkerPress(Number(e.nativeEvent.id));
    }
  };

  render() {
    const {showSelectedOnTop, ...props} = this.props;
    return (
      <YaMapNative
        {...props}
        ref={this.map}
        markers={this.prepareMarkers()}
        userLocationIcon={this.resolveImageUri(this.props.userLocationIcon)}
        onMarkerPress={this.onMarkerPress}
      />
    );
  }
}
