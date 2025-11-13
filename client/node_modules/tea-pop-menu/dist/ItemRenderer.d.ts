import * as React from 'react';
export interface RendererInput<T> {
    readonly data: T;
    readonly active: boolean;
    readonly hasSubMenu: boolean;
}
export declare type ItemRenderer<T> = (input: RendererInput<T>) => React.ReactNode;
export declare function defaultItemRenderer<T>(contentRenderer: (t: T) => React.ReactNode): ItemRenderer<T>;
