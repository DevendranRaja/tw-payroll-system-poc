import { Maybe } from './Maybe';
/**
 * Immutable list of data
 */
export declare class List<T> {
    private readonly elems;
    private constructor();
    static fromArray<T>(ts: Array<T>): List<T>;
    static empty<T>(): List<T>;
    head(): Maybe<T>;
    length(): number;
    tail(): List<T>;
    toArray(): Array<T>;
    map<T2>(f: (t: T) => T2): List<T2>;
    mapWithIndex<T2>(f: (t: T, i: number) => T2): List<T2>;
}
