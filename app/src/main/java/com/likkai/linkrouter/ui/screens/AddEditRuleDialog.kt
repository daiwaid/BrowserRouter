package com.likkai.linkrouter.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import com.likkai.linkrouter.ui.components.BrowserIcon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.likkai.linkrouter.browser.BrowserApp
import com.likkai.linkrouter.data.BrowserRule
import com.likkai.linkrouter.data.MatchType
import com.likkai.linkrouter.engine.editorPattern

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRuleDialog(
    editingRule: BrowserRule?,
    installedBrowsers: List<BrowserApp>,
    installedApps: List<BrowserApp>,
    onDismiss: () -> Unit,
    onSave: (String, BrowserApp, BrowserApp?) -> Unit,
    onUpdate: (BrowserRule) -> Unit
) {
    var pattern by remember {
        mutableStateOf(
            editingRule?.let { editorPattern(it.matchType, it.pattern) } ?: ""
        )
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
    val canSave = selectedBrowser != null && (pattern.isNotBlank() || sourceApp != null)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    if (isEditing) "Edit Rule" else "Add Rule",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "1. add a URL pattern and/or choose an app where the links are opened from\n" +
                        "2. select the browser to open links matching this rule with",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                val patternInteraction = remember { MutableInteractionSource() }
                val outline = MaterialTheme.colorScheme.outline
                val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                val fieldColors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = outline,
                    unfocusedBorderColor = outline,
                    focusedLabelColor = labelColor,
                    unfocusedLabelColor = labelColor
                )
                // The label stays on the border, which needs a focused state. That
                // state also thickens the stroke, so the outline is pinned to the
                // same weight as the other fields.
                BasicTextField(
                    value = pattern,
                    onValueChange = { pattern = it },
                    singleLine = true,
                    interactionSource = patternInteraction,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        autoCorrectEnabled = false
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(
                            minWidth = OutlinedTextFieldDefaults.MinWidth,
                            minHeight = OutlinedTextFieldDefaults.MinHeight
                        ),
                    decorationBox = { innerTextField ->
                        OutlinedTextFieldDefaults.DecorationBox(
                            value = pattern,
                            innerTextField = innerTextField,
                            enabled = true,
                            singleLine = true,
                            visualTransformation = VisualTransformation.None,
                            interactionSource = patternInteraction,
                            label = { Text("URL pattern") },
                            placeholder = { Text("*") },
                            supportingText = {
                                Text(
                                    "*.domain.com/* matches domain.com and its subdomains, on any path, " +
                                        "domain.com/topic/* matches every page under that path, " +
                                        "and domain.com/page matches that page only"
                                )
                            },
                            colors = fieldColors,
                            container = {
                                OutlinedTextFieldDefaults.Container(
                                    enabled = true,
                                    isError = false,
                                    interactionSource = patternInteraction,
                                    colors = fieldColors,
                                    focusedBorderThickness = OutlinedTextFieldDefaults.UnfocusedBorderThickness,
                                    unfocusedBorderThickness = OutlinedTextFieldDefaults.UnfocusedBorderThickness
                                )
                            }
                        )
                    }
                )
                LaunchedEffect(patternInteraction) {
                    patternInteraction.emit(FocusInteraction.Focus())
                }

                InstalledAppDropdown(
                    apps = installedApps,
                    selected = sourceApp,
                    allowAny = true,
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
                    val savedPattern = pattern.trim()
                    val savedSource = sourceApp
                    if (savedPattern.isEmpty() && savedSource == null) return@Button
                    val existing = editingRule
                    if (existing != null) {
                        onUpdate(
                            existing.copy(
                                matchType = MatchType.PATTERN,
                                pattern = savedPattern,
                                targetBrowserPackage = browser.packageName,
                                targetBrowserLabel = browser.label,
                                sourcePackage = savedSource?.packageName,
                                sourceLabel = savedSource?.label
                            )
                        )
                    } else {
                        onSave(savedPattern, browser, savedSource)
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
    var query by remember { mutableStateOf("") }
    var anchorSize by remember { mutableStateOf(IntSize.Zero) }
    val filtered = remember(apps, query) {
        if (query.isBlank()) {
            apps
        } else {
            apps.filter { app ->
                app.label.contains(query, ignoreCase = true) ||
                    app.packageName.contains(query, ignoreCase = true)
            }
        }
    }
    val showAny = allowAny && query.isBlank()

    fun dismiss() {
        expanded = false
        query = ""
    }

    // A material dropdown measures its children intrinsically, which crashes on
    // lazy lists and text fields. This popup does not, so search and the app list
    // can both live in the menu.
    Box(Modifier.fillMaxWidth()) {
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
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { anchorSize = it.size }
        )
        if (!expanded) {
            Box(
                Modifier
                    .matchParentSize()
                    .clickable { expanded = true }
            )
        }
        if (expanded && anchorSize != IntSize.Zero) {
            val menuWidth = with(LocalDensity.current) { anchorSize.width.toDp() }
            Popup(
                alignment = Alignment.TopStart,
                offset = IntOffset(0, anchorSize.height),
                onDismissRequest = { dismiss() },
                properties = PopupProperties(
                    focusable = true,
                    clippingEnabled = false
                )
            ) {
                Surface(
                    modifier = Modifier
                        .width(menuWidth)
                        .heightIn(max = 360.dp),
                    shape = MaterialTheme.shapes.extraSmall,
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 6.dp,
                    tonalElevation = 3.dp
                ) {
                    Column {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            singleLine = true,
                            placeholder = { Text("Search apps") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null)
                            },
                            keyboardOptions = KeyboardOptions(autoCorrectEnabled = false),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                        LazyColumn(Modifier.height(280.dp).fillMaxWidth()) {
                            if (showAny) {
                                item(key = "any") {
                                    DropdownMenuItem(
                                        text = { Text("Any app") },
                                        leadingIcon = {
                                            Icon(Icons.Default.Apps, contentDescription = null)
                                        },
                                        onClick = {
                                            onSelect(null)
                                            dismiss()
                                        }
                                    )
                                }
                            }
                            if (apps.isEmpty()) {
                                item(key = "none") {
                                    DropdownMenuItem(
                                        text = { Text("No apps installed") },
                                        onClick = {},
                                        enabled = false
                                    )
                                }
                            } else if (filtered.isEmpty()) {
                                item(key = "no-match") {
                                    DropdownMenuItem(
                                        text = { Text("No matching apps") },
                                        onClick = {},
                                        enabled = false
                                    )
                                }
                            }
                            items(filtered, key = { it.packageName }) { app ->
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
                                        dismiss()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun BrowserRule.toSourceApp(installedApps: List<BrowserApp>): BrowserApp? {
    val pkg = sourcePackage ?: return null
    return installedApps.find { it.packageName == pkg }
        ?: BrowserApp(label = sourceLabel ?: pkg, packageName = pkg)
}
