import React from 'react';
import { requireNativeComponent, Platform, ImageSourcePropType, UIManager, findNodeHandle } from 'react-native';
// @ts-ignore
import resolveAssetSource from 'react-native/Libraries/Image/resolveAssetSource';
import { Point } from '../interfaces';

export interface MarkerProps {
  children?: React.ReactElement;
  zIndex?: number;
  scale?: number;
  rotated?: boolean;
  onPress?: () => void;
  point: Point;
  source?: ImageSourcePropType;
  anchor?: { x: number, y: number };
  visible?: boolean;
  handled?: boolean;
}

const NativeMarkerComponent = requireNativeComponent<MarkerProps & { pointerEvents: 'none' }>('YamapMarker');

interface State {
  recreateKey: boolean;
  children: any;
}

export class Marker extends React.Component<MarkerProps, State> {
  static defaultProps = {
    rotated: false,
  };

  state = {
    recreateKey: false,
    children: this.props.children
  };

  private getCommand(cmd: string): any {
    if (Platform.OS === 'ios') {
      return UIManager.getViewManagerConfig('YamapMarker').Commands[cmd];
    } else {
      return cmd;
    }
  }

  static getDerivedStateFromProps(nextProps: MarkerProps, prevState: State): Partial<State> {
    if (Platform.OS === 'ios') {
      return {
        children: nextProps.children,
        recreateKey:
          nextProps.children === prevState.children
            ? prevState.recreateKey
            : !prevState.recreateKey
      };
    }

    return {
      children: nextProps.children,
      recreateKey: Boolean(nextProps.children)
    };
  }

  private resolveImageUri(img?: ImageSourcePropType) {
    return img ? resolveAssetSource(img).uri : '';
  }

  private getProps() {
    return {
      ...this.props,
      source: this.resolveImageUri(this.props.source)
    };
  }

  public animatedMoveTo(coords: Point, duration: number) {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this),
      this.getCommand('animatedMoveTo'),
      [coords, duration]
    );
  }

  public animatedRotateTo(angle: number, duration: number) {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this),
      this.getCommand('animatedRotateTo'),
      [angle, duration]
    );
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
