#import <Foundation/NSArray.h>
#import <Foundation/NSDictionary.h>
#import <Foundation/NSError.h>
#import <Foundation/NSObject.h>
#import <Foundation/NSSet.h>
#import <Foundation/NSString.h>
#import <Foundation/NSValue.h>

@class BismarckBismarckState, BismarckKotlinEnum<E>, BismarckKotlinThrowable, BismarckKotlinArray<T>, BismarckKotlinException, BismarckKotlinByteArray, BismarckBaseBismarck<T>, BismarckFetch<T>, BismarckKotlinByteIterator;

@protocol BismarckBismarck, BismarckListener, BismarckTransform, BismarckCloseable, BismarckKotlinComparable, BismarckPersister, BismarckKotlinx_coroutines_coreCoroutineScope, BismarckFetcher, BismarckRateLimiter, BismarckKotlinSuspendFunction0, BismarckKotlinIterator, BismarckKotlinCoroutineContext, BismarckKotlinFunction, BismarckKotlinCoroutineContextElement, BismarckKotlinCoroutineContextKey;

NS_ASSUME_NONNULL_BEGIN
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunknown-warning-option"
#pragma clang diagnostic ignored "-Wnullability"

@interface KotlinBase : NSObject
- (instancetype)init __attribute__((unavailable));
+ (instancetype)new __attribute__((unavailable));
+ (void)initialize __attribute__((objc_requires_super));
@end;

@interface KotlinBase (KotlinBaseCopying) <NSCopying>
@end;

__attribute__((objc_runtime_name("KotlinMutableSet")))
__attribute__((swift_name("KotlinMutableSet")))
@interface BismarckMutableSet<ObjectType> : NSMutableSet<ObjectType>
@end;

__attribute__((objc_runtime_name("KotlinMutableDictionary")))
__attribute__((swift_name("KotlinMutableDictionary")))
@interface BismarckMutableDictionary<KeyType, ObjectType> : NSMutableDictionary<KeyType, ObjectType>
@end;

@interface NSError (NSErrorKotlinException)
@property (readonly) id _Nullable kotlinException;
@end;

__attribute__((objc_runtime_name("KotlinNumber")))
__attribute__((swift_name("KotlinNumber")))
@interface BismarckNumber : NSNumber
- (instancetype)initWithChar:(char)value __attribute__((unavailable));
- (instancetype)initWithUnsignedChar:(unsigned char)value __attribute__((unavailable));
- (instancetype)initWithShort:(short)value __attribute__((unavailable));
- (instancetype)initWithUnsignedShort:(unsigned short)value __attribute__((unavailable));
- (instancetype)initWithInt:(int)value __attribute__((unavailable));
- (instancetype)initWithUnsignedInt:(unsigned int)value __attribute__((unavailable));
- (instancetype)initWithLong:(long)value __attribute__((unavailable));
- (instancetype)initWithUnsignedLong:(unsigned long)value __attribute__((unavailable));
- (instancetype)initWithLongLong:(long long)value __attribute__((unavailable));
- (instancetype)initWithUnsignedLongLong:(unsigned long long)value __attribute__((unavailable));
- (instancetype)initWithFloat:(float)value __attribute__((unavailable));
- (instancetype)initWithDouble:(double)value __attribute__((unavailable));
- (instancetype)initWithBool:(BOOL)value __attribute__((unavailable));
- (instancetype)initWithInteger:(NSInteger)value __attribute__((unavailable));
- (instancetype)initWithUnsignedInteger:(NSUInteger)value __attribute__((unavailable));
+ (instancetype)numberWithChar:(char)value __attribute__((unavailable));
+ (instancetype)numberWithUnsignedChar:(unsigned char)value __attribute__((unavailable));
+ (instancetype)numberWithShort:(short)value __attribute__((unavailable));
+ (instancetype)numberWithUnsignedShort:(unsigned short)value __attribute__((unavailable));
+ (instancetype)numberWithInt:(int)value __attribute__((unavailable));
+ (instancetype)numberWithUnsignedInt:(unsigned int)value __attribute__((unavailable));
+ (instancetype)numberWithLong:(long)value __attribute__((unavailable));
+ (instancetype)numberWithUnsignedLong:(unsigned long)value __attribute__((unavailable));
+ (instancetype)numberWithLongLong:(long long)value __attribute__((unavailable));
+ (instancetype)numberWithUnsignedLongLong:(unsigned long long)value __attribute__((unavailable));
+ (instancetype)numberWithFloat:(float)value __attribute__((unavailable));
+ (instancetype)numberWithDouble:(double)value __attribute__((unavailable));
+ (instancetype)numberWithBool:(BOOL)value __attribute__((unavailable));
+ (instancetype)numberWithInteger:(NSInteger)value __attribute__((unavailable));
+ (instancetype)numberWithUnsignedInteger:(NSUInteger)value __attribute__((unavailable));
@end;

