package com.likkai.linkrouter.data

enum class MatchType {
    EXACT_URL,
    DOMAIN,
    /** Every link opened from one app, regardless of the URL. */
    SOURCE_APP
}
