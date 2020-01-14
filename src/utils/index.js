import {processColor} from 'react-native';

export function processColorProps(props, name) {
  if (props[name]) {
    props[name] = processColor(props[name]);
  }
}
