package com.ven.app.ui.navigation

import com.ven.app.ui.icons.VexIconPair
import com.ven.app.ui.icons.VexIcons

/**
 * 5 Tab item navigasi bottom bar untuk aplikasi VEN (Virtual Exhibition).
 * Urutan dari kiri ke kanan (sesuai DESIGN.md §7):
 * 1. Home (rumah)
 * 2. Schedule (kalender bertitik)
 * 3. AI Assistant (sparkle / bintang berkilau 4 sudut)
 * 4. Explore (kaca pembesar)
 * 5. Profile (foto avatar pengguna)
 *
 * Aturan tampilan:
 * - Aktif = ikon terisi (filled), nonaktif = outline.
 * - Tanpa label teks, tanpa pill indikator.
 * - Tab Profile menampilkan foto avatar bulat 28dp dengan border saat aktif.
 */
enum class BottomNavTab(
    val route: String,
    val title: String,
    val iconPair: VexIconPair? = null,
) {
    Home(
        route = "home",
        title = "Home",
        iconPair = VexIcons.Nav.Home,
    ),
    Schedule(
        route = "schedule",
        title = "Schedule",
        iconPair = VexIcons.Nav.Schedule,
    ),
    Ai(
        route = "ai",
        title = "AI Assistant",
        iconPair = VexIcons.Nav.Ai,
    ),
    Explore(
        route = "explore",
        title = "Explore",
        iconPair = VexIcons.Nav.Explore,
    ),
    Profile(
        route = "profile",
        title = "Profile",
        iconPair = null,
    );

    val isProfile: Boolean
        get() = this == Profile
}
