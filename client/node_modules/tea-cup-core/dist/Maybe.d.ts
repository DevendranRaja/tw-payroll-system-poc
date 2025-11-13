/**
 * Maybe is either "just something", ot "nothing" !
 */
export type Maybe<T> = Just<T> | Nothing<T>;
/**
 * A Maybe with something inside
 */
export declare class Just<T> {
    readonly type: 'Just';
    readonly value: T;
    constructor(value: T);
    map<T2>(f: (t: T) => T2): Maybe<T2>;
    withDefault(_: T): T;
    withDefaultSupply(f: () => T): T;
    orElse(e: Maybe<T>): Maybe<T>;
    orElseSupply(f: () => Maybe<T>): Maybe<T>;
    forEach(f: (t: T) => void): void;
    andThen<T2>(f: (t: T) => Maybe<T2>): Maybe<T2>;
    toNative(): T | undefined;
    isJust(): boolean;
    isNothing(): boolean;
    filter(f: (t: T) => boolean): Maybe<T>;
}
/**
 * Wrap an object into a Maybe
 * @param t the object to wrap
 */
export declare function just<T>(t: T): Maybe<T>;
/**
 * An "empty" Maybe
 */
export declare class Nothing<T> {
    readonly type: 'Nothing';
    static value: Nothing<never>;
    private constructor();
    map<T2>(f: (t: T) => T2): Maybe<T2>;
    withDefault(d: T): T;
    withDefaultSupply(f: () => T): T;
    orElse(e: Maybe<T>): Maybe<T>;
    orElseSupply(f: () => Maybe<T>): Maybe<T>;
    forEach(f: (t: T) => void): void;
    andThen<T2>(f: (t: T) => Maybe<T2>): Maybe<T2>;
    toNative(): T | undefined;
    isJust(): boolean;
    isNothing(): boolean;
    filter(): Maybe<T>;
}
/**
 * The "nothing" maybe.
 */
export declare const nothing: Maybe<never>;
/**
 * Make a Maybe with a "nullable" object
 * @param t the object, or undef, or null
 */
export declare function maybeOf<T>(t: T | undefined | null): Maybe<T>;
/**
 * Apply a function if all the arguments are `Just` a value.
 * @param m1 First Maybe
 * @param m2 Second Maybe
 * @param func the function applied on the values of the Maybes
 */
export declare function map2<T1, T2, T3>(m1: Maybe<T1>, m2: Maybe<T2>, func: (a: T1, b: T2) => T3): Maybe<T3>;
