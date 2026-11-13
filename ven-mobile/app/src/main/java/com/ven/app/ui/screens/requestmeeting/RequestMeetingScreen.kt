package com.ven.app.ui.screens.requestmeeting

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ven.app.R
import com.ven.app.ui.components.layout.VexBottomActionBar
import com.ven.app.ui.components.layout.VexTopBar
import com.ven.app.ui.components.ui.VexButton
import com.ven.app.ui.components.ui.VexButtonVariant
import com.ven.app.ui.icons.VexIcon
import com.ven.app.ui.icons.VexIcons
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexRadius
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme
import com.ven.app.util.DateFormatter
import java.time.LocalDate
import java.time.YearMonth

/**
 * Halaman Pengajuan Pertemuan (Request Meeting Screen).
 *
 * Sesuai spesifikasi DESIGN.md §8.6.1:
 * - TopBar SubPage: [VexTopBar.Back] dengan judul "Request meeting" dan divider 1dp.
 * - Konten scrollable (LazyColumn):
 *   1. Seksi Info & Banner pameran (16:9, radius lg 16dp).
 *   2. Seksi Chose Date: Grid kalender 7 kolom interaktif dengan tanggal aktif lingkaran BrandPurple.
 *   3. Seksi Chose Time: Segmented time picker (HH : MM + AM/PM toggle) interaktif.
 *   4. Seksi Pesan: Input box "Message..." multiline berbingkai.
 * - Footer Tetap ([VexBottomActionBar]):
 *   - Tombol "Send Request" FIXED di bawah seperti BottomNav (tidak ikut ter-scroll di dalam konten).
 *   - Memiliki insets navigasi & keyboard (`imePadding`).
 */
