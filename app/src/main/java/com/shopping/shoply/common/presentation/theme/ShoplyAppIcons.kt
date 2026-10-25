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

    // —— Lists, search and contact ——
    val ChevronRight: ImageVector by lazy {
        lineIcon("ChevronRight", "M9.5 6l6 6-6 6", autoMirror = true)
    }
    val Close: ImageVector by lazy {
        lineIcon("Close", "M7 7l10 10M17 7L7 17")
    }
    val Chat: ImageVector by lazy {
        lineIcon("Chat", "M4.5 6.5a2 2 0 0 1 2-2h11a2 2 0 0 1 2 2v8a2 2 0 0 1 -2 2H10l-5.5 4z")
    }
    val Mail: ImageVector by lazy {
        lineIcon("Mail", rect(3f, 5f, 18f, 14f, 3.2f), "M5 8l6.2 4.3a1.5 1.5 0 0 0 1.6 0L19 8")
    }
    val Phone: ImageVector by lazy {
        lineIcon(
            "Phone",
            "M6.5 4h3l1.5 4-2 1.4a11 11 0 0 0 5 5l1.4-2 4 1.5v3a2 2 0 0 1 -2.2 2A15.5 15.5 0 0 1 4.5 6.2 2 2 0 0 1 6.5 4z",
        )
    }

    // —— Profile ——
    val Gear: ImageVector by lazy {
        lineIcon(
            "Gear",
            "M12 3.5l1.6 1.9 2.4 -0.6 0.8 2.3 2.3 0.8 -0.6 2.4 1.9 1.7 -1.9 1.6 0.6 2.4 -2.3 0.8 -0.8 2.3 -2.4 -0.6" +
                "L12 20.5l-1.6 -1.9 -2.4 0.6 -0.8 -2.3 -2.3 -0.8 0.6 -2.4L3.5 12l1.9 -1.7 -0.6 -2.4 2.3 -0.8 0.8 -2.3 2.4 0.6z",
            circle(12f, 12f, 3f),
        )
    }
    val Edit: ImageVector by lazy {
        lineIcon(
            "Edit",
            "M4.5 19.5l1 -4L15.8 5.2a2 2 0 0 1 2.9 0l0.1 0.1a2 2 0 0 1 0 2.9L8.5 18.5z",
            "M13.5 7.5l3 3",
        )
    }
    val Store: ImageVector by lazy {
        lineIcon(
            "Store",
            "M4 9.5l1.5 -5h13l1.5 5",
            "M4 9.5a2.7 2.7 0 0 0 5.3 0 2.7 2.7 0 0 0 5.4 0 2.7 2.7 0 0 0 5.3 0",
            "M5.5 12v8h13v-8M10 20v-4.5h4V20",
        )
    }
    val Card: ImageVector by lazy {
        lineIcon("Card", rect(3f, 5.5f, 18f, 13f, 2.6f), "M3 10h18M7 15h3")
    }
    val Help: ImageVector by lazy {
        lineIcon("Help", circle(12f, 12f, 8.5f), "M9.6 9.6a2.5 2.5 0 0 1 4.8 0.9c0 1.7 -2.4 2.1 -2.4 3.6M12 17v0.01")
    }
    val Logout: ImageVector by lazy {
        lineIcon(
            "Logout",
            "M14 4.5h3.5a2 2 0 0 1 2 2v11a2 2 0 0 1 -2 2H14",
            "M10 8l-4 4 4 4M6 12h9",
            autoMirror = true,
        )
    }

    // —— Notifications ——
    val Tag: ImageVector by lazy {
        lineIcon(
            "Tag",
            "M3.8 12.6V4.8a1 1 0 0 1 1 -1h7.8l7.6 7.6a1.4 1.4 0 0 1 0 2l-5.8 5.8a1.4 1.4 0 0 1 -2 0z",
            circle(8.3f, 8.3f, 1.5f),
        )
    }
    val Package: ImageVector by lazy {
        lineIcon("Package", "M12 3.5l8 4v9l-8 4-8-4v-9z", "M4 7.5l8 4 8-4M12 11.5v9")
    }

    // —— Product details ——
    val Share: ImageVector by lazy {
        lineIcon(
            "Share",
            circle(17.5f, 5.5f, 2.3f),
            circle(6.5f, 12f, 2.3f),
            circle(17.5f, 18.5f, 2.3f),
            "M8.5 10.8l7-4M8.5 13.2l7 4",
        )
    }
    val Minus: ImageVector by lazy {
        lineIcon("Minus", "M5.5 12h13")
    }

    // —— Cart ——
    val Trash: ImageVector by lazy {
        lineIcon(
            "Trash",
            "M4.5 7h15M9.5 7V5.2c0 -0.4 0.3 -0.7 0.7 -0.7h3.6c0.4 0 0.7 0.3 0.7 0.7V7",
            "M6.5 7l0.8 12a1.5 1.5 0 0 0 1.5 1.4h6.4a1.5 1.5 0 0 0 1.5 -1.4l0.8 -12M10 11v5.5M14 11v5.5",
        )
    }
    val Info: ImageVector by lazy {
        lineIcon("Info", circle(12f, 12f, 8.5f), "M12 11v5.5M12 7.8v0.01")
    }
    val CheckCircle: ImageVector by lazy {
        lineIcon("CheckCircle", circle(12f, 12f, 8.5f), "M8.3 12.3l2.5 2.5 5 -5.3")
    }
}
