package com.likkai.linkrouter.engine

import android.net.Uri
import com.likkai.linkrouter.data.MatchType

/**
 * A link pattern. Star matches any text.
 * A pattern without a scheme matches http and https.
 * A pattern without a path matches only the root page.
 * A host with no star matches that host and its www host, not other subdomains.
 * A leading star-dot host also matches the apex host.
 * A lone star matches every URL.
 */
internal fun urlMatchesPattern(pattern: String, url: String): Boolean {
    if (pattern.trim() == "*") {
        return Uri.parse(url).host != null
    }
    val parsed = parseUrlPattern(pattern) ?: return false
    val uri = Uri.parse(url)
    val scheme = uri.scheme?.lowercase() ?: return false
    val host = uri.host?.lowercase() ?: return false
    val path = uri.path?.takeIf { it.isNotEmpty() } ?: "/"

    if (parsed.scheme != null && !globMatches(parsed.scheme, scheme)) return false
    if (!hostMatches(parsed.host, host)) return false
    val pathPattern = parsed.path
    if (pathPattern == null) {
        if (path != "/") return false
    } else if (!pathMatches(pathPattern, path)) {
        return false
    }
    val queryPattern = parsed.query
    if (queryPattern != null && !globMatches(queryPattern, uri.query ?: "")) return false
    return true
}

/**
 * Domain rules saved before wildcard patterns are shown and matched as the whole site.
 */
internal fun editorPattern(matchType: MatchType, pattern: String): String {
    val trimmed = pattern.trim()
    if (
        matchType == MatchType.DOMAIN &&
        trimmed.isNotEmpty() &&
        !trimmed.contains('*') &&
        !trimmed.contains('/') &&
        !trimmed.contains("://")
    ) {
        return "*.$trimmed/*"
    }
    return pattern
}

private data class ParsedPattern(
    val scheme: String?,
    val host: String,
    val path: String?,
    val query: String?
)

private fun parseUrlPattern(raw: String): ParsedPattern? {
    var rest = raw.trim()
    if (rest.isEmpty()) return null

    var query: String? = null
    val queryStart = rest.indexOf('?')
    if (queryStart >= 0) {
        query = rest.substring(queryStart + 1).substringBefore('#')
        rest = rest.substring(0, queryStart)
    }
    rest = rest.substringBefore('#')
    if (rest.isEmpty()) return null

    var scheme: String? = null
    val schemeSep = rest.indexOf("://")
    if (schemeSep >= 0) {
        scheme = rest.substring(0, schemeSep).lowercase()
        rest = rest.substring(schemeSep + 3)
    }

    val slash = rest.indexOf('/')
    var host = (if (slash >= 0) rest.substring(0, slash) else rest).lowercase()
    val path = if (slash >= 0) rest.substring(slash) else null
    val portSep = host.lastIndexOf(':')
    if (portSep > 0 && host.substring(portSep + 1).all { it.isDigit() }) {
        host = host.substring(0, portSep)
    }
    if (host.isEmpty()) return null
    return ParsedPattern(scheme, host, path, query)
}

private fun hostMatches(glob: String, host: String): Boolean {
    if (globMatches(glob, host)) return true
    // "*.domain.com" also matches the apex host "domain.com".
    if (glob.startsWith("*.") && globMatches(glob.removePrefix("*."), host)) return true
    // "domain.com" also matches "www.domain.com", and no other subdomain.
    if (!glob.contains('*') && !glob.startsWith("www.") && host == "www.$glob") return true
    return false
}

private fun pathMatches(glob: String, path: String): Boolean {
    if (globMatches(glob, path)) return true
    if (!glob.contains('*') && path == "$glob/") return true
    if (glob.endsWith("/*")) {
        val parent = glob.removeSuffix("/*")
        if (parent.isNotEmpty() && (path == parent || path == "$parent/")) return true
    }
    return false
}

private fun globMatches(glob: String, value: String): Boolean {
    val regex = buildString {
        append('^')
        glob.forEach { char ->
            if (char == '*') append(".*") else append(Regex.escape(char.toString()))
        }
        append('$')
    }
    return Regex(regex).matches(value)
}
