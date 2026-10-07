package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.InstalledAppPermissionInfo
import com.example.engine.PermissionAuditSummary
import com.example.engine.SensitivePermissionType
import com.example.engine.SensitiveRiskLevel
import com.example.ui.theme.*

@Composable
fun AppSecurityScreen(
    scannedApps: List<InstalledAppPermissionInfo>,
    summary: PermissionAuditSummary,
    isScanning: Boolean,
    onRescan: () -> Unit,
    onUninstallApp: (String) -> Unit,
    onOpenSettings: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredApps = remember(scannedApps, selectedFilter, searchQuery) {
        scannedApps.filter { app ->
            val matchesSearch = searchQuery.isBlank() ||
                app.appName.contains(searchQuery, ignoreCase = true) ||
                app.packageName.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                "SMS" -> app.hasSmsPermission
                "LOCATION" -> app.hasLocationPermission
                "ACCESSIBILITY" -> app.hasAccessibilityPermission
                "OVERLAY" -> app.hasScreenOverlayPermission
                "HIGH_RISK" -> app.riskLevel == SensitiveRiskLevel.HIGH_RISK
                else -> app.sensitivePermissionsCount > 0
            }

            matchesSearch && matchesFilter
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SatarkBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Navigation & Actions Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("app_security_back")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = SatarkInk)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Permission Auditor",
                    style = MaterialTheme.typography.titleMedium,
                    color = SatarkInk,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${summary.totalAppsScanned} apps with sensitive permissions • ${summary.highRiskAppsCount} high risk (Targeted Visibility)",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (summary.highRiskAppsCount > 0) SatarkDanger else SatarkOk,
                    fontSize = 11.5.sp
                )
            }

            IconButton(
                onClick = onRescan,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(SatarkAccentAlpha)
                    .testTag("rescan_apps_button")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Rescan", tint = SatarkAccent)
            }
        }

        // Live Scanning Progress Line
        if (isScanning) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = SatarkAccent,
                trackColor = SatarkBorder
            )
        }

        // Search Input Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by app name or package...", color = SatarkDim, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SatarkDim, modifier = Modifier.size(18.dp)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = SatarkDim, modifier = Modifier.size(16.dp))
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("app_search_field"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = SatarkInk,
                unfocusedTextColor = SatarkInk,
                focusedBorderColor = SatarkAccent,
                unfocusedBorderColor = SatarkBorder,
                focusedContainerColor = SatarkPanelCard,
                unfocusedContainerColor = SatarkPanelCard
            )
        )

        // Summary Metric Tiles Row (SMS, Location, Accessibility, Overlay)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PermissionStatPill(
                modifier = Modifier.weight(1f),
                title = "SMS / OTP",
                count = "${summary.appsWithSmsCount}",
                icon = Icons.Default.Message,
                color = SatarkPurple,
                isSelected = selectedFilter == "SMS",
                onClick = { selectedFilter = if (selectedFilter == "SMS") "ALL" else "SMS" }
            )
            PermissionStatPill(
                modifier = Modifier.weight(1f),
                title = "Location",
                count = "${summary.appsWithLocationCount}",
                icon = Icons.Default.LocationOn,
                color = SatarkAccent,
                isSelected = selectedFilter == "LOCATION",
                onClick = { selectedFilter = if (selectedFilter == "LOCATION") "ALL" else "LOCATION" }
            )
            PermissionStatPill(
                modifier = Modifier.weight(1f),
                title = "Accessibility",
                count = "${summary.appsWithAccessibilityCount}",
                icon = Icons.Default.Visibility,
                color = SatarkWarn,
                isSelected = selectedFilter == "ACCESSIBILITY",
                onClick = { selectedFilter = if (selectedFilter == "ACCESSIBILITY") "ALL" else "ACCESSIBILITY" }
            )
            PermissionStatPill(
                modifier = Modifier.weight(1f),
                title = "Overlay",
                count = "${summary.appsWithOverlayCount}",
                icon = Icons.Default.Layers,
                color = SatarkDanger,
                isSelected = selectedFilter == "OVERLAY",
                onClick = { selectedFilter = if (selectedFilter == "OVERLAY") "ALL" else "OVERLAY" }
            )
        }

        // Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedFilter == "ALL",
                onClick = { selectedFilter = "ALL" },
                label = { Text("All (${scannedApps.count { it.sensitivePermissionsCount > 0 }})", fontSize = 11.5.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SatarkAccentAlpha,
                    selectedLabelColor = SatarkAccent
                )
            )
            FilterChip(
                selected = selectedFilter == "HIGH_RISK",
                onClick = { selectedFilter = "HIGH_RISK" },
                label = { Text("High Risk (${summary.highRiskAppsCount})", fontSize = 11.5.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SatarkDangerAlpha,
                    selectedLabelColor = SatarkDanger
                )
            )
            FilterChip(
                selected = selectedFilter == "SMS",
                onClick = { selectedFilter = "SMS" },
                label = { Text("SMS / OTP (${summary.appsWithSmsCount})", fontSize = 11.5.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SatarkPurpleAlpha,
                    selectedLabelColor = SatarkPurple
                )
            )
            FilterChip(
                selected = selectedFilter == "ACCESSIBILITY",
                onClick = { selectedFilter = "ACCESSIBILITY" },
                label = { Text("Accessibility (${summary.appsWithAccessibilityCount})", fontSize = 11.5.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SatarkWarnAlpha,
                    selectedLabelColor = SatarkWarn
                )
            )
            FilterChip(
                selected = selectedFilter == "LOCATION",
                onClick = { selectedFilter = "LOCATION" },
                label = { Text("Location (${summary.appsWithLocationCount})", fontSize = 11.5.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SatarkAccentAlpha,
                    selectedLabelColor = SatarkAccent
                )
            )
        }

        // Clean, Scrollable List of Installed Applications
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("installed_apps_scrollable_list"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (filteredApps.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SatarkPanelCard,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SatarkOk, modifier = Modifier.size(36.dp))
                            Text("No Suspicious Apps Found", style = MaterialTheme.typography.titleMedium, color = SatarkInk, fontWeight = FontWeight.Bold)
                            Text("No applications match the selected filter criteria.", style = MaterialTheme.typography.bodySmall, color = SatarkDim)
                        }
                    }
                }
            } else {
                items(filteredApps, key = { it.packageName }) { app ->
                    InstalledAppCard(
                        app = app,
                        onUninstall = { onUninstallApp(app.packageName) },
                        onOpenSettings = { onOpenSettings(app.packageName) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionStatPill(
    title: String,
    count: String,
    icon: ImageVector,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) color.copy(alpha = 0.2f) else SatarkPanelCard,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(if (isSelected) color else SatarkBorder)
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Text(
                text = count,
                style = MaterialTheme.typography.titleSmall,
                color = SatarkInk,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = SatarkDim,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun InstalledAppCard(
    app: InstalledAppPermissionInfo,
    onUninstall: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val isHighRisk = app.riskLevel == SensitiveRiskLevel.HIGH_RISK
    val riskBadgeColor = when (app.riskLevel) {
        SensitiveRiskLevel.HIGH_RISK -> SatarkDanger
        SensitiveRiskLevel.MEDIUM_RISK -> SatarkWarn
        SensitiveRiskLevel.LOW_RISK -> SatarkOk
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("app_item_${app.packageName}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SatarkPanelCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(if (isHighRisk) SatarkDanger else SatarkBorder)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Row: App Label + Risk Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = riskBadgeColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = app.appName.take(1).uppercase(),
                                color = riskBadgeColor,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Column {
                        Text(
                            text = app.appName,
                            style = MaterialTheme.typography.titleSmall,
                            color = SatarkInk,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${app.packageName} • v${app.versionName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SatarkDim,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = riskBadgeColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (isHighRisk) "🔴 HIGH RISK" else if (app.riskLevel == SensitiveRiskLevel.MEDIUM_RISK) "🟡 MEDIUM" else "🟢 LOW",
                        style = MaterialTheme.typography.labelSmall,
                        color = riskBadgeColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Risk Explanation Summary
            Text(
                text = "• ${app.riskSummary}",
                style = MaterialTheme.typography.bodySmall,
                color = if (isHighRisk) SatarkDanger else SatarkInk,
                fontSize = 12.sp
            )

            if (app.permissionRationale.isNotBlank()) {
                Text(
                    text = "Context: ${app.permissionRationale}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SatarkDim,
                    fontSize = 11.sp
                )
            }

            // Detected Sensitive Permission Tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (app.hasAccessibilityPermission) {
                    SensitiveTag("Accessibility", SatarkWarn)
                }
                if (app.hasSmsPermission) {
                    SensitiveTag("SMS / OTP", SatarkPurple)
                }
                if (app.hasLocationPermission) {
                    SensitiveTag("Location", SatarkAccent)
                }
                if (app.hasScreenOverlayPermission) {
                    SensitiveTag("Overlay", SatarkDanger)
                }
            }

            // Action Buttons: Manage in Settings / Uninstall
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenSettings,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("manage_perm_${app.packageName}"),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(SatarkAccent)
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = SatarkAccent, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Manage Permissions", color = SatarkAccent, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                }

                if (!app.isSystemApp) {
                    Button(
                        onClick = onUninstall,
                        colors = ButtonDefaults.buttonColors(containerColor = if (isHighRisk) SatarkDanger else SatarkBorder),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("uninstall_${app.packageName}"),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Uninstall", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SensitiveTag(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
