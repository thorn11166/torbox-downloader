package com.torbox.downloader.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.torbox.downloader.R
import com.torbox.downloader.ui.state.SettingsUIState
import com.torbox.downloader.ui.viewmodel.DownloadViewModel

@Composable
fun SettingsScreen(
    viewModel: DownloadViewModel,
    state: SettingsUIState
) {
    var apiKey by remember { mutableStateOf(state.apiKey) }
    var downloadFolder by remember { mutableStateOf(state.downloadFolder) }
    var autoDelete by remember { mutableStateOf(state.autoDelete) }
    var notificationsEnabled by remember { mutableStateOf(state.notificationsEnabled) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                stringResource(R.string.settings),
                style = MaterialTheme.typography.headlineSmall
            )

            // API Key Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(R.string.api_key),
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    placeholder = { Text(stringResource(R.string.api_key_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
            }

            // Download Folder Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(R.string.download_folder),
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = downloadFolder,
                    onValueChange = { downloadFolder = it },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    singleLine = true
                )
                Button(
                    onClick = { /* TODO: Implement folder picker */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.select_folder))
                }
            }

            // Auto-delete Checkbox
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.auto_delete_completed),
                    style = MaterialTheme.typography.bodyMedium
                )
                Checkbox(
                    checked = autoDelete,
                    onCheckedChange = { autoDelete = it }
                )
            }

            // Notifications Checkbox
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.notifications_enabled),
                    style = MaterialTheme.typography.bodyMedium
                )
                Checkbox(
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
            }

            // Save Button
            Button(
                onClick = {
                    viewModel.saveSettings(
                        apiKey,
                        downloadFolder,
                        autoDelete,
                        notificationsEnabled
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(end = 8.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Text(stringResource(R.string.save_settings))
            }

            // Status Messages
            if (state.error != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.error)
                        .padding(12.dp)
                ) {
                    Text(
                        state.error,
                        color = MaterialTheme.colorScheme.onError,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (state.message != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(12.dp)
                ) {
                    Text(
                        state.message,
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
