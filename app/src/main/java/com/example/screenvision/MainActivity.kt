package com.example.screenvision

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.Surface
import android.view.SurfaceView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.*


class MainActivity : AppCompatActivity() {

    private val TAG = "ScreenCaptureFragment"

    private val STATE_RESULT_CODE = "result_code"
    private val STATE_RESULT_DATA = "result_data"

    private val REQUEST_MEDIA_PROJECTION = 1

    private var mScreenDensity = 0

    private var mResultCode = 0
    private lateinit var mResultData: Intent

    private lateinit var mSurface: Surface
    private var mImageReader: ImageReader? = null
    private var mMediaProjection: MediaProjection?=null
    private var mVirtualDisplay: VirtualDisplay?=null
    private lateinit var mMediaProjectionManager: MediaProjectionManager
    private lateinit var mButtonToggle: Button
    private lateinit var mSurfaceView: SurfaceView

    private var mCreateScreenCaptureIntent: Intent? = null


    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState != null) {
            mResultCode =
                savedInstanceState.getInt(STATE_RESULT_CODE)
            mResultData =
                savedInstanceState.getParcelable(STATE_RESULT_DATA)!!
        }



        val metrics=this.resources.displayMetrics

        mImageReader = ImageReader.newInstance(metrics.widthPixels,metrics.heightPixels, PixelFormat.RGBA_8888, 2);

        mImageReader!!.setOnImageAvailableListener(ImageListener(), Handler())

        this.windowManager.getDefaultDisplay().getMetrics(metrics)
        mScreenDensity = metrics.densityDpi
        mMediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        mCreateScreenCaptureIntent = mMediaProjectionManager.createScreenCaptureIntent();


        mSurfaceView = findViewById(R.id.surface)
        mSurface = mSurfaceView.holder.surface
        mButtonToggle = findViewById(R.id.toggle)
        mButtonToggle.setOnClickListener {
            if (mVirtualDisplay == null) {
                startScreenCapture()
                val intent = Intent(this, DisplayRecorderService::class.java)
                intent.action = DisplayRecorderService.ACTION_START
                startService(intent)
            } else {
//                val intent = Intent(this, DisplayRecorderService::class.java)
//                intent.action = DisplayRecorderService.ACTION_STOP
//                startService(intent)
                stopScreenCapture()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode != RESULT_OK) {
                Log.i(TAG, "User cancelled")
                Toast.makeText(this, "R.string.user_cancelled", Toast.LENGTH_SHORT).show()
                return
            }
//            val activity: Activity = getActivity() ?: return
            Log.i(
                TAG,
                "Starting screen capture"
            )
            mResultCode = resultCode
            mResultData = data!!
            setUpMediaProjection()
            setUpVirtualDisplay()
        }
    }

    override fun onPause() {
        super.onPause()
//        stopScreenCapture()
    }

    override fun onDestroy() {
        super.onDestroy()
        val intent = Intent(this, DisplayRecorderService::class.java)
                intent.action = DisplayRecorderService.ACTION_STOP
                startService(intent)
        tearDownMediaProjection()
    }

    private fun setUpMediaProjection() {
        mMediaProjection = mMediaProjectionManager.getMediaProjection(mResultCode, mResultData)
    }

    private fun tearDownMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection!!.stop()
            mMediaProjection = null
        }
    }

    private fun startScreenCapture() {
        val activity: Activity = this
        if (mSurface == null || activity == null) {
            return
        }
        if (mMediaProjection != null) {
            setUpVirtualDisplay()
        } else if (mResultCode != 0 && mResultData != null) {
            setUpMediaProjection()
            setUpVirtualDisplay()
        } else {
            Log.i(
                TAG,
                "Requesting confirmation"
            )
            // This initiates a prompt dialog for the user to confirm screen projection.
            startActivityForResult(
                mMediaProjectionManager.createScreenCaptureIntent(),
                REQUEST_MEDIA_PROJECTION
            )
        }
    }

    private fun setUpVirtualDisplay() {

        Log.i(
            TAG,
            "Setting up a VirtualDisplay: " +
                    mSurfaceView.width + "x" + mSurfaceView.height +
                    " (" + mScreenDensity + ")"
        )

        mVirtualDisplay = mMediaProjection?.createVirtualDisplay(
            "ScreenCapture",
            this.resources.displayMetrics.widthPixels, this.resources.displayMetrics.heightPixels, mScreenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            mImageReader?.surface, null, null
        )

        mButtonToggle.text = "stop"
    }

    private fun stopScreenCapture() {
        if (mVirtualDisplay == null) {
            return
        }
        mVirtualDisplay!!.release()
        mVirtualDisplay = null
        mButtonToggle.text = "start"
    }

}