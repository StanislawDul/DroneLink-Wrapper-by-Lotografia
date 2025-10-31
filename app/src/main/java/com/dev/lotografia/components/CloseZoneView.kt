package com.dev.lotografia.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun CloseZoneView(isActive: Boolean = false) {

  val scale by animateFloatAsState(
    targetValue = if (isActive) 1f else 0.7f,
    animationSpec = tween(durationMillis = 150)
  )

  val bgColor by animateColorAsState(
    targetValue = if (isActive)
      Color.Red.copy(alpha = 0.6f)
    else
      Color.Black.copy(alpha = 0.4f),
    animationSpec = tween(durationMillis = 150)
  )

  Box(
    modifier = Modifier
      .graphicsLayer(scaleX = scale, scaleY = scale)
      .size(60.dp.times(scale))
      .clip(CircleShape)
      .background(bgColor)
      .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape)
      .padding(2.dp),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      Icons.Default.Close,
      contentDescription = "Zamknij",
      tint = Color.White.copy(alpha = 0.9f),
      modifier = Modifier.size(28.dp)
    )
  }
}
