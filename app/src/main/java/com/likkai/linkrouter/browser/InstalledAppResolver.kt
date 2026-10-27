package com.likkai.linkrouter.browser

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

class InstalledAppResolver(private val context: Context) {

    /**
     * Apps the user can pick as a link source: every launchable app, plus any
     * other app they installed. LinkRouter itself is excluded.
     */
    fun getInstalledApps(): List<BrowserApp> {
        val packageManager = context.packageManager
        val launchable = packageManager.queryIntentActivities(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),
            PackageManager.MATCH_ALL
        ).map { it.activityInfo.packageName }.toSet()

        return packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .asSequence()
            .filter { it.packageName != context.packageName }
            .filter { info ->
                info.packageName in launchable || info.flags and ApplicationInfo.FLAG_SYSTEM == 0
            }
            .map { info ->
                val label = info.loadLabel(packageManager).toString().ifBlank { info.packageName }
                BrowserApp(label = label, packageName = info.packageName)
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
            .toList()
    }
}
