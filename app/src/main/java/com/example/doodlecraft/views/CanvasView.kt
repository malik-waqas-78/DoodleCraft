package com.example.doodlecraft.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.util.Stack
import kotlin.math.max
import kotlin.math.min

// --- Enums for Tools ---
enum class ToolMode { BRUSH, SHAPE, TEXT }
enum class ShapeType { RECTANGLE, CIRCLE, LINE }

class CanvasView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // --- State Variables ---
    private var currentPaint = createPaint()
    private var currentPath = Path()
    private var currentToolMode = ToolMode.BRUSH
    private var currentShapeType = ShapeType.RECTANGLE
    private var textToPlace: String? = null

    private var brushColor = Color.BLACK
    private var canvasBackgroundColor = Color.WHITE

    private var startX = 0f
    private var startY = 0f
    private var motionEventX = 0f
    private var motionEventY = 0f

    // --- Bitmap and Undo/Redo Stacks ---
    private var canvasBitmap: Bitmap? = null
    private lateinit var drawCanvas: Canvas
    private val undoStack = Stack<Bitmap>()
    private val redoStack = Stack<Bitmap>()

    // --- Setup ---
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            drawCanvas = Canvas(canvasBitmap!!)
            // Initial clear to set background color
            canvasBitmap?.eraseColor(canvasBackgroundColor)
            invalidate()
        }
    }

    private fun createPaint(): Paint {
        return Paint().apply {
            color = brushColor
            isAntiAlias = true
            strokeWidth = 10f
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }
    }

    // --- Drawing Logic ---
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvasBitmap?.let { canvas.drawBitmap(it, 0f, 0f, null) }

        // Draw preview of current action
        if (currentToolMode == ToolMode.BRUSH) {
            canvas.drawPath(currentPath, currentPaint)
        } else if (currentToolMode == ToolMode.SHAPE && startX != 0f) {
            drawShape(canvas, currentShapeType, createPreviewRect(), currentPaint)
        }
    }

    private fun drawShape(canvas: Canvas, type: ShapeType, rect: RectF, paint: Paint) {
        when (type) {
            ShapeType.RECTANGLE -> canvas.drawRect(rect, paint)
            ShapeType.CIRCLE -> canvas.drawOval(rect, paint)
            ShapeType.LINE -> canvas.drawLine(startX, startY, motionEventX, motionEventY, paint)
        }
    }

    private fun createPreviewRect(): RectF {
        return RectF(min(startX, motionEventX), min(startY, motionEventY), max(startX, motionEventX), max(motionEventY, startY))
    }

    // --- Touch Handling ---
    override fun onTouchEvent(event: MotionEvent): Boolean {
        motionEventX = event.x
        motionEventY = event.y

        when (currentToolMode) {
            ToolMode.BRUSH -> handlePathDrawing(event)
            ToolMode.SHAPE -> handleShapeDrawing(event)
            ToolMode.TEXT -> handleTextPlacement(event)
        }
        return true
    }

    private fun handlePathDrawing(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                saveStateToUndoStack()
                currentPath.reset()
                currentPath.moveTo(motionEventX, motionEventY)
            }
            MotionEvent.ACTION_MOVE -> currentPath.lineTo(motionEventX, motionEventY)
            MotionEvent.ACTION_UP -> {
                drawCanvas.drawPath(currentPath, currentPaint)
                currentPath.reset()
            }
        }
        invalidate()
    }

    private fun handleShapeDrawing(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                saveStateToUndoStack()
                startX = motionEventX
                startY = motionEventY
            }
            MotionEvent.ACTION_MOVE -> { /* Preview is handled in onDraw */ }
            MotionEvent.ACTION_UP -> {
                drawShape(drawCanvas, currentShapeType, createPreviewRect(), currentPaint)
                startX = 0f // Reset start points
                startY = 0f
            }
        }
        invalidate()
    }

    private fun handleTextPlacement(event: MotionEvent) {
        if (event.action == MotionEvent.ACTION_DOWN) {
            textToPlace?.let { text ->
                saveStateToUndoStack()
                drawCanvas.drawText(text, motionEventX, motionEventY, currentPaint)
                textToPlace = null
                setToolToBrush()
                invalidate()
            }
        }
    }

    // --- Undo/Redo and State Management ---
    private fun saveStateToUndoStack() {
        canvasBitmap?.let {
            undoStack.push(it.copy(Bitmap.Config.ARGB_8888, true))
            redoStack.clear()
        }
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            canvasBitmap?.let { redoStack.push(it.copy(Bitmap.Config.ARGB_8888, true)) }
            canvasBitmap = undoStack.pop()
            drawCanvas = Canvas(canvasBitmap!!)
            invalidate()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            canvasBitmap?.let { undoStack.push(it.copy(Bitmap.Config.ARGB_8888, true)) }
            canvasBitmap = redoStack.pop()
            drawCanvas = Canvas(canvasBitmap!!)
            invalidate()
        }
    }

    fun clearCanvas() {
        saveStateToUndoStack() // Save the current state before clearing
        canvasBitmap?.eraseColor(canvasBackgroundColor)
        invalidate()
    }

    // --- Public API for MainActivity ---
    fun getDrawingAsBitmap(): Bitmap? = canvasBitmap

    fun setBrushColor(newColor: Int) {
        brushColor = newColor
        currentPaint.color = brushColor
    }

    fun setBrushSize(newSize: Float) {
        currentPaint.strokeWidth = newSize
        currentPaint.textSize = newSize * 3
    }

    fun setToolToBrush() {
        currentToolMode = ToolMode.BRUSH
        currentPaint.style = Paint.Style.STROKE
        currentPaint.color = brushColor
    }

    fun setToolToEraser() {
        currentToolMode = ToolMode.BRUSH
        currentPaint.style = Paint.Style.STROKE
        currentPaint.color = canvasBackgroundColor
    }

    fun setToolToShape(shapeType: ShapeType) {
        currentToolMode = ToolMode.SHAPE
        currentShapeType = shapeType
        currentPaint.style = Paint.Style.STROKE
        currentPaint.color = brushColor
    }

    fun setToolToText(text: String) {
        currentToolMode = ToolMode.TEXT
        textToPlace = text
        currentPaint.style = Paint.Style.FILL
        currentPaint.color = brushColor
        currentPaint.textSize = 60f
    }
}
