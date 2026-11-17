package app.sadhan.ui.tools

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.sadhan.core.age
import app.sadhan.core.daysBetween
import app.sadhan.ui.Choice
import app.sadhan.ui.ErrorText
import app.sadhan.ui.ResultCard
import app.sadhan.ui.ToolColumn
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgeDatesScreen() {
    var mode by remember { mutableStateOf("Age") }
    var start by remember { mutableStateOf(LocalDate.now().minusYears(25)) }
    var end by remember { mutableStateOf(LocalDate.now()) }
    var editing by remember { mutableStateOf<String?>(null) }
    ToolColumn {
        Choice("Mode", mode, listOf("Age", "Days between")) { mode = it }
        OutlinedButton(onClick = { editing = "start" }) { Text("${if (mode == "Age") "Birth date" else "Start date"}: $start") }
        OutlinedButton(onClick = { editing = "end" }) { Text("End date: $end") }
        if (mode == "Age") {
            if (start.isAfter(end)) ErrorText("Birth date must not be after end date")
            else {
                val period = age(start, end)
                ResultCard("Exact age", "${period.years} years, ${period.months} months, ${period.days} days")
            }
        } else {
            ResultCard("Days between", daysBetween(start, end).toString())
            Text("Signed calendar days, excluding the end date.")
        }
    }
    editing?.let { field ->
        val initial = if (field == "start") start else end
        val state = rememberDatePickerState(initialSelectedDateMillis = initial.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { editing = null },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        val date = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                        if (field == "start") start = date else end = date
                    }
                    editing = null
                }, enabled = state.selectedDateMillis != null) { Text("Select") }
            },
            dismissButton = { TextButton(onClick = { editing = null }) { Text("Cancel") } },
        ) { DatePicker(state = state) }
    }
}
