export declare interface Serializer<T> {
    serialize(data: T): ArrayBuffer
    deserialize(bytes: ArrayBuffer): T | undefined
}

export declare interface Listener<T> {
    onUpdate(data: T | undefined): void;
}

export declare interface Transform<T> {
    transform(input: T | undefined): T | undefined;
}

export declare type BismarckState =
    "Fresh"
    | "Stale"
    | "Fetching"
    | "Error";

export declare function jsonSerializer<T>(): Serializer<T>;

export declare class Bismarck<T> {
    public eachValue(action: (value: T | undefined) => void);
    public eachState(action: (value: BismarckState | undefined) => void);

    /**
     * Manually set the data of the bismarck.
     */
    public insert(data?: T): void;

    /**
     * Synchronously grab the latest cached version of the data.
     */
    public peek(): T | undefined;

    /**
     * Synchronously grab the latest version of the bismarck state.
     */
    public peekState(): BismarckState

    /**
     * The bismarck will usually employ some sort of timer or hash comparison to determine this.
     * Can also call [invalidate] to force this to false.
     */
    public isFresh(): boolean

    /**
     * Should cause [isFresh] to return false.
     */
    public invalidate(): void;

    /**
     * Trigger asyncFetch of this and all dependencies where [isFresh] is false.
     */
    public refresh(): void;

    /**
     * FIFO executed just after data insertion and before dependent invalidation
     */
    public addListener(listener: Listener<T>): Bismarck<T>;

    /**
     * Remove a previously added listener
     */
    public removeListener(listener: Listener<T>): Bismarck<T>;

    /**
     * FIFO executed just after data insertion and before dependent invalidation
     */
    public addTransform(transform: Transform<T>): Bismarck<T>;

    /**
     * Remove a previously added listener
     */
    public removeTransform(transform: Transform<T>): Bismarck<T>;

    /**
     * Dependency chaining. Does not detect circular references, so be careful.
     */
    public addDependent(other: Bismarck<unknown>): Bismarck<T>;

    /**
     * Dependency chaining. Does not detect circular references, so be careful.
     */
    public removeDependent(other: Bismarck<unknown>): Bismarck<T>;

    /**
     * Type-agnostic method for clearing data,
     * since logouts will often cause this to happen in a foreach loop.
     */
    public clear(): void;

    /**
     * Sometimes you do bad things and the data gets changed without an [insert] call. Shame on you.
     */
    public notifyChanged(): void;
}

export declare class DedupingBismarck<T> extends Bismarck<T> {}