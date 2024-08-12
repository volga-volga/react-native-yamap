import React from 'react';
import { Point } from '../interfaces';
export interface CircleProps {
    fillColor?: string;
    strokeColor?: string;
    strokeWidth?: number;
    zIndex?: number;
    onPress?: () => void;
    center: Point;
    radius: number;
    children?: undefined;
}
export declare class Circle extends React.Component<CircleProps> {
    static defaultProps: {};
    render(): React.JSX.Element;
}
