import React from 'react';
import { Point } from '../interfaces';
export interface PolygonProps {
    fillColor?: string;
    strokeColor?: string;
    strokeWidth?: number;
    zIndex?: number;
    onPress?: () => void;
    points: Point[];
    innerRings?: (Point[])[];
    children?: undefined;
}
export declare class Polygon extends React.Component<PolygonProps> {
    static defaultProps: {
        innerRings: never[];
    };
    render(): React.JSX.Element;
}
