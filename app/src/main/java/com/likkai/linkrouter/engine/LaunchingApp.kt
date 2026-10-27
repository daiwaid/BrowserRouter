package com.likkai.linkrouter.engine

import android.app.Activity
import android.os.Build

/**
 * Package of the app that handed this link to LinkRouter.
 *
 * API 34+ reports the real launching package. Older releases fall back to the
 * platform referrer (`android-app://<package>`). An http referrer, or this app
 * itself, is treated as unknown so a rule cannot match a spoofed or self-launch.
 */
private const val ANDROID_APP_SCHEME = "android-app"

fun Activity.launchingAppPackage(): String? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        launchedFromPackage
            ?.takeIf { it.isNotBlank() && !it.equals(packageName, ignoreCase = true) }
            ?.let { return it }
    }

    val uri = referrer ?: return null
    if (uri.scheme != ANDROID_APP_SCHEME) return null
    return uri.host?.takeIf { it.isNotBlank() && !it.equals(packageName, ignoreCase = true) }
}
