export default class CallbacksManager {
    static callbacks: {
        [id: string]: (...arg: any[]) => void;
    };
    static addCallback(callback: (...arg: any[]) => void): string;
    static call(id: string, ...args: any[]): void;
}
