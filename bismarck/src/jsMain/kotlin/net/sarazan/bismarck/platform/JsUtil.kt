package net.sarazan.bismarck.platform

import org.w3c.dom.url.URL

fun URL.parent(): URL? {
    if (pathname == "/") return null
    val parent = href.subSequence(0, href.lastIndexOf("/") + 1)
    return URL(parent.toString())
}
