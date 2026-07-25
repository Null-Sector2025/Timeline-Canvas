package com.tcanvas.timecanvas.render.fms.core

import com.tcanvas.timecanvas.render.fms.config.FmsRenderConfig
import com.tcanvas.timecanvas.render.fms.model.FramePoint
import com.tcanvas.timecanvas.render.fms.model.TrackSegment
import kotlin.math.abs

class FmsEngine private constructor() {
    companion object {
        val Instance by lazy { FmsEngine() }
        val config = FmsRenderConfig.Global
    }

    private val rawPointBuffer = mutableListOf<FramePoint>()

    /**
     * 实时录入触摸点，录制悬浮窗持续调用
     */
    fun feedTouchPoint(x: Float, y: Float, pressure: Float = 1f) {
        val point = FramePoint(
            x = x,
            y = y,
            timeStamp = System.currentTimeMillis(),
            pressure = pressure
        )
        rawPointBuffer.add(point)
    }

    /**
     * 轨迹压缩：过滤冗余密集点，减小存储体积
     */
    fun compressSegment(segment: TrackSegment): TrackSegment {
        if (!config.enableCompress || segment.pointList.size < 3) return segment
        val output = mutableListOf<FramePoint>()
        output.add(segment.pointList.first())

        for (i in 1 until segment.pointList.lastIndex) {
            val prev = segment.pointList[i - 1]
            val curr = segment.pointList[i]
            val next = segment.pointList[i + 1]

            val dx1 = curr.x - prev.x
            val dy1 = curr.y - prev.y
            val dx2 = next.x - curr.x
            val dy2 = next.y - curr.y

            val deviation = abs(dx1 * dy2 - dy1 * dx2)
            if (deviation > config.compressThreshold) {
                output.add(curr.copy(isKeyPoint = true))
            }
        }
        output.add(segment.pointList.last())
        return segment.copy(pointList = output)
    }

    /**
     三阶贝塞尔平滑插值，生成顺滑曲线采样点
     */
    fun buildSmoothInterpolatedPath(points: List<FramePoint>): MutableList<FramePoint> {
        val smoothList = mutableListOf<FramePoint>()
        if (points.size < 2) return smoothList.apply { addAll(points) }

        smoothList.add(points.first())
        for (i in 0 until points.size - 1) {
            val p0 = if (i == 0) points[i] else points[i - 1]
            val p1 = points[i]
            val p2 = points[i + 1]
            val p3 = if (i + 2 >= points.size) points[i + 1] else points[i + 2]

            // 4点贝塞尔采样 20等分
            for (t in 1..20) {
                val ft = t / 20f
                val interpolated = bezier4(p0, p1, p2, p3, ft)
                smoothList.add(interpolated)
            }
        }
        smoothList.add(points.last())
        return smoothList
    }

    private fun bezier4(
        p0: FramePoint, p1: FramePoint, p2: FramePoint, p3: FramePoint, t: Float
    ): FramePoint {
        val mt = 1 - t
        val x = mt*mt*mt*p0.x + 3*mt*mt*t*p1.x + 3*mt*t*t*p2.x + t*t*t*p3.x
        val y = mt*mt*mt*p0.y + 3*mt*mt*t*p1.y + 3*mt*t*t*p2.y + t*t*t*p3.y
        val time = (p1.timeStamp * mt + p2.timeStamp * t).toLong()
        val press = p1.pressure * mt + p2.pressure * t
        return FramePoint(x, y, time, press)
    }

    /**
     结束一段录制，生成完整轨迹段并清空缓存
     */
    fun finishRecord(): TrackSegment {
        val seg = TrackSegment(pointList = rawPointBuffer.toMutableList())
        rawPointBuffer.clear()
        return compressSegment(seg)
    }

    fun clearAllBuffer() = rawPointBuffer.clear()
}
