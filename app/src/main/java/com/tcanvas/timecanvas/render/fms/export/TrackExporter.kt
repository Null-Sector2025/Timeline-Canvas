package com.tcanvas.timecanvas.render.fms.export

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Environment
import com.tcanvas.timecanvas.render.fms.core.FmsEngine
import com.tcanvas.timecanvas.render.fms.model.TrackSegment
import com.tcanvas.timecanvas.render.fms.painter.GlassNeonPathPainter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

class TrackExporter(private val painter: GlassNeonPathPainter) {
    private val rootDir = File(Environment.getExternalStorageDirectory(), "TimeCanvas")
    private val recordDir = File(rootDir, "Records")
    private val exportDir = File(rootDir, "Export")

    init {
        rootDir.mkdirs()
        recordDir.mkdirs()
        exportDir.mkdirs()
    }

    /**
     保存原始轨迹 .tcanvas 私有格式
     */
    fun saveSourceFile(seg: TrackSegment): String {
        val targetFile = File(recordDir, "${seg.segmentId}.tcanvas")
        OutputStreamWriter(FileOutputStream(targetFile)).use { writer ->
            seg.pointList.forEach { p ->
                writer.write("${p.x},${p.y},${p.timeStamp},${p.pressure}\n")
            }
        }
        return targetFile.absolutePath
    }

    /**
     渲染整张静态高清图片
     */
    fun exportToBitmap(seg: TrackSegment, width: Int = 1080, height: Int = 2400): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(0xFF080A12.toInt())
        val smoothPoints = FmsEngine.Instance.buildSmoothInterpolatedPath(seg.pointList)
        painter.drawPath(canvas, smoothPoints)
        return bitmap
    }

    fun saveBitmapToPng(bitmap: Bitmap, name: String): String {
        val outFile = File(exportDir, "$name.png")
        FileOutputStream(outFile).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 95, it)
        }
        bitmap.recycle()
        return outFile.absolutePath
    }

    // 预留接口：后续GIF、MP4视频逐帧渲染合成
    fun exportGif(seg: TrackSegment) {}
    fun exportMp4(seg: TrackSegment) {}
}
