package com.taximeter.app.ui.profile

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.taximeter.app.databinding.ActivityProfileBinding
import com.taximeter.app.viewmodels.AuthViewModel

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var viewModel: AuthViewModel

    companion object {
        private const val TAG = "ProfileActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate started")

        try {
            binding = ActivityProfileBinding.inflate(layoutInflater)
            setContentView(binding.root)
            Log.d(TAG, "Binding inflated successfully")

            viewModel = ViewModelProvider(this)[AuthViewModel::class.java]
            Log.d(TAG, "ViewModel initialized")

            setupToolbar()
            loadProfile()

            Log.d(TAG, "onCreate completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            Toast.makeText(this, "Erreur: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupToolbar() {
        try {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.apply {
                title = "Profil du Chauffeur"
                setDisplayHomeAsUpEnabled(true)
            }
            Log.d(TAG, "Toolbar setup successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up toolbar", e)
        }
    }

    private fun loadProfile() {
        try {
            Log.d(TAG, "Loading profile...")
            val user = viewModel.getCurrentUser()

            if (user != null) {
                Log.d(TAG, "User found: ${user.id}")

                binding.tvDriverName.text = "${user.profile.firstName} ${user.profile.name}"
                binding.tvAge.text = "Âge: ${user.profile.age} ans"
                binding.tvLicense.text = "Permis: ${user.profile.licenseType}"
                binding.tvPhone.text = user.profile.phone

                Log.d(TAG, "Profile data loaded successfully")

                // Generate QR Code
                generateQRCode(user.id)
            } else {
                Log.w(TAG, "No user found, using default values")
                // Set default values
                binding.tvDriverName.text = "Utilisateur"
                binding.tvAge.text = "Âge: N/A"
                binding.tvLicense.text = "Permis: N/A"
                binding.tvPhone.text = "Téléphone: N/A"

                Toast.makeText(this, "Aucun utilisateur connecté", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading profile", e)
            Toast.makeText(this, "Erreur de chargement du profil", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generateQRCode(driverId: String) {
        try {
            Log.d(TAG, "Generating QR code for driver: $driverId")

            val multiFormatWriter = MultiFormatWriter()
            val bitMatrix: BitMatrix = multiFormatWriter.encode(
                "DRIVER_ID:$driverId",
                BarcodeFormat.QR_CODE,
                400,
                400
            )
            val barcodeEncoder = BarcodeEncoder()
            val bitmap: Bitmap = barcodeEncoder.createBitmap(bitMatrix)
            binding.ivQrCode.setImageBitmap(bitmap)

            Log.d(TAG, "QR code generated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error generating QR code", e)
            Toast.makeText(this, "Erreur lors de la génération du code QR", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        Log.d(TAG, "Back button pressed")
        onBackPressed()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "ProfileActivity destroyed")
    }
}