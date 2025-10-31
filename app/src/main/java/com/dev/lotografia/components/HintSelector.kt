package com.dev.lotografia.components

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dev.lotografia.viewmodels.HintViewModel

@Preview
@Composable
fun HintSelector(
  modifier: Modifier = Modifier,
  viewModel: HintViewModel = HintViewModel(Application())
) {
  val hints by viewModel.categoryHints.collectAsState()
  val currentIndex by viewModel.currentIndex.collectAsState()
  val currentHint = hints.getOrNull(currentIndex)

  val textStyle = MaterialTheme.typography.labelLarge.copy(
    shadow = Shadow(
      color = Color.Gray,
      offset = Offset(1f, 1f),
      blurRadius = 4f
    )
  )
  Box(
    modifier = modifier
  ) {
    Box(
      modifier = modifier
        .matchParentSize()
        .blur(20.dp)
        .background(Color.White.copy(alpha = 0.3f))
    )
    Row(
      Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = {
        viewModel.prevCategory()
      }) {
        Icon(
          Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Wstecz",
          tint = Color.White
        )
      }

      Text(
        text = currentHint?.category ?: "Brak wskazówek",
        color = Color.White,
        style = textStyle
      )

      IconButton(onClick = {
        viewModel.nextCategory()
      }) {
        Icon(
          Icons.AutoMirrored.Filled.ArrowForward,
          contentDescription = "Dalej",
          tint = Color.White
        )
      }
    }
  }

}