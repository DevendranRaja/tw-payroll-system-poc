/**
 * Helper for managing tuples.
 */
export declare class Tuple<A, B> {
    readonly a: A;
    readonly b: B;
    constructor(a: A, b: B);
    /**
     * Transform to a native tuple (array-like)
     */
    toNative(): [A, B];
    mapFirst<C>(f: (a: A) => C): Tuple<C, B>;
    mapSecond<C>(f: (b: B) => C): Tuple<A, C>;
    mapBoth<C, D>(f1: (a: A) => C, f2: (b: B) => D): Tuple<C, D>;
    static fromNative<A, B>(t: [A, B]): Tuple<A, B>;
    /**
     * Create a new tuple with passed values
     * @param a the first value
     * @param b the second value
     */
    static t2<A, B>(a: A, b: B): Tuple<A, B>;
    /**
     * Create a native tuple with passed values
     * @param a the first value
     * @param b the second value
     */
    static t2n<A, B>(a: A, b: B): [A, B];
}
