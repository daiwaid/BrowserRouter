package com.likkai.linkrouter.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.*
import com.likkai.linkrouter.ui.components.BrowserIcon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.likkai.linkrouter.browser.BrowserApp
import com.likkai.linkrouter.data.BrowserRule
import com.likkai.linkrouter.data.MatchType

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditRuleDialog(
    editingRule: BrowserRule?,
    installedBrowsers: List<BrowserApp>,
    installedApps: List<BrowserApp>,
    onDismiss: () -> Unit,
    onSave: (MatchType, String, BrowserApp, BrowserApp?) -> Unit,
    onUpdate: (BrowserRule) -> Unit
) {
    var matchType by remember {
        mutableStateOf(editingRule?.matchType ?: MatchType.DOMAIN)
    }
    var pattern by remember {
        mutableStateOf(editingRule?.pattern ?: "")
    }
    var selectedBrowser by remember {
        mutableStateOf(
            editingRule?.let { rule ->
                installedBrowsers.find { it.packageName == rule.targetBrowserPackage }
                    ?: BrowserApp(rule.targetBrowserLabel, rule.targetBrowserPackage)
            }
        )
    }
    var sourceApp by remember {
        mutableStateOf(editingRule?.toSourceApp(installedApps))
    }
    var showBrowserPicker by remember { mutableStateOf(false) }
    var showAppPicker by remember { mutableStateOf(false) }

    val isEditing = editingRule != null
    val canSave = selectedBrowser != null && when (matchType) {
        MatchType.SOURCE_APP -> sourceApp != null
        MatchType.EXACT_URL, MatchType.DOMAIN -> pattern.isNotBlank()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (isEditing) "Edit Rule" else "Add Rule",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Match Type",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = matchType == MatchType.DOMAIN,
                        onClick = { matchType = MatchType.DOMAIN },
                        label = { Text("Entire Domain") }
                    )
                    FilterChip(
                        selected = matchType == MatchType.EXACT_URL,
                        onClick = { matchType = MatchType.EXACT_URL },
                        label = { Text("Exact URL") }
                    )
                    FilterChip(
                        selected = matchType == MatchType.SOURCE_APP,
                        onClick = { matchType = MatchType.SOURCE_APP },
                        label = { Text("From app") }
                    )
                }

                if (matchType != MatchType.SOURCE_APP) {
                    val exactUrl = matchType == MatchType.EXACT_URL
                    OutlinedTextField(
                        value = pattern,
                        onValueChange = { pattern = it },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            autoCorrectEnabled = false
                        ),
                        label = { Text(if (exactUrl) "URL" else "Domain") },
                        placeholder = {
                            Text(if (exactUrl) "https://example.com/home" else "example.com")
                        },
                        supportingText = {
                            Text(
                                if (exactUrl) "Match this exact URL only"
                                else "Match all URLs on this domain (including subdomains)"
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Text(
                    "Opened from",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAppPicker = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (sourceApp != null) {
                            BrowserIcon(
                                packageName = sourceApp!!.packageName,
                                size = 28.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.Apps,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                sourceApp?.label ?: if (matchType == MatchType.SOURCE_APP) {
                                    "Select an app"
                                } else {
                                    "Any app"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = if (sourceApp != null)
                                    MaterialTheme.colorScheme.onSurface
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (sourceApp != null) {
                                Text(
                                    sourceApp!!.packageName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        if (sourceApp != null && matchType != MatchType.SOURCE_APP) {
                            IconButton(onClick = { sourceApp = null }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Match any app"
                                )
                            }
                        }
                    }
                }
                Text(
                    when (matchType) {
                        MatchType.SOURCE_APP -> "Every link opened from this app"
                        MatchType.DOMAIN, MatchType.EXACT_URL ->
                            "Optional. Limit this rule to links opened from one app."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    "Open with",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showBrowserPicker = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (selectedBrowser != null) {
                            BrowserIcon(
                                packageName = selectedBrowser!!.packageName,
                                size = 28.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.OpenInBrowser,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            selectedBrowser?.label ?: "Select a browser",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (selectedBrowser != null)
                                MaterialTheme.colorScheme.onSurface
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val browser = selectedBrowser ?: return@Button
                    val savedPattern = if (matchType == MatchType.SOURCE_APP) "" else pattern.trim()
                    val savedSource = sourceApp
                    val existing = editingRule
                    if (existing != null) {
                        onUpdate(
                            existing.copy(
                                matchType = matchType,
                                pattern = savedPattern,
                                targetBrowserPackage = browser.packageName,
                                targetBrowserLabel = browser.label,
                                sourcePackage = savedSource?.packageName,
                                sourceLabel = savedSource?.label
                            )
                        )
                    } else {
                        onSave(matchType, savedPattern, browser, savedSource)
                    }
                },
                enabled = canSave
            ) {
                Text(if (isEditing) "Update" else "Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    if (showBrowserPicker) {
        BrowserPickerDialog(
            browsers = installedBrowsers,
            selectedPackage = selectedBrowser?.packageName,
            onSelect = { browser ->
                selectedBrowser = browser
                showBrowserPicker = false
            },
            onDismiss = { showBrowserPicker = false }
        )
    }

    if (showAppPicker) {
        BrowserPickerDialog(
            browsers = installedApps,
            selectedPackage = sourceApp?.packageName,
            onSelect = { app ->
                sourceApp = app
                showAppPicker = false
            },
            onDismiss = { showAppPicker = false },
            title = "Opened from",
            emptyMessage = "No apps found on this device.",
            showSearch = true,
            anyLabel = if (matchType == MatchType.SOURCE_APP) null else "Any app",
            onSelectAny = if (matchType == MatchType.SOURCE_APP) {
                null
            } else {
                {
                    sourceApp = null
                    showAppPicker = false
                }
            }
        )
    }
}

private fun BrowserRule.toSourceApp(installedApps: List<BrowserApp>): BrowserApp? {
    val pkg = sourcePackage ?: return null
    return installedApps.find { it.packageName == pkg }
        ?: BrowserApp(label = sourceLabel ?: pkg, packageName = pkg)
}
