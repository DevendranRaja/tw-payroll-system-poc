import { Task } from './Task';
/**
 * Generate Rantom numbers.
 */
export declare class Random {
    /**
     * Generate a random int between lo and hi
     * @param lo
     * @param hi
     */
    static fromIntervalInclusive(lo: number, hi: number): Task<never, number>;
}
