package com.dev.lotografia.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun DronelinkNotInstalledView(modifier: Modifier = Modifier, redirectToStore: () -> Unit) {
  Column(
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Row(
      modifier = modifier
    ) {
      Text(text = "Nie wykryto zainstalowanej aplikacji ", color = Color.Black)
      Text(text = "dronelink", fontWeight = FontWeight.Bold, color = Color.Black)
    }
    Spacer(modifier.height(16.dp))
    Button(
      onClick = redirectToStore,
      modifier = modifier,
      colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
    ) {
      Text("Otwórz dronelink w sklepie play")
    }
  }
}