import {guid} from './index';

export default class CallbacksManager {
  static callbacks = {};

  static addCallback(cb) {
    const id = guid();
    CallbacksManager.callbacks[id] = (...args) => {
      cb(...args);
      delete CallbacksManager.callbacks[id];
    };
    return id;
  }

  static call(id, ...args) {
    if (CallbacksManager.callbacks[id]) {
      CallbacksManager.callbacks[id](...args);
    }
  }
}
