package com.example.doodlecraft.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.max
import kotlin.math.min

// --- Data classes and Enums for drawing ---

enum class ToolMode {
    BRUSH, SHAPE, TEXT
}

enum class ShapeType {
    RECTANGLE, CIRCLE, LINE
}

sealed class DrawingAction {
    data class PathAction(val path: Path, val paint: Paint) : DrawingAction()
    data class ShapeAction(val type: ShapeType, val rect: RectF, val paint: Paint) : DrawingAction()
    data class TextAction(val text: String, val x: Float, val y: Float, val paint: Paint) : DrawingAction()
}

// --- CanvasView Implementation ---

class CanvasView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // --- State Variables ---
    private var currentPaint = createPaint()
    private var currentPath = Path()
    private var currentToolMode = ToolMode.BRUSH
    private var currentShapeType = ShapeType.RECTANGLE
    private var textToPlace: String? = null

    private val completedActions = mutableListOf<DrawingAction>()
    private val undoneActions = mutableListOf<DrawingAction>()

    private var brushColor = Color.BLACK
    private var canvasBackgroundColor = Color.WHITE

    private var startX = 0f
    private var startY = 0f
    private var previewRect: RectF? = null

    // --- Core Drawing Logic ---

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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(canvasBackgroundColor)

        for (action in completedActions) {
            drawAction(canvas, action)
        }

        if (currentToolMode == ToolMode.BRUSH) {
            canvas.drawPath(currentPath, currentPaint)
        } else if (currentToolMode == ToolMode.SHAPE) {
            previewRect?.let { drawShape(canvas, currentShapeType, it, currentPaint) }
        }
    }

    private fun drawAction(canvas: Canvas, action: DrawingAction) {
        when (action) {
            is DrawingAction.PathAction -> canvas.drawPath(action.path, action.paint)
            is DrawingAction.ShapeAction -> drawShape(canvas, action.type, action.rect, action.paint)
            is DrawingAction.TextAction -> canvas.drawText(action.text, action.x, action.y, action.paint)
        }
    }

    private fun drawShape(canvas: Canvas, type: ShapeType, rect: RectF, paint: Paint) {
        when (type) {
            ShapeType.RECTANGLE -> canvas.drawRect(rect, paint)
            ShapeType.CIRCLE -> canvas.drawOval(rect, paint)
            ShapeType.LINE -> canvas.drawLine(rect.left, rect.top, rect.right, rect.bottom, paint)
        }
    }

    // --- Touch Event Handling ---

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (currentToolMode) {
            ToolMode.BRUSH -> handlePathDrawing(event)
            ToolMode.SHAPE -> handleShapeDrawing(event)
            ToolMode.TEXT -> handleTextPlacement(event)
        }
        return true
    }

    private fun handlePathDrawing(event: MotionEvent) {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                currentPath.reset()
                currentPath.moveTo(x, y)
            }
            MotionEvent.ACTION_MOVE -> currentPath.lineTo(x, y)
            MotionEvent.ACTION_UP -> {
                completedActions.add(DrawingAction.PathAction(currentPath, Paint(currentPaint)))
                undoneActions.clear()
                currentPath = Path()
            }
        }
        invalidate()
    }

    private fun handleShapeDrawing(event: MotionEvent) {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = x
                startY = y
                previewRect = RectF(x, y, x, y)
            }
            MotionEvent.ACTION_MOVE -> {
                previewRect?.set(min(startX, x), min(startY, y), max(startX, x), max(startY, y))
                if (currentShapeType == ShapeType.LINE) {
                    previewRect?.right = x
                    previewRect?.bottom = y
                }
            }
            MotionEvent.ACTION_UP -> {
                previewRect?.let {
                    val finalRect = if (currentShapeType == ShapeType.LINE) RectF(startX, startY, x, y) else it
                    completedActions.add(DrawingAction.ShapeAction(currentShapeType, finalRect, Paint(currentPaint)))
                    undoneActions.clear()
                }
                previewRect = null
            }
        }
        invalidate()
    }

    private fun handleTextPlacement(event: MotionEvent) {
        if (event.action == MotionEvent.ACTION_DOWN) {
            textToPlace?.let { text ->
                completedActions.add(DrawingAction.TextAction(text, event.x, event.y, Paint(currentPaint)))
                undoneActions.clear()
                textToPlace = null
                setToolToBrush() // Revert to brush tool
                invalidate()
            }
        }
    }

    // --- Public API for MainActivity ---

    fun setBrushColor(newColor: Int) {
        brushColor = newColor
        currentPaint.color = brushColor
    }

    fun setBrushSize(newSize: Float) {
        currentPaint.strokeWidth = newSize
        currentPaint.textSize = newSize * 3 // Make text size proportional
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
        currentPaint.textSize = 60f // Default text size
    }

    fun clearCanvas() {
        completedActions.clear()
        undoneActions.clear()
        currentPath.reset()
        previewRect = null
        invalidate()
    }

    fun undo() {
        if (completedActions.isNotEmpty()) {
            undoneActions.add(completedActions.removeAt(completedActions.size - 1))
            invalidate()
        }
    }

    fun redo() {
        if (undoneActions.isNotEmpty()) {
            val lastUndoneAction = undoneActions.removeAt(undoneActions.size - 1)
            completedActions.add(lastUndoneAction)
            invalidate()
        }
    }
}
