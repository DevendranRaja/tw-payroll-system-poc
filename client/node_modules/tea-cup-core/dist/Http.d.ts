import { Task } from './Task';
import { Decoder } from './Decode';
/**
 * Turns JS's fetch into Tasks.
 */
export declare class Http {
    /**
     * Create a task over the native fetch() function.
     * @param request the request
     * @param init the request init
     */
    static fetch(request: RequestInfo, init?: RequestInit): Task<Error, Response>;
    /**
     * Helper for JSON responses : uses passed decoder to convert a
     * JSON response
     * @param t the fetch task
     * @param d the decoder
     */
    static jsonBody<R>(t: Task<Error, Response>, d: Decoder<R>): Task<Error, R>;
    /**
     * Helper for string responses.
     * @param t the fetch task
     */
    static stringBody(t: Task<Error, Response>): Task<Error, string>;
    /**
     * Helper for turning a response that is not ok into an error
     * @param t the response
     * @param f a function that maps the Response to a type
     */
    static ifOk<R>(t: Task<Error, Response>, f: (r: Response) => Task<Error, R>): Task<Error, R>;
}
