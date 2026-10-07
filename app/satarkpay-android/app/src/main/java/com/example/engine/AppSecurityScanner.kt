package com.example.engine

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield

enum class SensitivePermissionType(val label: String, val description: String) {
    SMS("SMS & OTP Access", "Declared capability to read or receive SMS verification codes"),
    LOCATION("Location Access", "Declared GPS location access"),
    ACCESSIBILITY("Accessibility Service", "Can observe screen and simulate gestures (high misuse risk)"),
    SCREEN_OVERLAY("Screen Overlay", "Can display floating views over other applications"),
    NOTIFICATION("Notification Access", "Can read incoming notification banners"),
    CAMERA_MIC("Camera & Microphone", "Can access camera or microphone hardware")
}

enum class SensitiveRiskLevel(val label: String) {
    HIGH_RISK("High Risk"),       // Remote access screen-control tool or dangerous SMS+Accessibility combo
    MEDIUM_RISK("Needs Review"),   // Accessibility or SMS permission present without full remote control
    LOW_RISK("Standard")          // Normal system/user permissions without high-risk indicators
}

data class InstalledAppPermissionInfo(
    val packageName: String,
    val appName: String,
    val versionName: String,
    val isSystemApp: Boolean,
    val hasSmsPermission: Boolean,
    val hasLocationPermission: Boolean,
    val hasAccessibilityPermission: Boolean,
    val hasScreenOverlayPermission: Boolean,
    val hasNotificationPermission: Boolean,
    val hasCameraMicPermission: Boolean,
    val detectedSensitivePermissions: List<SensitivePermissionType>,
    val sensitivePermissionsCount: Int,
    val riskLevel: SensitiveRiskLevel,
    val riskSummary: String,
    val permissionRationale: String,
    val isSampleReference: Boolean = false
)

data class PermissionAuditSummary(
    val totalAppsScanned: Int,
    val appsWithSmsCount: Int,
    val appsWithLocationCount: Int,
    val appsWithAccessibilityCount: Int,
    val appsWithOverlayCount: Int,
    val highRiskAppsCount: Int,
    val isTargetedQuery: Boolean = true
)

object AppSecurityScanner {

    // Known Remote Access & screen control tools frequently abused in Digital Arrest / fake support scams
    private val KNOWN_REMOTE_CONTROL_APPS = mapOf(
        "com.anydesk.anydeskandroid" to "AnyDesk Remote Desktop",
        "com.teamviewer.quicksupport.market" to "TeamViewer QuickSupport",
        "com.teamviewer.teamviewer.market.mobile" to "TeamViewer Remote",
        "com.rustdesk.rustdesk" to "RustDesk Remote Access",
        "com.sand.airdroid" to "AirDroid Remote Support",
        "com.splashtop.remote" to "Splashtop Remote"
    )

    /**
     * Performs an asynchronous, non-blocking scan of packages visible to the application
     * on Dispatchers.IO to guarantee the UI thread never freezes.
     */
    suspend fun scanInstalledApplications(context: Context): List<InstalledAppPermissionInfo> =
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val resultList = mutableListOf<InstalledAppPermissionInfo>()

            // 1. First specifically check known high-risk remote control packages declared in <queries>
            for ((pkgName, appTitle) in KNOWN_REMOTE_CONTROL_APPS) {
                try {
                    val pkgInfo = pm.getPackageInfo(pkgName, PackageManager.GET_PERMISSIONS)
                    val requestedPerms = pkgInfo.requestedPermissions ?: emptyArray()
                    val hasAccessibility = requestedPerms.any { it.contains("ACCESSIBILITY", ignoreCase = true) }
                    val hasOverlay = requestedPerms.any { it.contains("SYSTEM_ALERT_WINDOW", ignoreCase = true) }

                    val detected = mutableListOf<SensitivePermissionType>()
                    if (hasAccessibility) detected.add(SensitivePermissionType.ACCESSIBILITY)
                    if (hasOverlay) detected.add(SensitivePermissionType.SCREEN_OVERLAY)

                    resultList.add(
                        InstalledAppPermissionInfo(
                            packageName = pkgName,
                            appName = appTitle,
                            versionName = pkgInfo.versionName ?: "1.0",
                            isSystemApp = false,
                            hasSmsPermission = false,
                            hasLocationPermission = false,
                            hasAccessibilityPermission = true,
                            hasScreenOverlayPermission = hasOverlay,
                            hasNotificationPermission = false,
                            hasCameraMicPermission = false,
                            detectedSensitivePermissions = detected,
                            sensitivePermissionsCount = detected.size,
                            riskLevel = SensitiveRiskLevel.HIGH_RISK,
                            riskSummary = "Active Remote Screen Sharing tool. Often coerced in Digital Arrest and fake refund frauds.",
                            permissionRationale = "Allows external operators to view your screen and observe banking OTPs.",
                            isSampleReference = false
                        )
                    )
                } catch (_: PackageManager.NameNotFoundException) {
                    // App is not installed on the device (clean state)
                } catch (_: Exception) {
                    // Graceful fallback
                }
            }

