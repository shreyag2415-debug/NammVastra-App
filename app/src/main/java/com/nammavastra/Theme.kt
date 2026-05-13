package com.nammavastra
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF5A5A40),
    background = Color(0xFFF5F2ED),
    surface = Color(0xFFF5F2ED)
)

@Composable
fun NammaVastraTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColorScheme, content = content)
}