__attribute__((objc_runtime_name("KotlinByte")))
__attribute__((swift_name("KotlinByte")))
@interface BismarckByte : BismarckNumber
- (instancetype)initWithChar:(char)value;
+ (instancetype)numberWithChar:(char)value;
@end;

__attribute__((objc_runtime_name("KotlinUByte")))
__attribute__((swift_name("KotlinUByte")))
@interface BismarckUByte : BismarckNumber
- (instancetype)initWithUnsignedChar:(unsigned char)value;
+ (instancetype)numberWithUnsignedChar:(unsigned char)value;
@end;

__attribute__((objc_runtime_name("KotlinShort")))
__attribute__((swift_name("KotlinShort")))
@interface BismarckShort : BismarckNumber
- (instancetype)initWithShort:(short)value;
+ (instancetype)numberWithShort:(short)value;
@end;

__attribute__((objc_runtime_name("KotlinUShort")))
__attribute__((swift_name("KotlinUShort")))
@interface BismarckUShort : BismarckNumber
- (instancetype)initWithUnsignedShort:(unsigned short)value;
+ (instancetype)numberWithUnsignedShort:(unsigned short)value;
@end;

__attribute__((objc_runtime_name("KotlinInt")))
__attribute__((swift_name("KotlinInt")))
@interface BismarckInt : BismarckNumber
- (instancetype)initWithInt:(int)value;
+ (instancetype)numberWithInt:(int)value;
@end;

__attribute__((objc_runtime_name("KotlinUInt")))
__attribute__((swift_name("KotlinUInt")))
@interface BismarckUInt : BismarckNumber
- (instancetype)initWithUnsignedInt:(unsigned int)value;
+ (instancetype)numberWithUnsignedInt:(unsigned int)value;
@end;

__attribute__((objc_runtime_name("KotlinLong")))
__attribute__((swift_name("KotlinLong")))
@interface BismarckLong : BismarckNumber
- (instancetype)initWithLongLong:(long long)value;
+ (instancetype)numberWithLongLong:(long long)value;
@end;

__attribute__((objc_runtime_name("KotlinULong")))
__attribute__((swift_name("KotlinULong")))
@interface BismarckULong : BismarckNumber
- (instancetype)initWithUnsignedLongLong:(unsigned long long)value;
+ (instancetype)numberWithUnsignedLongLong:(unsigned long long)value;
@end;

__attribute__((objc_runtime_name("KotlinFloat")))
__attribute__((swift_name("KotlinFloat")))
@interface BismarckFloat : BismarckNumber
- (instancetype)initWithFloat:(float)value;
+ (instancetype)numberWithFloat:(float)value;
@end;

__attribute__((objc_runtime_name("KotlinDouble")))
__attribute__((swift_name("KotlinDouble")))
@interface BismarckDouble : BismarckNumber
- (instancetype)initWithDouble:(double)value;
+ (instancetype)numberWithDouble:(double)value;
@end;

