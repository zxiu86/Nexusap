package com.example.ui.screens.home

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.geometry.Offset
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DownloadedChapter
import com.example.data.settings.AppSettingsManager
import com.example.ui.theme.HarmattanFontFamily
import com.example.ui.theme.HarmattanTypography
import com.example.ui.theme.NexusGold
import com.example.ui.theme.NexusOrange
import com.example.ui.theme.ThemePalettes
import com.example.ui.viewmodel.HomeUiState
import com.example.util.AppVersionConfig

/**
 * Modern Sub-Pages Category Enum for Settings
 */
enum class SettingsSubCategory(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val gradientColors: List<Color>
) {
    APPEARANCE(
        title = "المظهر والتخصيص",
        subtitle = "الألوان، التدرجات، الخلفيات، وفضاء الفوتر الأسطوري",
        icon = Icons.Default.Palette,
        gradientColors = listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))
    ),
    READER(
        title = "محرك القراءة والعرض",
        subtitle = "نمط التمرير، دقة الصور، وإبقاء الشاشة مضاءة",
        icon = Icons.Default.MenuBook,
        gradientColors = listOf(Color(0xFF06B6D4), Color(0xFF3B82F6))
    ),
    DOWNLOADS(
        title = "التنزيلات والقراءة أوفلاين",
        subtitle = "إدارة الفصول المحملة محلياً، التحميل عبر الواي فاي",
        icon = Icons.Default.Download,
        gradientColors = listOf(Color(0xFF10B981), Color(0xFF059669))
    ),
    CACHE_STORAGE(
        title = "الذاكرة المؤقتة والتخزين",
        subtitle = "تفريغ ذاكرة الصور وبيانات التصفح لتحرير المساحة",
        icon = Icons.Default.CleaningServices,
        gradientColors = listOf(Color(0xFFF59E0B), Color(0xFFD97706))
    ),
    ACCOUNT_CLOUD(
        title = "حساب Nexus والمزامنة السحابية",
        subtitle = "حفظ ومزامنة المفضلة، السجل، وصلاحيات المشرف",
        icon = Icons.Default.CloudSync,
        gradientColors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
    ),
    REPORTS(
        title = "البلاغات وطلبات الأعمال",
        subtitle = "تقديم بلاغ عن فصل، طلب مانجا، ومتابعة الردود",
        icon = Icons.Default.ReportProblem,
        gradientColors = listOf(Color(0xFFEC4899), Color(0xFFF43F5E))
    ),
    ABOUT_UPDATES(
        title = "حول التطبيق والتحديثات",
        subtitle = "إصدار v2.0.7 SUPER، سجل التغييرات، وقناة التليجرام",
        icon = Icons.Default.Info,
        gradientColors = listOf(Color(0xFF0284C7), Color(0xFF0288D1))
    )
}

/**
 * Modular Settings Container supporting sub-pages navigation
 */
@Composable
fun SettingsModernContainer(
    uiState: HomeUiState,
    onReadChapter: (String, Int) -> Unit,
    onDeleteDownload: (String, Int) -> Unit,
    onTriggerUpdate: () -> Unit,
    onCheckCloudUpdates: () -> Unit,
    onRefreshData: () -> Unit,
    onExploreHome: () -> Unit,
    onClearCache: () -> Unit,
    onUpdateReaderMode: (Int) -> Unit = {},
    onUpdateImageQuality: (Int) -> Unit = {},
    onUpdateKeepScreenOn: (Boolean) -> Unit = {},
    onUpdateWifiOnlyDownloads: (Boolean) -> Unit = {},
    onUpdateAutoSyncUpdates: (Boolean) -> Unit = {},
    onUpdateThemeMode: (Int) -> Unit = {},
    onUpdateBackgroundStyle: (Int) -> Unit = {},
    onUpdateAccentColor: (Int) -> Unit = {},
    onUpdateCardAnimationEnabled: (Boolean) -> Unit = {},
    onUpdateCosmicSpaceFooterEnabled: (Boolean) -> Unit = {},
    onUpdateFooterWaveSpeed: (Int) -> Unit = {},
    onUpdateFooterWaveInterval: (Int) -> Unit = {},
    onUpdateFooterWaveColor: (Int) -> Unit = {},
    onUpdatePreventChapterCache: (Boolean) -> Unit = {},
    onDeleteAllDownloads: () -> Unit = {},
    onOpenAuthDialog: () -> Unit = {},
    onOpenAdminDialog: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onSyncCloud: () -> Unit = {},
    onOpenSubmitReportDialog: (String, String) -> Unit = { _, _ -> },
    onOpenUserReportsDialog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentSubCategory by rememberSaveable { mutableStateOf<SettingsSubCategory?>(null) }
    val currentThemePreset = ThemePalettes.getPresetById(uiState.appSettings.accentColor)
    val accentPrimary = currentThemePreset.primaryColor

    // Hardware/System back button navigates back to Settings Main Hub
    BackHandler(enabled = currentSubCategory != null) {
        currentSubCategory = null
    }

    MaterialTheme(typography = HarmattanTypography) {
        CompositionLocalProvider(LocalTextStyle provides TextStyle(fontFamily = HarmattanFontFamily)) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .testTag("settings_modern_container")
            ) {
                AnimatedContent(
                    targetState = currentSubCategory,
                    transitionSpec = {
                        if (targetState != null) {
                            (slideInHorizontally { width -> -width } + fadeIn()) togetherWith
                                    (slideOutHorizontally { width -> width } + fadeOut())
                        } else {
                            (slideInHorizontally { width -> width } + fadeIn()) togetherWith
                                    (slideOutHorizontally { width -> -width } + fadeOut())
                        }
                    },
                    label = "settings_subpage_animation"
                ) { subPage ->
                    if (subPage == null) {
                        // Main Hub with Category Cards
                        SettingsMainHubView(
                            uiState = uiState,
                            accentPrimary = accentPrimary,
                            onCategoryClick = { currentSubCategory = it },
                            onOpenAuth = onOpenAuthDialog,
                            onOpenAdmin = onOpenAdminDialog,
                            onOpenUpdates = onCheckCloudUpdates
                        )
                    } else {
                        // Sub-Page Detail View
                        SettingsSubPageDetailView(
                            category = subPage,
                            uiState = uiState,
                            accentPrimary = accentPrimary,
                            onBackClick = { currentSubCategory = null },
                            onReadChapter = onReadChapter,
                            onDeleteDownload = onDeleteDownload,
                            onDeleteAllDownloads = onDeleteAllDownloads,
                            onTriggerUpdate = onTriggerUpdate,
                            onCheckCloudUpdates = onCheckCloudUpdates,
                            onClearCache = onClearCache,
                            onUpdateReaderMode = onUpdateReaderMode,
                            onUpdateImageQuality = onUpdateImageQuality,
                            onUpdateKeepScreenOn = onUpdateKeepScreenOn,
                            onUpdateWifiOnlyDownloads = onUpdateWifiOnlyDownloads,
                            onUpdateAutoSyncUpdates = onUpdateAutoSyncUpdates,
                            onUpdateThemeMode = onUpdateThemeMode,
                            onUpdateBackgroundStyle = onUpdateBackgroundStyle,
                            onUpdateAccentColor = onUpdateAccentColor,
                            onUpdateCardAnimationEnabled = onUpdateCardAnimationEnabled,
                            onUpdateCosmicSpaceFooterEnabled = onUpdateCosmicSpaceFooterEnabled,
                            onUpdateFooterWaveSpeed = onUpdateFooterWaveSpeed,
                            onUpdateFooterWaveInterval = onUpdateFooterWaveInterval,
                            onUpdateFooterWaveColor = onUpdateFooterWaveColor,
                            onUpdatePreventChapterCache = onUpdatePreventChapterCache,
                            onOpenAuthDialog = onOpenAuthDialog,
                            onOpenAdminDialog = onOpenAdminDialog,
                            onSignOut = onSignOut,
                            onSyncCloud = onSyncCloud,
                            onOpenSubmitReportDialog = onOpenSubmitReportDialog,
                            onOpenUserReportsDialog = onOpenUserReportsDialog
                        )
                    }
                }
            }
        }
    }
}

