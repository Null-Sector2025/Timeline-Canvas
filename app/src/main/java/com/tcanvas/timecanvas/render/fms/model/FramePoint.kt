package com.tcanvas.timecanvas.render.fms.model

import android.graphics.PointF

/**
 * FMS 单帧采样点
 * @param x 屏幕X坐标
 * @param y 屏幕Y坐标
 * @param timeStamp 相对录制起始时间戳 ms
 * @param pressure 触控压力 0~1
 * @param isKeyPoint 是否关键采样点（压缩优化标记）
 */
data class FramePoint(
    val x: Float,
    val y: Float,
    val timeStamp: Long,
    val pressure: Float = 1f,
    val isKeyPoint: Boolean = false
) {
    fun toPointF(): PointF = PointF(x, y)
}

/**
 * 整条录制轨迹段落
 */
data class TrackSegment(
    val pointList: MutableList<FramePoint> = mutableListOf(),
    val segmentId: Long = System.currentTimeMillis()
)
