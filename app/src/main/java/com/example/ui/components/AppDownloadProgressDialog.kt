package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.util.InAppUpdateManager
import com.example.util.UpdateDownloadProgressState
import com.example.util.UpdateDownloadStatus

/**
 * Dedicated In-App Update Live Download Popup Dialog.
 * Displays real-time download progress, downloaded/total MB, speed, remaining time, and instant install actions.
 */
@Composable
fun AppDownloadProgressDialog(
    downloadState: UpdateDownloadProgressState,
    onInstallClick: () -> Unit,
    onCancelClick: () -> Unit,
    onHideClick: () -> Unit,
    onRetryClick: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!downloadState.isDialogVisible) return

    val context = LocalContext.current
    val accentPrimary = MaterialTheme.colorScheme.primary
    val accentSecondary = MaterialTheme.colorScheme.secondary
    val onAccentPrimary = MaterialTheme.colorScheme.onPrimary

    val infiniteTransition = rememberInfiniteTransition(label = "download_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val animatedProgress by animateFloatAsState(
        targetValue = downloadState.progress,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "animated_progress"
    )

    Dialog(
        onDismissRequest = {
            if (downloadState.status == UpdateDownloadStatus.DOWNLOADING ||
                downloadState.status == UpdateDownloadStatus.CONNECTING ||
                downloadState.status == UpdateDownloadStatus.VERIFYING
            ) {
                onHideClick()
            } else {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = downloadState.status != UpdateDownloadStatus.DOWNLOADING
        )
    ) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(
                1.5.dp,
                Brush.linearGradient(
                    listOf(
                        accentPrimary,
                        accentSecondary.copy(alpha = 0.8f),
                        accentPrimary.copy(alpha = 0.4f)
                    )
                )
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("app_download_progress_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Glowing Icon Area
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(accentPrimary.copy(alpha = 0.35f), Color.Transparent)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .size(56.dp)
                            .scale(if (downloadState.status == UpdateDownloadStatus.DOWNLOADING) pulseScale else 1f),
                        shape = CircleShape,
                        color = SurfaceVariantDark,
                        border = BorderStroke(1.5.dp, accentPrimary)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            when (downloadState.status) {
                                UpdateDownloadStatus.COMPLETED -> {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                                UpdateDownloadStatus.ERROR -> {
                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                                UpdateDownloadStatus.VERIFYING -> {
                                    Icon(
                                        imageVector = Icons.Default.CloudSync,
                                        contentDescription = null,
                                        tint = accentPrimary,
                                        modifier = Modifier
                                            .size(28.dp)
                                            .rotate(rotationAngle)
                                    )
                                }
                                else -> {
                                    Icon(
                                        imageVector = Icons.Default.CloudDownload,
                                        contentDescription = null,
                                        tint = accentPrimary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Title
                Text(
                    text = when (downloadState.status) {
                        UpdateDownloadStatus.COMPLETED -> "اكتمل تنزيل التحديث! 🚀"
                        UpdateDownloadStatus.ERROR -> "تعذر إكمال التنزيل ⚠️"
                        UpdateDownloadStatus.CANCELLED -> "تم إلغاء التنزيل"
                        UpdateDownloadStatus.VERIFYING -> "جاري التحقق من التحديث 🛡️"
                        else -> "تنزيل تحديث Nexus v${downloadState.versionName.ifBlank { "2.1.0" }}"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Subtitle / Status description
                Text(
                    text = when (downloadState.status) {
                        UpdateDownloadStatus.CONNECTING -> "جاري الاتصال بالسيرفر وتجهيز ملف الحزمة..."
                        UpdateDownloadStatus.DOWNLOADING -> "يتم الآن تنزيل ملف التطبيق مباشرة..."
                        UpdateDownloadStatus.VERIFYING -> "فحص تكامل ملف APK وسلامته للتثبيت الآمن..."
                        UpdateDownloadStatus.COMPLETED -> "تم التحقق من الحزمة بنجاح وجاهزة للتثبيت فوراً."
                        UpdateDownloadStatus.ERROR -> downloadState.errorMessage ?: "حدث خطأ غير متوقع أثناء التنزيل."
                        UpdateDownloadStatus.CANCELLED -> "تم إيقاف عملية التنزيل بناءً على طلبك."
                        UpdateDownloadStatus.IDLE -> "جاهز للتنزيل"
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.5.sp,
                        color = when (downloadState.status) {
                            UpdateDownloadStatus.ERROR -> Color(0xFFEF4444)
                            UpdateDownloadStatus.COMPLETED -> Color(0xFF10B981)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Live Progress Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = BackgroundDark,
                    border = BorderStroke(1.dp, SurfaceElevated),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Top Numbers Row: Percent & Downloaded MB
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "${downloadState.progressPercent}%",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 22.sp,
                                        color = when (downloadState.status) {
                                            UpdateDownloadStatus.COMPLETED -> Color(0xFF10B981)
                                            UpdateDownloadStatus.ERROR -> Color(0xFFEF4444)
                                            else -> accentPrimary
                                        }
                                    )
                                )

                                if (downloadState.status == UpdateDownloadStatus.DOWNLOADING ||
                                    downloadState.status == UpdateDownloadStatus.CONNECTING
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = accentPrimary
                                    )
                                }
                            }

                            // Size Fraction (e.g., 18.5 MB / 35.0 MB)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SurfaceCard,
                                border = BorderStroke(0.8.dp, SurfaceElevated)
                            ) {
                                Text(
                                    text = "${downloadState.formattedDownloadedSize} / ${downloadState.formattedTotalSize}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Linear Animated Progress Bar
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = when (downloadState.status) {
                                UpdateDownloadStatus.COMPLETED -> Color(0xFF10B981)
                                UpdateDownloadStatus.ERROR -> Color(0xFFEF4444)
                                else -> accentPrimary
                            },
                            trackColor = SurfaceVariantDark,
                            strokeCap = StrokeCap.Round
                        )

                        // Speed & ETA Metrics Row
                        if (downloadState.status == UpdateDownloadStatus.DOWNLOADING) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Download Speed
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = null,
                                        tint = accentPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = downloadState.formattedSpeed,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = accentPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.5.sp
                                        )
                                    )
                                }

                                // Remaining Time
                                val remainingText = if (downloadState.estimatedRemainingSeconds > 0) {
                                    val sec = downloadState.estimatedRemainingSeconds
                                    if (sec >= 60) {
                                        "متبقي ~${sec / 60} د و ${sec % 60} ث"
                                    } else {
                                        "متبقي ~${sec} ثواني"
                                    }
                                } else {
                                    "جاري الحساب..."
                                }

                                Text(
                                    text = remainingText,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextTertiary,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons based on status
                when (downloadState.status) {
                    UpdateDownloadStatus.DOWNLOADING,
                    UpdateDownloadStatus.CONNECTING,
                    UpdateDownloadStatus.VERIFYING -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onCancelClick,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, SurfaceElevated),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                            ) {
                                Text("إلغاء التنزيل", fontSize = 12.5.sp)
                            }

                            Button(
                                onClick = onHideClick,
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accentPrimary,
                                    contentColor = onAccentPrimary
                                )
                            ) {
                                Text("المتابعة بالخلفية", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                            }
                        }
                    }

                    UpdateDownloadStatus.COMPLETED -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onInstallClick,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("install_update_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF10B981),
                                    contentColor = Color.White
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RocketLaunch,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text("تثبيت التحديث الآن ⚡", fontWeight = FontWeight.Black, fontSize = 14.sp)
                                }
                            }

                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, SurfaceElevated),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                            ) {
                                Text("إغلاق", fontSize = 12.sp)
                            }
                        }
                    }

                    UpdateDownloadStatus.ERROR,
                    UpdateDownloadStatus.CANCELLED -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = onRetryClick,
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .height(44.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = accentPrimary,
                                        contentColor = onAccentPrimary
                                    )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text("إعادة المحاولة", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                                    }
                                }

                                OutlinedButton(
                                    onClick = onDismiss,
                                    modifier = Modifier
                                        .weight(0.8f)
                                        .height(44.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, SurfaceElevated),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextTertiary)
                                ) {
                                    Text("إغلاق", fontSize = 12.sp)
                                }
                            }

                            // Browser fallback
                            TextButton(
                                onClick = {
                                    InAppUpdateManager.openDownloadInBrowser(context, downloadState.downloadUrl)
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.OpenInBrowser,
                                        contentDescription = null,
                                        tint = accentPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "التحميل المباشر عبر المتصفح ↗",
                                        color = accentPrimary,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    UpdateDownloadStatus.IDLE -> {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accentPrimary,
                                contentColor = onAccentPrimary
                            )
                        ) {
                            Text("حسناً", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
