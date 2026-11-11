package com.ven.app.ui.navigation

import androidx.annotation.StringRes
import com.ven.app.R
import com.ven.app.ui.icons.VexIconPair
import com.ven.app.ui.icons.VexIcons

/**
 * 5 Tab item navigasi bottom bar untuk aplikasi VEN (Virtual Exhibition).
 * Urutan dari kiri ke kanan (sesuai DESIGN.md A15 7):
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
    @param:StringRes val titleRes: Int,
    val iconPair: VexIconPair? = null,
) {
    Home(
        route = "home",
        titleRes = R.string.bottom_nav_home,
        iconPair = VexIcons.Nav.Home,
    ),
    Schedule(
        route = "schedule",
        titleRes = R.string.bottom_nav_schedule,
        iconPair = VexIcons.Nav.Schedule,
    ),
    Ai(
        route = "ai",
        titleRes = R.string.bottom_nav_ai,
        iconPair = VexIcons.Nav.Ai,
    ),
    Explore(
        route = "explore",
        titleRes = R.string.bottom_nav_explore,
        iconPair = VexIcons.Nav.Explore,
    ),
    Profile(
        route = "profile",
        titleRes = R.string.bottom_nav_profile,
        iconPair = null,
    );

    val isProfile: Boolean
        get() = this == Profile
}
