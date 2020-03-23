# Bismarck (it syncs)
A new caching/syncing library for Kotlin Multiplatform

Under heavy construction. Better examples and instructions coming soon.

```kotlin
val bismarck = BaseBismarck<String>()
  .persister(FilePersister("/saves", "123", JavaSerializer()))
  .rateLimiter(SimpleRateLimiter(15 * 60 * 1000))
  .fetcher { 
      // do something expensive here
      "Some hugely important derived value"
  }   
  
  bismarck.eachValue { 
      println("Received value $it")
  }
```
