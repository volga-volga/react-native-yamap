import React from 'react';
import { requireNativeComponent, NativeModules, PermissionsAndroid, InteractionManager } from 'react-native';
import resolveAssetSource from 'react-native/Libraries/Image/resolveAssetSource.js';

const { yamap } = NativeModules;

const YaMapNative = requireNativeComponent('YamapView');

export default class YaMap extends React.Component {
  static init(apiKey) {
    yamap.init(apiKey);
  }

  componentDidMount() {
    InteractionManager.runAfterInteractions(() => {
      PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION, {
        title: 'Запрос доступа',
        message: 'Приложению требуется доступ к геолокации',
      });
    });
  }

  prepareMarkers = () => {
    if (this.props.markers) {
      return this.props.markers.map((marker, index) => ({
        id: String(marker.id || index),
        zIndex: typeof marker.zIndex === 'number' ? marker.zIndex : 1,
        lon: marker.lon,
        lat: marker.lat,
        source: marker.source ? resolveAssetSource(marker.source).uri : '',
      }));
    }
    return undefined;
  };

  onMarkerPress = (e) => {
    if (this.props.onMarkerPress) {
      this.props.onMarkerPress(Number(e.nativeEvent.id));
    }
  };

  render() {
    const { showSelectedOnTop, ...props} = this.props;
    return (
      <YaMapNative
        {...props}
        markers={this.prepareMarkers()}
        onMarkerPress={this.onMarkerPress}
      />
    );
  }
}
