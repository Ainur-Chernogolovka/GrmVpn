package com.v2ray.ang.ui.main

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Decorative background: falling code, dotted "globe" and a network of connections. */
@Composable
internal fun GrmBackdrop(accent: Color, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().height(460.dp)) {
        GlobeAndNetwork(accent)
        MatrixRain(accent)
    }
}

@Composable
private fun GlobeAndNetwork(accent: Color) {
    Canvas(modifier = Modifier.fillMaxWidth().height(460.dp)) {
        val spacing = 14.dp.toPx()
        val cx = size.width / 2f
        val cy = 250.dp.toPx()
        val rx = 300.dp.toPx()
        val ry = 220.dp.toPx()
        val strong = ArrayList<Offset>()
        val middle = ArrayList<Offset>()
        val faint = ArrayList<Offset>()
        var y = 0f
        while (y < size.height) {
            var x = 0f
            while (x < size.width) {
                val dx = (x - cx) / rx
                val dy = (y - cy) / ry
                val d = dx * dx + dy * dy
                when {
                    d < 0.25f -> strong.add(Offset(x, y))
                    d < 0.55f -> middle.add(Offset(x, y))
                    d < 1f -> faint.add(Offset(x, y))
                }
                x += spacing
            }
            y += spacing
        }
        val dot = Color(0xFF82D2FF)
        drawPoints(faint, PointMode.Points, dot.copy(alpha = 0.12f), 2.5f, StrokeCap.Round)
        drawPoints(middle, PointMode.Points, dot.copy(alpha = 0.26f), 2.5f, StrokeCap.Round)
        drawPoints(strong, PointMode.Points, dot.copy(alpha = 0.42f), 2.5f, StrokeCap.Round)

        // Network arcs, designed on a 390 px wide canvas.
        val k = size.width / 390f
        val top = 120.dp.toPx()
        fun p(x: Float, y: Float) = Offset(x * k, top + y * k)
        val arcs = Path().apply {
            quad(p(30f, 250f), p(120f, 80f), p(230f, 130f))
            quad(p(230f, 130f), p(320f, 90f), p(360f, 200f))
            quad(p(60f, 140f), p(150f, 190f), p(230f, 130f))
            quad(p(120f, 300f), p(200f, 200f), p(330f, 280f))
            quad(p(20f, 180f), p(80f, 120f), p(150f, 150f))
        }
        drawPath(arcs, GrmColors.Cyan.copy(alpha = 0.38f), style = Stroke(width = 1.2f * k))
        val nodes = listOf(
            p(30f, 250f), p(230f, 130f), p(360f, 200f), p(60f, 140f),
            p(120f, 300f), p(330f, 280f), p(20f, 180f), p(150f, 150f),
        )
        for (node in nodes) {
            drawCircle(GrmColors.Cyan, radius = 2.6f * k, center = node)
        }
        for (i in intArrayOf(1, 2, 3, 5)) {
            drawCircle(accent.copy(alpha = 0.22f), radius = 7f * k, center = nodes[i])
        }
    }
}

/** Quadratic curve written as a cubic one, so it works with every Compose version. */
private fun Path.quad(from: Offset, control: Offset, to: Offset) {
    moveTo(from.x, from.y)
    cubicTo(
        from.x + (control.x - from.x) * 2f / 3f,
        from.y + (control.y - from.y) * 2f / 3f,
        to.x + (control.x - to.x) * 2f / 3f,
        to.y + (control.y - to.y) * 2f / 3f,
        to.x,
        to.y,
    )
}

private val RainColumns = listOf(
    "0110100101101001011010010110100101101001",
    "7F3A9C01B8E2D45A6C90F1E37B2A8D04",
    "1001011100101101001110100101101001011",
    "0010110100110101001011010011010100101101",
    "A4E19B07C3D852F6A1B04E9D7C35",
    "1101001010110100101101001011010010110",
    "0110011010010110100101101001011010",
    "5B2E90C1F7A43D68E0B5",
).map { column -> column.toList().joinToString("\n") }

@Composable
private fun MatrixRain(accent: Color) {
    val columns = remember { RainColumns }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(430.dp)
            .clipToBounds()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                drawContent()
                drawRect(
                    brush = Brush.verticalGradient(listOf(Color.Black, Color.Transparent)),
                    blendMode = BlendMode.DstIn,
                )
            }
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        columns.forEachIndexed { index, text ->
            Text(
                text = text,
                color = (if (index % 3 == 2) GrmColors.Cyan else accent).copy(alpha = 0.15f),
                fontFamily = GrmFonts.Mono,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                modifier = Modifier.padding(top = (index * 13 % 50).dp),
            )
        }
    }
}
