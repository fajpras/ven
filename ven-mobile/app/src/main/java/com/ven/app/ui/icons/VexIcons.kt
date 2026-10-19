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
data class VexIconPair(@param:DrawableRes val outline: Int, @param:DrawableRes val filled: Int)

object VexIcons {
    // Navigation icons (outline + filled pairs)
    object Nav {
        val Home = VexIconPair(R.drawable.ic_nav_home_outline, R.drawable.ic_nav_home_filled)
        val Schedule = VexIconPair(R.drawable.ic_nav_schedule_outline, R.drawable.ic_nav_schedule_filled)
        val Ai = VexIconPair(R.drawable.ic_nav_ai_outline, R.drawable.ic_nav_ai_filled)
        val Explore = VexIconPair(R.drawable.ic_nav_explore_outline, R.drawable.ic_nav_explore_filled)
    }

    // Tab icons (Profile tabs)
    object Tab {
        val Posts = VexIconPair(R.drawable.ic_tab_posts_outline, R.drawable.ic_tab_posts_filled)
        val Exhibits = VexIconPair(R.drawable.ic_tab_exhibits_outline, R.drawable.ic_tab_exhibits_filled)
    }

    // Action icons
    @DrawableRes val Add = R.drawable.ic_action_add
    @DrawableRes val Menu = R.drawable.ic_action_menu
    @DrawableRes val Back = R.drawable.ic_action_back
    @DrawableRes val Edit = R.drawable.ic_action_edit
    @DrawableRes val Check = R.drawable.ic_action_check
    @DrawableRes val Close = R.drawable.ic_action_close
    @DrawableRes val History = R.drawable.ic_action_history
    @DrawableRes val ChevronLeft = R.drawable.ic_action_chevron_left
    @DrawableRes val ChevronRight = R.drawable.ic_action_chevron_right
    @DrawableRes val ChevronDown = R.drawable.ic_action_chevron_down
    @DrawableRes val Search = R.drawable.ic_nav_explore_outline
    @DrawableRes val Filter = R.drawable.ic_action_filter
    @DrawableRes val ScheduleAction = R.drawable.ic_nav_schedule_outline
    @DrawableRes val Play = R.drawable.ic_action_play
    @DrawableRes val NewChat = R.drawable.ic_action_new_chat
    @DrawableRes val Send = R.drawable.ic_action_send
    @DrawableRes val Visibility = R.drawable.eyes_on
    @DrawableRes val VisibilityOff = R.drawable.eyes_off
    @DrawableRes val EyesOn = R.drawable.eyes_on
    @DrawableRes val EyesOff = R.drawable.eyes_off

    // Settings icons
    @DrawableRes val SettingsProfile = R.drawable.ic_settings_profile
    @DrawableRes val SettingsEmail = R.drawable.ic_settings_email
    @DrawableRes val SettingsPassword = R.drawable.ic_settings_password
    @DrawableRes val SettingsAccountStatus = R.drawable.ic_settings_account_status
    @DrawableRes val SettingsHistory = R.drawable.ic_settings_history
    @DrawableRes val SettingsTheme = R.drawable.ic_settings_theme
    @DrawableRes val SettingsLanguage = R.drawable.ic_settings_language
    @DrawableRes val SettingsPrivacy = R.drawable.ic_settings_privacy
    @DrawableRes val SettingsHelp = R.drawable.ic_settings_help
    @DrawableRes val SettingsLogout = R.drawable.ic_settings_logout

    // Editor icons
    @DrawableRes val EditorText = 0 // Rendered via typography "Aa"
    @DrawableRes val EditorPanel = R.drawable.ic_panel
    @DrawableRes val EditorFurniture = R.drawable.ic_furniture

    // Status icons
    @DrawableRes val StatusCheckFilled = R.drawable.ic_action_check
    @DrawableRes val StatusCircleOutline = 0

    // Logos
    @DrawableRes val LogoVen = R.drawable.logo_ven
    @DrawableRes val LogoVex = R.drawable.logo_ven // Alias for DESIGN.md compatibility
    @DrawableRes val LogoGoogle = R.drawable.ic_google // Multi-color, NO tint
    @DrawableRes val GoogleColour = R.drawable.ic_google
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