@Composable
fun RequestMeetingScreen(
    state: RequestMeetingUiState,
    onBackClick: () -> Unit,
    onDaySelected: (CalendarDay) -> Unit,
    onPrevMonth: () -> Unit = {},
    onNextMonth: () -> Unit = {},
    onHourChange: (String) -> Unit,
    onMinuteChange: (String) -> Unit,
    onPeriodChange: (Boolean) -> Unit,
    onMessageChange: (String) -> Unit,
    onSendRequestClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.bg,
        topBar = {
            VexTopBar.Back(
                title = stringResource(R.string.request_meeting_title),
                onBackClick = onBackClick,
            )
        },
        bottomBar = {
            VexBottomActionBar {
                VexButton(
                    text = stringResource(R.string.request_meeting_send_request),
                    onClick = onSendRequestClick,
                    variant = VexButtonVariant.Primary,
                    loading = state.isSending,
                    enabled = state.hourText.isNotBlank() && state.minuteText.isNotBlank(),
                )
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = VexSpace.s6),
        ) {
            // ---------------------------------------------------------
            // Seksi 1: Judul & Banner Pameran
            // ---------------------------------------------------------
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = VexSpace.s6),
                ) {
                    Spacer(modifier = Modifier.height(VexSpace.s5))

                    Text(
                        text = state.pameranTitle,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp,
                        ),
                        color = colors.text,
                        modifier = Modifier.padding(bottom = VexSpace.s4),
                    )

                    // Banner Poster Pameran (16:9, radius VexRadius.lg)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(VexRadius.lg))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF1E2A4A),
                                        Color(0xFF0F5A8E),
                                        Color(0xFF07A8A3),
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(VexSpace.s4),
                        ) {
                            Text(
                                text = "PBL EXPO 2026",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                ),
                                color = Color.White,
                            )
                            Spacer(modifier = Modifier.height(VexSpace.s1))
                            Text(
                                text = "INNOVATE • COLLABORATE • ELEVATE",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = 1.sp,
                                ),
                                color = Color.White.copy(alpha = 0.9f),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(VexSpace.s5))
                }

                HorizontalDivider(
                    thickness = 1.dp,
                    color = colors.border,
                )
            }

            // ---------------------------------------------------------
            // Seksi 2: Pemilihan Tanggal ("Chose Date")
            // ---------------------------------------------------------
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = VexSpace.s6),
                ) {
                    Spacer(modifier = Modifier.height(VexSpace.s5))

                    Text(
                        text = stringResource(R.string.request_meeting_chose_date),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                        ),
                        color = colors.text,
                    )

                    Spacer(modifier = Modifier.height(VexSpace.s3))

                    // Baris Navigasi Bulan & Tahun (mis. "September 2026" + Chevron Prev & Next)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = state.monthTitleText,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                            ),
                            color = colors.text,
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(
                                onClick = onPrevMonth,
                                modifier = Modifier.size(36.dp),
                            ) {
                                VexIcon(
                                    id = VexIcons.ChevronLeft,
                                    contentDescription = stringResource(R.string.request_meeting_prev_month),
                                    tint = colors.text,
                                    size = 20.dp,
                                )
                            }

                            IconButton(
                                onClick = onNextMonth,
                                modifier = Modifier.size(36.dp),
                            ) {
                                VexIcon(
                                    id = VexIcons.ChevronRight,
                                    contentDescription = stringResource(R.string.request_meeting_next_month),
                                    tint = colors.text,
                                    size = 20.dp,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(VexSpace.s2))

                    Text(
                        text = state.selectedDateText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                        ),
                        color = colors.textMuted,
                    )

                    Spacer(modifier = Modifier.height(VexSpace.s4))

                    // Header Hari: Mon, Tue, Wed, Thu, Fri, Sat, Sun
                    val dayHeaders = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        dayHeaders.forEach { dayName ->
                            Text(
                                text = dayName,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Normal,
                                ),
                                color = colors.textMuted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(VexSpace.s3))

                    // Grid Tanggal 7 Kolom
                    val rows = state.calendarDays.chunked(7)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(VexSpace.s3),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        rows.forEach { week ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                week.forEach { day ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(vertical = 2.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        val isSelected = day.isSelected && day.isCurrentMonth
                                        val textColor = when {
                                            isSelected -> colors.onPrimary
                                            !day.isCurrentMonth -> colors.textDisabled
                                            else -> colors.text
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isSelected) colors.primary else Color.Transparent
                                                )
                                                .clickable(
                                                    enabled = day.isCurrentMonth,
                                                    role = Role.Button,
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = ripple(bounded = true),
                                                    onClick = { onDaySelected(day) },
                                                ),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                text = day.dayNumber.toString(),
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                ),
                                                color = textColor,
                                                textAlign = TextAlign.Center,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(VexSpace.s5))
                }

                HorizontalDivider(
                    thickness = 1.dp,
                    color = colors.border,
                )
            }

            // ---------------------------------------------------------
            // Seksi 3: Pemilihan Jam ("Chose Time" - Segmented Picker)
            // ---------------------------------------------------------
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = VexSpace.s6),
                ) {
                    Spacer(modifier = Modifier.height(VexSpace.s5))

                    Text(
                        text = stringResource(R.string.request_meeting_chose_time),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                        ),
                        color = colors.text,
                    )

                    Spacer(modifier = Modifier.height(VexSpace.s4))

                    // Segmented Time Picker Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Kotak Jam (Hour)
                        TimeDigitBox(
                            value = state.hourText,
                            onValueChange = onHourChange,
                            modifier = Modifier.weight(1f),
                        )

                        // Tanda Titik Dua
                        Text(
                            text = ":",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = colors.text,
                            textAlign = TextAlign.Center,
                        )

                        // Kotak Menit (Minute)
                        TimeDigitBox(
                            value = state.minuteText,
                            onValueChange = onMinuteChange,
                            modifier = Modifier.weight(1f),
                        )

                        // Tombol AM
                        PeriodToggleBox(
                            label = stringResource(R.string.request_meeting_am),
                            isSelected = state.isAm,
                            onClick = { onPeriodChange(true) },
                            modifier = Modifier.weight(1f),
                        )

                        // Tombol PM
                        PeriodToggleBox(
                            label = stringResource(R.string.request_meeting_pm),
                            isSelected = !state.isAm,
                            onClick = { onPeriodChange(false) },
                            modifier = Modifier.weight(1f),
                        )
                    }

                    Spacer(modifier = Modifier.height(VexSpace.s5))
                }

                HorizontalDivider(
                    thickness = 1.dp,
                    color = colors.border,
                )
            }

            // ---------------------------------------------------------
            // Seksi 4: Catatan Pesan ("Message...")
            // ---------------------------------------------------------
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = VexSpace.s6),
                ) {
                    Spacer(modifier = Modifier.height(VexSpace.s5))

                    val shape = RoundedCornerShape(VexRadius.md)
                    BasicTextField(
                        value = state.message,
                        onValueChange = onMessageChange,
                        cursorBrush = SolidColor(colors.primary),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = colors.text,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .border(1.dp, colors.borderInput, shape)
                            .clip(shape)
                            .background(colors.surfaceInput)
                            .padding(VexSpace.s4),
                        decorationBox = { innerTextField ->
                            Box(modifier = Modifier.fillMaxSize()) {
                                if (state.message.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.request_meeting_message_hint),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = colors.textMuted,
                                    )
                                }
                                innerTextField()
                            }
                        },
                    )

                    Spacer(modifier = Modifier.height(VexSpace.s5))
                }
            }
        }
    }
}

