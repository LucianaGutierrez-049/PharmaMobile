package pe.edu.upeu.pharmamobilee.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Paleta de la marca: verde esmeralda (confianza sanitaria) con neutros fríos y limpios.
private val Esmeralda = Color(0xFF0B6B57)
private val EsmeraldaClaro = Color(0xFF5FD9B6)
private val AzulConfianza = Color(0xFF2F5FA7)
private val Ambar = Color(0xFF8A5100)
private val RojoAlerta = Color(0xFFBA1A1A)

private val LightColors = lightColorScheme(
    primary = Esmeralda,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC8F2E3),
    onPrimaryContainer = Color(0xFF00201A),
    secondary = AzulConfianza,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCE6FF),
    onSecondaryContainer = Color(0xFF001A40),
    tertiary = Ambar,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE2C2),
    onTertiaryContainer = Color(0xFF2C1600),
    background = Color(0xFFF7FAF9),
    onBackground = Color(0xFF161D1B),
    surface = Color(0xFFF7FAF9),
    onSurface = Color(0xFF161D1B),
    surfaceVariant = Color(0xFFDDE6E2),
    onSurfaceVariant = Color(0xFF424B47),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF1F5F3),
    surfaceContainer = Color(0xFFEBF0EE),
    surfaceContainerHigh = Color(0xFFE5EBE8),
    surfaceContainerHighest = Color(0xFFDFE5E2),
    outline = Color(0xFF727C78),
    outlineVariant = Color(0xFFC2CBC7),
    error = RojoAlerta,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

private val DarkColors = darkColorScheme(
    primary = EsmeraldaClaro,
    onPrimary = Color(0xFF00382C),
    primaryContainer = Color(0xFF005141),
    onPrimaryContainer = Color(0xFFC8F2E3),
    secondary = Color(0xFFB0C6FF),
    onSecondary = Color(0xFF002E69),
    secondaryContainer = Color(0xFF14468F),
    onSecondaryContainer = Color(0xFFDCE6FF),
    tertiary = Color(0xFFFFB875),
    onTertiary = Color(0xFF4A2800),
    tertiaryContainer = Color(0xFF693C00),
    onTertiaryContainer = Color(0xFFFFE2C2),
    background = Color(0xFF0F1513),
    onBackground = Color(0xFFDEE4E1),
    surface = Color(0xFF0F1513),
    onSurface = Color(0xFFDEE4E1),
    surfaceVariant = Color(0xFF404945),
    onSurfaceVariant = Color(0xFFC0C9C5),
    surfaceContainerLowest = Color(0xFF0A0F0E),
    surfaceContainerLow = Color(0xFF171D1B),
    surfaceContainer = Color(0xFF1B2120),
    surfaceContainerHigh = Color(0xFF252B2A),
    surfaceContainerHighest = Color(0xFF303634),
    outline = Color(0xFF8A938F),
    outlineVariant = Color(0xFF404945),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val PharmaTypography = Typography(
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 38.sp, letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 34.sp, letterSpacing = (-0.25).sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 30.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 19.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    titleSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp)
)

private val PharmaShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun PharmaMobilTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = PharmaTypography,
        shapes = PharmaShapes,
        content = content
    )
}
