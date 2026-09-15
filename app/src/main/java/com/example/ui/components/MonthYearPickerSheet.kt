package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppTheme
import com.example.ui.theme.buttonTactilePress
import java.util.Calendar

data class MonthItem(
    val monthNumber: Int, // 1 to 12
    val shortName: String,
    val fullName: String
)

private val ALL_MONTHS = listOf(
    MonthItem(1, "Jan", "January"),
    MonthItem(2, "Feb", "February"),
    MonthItem(3, "Mar", "March"),
    MonthItem(4, "Apr", "April"),
    MonthItem(5, "May", "May"),
    MonthItem(6, "Jun", "June"),
    MonthItem(7, "Jul", "July"),
    MonthItem(8, "Aug", "August"),
    MonthItem(9, "Sep", "September"),
    MonthItem(10, "Oct", "October"),
    MonthItem(11, "Nov", "November"),
    MonthItem(12, "Dec", "December")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthYearPickerSheet(
    selectedMonth: Int,
    selectedYear: Int,
    onMonthYearSelected: (month: Int, year: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isDark = AppTheme.colors.isDark

    var pickerYear by remember(selectedYear) { mutableIntStateOf(selectedYear) }
    var pickerMonth by remember(selectedMonth) { mutableIntStateOf(selectedMonth) }

    val currentCalendar = remember { Calendar.getInstance() }
    val thisMonth = currentCalendar.get(Calendar.MONTH) + 1
    val thisYear = currentCalendar.get(Calendar.YEAR)

    val brandForestGreen = if (isDark) Color(0xFF4C7B5F) else Color(0xFF466A55)
    val sheetBgColor = if (isDark) Color(0xFF1E2024) else Color(0xFFFFFFFF)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBgColor,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 38.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isDark) Color(0xFF4B5563) else Color(0xFFD1D5DB))
            )
        },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("month_year_picker_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp, vertical = 6.dp)
        ) {
            // 1. Header: Title, Subtitle & Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Select Month",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = if (isDark) Color(0xFFF3F4F6) else Color(0xFF111827)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Pick a period to view statements and budgets",
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 13.5.sp,
                        color = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = if (isDark) Color(0xFFD1D5DB) else Color(0xFF1F2937),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Year Selector: Circle < button, centered Year, Circle > button
            val prevYearInteraction = remember { MutableInteractionSource() }
            val nextYearInteraction = remember { MutableInteractionSource() }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Prev Year Circle Button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF282B30) else Color(0xFFF1F2F4))
                        .buttonTactilePress(pressedScale = 0.90f, interactionSource = prevYearInteraction)
                        .clickable(
                            interactionSource = prevYearInteraction,
                            indication = null,
                            onClick = { pickerYear -= 1 }
                        )
                        .testTag("prev_year_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                        contentDescription = "Previous Year",
                        tint = if (isDark) Color(0xFFD1D5DB) else Color(0xFF1F2937),
                        modifier = Modifier
                            .size(14.dp)
                            .padding(end = 1.dp)
                    )
                }

                // Centered Year Text
                Text(
                    text = pickerYear.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (isDark) Color(0xFFF3F4F6) else Color(0xFF111827),
                    modifier = Modifier.testTag("picker_year_text")
                )

                // Next Year Circle Button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF282B30) else Color(0xFFF1F2F4))
                        .buttonTactilePress(pressedScale = 0.90f, interactionSource = nextYearInteraction)
                        .clickable(
                            interactionSource = nextYearInteraction,
                            indication = null,
                            onClick = { pickerYear += 1 }
                        )
                        .testTag("next_year_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "Next Year",
                        tint = if (isDark) Color(0xFFD1D5DB) else Color(0xFF1F2937),
                        modifier = Modifier
                            .size(14.dp)
                            .padding(start = 1.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. 12-Month Grid: 4 rows x 3 columns
            val rows = ALL_MONTHS.chunked(3)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rows.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowItems.forEach { item ->
                            val isSelected = (item.monthNumber == pickerMonth)
                            val tileInteraction = remember { MutableInteractionSource() }

                            val tileBg = if (isSelected) {
                                brandForestGreen
                            } else {
                                if (isDark) Color(0xFF23252A) else Color(0xFFF7F7F8)
                            }
                            val tileBorder = if (!isSelected && isDark) {
                                BorderStroke(1.dp, Color(0xFF2E3138))
                            } else null

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(62.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .buttonTactilePress(pressedScale = 0.96f, interactionSource = tileInteraction)
                                    .clickable(
                                        interactionSource = tileInteraction,
                                        indication = null,
                                        onClick = { pickerMonth = item.monthNumber }
                                    )
                                    .testTag("month_item_${item.monthNumber}"),
                                shape = RoundedCornerShape(16.dp),
                                color = tileBg,
                                border = tileBorder,
                                shadowElevation = 0.dp
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    // Centered Month Name & 2-Digit Number
                                    Column(
                                        modifier = Modifier.align(Alignment.Center),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = item.shortName,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                            color = if (isSelected) Color.White else if (isDark) Color(0xFFF3F4F6) else Color(0xFF18181B),
                                            fontSize = 15.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = String.format("%02d", item.monthNumber),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                            color = if (isSelected) Color.White.copy(alpha = 0.95f) else if (isDark) Color(0xFF9CA3AF) else Color(0xFF71717A),
                                            fontSize = 12.sp
                                        )
                                    }

                                    // White Circular Checkmark Badge for Selected Tile
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(top = 8.dp, end = 8.dp)
                                                .size(18.dp)
                                                .clip(CircleShape)
                                                .background(Color.White),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = brandForestGreen,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Quick Action Row: "Jump to Current Month" & "Active: Mon Year"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clickable {
                            pickerYear = thisYear
                            pickerMonth = thisMonth
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = "Jump to Current Month",
                        tint = if (isDark) Color(0xFFD1D5DB) else Color(0xFF374151),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Jump to Current Month",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.5.sp,
                        color = if (isDark) Color(0xFFE5E7EB) else Color(0xFF1F2937)
                    )
                }

                Text(
                    text = "Active: ${ALL_MONTHS.getOrNull(pickerMonth - 1)?.shortName ?: "Jan"} $pickerYear",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.5.sp,
                    color = if (isDark) Color(0xFF6EE7B7) else Color(0xFF3B7A57)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 5. "Done" Pill Button
            val doneInteraction = remember { MutableInteractionSource() }
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .buttonTactilePress(pressedScale = 0.98f, interactionSource = doneInteraction)
                    .clickable(
                        interactionSource = doneInteraction,
                        indication = null,
                        onClick = {
                            onMonthYearSelected(pickerMonth, pickerYear)
                            onDismiss()
                        }
                    )
                    .testTag("month_picker_done_button"),
                shape = RoundedCornerShape(26.dp),
                color = brandForestGreen,
                shadowElevation = 0.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Done",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

