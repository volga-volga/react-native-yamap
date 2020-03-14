import {processColor} from 'react-native';

export function processColorProps(props, name) {
  if (props[name]) {
    props[name] = processColor(props[name]);
  }
}

export function guid() {
  function s4() {
    return Math.floor((1 + Math.random()) * 0x10000)
      .toString(16)
      .substring(1);
  }
  return `${s4()}${s4()}-${s4()}-${s4()}-${s4()}-${s4()}${s4()}${s4()}`;
}
