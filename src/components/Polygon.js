import React from 'react';
import {requireNativeComponent, processColor} from 'react-native';

const NativePolygon = requireNativeComponent('YamapPolygon');

export default class Polygon extends React.Component {
  processColorProps(props, name) {
    if (props[name]) {
      props[name] = processColor(props[name]);
    }
  }

  getChildren() {
    if (this.props.children) {
      if (Array.isArray(this.props.children)) {
        return this.props.children.filter(child => child);
      }
      return this.props.children;
    }
    return undefined;
  }

  render() {
    const props = {...this.props};
    this.processColorProps(props, 'fillColor');
    this.processColorProps(props, 'strokeColor');
    return (
      <NativePolygon {...props}>
        {this.getChildren()}
      </NativePolygon>
    );
  }
}
