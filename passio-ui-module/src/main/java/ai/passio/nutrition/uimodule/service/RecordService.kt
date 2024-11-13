package ai.passio.nutrition.uimodule.service

import android.app.Activity.RESULT_OK
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
import android.hardware.display.DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Environment
import android.os.Environment.DIRECTORY_MOVIES
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Process
import android.util.Log
import java.io.IOException

private const val EXTRA_RESULT_CODE = "resultcode"
private const val EXTRA_DATA = "data"
private const val ONGOING_NOTIFICATION_ID = 23
private const val CHANNEL_ID = "passioChannel"

class RecordService : Service() {

    private var serviceHandler: ServiceHandler? = null
    private var serviceThread: HandlerThread? = null

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var mediaRecorder: MediaRecorder? = null
    private var resultCode = 0
    private var data: Intent? = null
    private var screenStateReceiver: BroadcastReceiver? = null

    private var started = false

    companion object {
        fun newIntent(context: Context, resultCode: Int, data: Intent): Intent {
            val intent = Intent(context, RecordService::class.java).apply {
                putExtra(EXTRA_RESULT_CODE, resultCode)
                putExtra(EXTRA_DATA, data)
            }
            return intent
        }
    }

    inner class RecordBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("RRRR", "Intent action: ${intent.action}")
            when (intent.action) {
                Intent.ACTION_SCREEN_ON -> startRecording(resultCode, data!!)
                Intent.ACTION_SCREEN_OFF -> stopRecording()
                Intent.ACTION_CONFIGURATION_CHANGED -> {
                    stopRecording()
                    startRecording(resultCode, data!!)
                }
            }
        }
    }

    private inner class ServiceHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            if (resultCode == RESULT_OK) {
                startRecording(resultCode, data!!)
            }
        }
    }

    override fun onCreate() {
        val notificationIntent = Intent(this, RecordService::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_MUTABLE)

        val channel = NotificationChannel(CHANNEL_ID, "PassioRecordChannel", NotificationManager.IMPORTANCE_HIGH)
        channel.description = "Passio channel for foreground service notifications"
        val notificationManager = getSystemService<NotificationManager>(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        val notification = Notification.Builder(this)
            .setChannelId(CHANNEL_ID)
            .setContentTitle("ScreenRecorder")
            .setContentText("Your screen is being recorded and saved to your phone.")
            .setContentIntent(pendingIntent)
            .setTicker("TickerText")
            .build()

        startForeground(ONGOING_NOTIFICATION_ID, notification)

        screenStateReceiver = RecordBroadcastReceiver()
        val screenStateFilter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_CONFIGURATION_CHANGED)
        }
        registerReceiver(screenStateReceiver, screenStateFilter)

        serviceThread = HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND).apply {
            start()
            serviceHandler = ServiceHandler(looper)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0)
        data = intent.getParcelableExtra(EXTRA_DATA)

        if (resultCode == 0 || data == null) {
            throw IllegalArgumentException("Result code or data missing")
        }

        serviceHandler?.obtainMessage()?.let { msg ->
            msg.arg1 = startId
            serviceHandler?.sendMessage(msg)
        }

        return START_REDELIVER_INTENT
    }

    private fun startRecording(resultCode: Int, data: Intent) {
        // android.os.Debug.waitForDebugger()
        Log.d("RRRR", "Init start recording")

        val projectionManager = applicationContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(applicationContext)
        } else {
            MediaRecorder()
        }

        val metrics = Resources.getSystem().displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val density = metrics.densityDpi

        mediaRecorder?.apply {
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setVideoEncodingBitRate(3 * 1024 * 1024)
            setVideoFrameRate(30)
            setVideoSize(1080, 1920)
        }

        val videoDir = Environment.getExternalStoragePublicDirectory(DIRECTORY_MOVIES).absolutePath
        val timestamp = System.currentTimeMillis()

        var orientation = "portrait"
        if (width > height) {
            orientation = "landscape"
        }
        val filePathAndName = videoDir + "/time_" + timestamp.toString() + "_mode_" + orientation + ".mp4"
        Log.d("RRRR", "File: $filePathAndName")
        mediaRecorder?.setOutputFile(filePathAndName)

        try {
            mediaRecorder?.prepare()
        } catch (e: IllegalStateException) {
            Log.e("RRRR", "Media recorder prepare error: ${e.message}")
            e.printStackTrace()
        } catch (e: IOException) {
            Log.e("RRRR", "Media recorder prepare error: ${e.message}")
            e.printStackTrace()
        }

        mediaProjection = projectionManager.getMediaProjection(resultCode, data)
        mediaProjection?.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() {
                super.onStop()
                stopRecording()
            }
        }, null)
        virtualDisplay = mediaProjection!!.createVirtualDisplay(
            "PassioScreenRecording", width, height, density, VIRTUAL_DISPLAY_FLAG_PRESENTATION,
            mediaRecorder!!.surface, null, null
        )
        try {
            mediaRecorder?.start()
        } catch (e: Exception) {
            Log.e("RRRR", "Media recorder start error: ${e.message}")
            e.printStackTrace()
        }

        started = true
        Log.d("RRRR", "Started recording")
    }

    private fun stopRecording() {
        if (!started) {
            return
        }
        started = false

        try {
            mediaRecorder?.stop()
        } catch (e: RuntimeException) {
            Log.e("RRRR", "Error stopping MediaRecorder: ${e.message}")
            // Handle any cleanup here if needed
        } finally {
            mediaRecorder?.release()
            virtualDisplay?.release()
            mediaProjection?.stop()
            mediaRecorder = null
            virtualDisplay = null
            mediaProjection = null
        }
        Log.d("RRRR", "Stopped recording")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecording()
        unregisterReceiver(screenStateReceiver)
        stopSelf()
        serviceThread?.quit()
        serviceHandler = null
        Log.d("RRRR", "Recorder service stopped")
    }
}