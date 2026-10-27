package com.likkai.linkrouter.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
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
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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

                InstalledAppDropdown(
                    apps = installedApps,
                    selected = sourceApp,
                    allowAny = matchType != MatchType.SOURCE_APP,
                    onSelect = { sourceApp = it }
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

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InstalledAppDropdown(
    apps: List<BrowserApp>,
    selected: BrowserApp?,
    allowAny: Boolean,
    onSelect: (BrowserApp?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selected?.label ?: if (allowAny) "Any app" else "",
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text("Opened from") },
            placeholder = { Text("Select an app") },
            leadingIcon = {
                if (selected != null) {
                    BrowserIcon(packageName = selected.packageName, size = 24.dp)
                } else {
                    Icon(
                        Icons.Default.Apps,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            supportingText = {
                Text(
                    when {
                        selected != null -> selected.packageName
                        allowAny -> "Optional. Choose an installed app, or leave as Any app."
                        else -> "Every link opened from the selected app"
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 320.dp)
        ) {
            if (allowAny) {
                DropdownMenuItem(
                    text = { Text("Any app") },
                    leadingIcon = {
                        Icon(Icons.Default.Apps, contentDescription = null)
                    },
                    onClick = {
                        onSelect(null)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
            if (apps.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No apps installed") },
                    onClick = {},
                    enabled = false,
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
            apps.forEach { app ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                app.label,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                app.packageName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    leadingIcon = {
                        BrowserIcon(packageName = app.packageName, size = 24.dp)
                    },
                    onClick = {
                        onSelect(app)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

private fun BrowserRule.toSourceApp(installedApps: List<BrowserApp>): BrowserApp? {
    val pkg = sourcePackage ?: return null
    return installedApps.find { it.packageName == pkg }
        ?: BrowserApp(label = sourceLabel ?: pkg, packageName = pkg)
}
