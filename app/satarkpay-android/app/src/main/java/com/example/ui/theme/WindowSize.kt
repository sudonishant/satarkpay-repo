package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Three-tier breakpoint system matching Material 3 guidelines. */
enum class WindowWidthClass { COMPACT, MEDIUM, EXPANDED }
enum class WindowHeightClass { COMPACT, MEDIUM, EXPANDED }

data class WindowSizeInfo(
    val widthClass: WindowWidthClass,
    val heightClass: WindowHeightClass,
    val widthDp: Dp,
    val heightDp: Dp,
) {
    /** Phone portrait / very narrow window */
    val isCompact get() = widthClass == WindowWidthClass.COMPACT
    /** Phone landscape / small tablet */
    val isMedium get() = widthClass == WindowWidthClass.MEDIUM
    /** Tablet / desktop / foldable-unfolded */
    val isExpanded get() = widthClass == WindowWidthClass.EXPANDED
    /** Tablets and larger */
    val isTabletOrLarger get() = !isCompact
    /** Short screen — phone landscape */
    val isShortHeight get() = heightClass == WindowHeightClass.COMPACT
    /** Columns to use for grids */
    val gridColumns: Int get() = when (widthClass) {
        WindowWidthClass.COMPACT  -> 2
        WindowWidthClass.MEDIUM   -> 3
        WindowWidthClass.EXPANDED -> 4
    }
    /** Horizontal content padding */
    val contentPadding: Dp get() = when (widthClass) {
        WindowWidthClass.COMPACT  -> 16.dp
        WindowWidthClass.MEDIUM   -> 28.dp
        WindowWidthClass.EXPANDED -> 48.dp
    }
    /** Maximum card width so content doesn't stretch too wide on tablets */
    val maxCardWidth: Dp get() = when (widthClass) {
        WindowWidthClass.COMPACT  -> Dp.Infinity
        WindowWidthClass.MEDIUM   -> 560.dp
        WindowWidthClass.EXPANDED -> 720.dp
    }
}

@Composable
fun rememberWindowSizeInfo(): WindowSizeInfo {
    val config = LocalConfiguration.current
    val widthDp  = config.screenWidthDp.dp
    val heightDp = config.screenHeightDp.dp
    return remember(widthDp, heightDp) {
        WindowSizeInfo(
            widthClass = when {
                widthDp < 600.dp  -> WindowWidthClass.COMPACT
                widthDp < 840.dp  -> WindowWidthClass.MEDIUM
                else              -> WindowWidthClass.EXPANDED
            },
            heightClass = when {
                heightDp < 480.dp -> WindowHeightClass.COMPACT
                heightDp < 900.dp -> WindowHeightClass.MEDIUM
                else              -> WindowHeightClass.EXPANDED
            },
            widthDp  = widthDp,
            heightDp = heightDp,
        )
    }
}
