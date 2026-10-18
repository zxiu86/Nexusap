package com.example.ui.components

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.NexusGold
import com.example.ui.theme.NexusOrange
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.AppVersionConfig
import com.example.util.ResourceSetupManager
import com.example.util.ResourceSetupState

/**
 * Mandatory First-Launch Resource Setup Dialog
 * Titled: "تحميل المظاهر والإعدادات والتخصيص"
 *
 * Discovers and streams the native Olympus MTX cleaning engine package for the device ABI,
 * displays actual byte size, verifies SHA-256 and ZIP integrity, and initializes native cleaning.
 */
@Composable
fun FirstLaunchResourceSetupDialog() {
    val isVisible by ResourceSetupManager.isDialogVisible.collectAsState()
    val state by ResourceSetupManager.setupState.collectAsState()
    val context = LocalContext.current

    if (!isVisible) return

    val detectedAbi = try {
        Build.SUPPORTED_ABIS?.firstOrNull() ?: "arm64-v8a"
    } catch (_: Throwable) {
        "arm64-v8a"
    }

    Dialog(
        onDismissRequest = {
            // Mandatory: Do not dismiss on accidental touch outside or back press
        },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(26.dp)),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(NexusGold, NexusOrange.copy(alpha = 0.6f))))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Icon with Animated Glow
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val pulseAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.6f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1400, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulseAlpha"
                )

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            Brush.radialGradient(listOf(NexusGold.copy(alpha = 0.25f * pulseAlpha), Color.Transparent)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = SurfaceElevated,
                        border = BorderStroke(1.dp, NexusGold.copy(alpha = 0.6f)),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = when (state) {
                                    is ResourceSetupState.Success -> Icons.Default.CheckCircle
                                    is ResourceSetupState.Error -> if ((state as ResourceSetupState.Error).isOffline) Icons.Default.WifiOff else Icons.Default.ErrorOutline
                                    else -> Icons.Default.AutoAwesome
                                },
                                contentDescription = null,
                                tint = when (state) {
                                    is ResourceSetupState.Success -> Color(0xFF10B981)
                                    is ResourceSetupState.Error -> Color(0xFFEF4444)
                                    else -> NexusGold
                                },
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                // Main Title
                Text(
                    text = "تنزيل المظهر وتخصيص الإعدادات",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "يحتاج التطبيق إلى تجهيز وتثبيت ملفات المظهر المتقدم وتخصيص الواجهة لمرة واحدة فقط لتمكين أفضل تجربة تصفح وأعلى درجات الاستقرار والانسيابية.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp)

                // Detailed Specs Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. حالة العملية
                        val statusText = when (val s = state) {
                            is ResourceSetupState.Idle -> "في انتظار البدء..."
                            is ResourceSetupState.Discovering -> "جارٍ فحص ملفات المظهر في السحابة..."
                            is ResourceSetupState.MetadataReady -> "تم العثور على الحزمة، جارٍ التجهيز..."
                            is ResourceSetupState.Downloading -> "جارٍ تنزيل ملفات المظهر (${s.speedText})..."
                            is ResourceSetupState.Verifying -> "جارٍ فحص سلامة وتكامل الملفات..."
                            is ResourceSetupState.Extracting -> "جارٍ فك وتثبيت ملفات التخصيص..."
                            is ResourceSetupState.InitializingEngine -> "جارٍ تفعيل إعدادات الواجهة والمظهر..."
                            is ResourceSetupState.Success -> "اكتمل تثبيت المظهر وتخصيص الإعدادات بنجاح!"
                            is ResourceSetupState.Error -> "تعذر إكمال التثبيت: ${s.message}"
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("حالة التجهيز:", fontSize = 11.5.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                            Text(
                                text = statusText,
                                fontSize = 11.5.sp,
                                color = when (state) {
                                    is ResourceSetupState.Success -> Color(0xFF10B981)
                                    is ResourceSetupState.Error -> Color(0xFFEF4444)
                                    else -> NexusGold
                                },
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f, fill = false),
                                textAlign = TextAlign.End
                            )
                        }

                        // 2. المعمارية
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("معمارية النظام:", fontSize = 11.5.sp, color = TextSecondary)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.White.copy(alpha = 0.08f)
                            ) {
                                Text(
                                    text = detectedAbi,
                                    fontSize = 11.sp,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // 3. إصدار الحزمة
                        val packageVersion = when (val s = state) {
                            is ResourceSetupState.MetadataReady -> s.version
                            is ResourceSetupState.Downloading -> s.version
                            is ResourceSetupState.Success -> s.version
                            else -> "v${AppVersionConfig.VERSION_NAME}"
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("إصدار المظهر:", fontSize = 11.5.sp, color = TextSecondary)
                            Text(packageVersion, fontSize = 11.5.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                        }

                        // 4. حجم ملف المظهر
                        val fileSize = when (val s = state) {
                            is ResourceSetupState.MetadataReady -> s.formattedSize
                            is ResourceSetupState.Downloading -> s.formattedSize
                            else -> "جارٍ تحديد الحجم"
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("حجم ملفات المظهر:", fontSize = 11.5.sp, color = TextSecondary)
                            Text(fileSize, fontSize = 11.5.sp, color = NexusGold, fontWeight = FontWeight.Bold)
                        }

                        // 5. التقدم والنسبة
                        when (val s = state) {
                            is ResourceSetupState.Downloading -> {
                                HorizontalDivider(color = Color.White.copy(alpha = 0.06f), modifier = Modifier.padding(vertical = 2.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("التقدم:", fontSize = 11.5.sp, color = TextSecondary)
                                    Text("${s.progressPercent}% (${s.formattedProgress})", fontSize = 11.5.sp, color = NexusOrange, fontWeight = FontWeight.Bold)
                                }
                            }
                            else -> {}
                        }
                    }
                }

                // Interactive Progress Bar
                when (val s = state) {
                    is ResourceSetupState.Downloading -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            LinearProgressIndicator(
                                progress = { (s.progressPercent / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = NexusGold,
                                trackColor = Color.White.copy(alpha = 0.1f)
                            )
                        }
                    }
                    is ResourceSetupState.Discovering,
                    is ResourceSetupState.MetadataReady,
                    is ResourceSetupState.Verifying,
                    is ResourceSetupState.Extracting,
                    is ResourceSetupState.InitializingEngine -> {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = NexusGold,
                            trackColor = Color.White.copy(alpha = 0.1f)
                        )
                    }
                    else -> {}
                }

                // Error Notice if applicable
                AnimatedVisibility(
                    visible = state is ResourceSetupState.Error,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF450A0A).copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = (state as? ResourceSetupState.Error)?.message.orEmpty(),
                            color = Color(0xFFFCA5A5),
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(10.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Action Buttons
                when (state) {
                    is ResourceSetupState.Error -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Retry Button
                            Button(
                                onClick = { ResourceSetupManager.startSetup(context) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Brush.horizontalGradient(listOf(NexusGold, NexusOrange))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                        Text("إعادة المحاولة الآن", color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                    is ResourceSetupState.Success -> {
                        Button(
                            onClick = { ResourceSetupManager.dismissDialogTemporarily() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Text("بدء استخدام التطبيق الآن", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    else -> {
                        // Ongoing progress notice
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = NexusGold,
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "يرجى الانتظار بينما يتم تجهيز وتثبيت المظهر...",
                                fontSize = 11.5.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
