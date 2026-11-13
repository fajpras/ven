package com.ven.app.ui.screens.ai.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ven.app.ui.screens.ai.AiChatDummyData
import com.ven.app.ui.theme.ChatBodyStyle
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

/**
 * Komponen pesan balasan dari asisten AI (DESIGN.md §8.12.3).
 * Sesuai wireframe `Wireframe - AI Chat-1.png`:
 * - Desain netral: Tanpa bubble, tanpa kartu atau background pembungkus.
 * - Teks langsung dicetak di atas latar kanvas (#1F1F1F).
 * - Lebar penuh dengan padding horizontal 24dp (VexSpace.s6).
 * - Perataan teks: TextAlign.Justify.
 * - Mendukung format teks daftar bernomor yang rapi.
 */
@Composable
fun ChatMessageAi(
    text: String,
    modifier: Modifier = Modifier,
    onItemClick: ((String) -> Unit)? = null,
) {
    val colors = VexTheme.colors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = VexSpace.s6),
    ) {
        Text(
            text = text,
            style = ChatBodyStyle,
            color = colors.text,
            textAlign = TextAlign.Justify,
        )
    }
}

/**
 * Indikator pemikiran / pengetikan AI (3 titik berdenyut halus).
 */
@Composable
fun AiTypingIndicator(
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors
    val transition = rememberInfiniteTransition(label = "AiTypingAnimation")

    val dot1Alpha by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "dot1Alpha",
    )

    val dot2Alpha by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "dot2Alpha",
    )

    val dot3Alpha by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "dot3Alpha",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = VexSpace.s6, vertical = VexSpace.s3),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .alpha(dot1Alpha)
                .clip(CircleShape)
                .background(colors.primaryText),
        )
        Box(
            modifier = Modifier
                .size(8.dp)
                .alpha(dot2Alpha)
                .clip(CircleShape)
                .background(colors.primaryText),
        )
        Box(
            modifier = Modifier
                .size(8.dp)
                .alpha(dot3Alpha)
                .clip(CircleShape)
                .background(colors.primaryText),
        )
    }
}

@Preview(name = "ChatMessageAi - Dark")
@Composable
private fun ChatMessageAiDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        ChatMessageAi(
            text = AiChatDummyData.INITIAL_AI_RESPONSE,
        )
    }
}

@Preview(name = "AiTypingIndicator - Dark")
@Composable
private fun AiTypingIndicatorDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        AiTypingIndicator()
    }
}