__attribute__((objc_runtime_name("KotlinBoolean")))
__attribute__((swift_name("KotlinBoolean")))
@interface BismarckBoolean : BismarckNumber
- (instancetype)initWithBool:(BOOL)value;
+ (instancetype)numberWithBool:(BOOL)value;
@end;

__attribute__((swift_name("Closeable")))
@protocol BismarckCloseable
@required
- (BOOL)closeAndReturnError:(NSError * _Nullable * _Nullable)error __attribute__((swift_name("close()")));
@end;

__attribute__((swift_name("Bismarck")))
@protocol BismarckBismarck <BismarckCloseable>
@required
- (id<BismarckBismarck>)addDependentOther:(id<BismarckBismarck>)other __attribute__((swift_name("addDependent(other:)")));
- (id<BismarckBismarck>)addListenerListener:(id<BismarckListener>)listener __attribute__((swift_name("addListener(listener:)")));
- (id<BismarckBismarck>)addTransformTransform:(id<BismarckTransform>)transform __attribute__((swift_name("addTransform(transform:)")));
- (void)clear __attribute__((swift_name("clear()")));
- (void)consumeEachDataAction:(void (^)(id _Nullable))action __attribute__((swift_name("consumeEachData(action:)")));
- (void)consumeEachStateAction:(void (^)(BismarckBismarckState * _Nullable))action __attribute__((swift_name("consumeEachState(action:)")));
- (void)insertData:(id _Nullable)data __attribute__((swift_name("insert(data:)")));
- (void)invalidate __attribute__((swift_name("invalidate()")));
- (BOOL)isFresh __attribute__((swift_name("isFresh()")));
- (void)notifyChanged __attribute__((swift_name("notifyChanged()")));
- (id _Nullable)peek __attribute__((swift_name("peek()")));
- (BismarckBismarckState *)peekState __attribute__((swift_name("peekState()")));
- (void)refresh __attribute__((swift_name("refresh()")));
- (id<BismarckBismarck>)removeDependentOther:(id<BismarckBismarck>)other __attribute__((swift_name("removeDependent(other:)")));
- (id<BismarckBismarck>)removeListenerListener:(id<BismarckListener>)listener __attribute__((swift_name("removeListener(listener:)")));
- (id<BismarckBismarck>)removeTransformTransform:(id<BismarckTransform>)transform __attribute__((swift_name("removeTransform(transform:)")));
@end;

__attribute__((swift_name("KotlinComparable")))
@protocol BismarckKotlinComparable
@required
- (int32_t)compareToOther:(id _Nullable)other __attribute__((swift_name("compareTo(other:)")));
@end;

__attribute__((swift_name("KotlinEnum")))
@interface BismarckKotlinEnum<E> : KotlinBase <BismarckKotlinComparable>
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer));
- (int32_t)compareToOther:(E)other __attribute__((swift_name("compareTo(other:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString *name __attribute__((swift_name("name")));
@property (readonly) int32_t ordinal __attribute__((swift_name("ordinal")));
@end;

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("BismarckState")))
@interface BismarckBismarckState : BismarckKotlinEnum<BismarckBismarckState *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) BismarckBismarckState *fresh __attribute__((swift_name("fresh")));
@property (class, readonly) BismarckBismarckState *stale __attribute__((swift_name("stale")));
@property (class, readonly) BismarckBismarckState *fetching __attribute__((swift_name("fetching")));
@property (class, readonly) BismarckBismarckState *error __attribute__((swift_name("error")));
- (int32_t)compareToOther:(BismarckBismarckState *)other __attribute__((swift_name("compareTo(other:)")));
@end;

__attribute__((swift_name("Fetcher")))
@protocol BismarckFetcher
@required
@end;

