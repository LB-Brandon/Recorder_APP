package com.brandon.recorderapp

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.brandon.recorderapp.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import java.io.IOException

class MainActivity : AppCompatActivity(), OnTimerTickListener {
    private lateinit var binding: ActivityMainBinding
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var fileName: String = ""
    private var state: State = State.RELEASE
    private lateinit var timer: Timer


    private enum class State {
        RELEASE, RECORDING, PLAYING
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {    // 허용 시
                Log.d("Permission", "Granted")
                startRecording()
            } else {    // 거절 시
                Log.d("Permission", "Denied")
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fileName = "${externalCacheDir?.absolutePath}/audiorecordtest.3gp"
        timer = Timer(this)

        binding.btnRecord.setOnClickListener {
            // State 에 따른 녹음 버튼 동작
            handleRecordButtonAction()
        }
        binding.btnPlay.setOnClickListener {
            // State 에 따른 플레이 버튼 동삭
            handlePlayButtonAction()
        }
        binding.btnPlay.isEnabled = false
    }

    private fun handleRecordButtonAction() {
        //    1. 릴리즈 → 녹음중 → 릴리즈(저장)
        //    2. 릴리즈 → 재생중 → 릴리즈
        //    * 녹음중 ↔ 재생중 간에 이동이 일어나선 안된다!!
        when (state) {
            State.RELEASE   -> {
                // 대기중 -> 녹음 시작
                requestRecordPermissionAndStartRecord()
//                startRecording()
            }

            State.PLAYING   -> {
                // 동작하지 않음

            }

            State.RECORDING -> {
                // 녹음 중 -> 녹음 정지
                stopRecording()
            }
        }
    }

    private fun handlePlayButtonAction() {
        //    1. 릴리즈 → 녹음중 → 릴리즈(저장)
        //    2. 릴리즈 → 재생중 → 릴리즈
        //    * 녹음중 ↔ 재생중 간에 이동이 일어나선 안된다!!
        when (state) {
            State.RELEASE   -> {
                // 녹음된 파일 재생
                startPlaying()
            }

            State.PLAYING   -> {
                // 녹음 파일 재생 정지
                stopPlaying()
            }

            State.RECORDING -> {
                // 동작하지 않음
            }
        }
    }

    private fun stopPlaying() {

        player?.release()
        player = null
        state = State.RELEASE

        timer.stop()

        binding.btnRecord.apply {
            isEnabled = true
            alpha = 1.0f
        }
        binding.btnPlay.apply {
            setImageDrawable(
                ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_baseline_play_arrow_24)
            )
        }
    }

    private fun startPlaying() {

        player = MediaPlayer()?.apply {
            try {
                Log.d("file", "$fileName")
                setDataSource(fileName)
//                prepareAsync()
//                this.setOnPreparedListener { mp ->
//                    mp.start()
//                }
                prepare()
            } catch (e: IOException) {
                Log.e("PLAYER", "Media Player prepare() failed $e")
            }
            start()

        }
        state = State.PLAYING

        binding.viewWaveForm.clearWave()
        timer.start()

        // 녹음 파일이 모두 재생된 경우 player 해제와 함께 UI 변경
        player?.setOnCompletionListener {
            stopPlaying()
        }

        binding.btnRecord.apply {
            isEnabled = false
            alpha = 0.3f
        }
        binding.btnPlay.apply {
            setImageDrawable(
                ContextCompat.getDrawable(this@MainActivity, R.drawable.baseline_pause_24)
            )
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


    private fun startRecording() {
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(this)
        } else {
            MediaRecorder()
        }
        // change state
        state = State.RECORDING

        // Recorder setting
        recorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
                start()
            } catch (e: IOException) {
                Log.e("RECORDER", "prepare() failed $e")
            }
        }

        binding.viewWaveForm.clearData()

        // 진폭을 계속 업데이트 받으려면 새로운 스레드를 만들어 콜백으로 전달해야 한다
        timer.start()

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

    private fun requestRecordPermissionAndStartRecord() {
        when {
            isRecordPermissionGranted()           -> {
                // Permission is already granted
                Log.d("Listener", "Permission is already granted")
                startRecording()
            }

            shouldShowRecordPermissionRationale() -> {
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

            else                                  -> {
                // Permission has not been asked yet
                Log.d("Listener", "Permission asked")
                requestPermissionLauncher.launch(
                    Manifest.permission.RECORD_AUDIO
                )
            }
        }
    }

    private fun shouldShowRecordPermissionRationale(): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            this, Manifest.permission.RECORD_AUDIO
        )
    }

    private fun isRecordPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onTick(duration: Long) {
        val millisecond = duration % 1000  // 0~99
        val second = (duration / 1000) % 60   // 0~59
        val minute = (duration / 1000 / 60)     // 0~

        // Timer 으로 부터 전달 받는 duration
        binding.tvTimer.text = String.format("%02d:%02d:%02d", minute, second, millisecond / 10)

        if (state == State.PLAYING) {
            binding.viewWaveForm.replayAmplitude()
        } else if (state == State.RECORDING){
            // recorder 로 부터 받은 amplitude 를
            binding.viewWaveForm.addAmplitude(recorder?.maxAmplitude?.toFloat() ?: 0f)
        }
    }
}