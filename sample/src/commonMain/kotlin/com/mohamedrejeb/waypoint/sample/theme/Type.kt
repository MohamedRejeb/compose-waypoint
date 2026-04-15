package com.mohamedrejeb.waypoint.sample.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import waypoint.sample.generated.resources.PlusJakartaSans_Bold
import waypoint.sample.generated.resources.PlusJakartaSans_Medium
import waypoint.sample.generated.resources.PlusJakartaSans_Regular
import waypoint.sample.generated.resources.PlusJakartaSans_SemiBold
import waypoint.sample.generated.resources.Res

@Composable
fun SampleTypography(): Typography {
    val plusJakartaSans = FontFamily(
        Font(Res.font.PlusJakartaSans_Regular, FontWeight.Normal),
        Font(Res.font.PlusJakartaSans_Medium, FontWeight.Medium),
        Font(Res.font.PlusJakartaSans_SemiBold, FontWeight.SemiBold),
        Font(Res.font.PlusJakartaSans_Bold, FontWeight.Bold),
    )

    val defaults = Typography()

    return Typography(
        displayLarge = defaults.displayLarge.copy(fontFamily = plusJakartaSans, fontWeight = FontWeight.Bold),
        displayMedium = defaults.displayMedium.copy(fontFamily = plusJakartaSans, fontWeight = FontWeight.Bold),
        displaySmall = defaults.displaySmall.copy(fontFamily = plusJakartaSans, fontWeight = FontWeight.Bold),
        headlineLarge = defaults.headlineLarge.copy(fontFamily = plusJakartaSans, fontWeight = FontWeight.SemiBold),
        headlineMedium = defaults.headlineMedium.copy(fontFamily = plusJakartaSans, fontWeight = FontWeight.SemiBold),
        headlineSmall = defaults.headlineSmall.copy(fontFamily = plusJakartaSans, fontWeight = FontWeight.SemiBold),
        titleLarge = defaults.titleLarge.copy(fontFamily = plusJakartaSans, fontWeight = FontWeight.SemiBold),
        titleMedium = defaults.titleMedium.copy(fontFamily = plusJakartaSans, fontWeight = FontWeight.SemiBold),
        titleSmall = defaults.titleSmall.copy(fontFamily = plusJakartaSans, fontWeight = FontWeight.SemiBold),
        bodyLarge = defaults.bodyLarge.copy(fontFamily = plusJakartaSans, fontWeight = FontWeight.Normal),
        bodyMedium = defaults.bodyMedium.copy(fontFamily = plusJakartaSans, fontWeight = FontWeight.Normal),
        bodySmall = defaults.bodySmall.copy(fontFamily = plusJakartaSans, fontWeight = FontWeight.Normal),
        labelLarge = defaults.labelLarge.copy(fontFamily = plusJakartaSans, fontWeight = FontWeight.Medium),
        labelMedium = defaults.labelMedium.copy(fontFamily = plusJakartaSans, fontWeight = FontWeight.Medium),
        labelSmall = defaults.labelSmall.copy(fontFamily = plusJakartaSans, fontWeight = FontWeight.Medium),
    )
}
