package com.alfan.story.activities

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.alfan.story.R
import com.alfan.story.databinding.ActivityCreateBinding
import com.alfan.story.library.CameraExt
import com.alfan.story.viewmodels.CreateState
import com.alfan.story.viewmodels.StoryViewModel
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.size
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.io.File
import java.util.concurrent.TimeUnit

class CreateActivity : AppCompatActivity() {

    private var imgPhoto: File? = null

    private val storyVM: StoryViewModel by inject()

    private lateinit var progressDialog: ProgressDialog

    private var locRequest: LocationRequest? = null

    private var locCallBack: LocationCallback? = null

    private var loc: Location? = null

    private lateinit var createBind: ActivityCreateBinding

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createBind = ActivityCreateBinding.inflate(layoutInflater)
        setContentView(createBind.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        progressDialog = ProgressDialog(this@CreateActivity)
        progressDialog.setMessage(getString(R.string.label_loading))
        progressDialog.setCancelable(false)

        if (!arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ).all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQUEST_CODE_PERMISSIONS
            )
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        createBind.cameraButton.setOnClickListener {
            launcherCamera.launch(Intent(this@CreateActivity, CameraActivity::class.java))
        }

        createBind.galleryButton.setOnClickListener {
            startGallery()
        }

        createBind.upload.setOnClickListener {
            if (checkLocationPermission()) {
                fusedLocationClient.lastLocation.addOnSuccessListener { locate: Location? ->
                    if (locate != null) {
                        loc = locate
                    }

                    if (locate != null) {
                        if (imgPhoto != null) {
                            progressDialog.show()

                            lifecycleScope.launch {
                                val photo = Compressor.compress(this@CreateActivity, imgPhoto!!) {
                                    size(1_000_000)
                                }

                                storyVM.create(
                                    createBind.desc.text.toString(),
                                    locate!!.latitude.toString(),
                                    locate!!.longitude.toString(),
                                    photo
                                ).observe(this@CreateActivity) {
                                    progressDialog.dismiss()
                                    when(it) {
                                        CreateState.Success -> {
                                            Toast.makeText(this@CreateActivity, getString(R.string.message_success_create), Toast.LENGTH_LONG).show()
                                            finish()
                                        }
                                        is CreateState.Error -> {
                                            errorDialog(it.message)
                                        }
                                    }
                                }
                            }
                        } else {
                            progressDialog.dismiss()
                            Toast.makeText(this@CreateActivity,
                                getString(R.string.message_error_photo_notselected),
                                Toast.LENGTH_LONG).show()
                        }
                    } else {
                        initLocation()
                    }
                }.addOnFailureListener {
                    errorDialog(it.localizedMessage ?: "Location not found")
                }
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    REQUEST_CODE_PERMISSIONS
                )
            }
        }
    }

    private fun errorDialog(message: String) {
        MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_MaterialComponents_Dialog_Alert)
            .setTitle(resources.getString(R.string.lebel_informasi))
            .setMessage(message)
            .setCancelable(true)
            .setPositiveButton(resources.getString(R.string.label_dialog_positive_action)) { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun checkLocationPermission(): Boolean {
        return (ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
    }

    private fun initLocation() {
        locRequest = LocationRequest.create().apply {
            interval = TimeUnit.SECONDS.toMillis(1)
            maxWaitTime = TimeUnit.SECONDS.toMillis(1)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locRequest!!)

        val client: SettingsClient = LocationServices.getSettingsClient(this)

        client.checkLocationSettings(builder.build())
            .addOnSuccessListener {
            }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    try {
                        resolutionLauncher.launch(IntentSenderRequest.Builder(exception.resolution).build())
                    } catch (sendEx: IntentSender.SendIntentException) {
                        Toast.makeText(this, sendEx.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }

        locCallBack = object: LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                loc = locationResult.lastLocation
                for (locate in locationResult.locations) {
                     loc = locate
                }
            }
        }


        startLocationUpdates()
    }

    private val resolutionLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        when (result.resultCode) {
            RESULT_OK ->  { }
            RESULT_CANCELED -> {
                Toast.makeText(this@CreateActivity, getString(R.string.warning_permission_location), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            val selected: Uri = it.data?.data as Uri
            imgPhoto = CameraExt.uriToFile(selected, this@CreateActivity)

            Glide.with(this@CreateActivity)
                .load(imgPhoto)
                .placeholder(R.drawable.imagedicoding)
                .centerCrop()
                .into(createBind.preview)
        }
    }

    private val launcherCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CameraExt.CAMERAX_RESULT) {
            imgPhoto = it.data?.getSerializableExtra("picture") as File

            Glide.with(this@CreateActivity)
                .load(imgPhoto)
                .placeholder(R.drawable.imagedicoding)
                .centerCrop()
                .into(createBind.preview)
        }
    }

    private fun startLocationUpdates() {
        try {
            fusedLocationClient.requestLocationUpdates(
                locRequest!!,
                locCallBack!!,
                Looper.getMainLooper()
            )
        } catch (exception: SecurityException) {
            Log.e(localClassName, "Error : " + exception.message)
        }
    }

    private fun stopLocationUpdates() {
        try {
            fusedLocationClient.removeLocationUpdates(locCallBack!!)
        } catch (exception: Exception) {
            Log.e(localClassName, "Error : " + exception.message)
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ).all {
                    ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
                }) {
                Toast.makeText(
                    this@CreateActivity,
                    "Don't get permission",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            } else {
                initLocation()
            }
        }
    }

    companion object {
        const val REQUEST_CODE_PERMISSIONS = 36
    }

}