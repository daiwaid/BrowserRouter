package com.likkai.linkrouter.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.likkai.linkrouter.browser.BrowserApp
import com.likkai.linkrouter.ui.components.BrowserIcon

@Composable
fun BrowserPickerDialog(
    browsers: List<BrowserApp>,
    selectedPackage: String?,
    onSelect: (BrowserApp) -> Unit,
    onDismiss: () -> Unit,
    title: String = "Select Browser",
    emptyMessage: String = "No browsers found on this device.",
    showSearch: Boolean = false,
    anyLabel: String? = null,
    onSelectAny: (() -> Unit)? = null
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(browsers, query) {
        if (query.isBlank()) {
            browsers
        } else {
            browsers.filter {
                it.label.contains(query, ignoreCase = true) ||
                    it.packageName.contains(query, ignoreCase = true)
            }
        }
    }
    val selectAny = onSelectAny
    val anyText = anyLabel
    val showAny = selectAny != null && anyText != null && query.isBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(title, fontWeight = FontWeight.Bold)
        },
        text = {
            if (browsers.isEmpty() && !showAny) {
                Text(
                    emptyMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (showSearch) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                autoCorrectEnabled = false
                            ),
                            placeholder = { Text("Search apps") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                    }
                    if (filtered.isEmpty() && !showAny) {
                        Text(
                            "No matching apps",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            tonalElevation = 2.dp
                        ) {
                            LazyColumn(
                                modifier = if (showSearch) {
                                    Modifier.heightIn(max = 360.dp)
                                } else {
                                    Modifier
                                }
                            ) {
                                if (selectAny != null && anyText != null && query.isBlank()) {
                                    item(key = "any") {
                                        PickerRow(
                                            label = anyText,
                                            packageName = null,
                                            selected = selectedPackage == null,
                                            onClick = selectAny
                                        )
                                    }
                                }
                                items(filtered, key = { it.packageName }) { browser ->
                                    PickerRow(
                                        label = browser.label,
                                        packageName = browser.packageName,
                                        selected = browser.packageName == selectedPackage,
                                        onClick = { onSelect(browser) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun PickerRow(
    label: String,
    packageName: String?,
    selected: Boolean,
    onClick: () -> Unit
) {
    val name = packageName
    ListItem(
        headlineContent = {
            Text(
                label,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        },
        supportingContent = if (name == null) {
            null
        } else {
            {
                Text(
                    name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        leadingContent = if (name == null) {
            null
        } else {
            {
                BrowserIcon(
                    packageName = name,
                    size = 32.dp
                )
            }
        },
        trailingContent = {
            if (selected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}
