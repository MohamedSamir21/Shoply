package com.shopping.shoply.common.presentation.theme

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * App icons from the Shoply design (app-ui.jsx `AIc`), the ones Home and the bottom navigation use.
 * Each one is built on first use, then reused. Add more here as screens need them.
 */
object ShoplyAppIcons {
    // —— Bottom navigation ——
    val Home: ImageVector by lazy {
        lineIcon("Home", "M4 10.5L12 4l8 6.5V19a1.5 1.5 0 0 1 -1.5 1.5H15v-5.5H9v5.5H5.5A1.5 1.5 0 0 1 4 19z")
    }
    val Grid: ImageVector by lazy {
        lineIcon(
            "Grid",
            rect(4f, 4f, 6.5f, 6.5f, 1.8f),
            rect(13.5f, 4f, 6.5f, 6.5f, 1.8f),
            rect(4f, 13.5f, 6.5f, 6.5f, 1.8f),
            rect(13.5f, 13.5f, 6.5f, 6.5f, 1.8f),
        )
    }
    val Bag: ImageVector by lazy {
        lineIcon(
            "Bag",
            "M5.5 8h13l-1 11.2a1.5 1.5 0 0 1 -1.5 1.3H8a1.5 1.5 0 0 1 -1.5 -1.3z",
            "M9 10.5V7a3 3 0 0 1 6 0v3.5",
        )
    }
    val Receipt: ImageVector by lazy {
        lineIcon(
            "Receipt",
            "M6 3.5h12v17l-2.5-1.5-2 1.5-1.5-1.2-1.5 1.2-2-1.5L6 20.5z",
            "M9 8.5h6M9 12h6M9 15.5h3.5",
        )
    }
    val User: ImageVector by lazy {
        lineIcon("User", circle(12f, 8.5f, 3.7f), "M5 20a7 7 0 0 1 14 0")
    }

    // —— Header and search ——
    val Search: ImageVector by lazy {
        lineIcon("Search", circle(11f, 11f, 6.5f), "M16 16l4 4")
    }
    val Filter: ImageVector by lazy {
        lineIcon(
            "Filter",
            "M4 7h9M18 7h2M4 17h2M11 17h9",
            circle(15.5f, 7f, 2.3f),
            circle(8.5f, 17f, 2.3f),
        )
    }
    val Bell: ImageVector by lazy {
        lineIcon("Bell", "M6 10a6 6 0 0 1 12 0v4l1.5 2.5h-15L6 14z", "M10 19.5a2 2 0 0 0 4 0")
    }
    val Pin: ImageVector by lazy {
        lineIcon(
            "Pin",
            "M12 21s-6.5-5.6-6.5-11a6.5 6.5 0 0 1 13 0c0 5.4-6.5 11-6.5 11z",
            circle(12f, 10f, 2.4f),
        )
    }
    val ChevronDown: ImageVector by lazy {
        lineIcon("ChevronDown", "M6.5 9.5L12 15l5.5-5.5")
    }
    val Back: ImageVector by lazy {
        lineIcon("Back", "M15 5l-7 7 7 7", autoMirror = true)
    }

    // —— Product card ——
    val Heart: ImageVector by lazy {
        lineIcon("Heart", "M12 19.5s-7.5-4.4-7.5-9.8A4.2 4.2 0 0 1 12 7.3a4.2 4.2 0 0 1 7.5 2.4c0 5.4-7.5 9.8-7.5 9.8z")
    }
    val HeartFilled: ImageVector by lazy {
        filledIcon(
            "HeartFilled",
            "M12 20.3l-0.4 -0.2C11.3 19.9 3.7 15.4 3.7 9.7A5 5 0 0 1 12 6.2a5 5 0 0 1 8.3 3.5c0 5.7-7.6 10.2-7.9 10.4z",
        )
    }
    val Star: ImageVector by lazy {
        filledIcon("Star", "M12 3.6l2.6 5.3 5.8 0.8-4.2 4.1 1 5.8L12 16.9l-5.2 2.7 1-5.8-4.2-4.1 5.8-0.8z")
    }
    val Plus: ImageVector by lazy {
        lineIcon("Plus", "M12 5v14M5 12h14")
    }

    // —— States and promo art ——
    val WifiOff: ImageVector by lazy {
        lineIcon(
            "WifiOff",
            "M2.5 9a14 14 0 0 1 5-2.8M10.5 5.6A14 14 0 0 1 21.5 9M5.8 12.4a9 9 0 0 1 4.2-2.2" +
                "M14.5 10.6a9 9 0 0 1 3.7 1.8M9 15.8a4.5 4.5 0 0 1 6 0M12 19.5v0.01",
            "M4 4l16 16",
        )
    }
    val Truck: ImageVector by lazy {
        lineIcon(
            "Truck",
            "M3.5 7.5a1.5 1.5 0 0 1 1.5-1.5h8a1.5 1.5 0 0 1 1.5 1.5v9h-11z",
            "M14.5 10h3.4l2.6 3v3.5h-6",
            circle(7.5f, 17.5f, 1.8f),
            circle(17f, 17.5f, 1.8f),
        )
    }
    val Split: ImageVector by lazy {
        lineIcon("Split", circle(12f, 12f, 8.5f), "M12 3.5V12h8.5M12 12l-6 6")
    }
}
