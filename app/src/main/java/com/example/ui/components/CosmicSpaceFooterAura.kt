package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.StrokeCap
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Star Classification for Realistic Multi-Depth Cosmic Rendering
 */
private enum class CosmicStarKind {
    BRILLIANT_FLARE,   // Bright luminous star with realistic 4-point cross-diffraction flare
    CELESTIAL_SPARK,    // Mid-distance star with soft corona halo and organic scintillation
    DISTANT_STARDUST   // Pinpoint micro-dust star providing deep celestial depth
}

/**
 * Realistic Cosmic Star Particle Model
 */
private class RealisticCosmicStar(
    var x: Float, // Normalized 0f..1f
    var y: Float, // Normalized 0f..1f
    val vx: Float, // Organic drift speed X
    val vy: Float, // Organic drift speed Y
    val baseRadius: Float,
    val chromaticTint: Color, // Spectral star color
    val kind: CosmicStarKind,
    val twinkleSpeed: Float,
    val phase: Float,
    val flareSpan: Float = 0f
)

/**
 * 🌌 Cosmic Space Footer Aura (المظهر الكوني الثابت والواقعي لخلفية وأسفل الفوتر)
 *
 * المميزات والتحديثات الأسطورية:
 * 1. مظهر ثابت ومستقر 100%: لا يشغل مساحة ديناميكية ولا يسبب أي تحريك أو اهتزاز للفوتر.
 * 2. استغلال المساحة السفلية الفارغة تحت الفوتر وخلفه بالكامل بتدرج كوني عميق وجذاب.
 * 3. نجوم فلكية واقعية فائقة الجمال بدقة فيزيائية:
 *    - نجوم عملاقة متألقة ذات إشعاعات ضوئية رباعية (4-Point Diffraction Spikes) وهالة كورونا متدرجة.
 *    - نجوم متوسطة ذات وميض عضوي طبيعي (Scintillation) مستمر ومتناسق.
 *    - جسيمات غبار النجوم (Stardust) الدقيقة التي تمنح عمقاً فضائياً واقعياً ثلاثي الطبقات.
 * 4. شهاب كوني ناعم (Shooting Star / Meteor) يعبر فضاء الفوتر دورياً بذيل مضيء متلاشي بانسيابية.
 * 5. حركة هادئة وظهور واختفاء تدريجي فائق السلاسة دون أي انقطاع مفاجئ.
 */
