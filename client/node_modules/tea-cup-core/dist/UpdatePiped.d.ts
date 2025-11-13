import { Cmd } from './Cmd';
export type UpdateFunction<Model, Msg> = GenericUpdateFunction<Model, Cmd<Msg>>;
/**
 * Piped updates.
 * Useful for chaining building blocks making up the update loop.
 */
export declare function updatePiped<Model, Msg>(model: Model, ...updates: readonly UpdateFunction<Model, Msg>[]): [Model, Cmd<Msg>];
export type GenericUpdateFunction<Model, Cmd> = (model: Model) => [Model, Cmd];
/**
 * Generic implementation of chained updates.
 * See updatePiped().
 */
export declare function genericUpdatePiped<Model, Cmd>(combine: (cmd1: Cmd, cmd2: Cmd) => Cmd, none: Cmd, model: Model, ...updates: readonly GenericUpdateFunction<Model, Cmd>[]): [Model, Cmd];
