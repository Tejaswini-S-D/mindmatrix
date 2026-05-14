package com.kreeda.ankana.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kreeda.ankana.R
import com.kreeda.ankana.ui.theme.*
import com.kreeda.ankana.viewmodel.CalendarViewModel
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(viewModel: CalendarViewModel = viewModel()) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val bookedSlots by viewModel.bookedSlots.collectAsState()
    val selectedSlot by viewModel.selectedSlot.collectAsState()
    val isBooking by viewModel.isBooking.collectAsState()
    val context = LocalContext.current

    // Booking dialog state
    var showBookingDialog by remember { mutableStateOf(false) }
    var teamName by remember { mutableStateOf("") }
    var bookedBy by remember { mutableStateOf("") }
    var sport by remember { mutableStateOf("Cricket") }

    // Collect booking results
    LaunchedEffect(Unit) {
        viewModel.bookingResult.collectLatest { result ->
            result.onSuccess {
                Toast.makeText(context, "✅ Slot booked successfully!", Toast.LENGTH_SHORT).show()
                showBookingDialog = false
                teamName = ""
                bookedBy = ""
            }
            result.onFailure { error ->
                Toast.makeText(context, "❌ ${error.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // ── Header ──────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(PrimaryGreen, GradientMiddle)
                    )
                )
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Column {
                Text(
                    text = "📅 ${stringResource(R.string.ground_booking)}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = stringResource(R.string.booking_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }

        // ── Date Selector (Horizontal scroll of next 7 days) ────────────
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            viewModel.getNextSevenDays().forEach { date ->
                val isSelected = date == selectedDate
                val isToday = date == LocalDate.now()

                Card(
                    modifier = Modifier
                        .width(64.dp)
                        .clickable { viewModel.selectDate(date) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isSelected -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (isSelected) 6.dp else 1.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) Color.White.copy(alpha = 0.8f)
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${date.dayOfMonth}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White
                            else MaterialTheme.colorScheme.onSurface
                        )
                        if (isToday) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) Color.White
                                        else MaterialTheme.colorScheme.primary
                                    )
                            )
                        }
                    }
                }
            }
        }

        // ── Legend ──────────────────────────────────────────────────────
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LegendItem(color = MaterialTheme.colorScheme.surfaceVariant, label = stringResource(R.string.slot_available))
            LegendItem(color = ErrorRed.copy(alpha = 0.3f), label = stringResource(R.string.slot_booked))
            LegendItem(color = MaterialTheme.colorScheme.primary, label = stringResource(R.string.slot_selected))
        }

        // ── Time Slots Grid ─────────────────────────────────────────────
        Spacer(modifier = Modifier.height(12.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(viewModel.timeSlots.size) { index ->
                val slot = viewModel.timeSlots[index]
                val isBooked = bookedSlots.contains(slot)
                val isSelected = selectedSlot == slot

                val bgColor by animateColorAsState(
                    targetValue = when {
                        isBooked -> ErrorRed.copy(alpha = 0.15f)
                        isSelected -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    animationSpec = tween(300),
                    label = "slotColor"
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp)
                        .then(
                            if (isSelected && !isBooked) Modifier.border(
                                2.dp,
                                MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(14.dp)
                            ) else Modifier
                        )
                        .clickable(enabled = !isBooked) {
                            viewModel.selectSlot(slot)
                        },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = bgColor),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (isSelected) 4.dp else 1.dp
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = slot.replace(" - ", "\n"),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 16.sp,
                            color = when {
                                isBooked -> ErrorRed
                                isSelected -> Color.White
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Icon(
                            imageVector = when {
                                isBooked -> Icons.Filled.Lock
                                isSelected -> Icons.Filled.CheckCircle
                                else -> Icons.Filled.Schedule
                            },
                            contentDescription = null,
                            tint = when {
                                isBooked -> ErrorRed
                                isSelected -> Color.White
                                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // ── Book Button ─────────────────────────────────────────────────
        AnimatedVisibility(
            visible = selectedSlot != null,
            enter = fadeIn() + scaleIn(),
        ) {
            Button(
                onClick = { showBookingDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                enabled = !isBooking
            ) {
                if (isBooking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "📅 ${stringResource(R.string.book_this_slot)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }

    // ── Booking Dialog ──────────────────────────────────────────────────
    if (showBookingDialog) {
        AlertDialog(
            onDismissRequest = { showBookingDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.confirm_booking),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "${stringResource(R.string.slot_label)}: ${selectedSlot ?: ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${stringResource(R.string.date_label)}: ${selectedDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = bookedBy,
                        onValueChange = { bookedBy = it },
                        label = { Text(stringResource(R.string.your_name)) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = teamName,
                        onValueChange = { teamName = it },
                        label = { Text(stringResource(R.string.team_name)) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Sport selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Cricket", "Volleyball", "Kabaddi", "Football").forEach { s ->
                            FilterChip(
                                selected = sport == s,
                                onClick = { sport = s },
                                label = { Text(s, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (bookedBy.isNotBlank() && teamName.isNotBlank()) {
                            viewModel.bookSelectedSlot(bookedBy, teamName, sport)
                        } else {
                            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isBooking
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showBookingDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
