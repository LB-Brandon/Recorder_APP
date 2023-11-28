package com.brandon.recorderapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.brandon.recorderapp.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import java.io.IOException

class MainActivity : AppCompatActivity(), OnTimerTickListener{
    private lateinit var binding: ActivityMainBinding

    //    1. 릴리즈 → 녹음중 → 릴리즈(저장)
    //    2. 릴리즈 → 재생중 → 릴리즈
    //    * 녹음중 ↔ 재생중 간에 이동이 일어나선 안된다!!
    private enum class State {
        RELEASE, RECORDING, PLAYING
    }

    private lateinit var timer: Timer

    private var state: State = State.RELEASE
    private var recorder: MediaRecorder? = null
    private var fileName: String = ""
    private var player: MediaPlayer? = null
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {    // 허용 시
                Log.d("Permission", "Granted")
                toggleRecording(true)
            } else {    // 거절 시
                Log.d("Permission", "Denied")
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Main-record", "click")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // functionality of record
        fileName = "${externalCacheDir?.absolutePath}/audiorecordtest.3gp"
        timer = Timer(this)

        // record button functionality with permission check
        binding.btnRecord.setOnClickListener {


            when (state) {
                State.RELEASE -> {
                    startRecordWithPermissionCheck()
                }

                State.RECORDING -> {
                    toggleRecording(false)
                }

                State.PLAYING -> {

                }
            }

        }

        binding.btnPlay.setOnClickListener {
            when (state) {
                State.RELEASE -> {
                    togglePlaying(true)
                }

                State.RECORDING -> {
                    // do nothing
                }

                State.PLAYING -> {
                    // do nothing
                }
            }
        }

        binding.btnStop.setOnClickListener {
            when (state) {
                State.RELEASE -> {
                    // do nothing
                }

                State.RECORDING -> {
                    // do nothing
                }

                State.PLAYING -> {
                    togglePlaying(false)
                }
            }
        }

    }


    private fun startRecordWithPermissionCheck() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission is already granted
                toggleRecording(true)
                Log.d("Listener", "Answer when Permission is granted")
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.RECORD_AUDIO
            ) -> {
                // Additional rationale should be displayed
                Snackbar.make(
                    binding.root, "Record access is required to record voice", Snackbar.LENGTH_SHORT
                ).apply {
                    setAction("Setting") {
                        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                    show()
                }
                Log.d("Listener", "Permission has been denied but check again")
            }

            else -> {
                // Permission has not been asked yet
                Log.d("Listener", "Permission asked")
                requestPermissionLauncher.launch(
                    Manifest.permission.RECORD_AUDIO
                )
            }
        }
    }

    private fun togglePlaying(start: Boolean) {
        if (start) {
            startPlaying()
        } else {
            stopPlaying()
        }
    }


    private fun startPlaying() {
        state = State.PLAYING

        player = MediaPlayer().apply {
            try {
                setDataSource(fileName)
                prepare()
            } catch (e: IOException) {
                Log.e("PLAYER", "Media Player prepare() failed $e")
            }
            start()
        }

        // when play is done
        player?.setOnCompletionListener {
            stopPlaying()
        }

        binding.btnRecord.apply {
            isEnabled = false
            alpha = 0.3f
        }
    }

    private fun stopPlaying() {
        state = State.RELEASE

        player?.release()
        player = null

        binding.btnRecord.apply {
            isEnabled = true
            alpha = 1.0f
        }
    }


    private fun toggleRecording(start: Boolean) {
        if (start) {
            startRecording()
        } else {
            stopRecording()
        }
    }


    private fun startRecording() {
        // Initialize MediaRecorder
        // Recorder operates asynchronously
        state = State.RECORDING
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
        // Timer start
        timer.start()

        // 진폭
        recorder?.maxAmplitude?.toFloat()

        // Change UI
        binding.btnRecord.apply {
            setImageDrawable(
                ContextCompat.getDrawable(
                    this@MainActivity, R.drawable.ic_baseline_stop_24
                )
            )
            imageTintList = ColorStateList.valueOf(Color.BLACK)
        }

        binding.btnPlay.apply {
            isEnabled = false
            alpha = 0.3f
        }

    }

    private fun stopRecording() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
        timer.stop()
        state = State.RELEASE

        // Change UI
        binding.btnRecord.apply {
            setImageDrawable(
                ContextCompat.getDrawable(
                    this@MainActivity, R.drawable.ic_baseline_fiber_manual_record_24
                )
            )
            imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this@MainActivity, R.color.red))
        }
        binding.btnPlay.apply {
            isEnabled = true
            alpha = 1.0f
        }
    }


    private fun navigateToAppSetting() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    override fun onTick(duration: Long) {
        binding.viewWaveForm.addAmplitude(recorder?.maxAmplitude?.toFloat() ?: 0f)
    }

}
