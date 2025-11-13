import { Box } from './Box';
import { Dim } from './Dim';
export declare function place(viewport: Dim, refBox: Box, elem: Dim): Box;
export declare function placeCombo(viewport: Dim, refBox: Box, elem: Dim): Box;
interface Placed1D {
    readonly offset: number;
    readonly len: number;
}
export declare function place1DEnd(viewportW: number, refX: number, refW: number, elemW: number): Placed1D;
export declare function place1DStart(viewportW: number, refX: number, refW: number, elemW: number): Placed1D;
export {};
