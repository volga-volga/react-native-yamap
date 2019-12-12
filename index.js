import React from 'react';
import { requireNativeComponent, NativeModules, PermissionsAndroid, InteractionManager } from 'react-native';

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
      return this.props.markers.map(marker => ({
        id: String(marker.id),
        lon: marker.lon,
        lat: marker.lat,
        selected: Boolean(marker.selected),
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
    return (
      <YaMapNative
        {...this.props}
        markers={this.prepareMarkers()}
        onMarkerPress={this.onMarkerPress}
      />
    );
  }
}
