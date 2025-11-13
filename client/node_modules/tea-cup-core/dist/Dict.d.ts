import { Tuple } from './Tuple';
import { Maybe } from './Maybe';
export declare class Dict<T> {
    private readonly d;
    private constructor();
    static empty<T>(): Dict<T>;
    static fromList<T>(entries: readonly Tuple<string, T>[]): Dict<T>;
    put(key: string, value: T): Dict<T>;
    get(key: string): Maybe<T>;
    exists(key: string): boolean;
    remove(key: string): Dict<T>;
    keys(): readonly string[];
    size(): number;
    isEmpty(): boolean;
    toList(): readonly Tuple<string, T>[];
    filter(f: (t: Tuple<string, T>) => boolean): Dict<T>;
    update(key: string, f: (v: Maybe<T>) => Maybe<T>): Dict<T>;
}
