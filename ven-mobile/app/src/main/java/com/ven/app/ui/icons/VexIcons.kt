package com.ven.app.ui.icons

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import com.ven.app.R
import com.ven.app.ui.theme.VexSize

@Immutable
data class VexIconPair(@DrawableRes val outline: Int, @DrawableRes val filled: Int)

object VexIcons {
    // Navigation icons (outline + filled pairs)
    object Nav {
        val Home = VexIconPair(R.drawable.ic_nav_home_outline, R.drawable.ic_nav_home_filled)
        val Schedule = VexIconPair(R.drawable.ic_nav_schedule_outline, R.drawable.ic_nav_schedule_filled)
        val Ai = VexIconPair(R.drawable.ic_nav_ai_outline, R.drawable.ic_nav_ai_filled)
        val Explore = VexIconPair(R.drawable.ic_nav_explore_outline, R.drawable.ic_nav_explore_filled)
    }

    // Tab icons
    object Tab {
        val Posts = VexIconPair(0, 0)     // TODO: R.drawable.ic_tab_posts_outline, _filled
        val Exhibits = VexIconPair(0, 0)  // TODO: R.drawable.ic_tab_exhibits_outline, _filled
    }

    // Action icons
    @DrawableRes val Add = R.drawable.ic_action_add
    @DrawableRes val Menu = 0             // TODO: R.drawable.ic_action_menu
    @DrawableRes val Back = 0             // TODO: R.drawable.ic_action_back
    @DrawableRes val Close = 0            // TODO: R.drawable.ic_action_close
    @DrawableRes val ChevronRight = 0     // TODO: R.drawable.ic_action_chevron_right
    @DrawableRes val ChevronDown = 0      // TODO: R.drawable.ic_action_chevron_down
    @DrawableRes val Search = 0           // TODO: R.drawable.ic_action_search
    @DrawableRes val Filter = 0           // TODO: R.drawable.ic_action_filter
    @DrawableRes val Calendar = R.drawable.ic_action_calendar
    @DrawableRes val Play = R.drawable.ic_action_play
    @DrawableRes val NewChat = 0          // TODO: R.drawable.ic_action_new_chat
    @DrawableRes val Visibility = 0       // TODO: R.drawable.ic_action_visibility
    @DrawableRes val VisibilityOff = 0    // TODO: R.drawable.ic_action_visibility_off

    // Settings icons
    @DrawableRes val SettingsProfile = 0
    @DrawableRes val SettingsEmail = 0
    @DrawableRes val SettingsPassword = 0
    @DrawableRes val SettingsAccountStatus = 0
    @DrawableRes val SettingsHistory = 0
    @DrawableRes val SettingsTheme = 0
    @DrawableRes val SettingsLanguage = 0
    @DrawableRes val SettingsPrivacy = 0
    @DrawableRes val SettingsHelp = 0
    @DrawableRes val SettingsLogout = 0

    // Editor icons
    @DrawableRes val EditorText = 0
    @DrawableRes val EditorPanel = 0
    @DrawableRes val EditorFurniture = 0

    // Status icons
    @DrawableRes val StatusCheckFilled = 0
    @DrawableRes val StatusCircleOutline = 0

    // Logos
    @DrawableRes val LogoVen = R.drawable.logo_ven
    @DrawableRes val LogoVex = R.drawable.logo_ven // Alias for DESIGN.md compatibility
    @DrawableRes val LogoGoogle = 0                // Multi-color, NO tint
}

@Composable
fun VexIcon(
    @DrawableRes id: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    size: Dp = VexSize.icon,
) {
    if (id != 0) {
        Icon(
            painter = painterResource(id),
            contentDescription = contentDescription,
            modifier = modifier.size(size),
            tint = tint,
        )
    }
}

/**
 * Logo resmi VEN (Virtual Exhibition).
 * Otomatis menampilkan logo ungu di Light Mode dan logo putih di Dark Mode
 * melalui konfigurasi resource drawable & drawable-night.
 */
@Composable
fun VexLogo(
    modifier: Modifier = Modifier,
    contentDescription: String? = "VEN Logo",
) {
    Image(
        painter = painterResource(VexIcons.LogoVen),
        contentDescription = contentDescription,
        modifier = modifier,
    )
}
