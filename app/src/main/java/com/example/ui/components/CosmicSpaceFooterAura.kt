package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Star particle data model for organic cosmic movement and twinkling
 */
private data class CosmicStar(
    var x: Float, // Normalized 0f..1f
    var y: Float, // Normalized 0f..1f
    val vx: Float, // Drift speed X
    val vy: Float, // Drift speed Y
    val baseRadius: Float, // Star core radius (dp)
    val color: Color, // Star tint
    val twinkleSpeed: Float, // Twinkle frequency
    var phase: Float, // Current oscillation phase
    val hasFlare: Boolean = false // Diamond flare glow
)

/**
 * 🌌 Cosmic Space Footer Aura (التصميم الأسطوري الكوني لخلفية الفوتر)
 *
 * Feature Highlights:
 * 1. Deep cosmic black shadow that launches upwards from behind the footer and breathes smoothly (expands and contracts with zero sharp edges).
 * 2. Organic floating stars that glide randomly across the cosmic aura above and around the footer.
 * 3. Stars smoothly fade in and out continuously without sudden pop or visual artifacts.
 * 4. Unlocks after reaching 500 read chapters or instantly for Admins/Supervisors.
 * 5. Full on/off control via settings and quick toggle.
 */
@Composable
fun CosmicSpaceFooterAura(
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    if (!enabled) return

    val infiniteTransition = rememberInfiniteTransition(label = "cosmic_footer_infinite")

    // Smooth breathing expansion & contraction of the cosmic black shadow bounds upwards
    val breathProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cosmic_footer_breath_height"
    )

    // Smooth lateral cosmic drift angle
    val cosmicDriftAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cosmic_footer_drift_angle"
    )

    // Star generation with deterministic organic seed
    val stars = remember {
        val rand = Random(42069)
        val palette = listOf(
            Color(0xFFFFFFFF), // Pure White Star
            Color(0xFFE0F7FA), // Nebula Cyan
            Color(0xFFFFF9C4), // Starlight Gold
            Color(0xFFEDE7F6), // Cosmic Violet
            Color(0xFF80D8FF), // Vivid Diamond Blue
            Color(0xFFFFD54F)  // Warm Golden Spark
        )

        List(32) { index ->
            CosmicStar(
                x = rand.nextFloat(),
                y = 0.05f + rand.nextFloat() * 0.92f,
                vx = (rand.nextFloat() - 0.5f) * 0.00015f,
                vy = (rand.nextFloat() - 0.55f) * 0.00018f, // Gentle upward/lateral drift
                baseRadius = 1.0f + rand.nextFloat() * 2.2f,
                color = palette[index % palette.size],
                twinkleSpeed = 0.8f + rand.nextFloat() * 1.6f,
                phase = rand.nextFloat() * (2 * PI.toFloat()),
                hasFlare = index % 5 == 0
            )
        }
    }

    // Frame ticker for smooth continuous organic star positions & continuous fading
    var frameTimeNanos by remember { mutableLongStateOf(0L) }
    LaunchedEffect(enabled) {
        while (enabled) {
            withFrameNanos { time ->
                frameTimeNanos = time
            }
        }
    }

    // Dynamic expanded height: smooth expansion upwards from 105dp to 165dp
    val dynamicAuraHeight = 105.dp + (60.dp * breathProgress)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(dynamicAuraHeight)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            if (canvasWidth <= 0f || canvasHeight <= 0f) return@Canvas

            // 1. 🌌 Soft Deep Cosmic Shadow with Organic Radial & Vertical Fade upwards from behind Footer
            val rad = Math.toRadians(cosmicDriftAngle.toDouble())
            val driftOffsetX = (cos(rad) * 40f).toFloat()

            // Vertical Cosmic Dark Shadow Gradient (Fading upwards into transparency, dense at footer base)
            val cosmicShadowBrush = Brush.verticalGradient(
                colors = listOf(
                    Color(0x00000000), // 100% smooth transparent fade at the top edge
                    Color(0xFF130F2A).copy(alpha = 0.35f),
                    Color(0xFF0B0E1E).copy(alpha = 0.65f),
                    Color(0xFF060914).copy(alpha = 0.88f),
                    Color(0xFF020306).copy(alpha = 0.96f)  // Dense cosmic black shadow behind footer
                ),
                startY = 0f,
                endY = canvasHeight
            )
            drawRect(brush = cosmicShadowBrush)

            // Cosmic Subtle Nebula Glow Blob (Gently pulsing around lower center)
            val nebulaCenterX = (canvasWidth * 0.5f) + driftOffsetX
            val nebulaCenterY = canvasHeight * 0.65f
            val nebulaRadius = (canvasWidth * 0.45f) * (0.9f + 0.2f * breathProgress)

            val nebulaBrush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF2A1654).copy(alpha = 0.30f + 0.15f * breathProgress),
                    Color(0xFF0F1E3D).copy(alpha = 0.20f),
                    Color(0x00000000)
                ),
                center = Offset(nebulaCenterX, nebulaCenterY),
                radius = nebulaRadius
            )
            drawCircle(brush = nebulaBrush, radius = nebulaRadius, center = Offset(nebulaCenterX, nebulaCenterY))

            // 2. ✨ Moving Organic Stars with Gentle Smooth Fade In and Out
            val timeSec = (frameTimeNanos / 1_000_000_000.0).toFloat()

            stars.forEach { star ->
                // Update position with organic drift
                star.x += star.vx
                if (star.x < 0f) star.x += 1f
                if (star.x > 1f) star.x -= 1f

                star.y += star.vy
                if (star.y < 0.05f) star.y += 0.90f
                if (star.y > 0.98f) star.y -= 0.90f

                // Smooth sinusoidal alpha oscillation (fades in and out without sudden cut)
                val oscillation = sin(timeSec * star.twinkleSpeed + star.phase)
                val normalizedOscillation = (oscillation + 1f) * 0.5f // 0..1

                // Vertical top-edge soft fade (stars smoothly vanish before reaching the top boundary)
                val topFade = ((star.y - 0.05f) / 0.35f).coerceIn(0f, 1f)
                val bottomFade = ((0.98f - star.y) / 0.15f).coerceIn(0f, 1f)
                val starAlpha = (normalizedOscillation * 0.85f + 0.15f) * topFade * bottomFade

                if (starAlpha > 0.02f) {
                    val px = star.x * canvasWidth
                    val py = star.y * canvasHeight
                    val radius = star.baseRadius * (0.8f + 0.4f * normalizedOscillation)

                    // Draw outer soft glow halo
                    drawCircle(
                        color = star.color.copy(alpha = starAlpha * 0.35f),
                        radius = radius * 2.6f,
                        center = Offset(px, py)
                    )

                    // Draw sharp luminous core
                    drawCircle(
                        color = star.color.copy(alpha = starAlpha),
                        radius = radius,
                        center = Offset(px, py)
                    )

                    // Draw 4-point diamond flare for prominent stars
                    if (star.hasFlare && starAlpha > 0.45f) {
                        val flareLength = radius * 3.8f * starAlpha
                        val flareAlpha = starAlpha * 0.6f
                        val flareColor = star.color.copy(alpha = flareAlpha)

                        drawLine(
                            color = flareColor,
                            start = Offset(px - flareLength, py),
                            end = Offset(px + flareLength, py),
                            strokeWidth = 1.2f
                        )
                        drawLine(
                            color = flareColor,
                            start = Offset(px, py - flareLength),
                            end = Offset(px, py + flareLength),
                            strokeWidth = 1.2f
                        )
                    }
                }
            }
        }
    }
}