/**
 * Main Settings Hub showing categorized entry points
 */
@Composable
private fun SettingsMainHubView(
    uiState: HomeUiState,
    accentPrimary: Color,
    onCategoryClick: (SettingsSubCategory) -> Unit,
    onOpenAuth: () -> Unit,
    onOpenAdmin: () -> Unit,
    onOpenUpdates: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // App Hero Header Card with SUPER v2.0.3 Branding
        item(key = "settings_hero_header") {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(
                    1.2.dp,
                    Brush.linearGradient(
                        listOf(accentPrimary.copy(alpha = 0.6f), MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    )
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = accentPrimary.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, accentPrimary.copy(alpha = 0.4f)),
                        modifier = Modifier.size(54.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = accentPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "إعدادات التطبيق",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 18.sp
                                )
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = accentPrimary.copy(alpha = 0.18f),
                                border = BorderStroke(0.5.dp, accentPrimary.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = AppVersionConfig.getSettingsVersionLabel(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = accentPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "تخصيص كامل مقسم لأقسام فرعية بتصميم عصري وأنيق",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.5.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Quick User Summary Card if Signed In
        val user = uiState.currentUser
        if (user != null) {
            item(key = "settings_user_quick_card") {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = accentPrimary.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, accentPrimary.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCategoryClick(SettingsSubCategory.ACCOUNT_CLOUD) }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = accentPrimary.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, accentPrimary),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = (user.displayName.ifBlank { user.email }).take(1).uppercase(),
                                    fontWeight = FontWeight.Black,
                                    color = accentPrimary,
                                    fontSize = 16.sp
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = user.displayName.ifBlank { "مستخدم Nexus" },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (user.isAdmin) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = NexusGold.copy(alpha = 0.25f)
                                    ) {
                                        Text(
                                            text = "ADMIN",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = NexusGold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = user.email,
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = accentPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Section Title
        item(key = "settings_categories_title") {
            Text(
                text = "الأقسام والإعدادات الرئيسية:",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.5.sp
                ),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }

        // List of Modern Sub-Page Cards
        items(SettingsSubCategory.values(), key = { it.name }) { category ->
            SettingsCategoryCard(
                category = category,
                badgeCount = when (category) {
                    SettingsSubCategory.DOWNLOADS -> if (uiState.downloadedChapters.isNotEmpty()) "${uiState.downloadedChapters.size} فصول" else null
                    SettingsSubCategory.REPORTS -> if (uiState.userReports.isNotEmpty()) "${uiState.userReports.size}" else null
                    SettingsSubCategory.ABOUT_UPDATES -> if (uiState.updateInfo.updateAvailable) "تحديث!" else null
                    else -> null
                },
                onClick = { onCategoryClick(category) }
            )
        }
    }
}

/**
 * Modern Card Item for Each Sub-Category in the Settings Hub
 */
@Composable
private fun SettingsCategoryCard(
    category: SettingsSubCategory,
    badgeCount: String? = null,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = category.gradientColors.first().copy(alpha = 0.15f),
                border = BorderStroke(1.dp, category.gradientColors.first().copy(alpha = 0.35f)),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = null,
                        tint = category.gradientColors.first(),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = category.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    if (badgeCount != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = category.gradientColors.first().copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = badgeCount,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = category.gradientColors.first()
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Text(
                    text = category.subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Sub-Page Detail View hosting specific categorized settings
 */
@Composable
private fun SettingsSubPageDetailView(
    category: SettingsSubCategory,
    uiState: HomeUiState,
    accentPrimary: Color,
    onBackClick: () -> Unit,
    onReadChapter: (String, Int) -> Unit,
    onDeleteDownload: (String, Int) -> Unit,
    onDeleteAllDownloads: () -> Unit,
    onTriggerUpdate: () -> Unit,
    onCheckCloudUpdates: () -> Unit,
    onClearCache: () -> Unit,
    onUpdateReaderMode: (Int) -> Unit,
    onUpdateImageQuality: (Int) -> Unit,
    onUpdateKeepScreenOn: (Boolean) -> Unit,
    onUpdateWifiOnlyDownloads: (Boolean) -> Unit,
    onUpdateAutoSyncUpdates: (Boolean) -> Unit,
    onUpdateThemeMode: (Int) -> Unit,
    onUpdateBackgroundStyle: (Int) -> Unit,
    onUpdateAccentColor: (Int) -> Unit,
    onUpdateCardAnimationEnabled: (Boolean) -> Unit,
    onUpdateCosmicSpaceFooterEnabled: (Boolean) -> Unit,
    onUpdateFooterWaveSpeed: (Int) -> Unit = {},
    onUpdateFooterWaveInterval: (Int) -> Unit = {},
    onUpdateFooterWaveColor: (Int) -> Unit = {},
    onUpdatePreventChapterCache: (Boolean) -> Unit,
    onOpenAuthDialog: () -> Unit,
    onOpenAdminDialog: () -> Unit,
    onSignOut: () -> Unit,
    onSyncCloud: () -> Unit,
    onOpenSubmitReportDialog: (String, String) -> Unit,
    onOpenUserReportsDialog: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Sub-Page Header with Back Button
        item(key = "subpage_top_bar") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "الرجوع للإعدادات",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = category.subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Sub-Page Specific Content
        when (category) {
            SettingsSubCategory.APPEARANCE -> {
                item {
                    AppearanceSubPage(
                        uiState = uiState,
                        accentPrimary = accentPrimary,
                        onUpdateThemeMode = onUpdateThemeMode,
                        onUpdateAccentColor = onUpdateAccentColor,
                        onUpdateBackgroundStyle = onUpdateBackgroundStyle,
                        onUpdateCardAnimationEnabled = onUpdateCardAnimationEnabled,
                        onUpdateCosmicSpaceFooterEnabled = onUpdateCosmicSpaceFooterEnabled,
                        onUpdateFooterWaveSpeed = onUpdateFooterWaveSpeed,
                        onUpdateFooterWaveInterval = onUpdateFooterWaveInterval,
                        onUpdateFooterWaveColor = onUpdateFooterWaveColor
                    )
                }
            }
            SettingsSubCategory.READER -> {
                item {
                    ReaderSubPage(
                        uiState = uiState,
                        accentPrimary = accentPrimary,
                        onUpdateReaderMode = onUpdateReaderMode,
                        onUpdateImageQuality = onUpdateImageQuality,
                        onUpdateKeepScreenOn = onUpdateKeepScreenOn,
                        onUpdatePreventChapterCache = onUpdatePreventChapterCache
                    )
                }
            }
            SettingsSubCategory.DOWNLOADS -> {
                item {
                    DownloadsSubPage(
                        uiState = uiState,
                        accentPrimary = accentPrimary,
                        onUpdateWifiOnlyDownloads = onUpdateWifiOnlyDownloads,
                        onReadChapter = onReadChapter,
                        onDeleteDownload = onDeleteDownload,
                        onDeleteAllDownloads = onDeleteAllDownloads
                    )
                }
            }
            SettingsSubCategory.CACHE_STORAGE -> {
                item {
                    CacheStorageSubPage(
                        accentPrimary = accentPrimary,
                        onClearCache = onClearCache
                    )
                }
            }
            SettingsSubCategory.ACCOUNT_CLOUD -> {
                item {
                    AccountCloudSubPage(
                        uiState = uiState,
                        accentPrimary = accentPrimary,
                        onOpenAuthDialog = onOpenAuthDialog,
                        onOpenAdminDialog = onOpenAdminDialog,
                        onSignOut = onSignOut,
                        onSyncCloud = onSyncCloud
                    )
                }
            }
            SettingsSubCategory.REPORTS -> {
                item {
                    ReportsSubPage(
                        uiState = uiState,
                        accentPrimary = accentPrimary,
                        onOpenSubmitReportDialog = onOpenSubmitReportDialog,
                        onOpenUserReportsDialog = onOpenUserReportsDialog
                    )
                }
            }
            SettingsSubCategory.ABOUT_UPDATES -> {
                item {
                    AboutUpdatesSubPage(
                        uiState = uiState,
                        accentPrimary = accentPrimary,
                        onTriggerUpdate = onTriggerUpdate,
                        onCheckCloudUpdates = onCheckCloudUpdates
                    )
                }
            }
        }
    }
}

/**
 * 1. Appearance & Theming Sub-Page - Redesigned with Dynamic Spacious Layout,
 * Quick Preset Themes, Live Appearance Studio 2.0, Footer Wave Customizer, and Elegant White Theme Showcase
 */
@Composable
private fun AppearanceSubPage(
    uiState: HomeUiState,
    accentPrimary: Color,
    onUpdateThemeMode: (Int) -> Unit,
    onUpdateAccentColor: (Int) -> Unit,
    onUpdateBackgroundStyle: (Int) -> Unit,
    onUpdateCardAnimationEnabled: (Boolean) -> Unit,
    onUpdateCosmicSpaceFooterEnabled: (Boolean) -> Unit,
    onUpdateFooterWaveSpeed: (Int) -> Unit,
    onUpdateFooterWaveInterval: (Int) -> Unit,
    onUpdateFooterWaveColor: (Int) -> Unit
) {
    val themeMode = uiState.appSettings.themeMode
    val accentColor = uiState.appSettings.accentColor
    val backgroundStyle = uiState.appSettings.backgroundStyle
    val cardAnimationEnabled = uiState.appSettings.cardAnimationEnabled
    val cosmicSpaceFooterEnabled = uiState.appSettings.cosmicSpaceFooterEnabled
    val footerWaveSpeed = uiState.appSettings.footerWaveSpeed
    val footerWaveInterval = uiState.appSettings.footerWaveInterval
    val footerWaveColor = uiState.appSettings.footerWaveColor
    val isCosmicUnlocked = uiState.isCosmicAuraUnlocked
    val totalRead = uiState.totalReadChaptersCount

    val activePreset = remember(accentColor) { ThemePalettes.getPresetById(accentColor) }
    var selectedPaletteTab by remember { mutableStateOf(if (activePreset.isMultiColor) 1 else 0) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // 🌟 1. Interactive Live Appearance Studio 2.0 (لوحة المعاينة التفاعلية الحية المباشرة)
        LiveAppearanceStudioCard(
            themeMode = themeMode,
            accentPreset = activePreset,
            backgroundStyle = backgroundStyle,
            cardAnimationEnabled = cardAnimationEnabled,
            footerWaveSpeed = footerWaveSpeed,
            footerWaveInterval = footerWaveInterval,
            footerWaveColor = footerWaveColor
        )

        // ⚡ 2. Quick Master Presets (السمات السريعة الجاهزة بنقرة واحدة)
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = accentPrimary.copy(alpha = 0.15f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = accentPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "سمات جاهزة متناسقة",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = "تطبيق فوري لنمط الإضاءة واللون المناسب بنقرة واحدة",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }

                // Horizontal Scrollable Quick Master Themes Carousel
                val quickMasterThemes = remember {
                    listOf(
                        QuickMasterThemeItem(
                            id = "gold_imperial",
                            title = "ذهب إمبراطوري",
                            subtitle = "ذهبي ملكي دافئ",
                            icon = "👑",
                            accentId = 0, // NexusGold
                            themeMode = 1,
                            backgroundStyle = 0,
                            swatchColors = listOf(NexusGold, NexusOrange),
                            badge = "ملكي"
                        ),
                        QuickMasterThemeItem(
                            id = "blue_royal",
                            title = "أزرق ملكي",
                            subtitle = "ياقوتي ساطع",
                            icon = "🔷",
                            accentId = 1, // NexusBluePrimary
                            themeMode = 1,
                            backgroundStyle = 0,
                            swatchColors = listOf(Color(0xFF2196F3), Color(0xFF1565C0)),
                            badge = "كلاسيكي"
                        ),
                        QuickMasterThemeItem(
                            id = "emerald_imperial",
                            title = "زمرد ملكي",
                            subtitle = "أخضر زمردي نقي",
                            icon = "🌿",
                            accentId = 5, // NexusEmeraldPrimary
                            themeMode = 1,
                            backgroundStyle = 0,
                            swatchColors = listOf(Color(0xFF10B981), Color(0xFF059669)),
                            badge = "منعش"
                        ),
                        QuickMasterThemeItem(
                            id = "violet_cosmic",
                            title = "بنفسجي كوني",
                            subtitle = "سديم الفضاء الساحر",
                            icon = "🌌",
                            accentId = 6, // NexusVioletPrimary
                            themeMode = 1,
                            backgroundStyle = 0,
                            swatchColors = listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9)),
                            badge = "فلكي"
                        ),
                        QuickMasterThemeItem(
                            id = "white_pearl",
                            title = "أبيض لؤلؤي",
                            subtitle = "أناقة ناصعة مريحة",
                            icon = "🤍",
                            accentId = 8, // NexusWhitePrimary
                            themeMode = 2,
                            backgroundStyle = 2,
                            swatchColors = listOf(Color.White, Color(0xFFCBD5E1)),
                            badge = "مريح"
                        ),
                        QuickMasterThemeItem(
                            id = "marine_deep",
                            title = "أزرق بحري",
                            subtitle = "أعماق المحيط الهادئ",
                            icon = "🌊",
                            accentId = 3, // NexusMarineBluePrimary (#0000B3 / #3333FF)
                            themeMode = 1,
                            backgroundStyle = 0,
                            swatchColors = listOf(Color(0xFF3333FF), Color(0xFF000080)),
                            badge = "بحري"
                        ),
                        QuickMasterThemeItem(
                            id = "crimson_fire",
                            title = "أحمر قرمزي",
                            subtitle = "توهج ناري ملكي",
                            icon = "🔥",
                            accentId = 2, // NexusRedPrimary
                            themeMode = 1,
                            backgroundStyle = 0,
                            swatchColors = listOf(Color(0xFFE53935), Color(0xFFB71C1C)),
                            badge = "حماسي"
                        ),
                        QuickMasterThemeItem(
                            id = "cherry_blossom",
                            title = "أزهار الكرز",
                            subtitle = "وردي ساكورا متألق",
                            icon = "🌸",
                            accentId = 4, // NexusCherryBlossomPrimary
                            themeMode = 1,
                            backgroundStyle = 0,
                            swatchColors = listOf(Color(0xFFFF77E1), Color(0xFFFF9BEB)),
                            badge = "ساكورا"
                        ),
                        QuickMasterThemeItem(
                            id = "amber_radiant",
                            title = "كهرماني مشرق",
                            subtitle = "إشعاع ذهبي دافئ",
                            icon = "⚡",
                            accentId = 7, // NexusAmberPrimary
                            themeMode = 1,
                            backgroundStyle = 0,
                            swatchColors = listOf(Color(0xFFF59E0B), Color(0xFFD97706)),
                            badge = "مشع"
                        )
                    )
                }

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(quickMasterThemes, key = { it.id }) { preset ->
                        val isSelected = (accentColor == preset.accentId) &&
                                (themeMode == preset.themeMode || (preset.themeMode == 1 && themeMode == 0))

                        QuickMasterThemeCard(
                            preset = preset,
                            isSelected = isSelected,
                            onClick = {
                                onUpdateThemeMode(preset.themeMode)
                                onUpdateAccentColor(preset.accentId)
                                onUpdateBackgroundStyle(preset.backgroundStyle)
                            }
                        )
                    }
                }
            }
        }

        // 💡 3. Lighting Mode Card (وضع الإضاءة العام)
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "وضع الإضاءة العام",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "اختر نمط الإضاءة الأنسب لراحتك البصرية أثناء التصفح",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = accentPrimary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = when (themeMode) {
                                1 -> "داكن 🌙"
                                2 -> "فاتح ☀️"
                                else -> "تلقائي ⚙️"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = accentPrimary,
                                fontSize = 10.5.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LightingModeCard(
                        title = "تلقائي",
                        subtitle = "حسب النظام",
                        icon = Icons.Default.Brightness4,
                        isSelected = themeMode == 0,
                        accent = accentPrimary,
                        onClick = { onUpdateThemeMode(0) },
                        modifier = Modifier.weight(1f)
                    )

                    LightingModeCard(
                        title = "داكن",
                        subtitle = "مريح للعين",
                        icon = Icons.Default.Brightness4,
                        isSelected = themeMode == 1,
                        accent = accentPrimary,
                        onClick = { onUpdateThemeMode(1) },
                        modifier = Modifier.weight(1f)
                    )

                    LightingModeCard(
                        title = "فاتح",
                        subtitle = "أبيض أنيق",
                        icon = Icons.Default.AutoAwesome,
                        isSelected = themeMode == 2,
                        accent = accentPrimary,
                        onClick = { onUpdateThemeMode(2) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 🤍 4. Theme Palette & Spotlight on the New "Elegant White" Theme (السمة واللون الأبيض الأنيق)
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header with badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "سمة التطبيق وألوان الهوية",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "اختر السمة التي تزين عناصر الواجهة وقوائم التطبيق",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = activePreset.primaryColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = activePreset.name,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = activePreset.primaryColor,
                                fontSize = 10.5.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                // ✨ Featured Spotlight: Elegant White Theme Banner (اللون الأبيض الأنيق والمريح)
                val isWhiteActive = accentColor == 8 || accentColor == 18
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isWhiteActive) Color(0xFFF8FAFC) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(
                        if (isWhiteActive) 2.dp else 1.2.dp,
                        if (isWhiteActive) Color(0xFFE2E8F0) else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onUpdateAccentColor(8) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // White Pearl Icon Badge
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFFFFFF),
                            border = BorderStroke(1.5.dp, Color(0xFFCBD5E1)),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFF0F172A),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "اللون الأبيض الأنيق والمريح",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                        color = if (isWhiteActive) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF0F172A)
                                ) {
                                    Text(
                                        text = "جديد ✨",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "مظهر لؤلؤي ناصع ومتوازن يمنحك راحة بصرية فائقة وأناقة ملكية بدون إجهاد للعين",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 10.5.sp,
                                    color = if (isWhiteActive) Color(0xFF475569) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 14.sp
                                )
                            )
                        }

                        // Activation state badge
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isWhiteActive) Color(0xFF0F172A) else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, if (isWhiteActive) Color(0xFF0F172A) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = if (isWhiteActive) "مفعل ✓" else "تفعيل",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isWhiteActive) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Tab Switcher between Solid and Gradient Presets
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PaletteCategoryTab(
                        title = "الألوان الأساسية الفاخرة",
                        count = ThemePalettes.SOLID_PRESETS.size,
                        isSelected = selectedPaletteTab == 0,
                        accent = accentPrimary,
                        onClick = { selectedPaletteTab = 0 },
                        modifier = Modifier.weight(1f)
                    )
                    PaletteCategoryTab(
                        title = "تدرجات أحدث الأعمال",
                        count = ThemePalettes.GRADIENT_PRESETS.size,
                        isSelected = selectedPaletteTab == 1,
                        accent = accentPrimary,
                        onClick = { selectedPaletteTab = 1 },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Display selected palette items
                if (selectedPaletteTab == 0) {
                    // Solid Presets
                    val solidPresets = ThemePalettes.SOLID_PRESETS
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(solidPresets, key = { it.id }) { preset ->
                            val isSelected = accentColor == preset.id
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isSelected) 0.85f else 0.4f),
                                border = BorderStroke(
                                    if (isSelected) 2.dp else 1.dp,
                                    if (isSelected) preset.primaryColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier
                                    .width(105.dp)
                                    .clickable { onUpdateAccentColor(preset.id) }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(preset.primaryColor)
                                            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = if (preset.id == 8) Color(0xFF0F172A) else Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = preset.name,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) preset.primaryColor else MaterialTheme.colorScheme.onSurface
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Gradient Presets
                    val gradientPresets = ThemePalettes.GRADIENT_PRESETS
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(gradientPresets, key = { it.id }) { preset ->
                            val isSelected = accentColor == preset.id
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isSelected) 0.85f else 0.4f),
                                border = BorderStroke(
                                    if (isSelected) 2.dp else 1.dp,
                                    if (isSelected) preset.primaryColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier
                                    .width(135.dp)
                                    .clickable { onUpdateAccentColor(preset.id) }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Brush.sweepGradient(preset.gradientColors))
                                            .border(1.2.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = preset.name,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontSize = 11.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) preset.primaryColor else MaterialTheme.colorScheme.onSurface
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 🌊 5. Footer Ripple Wave Studio (تخصيص التموج اللوني لشريط الفوتر)
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = accentPrimary.copy(alpha = 0.15f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Waves,
                                    contentDescription = null,
                                    tint = accentPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "التموج اللوني لشريط الفوتر",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = accentPrimary
                                ) {
                                    Text(
                                        text = "تفاعلي ✨",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "تحكم في سرعة مرور التموج، وسرعة ظهوره في كل دورة، ولونه",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }

                // Section A: Wave Color Selection
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "١. لون التموج اللوني:",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    val colorPresets = ThemePalettes.FOOTER_WAVE_COLOR_PRESETS
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(colorPresets, key = { it.id }) { preset ->
                            val isSelected = footerWaveColor == preset.id
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isSelected) 0.85f else 0.4f),
                                border = BorderStroke(
                                    if (isSelected) 2.dp else 1.dp,
                                    if (isSelected) accentPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier
                                    .width(115.dp)
                                    .clickable { onUpdateFooterWaveColor(preset.id) }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val sweepColors = if (preset.previewGradient.size >= 2) {
                                        preset.previewGradient
                                    } else if (preset.colors.size >= 2) {
                                        preset.colors
                                    } else {
                                        listOf(accentPrimary, accentPrimary.copy(alpha = 0.5f))
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Brush.sweepGradient(sweepColors))
                                            .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = preset.name,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) accentPrimary else MaterialTheme.colorScheme.onSurface
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                // Section B: Wave Sweep Speed (سرعة مرورها)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "٢. سرعة مرور التموج (Sweep Speed):",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "سرعة اجتياز الفوتر",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            WaveSettingCard(
                                title = "هادئ وناعم",
                                durationLabel = "بطيء (2.2s)",
                                subtitle = "مرور انسيابي هادئ ومريح",
                                isSelected = footerWaveSpeed == 0,
                                accent = accentPrimary,
                                onClick = { onUpdateFooterWaveSpeed(0) },
                                modifier = Modifier.weight(1f)
                            )
                            WaveSettingCard(
                                title = "متوازن وطبيعي",
                                durationLabel = "عادي (1.2s)",
                                subtitle = "السرعة القياسية المتناسقة",
                                isSelected = footerWaveSpeed == 1,
                                accent = accentPrimary,
                                onClick = { onUpdateFooterWaveSpeed(1) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            WaveSettingCard(
                                title = "سريع وحيوي",
                                durationLabel = "سريع (0.8s)",
                                subtitle = "حركة رشيقة ومرحة",
                                isSelected = footerWaveSpeed == 2,
                                accent = accentPrimary,
                                onClick = { onUpdateFooterWaveSpeed(2) },
                                modifier = Modifier.weight(1f)
                            )
                            WaveSettingCard(
                                title = "خاطف وفائق",
                                durationLabel = "فائق (0.5s)",
                                subtitle = "وميض خاطف وسريع جداً",
                                isSelected = footerWaveSpeed == 3,
                                accent = accentPrimary,
                                onClick = { onUpdateFooterWaveSpeed(3) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Section C: Wave Cycle Interval / Frequency (سرعة الظهور في كل دورة)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "٣. سرعة الظهور في كل دورة (Cycle Interval):",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "فترة التوقف بين كل تموج",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            WaveSettingCard(
                                title = "مستمر وفوري",
                                durationLabel = "توقف 0.6s",
                                subtitle = "تموج متعاقب باستمرار",
                                isSelected = footerWaveInterval == 0,
                                accent = accentPrimary,
                                onClick = { onUpdateFooterWaveInterval(0) },
                                modifier = Modifier.weight(1f)
                            )
                            WaveSettingCard(
                                title = "سريع ومتكرر",
                                durationLabel = "توقف 1.5s",
                                subtitle = "ظهور متكرر وبإيقاع نشط",
                                isSelected = footerWaveInterval == 1,
                                accent = accentPrimary,
                                onClick = { onUpdateFooterWaveInterval(1) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            WaveSettingCard(
                                title = "متوازن ومريح",
                                durationLabel = "توقف 3.0s",
                                subtitle = "الإيقاع الطبيعي المريح للعين",
                                isSelected = footerWaveInterval == 2,
                                accent = accentPrimary,
                                onClick = { onUpdateFooterWaveInterval(2) },
                                modifier = Modifier.weight(1f)
                            )
                            WaveSettingCard(
                                title = "هادئ ومتباعد",
                                durationLabel = "توقف 5.0s",
                                subtitle = "ظهور هادئ على فترات",
                                isSelected = footerWaveInterval == 3,
                                accent = accentPrimary,
                                onClick = { onUpdateFooterWaveInterval(3) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Live Mini Footer Wave Strip
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "معاينة حية وفورية للتموج الحالي:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    MiniFooterWavePreview(
                        footerWaveSpeed = footerWaveSpeed,
                        footerWaveInterval = footerWaveInterval,
                        footerWaveColor = footerWaveColor,
                        accentColorId = accentColor,
                        accentPrimary = accentPrimary
                    )
                }
            }
        }

        // 🎨 6. Background Style & Card Effects Card (نمط خلفية التطبيق وتأثيرات الإطارات)
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column {
                    Text(
                        text = "نمط خلفية التطبيق",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "اختر درجة عمق السواد أو النقاء لخلفيات الشاشات",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BackgroundStyleCard(
                        title = "الافتراضي",
                        subtitle = "ناعم ونقي",
                        isSelected = backgroundStyle == 0,
                        accent = accentPrimary,
                        onClick = { onUpdateBackgroundStyle(0) },
                        modifier = Modifier.weight(1f)
                    )

                    BackgroundStyleCard(
                        title = "AMOLED",
                        subtitle = "سواد عميق",
                        isSelected = backgroundStyle == 1,
                        accent = accentPrimary,
                        onClick = { onUpdateBackgroundStyle(1) },
                        modifier = Modifier.weight(1f)
                    )

                    BackgroundStyleCard(
                        title = "أبيض ناصع",
                        subtitle = "مريح وأنيق",
                        isSelected = backgroundStyle == 2,
                        accent = accentPrimary,
                        onClick = { onUpdateBackgroundStyle(2) },
                        modifier = Modifier.weight(1f)
                    )
                }

                SettingsSwitchRow(
                    title = "تحريك إطارات أحدث الأعمال والتدرجات",
                    subtitle = "تفعيل هالة الضوء المتحركة والتموج التجميلي لبطاقات المانجا الحديثة",
                    checked = cardAnimationEnabled,
                    onCheckedChange = onUpdateCardAnimationEnabled,
                    accent = accentPrimary
                )
            }
        }

        // 🌌 7. Legendary Cosmic Space Footer Aura Section (فضاء الفوتر الكوني)
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isCosmicUnlocked) Color(0xFF0F0B1E) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            border = BorderStroke(
                1.2.dp,
                if (isCosmicUnlocked) Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), Color(0xFF38BDF8), Color(0xFFFFD700)))
                else Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)))
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isCosmicUnlocked) Color(0xFF8B5CF6).copy(alpha = 0.25f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = if (isCosmicUnlocked) Color(0xFFFFD700) else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "المظهر الأسطوري: فضاء الفوتر الكوني",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCosmicUnlocked) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp
                                )
                            )
                            Text(
                                text = if (isCosmicUnlocked) "مفتوح ومتاح للاستخدام ✨" else "يفتح تلقائياً بعد قراءة 500 فصل 🔒",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isCosmicUnlocked) Color(0xFF38BDF8) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.5.sp
                                )
                            )
                        }
                    }

                    Switch(
                        checked = cosmicSpaceFooterEnabled && isCosmicUnlocked,
                        onCheckedChange = { if (isCosmicUnlocked) onUpdateCosmicSpaceFooterEnabled(it) },
                        enabled = isCosmicUnlocked,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF8B5CF6),
                            uncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                // 500 Chapters Progress Bar
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val progressFraction = (totalRead / 500f).coerceIn(0f, 1f)
                    val percent = (progressFraction * 100).toInt()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "شريط إنجاز القراءة الكوني:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isCosmicUnlocked) Color(0xFFE0E7FF) else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                        Text(
                            text = if (isCosmicUnlocked && uiState.currentUser?.isAdmin == true) "صلاحية مشرف 👑 ($totalRead / 500)" else "$totalRead من 500 فصل ($percent%)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isCosmicUnlocked) Color(0xFFFFD700) else accentPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            )
                        )
                    }

                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (isCosmicUnlocked) Color(0xFF8B5CF6) else accentPrimary,
                        trackColor = if (isCosmicUnlocked) Color(0xFF1E1B4B) else MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                Text(
                    text = "تصميم فضاء أسطوري ينطلق من خلف شريط الفوتر السفلي كظل أسود ناعم يتنفس بنعومة نحو الأعلى مع نجوم متوهجة تتحرك وتتلاشى بسلاسة تامة بدون أي حواف حادة أو تأثير على عناصر الفوتر.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (isCosmicUnlocked) Color(0xFFB0BEC5) else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.5.sp,
                        lineHeight = 15.sp
                    )
                )
            }
        }

        // 🔄 8. Reset Appearance Defaults Action
        OutlinedButton(
            onClick = {
                onUpdateThemeMode(1) // Dark mode
                onUpdateAccentColor(0) // Default indigo
                onUpdateBackgroundStyle(0) // Default
                onUpdateFooterWaveSpeed(1) // Normal
                onUpdateFooterWaveInterval(2) // Balanced
                onUpdateFooterWaveColor(0) // Matching theme
                onUpdateCardAnimationEnabled(true)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "استعادة مظهر نكسوس الافتراضي",
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )
            }
        }
    }
}

