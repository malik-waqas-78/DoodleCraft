package com.example.doodlecraft

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.doodlecraft.databinding.ActivityHistoryBinding

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply the theme before the view is created
        when (ThemeManager.getTheme(this)) {
            ThemeMode.LIGHT -> setTheme(R.style.Theme_DoodleCraft_Light)
            ThemeMode.DARK -> setTheme(R.style.Theme_DoodleCraft_Dark)
            ThemeMode.NIGHT -> setTheme(R.style.Theme_DoodleCraft_Night)
        }

        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
