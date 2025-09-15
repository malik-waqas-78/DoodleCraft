package com.example.doodlecraft

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.doodlecraft.databinding.ActivityMainBinding
import com.example.doodlecraft.databinding.DialogBrushSizeBinding
import com.example.doodlecraft.db.HistoryDatabase
import com.example.doodlecraft.db.HistoryEntity
import com.example.doodlecraft.views.ShapeType
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val STORAGE_PERMISSION_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        val currentTheme = ThemeManager.getTheme(this)
        when (currentTheme) {
            ThemeMode.LIGHT -> setTheme(R.style.Theme_DoodleCraft_Light)
            ThemeMode.DARK -> setTheme(R.style.Theme_DoodleCraft_Dark)
            ThemeMode.NIGHT -> setTheme(R.style.Theme_DoodleCraft_Night)
        }
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topAppBar)
        setupClickListeners()
        handleIncomingIntents()

        // Set initial canvas background based on theme
        val canvasBgColor = if (currentTheme == ThemeMode.LIGHT) Color.WHITE else Color.parseColor("#FF121212")
        binding.canvasView.setCanvasBackgroundColor(canvasBgColor)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> { saveDrawing(); true }
            R.id.action_share -> { shareDrawing(); true }
            R.id.action_clear -> { binding.canvasView.clearCanvas(); true }
            R.id.action_history -> { startActivity(Intent(this, HistoryActivity::class.java)); true }
            R.id.action_settings -> { startActivity(Intent(this, SettingsActivity::class.java)); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupClickListeners() {
        binding.btnBrush.setOnClickListener { binding.canvasView.setToolToBrush() }
        binding.btnEraser.setOnClickListener { binding.canvasView.setToolToEraser() }
        binding.btnShapes.setOnClickListener { showShapeSelectionDialog() }
        binding.btnText.setOnClickListener { showTextDialog() }
        binding.btnSize.setOnClickListener { showBrushSizeDialog() }
        binding.btnColor.setOnClickListener { showColorPickerDialog(isCanvasBg = false) }
        binding.btnCanvasColor.setOnClickListener { showColorPickerDialog(isCanvasBg = true) }
        binding.btnUndo.setOnClickListener { binding.canvasView.undo() }
        binding.btnRedo.setOnClickListener { binding.canvasView.redo() }
    }

    private fun saveDrawing() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
            return
        }
        val bitmap = binding.canvasView.getDrawingAsBitmap() ?: return
        Toast.makeText(this, "Saving...", Toast.LENGTH_SHORT).show()
        Thread {
            val imageUri = saveBitmapToGallery(bitmap)
            runOnUiThread {
                if (imageUri != null) Toast.makeText(this, "Drawing saved!", Toast.LENGTH_SHORT).show()
                else Toast.makeText(this, "Failed to save.", Toast.LENGTH_LONG).show()
            }
            if (imageUri != null) {
                val entity = HistoryEntity(filePath = imageUri.toString(), timestamp = System.currentTimeMillis())
                HistoryDatabase.getDatabase(applicationContext).historyDao().insertDrawing(entity)
            }
        }.start()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE && grantResults.getOrNull(0) == PackageManager.PERMISSION_GRANTED) {
            saveDrawing()
        } else {
            Toast.makeText(this, "Permission denied.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveBitmapToGallery(bitmap: Bitmap): Uri? {
        val filename = "DoodleCraft_${System.currentTimeMillis()}.jpg"
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
        val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val imageUri = contentResolver.insert(collection, values) ?: return null
        return try {
            contentResolver.openOutputStream(imageUri)?.use {
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)) throw IOException("Failed to save bitmap.")
            } ?: throw IOException("Failed to get output stream.")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                contentResolver.update(imageUri, values, null, null)
            }
            imageUri
        } catch (e: IOException) {
            e.printStackTrace()
            contentResolver.delete(imageUri, null, null)
            null
        }
    }

    private fun showBrushSizeDialog() {
        // ... (implementation remains the same)
    }

    private fun showColorPickerDialog(isCanvasBg: Boolean) {
        val colors = arrayOf("Black", "Red", "Green", "Blue", "Yellow", "White", "Gray")
        val colorValues = intArrayOf(Color.BLACK, Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.WHITE, Color.GRAY)
        AlertDialog.Builder(this).apply {
            setTitle(if (isCanvasBg) "Choose Background Color" else "Choose Brush Color")
            setItems(colors) { _, which ->
                if (isCanvasBg) {
                    binding.canvasView.setCanvasBackgroundColor(colorValues[which])
                } else {
                    binding.canvasView.setBrushColor(colorValues[which])
                }
            }
        }.create().show()
    }

    // ... other dialog functions ...

    private fun shareDrawing() {
        // ... (implementation remains the same)
    }

    private fun saveBitmapToCache(bitmap: Bitmap): Uri? {
        // ... (implementation remains the same)
    }

    private fun handleIncomingIntents() {
        // ... (implementation remains the same)
    }
}