/**
 * Interactive Live Preview Studio Card showing active visual identity in real-time
 */
@Composable
private fun LiveAppearanceStudioCard(
    themeMode: Int,
    accentPreset: com.example.ui.theme.ThemePalettePreset,
    backgroundStyle: Int,
    cardAnimationEnabled: Boolean,
    footerWaveSpeed: Int,
    footerWaveInterval: Int,
    footerWaveColor: Int
) {
    val accentPrimary = accentPreset.primaryColor
    val isWhiteTheme = accentPreset.id == 8 || accentPreset.id == 18

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isWhiteTheme) Color(0xFFF8FAFC) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.5.dp,
            Brush.linearGradient(
                listOf(
                    accentPrimary.copy(alpha = 0.7f),
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                )
            )
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = accentPrimary.copy(alpha = 0.18f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = accentPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "لوحة المعاينة التفاعلية المباشرة",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "تفاعل فوري مع إعدادات المظهر والتموج",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accentPrimary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "مباشر ●",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = accentPrimary,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }

            // Interactive Mini Phone Canvas
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = when (backgroundStyle) {
                    1 -> Color(0xFF030712)
                    2 -> Color(0xFFFFFFFF)
                    else -> if (themeMode == 1) Color(0xFF0F172A) else Color(0xFFF1F5F9)
                },
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Mini Top Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(accentPrimary)
                            )
                            Text(
                                text = "NEXUS AP",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.5.sp,
                                    color = if (themeMode == 1 || backgroundStyle == 1) Color.White else Color(0xFF0F172A)
                                )
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = accentPrimary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = accentPreset.name,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    color = accentPrimary,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Mini Manga Card Mockup
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (themeMode == 1 || backgroundStyle == 1) Color(0xFF1E293B) else Color.White,
                        border = BorderStroke(
                            1.dp,
                            if (cardAnimationEnabled) accentPrimary.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = accentPrimary.copy(alpha = 0.25f),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = accentPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "سيد التنانين السماوية",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (themeMode == 1 || backgroundStyle == 1) Color.White else Color(0xFF0F172A)
                                    )
                                )
                                Text(
                                    text = "فصل 184 • تحديث مستمر",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }

                    // Mini Interactive Bottom Navigation Footer Bar with Wave Animation
                    MiniFooterWavePreview(
                        footerWaveSpeed = footerWaveSpeed,
                        footerWaveInterval = footerWaveInterval,
                        footerWaveColor = footerWaveColor,
                        accentColorId = accentPreset.id,
                        accentPrimary = accentPrimary
                    )
                }
            }
        }
    }
}

