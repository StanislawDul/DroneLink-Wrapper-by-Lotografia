package com.dev.lotografia.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dev.lotografia.viewmodels.HintViewModel


@Composable
fun GlassyPanel(
  modifier: Modifier,
  viewModel: HintViewModel,
  width: Dp = 300.dp,
  height: Dp,
  onClose: () -> Unit = {},
) {
  val descriptionTextStyle = MaterialTheme.typography.labelLarge.copy(
    shadow = Shadow(
      color = Color.Black,
      offset = Offset(1f, 1f),
      blurRadius = 4f
    )
  )
  val titleTextStyle = MaterialTheme.typography.labelLarge.copy(
    fontSize = descriptionTextStyle.fontSize * 1.5,
    shadow = Shadow(
      color = Color.Black,
      offset = Offset(1f, 1f),
      blurRadius = 4f
    )
  )

  val hints by viewModel.categoryHints.collectAsState()
  val currentIndex by viewModel.currentIndex.collectAsState()
  val currentHint = hints.getOrNull(currentIndex)

  Row(
    modifier = modifier
      .wrapContentSize()
      .width(width)
      .wrapContentHeight()
  ) {
    Box(
      modifier = modifier
        .wrapContentSize()
        .height(height)
        .width(width)
        .clip(RoundedCornerShape(16.dp))
    ) {
      // 1 Tło blurujące otoczenie (ale nie zawartość)
      Box(
        modifier = modifier
          .matchParentSize()
          .blur(20.dp)
          .background(Color.White.copy(alpha = 0.2f))
      )

      // 2 Ostra zawartość UI
      Column(
        modifier
          .matchParentSize()
          .padding(horizontal = 4.dp, vertical = 4.dp)
          .clip(RoundedCornerShape(12.dp)),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = modifier
            .wrapContentHeight()
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.2f)),
          contentAlignment = Alignment.Center
        ) {
          LazyColumn() {
            items(currentHint?.hints ?: emptyList()) { item ->
              Text(
                modifier = modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                text = item.title,
                color = Color.White,
                style = titleTextStyle
              )
              Text(
                modifier = modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                text = item.content,
                color = Color.White,
                style = descriptionTextStyle
              )
            }
          }
        }
      }
    }
  }
}
