export type F<T, R> = (t: T) => R;
export declare function memoize<T, R>(f: F<T, R>, compareFn?: (o1: T, o2: T) => boolean): F<T, R>;
