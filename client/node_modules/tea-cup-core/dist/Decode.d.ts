import { Result } from './Result';
import { Maybe } from './Maybe';
import { List } from './List';
/**
 * Decoder for a given type.
 */
export declare class Decoder<T> {
    private readonly f;
    constructor(f: (o: any) => Result<string, T>);
    /**
     * Attempt to decode passed JSON string into a T
     * @param s the string to decode
     */
    decodeString(s: string): Result<string, T>;
    /**
     * Attempt to decode a JS value into a T
     * @param o the value to decode
     */
    decodeValue(o: any): Result<string, T>;
}
/**
 * Decoding primitives and utilities.
 */
export declare class Decode {
    /**
     * string decoder
     */
    static str: Decoder<string>;
    /**
     * boolean decoder
     */
    static bool: Decoder<boolean>;
    /**
     * number decoder
     */
    static num: Decoder<number>;
    /**
     * Decoder for null/undefined values
     * @param d the decoder to be used if the value is not null or undefined
     */
    static nullable<T>(d: Decoder<T>): Decoder<Maybe<T>>;
    /**
     * Decoder for nullable types
     * @param d the decoder to be used if the value is not null
     */
    static orNull<T>(d: Decoder<T>): Decoder<T | null>;
    /**
     * Decoder for lists
     * @param d the decoder for elements in the list
     */
    static list<T>(d: Decoder<T>): Decoder<List<T>>;
    /**
     * Decoder for arrays
     * @param d the decoder for elements in the array
     */
    static array<T>(d: Decoder<T>): Decoder<Array<T>>;
    /**
     * Decoder for object fields
     * @param key the name of the field
     * @param d the decoder for the field's value
     */
    static field<T>(key: string, d: Decoder<T>): Decoder<T>;
    /**
     * Decoder for optinal object fields
     * @param key the name of the optinal field
     * @param d the decoder for the field's value
     */
    static optionalField<T>(key: string, d: Decoder<T>): Decoder<T | undefined>;
    /**
     * Decoder for navigable object properties
     * @param keys a list of fields to navigate
     * @param d the decoder for the leaf value
     */
    static at<T>(keys: ReadonlyArray<string>, d: Decoder<T>): Decoder<T>;
    /**
     * Decoder for optional values : turns decoding failures into maybes.
     * @param d the decoder to be used
     */
    static maybe<T>(d: Decoder<T>): Decoder<Maybe<T>>;
    /**
     * Tries passed decoders sequentially, and fails if no decoder succeeds
     * @param ds an array of Decoders to try, one after the othen
     */
    static oneOf<T>(ds: ReadonlyArray<Decoder<T>>): Decoder<T>;
    /**
     * Map a decoder
     * @param f the mapping function
     * @param d the decoder to use
     */
    static map<T1, T2>(f: (t1: T1) => T2, d: Decoder<T1>): Decoder<T2>;
    static map2<T1, T2, T3>(f: (t1: T1, t2: T2) => T3, d1: Decoder<T1>, d2: Decoder<T2>): Decoder<T3>;
    static map3<T1, T2, T3, T4>(f: (t1: T1, t2: T2, t3: T3) => T4, d1: Decoder<T1>, d2: Decoder<T2>, d3: Decoder<T3>): Decoder<T4>;
    static map4<T1, T2, T3, T4, T5>(f: (t1: T1, t2: T2, t3: T3, t4: T4) => T5, d1: Decoder<T1>, d2: Decoder<T2>, d3: Decoder<T3>, d4: Decoder<T4>): Decoder<T5>;
    static map5<T1, T2, T3, T4, T5, T6>(f: (t1: T1, t2: T2, t3: T3, t4: T4, t5: T5) => T6, d1: Decoder<T1>, d2: Decoder<T2>, d3: Decoder<T3>, d4: Decoder<T4>, d5: Decoder<T5>): Decoder<T6>;
    static map6<T1, T2, T3, T4, T5, T6, T7>(f: (t1: T1, t2: T2, t3: T3, t4: T4, t5: T5, t6: T6) => T7, d1: Decoder<T1>, d2: Decoder<T2>, d3: Decoder<T3>, d4: Decoder<T4>, d5: Decoder<T5>, d6: Decoder<T6>): Decoder<T7>;
    static map7<T1, T2, T3, T4, T5, T6, T7, T8>(f: (t1: T1, t2: T2, t3: T3, t4: T4, t5: T5, t6: T6, t7: T7) => T8, d1: Decoder<T1>, d2: Decoder<T2>, d3: Decoder<T3>, d4: Decoder<T4>, d5: Decoder<T5>, d6: Decoder<T6>, d7: Decoder<T7>): Decoder<T8>;
    static map8<T1, T2, T3, T4, T5, T6, T7, T8, T9>(f: (t1: T1, t2: T2, t3: T3, t4: T4, t5: T5, t6: T6, t7: T7, t8: T8) => T9, d1: Decoder<T1>, d2: Decoder<T2>, d3: Decoder<T3>, d4: Decoder<T4>, d5: Decoder<T5>, d6: Decoder<T6>, d7: Decoder<T7>, d8: Decoder<T8>): Decoder<T9>;
    /**
     * Decoder for big objects, where map8() is not enough.
     * @param dobject an object with decoders
     */
    static mapObject<T>(dobject: DecoderObject<T>): Decoder<T>;
    /**
     * Convenience, map decoder object to another decoder object
     * @param decoders an object with decoders
     * @param fun the mapper function
     */
    static mapFields<T, T2>(decoders: DecoderObject<T>, fun: DecoderObjectMapper<T, T2>): DecoderObject<T2>;
    /**
     * Convenience, map docoders to required field decoders
     * @param decoders an object with decoders
     */
    static mapRequiredFields<T>(decoders: DecoderObject<T>): DecoderObject<T>;
    /**
     * Convenience, map decoders to optional field decoders
     * @param decoders an object with decoders
     */
    static mapOptionalFields<T>(decoders: DecoderObject<T>): DecoderObject<OptionalFields<T>>;
    /**
     * Decoder for fixed-length tuples.
     * @param decoders an array with decoders
     */
    static mapTuple<T extends any[]>(decoders: DecoderArray<T>): Decoder<T>;
    /**
     * Decoder for recursive data structures
     * @param f a no-arg function that yields a decoder
     */
    static lazy<T>(f: () => Decoder<T>): Decoder<T>;
    /**
     * Decoder for any value
     */
    static value: Decoder<any>;
    /**
     * Decoder for null
     * @param t the result to yield in case the decoded value is null
     */
    static null<T>(t: T): Decoder<T>;
    /**
     * Decoder that always succeed
     * @param t the value to yield
     */
    static succeed<T>(t: T): Decoder<T>;
    /**
     * Decoder that fails
     * @param msg the message to use in the resulting Err
     */
    static fail<T>(msg: string): Decoder<T>;
    /**
     * Chain decoders
     * @param f the function to apply if the first decoder has succeeded
     * @param d the first decoder to use
     */
    static andThen<T1, T2>(f: (t1: T1) => Decoder<T2>, d: Decoder<T1>): Decoder<T2>;
}
export type DecoderObject<T> = Required<{
    [P in keyof T]: Decoder<T[P]>;
}>;
export type DecoderArray<A extends any[]> = Required<{
    [P in keyof A]: A[P] extends A[number] ? Decoder<A[P]> : never;
}>;
export type OptionalFields<T> = {
    [P in keyof T]: T[P] | undefined;
};
export type DecoderObjectMapper<T, T2> = (k: keyof T, d: Decoder<T[keyof T]>) => [keyof T2, Decoder<T2[keyof T2]>];
