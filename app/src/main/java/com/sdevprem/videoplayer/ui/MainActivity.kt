package com.sdevprem.videoplayer.ui

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sdevprem.videoplayer.R
import com.sdevprem.videoplayer.databinding.ActivityMainBinding
import com.sdevprem.videoplayer.utils.isStoragePermissionGranted
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (!it)
                showStorageRequestPermissionRationale()
            else setupLayout()

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestStoragePermission()
    }

    private fun setupLayout() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navController = supportFragmentManager.findFragmentById(
            binding.fragmentContainerView.id
        )!!.findNavController()

        val configuration = AppBarConfiguration.Builder(
            R.id.folders,
            R.id.videos
        ).setOpenableLayout(binding.drawerLayout)
            .build()

        binding.toolbar.setupWithNavController(navController, configuration)
        binding.drawerNavView.setupWithNavController(navController)
        binding.bottomNav.setupWithNavController(navController)
    }

    private fun requestStoragePermission() {
        when {
            isStoragePermissionGranted() -> setupLayout()

            shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE) ->
                showStorageRequestPermissionRationale()

            else -> permissionLauncher.launch(READ_EXTERNAL_STORAGE)

        }
    }

    private fun showStorageRequestPermissionRationale() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Permission required!")
            .setMessage("Please provide storage permission in order to load videos")
            .setPositiveButton("Grant") { _, _ ->
                permissionLauncher.launch(READ_EXTERNAL_STORAGE)
            }
            .setNegativeButton("Deny") { _, _ ->
                //exit the app
                finish()
            }
            .show()
    }
}