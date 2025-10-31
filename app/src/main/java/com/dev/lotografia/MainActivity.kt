package com.dev.lotografia

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import com.dev.lotografia.components.DronelinkNotInstalledView

class MainActivity : ComponentActivity() {
  private val targetPackage = "com.dronelink.dronelink"

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
      setContent {
        MaterialTheme {
          MainScreen(
            Modifier,
            redirectToStore = { openPlayStore(targetPackage) }
          )
        }
      }
  }

  override fun onResume() {
    super.onResume()
    checkIfAppInstalledAndHandle()
  }

  private fun checkIfAppInstalledAndHandle() {
    if (isAppInstalled(targetPackage)) {
      finishAndRemoveTask()
      startOverlay()
    }
  }

  private fun startOverlay() {
    if (!Settings.canDrawOverlays(this)) {
      askForOverlayPermission()
    } else {
      launchApp(targetPackage)
      startService(Intent(this, BubbleService::class.java))
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
      Log.i("Czy jest apka", "Znaleziono dronelink")
      true
    } catch (e: PackageManager.NameNotFoundException) {
      Log.i("Czy jest apka", "Nie znaleziono dronelink")
      false
    }
  }

  private fun openPlayStore(packageName: String) {
    try {
      startActivity(Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri()))
    } catch (_: android.content.ActivityNotFoundException) {
      startActivity(
        Intent(
          Intent.ACTION_VIEW,
          "https://play.google.com/store/apps/details?id=$packageName".toUri()
        )
      )
    }
  }

  private fun askForOverlayPermission() {
    val intent = Intent(
      Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
      "package:$packageName".toUri()
    )
    Toast.makeText(this, "Włącz uprawnienie 'Rysowanie nad innymi aplikacjami'", Toast.LENGTH_LONG)
      .show()
    startActivity(intent)
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(modifier: Modifier, redirectToStore: () -> Unit) {
  Scaffold(
    topBar = { TopAppBar(title = { Text("Lotografia") }) }
  ) { padding ->
    Box(
      modifier = modifier
        .fillMaxSize()
        .padding(paddingValues = padding),
      contentAlignment = Alignment.Center
    ) {
      DronelinkNotInstalledView(modifier, redirectToStore)
    }
  }
}
