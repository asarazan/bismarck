# Bismarck (it syncs)
A new caching/syncing library for Kotlin Multiplatform

Under heavy construction. Better examples and instructions coming soon.

```kotlin
val cache = Bismarck.create<UserRecords> {
    fileStorage { 
        path = "users.json"
        serializer = JsonSerializer(UserRecords.serializer()) // requires bismarck-serializer-kotlinx
    }
    freshness { 
        path = "users-freshness" // freshness state will survive shutdowns if you specify a path
        millis = 15 * 60 * 1000 // data is considered fresh for 15 minutes
    }
    fetcher {
        myApi.fetchAndProcessHundredsOfRecords()
    }
}

// Bismarck is coroutine-driven, 
// so async and observations should be scoped.
someScope.launch {

    // This will be called whenever the data is updated, as long as the scope continues.
    cache.eachValue {
        println("Received value ${it.bar}")
    }
    
    // It's helpful to know what your cache is doing. Observe state to find out!
    cache.eachState {
        spinner.isVisible = it == Bismarck.State.Fetching
    }
    
    // Same for error states!
    cache.eachError {
        warningIcon.isVisible = it != null
    }
}

// You can also grab all of these values directly
val current = cache.value
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
