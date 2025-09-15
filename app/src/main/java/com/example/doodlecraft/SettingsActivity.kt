package com.example.doodlecraft

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.doodlecraft.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply the theme before the view is created
        when (ThemeManager.getTheme(this)) {
            ThemeMode.LIGHT -> setTheme(R.style.Theme_DoodleCraft_Light)
            ThemeMode.DARK -> setTheme(R.style.Theme_DoodleCraft_Dark)
            ThemeMode.NIGHT -> setTheme(R.style.Theme_DoodleCraft_Night)
        }

        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupThemeSelector()
    }

    private fun setupThemeSelector() {
        // Set the initial state of the radio buttons
        when (ThemeManager.getTheme(this)) {
            ThemeMode.LIGHT -> binding.radioLight.isChecked = true
            ThemeMode.DARK -> binding.radioDark.isChecked = true
            ThemeMode.NIGHT -> binding.radioNight.isChecked = true
        }

        // Set listener for theme changes
        binding.themeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedTheme = when (checkedId) {
                R.id.radio_light -> ThemeMode.LIGHT
                R.id.radio_dark -> ThemeMode.DARK
                else -> ThemeMode.NIGHT
            }
            ThemeManager.setTheme(this, selectedTheme)
            // Recreate the activity to apply the new theme immediately
            recreate()
        }
    }
}
