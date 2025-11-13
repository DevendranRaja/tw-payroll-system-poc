export declare class Dim {
    readonly w: number;
    readonly h: number;
    constructor(w: number, h: number);
    static zero: Dim;
}
export declare function dim(w: number, h?: number): Dim;
