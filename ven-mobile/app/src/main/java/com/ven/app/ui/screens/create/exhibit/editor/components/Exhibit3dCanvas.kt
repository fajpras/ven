package com.ven.app.ui.screens.create.exhibit.editor.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ven.app.R
import com.ven.app.ui.icons.VexIcon
import com.ven.app.ui.icons.VexIcons
import com.ven.app.ui.screens.create.exhibit.editor.ExhibitEditorSheet
import com.ven.app.ui.screens.create.exhibit.editor.ExhibitPanelSlot
import com.ven.app.ui.theme.VexRadius
import com.ven.app.ui.theme.VexSize
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

/**
 * Komponen Kanvas 3D Interaktif untuk Layar Editor Exhibit (§8.14.2).
 * - Kanvas 3D memenuhi bagian atas, sudut bawah melengkung 28dp ([VexRadius.xl]).
 * - Latar abu gelap bergrid isometrik 3D dan rendering ruang pameran dinamis.
 * - Tombol Panah Kembali di kiri-atas.
 * - Kolom 4 FloatingToolButton di kanan-atas:
 *   1. Aa (Details)
 *   2. Panel (Karya)
 *   3. Furnitur (Dekorasi)
 *   4. Warna (Palet Warna Dinding)
 * - Indikator Penghitung Panel di kiri-bawah canvas ("X/N panels").
 */
