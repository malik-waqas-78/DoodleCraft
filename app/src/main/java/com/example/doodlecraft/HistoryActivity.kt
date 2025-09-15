package com.example.doodlecraft

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.doodlecraft.adapters.HistoryAdapter
import com.example.doodlecraft.databinding.ActivityHistoryBinding
import com.example.doodlecraft.db.HistoryDatabase
import com.example.doodlecraft.db.HistoryEntity

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        when (ThemeManager.getTheme(this)) {
            ThemeMode.LIGHT -> setTheme(R.style.Theme_DoodleCraft_Light)
            ThemeMode.DARK -> setTheme(R.style.Theme_DoodleCraft_Dark)
            ThemeMode.NIGHT -> setTheme(R.style.Theme_DoodleCraft_Night)
        }
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Apply window insets handling
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            WindowInsetsCompat.CONSUMED
        }
        setSupportActionBar(binding.topAppBar)
        setupRecyclerView()
        loadHistory()
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter(
            onEditClicked = { historyEntity ->
//                val intent = Intent(this, MainActivity::class.java).apply {
//                    putExtra("EDIT_DRAWING_URI", historyEntity.filePath)
//                }
//                startActivity(intent)
            },
            onDeleteClicked = { historyEntity ->
                showDeleteConfirmationDialog(historyEntity)
            }
        )
        binding.historyRecyclerView.apply {
            adapter = historyAdapter
            layoutManager = GridLayoutManager(this@HistoryActivity, 2)
        }
    }

    private fun loadHistory() {
        Thread {
            val historyList =
                HistoryDatabase.getDatabase(applicationContext).historyDao().getAllDrawings()
            runOnUiThread {
                historyAdapter.submitList(historyList)
            }
        }.start()
    }

    private fun showDeleteConfirmationDialog(entity: HistoryEntity) {
        AlertDialog.Builder(this)
            .setTitle("Delete Drawing")
            .setMessage("Are you sure you want to delete this drawing? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteDrawing(entity)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteDrawing(entity: HistoryEntity) {
        Thread {
            HistoryDatabase.getDatabase(applicationContext).historyDao().deleteDrawing(entity)
            try {
                contentResolver.delete(Uri.parse(entity.filePath), null, null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            loadHistory()
        }.start()
    }
}
