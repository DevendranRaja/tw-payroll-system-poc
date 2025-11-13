export declare class Pos {
    readonly x: number;
    readonly y: number;
    constructor(x: number, y: number);
    add(p: Pos): Pos;
    static origin: Pos;
}
export declare function pos(x: number, y: number): Pos;
