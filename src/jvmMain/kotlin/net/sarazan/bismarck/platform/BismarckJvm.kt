package net.sarazan.bismarck.platform

import java.util.concurrent.Executors

class BismarckJvm {

    companion object {
        val DEFAULT_EXECUTOR = Executors.newCachedThreadPool()
    }
    
}