/**
 * Animated Mini Footer Wave Bar Preview
 */
@Composable
private fun MiniFooterWavePreview(
    footerWaveSpeed: Int,
    footerWaveInterval: Int,
    footerWaveColor: Int,
    accentColorId: Int,
    accentPrimary: Color
) {
    key(footerWaveSpeed, footerWaveInterval, footerWaveColor, accentColorId) {
        val sweepDuration = remember(footerWaveSpeed) {
            when (footerWaveSpeed) {
                0 -> 2200
                1 -> 1200
                2 -> 800
                3 -> 500
                else -> 1200
            }
        }

        val pauseDuration = remember(footerWaveInterval) {
            when (footerWaveInterval) {
                0 -> 600
                1 -> 1500
                2 -> 3000
                3 -> 5000
                else -> 3000
            }
        }

        val totalDuration = sweepDuration + pauseDuration

        val infiniteTransition = rememberInfiniteTransition(label = "mini_wave_anim")
        val waveProgress by infiniteTransition.animateFloat(
            initialValue = -0.3f,
            targetValue = 1.3f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = totalDuration
                    -0.3f at 0
                    1.3f at sweepDuration using FastOutSlowInEasing
                    1.3f at totalDuration
                },
                repeatMode = RepeatMode.Restart
            ),
            label = "mini_wave_sweep"
        )

        val waveAlpha by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = totalDuration
                    0.2f at 0
                    0.85f at (sweepDuration / 2)
                    0.15f at (sweepDuration - 60)
                    0f at sweepDuration
                    0f at totalDuration
                },
                repeatMode = RepeatMode.Restart
            ),
            label = "mini_wave_alpha"
        )

        val cosmeticColors = remember(footerWaveColor, accentColorId) {
            val resolved = ThemePalettes.resolveFooterWaveColors(footerWaveColor, accentColorId)
            if (resolved.size >= 2) resolved else listOf(accentPrimary, accentPrimary.copy(alpha = 0.5f))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF0F172A))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Wave Shimmer Canvas with safe dimensions and horizontal gradient
            if (waveAlpha > 0.01f) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    if (canvasWidth > 0f && canvasHeight > 0f) {
                        val centerX = canvasWidth * waveProgress
                        val waveWidth = canvasWidth * 0.35f
                        val waveBrush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                cosmeticColors.first().copy(alpha = 0.35f * waveAlpha),
                                cosmeticColors[cosmeticColors.size / 2].copy(alpha = 0.85f * waveAlpha),
                                cosmeticColors.last().copy(alpha = 0.35f * waveAlpha),
                                Color.Transparent
                            ),
                            startX = centerX - waveWidth,
                            endX = centerX + waveWidth
                        )
                        drawRect(brush = waveBrush)
                    }
                }
            }

            // Mock Navigation Icons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MiniNavDot(active = true, accent = accentPrimary)
                MiniNavDot(active = false, accent = accentPrimary)
                MiniNavDot(active = false, accent = accentPrimary)
                MiniNavDot(active = false, accent = accentPrimary)
            }
        }
    }
}

