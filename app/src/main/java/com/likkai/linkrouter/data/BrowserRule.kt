package com.likkai.linkrouter.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "browser_rules")
data class BrowserRule(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val matchType: MatchType,
    val pattern: String,
    val targetBrowserPackage: String,
    val targetBrowserLabel: String,
    val orderIndex: Int,
    val isEnabled: Boolean = true,
    /** Package of the app the link must be opened from. Null matches every app. */
    val sourcePackage: String? = null,
    /** Human-readable label for [sourcePackage], captured when the rule is saved. */
    val sourceLabel: String? = null
)
