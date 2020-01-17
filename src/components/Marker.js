import React from 'react';
import {requireNativeComponent} from 'react-native';
import resolveAssetSource from 'react-native/Libraries/Image/resolveAssetSource';

const NativeMarker = requireNativeComponent('YamapMarker');

export default class Marker extends React.Component {
  resolveImageUri(img) {
    return img ? resolveAssetSource(img).uri : '';
  }

  render() {
    const props = {...this.props};
    return (
      <NativeMarker
        {...props}
        key={Boolean(this.props.children)}
        source={this.resolveImageUri(this.props.source)}
      />
    );
  }
}
