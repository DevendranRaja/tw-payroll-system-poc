import { Task } from './Task';
/**
 * Generate a UUID. Side effect, use uuid() that returns a Task instead
 */
export declare function nextUuid(): string;
/**
 * Return a Task that generates a UUID
 */
export declare function uuid(): Task<never, string>;
