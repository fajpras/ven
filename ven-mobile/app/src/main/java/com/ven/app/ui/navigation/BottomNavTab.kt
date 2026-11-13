package com.ven.app.ui.navigation

import com.ven.app.ui.icons.VexIconPair
import com.ven.app.ui.icons.VexIcons

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