@Composable
private fun MiniNavDot(active: Boolean, accent: Color) {
    Box(
        modifier = Modifier
            .size(if (active) 14.dp else 10.dp)
            .clip(CircleShape)
            .background(if (active) accent else Color(0xFF64748B)),
        contentAlignment = Alignment.Center
    ) {
        if (active) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}

data class QuickMasterThemeItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: String,
    val accentId: Int,
    val themeMode: Int,
    val backgroundStyle: Int,
    val swatchColors: List<Color>,
    val badge: String
)

@Composable
private fun QuickMasterThemeCard(
    preset: QuickMasterThemeItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primarySwatch = preset.swatchColors.firstOrNull() ?: MaterialTheme.colorScheme.primary
    val isWhitePreset = preset.accentId == 8

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) {
            if (isWhitePreset) Color(0xFFF1F5F9) else primarySwatch.copy(alpha = 0.16f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        },
        border = BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) {
                if (isWhitePreset) Color(0xFF0F172A) else primarySwatch
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
            }
        ),
        modifier = modifier
            .width(132.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Top Row: Emoji & Swatch Dot
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = preset.icon,
                    fontSize = 20.sp
                )

                // Swatch Indicator with Checkmark
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(
                            if (preset.swatchColors.size > 1) {
                                Brush.sweepGradient(preset.swatchColors)
                            } else {
                                Brush.linearGradient(listOf(primarySwatch, primarySwatch))
                            }
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = if (isWhitePreset) Color(0xFF0F172A) else Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Title & Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = preset.title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                        color = if (isSelected) {
                            if (isWhitePreset) Color(0xFF0F172A) else primarySwatch
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        fontSize = 12.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }

            // Subtitle
            Text(
                text = preset.subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 10.sp,
                    color = if (isSelected) {
                        if (isWhitePreset) Color(0xFF334155) else MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    lineHeight = 13.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LightingModeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isSelected) 0.85f else 0.35f),
        border = BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) accent else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) accent else MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 9.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun PaletteCategoryTab(
    title: String,
    count: Int,
    isSelected: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(9.dp),
        color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
        border = if (isSelected) BorderStroke(1.dp, accent.copy(alpha = 0.4f)) else null,
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$title ($count)",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
private fun WaveSettingCard(
    title: String,
    durationLabel: String,
    subtitle: String,
    isSelected: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isSelected) 0.85f else 0.35f),
        border = BorderStroke(
            if (isSelected) 1.8.dp else 1.dp,
            if (isSelected) accent else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 9.dp, horizontal = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) accent else MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp
                    )
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (isSelected) accent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = durationLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) accent else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 9.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 2. Reader & Display Sub-Page
 */
