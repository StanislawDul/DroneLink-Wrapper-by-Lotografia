package com.dev.lotografia

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
  private val targetPackage = "com.dronelink.dronelink"

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      MaterialTheme {
        MainScreen (
          onStartOverlay = { startOverlay()}
        )
      }
    }
  }
  private fun startOverlay() {
    if (!Settings.canDrawOverlays(this)) {
      askForOverlayPermission()
    } else {
      val appInstalled = isAppInstalled(targetPackage)
      Log.i("Czy jest apka", "Znaleziono apke: $appInstalled")
      if (appInstalled) {
        launchApp(targetPackage)
        startService(Intent(this, BubbleService::class.java))
      } else {
        openPlayStore(targetPackage)
      }
    }
  }

  private fun launchApp(packageName: String) {
    val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
    if (launchIntent != null) {
      launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      startActivity(launchIntent)
    } else {
      Toast.makeText(this, "Nie można uruchomić aplikacji", Toast.LENGTH_SHORT).show()
    }
  }

  private fun isAppInstalled(packageName: String): Boolean {
    return try {
      packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
      true
    } catch (e: PackageManager.NameNotFoundException) {
      false
    }
  }

  private fun openPlayStore(packageName: String) {
    try {
      startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
    } catch (e: android.content.ActivityNotFoundException) {
      startActivity(
        Intent(
          Intent.ACTION_VIEW,
          Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
        )
      )
    }
  }

  private fun askForOverlayPermission() {
    val intent = Intent(
      Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
      Uri.parse("package:$packageName")
    )
    Toast.makeText(this, "Włącz uprawnienie 'Rysowanie nad innymi aplikacjami'", Toast.LENGTH_LONG)
      .show()
    startActivity(intent)
  }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onStartOverlay: () -> Unit) {
  Scaffold(
    topBar = { TopAppBar(title = { Text("Overlay Compose Demo") }) }
  ) { padding ->
    Button(
      onClick = onStartOverlay,
      modifier = androidx.compose.ui.Modifier
        .padding(padding)
        .padding(16.dp)
    ) {
      Text("Uruchom nakładkę")
    }
  }
}
