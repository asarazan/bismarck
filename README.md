# Route Maps for Kotlin Multiplatform

This is a pure Kotlin solution for general path-based routing. Inspired by Ktor, but more generally applicable.

### Usage

```kotlin
val map = routeMapOf(
    "/users" to allUsersHandler,
    "/users/{userId}" to lookupUserHandler,
    "/users/yolo/*" to singleSegmentFallback,
    "/**" to everythingFallback
)

val route = map["/users/some_random_uid"]!!
val handler = route.value
val userId = route.params["userId"] // some_random_uid
val originalRoute = route.route // "/users/{userId}"
```

### Quality / Precedence
Precedence calculations are simple but relatively effective: 
1. Replace all route param/wildcards with ""
2. Run the Levenshtein distance between that and the input
3. Sort, and take the lowest distance

### TODO
* Would like to get test coverage numbers :D
* Eventual goal is to create a full url routing library on top of this, complete with http method support etc.