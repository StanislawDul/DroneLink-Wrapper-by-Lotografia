package com.dev.lotografia

import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

class BubbleService : LifecycleService(), ViewModelStoreOwner, SavedStateRegistryOwner {
  override val viewModelStore = ViewModelStore()
  private val savedStateRegistryController = SavedStateRegistryController.create(this)
  override val savedStateRegistry: SavedStateRegistry
    get() = savedStateRegistryController.savedStateRegistry

  override fun onCreate() {
    super.onCreate()
    savedStateRegistryController.performAttach()
    savedStateRegistryController.performRestore(Bundle())
    lifecycleRegistry.currentState = Lifecycle.State.STARTED
    createOverlay()
  }

  private lateinit var windowManager: WindowManager
  private var bubbleView: ComposeView? = null
  private var expandedView: ComposeView? = null
  private val lifecycleRegistry = LifecycleRegistry(this)
  private fun createOverlay() {
    windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

    var paramsX = 200
    var paramsY = 400

    val bubbleParams = WindowManager.LayoutParams(
      WindowManager.LayoutParams.WRAP_CONTENT,
      WindowManager.LayoutParams.WRAP_CONTENT,
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
      else
        WindowManager.LayoutParams.TYPE_PHONE,
      WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
      PixelFormat.TRANSLUCENT
    ).apply {
      gravity = Gravity.TOP or Gravity.START
      x = paramsX
      y = paramsY
    }

    bubbleView = ComposeView(this).apply {
      setViewTreeLifecycleOwner(this@BubbleService)
      setViewTreeSavedStateRegistryOwner(this@BubbleService)
      setViewTreeViewModelStoreOwner(this@BubbleService)
      setContent {
        BubbleIcon(
          onClick = { toggleExpanded(paramsX, paramsY)},
          onDrag = {dx, dy ->
            paramsX += dx.toInt()
            paramsY += dy.toInt()
            bubbleParams.x = paramsX
            bubbleParams.y = paramsY
            windowManager.updateViewLayout(this@apply, bubbleParams)
          }
        )
      }
    }
    windowManager.addView(bubbleView, bubbleParams)
  }

  private fun toggleExpanded(x: Int, y: Int) {
    if (expandedView != null) {
      // zamknij
      windowManager.removeView(expandedView)
      expandedView = null
    } else {
      // otwórz
      val params = WindowManager.LayoutParams(
        (300 * resources.displayMetrics.density).toInt(),
        (400 * resources.displayMetrics.density).toInt(),
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
          WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
          WindowManager.LayoutParams.TYPE_PHONE,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT
      ).apply {
        gravity = Gravity.TOP or Gravity.START
        this.x = x + 120
        this.y = y
      }

      expandedView = ComposeView(this).apply {
        setViewTreeLifecycleOwner(this@BubbleService)
        setViewTreeSavedStateRegistryOwner(this@BubbleService)
        setViewTreeViewModelStoreOwner(this@BubbleService)
        setContent {
          ExpandedBubble(
            onClose = {
              windowManager.removeView(this)
              expandedView = null
            }
          )
        }
      }
      windowManager.addView(expandedView, params)
    }
  }
  override fun onDestroy() {
    super.onDestroy()
    bubbleView?.let { windowManager.removeView(it) }
    expandedView?.let { windowManager.removeView(it) }
  }
//  override fun onBind(p0: Intent?): IBinder? = null
}

@Composable
fun BubbleIcon(onClick: () -> Unit, onDrag: (Float, Float) -> Unit) {
  var offsetX by remember { mutableFloatStateOf(0f) }
  var offsetY by remember { mutableFloatStateOf(0f) }

  Box(
    modifier = Modifier
      .size(80.dp)
      .background(Color(0xFF3F51B5), CircleShape)
      .border(2.dp, Color.White, CircleShape)
      .pointerInput(Unit) {
        detectDragGestures(
          onDrag = { change, dragAmount ->
            change.consume()
            offsetX += dragAmount.x
            offsetY += dragAmount.y
            onDrag(dragAmount.x, dragAmount.y)
          },
          onDragEnd = {}
        )
      }.clickable { onClick() },
    contentAlignment = Alignment.Center
  ) {
    Text("💬", fontSize = MaterialTheme.typography.headlineMedium.fontSize)
  }
}

@Composable
fun ExpandedBubble(onClose: () -> Unit) {
  Surface(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xEE222222)),
    color = Color.Transparent,
    shape = MaterialTheme.shapes.medium
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text("Mini okno", color = Color.White)
      Spacer(Modifier.height(12.dp))
      Button(onClick = onClose) {
        Text("Zamknij")
      }
    }
  }
}