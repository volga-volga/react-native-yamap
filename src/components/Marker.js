import React from 'react';
import {requireNativeComponent, Platform} from 'react-native';
import resolveAssetSource from 'react-native/Libraries/Image/resolveAssetSource';

const NativeMarker = requireNativeComponent('YamapMarker');

export default class Marker extends React.Component {
  state = {
    recreateKey: false,
    children: this.props.children,
  };

  static getDerivedStateFromProps(nextProps, prevState) {
    if (Platform.OS === 'ios') {
      return {
        children: nextProps.children,
        recreateKey:
          nextProps.children === prevState.children
            ? prevState.recreateKey
            : !prevState.recreateKey,
      };
    }
    return {
      children: nextProps.children,
      recreateKey: Boolean(nextProps.children),
    };
  }

  resolveImageUri(img) {
    return img ? resolveAssetSource(img).uri : '';
  }

  render() {
    const props = {...this.props};
    alert(this.resolveImageUri(this.props.source));
    return (
      <NativeMarker
        {...props}
        key={this.state.recreateKey}
        source={this.resolveImageUri(this.props.source)}
      />
    );
  }
}
