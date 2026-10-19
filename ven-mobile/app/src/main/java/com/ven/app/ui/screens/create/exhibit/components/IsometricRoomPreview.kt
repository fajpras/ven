package com.ven.app.ui.screens.create.exhibit.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexRadius
import com.ven.app.ui.theme.VexTheme

/**
 * Visualisasi Isometrik Ruang Pameran 3D untuk kartu template exhibit.
 * Mereplikasi tampilan wireframe Figma (New Exhibit node 4256-704):
 * - Kanvas gelap bergrid isometrik (garis axis merah/hijau halus)
 * - Ruangan kuboid isometrik beratap terbuka dengan dinding ungu Brand VEN
 * - Lantai dalam putih terang bertekstur
 */
@Composable
fun IsometricRoomPreview(
    modifier: Modifier = Modifier,
    cardHeight: Dp = 176.dp,
    primaryWallColor: Color = Color(0xFF6C3BE8),
    scaleMultiplier: Float = 1.0f,
) {
    val colors = VexTheme.colors
    val isDark = (colors.bg.red * 0.299f + colors.bg.green * 0.587f + colors.bg.blue * 0.114f) < 0.5f
    val shape = RoundedCornerShape(VexRadius.md)

    val canvasBg = if (isDark) Color(0xFF262626) else Color(0xFFEFEFF4)
    val gridColor = if (isDark) Color(0xFF333333) else Color(0xFFDCDCE5)
    val floorColor = if (isDark) Color(0xFFF5F5FA) else Color(0xFFFFFFFF)
    val pitColor = if (isDark) Color(0xFFE4E4ED) else Color(0xFFE2E2EA)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(cardHeight)
            .clip(shape)
            .background(canvasBg)
            .border(width = 1.dp, color = colors.border, shape = shape),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val cx = width / 2f
            val cy = height / 2f

            // 1. Gambar Garis Grid Isometrik Latar Belakang
            val gridStep = 24.dp.toPx()
            val gridSlope = 0.55f

            // Garis miring kiri-ke-kanan & kanan-ke-kiri
            for (i in -8..8) {
                val offset = i * gridStep
                // Grid diagonal 1
                drawLine(
                    color = gridColor,
                    start = Offset(0f, cy + offset - width * 0.5f * gridSlope),
                    end = Offset(width, cy + offset + width * 0.5f * gridSlope),
                    strokeWidth = 1f,
                )
                // Grid diagonal 2
                drawLine(
                    color = gridColor,
                    start = Offset(0f, cy + offset + width * 0.5f * gridSlope),
                    end = Offset(width, cy + offset - width * 0.5f * gridSlope),
                    strokeWidth = 1f,
                )
            }

            // Garis Sumbu Axis Halus (Red/Green 3D axis)
            drawLine(
                color = Color(0x33FF4D4D),
                start = Offset(cx, cy),
                end = Offset(width * 0.9f, cy - width * 0.4f * gridSlope),
                strokeWidth = 1.5f,
            )
            drawLine(
                color = Color(0x334DFF4D),
                start = Offset(cx, cy),
                end = Offset(width * 0.1f, cy - width * 0.4f * gridSlope),
                strokeWidth = 1.5f,
            )

            // 2. Geometri Ruangan Isometrik
            val baseScale = (width * 0.28f * scaleMultiplier).coerceAtMost(width * 0.36f)
            val rw = baseScale
            val rh = baseScale * 0.56f
            val wallHeight = baseScale * 0.42f
            val yOffset = -wallHeight * 0.15f

            // Titik Inti Berlian Luar Bagian Atas
            val tOut = Offset(cx, cy - rh + yOffset)
            val rOut = Offset(cx + rw, cy + yOffset)
            val bOut = Offset(cx, cy + rh + yOffset)
            val lOut = Offset(cx - rw, cy + yOffset)

            // Titik Inti Berlian Luar Bagian Bawah
            val lOutBot = Offset(lOut.x, lOut.y + wallHeight)
            val bOutBot = Offset(bOut.x, bOut.y + wallHeight)
            val rOutBot = Offset(rOut.x, rOut.y + wallHeight)

            // Dinding Luar Kiri (Front-Left Wall)
            val wallLeftColor = primaryWallColor
            val pathWallLeft = Path().apply {
                moveTo(lOut.x, lOut.y)
                lineTo(bOut.x, bOut.y)
                lineTo(bOutBot.x, bOutBot.y)
                lineTo(lOutBot.x, lOutBot.y)
                close()
            }
            drawPath(pathWallLeft, color = wallLeftColor)

            // Dinding Luar Kanan (Front-Right Wall - shading sedikit lebih gelap)
            val wallRightColor = Color(
                red = (primaryWallColor.red * 0.85f).coerceIn(0f, 1f),
                green = (primaryWallColor.green * 0.85f).coerceIn(0f, 1f),
                blue = (primaryWallColor.blue * 0.85f).coerceIn(0f, 1f),
            )
            val pathWallRight = Path().apply {
                moveTo(bOut.x, bOut.y)
                lineTo(rOut.x, rOut.y)
                lineTo(rOutBot.x, rOutBot.y)
                lineTo(bOutBot.x, bOutBot.y)
                close()
            }
            drawPath(pathWallRight, color = wallRightColor)

            // Ketebalan Dinding (Inner Diamond Ratio)
            val k = 0.80f
            val tIn = Offset(cx, cy - rh * k + yOffset)
            val rIn = Offset(cx + rw * k, cy + yOffset)
            val bIn = Offset(cx, cy + rh * k + yOffset)
            val lIn = Offset(cx - rw * k, cy + yOffset)

            // Kedalaman Lantai Dalam
            val floorDepth = wallHeight * 0.76f
            val tFloor = Offset(tIn.x, tIn.y + floorDepth)
            val rFloor = Offset(rIn.x, rIn.y + floorDepth)
            val bFloor = Offset(bIn.x, bIn.y + floorDepth)
            val lFloor = Offset(lIn.x, lIn.y + floorDepth)

            // Dinding Dalam Belakang-Kiri (Inner Back-Left Wall)
            val innerWallLeftColor = Color(
                red = (primaryWallColor.red * 0.90f).coerceIn(0f, 1f),
                green = (primaryWallColor.green * 0.90f).coerceIn(0f, 1f),
                blue = (primaryWallColor.blue * 0.90f).coerceIn(0f, 1f),
            )
            val pathInnerWallLeft = Path().apply {
                moveTo(tIn.x, tIn.y)
                lineTo(lIn.x, lIn.y)
                lineTo(lFloor.x, lFloor.y)
                lineTo(tFloor.x, tFloor.y)
                close()
            }
            drawPath(pathInnerWallLeft, color = innerWallLeftColor)

            // Dinding Dalam Belakang-Kanan (Inner Back-Right Wall - shade)
            val innerWallRightColor = Color(
                red = (primaryWallColor.red * 0.72f).coerceIn(0f, 1f),
                green = (primaryWallColor.green * 0.72f).coerceIn(0f, 1f),
                blue = (primaryWallColor.blue * 0.72f).coerceIn(0f, 1f),
            )
            val pathInnerWallRight = Path().apply {
                moveTo(tIn.x, tIn.y)
                lineTo(rIn.x, rIn.y)
                lineTo(rFloor.x, rFloor.y)
                lineTo(tFloor.x, tFloor.y)
                close()
            }
            drawPath(pathInnerWallRight, color = innerWallRightColor)

            // Lantai Dalam (Floor Plane - Putih Bersih)
            val pathFloor = Path().apply {
                moveTo(tFloor.x, tFloor.y)
                lineTo(rFloor.x, rFloor.y)
                lineTo(bFloor.x, bFloor.y)
                lineTo(lFloor.x, lFloor.y)
                close()
            }
            drawPath(pathFloor, color = floorColor)

            // Aksen Lubang/Panggung Tengah Lantai
            val kInner = 0.50f
            val tPit = Offset(cx, tFloor.y + (bFloor.y - tFloor.y) * (1f - kInner) * 0.5f)
            val rPit = Offset(cx + rw * k * kInner, cy + yOffset + floorDepth)
            val bPit = Offset(cx, bFloor.y - (bFloor.y - tFloor.y) * (1f - kInner) * 0.5f)
            val lPit = Offset(cx - rw * k * kInner, cy + yOffset + floorDepth)
            val pathPit = Path().apply {
                moveTo(tPit.x, tPit.y)
                lineTo(rPit.x, rPit.y)
                lineTo(bPit.x, bPit.y)
                lineTo(lPit.x, lPit.y)
                close()
            }
            drawPath(pathPit, color = pitColor)

            // Bagian Atas Tepi Dinding (Top Wall Rims)
            val rimColor = Color(
                red = (primaryWallColor.red * 1.15f).coerceIn(0f, 1f),
                green = (primaryWallColor.green * 1.15f).coerceIn(0f, 1f),
                blue = (primaryWallColor.blue * 1.15f).coerceIn(0f, 1f),
            )

            // Rim Depan-Kiri
            val pathRimFrontLeft = Path().apply {
                moveTo(lOut.x, lOut.y)
                lineTo(bOut.x, bOut.y)
                lineTo(bIn.x, bIn.y)
                lineTo(lIn.x, lIn.y)
                close()
            }
            drawPath(pathRimFrontLeft, color = rimColor)

            // Rim Depan-Kanan
            val pathRimFrontRight = Path().apply {
                moveTo(bOut.x, bOut.y)
                lineTo(rOut.x, rOut.y)
                lineTo(rIn.x, rIn.y)
                lineTo(bIn.x, bIn.y)
                close()
            }
            drawPath(pathRimFrontRight, color = primaryWallColor)

            // Rim Belakang-Kanan
            val pathRimBackRight = Path().apply {
                moveTo(rOut.x, rOut.y)
                lineTo(tOut.x, tOut.y)
                lineTo(tIn.x, tIn.y)
                lineTo(rIn.x, rIn.y)
                close()
            }
            drawPath(pathRimBackRight, color = rimColor)

            // Rim Belakang-Kiri
            val pathRimBackLeft = Path().apply {
                moveTo(tOut.x, tOut.y)
                lineTo(lOut.x, lOut.y)
                lineTo(lIn.x, lIn.y)
                lineTo(tIn.x, tIn.y)
                close()
            }
            drawPath(pathRimBackLeft, color = rimColor)

            // Garis Garis Bevel Halus
            val highlightColor = Color(0x44FFFFFF)
            drawLine(highlightColor, lOut, bOut, strokeWidth = 1.2f)
            drawLine(highlightColor, bOut, rOut, strokeWidth = 1.2f)
            drawLine(highlightColor, bOut, bOutBot, strokeWidth = 1.2f)
            drawLine(highlightColor, lOutBot, bOutBot, strokeWidth = 1.2f)
            drawLine(highlightColor, bOutBot, rOutBot, strokeWidth = 1.2f)
        }
    }
}

@Preview(name = "IsometricRoomPreview - Dark", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun IsometricRoomPreviewDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        IsometricRoomPreview()
    }
}

@Preview(name = "IsometricRoomPreview - Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun IsometricRoomPreviewLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        IsometricRoomPreview()
    }
}
