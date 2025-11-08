package com.taximeter.app.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.taximeter.app.R
import com.taximeter.app.databinding.ActivityMainBinding
import com.taximeter.app.ui.profile.ProfileActivity
import com.taximeter.app.viewmodels.TaxiMeterViewModel
import pub.devrel.easypermissions.EasyPermissions

class MainActivity : AppCompatActivity(), OnMapReadyCallback, EasyPermissions.PermissionCallbacks {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: TaxiMeterViewModel
    private var googleMap: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    companion object {
        private const val TAG = "MainActivity"
        private const val LOCATION_PERMISSION_REQUEST = 1001
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate started")

        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
            Log.d(TAG, "Binding inflated successfully")

            viewModel = ViewModelProvider(this)[TaxiMeterViewModel::class.java]
            Log.d(TAG, "ViewModel initialized successfully")

            setupToolbar()
            setupLocationUpdates() // Initialize this BEFORE setupMap
            setupMap()
            setupObservers()
            setupListeners()
            requestLocationPermissions()

            Log.d(TAG, "onCreate completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            Toast.makeText(this, "Erreur: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupToolbar() {
        try {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.title = "TAXIMÈTRE"
            Log.d(TAG, "Toolbar setup successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up toolbar", e)
        }
    }

    private fun setupMap() {
        try {
            val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as? SupportMapFragment

            if (mapFragment != null) {
                mapFragment.getMapAsync(this)
                Log.d(TAG, "Map fragment found and initialized")
            } else {
                Log.e(TAG, "Map fragment is null!")
                Toast.makeText(this, "Erreur de chargement de la carte", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up map", e)
            Toast.makeText(this, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        Log.d(TAG, "Map is ready")
        googleMap = map

        try {
            googleMap?.uiSettings?.apply {
                isZoomControlsEnabled = true
                isCompassEnabled = true
                isMyLocationButtonEnabled = true
            }

            if (hasLocationPermission()) {
                enableMyLocation()
            }
            Log.d(TAG, "Map configured successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error configuring map", e)
        }
    }

    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                googleMap?.isMyLocationEnabled = true

                // Get last location and move camera
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    location?.let {
                        val latLng = LatLng(it.latitude, it.longitude)
                        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                        Log.d(TAG, "Camera moved to current location: $latLng")
                    } ?: run {
                        Log.w(TAG, "Last location is null")
                    }
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "Security exception enabling location", e)
            }
        }
    }

    private fun setupLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        Log.d(TAG, "Location client initialized")

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    viewModel.updateLocation(location)
                    Log.d(TAG, "Location updated: ${location.latitude}, ${location.longitude}")
                }
            }
        }
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 1000
            fastestInterval = 500
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    mainLooper
                )
                Log.d(TAG, "Location updates started")
            } catch (e: Exception) {
                Log.e(TAG, "Error starting location updates", e)
            }
        }
    }

    private fun stopLocationUpdates() {
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            Log.d(TAG, "Location updates stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping location updates", e)
        }
    }

    private fun setupObservers() {
        viewModel.distanceKm.observe(this) { distance ->
            binding.tvDistance.text = String.format("%.2f km", distance)
        }

        viewModel.durationMinutes.observe(this) { duration ->
            val minutes = duration.toInt()
            val seconds = ((duration - minutes) * 60).toInt()
            binding.tvTime.text = String.format("%02d:%02d", minutes, seconds)
        }

        viewModel.totalFare.observe(this) { fare ->
            binding.tvFare.text = String.format("%.2f DH", fare)
        }

        viewModel.tripState.observe(this) { state ->
            when (state) {
                TaxiMeterViewModel.TripState.IDLE -> {
                    binding.btnStart.isEnabled = true
                    binding.btnPause.isEnabled = false
                    binding.btnStop.isEnabled = false
                    binding.btnStart.text = "DÉMARRER"
                }
                TaxiMeterViewModel.TripState.RUNNING -> {
                    binding.btnStart.isEnabled = false
                    binding.btnPause.isEnabled = true
                    binding.btnStop.isEnabled = true
                    binding.btnPause.text = "PAUSE"
                    startLocationUpdates()
                }
                TaxiMeterViewModel.TripState.PAUSED -> {
                    binding.btnStart.isEnabled = false
                    binding.btnPause.isEnabled = true
                    binding.btnStop.isEnabled = true
                    binding.btnPause.text = "REPRENDRE"
                    stopLocationUpdates()
                }
                TaxiMeterViewModel.TripState.COMPLETED -> {
                    binding.btnStart.isEnabled = true
                    binding.btnPause.isEnabled = false
                    binding.btnStop.isEnabled = false
                    stopLocationUpdates()
                    showTripSummary()
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnStart.setOnClickListener {
            viewModel.startTrip()
        }

        binding.btnPause.setOnClickListener {
            if (viewModel.tripState.value == TaxiMeterViewModel.TripState.RUNNING) {
                viewModel.pauseTrip()
            } else {
                viewModel.resumeTrip()
            }
        }

        binding.btnStop.setOnClickListener {
            viewModel.endTrip()
        }
    }

    private fun showTripSummary() {
        // Show trip summary dialog or navigate to summary activity
        // For now, just reset the trip
        viewModel.resetTrip()
    }

    private fun hasLocationPermission(): Boolean {
        return EasyPermissions.hasPermissions(this, *REQUIRED_PERMISSIONS)
    }

    private fun requestLocationPermissions() {
        if (!hasLocationPermission()) {
            EasyPermissions.requestPermissions(
                this,
                "Cette application nécessite l'accès à la localisation",
                LOCATION_PERMISSION_REQUEST,
                *REQUIRED_PERMISSIONS
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        Log.d(TAG, "Permissions granted: $perms")
        enableMyLocation()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        Log.w(TAG, "Permissions denied: $perms")
        Toast.makeText(this, "Permissions de localisation refusées", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
                true
            }
            R.id.action_history -> {
                Toast.makeText(this, "Historique à venir", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_settings -> {
                Toast.makeText(this, "Paramètres à venir", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
        Log.d(TAG, "Activity destroyed")
    }
}