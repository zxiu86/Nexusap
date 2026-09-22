package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.NexusGold
import com.example.ui.theme.NexusOrange
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

import com.example.ui.theme.ThemePalettes
import com.example.ui.theme.ThemePalettePreset

@Composable
fun AuthDialog(
    isOpen: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    successMessage: String?,
    accentColor: Int = 0,
    onDismiss: () -> Unit,
    onSignInEmail: (String, String) -> Unit,
    onSignUpEmail: (String, String, String) -> Unit,
    onResetPassword: (String) -> Unit = {},
    onGoogleSignInClick: () -> Unit = {},
    onClearMessages: () -> Unit = {}
) {
    if (!isOpen) return

    val preset = remember(accentColor) { ThemePalettes.getPresetById(accentColor) }
    val primaryColor = preset.primaryColor
    val secondaryColor = preset.secondaryColor
    val onPrimaryColor = preset.onPrimary
    val gradientColors = preset.gradientColors
    val isMultiColor = preset.isMultiColor

    val tabBrush = remember(preset) {
        if (gradientColors.size > 1) {
            Brush.horizontalGradient(gradientColors)
        } else {
            Brush.horizontalGradient(listOf(primaryColor, secondaryColor))
        }
    }

    val cardBorderBrush = remember(preset) {
        if (gradientColors.size > 1) {
            Brush.verticalGradient(
                gradientColors.map { it.copy(alpha = 0.75f) } + listOf(Color.White.copy(alpha = 0.08f))
            )
        } else {
            Brush.verticalGradient(
                listOf(
                    primaryColor.copy(alpha = 0.75f),
                    secondaryColor.copy(alpha = 0.45f),
                    Color.White.copy(alpha = 0.06f)
                )
            )
        }
    }

    // 0: Sign In, 1: Sign Up
    var authMode by remember { mutableIntStateOf(0) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = {
            onClearMessages()
            onDismiss()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(26.dp))
                .testTag("auth_dialog_card"),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
            ),
            border = BorderStroke(1.5.dp, cardBorderBrush),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Title & Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            onClearMessages()
                            onDismiss()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (authMode == 0) "تسجيل الدخول" else "إنشاء حساب جديد",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.size(36.dp))
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Info banner explaining the lightweight per-user persistence
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = primaryColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "تسجيل فوري وسريع لحفظ المفضلة والمشاهدة لاحقاً لكل مستخدم",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = primaryColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mode Tabs (Sign In vs Sign Up)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val loginBgModifier = if (authMode == 0) {
                        Modifier.background(tabBrush)
                    } else {
                        Modifier
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .then(loginBgModifier)
                            .clickable {
                                onClearMessages()
                                authMode = 0
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "تسجيل الدخول",
                            fontWeight = if (authMode == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (authMode == 0) onPrimaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }

                    val registerBgModifier = if (authMode == 1) {
                        Modifier.background(tabBrush)
                    } else {
                        Modifier
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .then(registerBgModifier)
                            .clickable {
                                onClearMessages()
                                authMode = 1
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "حساب جديد",
                            fontWeight = if (authMode == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (authMode == 1) onPrimaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Error Message Banner
                AnimatedVisibility(
                    visible = !errorMessage.isNullOrBlank(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEF4444).copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage ?: "",
                                color = Color(0xFFFF8888),
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Success Message Banner
                AnimatedVisibility(
                    visible = !successMessage.isNullOrBlank(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = successMessage ?: "",
                                color = Color(0xFF34D399),
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Form Fields (Only Username & Password)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Username Field
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("اسم المستخدم", fontSize = 12.sp) },
                        placeholder = { Text("مثال: zxiuzaid أو اسمك المستعار", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = primaryColor
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_username_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                            focusedLabelColor = primaryColor,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            cursorColor = primaryColor
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next,
                            autoCorrectEnabled = false
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("كلمة المرور", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = primaryColor
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "إخفاء كلمة المرور" else "إظهار كلمة المرور",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                            focusedLabelColor = primaryColor,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            cursorColor = primaryColor
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                if (username.isNotBlank() && password.isNotBlank()) {
                                    if (authMode == 0) {
                                        onSignInEmail(username, password)
                                    } else {
                                        onSignUpEmail(username, password, username)
                                    }
                                }
                            }
                        )
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Submit Button
                val buttonBgModifier = if (isMultiColor || gradientColors.size > 1) {
                    Modifier.background(tabBrush, RoundedCornerShape(14.dp))
                } else {
                    Modifier
                }

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        onClearMessages()
                        if (username.isNotBlank() && password.isNotBlank()) {
                            if (authMode == 0) {
                                onSignInEmail(username, password)
                            } else {
                                onSignUpEmail(username, password, username)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .then(buttonBgModifier)
                        .testTag("auth_submit_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isMultiColor || gradientColors.size > 1) Color.Transparent else primaryColor,
                        contentColor = onPrimaryColor,
                        disabledContainerColor = primaryColor.copy(alpha = 0.4f),
                        disabledContentColor = onPrimaryColor.copy(alpha = 0.5f)
                    ),
                    enabled = !isLoading && username.isNotBlank() && password.isNotBlank()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = onPrimaryColor,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (authMode == 0) "تسجيل الدخول" else "إنشاء الحساب",
                            fontWeight = FontWeight.Bold,
                            color = onPrimaryColor,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
