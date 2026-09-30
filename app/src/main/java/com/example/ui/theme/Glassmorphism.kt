package com.example.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * High-End Authentic Glassmorphism System for Nexus.
 *
 * Implements real physical optical refraction & frosted crystal aesthetics:
 * 1. Multi-gradient specular substrate (specular sheen + optical density + frosted diffuse).
 * 2. Directional optical rim reflection (grazing incidence lighting).
 * 3. Seam-aware borders: NEVER draws harsh white lines across phone screen edges or status bar.
 * 4. Ambient light refraction beams at interface boundaries.
 */
object GlassmorphicStyle {

    /**
     * Applies authentic Glassmorphism to the Top App Header.
     * Crucially avoids any top or side borders to eliminate harsh white lines at screen edges.
     * Only renders a delicate, luminous optical refraction line along the bottom seam.
     */
    fun Modifier.glassmorphicHeader(
        enabled: Boolean,
        surfaceColor: Color,
        primaryColor: Color
    ): Modifier = composed {
        if (!enabled) {
            this
        } else {
            this
                .drawBehind {
                    val width = size.width
                    val height = size.height

                    // 1. Frosted Glass Substrate (ambient light gradient)
                    val glassBrush = Brush.verticalGradient(
                        colors = listOf(
                            surfaceColor.copy(alpha = 0.88f),
                            surfaceColor.copy(alpha = 0.78f)
                        )
                    )
                    drawRect(brush = glassBrush)

                    // 2. Diagonal Specular Light Sheen (mimics glass reflection from ambient daylight)
                    val specularSheen = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.White.copy(alpha = 0.02f),
                            Color.Transparent,
                            primaryColor.copy(alpha = 0.04f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(width * 0.7f, height)
                    )
                    drawRect(brush = specularSheen)

                    // 3. Delicate Bottom Refraction Seam (NO top or side borders!)
                    val bottomSeamBrush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.12f),
                            primaryColor.copy(alpha = 0.45f),
                            Color.White.copy(alpha = 0.20f),
                            primaryColor.copy(alpha = 0.30f),
                            Color.Transparent
                        )
                    )
                    drawLine(
                        brush = bottomSeamBrush,
                        start = Offset(0f, height - 1.dp.toPx()),
                        end = Offset(width, height - 1.dp.toPx()),
                        strokeWidth = 1.2.dp.toPx()
                    )
                }
        }
    }

    /**
     * Applies authentic Glassmorphism to the Bottom Floating Footer Bar.
     * Uses a floating capsule with multi-layered specular reflections and a refined
     * optical rim that catches light softly on the top-left rather than a harsh white border.
     */
    fun Modifier.glassmorphicFooter(
        enabled: Boolean,
        surfaceColor: Color,
        primaryColor: Color,
        secondaryColor: Color = primaryColor,
        shape: Shape = RoundedCornerShape(26.dp)
    ): Modifier = composed {
        if (!enabled) {
            this
                .shadow(elevation = 14.dp, shape = shape, spotColor = primaryColor.copy(alpha = 0.25f))
                .clip(shape)
                .background(surfaceColor.copy(alpha = 0.96f))
                .border(
                    BorderStroke(
                        1.2.dp,
                        Brush.horizontalGradient(
                            listOf(
                                primaryColor.copy(alpha = 0.45f),
                                Color.White.copy(alpha = 0.08f),
                                secondaryColor.copy(alpha = 0.45f)
                            )
                        )
                    ),
                    shape
                )
        } else {
            this
                .shadow(
                    elevation = 20.dp,
                    shape = shape,
                    ambientColor = primaryColor.copy(alpha = 0.20f),
                    spotColor = primaryColor.copy(alpha = 0.35f)
                )
                .clip(shape)
                .drawBehind {
                    val width = size.width
                    val height = size.height

                    // 1. Frosted Optical Body with depth
                    val bodyBrush = Brush.verticalGradient(
                        colors = listOf(
                            surfaceColor.copy(alpha = 0.76f),
                            surfaceColor.copy(alpha = 0.84f)
                        )
                    )
                    drawRect(brush = bodyBrush)

                    // 2. Specular Diagonal Sheen
                    val sheenBrush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.14f),
                            Color.White.copy(alpha = 0.03f),
                            Color.Transparent,
                            primaryColor.copy(alpha = 0.07f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(width * 0.8f, height)
                    )
                    drawRect(brush = sheenBrush)

                    // 3. Top Inner Refraction Crest (laser-like light catch at the top edge)
                    val topCrestBrush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.35f),
                            primaryColor.copy(alpha = 0.60f),
                            Color.White.copy(alpha = 0.45f),
                            Color.Transparent
                        )
                    )
                    drawLine(
                        brush = topCrestBrush,
                        start = Offset(width * 0.15f, 1.dp.toPx()),
                        end = Offset(width * 0.85f, 1.dp.toPx()),
                        strokeWidth = 1.2.dp.toPx()
                    )
                }
                .border(
                    BorderStroke(
                        0.9.dp,
                        Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.30f),
                                primaryColor.copy(alpha = 0.45f),
                                secondaryColor.copy(alpha = 0.25f),
                                Color.White.copy(alpha = 0.06f),
                                Color.Transparent
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        )
                    ),
                    shape
                )
        }
    }

    /**
     * Applies authentic Glassmorphism to the Reader Top Bar.
     * No top or side borders; delicate bottom refraction seam with frosted acrylic background.
     */
    fun Modifier.glassmorphicReaderTop(
        enabled: Boolean,
        surfaceColor: Color,
        primaryColor: Color
    ): Modifier = composed {
        if (!enabled) {
            this
                .shadow(elevation = 8.dp)
                .background(surfaceColor.copy(alpha = 0.98f))
                .drawBehind {
                    // Standard bottom divider line
                    drawLine(
                        color = primaryColor.copy(alpha = 0.35f),
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
        } else {
            this
                .shadow(elevation = 16.dp, spotColor = primaryColor.copy(alpha = 0.25f))
                .drawBehind {
                    val width = size.width
                    val height = size.height

                    // 1. Frosted Glass Substrate
                    val glassBrush = Brush.verticalGradient(
                        listOf(
                            surfaceColor.copy(alpha = 0.86f),
                            surfaceColor.copy(alpha = 0.74f)
                        )
                    )
                    drawRect(brush = glassBrush)

                    // 2. Light Sheen
                    val sheen = Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.10f),
                            Color.Transparent,
                            primaryColor.copy(alpha = 0.05f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(width, height)
                    )
                    drawRect(brush = sheen)

                    // 3. Bottom Refraction Seam ONLY (No top or side borders!)
                    val seam = Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.20f),
                            primaryColor.copy(alpha = 0.50f),
                            Color.White.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    )
                    drawLine(
                        brush = seam,
                        start = Offset(0f, height - 1.dp.toPx()),
                        end = Offset(width, height - 1.dp.toPx()),
                        strokeWidth = 1.2.dp.toPx()
                    )
                }
        }
    }

    /**
     * Applies authentic Glassmorphism to the Reader Bottom Floating Controls Bar.
     * Refined top refraction seam with frosted acrylic background and no bottom clipping lines.
     */
    fun Modifier.glassmorphicReaderBottom(
        enabled: Boolean,
        surfaceColor: Color,
        primaryColor: Color
    ): Modifier = composed {
        if (!enabled) {
            this
                .shadow(elevation = 8.dp)
                .background(surfaceColor.copy(alpha = 0.98f))
                .drawBehind {
                    drawLine(
                        color = primaryColor.copy(alpha = 0.35f),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 1.dp.toPx()
                    )
                }
        } else {
            this
                .shadow(elevation = 16.dp, spotColor = primaryColor.copy(alpha = 0.25f))
                .drawBehind {
                    val width = size.width
                    val height = size.height

                    // 1. Frosted Glass Substrate
                    val glassBrush = Brush.verticalGradient(
                        listOf(
                            surfaceColor.copy(alpha = 0.74f),
                            surfaceColor.copy(alpha = 0.86f)
                        )
                    )
                    drawRect(brush = glassBrush)

                    // 2. Light Sheen
                    val sheen = Brush.linearGradient(
                        listOf(
                            primaryColor.copy(alpha = 0.05f),
                            Color.Transparent,
                            Color.White.copy(alpha = 0.10f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(width, height)
                    )
                    drawRect(brush = sheen)

                    // 3. Top Refraction Seam ONLY (No bottom or side borders!)
                    val seam = Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.25f),
                            primaryColor.copy(alpha = 0.50f),
                            Color.White.copy(alpha = 0.20f),
                            Color.Transparent
                        )
                    )
                    drawLine(
                        brush = seam,
                        start = Offset(0f, 1.dp.toPx()),
                        end = Offset(width, 1.dp.toPx()),
                        strokeWidth = 1.2.dp.toPx()
                    )
                }
        }
    }

    /**
     * Glassmorphism Card Style for settings and dialogs.
     */
    fun Modifier.glassmorphicCard(
        enabled: Boolean,
        surfaceColor: Color,
        primaryColor: Color,
        shape: Shape = RoundedCornerShape(22.dp)
    ): Modifier = composed {
        if (!enabled) {
            this
                .clip(shape)
                .background(surfaceColor)
                .border(
                    BorderStroke(1.2.dp, Color.White.copy(alpha = 0.08f)),
                    shape
                )
        } else {
            this
                .shadow(
                    elevation = 12.dp,
                    shape = shape,
                    ambientColor = primaryColor.copy(alpha = 0.15f),
                    spotColor = primaryColor.copy(alpha = 0.25f)
                )
                .clip(shape)
                .drawBehind {
                    val width = size.width
                    val height = size.height

                    val body = Brush.verticalGradient(
                        listOf(
                            surfaceColor.copy(alpha = 0.80f),
                            surfaceColor.copy(alpha = 0.70f)
                        )
                    )
                    drawRect(brush = body)

                    val sheen = Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.12f),
                            Color.White.copy(alpha = 0.02f),
                            Color.Transparent,
                            primaryColor.copy(alpha = 0.05f)
                        )
                    )
                    drawRect(brush = sheen)
                }
                .border(
                    BorderStroke(
                        0.9.dp,
                        Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.32f),
                                primaryColor.copy(alpha = 0.35f),
                                Color.White.copy(alpha = 0.06f),
                                Color.Transparent
                            )
                        )
                    ),
                    shape
                )
        }
    }
}
