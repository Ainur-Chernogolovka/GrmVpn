package com.v2ray.ang.ui.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform

/**
 * Big round power button: neon ring around a hooded emblem. Everything is drawn in a
 * 300x300 coordinate space and scaled to the size of the composable.
 */
@Composable
internal fun GrmPowerRing(connected: Boolean, modifier: Modifier = Modifier) {
    val accent by animateColorAsState(
        targetValue = if (connected) GrmColors.Green else GrmColors.Pink,
        animationSpec = tween(durationMillis = 500),
        label = "ringAccent",
    )
    val transition = rememberInfiniteTransition(label = "ringSpin")
    val spin by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "spin",
    )
    val spinBack by transition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 26000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "spinBack",
    )
    val shapes = remember { EmblemShapes() }
    val angle = if (connected) spin else 0f
    val angleBack = if (connected) spinBack else 0f

    Canvas(modifier = modifier) {
        val s = size.minDimension / 300f
        withTransform({ scale(s, s, pivot = Offset.Zero) }) {
            drawRing(accent, angle, angleBack)
            drawEmblem(shapes, accent, connected)
            drawPowerBadge(accent)
        }
    }
}

private val RingCenter = Offset(150f, 150f)

private fun DrawScope.drawRing(accent: Color, angle: Float, angleBack: Float) {
    drawCircle(GrmColors.Cyan.copy(alpha = 0.28f), radius = 146f, center = RingCenter, style = Stroke(width = 1f))
    rotate(degrees = angleBack, pivot = RingCenter) {
        drawCircle(
            color = GrmColors.Cyan.copy(alpha = 0.6f),
            radius = 134f,
            center = RingCenter,
            style = Stroke(width = 5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(1.5f, 7f))),
        )
    }
    rotate(degrees = angle, pivot = RingCenter) {
        val arcTopLeft = Offset(150f - 120f, 150f - 120f)
        val arcSize = Size(240f, 240f)
        for (i in 0 until 3) {
            val start = -100f + i * 120f
            drawArc(
                color = accent.copy(alpha = 0.25f),
                startAngle = start,
                sweepAngle = 102.6f,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = 14f, cap = StrokeCap.Round),
            )
            drawArc(
                color = accent,
                startAngle = start,
                sweepAngle = 102.6f,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = 6f, cap = StrokeCap.Round),
            )
        }
    }
    rotate(degrees = -angle * 0.6f, pivot = RingCenter) {
        drawCircle(
            color = GrmColors.Cyan.copy(alpha = 0.85f),
            radius = 108f,
            center = RingCenter,
            style = Stroke(
                width = 2f,
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(60f, 14f, 20f, 14f)),
            ),
        )
    }
}

private fun DrawScope.drawEmblem(shapes: EmblemShapes, accent: Color, connected: Boolean) {
    val disc = Brush.radialGradient(
        colors = if (connected) listOf(Color(0xFF1F4A8F), Color(0xFF0B1A4A)) else listOf(Color(0xFF2D3490), Color(0xFF0E1445)),
        center = Offset(150f, 126f),
        radius = 150f,
    )
    drawCircle(brush = disc, radius = 98f, center = RingCenter)
    drawCircle(accent.copy(alpha = 0.6f), radius = 98f, center = RingCenter, style = Stroke(width = 1.5f))

    val hoodFill = if (connected) Color(0xFF183D82) else Color(0xFF212876)
    val shoulderFill = if (connected) Color(0xFF14336F) else Color(0xFF1A2068)
    val discClip = Path().apply { addOval(Rect(center = RingCenter, radius = 98f)) }
    clipPath(discClip) {
        drawPath(shapes.shoulders, shoulderFill)
        drawPath(shapes.shoulders, accent.copy(alpha = 0.5f), style = Stroke(width = 1.2f))
        drawPath(shapes.hood, hoodFill)
        drawPath(shapes.hood, accent.copy(alpha = 0.8f), style = Stroke(width = 1.6f))
        drawPath(shapes.face, GrmColors.Void)
        drawPath(shapes.visor, accent.copy(alpha = 0.35f), style = Stroke(width = 9f, join = StrokeJoin.Round))
        drawPath(shapes.visor, accent)
    }
    drawPath(
        shapes.circuits,
        accent,
        style = Stroke(width = 1.6f, cap = StrokeCap.Round, join = StrokeJoin.Round),
    )
    for (node in shapes.nodes) {
        drawCircle(accent, radius = 2.4f, center = node)
    }
}

private fun DrawScope.drawPowerBadge(accent: Color) {
    val center = Offset(150f, 278f)
    drawCircle(Color(0xFF0E1445), radius = 20f, center = center)
    drawCircle(accent, radius = 20f, center = center, style = Stroke(width = 2f))
    drawLine(
        color = accent,
        start = Offset(150f, 267f),
        end = Offset(150f, 277f),
        strokeWidth = 2.4f,
        cap = StrokeCap.Round,
    )
    drawArc(
        color = accent,
        startAngle = -50f,
        sweepAngle = 280f,
        useCenter = false,
        topLeft = Offset(center.x - 9.2f, center.y - 9.2f),
        size = Size(18.4f, 18.4f),
        style = Stroke(width = 2.4f, cap = StrokeCap.Round),
    )
}

/** Paths of the hooded emblem, built once. Coordinates match the 300x300 space. */
private class EmblemShapes {
    val shoulders = Path().apply {
        moveTo(52f, 262f)
        cubicTo(70f, 214f, 108f, 204f, 150f, 204f)
        cubicTo(192f, 204f, 230f, 214f, 248f, 262f)
        close()
    }
    val hood = Path().apply {
        moveTo(150f, 58f)
        cubicTo(112f, 58f, 90f, 96f, 90f, 138f)
        lineTo(90f, 196f)
        cubicTo(90f, 209.33f, 97.33f, 218f, 112f, 222f)
        lineTo(188f, 222f)
        cubicTo(202.67f, 218f, 210f, 209.33f, 210f, 196f)
        lineTo(210f, 138f)
        cubicTo(210f, 96f, 188f, 58f, 150f, 58f)
        close()
    }
    val face = Path().apply {
        moveTo(150f, 82f)
        cubicTo(126f, 82f, 112f, 108f, 112f, 138f)
        lineTo(112f, 172f)
        cubicTo(137.33f, 190.67f, 162.67f, 190.67f, 188f, 172f)
        lineTo(188f, 138f)
        cubicTo(188f, 108f, 174f, 82f, 150f, 82f)
        close()
    }
    val visor = Path().apply {
        moveTo(120f, 136f)
        lineTo(150f, 146f)
        lineTo(180f, 136f)
        lineTo(176f, 150f)
        lineTo(150f, 158f)
        lineTo(124f, 150f)
        close()
    }
    val circuits = Path().apply {
        moveTo(90f, 118f); lineTo(72f, 118f); lineTo(66f, 110f)
        moveTo(90f, 140f); lineTo(70f, 140f)
        moveTo(90f, 162f); lineTo(74f, 162f); lineTo(68f, 170f)
        moveTo(210f, 118f); lineTo(228f, 118f); lineTo(234f, 110f)
        moveTo(210f, 140f); lineTo(230f, 140f)
        moveTo(210f, 162f); lineTo(226f, 162f); lineTo(232f, 170f)
    }
    val nodes = listOf(
        Offset(66f, 110f), Offset(70f, 140f), Offset(68f, 170f),
        Offset(234f, 110f), Offset(230f, 140f), Offset(232f, 170f),
    )
}
