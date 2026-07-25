package com.tcanvas.timecanvas.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.core.app.NotificationCompat
import com.skydoves.cloudy.CloudyView
import com.tcanvas.timecanvas.R
import com.tcanvas.timecanvas.render.fms.core.FmsEngine

class FloatRecordService : Service() {
    private lateinit var windowManager: WindowManager
    private var floatLayout: View? = null
    private var menuPanel: LinearLayout? = null
    private var layoutParams = WindowManager.LayoutParams()

    private var originalX = 0
    private var originalY = 0
    private var touchStartX = 0f
    private var touchStartY = 0f
    private var isRecordingTrack = false

    companion object {
        const val CHANNEL_ID = "FloatRecordChannel"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1001, buildNotification())
        initFloatWindow()
    }

    private fun initFloatWindow() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        floatLayout = LayoutInflater.from(this).inflate(R.layout.float_glass_ball, null)
        menuPanel = floatLayout?.findViewById(R.id.float_menu_panel)
        val floatBall = floatLayout?.findViewById<CloudyView>(R.id.glass_float_ball)

        layoutParams.apply {
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            format = PixelFormat.TRANSLUCENT
            width = 75
            height = 75
            gravity = Gravity.START or Gravity.TOP
            x = 30
            y = 450
        }

        windowManager.addView(floatLayout, layoutParams)

        floatBall?.setOnTouchListener { _, event ->
            val rawX = event.rawX
            val rawY = event.rawY
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    originalX = layoutParams.x
                    originalY = layoutParams.y
                    touchStartX = rawX
                    touchStartY = rawY
                    isRecordingTrack = true
                }
                MotionEvent.ACTION_MOVE -> {
                    layoutParams.x = originalX + (rawX - touchStartX).toInt()
                    layoutParams.y = originalY + (rawY - touchStartY).toInt()
                    windowManager.updateViewLayout(floatLayout, layoutParams)
                    if (isRecordingTrack) {
                        FmsEngine.Instance.feedTouchPoint(rawX, rawY)
                    }
                }
                MotionEvent.ACTION_UP -> {
                    isRecordingTrack = false
                    val screenWidth = resources.displayMetrics.widthPixels
                    layoutParams.x = if (layoutParams.x > screenWidth / 2) screenWidth - 75 else 0
                    windowManager.updateViewLayout(floatLayout, layoutParams)
                }
            }
            true
        }

        floatBall?.setOnClickListener {
            menuPanel?.visibility = if (menuPanel?.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        // 标记关键采样点
        menuPanel?.findViewById<View>(R.id.menu_mark)?.setOnClickListener {
            FmsEngine.Instance.feedTouchPoint(layoutParams.x.toFloat(), layoutParams.y.toFloat(), pressure = 1f)
            menuPanel?.visibility = View.GONE
        }
        // 停止录制并收尾轨迹
        menuPanel?.findViewById<View>(R.id.menu_stop)?.setOnClickListener {
            FmsEngine.Instance.finishRecord()
            stopSelf()
        }
    }

    private fun buildNotification(): Notification {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("后台时序录制运行中")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "悬浮录制后台服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "维持毛玻璃悬浮录制球后台常驻"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        floatLayout?.let {
            windowManager.removeView(it)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
