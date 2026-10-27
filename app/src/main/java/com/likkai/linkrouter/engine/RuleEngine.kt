package com.likkai.linkrouter.engine

import android.util.Log
import com.likkai.linkrouter.data.BrowserRule
import com.likkai.linkrouter.data.RuleRepository

class RuleEngine(
    private val repository: RuleRepository,
    private val appPackageName: String
) {

    companion object {
        private const val TAG = "RuleEngine"
    }

    /**
     * Find the target browser package for the given URL.
     * [sourcePackage] is the app that opened the link, or null when it is unknown.
     * Returns the package name of the matched browser, or null if no rule matches.
     * Rules are evaluated in list order. The first match wins.
     */
    fun findTargetBrowser(
        url: String,
        sourcePackage: String?,
        debug: Boolean = false
    ): String? {
        val rules = repository.getCachedRules()

        if (debug) {
            Log.d(TAG, "Evaluating URL: $url from ${sourcePackage ?: "unknown"} against ${rules.size} rules")
        }

        for (rule in rules) {
            if (!rule.isEnabled) {
                if (debug) Log.d(TAG, "Rule ${rule.id} (${rule.pattern}) is disabled, skipping")
                continue
            }

            if (rule.targetBrowserPackage == appPackageName) {
                if (debug) Log.d(TAG, "Rule ${rule.id} targets self, skipping (loop prevention)")
                continue
            }

            if (matches(rule, url, sourcePackage)) {
                if (debug) {
                    Log.d(
                        TAG,
                        "MATCH: Rule ${rule.id} (${rule.pattern.ifBlank { "*" }}, from=${rule.sourcePackage ?: "*"}) -> ${rule.targetBrowserPackage}"
                    )
                }
                return rule.targetBrowserPackage
            } else if (debug) {
                Log.d(
                    TAG,
                    "No match: Rule ${rule.id} (${rule.pattern.ifBlank { "*" }}, from=${rule.sourcePackage ?: "*"})"
                )
            }
        }

        if (debug) Log.d(TAG, "No rule matched for URL: $url")
        return null
    }

    private fun matches(rule: BrowserRule, url: String, sourcePackage: String?): Boolean {
        val pattern = editorPattern(rule.matchType, rule.pattern).trim()
        val hasPattern = pattern.isNotEmpty()
        val hasSource = !rule.sourcePackage.isNullOrBlank()
        if (!hasPattern && !hasSource) return false
        if (!sourceMatches(rule, sourcePackage)) return false
        if (!hasPattern) return true
        return urlMatchesPattern(pattern, url)
    }

    /**
     * A blank source matches links from any app.
     * Package names are compared case-insensitively because the platform referrer
     * host is lowercased.
     */
    private fun sourceMatches(rule: BrowserRule, sourcePackage: String?): Boolean {
        val required = rule.sourcePackage
        if (required.isNullOrBlank()) return true
        return sourcePackage != null && sourcePackage.equals(required, ignoreCase = true)
    }
}
