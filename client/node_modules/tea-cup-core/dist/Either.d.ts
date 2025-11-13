import { Maybe } from './Maybe';
/**
 * Either left, or right.
 */
export type Either<A, B> = Left<A, B> | Right<A, B>;
export declare class Left<A, B> {
    readonly tag = "Left";
    readonly value: A;
    constructor(value: A);
    isLeft(): boolean;
    isRight(): boolean;
    mapLeft<C>(f: (a: A) => C): Either<C, B>;
    mapRight<C>(f: (b: B) => C): Either<A, C>;
    match<R>(onLeft: (a: A) => R, onRight: (b: B) => R): R;
    get left(): Maybe<A>;
    get right(): Maybe<B>;
}
export declare class Right<A, B> {
    readonly tag = "Right";
    readonly value: B;
    constructor(value: B);
    isLeft(): boolean;
    isRight(): boolean;
    mapLeft<C>(f: (a: A) => C): Either<C, B>;
    mapRight<C>(f: (b: B) => C): Either<A, C>;
    match<R>(onLeft: (a: A) => R, onRight: (b: B) => R): R;
    get left(): Maybe<A>;
    get right(): Maybe<B>;
}
export declare function left<A, B>(a: A): Either<A, B>;
export declare function right<A, B>(b: B): Either<A, B>;
