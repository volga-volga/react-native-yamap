import {Image, type ImageSourcePropType, processColor} from 'react-native';

export const getImageUri = (source: ImageSourcePropType | undefined) =>
  source ? Image.resolveAssetSource(source).uri : undefined;

export const processColorsToNative = <T extends { [key: string]: any }>(props: T, colorProps: Array<keyof T>): any =>
  Object.fromEntries(
    Object.entries(props)
      .map(([name, value]) => [
        name,
        colorProps.includes(name)
          ? processColor(value)
          : value,
      ])
  );
