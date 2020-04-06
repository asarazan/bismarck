# Bismarck (it syncs)
A new caching/syncing library for Kotlin Multiplatform

Under heavy construction. Better examples and instructions coming soon.

```kotlin
val bismarck = Bismarck.create<String> {
  storage = FileStorage("/savefiles", JsonSerializer()) // requires bismarck-serializer-kotlinx
  freshness = PersistentFreshness("/savefiles-freshness", 15 * 60 * 1000) // data is considered fresh for 15 minutes
  fetcher = {
    // do something expensive here
    delay(1000)
    "Foo"
  }
}

// Bismarck is coroutine-driven, 
// so async and observations should be scoped.
someScope.launch { 
  bismarck.eachValue {
    println("Received value $it")    
  }
}
```

### Installation
```gradle
commonMain {
    dependencies {
        implementation "net.sarazan:bismarck:$bismarck_version" // currently 0.0.5
    }
}
```
