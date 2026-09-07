package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.background
import androidx.compose.ui.unit.dp

private val DarkColorScheme =
  darkColorScheme(
    primary = CMKDeepBlue,
    onPrimary = Color.White,
    secondary = CMKGoldAccent,
    onSecondary = CMKSlateDark,
    background = CMKBackgroundDark,
    surface = CMKSurfaceDark,
    onBackground = Color.White,
    onSurface = Color.White,
    tertiary = Blue100
  )

private val LightColorScheme =
  lightColorScheme(
    primary = CMKDeepBlue,
    onPrimary = Color.White,
    secondary = CMKGoldAccent,
    onSecondary = CMKDeepBlue,
    background = CMKBackgroundLight,
    surface = CMKSurfaceLight,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    tertiary = CMKDeepBlue
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Enforce CMK Brand Identity by default
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

@Composable
fun FourStoryMenuIcon(tint: Color = Color.White, modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier) {
    androidx.compose.foundation.layout.Column(
        modifier = modifier.size(24.dp).padding(vertical = 3.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        repeat(4) {
            androidx.compose.foundation.layout.Box(
                modifier = androidx.compose.ui.Modifier
                    .width(18.dp)
                    .height(2.dp)
                    .background(tint, androidx.compose.foundation.shape.RoundedCornerShape(1.dp))
            )
        }
    }
}