            // 2. Query visible installed packages under Android package visibility rules
            try {
                val flags = PackageManager.GET_PERMISSIONS
                val packages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(flags.toLong()))
                } else {
                    @Suppress("DEPRECATION")
                    pm.getInstalledPackages(flags)
                }

                for ((index, pkg) in packages.withIndex()) {
                    // Yield every 20 packages to prevent starving other background coroutines
                    if (index % 20 == 0) yield()

                    // Exclude self and apps already added from known list
                    if (pkg.packageName == context.packageName) continue
                    if (resultList.any { it.packageName == pkg.packageName }) continue

                    val isSystem = (pkg.applicationInfo?.flags ?: 0) and ApplicationInfo.FLAG_SYSTEM != 0
                    val appLabel = pkg.applicationInfo?.loadLabel(pm)?.toString() ?: pkg.packageName
                    val lowerPkg = pkg.packageName.lowercase()
                    val requestedPermissions = pkg.requestedPermissions ?: emptyArray()

                    val hasSms = requestedPermissions.any { perm ->
                        perm.equals("android.permission.READ_SMS", ignoreCase = true) ||
                        perm.equals("android.permission.RECEIVE_SMS", ignoreCase = true) ||
                        perm.equals("android.permission.SEND_SMS", ignoreCase = true)
                    }

                    val hasLocation = requestedPermissions.any { perm ->
                        perm.equals("android.permission.ACCESS_FINE_LOCATION", ignoreCase = true) ||
                        perm.equals("android.permission.ACCESS_COARSE_LOCATION", ignoreCase = true)
                    }

                    val hasAccessibility = requestedPermissions.any { perm ->
                        perm.contains("ACCESSIBILITY", ignoreCase = true)
                    }

                    val hasOverlay = requestedPermissions.any { perm ->
                        perm.contains("SYSTEM_ALERT_WINDOW", ignoreCase = true)
                    }

                    val hasNotification = requestedPermissions.any { perm ->
                        perm.contains("BIND_NOTIFICATION_LISTENER", ignoreCase = true)
                    }

                    val hasCamMic = requestedPermissions.any { perm ->
                        perm.equals("android.permission.CAMERA", ignoreCase = true) ||
                        perm.equals("android.permission.RECORD_AUDIO", ignoreCase = true)
                    }

                    val detected = mutableListOf<SensitivePermissionType>()
                    if (hasSms) detected.add(SensitivePermissionType.SMS)
                    if (hasLocation) detected.add(SensitivePermissionType.LOCATION)
                    if (hasAccessibility) detected.add(SensitivePermissionType.ACCESSIBILITY)
                    if (hasOverlay) detected.add(SensitivePermissionType.SCREEN_OVERLAY)
                    if (hasNotification) detected.add(SensitivePermissionType.NOTIFICATION)
                    if (hasCamMic) detected.add(SensitivePermissionType.CAMERA_MIC)

                    // Skip apps with 0 sensitive permissions to keep UI concise
                    if (detected.isEmpty()) continue

                    // Determine risk level with objective rationale
                    val isRemoteTool = KNOWN_REMOTE_CONTROL_APPS.containsKey(lowerPkg)
                    val riskLevel = when {
                        isRemoteTool -> SensitiveRiskLevel.HIGH_RISK
                        (hasAccessibility && hasSms && !isSystem) -> SensitiveRiskLevel.HIGH_RISK
                        (hasAccessibility && !isSystem) -> SensitiveRiskLevel.MEDIUM_RISK
                        (hasSms && !isSystem) -> SensitiveRiskLevel.MEDIUM_RISK
                        else -> SensitiveRiskLevel.LOW_RISK
                    }

                    val summary = when {
                        isRemoteTool -> "Remote screen-sharing tool (potential coercion vehicle)"
                        hasAccessibility && hasSms && !isSystem -> "Combined SMS & screen reading capability (Review recommended)"
                        hasAccessibility && !isSystem -> "Accessibility service declared. Capable of observing on-screen interactions."
                        hasSms && !isSystem -> "SMS reading declared. Standard for SMS clients; inspect if unexpected."
                        hasOverlay && !isSystem -> "Can display floating overlay windows above banking interfaces."
                        hasLocation -> "Location tracking declared."
                        else -> "Standard sensitive permissions"
                    }

                    val rationale = when {
                        isSystem -> "System application installed by device manufacturer."
                        hasSms -> "Declared SMS permission. Common in dialers, banks, and messengers."
                        hasAccessibility -> "Declared Accessibility permission. Common in screen readers and utility tools."
                        else -> "Declared standard Android permissions."
                    }

                    resultList.add(
                        InstalledAppPermissionInfo(
                            packageName = pkg.packageName,
                            appName = appLabel,
                            versionName = pkg.versionName ?: "1.0",
                            isSystemApp = isSystem,
                            hasSmsPermission = hasSms,
                            hasLocationPermission = hasLocation,
                            hasAccessibilityPermission = hasAccessibility,
                            hasScreenOverlayPermission = hasOverlay,
                            hasNotificationPermission = hasNotification,
                            hasCameraMicPermission = hasCamMic,
                            detectedSensitivePermissions = detected,
                            sensitivePermissionsCount = detected.size,
                            riskLevel = riskLevel,
                            riskSummary = summary,
                            permissionRationale = rationale,
                            isSampleReference = false
                        )
                    )
                }
            } catch (_: Exception) {
                // Return whatever packages were successfully collected
            }

            // Return prioritized list: High risk first, then Medium risk, then Low risk
            resultList.sortedWith(
                compareByDescending<InstalledAppPermissionInfo> { it.riskLevel == SensitiveRiskLevel.HIGH_RISK }
                    .thenByDescending { it.riskLevel == SensitiveRiskLevel.MEDIUM_RISK }
                    .thenByDescending { it.sensitivePermissionsCount }
            )
        }

    /**
     * Aggregates scan results into truthful, verifiable counts.
     */
    fun computeAuditSummary(apps: List<InstalledAppPermissionInfo>): PermissionAuditSummary {
        val total = apps.size
        val sms = apps.count { it.hasSmsPermission && !it.isSystemApp }
        val location = apps.count { it.hasLocationPermission && !it.isSystemApp }
        val accessibility = apps.count { it.hasAccessibilityPermission && !it.isSystemApp }
        val overlay = apps.count { it.hasScreenOverlayPermission && !it.isSystemApp }
        val highRisk = apps.count { it.riskLevel == SensitiveRiskLevel.HIGH_RISK }

        return PermissionAuditSummary(
            totalAppsScanned = total,
            appsWithSmsCount = sms,
            appsWithLocationCount = location,
            appsWithAccessibilityCount = accessibility,
            appsWithOverlayCount = overlay,
            highRiskAppsCount = highRisk,
            isTargetedQuery = true
        )
    }

    /**
     * Opens Android System Application Settings for a given package.
     * This is the recommended primary user action for manual review.
     */
    fun openAppDetailsSettings(context: Context, packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            // fallback
        }
    }

    /**
     * Triggers an uninstall intent for user-approved app removal.
     * Note: Android always displays the system package installer confirmation dialog
     * to the user before an application can be uninstalled.
     */
    fun requestAppUninstall(context: Context, packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE).apply {
                data = Uri.fromParts("package", packageName, null)
                putExtra(Intent.EXTRA_RETURN_RESULT, true)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            openAppDetailsSettings(context, packageName)
        }
    }
}
