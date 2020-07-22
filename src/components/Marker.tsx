import React from 'react';
import { requireNativeComponent, Platform, ImageSourcePropType } from 'react-native';
// @ts-ignore
import resolveAssetSource from 'react-native/Libraries/Image/resolveAssetSource';
import { Point } from '../interfaces';

export interface MarkerProps {
  children?: React.ReactElement;
  zIndex?: number;
  scale?: number;
  onPress?: () => void;
  point: Point;
  source?: ImageSourcePropType;
  anchor?: { x: number, y: number };
}

const NativeMarkerComponent = requireNativeComponent<MarkerProps & { pointerEvents: 'none' }>('YamapMarker');

interface State {
  recreateKey: boolean;
  children: any;
}

export class Marker extends React.Component<MarkerProps, State> {
  state = {
    recreateKey: false,
    children: this.props.children,
  };

  static getDerivedStateFromProps(nextProps: MarkerProps, prevState: State): Partial<State> {
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

  private resolveImageUri(img?: ImageSourcePropType) {
    return img ? resolveAssetSource(img).uri : '';
  }

  private getProps() {
    return {
      ...this.props,
      source: this.resolveImageUri(this.props.source),
    };
  }

  render() {
    return (
      <NativeMarkerComponent
        {...this.getProps()}
        key={String(this.state.recreateKey)}
        pointerEvents='none'
      />
    );
  }
}
