package com.dev.lotografia.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.dev.lotografia.R
import com.dev.lotografia.viewmodels.HintViewModel

@Composable
fun BubbleWithExpansion(
  modifier: Modifier,
  viewModel: HintViewModel,
  screenSize: MutableState<Size>,
  onDrag: (Float, Float) -> Unit,
  onDragEnd: () -> Unit,
  setPosition: () -> Unit
) {
  val screenWidth = screenSize.value.width.toInt()
  val screenHeight = screenSize.value.height.toInt()
  val partScreenWidth = (screenWidth * 0.3).toInt()

  val ratio = if (screenHeight == 0 || screenWidth == 0) 1
    else if (screenHeight >= screenWidth) screenHeight / screenWidth else screenWidth / screenHeight

  var expanded by remember { mutableStateOf(false) }

  // animowana szerokość prostokąta
  val width by animateDpAsState(targetValue = if (expanded) partScreenWidth.dp else 48.dp)
  val height by animateDpAsState(
    targetValue = if (expanded) {
      if (ratio >= 1.9)
        (screenHeight * 0.2).dp
      else
        (screenHeight * 0.4).dp
    } else 48.dp
  )

  val cornerRadius by animateDpAsState(targetValue = if (expanded) 16.dp else 32.dp)
  val logoSize by animateDpAsState(targetValue = if (expanded) 42.dp else 48.dp)

  Box(
    modifier = modifier
      .wrapContentSize()
      .pointerInput(Unit) {
        detectDragGestures(
          onDrag = { change, dragAmount ->
            if (!expanded) {
              change.consume()
              onDrag(dragAmount.x, dragAmount.y)
            }
          },
          onDragEnd = {
            if (!expanded) {
              onDragEnd()
            }
          }
        )
      }
      .clickable {
        expanded = !expanded
        if (expanded)
          setPosition()
      }
      .clip(RoundedCornerShape(cornerRadius))
      .background(Color.Transparent)
      .width(width)
//      .heightIn(0.dp, halfScreenHeight.dp)
      .height(height.value.dp)
  ) {
    Column(
      horizontalAlignment = Alignment.Start,
      verticalArrangement = Arrangement.Top,
      modifier = Modifier
        .wrapContentHeight()
        .fillMaxHeight()
    )
    {
      if (!expanded)
        Image(
          painter = painterResource(id = R.drawable.lotografia_logotype_no_text),
          contentDescription = null,
          modifier = Modifier
            .size(logoSize)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.9f))
            .padding(4.dp)
        )

      if (expanded) {
        AnimatedVisibility(
          visible = expanded,
          enter = slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
          ),
          exit = slideOutHorizontally(
            targetOffsetX = { -it },
            animationSpec = tween(durationMillis = 200)
          )
        ) {
          Column {
            Row {
              Image(
                painter = painterResource(id = R.drawable.lotografia_logotype_no_text),
                contentDescription = null,
                modifier = Modifier
                  .size(logoSize)
                  .clip(CircleShape)
                  .background(Color.White.copy(alpha = 0.9f))
                  .padding(1.dp)
              )
              HintSelector(
                viewModel = viewModel, modifier = Modifier
                  .width((width - logoSize))
                  .clip(RoundedCornerShape(16.dp))
                  .padding(2.dp, 0.dp, 0.dp, 0.dp)
              )
            }

            GlassyPanel(
              modifier = Modifier.fillMaxHeight(),
              viewModel = viewModel,
              width,
              height,
              onClose = { expanded = false }
            )
          }
        }
      }
    }
  }
}