package com.tcanvas.timecanvas.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
import com.tcanvas.timecanvas.render.fms.config.FmsRenderConfig
import com.tcanvas.timecanvas.render.fms.core.FmsEngine
import com.tcanvas.timecanvas.render.fms.model.TrackSegment
import com.tcanvas.timecanvas.render.fms.painter.GlassNeonPathPainter

@Composable
fun FmsPreviewCanvas(
    trackSegment: TrackSegment,
    modifier: Modifier = Modifier
) {
    val config = FmsRenderConfig.Global
    val painter = remember { GlassNeonPathPainter(config) }
    val smoothPoints = remember(trackSegment) {
        FmsEngine.Instance.buildSmoothInterpolatedPath(trackSegment.pointList)
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvas = drawContext.canvas.nativeCanvas
        painter.drawPath(canvas, smoothPoints)
    }
}
