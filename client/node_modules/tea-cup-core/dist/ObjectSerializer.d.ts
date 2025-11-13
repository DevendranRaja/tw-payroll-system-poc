export declare class ObjectSerializer {
    private readonly prototypesMap;
    private readonly discriminantField;
    private constructor();
    static withTeaCupClasses(): ObjectSerializer;
    static withClasses(classes: any[]): ObjectSerializer;
    addClasses(classes: any[]): ObjectSerializer;
    deserialize(s: string): any;
    serialize(obj: any): string;
    private applyTransformations;
}
