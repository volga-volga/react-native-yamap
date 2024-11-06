import React from 'react';
import { Point } from '../interfaces';
export interface PolylineProps {
    strokeColor?: string;
    outlineColor?: string;
    strokeWidth?: number;
    outlineWidth?: number;
    dashLength?: number;
    dashOffset?: number;
    gapLength?: number;
    zIndex?: number;
    onPress?: () => void;
    points: Point[];
    children?: undefined;
    handled?: boolean;
}
export declare class Polyline extends React.Component<PolylineProps> {
    render(): React.JSX.Element;
}