@Composable
private fun ReaderSubPage(
    uiState: HomeUiState,
    accentPrimary: Color,
    onUpdateReaderMode: (Int) -> Unit,
    onUpdateImageQuality: (Int) -> Unit,
    onUpdateKeepScreenOn: (Boolean) -> Unit,
    onUpdatePreventChapterCache: (Boolean) -> Unit
) {
    val readerMode = uiState.appSettings.readerMode
    val imageQuality = uiState.appSettings.imageQuality
    val keepScreenOn = uiState.appSettings.keepScreenOn
    val preventChapterCache = uiState.appSettings.preventChapterCache

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "نمط قارئ الفصول الافتراضي:",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReaderModeOptionCard(
                    title = "ويب تون",
                    subtitle = "عمودي مستمر",
                    isSelected = readerMode == 0,
                    accent = accentPrimary,
                    onClick = { onUpdateReaderMode(0) },
                    modifier = Modifier.weight(1f)
                )

                ReaderModeOptionCard(
                    title = "صفحات",
                    subtitle = "يمين لليسار RTL",
                    isSelected = readerMode == 1,
                    accent = accentPrimary,
                    onClick = { onUpdateReaderMode(1) },
                    modifier = Modifier.weight(1f)
                )

                ReaderModeOptionCard(
                    title = "أفقي",
                    subtitle = "يسار لليمين LTR",
                    isSelected = readerMode == 2,
                    accent = accentPrimary,
                    onClick = { onUpdateReaderMode(2) },
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = "دقة تحميل صفحات الفصول:",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReaderModeOptionCard(
                    title = "فائقة HD",
                    subtitle = "أعلى دقة",
                    isSelected = imageQuality == 0,
                    accent = accentPrimary,
                    onClick = { onUpdateImageQuality(0) },
                    modifier = Modifier.weight(1f)
                )

                ReaderModeOptionCard(
                    title = "متوازن",
                    subtitle = "موصى به",
                    isSelected = imageQuality == 1,
                    accent = accentPrimary,
                    onClick = { onUpdateImageQuality(1) },
                    modifier = Modifier.weight(1f)
                )

                ReaderModeOptionCard(
                    title = "توفير بيانات",
                    subtitle = "سريع",
                    isSelected = imageQuality == 2,
                    accent = accentPrimary,
                    onClick = { onUpdateImageQuality(2) },
                    modifier = Modifier.weight(1f)
                )
            }

            SettingsSwitchRow(
                title = "إبقاء الشاشة مضاءة أثناء القراءة",
                subtitle = "منع إيقاف تشغيل الشاشة التلقائي أثناء قراءة الفصول بدون لمس مستمر",
                checked = keepScreenOn,
                onCheckedChange = onUpdateKeepScreenOn,
                accent = accentPrimary
            )

            SettingsSwitchRow(
                title = "منع تراكم كاش الفصول",
                subtitle = "تفريغ ذاكرة الصور تلقائياً بعد إنهاء الفصل لمنع امتلاء مساحة الهاتف",
                checked = preventChapterCache,
                onCheckedChange = onUpdatePreventChapterCache,
                accent = accentPrimary
            )
        }
    }
}

