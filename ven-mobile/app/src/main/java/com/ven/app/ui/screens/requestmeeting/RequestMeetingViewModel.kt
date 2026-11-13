package com.ven.app.ui.screens.requestmeeting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ven.app.util.DateFormatter
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface RequestMeetingEvent {
    data object RequestSuccess : RequestMeetingEvent
}

class RequestMeetingViewModel : ViewModel() {

    private val initialMonth = YearMonth.of(2026, 9)
    private val initialDate = LocalDate.of(2026, 9, 30)

    private val _uiState = MutableStateFlow(
        RequestMeetingUiState(
            visibleMonth = initialMonth,
            selectedDate = initialDate,
            selectedDateText = DateFormatter.formatRequestMeetingDate(initialDate),
            monthTitleText = DateFormatter.formatMonthYear(initialMonth),
            calendarDays = buildMonthGrid(initialMonth, initialDate),
        )
    )
    val uiState: StateFlow<RequestMeetingUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<RequestMeetingEvent>()
    val events: SharedFlow<RequestMeetingEvent> = _events.asSharedFlow()

    fun onDaySelected(day: CalendarDay) {
        if (!day.isCurrentMonth) return
        _uiState.update { current ->
            val newSelectedDate = day.date
            current.copy(
                selectedDate = newSelectedDate,
                selectedDateText = DateFormatter.formatRequestMeetingDate(newSelectedDate),
                calendarDays = buildMonthGrid(current.visibleMonth, newSelectedDate),
            )
        }
    }

    fun onPrevMonth() {
        _uiState.update { current ->
            val prevMonth = current.visibleMonth.minusMonths(1)
            current.copy(
                visibleMonth = prevMonth,
                monthTitleText = DateFormatter.formatMonthYear(prevMonth),
                calendarDays = buildMonthGrid(prevMonth, current.selectedDate),
            )
        }
    }

    fun onNextMonth() {
        _uiState.update { current ->
            val nextMonth = current.visibleMonth.plusMonths(1)
            current.copy(
                visibleMonth = nextMonth,
                monthTitleText = DateFormatter.formatMonthYear(nextMonth),
                calendarDays = buildMonthGrid(nextMonth, current.selectedDate),
            )
        }
    }

    fun onHourChange(hour: String) {
        val sanitized = hour.take(2).filter { it.isDigit() }
        _uiState.update { it.copy(hourText = sanitized) }
    }

    fun onMinuteChange(minute: String) {
        val sanitized = minute.take(2).filter { it.isDigit() }
        _uiState.update { it.copy(minuteText = sanitized) }
    }

    fun onPeriodChange(isAm: Boolean) {
        _uiState.update { it.copy(isAm = isAm) }
    }

    fun onMessageChange(message: String) {
        _uiState.update { it.copy(message = message) }
    }

    fun sendRequest() {
        val current = _uiState.value
        if (current.hourText.isBlank() || current.minuteText.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Waktu pertemuan harus diisi") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, errorMessage = null) }
            kotlinx.coroutines.delay(800)
            _uiState.update { it.copy(isSending = false, isSuccess = true) }
            _events.emit(RequestMeetingEvent.RequestSuccess)
        }
    }
}
