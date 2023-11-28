package com.brandon.recorderapp

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.brandon.recorderapp.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {    // 허용 시
                Log.d("Permission", "Granted")
//                useRecordAudioPermission()

            } else {    // 거절 시
//                showRecordAudioRationaleDialog()
                Log.d("Permission", "Denied")
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRecord.setOnClickListener {
            Log.d("Main-record", "click")
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission is already granted
                    // TODO: Use RECORD
//                    Snackbar.make(binding.root, getString(R.string.permission_granted), Snackbar.LENGTH_SHORT).show()
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
    private fun navigateToAppSetting() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

}
