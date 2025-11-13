import { Maybe } from './Maybe';
/**
 * A computation may result in a value (Ok), or an error (Err)
 */
export type Result<E, R> = Ok<E, R> | Err<E, R>;
export declare class Ok<E, R> {
    readonly tag: 'Ok';
    readonly value: R;
    constructor(value: R);
    map<R2>(f: (r: R) => R2): Result<E, R2>;
    mapError<E2>(f: (e: E) => E2): Result<E2, R>;
    toMaybe(): Maybe<R>;
    match<X>(onOk: (r: R) => X, onErr: (e: E) => X): X;
    withDefault(v: R): R;
    withDefaultSupply(f: () => R): R;
    andThen<R2>(f: (r: R) => Result<E, R2>): Result<E, R2>;
    orElse(f: (e: E) => Result<E, R>): Result<E, R>;
}
export declare class Err<E, R> {
    readonly tag: 'Err';
    readonly err: E;
    constructor(err: E);
    map<R2>(f: (r: R) => R2): Result<E, R2>;
    mapError<E2>(f: (e: E) => E2): Result<E2, R>;
    toMaybe(): Maybe<R>;
    match<X>(onOk: (r: R) => X, onErr: (e: E) => X): X;
    withDefault(r: R): R;
    withDefaultSupply(f: () => R): R;
    andThen<R2>(f: (r: R) => Result<E, R2>): Result<E, R2>;
    orElse(f: (e: E) => Result<E, R>): Result<E, R>;
}
/**
 * Create an Ok Result
 * @param r the value to wrap
 */
export declare function ok<E, R>(r: R): Result<E, R>;
/**
 * Create an Err result
 * @param e the error to wrap
 */
export declare function err<E, R>(e: E): Result<E, R>;
