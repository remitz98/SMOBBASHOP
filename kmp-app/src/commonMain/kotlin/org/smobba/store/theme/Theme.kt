package org.smobba.store.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val IndigoPrimary = Color(0xFF4338CA)
val IndigoSecondary = Color(0xFF4F46E5)
val NavyDark = Color(0xFF0F172A)
val SlateBackground = Color(0xFFF8FAFC)
val SlateCard = Color(0xFFFFFFFF)
val EmeraldSuccess = Color(0xFF059669)
val AmberPending = Color(0xFFD97706)
val RoseDanger = Color(0xFFE11D48)

private val LightColorScheme = lightColorScheme(
    primary = IndigoPrimary,
    onPrimary = Color.White,
    secondary = IndigoSecondary,
    onSecondary = Color.White,
    background = SlateBackground,
    onBackground = NavyDark,
    surface = SlateCard,
    onSurface = NavyDark
)

@Composable
fun BbaStoreTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
