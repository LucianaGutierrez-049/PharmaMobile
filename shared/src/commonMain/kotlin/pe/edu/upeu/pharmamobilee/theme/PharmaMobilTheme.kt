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
import androidx.compose.ui.unit.dp

private val VerdeFarmacia = Color(0xFF0B6B57)
private val VerdeFarmaciaClaro = Color(0xFF2FBF8F)
private val AzulConfianza = Color(0xFF276EF1)
private val RojoAlerta = Color(0xFFBA1A1A)

private val LightColors = lightColorScheme(
    primary = VerdeFarmacia,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBCEEDC),
    onPrimaryContainer = Color(0xFF002116),
    secondary = AzulConfianza,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD7E2FF),
    onSecondaryContainer = Color(0xFF001B3F),
    background = Color(0xFFFAFDF9),
    onBackground = Color(0xFF191C1A),
    surface = Color(0xFFFAFDF9),
    onSurface = Color(0xFF191C1A),
    surfaceVariant = Color(0xFFDCE5DE),
    onSurfaceVariant = Color(0xFF404943),
    error = RojoAlerta,
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = VerdeFarmaciaClaro,
    onPrimary = Color(0xFF003828),
    primaryContainer = Color(0xFF00513D),
    onPrimaryContainer = Color(0xFFBCEEDC),
    secondary = Color(0xFFADC6FF),
    onSecondary = Color(0xFF002E66),
    secondaryContainer = Color(0xFF0B4C9A),
    onSecondaryContainer = Color(0xFFD7E2FF),
    background = Color(0xFF101411),
    onBackground = Color(0xFFE0E4DF),
    surface = Color(0xFF101411),
    onSurface = Color(0xFFE0E4DF),
    surfaceVariant = Color(0xFF404943),
    onSurfaceVariant = Color(0xFFC0C9C2),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

private val PharmaTypography = Typography()

private val PharmaShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun PharmaMobilTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColors
    } else {
        LightColors
    }

    MaterialTheme(
        colorScheme = colors,
        typography = PharmaTypography,
        shapes = PharmaShapes,
        content = content
    )
}
