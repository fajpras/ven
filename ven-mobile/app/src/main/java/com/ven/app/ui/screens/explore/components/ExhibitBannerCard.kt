package com.ven.app.ui.screens.explore.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ven.app.R
import com.ven.app.ui.components.ui.VexSkillChip
import com.ven.app.ui.components.ui.rememberAsyncImageBitmap
import com.ven.app.ui.theme.CategoryColorMap
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexRadius
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

/**
 * Kartu banner pameran landscape untuk tab Exhibits di Explore.
 */
@Composable
fun ExhibitBannerCard(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    imageRes: Int? = null,
    bannerUrl: String? = null,
    category: String? = null,
) {
    val colors = VexTheme.colors
    val shape = RoundedCornerShape(VexRadius.md)
    val bitmap = rememberAsyncImageBitmap(bannerUrl)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(shape)
            .background(colors.surfaceInput)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = colors.text.copy(alpha = 0.2f)),
                role = Role.Button,
                onClick = onClick,
            ),
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else if (imageRes != null) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.75f),
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY,
                    ),
                ),
        )

        if (!category.isNullOrBlank()) {
            val catColor = CategoryColorMap[category] ?: colors.primary
            VexSkillChip(
                text = category,
                categoryColor = catColor,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(VexSpace.s3),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(VexSpace.s4),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Bottom,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
        }
    }
}

@Preview(name = "ExhibitBannerCard - Dark Mode", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun ExhibitBannerCardDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        Box(modifier = Modifier.padding(16.dp)) {
            ExhibitBannerCard(
                title = "Open Source Competition",
                category = "Cybersecurity",
                imageRes = R.drawable.poster_2,
                onClick = {},
            )
        }
    }
}

@Preview(name = "ExhibitBannerCard - Light Mode", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun ExhibitBannerCardLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        Box(modifier = Modifier.padding(16.dp)) {
            ExhibitBannerCard(
                title = "PBL EXPO 2026",
                imageRes = R.drawable.poster_1,
                onClick = {},
            )
        }
    }
}
