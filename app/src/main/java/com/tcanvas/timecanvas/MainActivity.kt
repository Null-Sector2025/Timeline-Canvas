package com.tcanvas.timecanvas

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.skydoves.cloudy.CloudyBox
import com.tcanvas.timecanvas.render.fms.core.FmsEngine
import com.tcanvas.timecanvas.render.fms.export.TrackExporter
import com.tcanvas.timecanvas.render.fms.model.TrackSegment
import com.tcanvas.timecanvas.render.fms.painter.GlassNeonPathPainter
import com.tcanvas.timecanvas.service.FloatRecordService
import com.tcanvas.timecanvas.ui.FmsPreviewCanvas

val GlassBg = Color(0xCC282D40)
val NeonBlue = Color(0xFF00D4FF)
val DarkBg = Color(0xFF080A12)

class MainActivity : ComponentActivity() {
    private lateinit var exporter: TrackExporter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val painter = GlassNeonPathPainter(com.tcanvas.timecanvas.render.fms.config.FmsRenderConfig.Global)
        exporter = TrackExporter(painter)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(background = DarkBg)) {
                MainGlassPage()
            }
        }
    }

    @Composable
    fun MainGlassPage() {
        var isRecording by remember { mutableStateOf(false) }
        var currentTrack by remember { mutableStateOf(TrackSegment()) }
        val btnScale by animateFloatAsState(targetValue = if (isRecording) 0.92f else 1f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            CloudyBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                radius = 22,
                backgroundColor = GlassBg
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.app_name),
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.slogan),
                        color = Color(0xFFA8B2D3),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = stringResource(R.string.engine_mark),
                        color = NeonBlue,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // FMS轨迹预览画布
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                FmsPreviewCanvas(trackSegment = currentTrack)
            }

            Spacer(modifier = Modifier.height(36.dp))

            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .clickable {
                        isRecording = !isRecording
                        if (isRecording) startFloatService() else {
                            stopFloatService()
                            currentTrack = FmsEngine.Instance.finishRecord()
                        }
                    }
                    .alpha(btnScale)
            ) {
                CloudyBox(
                    modifier = Modifier.fillMaxSize(),
                    radius = 30,
                    backgroundColor = if (isRecording) NeonBlue.copy(alpha = 0.4f) else GlassBg
                ) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = "",
                        tint = Color.White,
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.Center)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = if (isRecording) stringResource(R.string.status_recording)
                else stringResource(R.string.status_idle),
                color = if (isRecording) NeonBlue else Color(0xFFA8B2D3)
            )

            Spacer(modifier = Modifier.height(40.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                GlassTextBtn(stringResource(R.string.mark_point))
                GlassTextBtn(stringResource(R.string.pause_record))
                GlassTextBtn(stringResource(R.string.stop_save))
                GlassTextBtn("导出PNG") {
                    val bitmap = exporter.exportToBitmap(currentTrack)
                    exporter.saveBitmapToPng(bitmap, "track_${System.currentTimeMillis()}")
                }
            }

            Spacer(modifier = Modifier.height(50.dp))

            CloudyBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
                    .clip(RoundedCornerShape(12.dp)),
                radius = 15,
                backgroundColor = GlassBg
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(
                        stringResource(R.string.tab_home),
                        stringResource(R.string.tab_gallery),
                        stringResource(R.string.tab_edit),
                        stringResource(R.string.tab_setting),
                        stringResource(R.string.tab_about)
                    ).forEach {
                        Text(
                            text = it,
                            color = Color(0xFFD0D8FF),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun GlassTextBtn(text: String, onClick: () -> Unit = {}) {
        CloudyBox(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            radius = 12,
            backgroundColor = GlassBg
        ) {
            Text(text = text, color = Color.White, style = MaterialTheme.typography.labelSmall)
        }
    }

    private fun startFloatService() {
        val intent = Intent(this, FloatRecordService::class.java)
        startForegroundService(intent)
    }

    private fun stopFloatService() {
        val intent = Intent(this, FloatRecordService::class.java)
        stopService(intent)
    }
}
