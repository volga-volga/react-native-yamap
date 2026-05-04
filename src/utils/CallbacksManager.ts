import { guid } from './guid';

export class CallbacksManager {
  static callbacks: { [id: string]: (...arg: any[]) => void} = {};

  static addCallback(callback: (...arg: any[]) => void) {
    const id = guid();
    CallbacksManager.callbacks[id] = (...args) => {
      callback(...args);
      delete CallbacksManager.callbacks[id];
    };
    return id;
  }

  static call(id: string, ...args: any[]) {
    if (CallbacksManager.callbacks[id]) {
      CallbacksManager.callbacks[id](...args);
    }
  }
}
