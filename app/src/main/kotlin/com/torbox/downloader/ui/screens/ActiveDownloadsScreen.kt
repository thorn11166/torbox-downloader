package com.torbox.downloader.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.torbox.downloader.R
import com.torbox.downloader.data.db.DownloadEntity
import com.torbox.downloader.data.models.DownloadLocalStatus
import com.torbox.downloader.ui.state.DownloadUIState
import com.torbox.downloader.ui.viewmodel.DownloadViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ActiveDownloadsScreen(
    viewModel: DownloadViewModel,
    state: DownloadUIState
) {
    var showMagnetDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.downloads.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.no_active_downloads),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.downloads) { download ->
                        DownloadCard(download, viewModel)
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showMagnetDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Text("+")
        }

        if (showMagnetDialog) {
            MagnetLinkDialog(
                onDismiss = { showMagnetDialog = false },
                onAddMagnet = { link ->
                    viewModel.addMagnetLink(link)
                    showMagnetDialog = false
                }
            )
        }

        // Show error/message snackbar
        if (state.error != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.error, RoundedCornerShape(4.dp))
                    .padding(16.dp)
            ) {
                Text(state.error, color = MaterialTheme.colorScheme.onError)
            }
        }
    }
}

@Composable
fun DownloadCard(
    download: DownloadEntity,
    viewModel: DownloadViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        download.torrentName,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        formatFileSize(download.fileSize),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    download.status.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = getStatusColor(download.status)
                )
            }

            // Progress bar
            if (download.status == DownloadLocalStatus.DOWNLOADING.toString() ||
                download.status == DownloadLocalStatus.CHECKING.toString()
            ) {
                val progress = if (download.fileSize > 0) {
                    download.downloadedBytes.toFloat() / download.fileSize.toFloat()
                } else {
                    0f
                }
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${formatFileSize(download.downloadedBytes)} / ${formatFileSize(download.fileSize)}",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        "Speed: ${formatFileSize(download.downloadSpeed)}/s",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (download.status == DownloadLocalStatus.COMPLETED.toString()) {
                    Button(
                        onClick = { viewModel.downloadFile(download.torrentId, download.torrentName) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.download_complete))
                    }
                }
                Button(
                    onClick = { viewModel.deleteDownload(download.torrentId) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.delete))
                }
            }
        }
    }
}

@Composable
fun MagnetLinkDialog(
    onDismiss: () -> Unit,
    onAddMagnet: (String) -> Unit
) {
    var magnetInput by remember { mutableStateOf("") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.magnet_link_dialog_title)) },
        text = {
            Column {
                androidx.compose.material3.TextField(
                    value = magnetInput,
                    onValueChange = { magnetInput = it },
                    placeholder = { Text(stringResource(R.string.magnet_link_input_hint)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (magnetInput.isNotEmpty()) {
                        onAddMagnet(magnetInput)
                    }
                }
            ) {
                Text(stringResource(R.string.add_download))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun getStatusColor(status: String): androidx.compose.ui.graphics.Color {
    return when (status) {
        DownloadLocalStatus.COMPLETED.toString() -> MaterialTheme.colorScheme.primary
        DownloadLocalStatus.DOWNLOADING.toString() -> MaterialTheme.colorScheme.tertiary
        DownloadLocalStatus.FAILED.toString() -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outline
    }
}

fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        else -> "${bytes / (1024 * 1024 * 1024)} GB"
    }
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
