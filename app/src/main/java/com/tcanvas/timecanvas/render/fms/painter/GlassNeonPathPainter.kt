package com.tcanvas.timecanvas.render.fms.painter

import android.graphics.*
import com.tcanvas.timecanvas.render.fms.config.FmsRenderConfig
import com.tcanvas.timecanvas.render.fms.model.FramePoint

class GlassNeonPathPainter(private val config: FmsRenderConfig) {
    private val basePath = Path()
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        maskFilter = BlurMaskFilter(config.glowRadius, BlurMaskFilter.Blur.NORMAL)
    }

    /**
     批量绘制整条平滑轨迹
     */
    fun drawPath(canvas: Canvas, pointList: List<FramePoint>) {
        if (pointList.size < 2) return
        basePath.reset()
        basePath.moveTo(pointList[0].x, pointList[0].y)

        for (i in 1 until pointList.size) {
            basePath.lineTo(pointList[i].x, pointList[i].y)
        }

        // 外层发光
        glowPaint.color = config.glowColor
        glowPaint.strokeWidth = config.baseLineWidth + 6f
        canvas.drawPath(basePath, glowPaint)

        // 主霓虹线条
        linePaint.color = config.mainNeonColor
        linePaint.strokeWidth = config.baseLineWidth
        canvas.drawPath(basePath, linePaint)
    }

    /**
     逐点增量绘制（录制实时预览）
     */
    fun drawIncrement(canvas: Canvas, last: FramePoint, curr: FramePoint) {
        val tempPath = Path().apply {
            moveTo(last.x, last.y)
            lineTo(curr.x, curr.y)
        }
        glowPaint.color = config.glowColor
        glowPaint.strokeWidth = config.baseLineWidth + 6
        canvas.drawPath(tempPath, glowPaint)

        linePaint.color = config.mainNeonColor
        linePaint.strokeWidth = config.baseLineWidth
        canvas.drawPath(tempPath, linePaint)
    }

    fun updateConfig(newCfg: FmsRenderConfig) {
        config.glowRadius = newCfg.glowRadius
        config.mainNeonColor = newCfg.mainNeonColor
        config.glowColor = newCfg.glowColor
        config.baseLineWidth = newCfg.baseLineWidth
    }
}
