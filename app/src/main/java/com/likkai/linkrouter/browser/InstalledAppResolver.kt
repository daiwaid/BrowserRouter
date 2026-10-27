package com.likkai.linkrouter.browser

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

class InstalledAppResolver(private val context: Context) {

    /**
     * Apps with a launcher icon, excluding LinkRouter itself.
     * These are the apps a link can be opened from.
     */
    fun getLaunchableApps(): List<BrowserApp> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val packageManager = context.packageManager

        return packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .map { info ->
                val appInfo = info.activityInfo.applicationInfo
                BrowserApp(
                    label = appInfo.loadLabel(packageManager).toString(),
                    packageName = info.activityInfo.packageName
                )
            }
            .filter { it.packageName != context.packageName }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }
}
