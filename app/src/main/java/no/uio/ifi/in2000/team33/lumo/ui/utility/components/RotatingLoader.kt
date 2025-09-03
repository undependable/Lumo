package no.uio.ifi.in2000.team33.lumo.ui.utility.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A simpler version of the rotating loader with a pizza slice design.
 * This implementation avoids complex clipping operations.
 */
@Composable
fun SimpleRotatingLoader(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    outerCircleColor: Color = MaterialTheme.colorScheme.primary,
    middleCircleColor: Color = Color(0xFFFCC00D),
    innerCircleColor: Color = Color(0xFFFC9C0D),
    purpleOuterColor: Color = Color(0xFFEADDFF),
    purpleMiddleColor: Color = Color(0xFFCBB6FF),
    purpleInnerColor: Color = Color(0xFF532C99),
    sliceAngle: Float = 95f,
    animationDuration: Int = 1500
) {
    // Create an infinite rotation animation
    val infiniteTransition = rememberInfiniteTransition(label = "loaderRotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(animationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            // Draw with the current rotation
            rotate(rotation) {
                val centerX = size.toPx() / 2
                val centerY = size.toPx() / 2

                // Draw the yellow/orange circles
                drawCircle(outerCircleColor, radius = size.toPx() / 2)
                drawCircle(middleCircleColor, radius = size.toPx() * 0.375f)
                drawCircle(innerCircleColor, radius = size.toPx() * 0.208f)

                // Draw the purple arc sections (approximately 95 degrees)
                drawPurpleSection(
                    centerX = centerX,
                    centerY = centerY,
                    startAngle = 0f, // Start at the right middle (0 degrees)
                    sweepAngle = sliceAngle, // Cover 95 degrees
                    maxRadius = size.toPx() / 2,
                    purpleOuterColor = purpleOuterColor,
                    purpleMiddleColor = purpleMiddleColor,
                    purpleInnerColor = purpleInnerColor
                )
            }
        }
    }
}

/**
 * Draws the purple section directly using arcs
 */
private fun DrawScope.drawPurpleSection(
    centerX: Float,
    centerY: Float,
    startAngle: Float,
    sweepAngle: Float,
    maxRadius: Float,
    purpleOuterColor: Color,
    purpleMiddleColor: Color,
    purpleInnerColor: Color
) {
    // Draw the outer purple arc
    drawArc(
        color = purpleOuterColor,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = true,
        topLeft = Offset(centerX - maxRadius, centerY - maxRadius),
        size = androidx.compose.ui.geometry.Size(maxRadius * 2, maxRadius * 2)
    )

    // Draw the middle purple arc
    val middleRadius = maxRadius * 0.75f
    drawArc(
        color = purpleMiddleColor,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = true,
        topLeft = Offset(centerX - middleRadius, centerY - middleRadius),
        size = androidx.compose.ui.geometry.Size(middleRadius * 2, middleRadius * 2)
    )

    // Draw the inner purple arc
    val innerRadius = maxRadius * 0.4166f
    drawArc(
        color = purpleInnerColor,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = true,
        topLeft = Offset(centerX - innerRadius, centerY - innerRadius),
        size = androidx.compose.ui.geometry.Size(innerRadius * 2, innerRadius * 2)
    )
}