/**
 * 3. Downloads & Offline Sub-Page
 */
@Composable
private fun DownloadsSubPage(
    uiState: HomeUiState,
    accentPrimary: Color,
    onUpdateWifiOnlyDownloads: (Boolean) -> Unit,
    onReadChapter: (String, Int) -> Unit,
    onDeleteDownload: (String, Int) -> Unit,
    onDeleteAllDownloads: () -> Unit
) {
    var showClearDialog by remember { mutableStateOf(false) }
    val downloaded = uiState.downloadedChapters
    val wifiOnly = uiState.appSettings.wifiOnlyDownloads

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SettingsSwitchRow(
                title = "التنزيل عبر Wi-Fi فقط",
                subtitle = "منع استهلاك باقة الإنترنت الخلوية أثناء تحميل الفصول",
                checked = wifiOnly,
                onCheckedChange = onUpdateWifiOnlyDownloads,
                accent = accentPrimary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "الفصول المحملة:",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "${downloaded.size} فصلاً متاحاً للقراءة بدون نت",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                if (downloaded.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { showClearDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFEF5350).copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF5350)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("حذف الكل", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (downloaded.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    downloaded.forEach { item ->
                        DownloadedChapterRow(
                            item = item,
                            accentPrimary = accentPrimary,
                            onRead = { onReadChapter(item.mangaId, item.chapterNumber) },
                            onDelete = { onDeleteDownload(item.mangaId, item.chapterNumber) }
                        )
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "لا توجد فصول محملة حالياً. يمكنك تحميل أي فصل من صفحة تفاصيل العمل.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.5.sp
                        ),
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = {
                Text(
                    text = "حذف جميع التنزيلات",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "هل أنت متأكد من حذف كافة الفصول المحملة محلياً وتحرير مساحة التخزين؟",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteAllDownloads()
                        showClearDialog = false
                    }
                ) {
                    Text(
                        text = "نعم، حذف الكل",
                        color = Color(0xFFEF5350),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("إلغاء")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 4. Cache & Storage Sub-Page
 */
@Composable
private fun CacheStorageSubPage(
    accentPrimary: Color,
    onClearCache: () -> Unit
) {
    val context = LocalContext.current
    var cleanedSuccess by remember { mutableStateOf(false) }
    val cacheSize = AppSettingsManager.getInstance(context).getCalculatedCacheSize(context)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "حجم الذاكرة المؤقتة:",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "مساحة الصور المصغرة والبيانات المخبأة",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                Text(
                    text = cacheSize,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = accentPrimary
                    )
                )
            }

            Button(
                onClick = {
                    onClearCache()
                    cleanedSuccess = true
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (cleanedSuccess) Color(0xFF10B981) else accentPrimary.copy(alpha = 0.15f),
                    contentColor = if (cleanedSuccess) Color.White else accentPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (cleanedSuccess) Icons.Default.CheckCircle else Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (cleanedSuccess) "تم تنظيف الكاش بنجاح ✓" else "تفريغ الذاكرة المؤقتة الآن",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

/**
 * 5. Account & Cloud Sync Sub-Page
 */
@Composable
private fun AccountCloudSubPage(
    uiState: HomeUiState,
    accentPrimary: Color,
    onOpenAuthDialog: () -> Unit,
    onOpenAdminDialog: () -> Unit,
    onSignOut: () -> Unit,
    onSyncCloud: () -> Unit
) {
    val user = uiState.currentUser

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (user == null) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, accentPrimary.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = accentPrimary.copy(alpha = 0.15f),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = accentPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "حفظ البيانات والمزامنة الفورية",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = "سجّل دخولك لحفظ مفضلتك، سجل القراءة، واستعادتها من أي هاتف",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    Button(
                        onClick = onOpenAuthDialog,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentPrimary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text(text = "تسجيل الدخول / إنشاء حساب جديد", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        } else {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.2.dp, accentPrimary.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = accentPrimary.copy(alpha = 0.2f),
                            border = BorderStroke(1.5.dp, accentPrimary),
                            modifier = Modifier.size(50.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = (user.displayName.ifBlank { user.email }).take(1).uppercase(),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp,
                                    color = accentPrimary
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = user.displayName.ifBlank { "مستخدم Nexus" },
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                                    border = BorderStroke(0.5.dp, Color(0xFF10B981))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(11.dp))
                                        Text(text = "متصل بالسحابة", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                    }
                                }
                            }

                            Text(
                                text = user.email,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )

                            if (uiState.lastCloudSyncTime > 0) {
                                Text(
                                    text = "آخر مزامنة: ${java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(uiState.lastCloudSyncTime))}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onSyncCloud,
                            enabled = !uiState.isCloudSyncing,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accentPrimary.copy(alpha = 0.18f),
                                contentColor = accentPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (uiState.isCloudSyncing) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), color = accentPrimary, strokeWidth = 2.dp)
                                } else {
                                    Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                                Text(
                                    text = if (uiState.isCloudSyncing) "جارِ المزامنة..." else "مزامنة السحابة",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = onSignOut,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text(text = "خروج", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Protected Admin Card
            if (user.isAdmin) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1810)),
                    border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(NexusGold, NexusOrange))),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = NexusGold.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, NexusGold),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, tint = NexusGold, modifier = Modifier.size(26.dp))
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "صلاحيات الأدمن المحمية",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp,
                                        color = NexusGold
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = NexusGold.copy(alpha = 0.25f)
                                    ) {
                                        Text(
                                            text = "MASTER ADMIN",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = NexusGold,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "حساب المشرف الرسمي alsaid66900@gmail.com. نشر إعلانات ومتابعة المزامنة السحابية.",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFFE2D6C0)
                                )
                            }
                        }

                        Button(
                            onClick = onOpenAdminDialog,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NexusGold,
                                contentColor = Color(0xFF1E1810)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color(0xFF1E1810), modifier = Modifier.size(18.dp))
                                Text(text = "فتح لوحة تحكم المشرف (Admin Dashboard)", fontWeight = FontWeight.Black, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 6. Reports & Feature Requests Sub-Page
 */
@Composable
private fun ReportsSubPage(
    uiState: HomeUiState,
    accentPrimary: Color,
    onOpenSubmitReportDialog: (String, String) -> Unit,
    onOpenUserReportsDialog: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = accentPrimary.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, accentPrimary.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenSubmitReportDialog("", "") }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = accentPrimary.copy(alpha = 0.2f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = accentPrimary, modifier = Modifier.size(20.dp))
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "إرسال بلاغ أو طلب عمل جديد",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "الإبلاغ عن مشكلة في فصل أو سيرفر، أو طلب إضافة مانجا أو ميزة جديدة",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = accentPrimary, modifier = Modifier.size(16.dp))
                }
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF6366F1).copy(alpha = 0.08f),
                border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenUserReportsDialog() }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF6366F1).copy(alpha = 0.2f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.History, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(20.dp))
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "سجل بلاغاتي وردود الإدارة",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            if (uiState.userReports.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF6366F1).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "${uiState.userReports.size} بلاغ",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF6366F1),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "متابعة حالة بلاغاتك وتعديلاتها وقرارات المشرفين والردود الإدارية",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

