import React from 'react';
import {requireNativeComponent} from 'react-native';
import {processColorProps} from '../utils';

const NativePolyline = requireNativeComponent('YamapPolyline');

export default class Polyline extends React.Component {
  render() {
    const props = {...this.props};
    processColorProps(props, 'fillColor');
    processColorProps(props, 'strokeColor');
    processColorProps(props, 'outlineColor');
    return <NativePolyline {...props} />;
  }
}
