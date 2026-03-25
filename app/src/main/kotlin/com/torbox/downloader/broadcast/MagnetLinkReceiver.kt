package com.torbox.downloader.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class MagnetLinkReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("MagnetLinkReceiver", "Received intent with action: $action")

        if (action == Intent.ACTION_VIEW) {
            val uri = intent.data
            Log.d("MagnetLinkReceiver", "URI: $uri")

            if (uri != null) {
                val appIntent = Intent(context, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    data = uri
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(appIntent)
            }
        }
    }

    companion object {
        private const val TAG = "MagnetLinkReceiver"
    }
}

// MainActivity import path - update as needed
import com.torbox.downloader.ui.MainActivity