@Composable
fun Exhibit3dCanvas(
    wallColor: Color,
    panelSlots: List<ExhibitPanelSlot>,
    totalPanels: Int,
    assignedPanels: Int,
    onBackClick: () -> Unit,
    onToolClick: (ExhibitEditorSheet) -> Unit,
    modifier: Modifier = Modifier,
    canvasHeight: Dp = 420.dp,
) {
    val colors = VexTheme.colors
    val isDark = (colors.bg.red * 0.299f + colors.bg.green * 0.587f + colors.bg.blue * 0.114f) < 0.5f
    val canvasShape = RoundedCornerShape(
        topStart = 0.dp,
        topEnd = 0.dp,
        bottomStart = VexRadius.xl,
        bottomEnd = VexRadius.xl,
    )

    val canvasBg = if (isDark) Color(0xFF222226) else Color(0xFFEFEFF4)
    val gridColor = if (isDark) Color(0xFF2E2E33) else Color(0xFFDCDCE5)
    val floorColor = if (isDark) Color(0xFFF7F7FC) else Color(0xFFFFFFFF)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(canvasHeight)
            .clip(canvasShape)
            .background(canvasBg),
    ) {
        // 1. Rendering Kanvas Isometrik 3D
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val cx = width / 2f
            val cy = height / 2f + 16.dp.toPx()

            // Grid Isometrik Dasar
            val gridStep = 28.dp.toPx()
            val gridSlope = 0.55f

            for (i in -10..10) {
                val offset = i * gridStep
                drawLine(
                    color = gridColor,
                    start = Offset(0f, cy + offset - width * 0.5f * gridSlope),
                    end = Offset(width, cy + offset + width * 0.5f * gridSlope),
                    strokeWidth = 1f,
                )
                drawLine(
                    color = gridColor,
                    start = Offset(0f, cy + offset + width * 0.5f * gridSlope),
                    end = Offset(width, cy + offset - width * 0.5f * gridSlope),
                    strokeWidth = 1f,
                )
            }

            // Sumbu Axis Halus
            drawLine(
                color = Color(0x33FF4D4D),
                start = Offset(cx, cy),
                end = Offset(width * 0.95f, cy - width * 0.45f * gridSlope),
                strokeWidth = 1.5f,
            )
            drawLine(
                color = Color(0x334DFF4D),
                start = Offset(cx, cy),
                end = Offset(width * 0.05f, cy - width * 0.45f * gridSlope),
                strokeWidth = 1.5f,
            )

            // Geometri Ruangan Isometrik Utama
            val rw = width * 0.36f
            val rh = rw * 0.56f
            val wallHeight = rw * 0.44f
            val yOffset = -wallHeight * 0.15f

            val tOut = Offset(cx, cy - rh + yOffset)
            val rOut = Offset(cx + rw, cy + yOffset)
            val bOut = Offset(cx, cy + rh + yOffset)
            val lOut = Offset(cx - rw, cy + yOffset)

            val lOutBot = Offset(lOut.x, lOut.y + wallHeight)
            val bOutBot = Offset(bOut.x, bOut.y + wallHeight)
            val rOutBot = Offset(rOut.x, rOut.y + wallHeight)

            // Dinding Luar Kiri
            val pathWallLeft = Path().apply {
                moveTo(lOut.x, lOut.y)
                lineTo(bOut.x, bOut.y)
                lineTo(bOutBot.x, bOutBot.y)
                lineTo(lOutBot.x, lOutBot.y)
                close()
            }
            drawPath(pathWallLeft, color = wallColor)

            // Dinding Luar Kanan (Shading)
            val wallRightColor = Color(
                red = (wallColor.red * 0.82f).coerceIn(0f, 1f),
                green = (wallColor.green * 0.82f).coerceIn(0f, 1f),
                blue = (wallColor.blue * 0.82f).coerceIn(0f, 1f),
            )
            val pathWallRight = Path().apply {
                moveTo(bOut.x, bOut.y)
                lineTo(rOut.x, rOut.y)
                lineTo(rOutBot.x, rOutBot.y)
                lineTo(bOutBot.x, bOutBot.y)
                close()
            }
            drawPath(pathWallRight, color = wallRightColor)

            // Dinding Dalam & Lantai
            val k = 0.82f
            val tIn = Offset(cx, cy - rh * k + yOffset)
            val rIn = Offset(cx + rw * k, cy + yOffset)
            val bIn = Offset(cx, cy + rh * k + yOffset)
            val lIn = Offset(cx - rw * k, cy + yOffset)

            val floorDepth = wallHeight * 0.78f
            val tFloor = Offset(tIn.x, tIn.y + floorDepth)
            val rFloor = Offset(rIn.x, rIn.y + floorDepth)
            val bFloor = Offset(bIn.x, bIn.y + floorDepth)
            val lFloor = Offset(lIn.x, lIn.y + floorDepth)

            // Dinding Dalam Belakang-Kiri
            val innerWallLeftColor = Color(
                red = (wallColor.red * 0.92f).coerceIn(0f, 1f),
                green = (wallColor.green * 0.92f).coerceIn(0f, 1f),
                blue = (wallColor.blue * 0.92f).coerceIn(0f, 1f),
            )
            val pathInnerWallLeft = Path().apply {
                moveTo(tIn.x, tIn.y)
                lineTo(lIn.x, lIn.y)
                lineTo(lFloor.x, lFloor.y)
                lineTo(tFloor.x, tFloor.y)
                close()
            }
            drawPath(pathInnerWallLeft, color = innerWallLeftColor)

            // Dinding Dalam Belakang-Kanan
            val innerWallRightColor = Color(
                red = (wallColor.red * 0.72f).coerceIn(0f, 1f),
                green = (wallColor.green * 0.72f).coerceIn(0f, 1f),
                blue = (wallColor.blue * 0.72f).coerceIn(0f, 1f),
            )
            val pathInnerWallRight = Path().apply {
                moveTo(tIn.x, tIn.y)
                lineTo(rIn.x, rIn.y)
                lineTo(rFloor.x, rFloor.y)
                lineTo(tFloor.x, tFloor.y)
                close()
            }
            drawPath(pathInnerWallRight, color = innerWallRightColor)

            // Lantai Dalam
            val pathFloor = Path().apply {
                moveTo(tFloor.x, tFloor.y)
                lineTo(rFloor.x, rFloor.y)
                lineTo(bFloor.x, bFloor.y)
                lineTo(lFloor.x, lFloor.y)
                close()
            }
            drawPath(pathFloor, color = floorColor)

            // Rims Dinding Atas
            val rimColor = Color(
                red = (wallColor.red * 1.15f).coerceIn(0f, 1f),
                green = (wallColor.green * 1.15f).coerceIn(0f, 1f),
                blue = (wallColor.blue * 1.15f).coerceIn(0f, 1f),
            )
            val pathRimLeft = Path().apply {
                moveTo(lOut.x, lOut.y)
                lineTo(bOut.x, bOut.y)
                lineTo(bIn.x, bIn.y)
                lineTo(lIn.x, lIn.y)
                close()
            }
            drawPath(pathRimLeft, color = rimColor)

            val pathRimRight = Path().apply {
                moveTo(bOut.x, bOut.y)
                lineTo(rOut.x, rOut.y)
                lineTo(rIn.x, rIn.y)
                lineTo(bIn.x, bIn.y)
                close()
            }
            drawPath(pathRimRight, color = wallColor)

            // Gambar Representasi Panel Karya di Dinding Belakang jika ada yang terisi
            if (assignedPanels > 0) {
                // Panel 1 di dinding dalam kiri
                val p1x = (tIn.x + lIn.x) / 2f + rw * 0.08f
                val p1y = (tIn.y + lIn.y) / 2f + floorDepth * 0.4f
                drawRect(
                    color = Color.White,
                    topLeft = Offset(p1x - 14.dp.toPx(), p1y - 18.dp.toPx()),
                    size = androidx.compose.ui.geometry.Size(28.dp.toPx(), 36.dp.toPx()),
                )
                drawRect(
                    color = Color(0xFF333333),
                    topLeft = Offset(p1x - 12.dp.toPx(), p1y - 16.dp.toPx()),
                    size = androidx.compose.ui.geometry.Size(24.dp.toPx(), 32.dp.toPx()),
                )

                // Panel 2 di dinding dalam kanan
                if (assignedPanels > 1) {
                    val p2x = (tIn.x + rIn.x) / 2f - rw * 0.08f
                    val p2y = (tIn.y + rIn.y) / 2f + floorDepth * 0.4f
                    drawRect(
                        color = Color.White,
                        topLeft = Offset(p2x - 14.dp.toPx(), p2y - 18.dp.toPx()),
                        size = androidx.compose.ui.geometry.Size(28.dp.toPx(), 36.dp.toPx()),
                    )
                    drawRect(
                        color = Color(0xFF4A4A5A),
                        topLeft = Offset(p2x - 12.dp.toPx(), p2y - 16.dp.toPx()),
                        size = androidx.compose.ui.geometry.Size(24.dp.toPx(), 32.dp.toPx()),
                    )
                }
            }
        }

        // 2. Tombol Kembali di Sisi Kiri-Atas (statusBarsPadding)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = VexSpace.s4, top = VexSpace.s2),
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0x66000000) else colors.surface.copy(alpha = 0.90f))
                    .then(
                        if (!isDark) Modifier.border(width = 1.dp, color = colors.border, shape = CircleShape)
                        else Modifier
                    ),
            ) {
                VexIcon(
                    id = VexIcons.Back,
                    contentDescription = stringResource(R.string.create_exhibit_editor_back),
                    size = VexSize.icon,
                    tint = if (isDark) Color.White else colors.text,
                )
            }
        }

        // 3. Kolom 4 FloatingToolButton di Sisi Kanan-Atas (statusBarsPadding)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(end = VexSpace.s4, top = VexSpace.s2),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Tool 1: Aa (Exhibit Details)
            FloatingToolButton(
                onClick = { onToolClick(ExhibitEditorSheet.Details) },
                contentDescription = stringResource(R.string.create_exhibit_tool_details),
            ) {
                Text(
                    text = "Aa",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                    ),
                    color = colors.text,
                )
            }

            // Tool 2: Panel (Atur Karya)
            FloatingToolButton(
                onClick = { onToolClick(ExhibitEditorSheet.Panels) },
                contentDescription = stringResource(R.string.create_exhibit_tool_panels),
            ) {
                VexIcon(
                    id = VexIcons.EditorPanel,
                    contentDescription = null,
                    size = 22.dp,
                    tint = colors.text,
                )
            }

            // Tool 3: Furniture (Dekorasi)
            FloatingToolButton(
                onClick = { onToolClick(ExhibitEditorSheet.Furniture) },
                contentDescription = stringResource(R.string.create_exhibit_tool_furniture),
            ) {
                VexIcon(
                    id = VexIcons.EditorFurniture,
                    contentDescription = null,
                    size = 22.dp,
                    tint = colors.text,
                )
            }

            // Tool 4: Color (Warna Dinding)
            FloatingToolButton(
                onClick = { onToolClick(ExhibitEditorSheet.Color) },
                contentDescription = stringResource(R.string.create_exhibit_tool_color),
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(wallColor)
                        .border(width = 2.dp, color = Color.White, shape = CircleShape),
                )
            }
        }

        // 4. Penghitung Panel di Kiri-Bawah Kanvas ("X/N panels")
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = VexSpace.s4, bottom = VexSpace.s4)
                .clip(RoundedCornerShape(VexRadius.full))
                .background(if (isDark) Color(0x99000000) else colors.surface.copy(alpha = 0.90f))
                .then(
                    if (!isDark) Modifier.border(width = 1.dp, color = colors.border, shape = RoundedCornerShape(VexRadius.full))
                    else Modifier
                )
                .padding(horizontal = VexSpace.s3, vertical = VexSpace.s1),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(VexSpace.s2),
        ) {
            VexIcon(
                id = VexIcons.EditorPanel,
                contentDescription = null,
                size = 16.dp,
                tint = if (isDark) Color.White else colors.text,
            )

            Text(
                text = stringResource(R.string.create_exhibit_panel_count, assignedPanels, totalPanels),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                ),
                color = if (isDark) Color.White else colors.text,
            )
        }
    }
}

/**
 * Komponen Tombol Alat Mengambang (Lingkaran 48dp, latar surface, shadow/elevasi)
 */
@Composable
fun FloatingToolButton(
    onClick: () -> Unit,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = VexTheme.colors

    Box(
        modifier = modifier
            .size(48.dp)
            .shadow(elevation = 6.dp, shape = CircleShape)
            .clip(CircleShape)
            .background(colors.surface)
            .border(width = 1.dp, color = colors.border, shape = CircleShape)
            .clickable(
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
