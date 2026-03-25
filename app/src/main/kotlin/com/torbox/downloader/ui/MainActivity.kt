package com.torbox.downloader.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.torbox.downloader.R
import com.torbox.downloader.ui.screens.ActiveDownloadsScreen
import com.torbox.downloader.ui.screens.DownloadHistoryScreen
import com.torbox.downloader.ui.screens.SettingsScreen
import com.torbox.downloader.ui.theme.TorBoxDownloaderTheme
import com.torbox.downloader.ui.viewmodel.DownloadViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: DownloadViewModel by viewModels()
    private val tag = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        handleIncomingIntent(intent)

        setContent {
            TorBoxDownloaderTheme {
                MainScreen(viewModel) { newIntent ->
                    handleIncomingIntent(newIntent)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent) {
        val action = intent.action
        Log.d(tag, "Handling intent with action: $action")

        when {
            action == Intent.ACTION_VIEW -> {
                val uri = intent.data
                Log.d(tag, "URI: $uri")
                
                when {
                    uri?.scheme == "magnet" -> {
                        val magnetLink = uri.toString()
                        Log.d(tag, "Intercepted magnet link: $magnetLink")
                        viewModel.addMagnetLink(magnetLink)
                    }
                    uri?.scheme == "content" || uri?.scheme == "file" -> {
                        val mimeType = intent.type
                        if (mimeType == "application/x-torrent" || uri?.path?.endsWith(".torrent") == true) {
                            Log.d(tag, "Intercepted torrent file: $uri")
                            viewModel.addTorrentUrl(uri.toString())
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    viewModel: DownloadViewModel,
    onIntentReceived: (Intent) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    label = { Text(stringResource(R.string.active_downloads)) },
                    icon = { }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    label = { Text(stringResource(R.string.download_history)) },
                    icon = { }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    label = { Text(stringResource(R.string.settings)) },
                    icon = { }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> {
                    val state by viewModel.downloadUIState.collectAsStateWithLifecycle()
                    ActiveDownloadsScreen(viewModel, state)
                }
                1 -> {
                    val state by viewModel.historyUIState.collectAsStateWithLifecycle()
                    DownloadHistoryScreen(viewModel, state)
                }
                2 -> {
                    val state by viewModel.settingsUIState.collectAsStateWithLifecycle()
                    SettingsScreen(viewModel, state)
                }
            }
        }
    }
}