/**
 * Komponen kotak digit untuk jam / menit (Segmented Time Box).
 */
@Composable
private fun TimeDigitBox(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors
    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .height(72.dp)
            .clip(shape)
            .background(colors.primary)
            .padding(horizontal = VexSpace.s2),
        contentAlignment = Alignment.Center,
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            cursorBrush = SolidColor(colors.onPrimary),
            textStyle = MaterialTheme.typography.titleLarge.copy(
                fontSize = 28.sp,
                fontWeight = FontWeight.Medium,
                color = colors.onPrimary,
                textAlign = TextAlign.Center,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * Komponen kotak toggle untuk AM / PM.
 */
@Composable
private fun PeriodToggleBox(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors
    val shape = RoundedCornerShape(12.dp)

    val bgColor = if (isSelected) colors.primary else colors.surfaceInput
    val textColor = if (isSelected) colors.onPrimary else colors.textMuted

    Box(
        modifier = modifier
            .height(72.dp)
            .clip(shape)
            .background(bgColor)
            .clickable(
                role = Role.RadioButton,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 20.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            ),
            color = textColor,
            textAlign = TextAlign.Center,
        )
    }
}

// ---------------------------------------------------------
// Previews untuk Dark Mode dan Light Mode
// ---------------------------------------------------------

@Preview(name = "RequestMeeting - Dark Mode", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun RequestMeetingDarkPreview() {
    val month = YearMonth.of(2026, 9)
    val date = LocalDate.of(2026, 9, 30)
    VexTheme(mode = ThemeMode.Dark) {
        RequestMeetingScreen(
            state = RequestMeetingUiState(
                visibleMonth = month,
                selectedDate = date,
                selectedDateText = DateFormatter.formatRequestMeetingDate(date),
                monthTitleText = DateFormatter.formatMonthYear(month),
                calendarDays = buildMonthGrid(month, date),
                hourText = "02",
                minuteText = "30",
                isAm = false,
            ),
            onBackClick = {},
            onDaySelected = {},
            onPrevMonth = {},
            onNextMonth = {},
            onHourChange = {},
            onMinuteChange = {},
            onPeriodChange = {},
            onMessageChange = {},
            onSendRequestClick = {},
        )
    }
}

@Preview(name = "RequestMeeting - Light Mode", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun RequestMeetingLightPreview() {
    val month = YearMonth.of(2026, 9)
    val date = LocalDate.of(2026, 9, 30)
    VexTheme(mode = ThemeMode.Light) {
        RequestMeetingScreen(
            state = RequestMeetingUiState(
                visibleMonth = month,
                selectedDate = date,
                selectedDateText = DateFormatter.formatRequestMeetingDate(date),
                monthTitleText = DateFormatter.formatMonthYear(month),
                calendarDays = buildMonthGrid(month, date),
                hourText = "02",
                minuteText = "30",
                isAm = false,
            ),
            onBackClick = {},
            onDaySelected = {},
            onPrevMonth = {},
            onNextMonth = {},
            onHourChange = {},
            onMinuteChange = {},
            onPeriodChange = {},
            onMessageChange = {},
            onSendRequestClick = {},
        )
    }
}
