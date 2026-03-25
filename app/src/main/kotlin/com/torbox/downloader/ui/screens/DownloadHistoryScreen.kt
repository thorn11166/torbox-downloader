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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.torbox.downloader.R
import com.torbox.downloader.data.db.DownloadHistoryEntity
import com.torbox.downloader.ui.state.HistoryUIState
import com.torbox.downloader.ui.viewmodel.DownloadViewModel

@Composable
fun DownloadHistoryScreen(
    viewModel: DownloadViewModel,
    state: HistoryUIState
) {
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                if (state.history.isNotEmpty()) {
                    Button(onClick = { viewModel.clearHistory() }) {
                        Text(stringResource(R.string.clear_history))
                    }
                }
            }

            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.history.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.no_history),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.history) { historyItem ->
                        HistoryCard(historyItem)
                    }
                }
            }
        }

        // Show error/message snackbar
        if (state.error != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.error)
                    .padding(16.dp)
            ) {
                Text(state.error, color = MaterialTheme.colorScheme.onError)
            }
        }
    }
}

@Composable
fun HistoryCard(
    history: DownloadHistoryEntity
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
                        history.torrentName,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        formatFileSize(history.fileSize),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    history.status.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                "${stringResource(R.string.added_date)}: ${formatDate(history.addedDate)}",
                style = MaterialTheme.typography.labelSmall
            )

            if (history.downloadedDate != null) {
                Text(
                    "Downloaded: ${formatDate(history.downloadedDate)}",
                    style = MaterialTheme.typography.labelSmall
                )
            }

            if (!history.downloadPath.isNullOrEmpty()) {
                Text(
                    history.downloadPath,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
