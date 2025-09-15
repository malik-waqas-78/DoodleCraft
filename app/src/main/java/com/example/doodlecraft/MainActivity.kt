package com.example.doodlecraft

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import com.example.doodlecraft.databinding.ActivityMainBinding
import com.example.doodlecraft.databinding.DialogBrushSizeBinding
import com.example.doodlecraft.views.ShapeType

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnBrush.setOnClickListener {
            binding.canvasView.setToolToBrush()
        }
        binding.btnEraser.setOnClickListener {
            binding.canvasView.setToolToEraser()
        }
        binding.btnShapes.setOnClickListener {
            showShapeSelectionDialog()
        }
        binding.btnText.setOnClickListener {
            showTextDialog()
        }
        binding.btnSize.setOnClickListener {
            showBrushSizeDialog()
        }
        binding.btnColor.setOnClickListener {
            showColorPickerDialog()
        }
        binding.btnUndo.setOnClickListener {
            binding.canvasView.undo()
        }
        binding.btnRedo.setOnClickListener {
            binding.canvasView.redo()
        }
    }

    private fun showBrushSizeDialog() {
        val dialogBinding = DialogBrushSizeBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Brush Size")
        builder.setView(dialogBinding.root)
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }

        dialogBinding.seekbarBrushSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.canvasView.setBrushSize(progress.toFloat())
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        builder.create().show()
    }

    private fun showColorPickerDialog() {
        val colors = arrayOf("Black", "Red", "Green", "Blue", "Yellow")
        val colorValues = intArrayOf(Color.BLACK, Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW)

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose a color")
        builder.setItems(colors) { _, which ->
            val selectedColor = colorValues[which]
            binding.canvasView.setBrushColor(selectedColor)
        }
        builder.create().show()
    }

    private fun showShapeSelectionDialog() {
        val shapes = arrayOf("Rectangle", "Circle", "Line")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose a shape")
        builder.setItems(shapes) { _, which ->
            val selectedShape = when (which) {
                0 -> ShapeType.RECTANGLE
                1 -> ShapeType.CIRCLE
                else -> ShapeType.LINE
            }
            binding.canvasView.setToolToShape(selectedShape)
        }
        builder.create().show()
    }

    private fun showTextDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Enter Text")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setPositiveButton("OK") { _, _ ->
            val text = input.text.toString()
            if (text.isNotEmpty()) {
                binding.canvasView.setToolToText(text)
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }
}