/**
 * 7. About & Updates Sub-Page
 */
@Composable
private fun AboutUpdatesSubPage(
    uiState: HomeUiState,
    accentPrimary: Color,
    onTriggerUpdate: () -> Unit,
    onCheckCloudUpdates: () -> Unit
) {
    val context = LocalContext.current
    val updateInfo = uiState.updateInfo

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Cloud Updates Check Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "حالة التحديثات السحابية:",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = AppVersionConfig.getCurrentVersionBadge(),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    Button(
                        onClick = onCheckCloudUpdates,
                        enabled = !updateInfo.isChecking,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentPrimary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        if (updateInfo.isChecking) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        } else {
                            Text("فحص السحابة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (updateInfo.updateAvailable) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "🚀 تحديث جديد متوفر: v${updateInfo.latestVersion}",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981),
                                fontSize = 13.sp
                            )
                            if (updateInfo.releaseNotes.isNotBlank()) {
                                Text(
                                    text = updateInfo.releaseNotes,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Button(
                                onClick = onTriggerUpdate,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("تحديث الآن", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // About Info & Telegram Channel Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Nexus Manga & Manhwa Reader",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                Text(
                    text = AppVersionConfig.getChangelogHeader(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = accentPrimary,
                        fontSize = 11.5.sp
                    )
                )

                AppVersionConfig.CURRENT_CHANGELOG_FEATURES.forEach { feature ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(text = "•", color = accentPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(
                            text = feature,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0288D1).copy(alpha = 0.1f),
                    border = BorderStroke(0.8.dp, Color(0xFF0288D1).copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/NexusManga"))
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color(0xFF0288D1), modifier = Modifier.size(18.dp))
                        Text(text = "انضم لقناة التيليجرام الرسمية لأحدث الأخبار", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0288D1))
                    }
                }

                Text(
                    text = AppVersionConfig.getSettingsFullDetails(),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 10.sp
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// UI Building Blocks & Cards
// -------------------------------------------------------------

@Composable
private fun ThemeModeCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isSelected) 0.8f else 0.4f),
        border = BorderStroke(
            if (isSelected) 1.5.dp else 1.dp,
            if (isSelected) accent else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Brightness4,
                contentDescription = null,
                tint = if (isSelected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) accent else MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 9.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun BackgroundStyleCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isSelected) 0.8f else 0.4f),
        border = BorderStroke(
            if (isSelected) 1.5.dp else 1.dp,
            if (isSelected) accent else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) accent else MaterialTheme.colorScheme.onSurface,
                    fontSize = 11.5.sp
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun ReaderModeOptionCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isSelected) 0.8f else 0.4f),
        border = BorderStroke(
            if (isSelected) 1.5.dp else 1.dp,
            if (isSelected) accent else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) accent else MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 9.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accent: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = accent,
                uncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

@Composable
private fun DownloadedChapterRow(
    item: DownloadedChapter,
    accentPrimary: Color,
    onRead: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${item.mangaTitle} - الفصل ${item.chapterNumber}",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${item.totalPages} صفحة • ${item.formattedSize}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Button(
                onClick = onRead,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentPrimary),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("قراءة", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "حذف الفصل",
                    tint = Color(0xFFEF5350),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
