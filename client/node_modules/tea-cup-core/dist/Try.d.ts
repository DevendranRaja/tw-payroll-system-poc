import { Result } from './Result';
export declare function Try<T>(f: () => T): Result<Error, T>;
export declare function asError(e: unknown): Error;
