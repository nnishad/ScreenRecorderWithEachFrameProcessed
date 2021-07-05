package com.example.screenvision

import android.R
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.text.TextUtils
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MIN


open class DisplayRecorderService :Service() {

    private val DEBUG = false
    private val TAG = "ScreenRecorderService"
    private val APP_DIR_NAME = "ScreenRecorder"
    companion object
    {
        val BASE = "com.example.screenvision.DisplayRecorderService."
        val ACTION_START = BASE + "ACTION_START"
        val ACTION_STOP = BASE + "ACTION_STOP"
        val EXTRA_RESULT_CODE = BASE + "EXTRA_RESULT_CODE"
    }


    val ACTION_PAUSE = BASE + "ACTION_PAUSE"
    val ACTION_RESUME = BASE + "ACTION_RESUME"
    val ACTION_QUERY_STATUS = BASE + "ACTION_QUERY_STATUS"
    val ACTION_QUERY_STATUS_RESULT = BASE + "ACTION_QUERY_STATUS_RESULT"
    val EXTRA_QUERY_RESULT_RECORDING = BASE + "EXTRA_QUERY_RESULT_RECORDING"
    val EXTRA_QUERY_RESULT_PAUSING = BASE + "EXTRA_QUERY_RESULT_PAUSING"
//    private val NOTIFICATION: Int = com.example.screenvision.R.string.app_name


//    private var mMediaProjectionManager: MediaProjectionManager? = null
//    private var mNotificationManager: NotificationManager? = null


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    /*override fun onCreate() {
        super.onCreate()
        *//*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val builder = NotificationCompat.Builder(this, "messages")
                .setContentText("streaming enabled")
                .setContentTitle("Stream to e-ink")
            startForeground(101, builder.build())
        }*//*

    }*/

    override fun onCreate() {
        super.onCreate()
        Log.v(TAG, "onCreate:")
        /*mMediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager*/
//        showNotification(TAG)
//        startForeground()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (DEBUG) Log.v(TAG, "onStartCommand:intent=$intent")
        var result = START_STICKY
        val action = intent?.action
        if (ACTION_START == action) {
            startForeground()
        }
        if (ACTION_STOP == action) {
            stopScreenRecord()
            result = START_NOT_STICKY
            return result
        }
        return result
    }

    /*private fun showNotification(text: CharSequence) {
        if (DEBUG) Log.v(TAG, "showNotification:$text")
        // Set the info for the views that show in the notification panel.
        val notification: Notification = Notification.Builder(this)
            .setSmallIcon(R.drawable.ic_btn_speak_now) // the status icon
            .setTicker(text) // the status text
            .setWhen(System.currentTimeMillis()) // the time stamp
            .setContentTitle(getText(com.example.screenvision.R.string.app_name)) // the label of the entry
            .setContentText(text) // the contents of the entry
            .setContentIntent(createPendingIntent()) // The intent to send when the entry is clicked
            .build()
        startForeground(NOTIFICATION, notification)
        // Send the notification.
        mNotificationManager!!.notify(NOTIFICATION, notification)

    }*/

    private fun createPendingIntent(): PendingIntent? {
        return PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), 0)
    }

    /**
     * stop screen recording
     */
     open fun stopScreenRecord() {
        stopForeground(true)
        /*if (mNotificationManager != null) {
            mNotificationManager!!.cancel(NOTIFICATION)
            mNotificationManager = null
        }*/
//        stopSelf()
    }

    private fun startForeground() {
        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("my_service", "My Background Service")
            } else {
                // If earlier version channel ID is not used
                // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                ""
            }

        val notificationBuilder = NotificationCompat.Builder(this, channelId )
        val notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.drawable.ic_btn_speak_now)
            .setPriority(PRIORITY_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(101, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String{
        val chan = NotificationChannel(channelId,
            channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }
}