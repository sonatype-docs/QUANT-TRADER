package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalQuantKitColors
import com.example.ui.theme.PnlPositive
import com.example.ui.theme.PnlPositiveCyan
import com.example.ui.theme.SunsetOrange

@Composable
fun SevenDayEquityCurve(
    realizedPnl: Double = 0.0,
    totalTrades: Int = 0,
    modifier: Modifier = Modifier
) {
    val colors = LocalQuantKitColors.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(90.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.canvas.copy(alpha = 0.6f))
            .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            if (totalTrades == 0 || realizedPnl == 0.0) {
                // Zero PnL Baseline - Flat Line
                val midY = height * 0.5f
                val dashed = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                drawLine(
                    color = colors.border,
                    start = Offset(0f, midY),
                    end = Offset(width, midY),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = dashed
                )
                // Baseline indicator point
                drawCircle(
                    color = SunsetOrange,
                    radius = 4.dp.toPx(),
                    center = Offset(width * 0.95f, midY)
                )
            } else {
                // Dynamic curve based on real performance
                val isPositive = realizedPnl >= 0
                val curveColor = if (isPositive) PnlPositive else Color(0xFFEF4444)

                val points = if (isPositive) {
                    listOf(
                        Offset(0f, height * 0.70f),
                        Offset(width * 0.25f, height * 0.65f),
                        Offset(width * 0.50f, height * 0.50f),
                        Offset(width * 0.75f, height * 0.40f),
                        Offset(width, height * 0.25f)
                    )
                } else {
                    listOf(
                        Offset(0f, height * 0.30f),
                        Offset(width * 0.25f, height * 0.35f),
                        Offset(width * 0.50f, height * 0.55f),
                        Offset(width * 0.75f, height * 0.65f),
                        Offset(width, height * 0.75f)
                    )
                }

                val strokePath = Path().apply {
                    moveTo(points[0].x, points[0].y)
                    for (i in 0 until points.size - 1) {
                        val p0 = points[i]
                        val p1 = points[i + 1]
                        val cx = (p0.x + p1.x) / 2f
                        cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                    }
                }

                val fillPath = Path().apply {
                    addPath(strokePath)
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            curveColor.copy(alpha = 0.25f),
                            curveColor.copy(alpha = 0.0f)
                        ),
                        startY = 0f,
                        endY = height
                    )
                )

                drawPath(
                    path = strokePath,
                    color = curveColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                val endPoint = points.last()
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = endPoint
                )
                drawCircle(
                    color = curveColor,
                    radius = 3.5.dp.toPx(),
                    center = endPoint
                )
            }
        }
    }
}

@Composable
fun ComparativeEquityTrajectoryChart(
    showCurveA: Boolean,
    showCurveB: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = LocalQuantKitColors.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.canvas)
            .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Horizontal coordinate guidelines
            val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
            val gridColor = colors.borderSubtle

            for (i in 1..3) {
                val y = height * (i * 0.25f)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = dashedEffect
                )
            }

            // Base axis line
            drawLine(
                color = colors.border,
                start = Offset(0f, height - 1.dp.toPx()),
                end = Offset(width, height - 1.dp.toPx()),
                strokeWidth = 1.dp.toPx()
            )

            // Strategy B (BTC Turtle - Cyan, explosive trend)
            if (showCurveB) {
                val turtlePoints = listOf(
                    Offset(0f, height * 0.90f),
                    Offset(width * 0.15f, height * 0.88f),
                    Offset(width * 0.30f, height * 0.82f),
                    Offset(width * 0.45f, height * 0.70f),
                    Offset(width * 0.60f, height * 0.55f),
                    Offset(width * 0.75f, height * 0.40f),
                    Offset(width * 0.88f, height * 0.25f),
                    Offset(width, height * 0.12f)
                )

                val bPath = Path().apply {
                    moveTo(turtlePoints[0].x, turtlePoints[0].y)
                    for (i in 0 until turtlePoints.size - 1) {
                        val p0 = turtlePoints[i]
                        val p1 = turtlePoints[i + 1]
                        val cx = (p0.x + p1.x) / 2f
                        cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                    }
                }

                val bArea = Path().apply {
                    addPath(bPath)
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }

                drawPath(
                    path = bArea,
                    brush = Brush.verticalGradient(
                        colors = listOf(PnlPositiveCyan.copy(alpha = 0.25f), Color.Transparent),
                        startY = 0f,
                        endY = height
                    )
                )

                drawPath(
                    path = bPath,
                    color = PnlPositiveCyan,
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )

                // High watermark & ending dots
                drawCircle(color = PnlPositiveCyan, radius = 4.dp.toPx(), center = Offset(width * 0.75f, height * 0.40f))
                drawCircle(color = Color.White, radius = 4.5.dp.toPx(), center = turtlePoints.last())
                drawCircle(color = PnlPositiveCyan, radius = 3.dp.toPx(), center = turtlePoints.last())
            }

            // Strategy A (Gold London ORB - Orange, steady consistent rise)
            if (showCurveA) {
                val orbPoints = listOf(
                    Offset(0f, height * 0.90f),
                    Offset(width * 0.18f, height * 0.80f),
                    Offset(width * 0.35f, height * 0.70f),
                    Offset(width * 0.52f, height * 0.60f),
                    Offset(width * 0.70f, height * 0.48f),
                    Offset(width * 0.86f, height * 0.36f),
                    Offset(width, height * 0.28f)
                )

                val aPath = Path().apply {
                    moveTo(orbPoints[0].x, orbPoints[0].y)
                    for (i in 0 until orbPoints.size - 1) {
                        val p0 = orbPoints[i]
                        val p1 = orbPoints[i + 1]
                        val cx = (p0.x + p1.x) / 2f
                        cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                    }
                }

                val aArea = Path().apply {
                    addPath(aPath)
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }

                drawPath(
                    path = aArea,
                    brush = Brush.verticalGradient(
                        colors = listOf(SunsetOrange.copy(alpha = 0.30f), Color.Transparent),
                        startY = 0f,
                        endY = height
                    )
                )

                drawPath(
                    path = aPath,
                    color = SunsetOrange,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Ending point
                drawCircle(color = Color.White, radius = 5.dp.toPx(), center = orbPoints.last())
                drawCircle(color = SunsetOrange, radius = 3.5.dp.toPx(), center = orbPoints.last())
            }
        }
    }
}
