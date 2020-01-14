import React from 'react';
import {requireNativeComponent} from 'react-native';
import {processColorProps} from '../utils';

const NativePolygon = requireNativeComponent('YamapPolygon');

export default class Polygon extends React.Component {
  render() {
    const props = {...this.props};
    processColorProps(props, 'fillColor');
    processColorProps(props, 'strokeColor');
    return <NativePolygon {...props} />;
  }
}
