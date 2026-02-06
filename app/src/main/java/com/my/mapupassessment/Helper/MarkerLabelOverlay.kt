package com.my.mapupassessment.Helper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.RectF
import android.graphics.Typeface
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay

class MarkerLabelOverlay(
    private val geoPoint: GeoPoint,
    private val label: String
) : Overlay() {

    var tag: String? = null

    private val bgPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 32f
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
    }

    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        if (shadow) return

        val point = Point()
        mapView.projection.toPixels(geoPoint, point)

        val padding = 12
        val textWidth = textPaint.measureText(label)

        val left = point.x - textWidth / 2 - padding
        val top: Float = (point.y - 90).toFloat()
        val right = point.x + textWidth / 2 + padding
        val bottom = top + 50

        canvas.drawRoundRect(
            RectF(left, top, right, bottom),
            16f,
            16f,
            bgPaint
        )

        canvas.drawText(
            label,
            left + padding,
            bottom - 15,
            textPaint
        )
    }
}
