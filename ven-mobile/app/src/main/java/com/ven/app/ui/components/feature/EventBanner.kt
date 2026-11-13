package com.ven.app.ui.components.feature

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ven.app.ui.components.ui.rememberAsyncImageBitmap
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexRadius
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

@Composable
fun EventBanner(
    title: String,
    dateText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    backgroundColor: Color = VexTheme.colors.surfaceInput,
) {
    val bitmap = rememberAsyncImageBitmap(imageUrl)

    Box(
        modifier = modifier
            .width(240.dp)
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(VexRadius.md))
            .background(backgroundColor)
            .clickable(onClick = onClick),
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(VexSpace.s3),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Bottom,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.85f),
                        offset = Offset(0f, 1f),
                        blurRadius = 3f,
                    ),
                ),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = dateText,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview(name = "EventBanner - Dark Mode", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun EventBannerDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        EventBanner(
            title = "Open Source Competition",
            dateText = "24th Sep 2026",
            onClick = {},
        )
    }
}

@Preview(name = "EventBanner - Light Mode", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun EventBannerLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        EventBanner(
            title = "PBL EXPO 2026",
            dateText = "1st Oct 2026",
            onClick = {},
        )
    }
}