@Composable
fun CosmicSpaceFooterAura(
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    if (!enabled) return

    val infiniteTransition = rememberInfiniteTransition(label = "cosmic_stars_transition")

    // Slow organic celestial drift phase
    val celestialDriftPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cosmic_drift_phase"
    )

    // Periodic Shooting Star / Meteor loop (every 14 seconds)
    val meteorProgress by infiniteTransition.animateFloat(
        initialValue = -0.3f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cosmic_meteor_progress"
    )

    // Generate balanced, realistic multi-depth starfield with deterministic organic seed
    val stars = remember {
        val rand = Random(554433)
        val spectralPalette = listOf(
            Color(0xFFFFFFFF), // Pure Brilliant White (Core Hot)
            Color(0xFFE0F7FA), // Diamond Ice Cyan (Class O/B)
            Color(0xFFFFF9C4), // Starlight Sunlit Gold (Class G)
            Color(0xFFE1BEE7), // Ethereal Violet Nebula
            Color(0xFF80D8FF), // Vivid Sapphire Spark (Class B)
            Color(0xFFFFD180), // Warm Amber Twilight (Class K)
            Color(0xFFB3E5FC)  // Soft Celestial Azure
        )

        val starList = mutableListOf<RealisticCosmicStar>()

        // 1. Brilliant Foreground Stars with Diffraction Spikes (7 stars)
        repeat(7) { i ->
            starList.add(
                RealisticCosmicStar(
                    x = 0.06f + rand.nextFloat() * 0.88f,
                    y = 0.08f + rand.nextFloat() * 0.84f,
                    vx = (rand.nextFloat() - 0.5f) * 0.00012f,
                    vy = (rand.nextFloat() - 0.5f) * 0.00008f,
                    baseRadius = 2.0f + rand.nextFloat() * 1.0f,
                    chromaticTint = spectralPalette[i % spectralPalette.size],
                    kind = CosmicStarKind.BRILLIANT_FLARE,
                    twinkleSpeed = 0.9f + rand.nextFloat() * 1.1f,
                    phase = rand.nextFloat() * (2 * PI.toFloat()),
                    flareSpan = 14f + rand.nextFloat() * 10f
                )
            )
        }

        // 2. Mid-distance Stars with Corona Halos (16 stars)
        repeat(16) { i ->
            starList.add(
                RealisticCosmicStar(
                    x = rand.nextFloat(),
                    y = 0.05f + rand.nextFloat() * 0.90f,
                    vx = (rand.nextFloat() - 0.5f) * 0.00016f,
                    vy = (rand.nextFloat() - 0.5f) * 0.00010f,
                    baseRadius = 1.2f + rand.nextFloat() * 0.8f,
                    chromaticTint = spectralPalette[(i + 2) % spectralPalette.size],
                    kind = CosmicStarKind.CELESTIAL_SPARK,
                    twinkleSpeed = 1.2f + rand.nextFloat() * 1.5f,
                    phase = rand.nextFloat() * (2 * PI.toFloat())
                )
            )
        }

        // 3. Deep Background Stardust Micro-Particles (24 particles)
        repeat(24) { i ->
            starList.add(
                RealisticCosmicStar(
                    x = rand.nextFloat(),
                    y = 0.04f + rand.nextFloat() * 0.92f,
                    vx = (rand.nextFloat() - 0.5f) * 0.00008f,
                    vy = (rand.nextFloat() - 0.5f) * 0.00006f,
                    baseRadius = 0.6f + rand.nextFloat() * 0.5f,
                    chromaticTint = spectralPalette[(i + 4) % spectralPalette.size],
                    kind = CosmicStarKind.DISTANT_STARDUST,
                    twinkleSpeed = 0.6f + rand.nextFloat() * 0.9f,
                    phase = rand.nextFloat() * (2 * PI.toFloat())
                )
            )
        }

        starList
    }

    // Frame ticker for smooth continuous movement & twinkling
    var frameTimeNanos by remember { mutableLongStateOf(0L) }
    LaunchedEffect(enabled) {
        while (enabled) {
            withFrameNanos { time ->
                frameTimeNanos = time
            }
        }
    }

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            if (canvasWidth <= 0f || canvasHeight <= 0f) return@Canvas

            // 1. 🌌 Deep Cosmic Space Backdrop with Soft Upper Fade
            // Seamlessly blends into the dark UI at top, deep cosmic navy/violet across and under footer
            val cosmicBackgroundBrush = Brush.verticalGradient(
                colors = listOf(
                    Color(0x00000000),             // Top edge 100% transparent feathering
                    Color(0x550B0E1E),             // Soft atmospheric upper entry
                    Color(0x99080B1A),             // Mid footer cosmic depth
                    Color(0xD9050714),             // Bottom footer dense cosmic darkness
                    Color(0xF202030A)              // Rich space void under the footer
                ),
                startY = 0f,
                endY = canvasHeight
            )
            drawRect(brush = cosmicBackgroundBrush)

            // Subtle gentle cosmic nebula cloud glow
            val driftShiftX = cos(celestialDriftPhase) * (canvasWidth * 0.08f)
            val nebulaCenter = Offset(
                x = canvasWidth * 0.5f + driftShiftX,
                y = canvasHeight * 0.70f
            )
            val nebulaRadius = canvasWidth * 0.55f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF311B92).copy(alpha = 0.18f),
                        Color(0xFF0D47A1).copy(alpha = 0.12f),
                        Color(0x00000000)
                    ),
                    center = nebulaCenter,
                    radius = nebulaRadius
                ),
                center = nebulaCenter,
                radius = nebulaRadius
            )

            val timeSec = (frameTimeNanos / 1_000_000_000.0).toFloat()

            // 2. ✨ Render Realistic Multi-Depth Stars
            stars.forEach { star ->
                // Drift position update
                star.x += star.vx
                if (star.x < 0f) star.x += 1f
                if (star.x > 1f) star.x -= 1f

                star.y += star.vy
                if (star.y < 0.02f) star.y += 0.96f
                if (star.y > 0.98f) star.y -= 0.96f

                // Non-linear organic twinkling scintillation
                val wave1 = sin(timeSec * star.twinkleSpeed + star.phase)
                val wave2 = sin(timeSec * (star.twinkleSpeed * 1.55f) + star.phase * 0.6f) * 0.35f
                val harmonic = (wave1 + wave2).coerceIn(-1f, 1f)
                val baseTwinkle = ((harmonic + 1f) * 0.5f) // 0..1

                // Smooth edge fade on all 4 borders so stars never pop or clip
                val edgeFadeX = sin((star.x.coerceIn(0.01f, 0.99f)) * PI.toFloat()).coerceIn(0f, 1f)
                val edgeFadeY = sin((star.y.coerceIn(0.01f, 0.99f)) * PI.toFloat()).coerceIn(0f, 1f)
                val edgeFade = (edgeFadeX * edgeFadeY).coerceIn(0f, 1f)

                val effectiveAlpha = (0.20f + 0.80f * baseTwinkle) * edgeFade
                if (effectiveAlpha <= 0.02f) return@forEach

                val px = star.x * canvasWidth
                val py = star.y * canvasHeight

                when (star.kind) {
                    CosmicStarKind.BRILLIANT_FLARE -> {
                        val currentRadius = star.baseRadius * (0.85f + 0.35f * baseTwinkle)

                        // 1. Soft Outermost Corona Halo
                        drawCircle(
                            color = star.chromaticTint.copy(alpha = effectiveAlpha * 0.22f),
                            radius = currentRadius * 4.2f,
                            center = Offset(px, py)
                        )

                        // 2. Inner Spectral Chromatic Halo
                        drawCircle(
                            color = star.chromaticTint.copy(alpha = effectiveAlpha * 0.55f),
                            radius = currentRadius * 2.0f,
                            center = Offset(px, py)
                        )

                        // 3. Hot White Core
                        drawCircle(
                            color = Color.White.copy(alpha = effectiveAlpha * 0.95f),
                            radius = currentRadius,
                            center = Offset(px, py)
                        )

                        // 4. Elegant 4-Point Optical Diffraction Spikes (Cross Flare)
                        if (effectiveAlpha > 0.30f) {
                            val spikeLength = star.flareSpan * (0.7f + 0.5f * baseTwinkle) * edgeFade
                            val spikeAlpha = effectiveAlpha * 0.65f
                            val spikeColor = star.chromaticTint.copy(alpha = spikeAlpha)

                            // Horizontal Spike
                            drawLine(
                                color = spikeColor,
                                start = Offset(px - spikeLength, py),
                                end = Offset(px + spikeLength, py),
                                strokeWidth = 1.3f,
                                cap = StrokeCap.Round
                            )

                            // Vertical Spike
                            drawLine(
                                color = spikeColor,
                                start = Offset(px, py - spikeLength),
                                end = Offset(px, py + spikeLength),
                                strokeWidth = 1.3f,
                                cap = StrokeCap.Round
                            )

                            // Inner White Accent on Spike Intersection
                            drawLine(
                                color = Color.White.copy(alpha = spikeAlpha * 0.9f),
                                start = Offset(px - spikeLength * 0.4f, py),
                                end = Offset(px + spikeLength * 0.4f, py),
                                strokeWidth = 1.0f
                            )
                            drawLine(
                                color = Color.White.copy(alpha = spikeAlpha * 0.9f),
                                start = Offset(px, py - spikeLength * 0.4f),
                                end = Offset(px, py + spikeLength * 0.4f),
                                strokeWidth = 1.0f
                            )
                        }
                    }

                    CosmicStarKind.CELESTIAL_SPARK -> {
                        val currentRadius = star.baseRadius * (0.85f + 0.30f * baseTwinkle)

                        // Soft Corona Halo
                        drawCircle(
                            color = star.chromaticTint.copy(alpha = effectiveAlpha * 0.30f),
                            radius = currentRadius * 2.6f,
                            center = Offset(px, py)
                        )

                        // Luminous Core
                        drawCircle(
                            color = Color.White.copy(alpha = effectiveAlpha * 0.85f),
                            radius = currentRadius,
                            center = Offset(px, py)
                        )
                    }

                    CosmicStarKind.DISTANT_STARDUST -> {
                        val dustAlpha = (effectiveAlpha * 0.65f).coerceIn(0f, 1f)
                        drawCircle(
                            color = star.chromaticTint.copy(alpha = dustAlpha),
                            radius = star.baseRadius,
                            center = Offset(px, py)
                        )
                    }
                }
            }

            // 3. 🌠 Subtle Periodic Meteor / Shooting Star Sweep (Active during window 0.0f..1.0f)
            if (meteorProgress in 0.0f..1.0f) {
                val meteorAlpha = sin(meteorProgress * PI.toFloat()).coerceIn(0f, 1f)
                if (meteorAlpha > 0.03f) {
                    val startX = (canvasWidth * 0.15f) + (canvasWidth * 0.70f) * meteorProgress
                    val startY = (canvasHeight * 0.15f) + (canvasHeight * 0.70f) * meteorProgress

                    val tailLengthX = canvasWidth * 0.12f
                    val tailLengthY = canvasHeight * 0.12f

                    val headPoint = Offset(startX, startY)
                    val tailPoint = Offset(startX - tailLengthX, startY - tailLengthY)

                    // Meteor tail line with linear gradient fade
                    val tailBrush = Brush.linearGradient(
                        colors = listOf(
                            Color(0x00FFFFFF),
                            Color(0xFF80D8FF).copy(alpha = meteorAlpha * 0.40f),
                            Color.White.copy(alpha = meteorAlpha * 0.90f)
                        ),
                        start = tailPoint,
                        end = headPoint
                    )

                    drawLine(
                        brush = tailBrush,
                        start = tailPoint,
                        end = headPoint,
                        strokeWidth = 1.8f,
                        cap = StrokeCap.Round
                    )

                    // Meteor brilliant head spark
                    drawCircle(
                        color = Color(0xFF80D8FF).copy(alpha = meteorAlpha * 0.60f),
                        radius = 3.5f,
                        center = headPoint
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = meteorAlpha),
                        radius = 1.8f,
                        center = headPoint
                    )
                }
            }
        }
    }
}
