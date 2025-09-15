package com.example.doodlecraft

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.doodlecraft.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        when (ThemeManager.getTheme(this)) {
            ThemeMode.LIGHT -> setTheme(R.style.Theme_DoodleCraft_Light)
            ThemeMode.DARK -> setTheme(R.style.Theme_DoodleCraft_Dark)
            ThemeMode.NIGHT -> setTheme(R.style.Theme_DoodleCraft_Night)
        }
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Apply window insets handling
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            WindowInsetsCompat.CONSUMED
        }
        setSupportActionBar(binding.topAppBar)
        setupThemeSelector()
        setupPrivacyPolicyLink()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Your custom back press logic
                startActivity(Intent(this@SettingsActivity, MainActivity::class.java))
                finish()
            }
        })
    }

    private fun setupThemeSelector() {
        when (ThemeManager.getTheme(this)) {
            ThemeMode.LIGHT -> binding.radioLight.isChecked = true
            ThemeMode.DARK -> binding.radioDark.isChecked = true
            ThemeMode.NIGHT -> binding.radioNight.isChecked = true
        }

        binding.themeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedTheme = when (checkedId) {
                R.id.radio_light -> ThemeMode.LIGHT
                R.id.radio_dark -> ThemeMode.DARK
                else -> ThemeMode.NIGHT
            }
            ThemeManager.setTheme(this, selectedTheme)
            recreate()
        }
    }

    private fun setupPrivacyPolicyLink() {
        binding.privacyPolicyLink.setOnClickListener {
            val url = "https://your-hosted-privacy-policy.com" // Placeholder URL
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Could not open browser.", Toast.LENGTH_SHORT).show()
            }
        }
    }


}