__attribute__((swift_name("KotlinThrowable")))
@interface BismarckKotlinThrowable : KotlinBase
- (instancetype)initWithMessage:(NSString * _Nullable)message __attribute__((swift_name("init(message:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithCause:(BismarckKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(cause:)"))) __attribute__((objc_designated_initializer));
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (instancetype)initWithMessage:(NSString * _Nullable)message cause:(BismarckKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(message:cause:)"))) __attribute__((objc_designated_initializer));
- (BismarckKotlinArray<NSString *> *)getStackTrace __attribute__((swift_name("getStackTrace()")));
- (void)printStackTrace __attribute__((swift_name("printStackTrace()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) BismarckKotlinThrowable * _Nullable cause __attribute__((swift_name("cause")));
@property (readonly) NSString * _Nullable message __attribute__((swift_name("message")));
@end;

__attribute__((swift_name("KotlinException")))
@interface BismarckKotlinException : BismarckKotlinThrowable
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (instancetype)initWithMessage:(NSString * _Nullable)message __attribute__((swift_name("init(message:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithMessage:(NSString * _Nullable)message cause:(BismarckKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(message:cause:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithCause:(BismarckKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(cause:)"))) __attribute__((objc_designated_initializer));
@end;

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("FetcherBismarckFetchError")))
@interface BismarckFetcherBismarckFetchError : BismarckKotlinException
- (instancetype)initWithMessage:(NSString *)message cause:(BismarckKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(message:cause:)"))) __attribute__((objc_designated_initializer));
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
+ (instancetype)new __attribute__((unavailable));
- (instancetype)initWithMessage:(NSString * _Nullable)message __attribute__((swift_name("init(message:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
- (instancetype)initWithCause:(BismarckKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(cause:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@end;

__attribute__((swift_name("Listener")))
@protocol BismarckListener
@required
- (void)onUpdateData:(id _Nullable)data __attribute__((swift_name("onUpdate(data:)")));
@end;

__attribute__((swift_name("Persister")))
@protocol BismarckPersister
@required
- (id _Nullable)get __attribute__((swift_name("get()")));
- (void)putData:(id _Nullable)data __attribute__((swift_name("put(data:)")));
@end;

__attribute__((swift_name("RateLimiter")))
@protocol BismarckRateLimiter
@required
- (BOOL)isFresh __attribute__((swift_name("isFresh()")));
- (void)reset __attribute__((swift_name("reset()")));
- (void)update __attribute__((swift_name("update()")));
@property (readonly) int64_t lastReset __attribute__((swift_name("lastReset")));
@end;

__attribute__((swift_name("Serializer")))
@protocol BismarckSerializer
@required
- (id _Nullable)deserializeBytes:(BismarckKotlinByteArray *)bytes __attribute__((swift_name("deserialize(bytes:)")));
- (BismarckKotlinByteArray *)serializeData:(id)data __attribute__((swift_name("serialize(data:)")));
@end;

__attribute__((swift_name("StateListener")))
@protocol BismarckStateListener
@required
- (void)onStateChangedState:(BismarckBismarckState *)state __attribute__((swift_name("onStateChanged(state:)")));
@end;

__attribute__((swift_name("Transform")))
@protocol BismarckTransform
@required
- (id _Nullable)transformInput:(id _Nullable)input __attribute__((swift_name("transform(input:)")));
@end;

__attribute__((swift_name("MemoryPersister")))
@interface BismarckMemoryPersister<T> : KotlinBase <BismarckPersister>
- (instancetype)initWithCached:(T _Nullable)cached __attribute__((swift_name("init(cached:)"))) __attribute__((objc_designated_initializer));
- (T _Nullable)get __attribute__((swift_name("get()")));
- (void)putData:(T _Nullable)data __attribute__((swift_name("put(data:)")));
@property T _Nullable cached __attribute__((swift_name("cached")));
@end;

__attribute__((swift_name("BaseBismarck")))
@interface BismarckBaseBismarck<T> : KotlinBase <BismarckBismarck>
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (BismarckBaseBismarck<T> *)addDependentOther:(id<BismarckBismarck>)other __attribute__((swift_name("addDependent(other:)")));
- (BismarckBaseBismarck<T> *)addListenerListener:(id<BismarckListener>)listener __attribute__((swift_name("addListener(listener:)")));
- (BismarckBaseBismarck<T> *)addTransformTransform:(id<BismarckTransform>)transform __attribute__((swift_name("addTransform(transform:)")));
- (void)asyncFetch __attribute__((swift_name("asyncFetch()")));
- (BOOL)closeAndReturnError:(NSError * _Nullable * _Nullable)error __attribute__((swift_name("close()")));
- (void)consumeEachDataAction:(void (^)(T _Nullable))action __attribute__((swift_name("consumeEachData(action:)")));
- (void)consumeEachStateAction:(void (^)(BismarckBismarckState * _Nullable))action __attribute__((swift_name("consumeEachState(action:)")));
- (BismarckBaseBismarck<T> *)coroutineScopeCoroutineScope:(id<BismarckKotlinx_coroutines_coreCoroutineScope>)coroutineScope __attribute__((swift_name("coroutineScope(coroutineScope:)")));
- (BismarckBaseBismarck<T> *)fetcherFetcher:(id<BismarckFetcher> _Nullable)fetcher __attribute__((swift_name("fetcher(fetcher:)")));
- (void)insertData:(T _Nullable)data __attribute__((swift_name("insert(data:)")));
- (void)invalidate __attribute__((swift_name("invalidate()")));
- (BOOL)isFresh __attribute__((swift_name("isFresh()")));
- (void)notifyChanged __attribute__((swift_name("notifyChanged()")));
- (void)onFetchBeginFetch:(BismarckFetch<T> *)fetch __attribute__((swift_name("onFetchBegin(fetch:)")));
- (BOOL)onFetchEndFetch:(BismarckFetch<T> *)fetch __attribute__((swift_name("onFetchEnd(fetch:)")));
- (void)onFetchErrorFetch:(BismarckFetch<T> *)fetch __attribute__((swift_name("onFetchError(fetch:)")));
- (T _Nullable)peek __attribute__((swift_name("peek()")));
- (BismarckBismarckState *)peekState __attribute__((swift_name("peekState()")));
- (BismarckBaseBismarck<T> *)persisterPersister:(id<BismarckPersister> _Nullable)persister __attribute__((swift_name("persister(persister:)")));
- (BismarckBaseBismarck<T> *)rateLimiterRateLimiter:(id<BismarckRateLimiter> _Nullable)rateLimiter __attribute__((swift_name("rateLimiter(rateLimiter:)")));
- (void)refresh __attribute__((swift_name("refresh()")));
- (void)releaseFetch __attribute__((swift_name("releaseFetch()")));
- (BismarckBaseBismarck<T> *)removeDependentOther:(id<BismarckBismarck>)other __attribute__((swift_name("removeDependent(other:)")));
- (BismarckBaseBismarck<T> *)removeListenerListener:(id<BismarckListener>)listener __attribute__((swift_name("removeListener(listener:)")));
- (BismarckBaseBismarck<T> *)removeTransformTransform:(id<BismarckTransform>)transform __attribute__((swift_name("removeTransform(transform:)")));
- (void)requestFetch __attribute__((swift_name("requestFetch()")));
@property (readonly) id<BismarckKotlinx_coroutines_coreCoroutineScope> coroutineScope __attribute__((swift_name("coroutineScope")));
@property (readonly) id<BismarckFetcher> _Nullable fetcher __attribute__((swift_name("fetcher")));
@property (readonly) id<BismarckPersister> _Nullable persister __attribute__((swift_name("persister")));
@property (readonly) id<BismarckRateLimiter> _Nullable rateLimiter __attribute__((swift_name("rateLimiter")));
@end;

__attribute__((swift_name("DedupingBismarck")))
@interface BismarckDedupingBismarck<T> : BismarckBaseBismarck<T>
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
@end;

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Fetch")))
@interface BismarckFetch<T> : KotlinBase
- (instancetype)initWithCreated:(int64_t)created userData:(BismarckMutableDictionary<NSString *, NSString *> *)userData finished:(BismarckLong * _Nullable)finished data:(T _Nullable)data error:(BismarckKotlinException * _Nullable)error __attribute__((swift_name("init(created:userData:finished:data:error:)"))) __attribute__((objc_designated_initializer));
- (int64_t)component1 __attribute__((swift_name("component1()")));
- (BismarckMutableDictionary<NSString *, NSString *> *)component2 __attribute__((swift_name("component2()")));
- (BismarckLong * _Nullable)component3 __attribute__((swift_name("component3()")));
- (T _Nullable)component4 __attribute__((swift_name("component4()")));
- (BismarckKotlinException * _Nullable)component5 __attribute__((swift_name("component5()")));
- (BismarckFetch<T> *)doCopyCreated:(int64_t)created userData:(BismarckMutableDictionary<NSString *, NSString *> *)userData finished:(BismarckLong * _Nullable)finished data:(T _Nullable)data error:(BismarckKotlinException * _Nullable)error __attribute__((swift_name("doCopy(created:userData:finished:data:error:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) int64_t created __attribute__((swift_name("created")));
@property (readonly) T _Nullable data __attribute__((swift_name("data")));
@property (readonly) BismarckKotlinException * _Nullable error __attribute__((swift_name("error")));
@property (readonly) BismarckLong * _Nullable finished __attribute__((swift_name("finished")));
@property (readonly) BismarckMutableDictionary<NSString *, NSString *> *userData __attribute__((swift_name("userData")));
@end;

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SimpleRateLimiter")))
@interface BismarckSimpleRateLimiter : KotlinBase <BismarckRateLimiter>
- (instancetype)initWithMs:(int64_t)ms __attribute__((swift_name("init(ms:)"))) __attribute__((objc_designated_initializer));
- (BOOL)isFresh __attribute__((swift_name("isFresh()")));
- (void)reset __attribute__((swift_name("reset()")));
- (void)update __attribute__((swift_name("update()")));
@property (readonly) int64_t lastReset __attribute__((swift_name("lastReset")));
@property (readonly) int64_t lastRun __attribute__((swift_name("lastRun")));
@property (readonly) int64_t ms __attribute__((swift_name("ms")));
@end;

@interface BismarckBaseBismarck (Extensions)
- (BismarckBaseBismarck *)fetcherFn:(id<BismarckKotlinSuspendFunction0>)fn __attribute__((swift_name("fetcher(fn:)")));
@end;

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("_LambdasKt")))
@interface Bismarck_LambdasKt : KotlinBase
+ (id<BismarckBismarck>)addListener:(id<BismarckBismarck>)receiver fn:(void (^)(id _Nullable))fn __attribute__((swift_name("addListener(_:fn:)")));
+ (id<BismarckBismarck>)addTransform:(id<BismarckBismarck>)receiver fn:(id _Nullable (^)(id _Nullable))fn __attribute__((swift_name("addTransform(_:fn:)")));
@end;

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CommonKt")))
@interface BismarckCommonKt : KotlinBase
+ (NSString *)createApplicationScreenMessage __attribute__((swift_name("createApplicationScreenMessage()")));
@end;

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ActualKt")))
@interface BismarckActualKt : KotlinBase
+ (int64_t)currentTimeMillis __attribute__((swift_name("currentTimeMillis()")));
+ (int64_t)currentTimeNano __attribute__((swift_name("currentTimeNano()")));
+ (NSString *)platformName __attribute__((swift_name("platformName()")));
@end;

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinArray")))
@interface BismarckKotlinArray<T> : KotlinBase
+ (instancetype)arrayWithSize:(int32_t)size init:(T _Nullable (^)(BismarckInt *))init __attribute__((swift_name("init(size:init:)")));
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (T _Nullable)getIndex:(int32_t)index __attribute__((swift_name("get(index:)")));
- (id<BismarckKotlinIterator>)iterator __attribute__((swift_name("iterator()")));
- (void)setIndex:(int32_t)index value:(T _Nullable)value __attribute__((swift_name("set(index:value:)")));
@property (readonly) int32_t size __attribute__((swift_name("size")));
@end;

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinByteArray")))
@interface BismarckKotlinByteArray : KotlinBase
+ (instancetype)arrayWithSize:(int32_t)size __attribute__((swift_name("init(size:)")));
+ (instancetype)arrayWithSize:(int32_t)size init:(BismarckByte *(^)(BismarckInt *))init __attribute__((swift_name("init(size:init:)")));
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (int8_t)getIndex:(int32_t)index __attribute__((swift_name("get(index:)")));
- (BismarckKotlinByteIterator *)iterator __attribute__((swift_name("iterator()")));
- (void)setIndex:(int32_t)index value:(int8_t)value __attribute__((swift_name("set(index:value:)")));
@property (readonly) int32_t size __attribute__((swift_name("size")));
@end;

__attribute__((swift_name("Kotlinx_coroutines_coreCoroutineScope")))
@protocol BismarckKotlinx_coroutines_coreCoroutineScope
@required
@property (readonly) id<BismarckKotlinCoroutineContext> coroutineContext __attribute__((swift_name("coroutineContext")));
@end;

__attribute__((swift_name("KotlinFunction")))
@protocol BismarckKotlinFunction
@required
@end;

__attribute__((swift_name("KotlinSuspendFunction0")))
@protocol BismarckKotlinSuspendFunction0 <BismarckKotlinFunction>
@required
@end;

__attribute__((swift_name("KotlinIterator")))
@protocol BismarckKotlinIterator
@required
- (BOOL)hasNext __attribute__((swift_name("hasNext()")));
- (id _Nullable)next __attribute__((swift_name("next()")));
@end;

__attribute__((swift_name("KotlinByteIterator")))
@interface BismarckKotlinByteIterator : KotlinBase <BismarckKotlinIterator>
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (BismarckByte *)next __attribute__((swift_name("next()")));
- (int8_t)nextByte __attribute__((swift_name("nextByte()")));
@end;

__attribute__((swift_name("KotlinCoroutineContext")))
@protocol BismarckKotlinCoroutineContext
@required
- (id _Nullable)foldInitial:(id _Nullable)initial operation:(id _Nullable (^)(id _Nullable, id<BismarckKotlinCoroutineContextElement>))operation __attribute__((swift_name("fold(initial:operation:)")));
- (id<BismarckKotlinCoroutineContextElement> _Nullable)getKey:(id<BismarckKotlinCoroutineContextKey>)key __attribute__((swift_name("get(key:)")));
- (id<BismarckKotlinCoroutineContext>)minusKeyKey:(id<BismarckKotlinCoroutineContextKey>)key __attribute__((swift_name("minusKey(key:)")));
- (id<BismarckKotlinCoroutineContext>)plusContext:(id<BismarckKotlinCoroutineContext>)context __attribute__((swift_name("plus(context:)")));
@end;

__attribute__((swift_name("KotlinCoroutineContextElement")))
@protocol BismarckKotlinCoroutineContextElement <BismarckKotlinCoroutineContext>
@required
@property (readonly) id<BismarckKotlinCoroutineContextKey> key __attribute__((swift_name("key")));
@end;

__attribute__((swift_name("KotlinCoroutineContextKey")))
@protocol BismarckKotlinCoroutineContextKey
@required
@end;

#pragma clang diagnostic pop
NS_ASSUME_NONNULL_END
