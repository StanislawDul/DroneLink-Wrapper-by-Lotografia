package com.dev.lotografia

import android.app.Application
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import androidx.window.layout.WindowMetricsCalculator
import com.dev.lotografia.components.BubbleWithExpansion
import com.dev.lotografia.components.CloseZoneView
import com.dev.lotografia.viewmodels.HintViewModel
import kotlin.math.hypot

fun Dp.toPx(context: Context): Float =
  this.value * context.resources.displayMetrics.density

class BubbleService : LifecycleService(), ViewModelStoreOwner, SavedStateRegistryOwner {
  override val viewModelStore = ViewModelStore()
  private val savedStateRegistryController = SavedStateRegistryController.create(this)
  override val savedStateRegistry: SavedStateRegistry
    get() = savedStateRegistryController.savedStateRegistry
  private val lifecycleRegistry = LifecycleRegistry(this)
  private lateinit var windowManager: WindowManager
  private var bubbleView: ComposeView? = null
  private var closeView: ComposeView? = null

  private var isNearCloseZone = false

  override fun onCreate() {
    super.onCreate()
    savedStateRegistryController.performAttach()
    savedStateRegistryController.performRestore(null)
    lifecycleRegistry.currentState = Lifecycle.State.STARTED
    windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    createOverlay()
  }

  private fun createOverlay() {
    var paramsX = 0
    var paramsY = 200

    var isDragging = false

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

    val closeParams = WindowManager.LayoutParams(
      WindowManager.LayoutParams.WRAP_CONTENT,
      WindowManager.LayoutParams.WRAP_CONTENT,
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
      else
        WindowManager.LayoutParams.TYPE_PHONE,
      WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
      PixelFormat.TRANSLUCENT
    ).apply {
      gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
      y = 150
    }

    bubbleView = ComposeView(this).apply {
      setViewTreeLifecycleOwner(this@BubbleService)
      setViewTreeSavedStateRegistryOwner(this@BubbleService)
      setViewTreeViewModelStoreOwner(this@BubbleService)


      setContent {
        val screenSize = remember { mutableStateOf(Size(0f, 0f)) }
        val context = LocalContext.current
        val currentContext by rememberUpdatedState(context)
        val bubbleSize = 64.dp


        LaunchedEffect(currentContext) {
          val metrics = WindowMetricsCalculator.getOrCreate()
            .computeCurrentWindowMetrics(currentContext)
          val bounds = metrics.bounds
          screenSize.value = Size(bounds.width().toFloat(), bounds.height().toFloat())

          bubbleParams.x = screenSize.value.width.toInt() - bubbleSize.value.toInt()
          windowManager.updateViewLayout(this@apply, bubbleParams)
          paramsX = bubbleParams.x
          paramsY = bubbleParams.y
        }


        val viewModel: HintViewModel = viewModel(
          factory = ViewModelProvider.AndroidViewModelFactory(context.applicationContext as Application)
        )
        val density = LocalDensity.current
        var bubbleCenterX by remember { mutableFloatStateOf(0f) }
        var bubbleCenterY by remember { mutableFloatStateOf(0f) }

        BubbleWithExpansion(
          Modifier,
          viewModel,
          screenSize,
          onDrag = { dx, dy ->
            isDragging = true
            paramsX += dx.toInt()
            paramsY += dy.toInt()
            bubbleParams.x = paramsX
            bubbleParams.y = paramsY
            windowManager.updateViewLayout(this@apply, bubbleParams)

            with(density) {
              bubbleCenterX = (paramsX + (bubbleSize.toPx() / 2))
              bubbleCenterY = (paramsY + (bubbleSize.toPx() / 2))
            }

            // sprawdź czy blisko krzyżyka
            checkCloseProximity(screenSize, bubbleCenterX, bubbleCenterY)

          },
          onDragEnd = {
            // sprawdź po puszczeniu palca
            if (isNearCloseZone) {
              onDestroy()
            }
            isDragging = false
          },
          setPosition = {
            bubbleParams.y = (screenSize.value.height * 0.2f).toInt()
            if(bubbleParams.x < (screenSize.value.width * 0.5f).toInt())
              bubbleParams.x = 0
            else if(bubbleParams.x > (screenSize.value.width * 0.5f).toInt())
              bubbleParams.x = screenSize.value.width.toInt() - bubbleSize.value.toInt()
            windowManager.updateViewLayout(this@apply, bubbleParams)
            paramsX = bubbleParams.x
            paramsY = bubbleParams.y
          }
        )
      }
    }

    closeView = ComposeView(this).apply {
      setViewTreeLifecycleOwner(this@BubbleService)
      setViewTreeSavedStateRegistryOwner(this@BubbleService)
      setViewTreeViewModelStoreOwner(this@BubbleService)
      setContent {
        CloseZoneView()
      }
      alpha = 0f
    }

    windowManager.addView(bubbleView, bubbleParams)
    windowManager.addView(closeView, closeParams)

    val handler = Handler(Looper.getMainLooper())
    handler.postDelayed(object : Runnable {
      override fun run() {
        closeView?.alpha = if (isDragging) 1f else 0f
        isDragging = false
        handler.postDelayed(this, 300)
      }
    }, 300)
  }

  override fun onDestroy() {
    super.onDestroy()
    bubbleView?.let { windowManager.removeView(it) }
    closeView?.let { windowManager.removeView(it) }
    viewModelStore.clear()
  }

  private fun checkCloseProximity(
    screenSize: MutableState<Size>,
    bubbleCenterX: Float,
    bubbleCenterY: Float,
  ) {
    val screenWidth = screenSize.value.width
    val screenHeight = screenSize.value.height
    val closeCenterX = (screenWidth / 2)
    val closeCenterY = screenHeight - 150.dp.toPx(this)

    val distance = hypot(bubbleCenterX - closeCenterX, bubbleCenterY - closeCenterY)

    val maxDistance = 100f
    when {
      distance < maxDistance -> {
        isNearCloseZone = true
        val pullStrength = (maxDistance - distance) / maxDistance
        val moveX = (closeCenterX - bubbleCenterX) * 0.15f * pullStrength
        val moveY = (closeCenterY - bubbleCenterY) * 0.15f * pullStrength

        val params = (bubbleView?.layoutParams as? WindowManager.LayoutParams)
        if (params != null) {
          params.x += moveX.toInt()
          params.y += moveY.toInt()
          windowManager.updateViewLayout(bubbleView, params)
        }
      }

      else -> {
        isNearCloseZone = false
      }
    }

    closeView?.setContent {
      CloseZoneView(isActive = isNearCloseZone)
    }
  }
}