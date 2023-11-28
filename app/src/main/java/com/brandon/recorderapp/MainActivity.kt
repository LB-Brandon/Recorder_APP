package com.brandon.recorderapp

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore.Audio.Media
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.brandon.recorderapp.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var recorder: MediaRecorder? = null
    private var fileName: String = ""

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {    // 허용 시
                Log.d("Permission", "Granted")
                onRecord()
            } else {    // 거절 시
                Log.d("Permission", "Denied")
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // functionality of record
        fileName = "${externalCacheDir?.absolutePath}/audiorecordtest.3gp"


        // record button functionality with permission check
        binding.btnRecord.setOnClickListener {
            Log.d("Main-record", "click")
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission is already granted
                    // TODO: Use RECORD
                    onRecord()
                    Log.d("Listener", "Answer when Permission is granted")
                }

                ActivityCompat.shouldShowRequestPermissionRationale(
                    this, android.Manifest.permission.RECORD_AUDIO
                ) -> {
                    // Additional rationale should be displayed
                    Snackbar.make(
                        binding.root,
                        "Record access is required to record voice",
                        Snackbar.LENGTH_SHORT
                    ).apply {
                        setAction("Setting") {
                            requestPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                        }
                        show()
                    }
                    Log.d("Listener", "Permission has been denied but check again")
                }

                else -> {
                    // Permission has not been asked yet
                    Log.d("Listener", "Permission asked")
                    requestPermissionLauncher.launch(
                        android.Manifest.permission.RECORD_AUDIO
                    )
                }
            }
        }

    }

    private fun onRecord() {
        // Initialize MediaRecorder
        // Recorder operates asynchronously
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(this)
        } else {
            MediaRecorder()
        }
        recorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
            } catch (e: IOException) {
                Log.e("RECORDER", "prepare() failed $e")
            }
            start()
        }

        // Change UI
        binding.btnRecord.apply {
            setImageDrawable(
                ContextCompat.getDrawable(
                    this@MainActivity,
                    R.drawable.ic_baseline_stop_24
                )
            )
            imageTintList = ColorStateList.valueOf(Color.BLACK)
        }

        binding.btnPlay.apply {
            isEnabled = false
            alpha = 0.3f
        }

    }

    private fun navigateToAppSetting() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

}
