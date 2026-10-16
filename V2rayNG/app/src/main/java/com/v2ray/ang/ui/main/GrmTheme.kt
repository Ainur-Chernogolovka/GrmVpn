package com.v2ray.ang.ui.main

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.v2ray.ang.R

/** Colors of the GRAMESTOS VPN look: indigo to teal gradient with neon accents. */
internal object GrmColors {
    val Green = Color(0xFF35F2A0)
    val Cyan = Color(0xFF3DE1FF)
    val Pink = Color(0xFFFF5A7A)
    val PinkText = Color(0xFFFF7A93)
    val TextPrimary = Color(0xFFEAF4FF)
    val TextSecondary = Color(0xC7EAF4FF)
    val GradientTop = Color(0xFF2D1F78)
    val GradientMid = Color(0xFF1F2F8C)
    val GradientBottom = Color(0xFF0C5878)
    val Card = Color(0x17FFFFFF)
    val CardBorder = Color(0x2EFFFFFF)
    val Void = Color(0xFF060924)
    val Ink = Color(0xFF04122E)
    val Sheet = Color(0xFF151C66)
    val Field = Color(0xFF070B2E)
}

/** Fonts bundled in res/font (Russo One, JetBrains Mono, Manrope: all with Cyrillic). */
internal object GrmFonts {
    val Display = FontFamily(Font(R.font.russo_one, FontWeight.Normal))
    val Mono = FontFamily(
        Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
        Font(R.font.jetbrains_mono_bold, FontWeight.Bold),
    )
    val Body = FontFamily(
        Font(R.font.manrope_medium, FontWeight.Medium),
        Font(R.font.manrope_bold, FontWeight.Bold),
    )
}
