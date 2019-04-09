import * as React from 'react';
import { ViewProps } from 'react-native';

declare module 'react-native-yamap';

export interface Marker {
  id: number,
  lon: number,
  lat: number,
  selected: boolean,
}

interface Props extends ViewProps {
  onMarkerPress?: (id: string) => void,
  markers: Marker[],
  center: { lon: number, lat: number },
}

declare class YaMap extends React.Component<Props> {
  static init(apiKey: string): void;
}

export default YaMap;
