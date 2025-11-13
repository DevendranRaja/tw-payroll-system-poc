import { Maybe } from './Maybe';
export declare class ListWithSelection<T> {
    private readonly l1;
    private readonly l2;
    private readonly sel;
    private constructor();
    static fromArray<T>(a: ReadonlyArray<T>): ListWithSelection<T>;
    static empty<T>(): ListWithSelection<T>;
    toArray(): ReadonlyArray<T>;
    select(f: (item: T, index: number, arr: ReadonlyArray<T>) => boolean): ListWithSelection<T>;
    selectIndex(index: number): ListWithSelection<T>;
    isSelected(item: T): boolean;
    getSelected(): Maybe<T>;
    getSelectedIndex(): Maybe<number>;
    length(): number;
}
