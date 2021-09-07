package com.banasiak.android.contactparser

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.banasiak.android.contactparser.example.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

  private lateinit var binding: ActivityMainBinding
  private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
  private lateinit var selectContactLauncher: ActivityResultLauncher<Void>

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    setupLaunchers()

    binding.button.setOnClickListener {
      val permission = Manifest.permission.READ_CONTACTS
      val hasPermission =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
      if (hasPermission) {
        selectContactLauncher.launch(null)
      } else {
        requestPermissionLauncher.launch(permission)
      }
    }

  }

  private fun setupLaunchers() {
    requestPermissionLauncher =
      registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
          selectContactLauncher.launch(null)
        } else {
          Toast.makeText(this, "READ_CONTACTS permission required", Toast.LENGTH_SHORT).show()
        }
      }

    selectContactLauncher =
      registerForActivityResult(ActivityResultContracts.PickContact()) { contactUri ->
        val contact = ContactParser.getInstance(this).parseContact(contactUri)
        binding.output.text = contact?.toVcf()
      }
  }

}