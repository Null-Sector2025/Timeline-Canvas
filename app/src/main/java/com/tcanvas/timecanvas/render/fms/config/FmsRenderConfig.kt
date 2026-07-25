package com.tcanvas.timecanvas.render.fms.config

import android.graphics.Color

/**
 * FMS全局渲染参数配置
 * 主界面设置页、悬浮录制实时调整
 */
data class FmsRenderConfig(
    // 采样
    var sampleIntervalMs: Long = 8,
    var enableCompress: Boolean = true,
    var compressThreshold: Float = 1.2f,

    // 线条基础
    var baseLineWidth: Float = 4.5f,
    var pressureWeightEnable: Boolean = true,

    // 霓虹发光科技效果
    var mainNeonColor: Int = Color.argb(255, 0, 212, 255),
    var glowColor: Int = Color.argb(80, 0, 212, 255),
    var glowRadius: Float = 18f,

    // 毛玻璃混合渲染
    var glassBlurLevel: Int = 25,
    var backgroundTransparent: Boolean = true,

    // 动画回放速度
    var playSpeed: Float = 1f
) {
    companion object {
        // 默认全局单例配置
        val Global = FmsRenderConfig()
    }
}
