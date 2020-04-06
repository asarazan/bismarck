# Bismarck (it syncs)
A new caching/syncing library for Kotlin Multiplatform

Under heavy construction. Better examples and instructions coming soon.

```kotlin
val cache = Bismarck.create<Foo> {
    fileStorage { 
        path = "foo.txt"
        serializer = JsonSerializer(Foo.serializer()) // requires bismarck-serializer-kotlinx
    }
    freshness { 
        path = "foo-fresh.txt"
        millis = 15 * 60 * 1000 // data is considered fresh for 15 minutes
    }
    fetcher {
        delay(1000) // imagine this is an expensive network fetch
        Foo(bar = 123)
    }
}

// Bismarck is coroutine-driven, 
// so async and observations should be scoped.
someScope.launch { 
  cache.eachValue {
    println("Received value ${it.bar}")    
  }
}
```

### Installation
```gradle
repositories {
    maven { url  "https://dl.bintray.com/asarazan/maven" }
}
//...
sourceSets {
    commonMain {
        dependencies {
            implementation "net.sarazan:bismarck:$bismarck_version" // currently 0.0.6
        }
    }
}
```
