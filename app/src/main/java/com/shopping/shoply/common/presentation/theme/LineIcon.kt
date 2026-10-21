package com.shopping.shoply.common.presentation.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/*
 * Builders for the Shoply line icons, ported from the design's app-ui.jsx:
 * 24 × 24 grid, 1.7 stroke, round caps and joins. Paths are SVG path strings, copied as-is except
 * that packed arc flags ("0 01") are written out ("0 0 1") so every parser reads them the same way.
 * Colour comes from Icon's tint, so the black here is only a placeholder.
 */

/** An outlined icon: every path is stroked. [autoMirror] flips it in right-to-left layouts. */
internal fun lineIcon(name: String, vararg paths: String, autoMirror: Boolean = false): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
        autoMirror = autoMirror,
    ).apply {
        paths.forEach { path ->
            addPath(
                pathData = addPathNodes(path),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.7f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
    }.build()

/** A solid icon: every path is filled (the design's filled heart and star). */
internal fun filledIcon(name: String, vararg paths: String): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        paths.forEach { path -> addPath(pathData = addPathNodes(path), fill = SolidColor(Color.Black)) }
    }.build()

/** SVG `<circle cx cy r>` as a path string (two half arcs). */
internal fun circle(cx: Float, cy: Float, r: Float): String =
    "M${cx - r},${cy}a$r,$r 0 1 0 ${2 * r},0a$r,$r 0 1 0 ${-2 * r},0z"

/** SVG `<rect x y width height rx>` as a path string. */
internal fun rect(x: Float, y: Float, w: Float, h: Float, rx: Float): String {
    val sw = w - 2 * rx
    val sh = h - 2 * rx
    return "M${x + rx},${y}h$sw" +
        "a$rx,$rx 0 0 1 $rx,${rx}v$sh" +
        "a$rx,$rx 0 0 1 ${-rx},${rx}h${-sw}" +
        "a$rx,$rx 0 0 1 ${-rx},${-rx}v${-sh}" +
        "a$rx,$rx 0 0 1 $rx,${-rx}z"
}
