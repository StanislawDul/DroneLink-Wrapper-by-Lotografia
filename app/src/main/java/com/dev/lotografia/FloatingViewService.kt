package com.dev.lotografia

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast

class FloatingViewService : Service() {
  override fun onBind(p0: Intent?): IBinder? = null

  private lateinit var windowManager: WindowManager
  private var floatingView: View? = null

  override fun onCreate() {
    super.onCreate()
    windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

    val inflater = LayoutInflater.from(this)
    floatingView = inflater.inflate(R.layout.floating_view, null)

    val params = WindowManager.LayoutParams(
      WindowManager.LayoutParams.WRAP_CONTENT,
      WindowManager.LayoutParams.WRAP_CONTENT,
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
      } else {
        WindowManager.LayoutParams.TYPE_PHONE
      },
      WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
      PixelFormat.TRANSLUCENT
    )

    params.gravity = Gravity.TOP or Gravity.START
    params.x = 100
    params.y = 300

    windowManager.addView(floatingView, params)

    val closeBtn = floatingView!!.findViewById<Button>(R.id.btn_close)
    closeBtn.setOnClickListener {
      Toast.makeText(this, "Nakładka zamknięta", Toast.LENGTH_SHORT).show()
      stopSelf()
    }

    floatingView!!.setOnTouchListener(
      object : View.OnTouchListener {
        private var initialX = 0
        private var initialY = 0
        private var initialTouchX = 0f
        private var initialTouchY = 0f

        override fun onTouch(p0: View?, p1: MotionEvent): Boolean {
          when (p1.action) {
            MotionEvent.ACTION_DOWN -> {
              initialX = params.x
              initialY = params.y
              initialTouchX = p1.rawX
              initialTouchY = p1.rawY
              return true
            }

            MotionEvent.ACTION_MOVE -> {
              params.x = initialX + (p1.rawX - initialTouchX).toInt()
              params.y = initialY + (p1.rawY - initialTouchY).toInt()
              windowManager.updateViewLayout(floatingView, params)
              return true
            }
          }
          return false
        }
      }
    )
  }

  override fun onDestroy() {
    super.onDestroy()
    floatingView?.let { windowManager.removeView(it) }
  }
}