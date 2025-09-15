package com.example.doodlecraft

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.doodlecraft.databinding.ActivityMainBinding
import com.example.doodlecraft.databinding.DialogBrushSizeBinding
import com.example.doodlecraft.views.ShapeType
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val STORAGE_PERMISSION_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnBrush.setOnClickListener { binding.canvasView.setToolToBrush() }
        binding.btnEraser.setOnClickListener { binding.canvasView.setToolToEraser() }
        binding.btnShapes.setOnClickListener { showShapeSelectionDialog() }
        binding.btnText.setOnClickListener { showTextDialog() }
        binding.btnSize.setOnClickListener { showBrushSizeDialog() }
        binding.btnColor.setOnClickListener { showColorPickerDialog() }
        binding.btnUndo.setOnClickListener { binding.canvasView.undo() }
        binding.btnRedo.setOnClickListener { binding.canvasView.redo() }
        binding.btnClear.setOnClickListener { binding.canvasView.clearCanvas() }
        binding.btnSave.setOnClickListener { saveDrawing() }
    }

    private fun saveDrawing() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
            return // Wait for permission result
        }

        val bitmap = binding.canvasView.getDrawingAsBitmap()
        if (bitmap == null) {
            Toast.makeText(this, "Nothing to save", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Saving...", Toast.LENGTH_SHORT).show()
        Thread {
            val success = saveBitmapToGallery(bitmap)
            runOnUiThread {
                if (success) {
                    Toast.makeText(this, "Drawing saved to gallery!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to save drawing.", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveDrawing() // Retry saving
            } else {
                Toast.makeText(this, "Permission denied. Cannot save drawing.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveBitmapToGallery(bitmap: Bitmap): Boolean {
        val filename = "DoodleCraft_${System.currentTimeMillis()}.jpg"
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val imageUri = contentResolver.insert(collection, values) ?: return false

        return try {
            contentResolver.openOutputStream(imageUri).use { outputStream ->
                if (outputStream == null) throw IOException("Failed to get output stream.")
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)) {
                    throw IOException("Failed to save bitmap.")
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                contentResolver.update(imageUri, values, null, null)
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            contentResolver.delete(imageUri, null, null)
            false
        }
    }

    private fun showBrushSizeDialog() {
        val dialogBinding = DialogBrushSizeBinding.inflate(layoutInflater)
        AlertDialog.Builder(this).apply {
            setTitle("Select Brush Size")
            setView(dialogBinding.root)
            setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            dialogBinding.seekbarBrushSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    binding.canvasView.setBrushSize(progress.toFloat())
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }.create().show()
    }

    private fun showColorPickerDialog() {
        val colors = arrayOf("Black", "Red", "Green", "Blue", "Yellow")
        val colorValues = intArrayOf(Color.BLACK, Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW)
        AlertDialog.Builder(this).apply {
            setTitle("Choose a color")
            setItems(colors) { _, which ->
                binding.canvasView.setBrushColor(colorValues[which])
            }
        }.create().show()
    }

    private fun showShapeSelectionDialog() {
        val shapes = arrayOf("Rectangle", "Circle", "Line")
        AlertDialog.Builder(this).apply {
            setTitle("Choose a shape")
            setItems(shapes) { _, which ->
                val selectedShape = when (which) {
                    0 -> ShapeType.RECTANGLE
                    1 -> ShapeType.CIRCLE
                    else -> ShapeType.LINE
                }
                binding.canvasView.setToolToShape(selectedShape)
            }
        }.create().show()
    }

    private fun showTextDialog() {
        val input = EditText(this).apply { inputType = InputType.TYPE_CLASS_TEXT }
        AlertDialog.Builder(this).apply {
            setTitle("Enter Text")
            setView(input)
            setPositiveButton("OK") { _, _ ->
                val text = input.text.toString()
                if (text.isNotEmpty()) binding.canvasView.setToolToText(text)
            }
            setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        }.show()
    }
}
