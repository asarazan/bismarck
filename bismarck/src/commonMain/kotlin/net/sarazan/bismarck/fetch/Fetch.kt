package net.sarazan.bismarck.fetch

typealias Fetch<T> = suspend () -> T?
