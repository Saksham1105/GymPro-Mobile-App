package com.example.ui.screens.attendance

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.utils.FormatUtils
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    navController: NavController,
    viewModel: AttendanceViewModel = viewModel()
) {
    val membersWithAttendance by viewModel.membersWithAttendance.collectAsStateWithLifecycle()
    val showExpired by viewModel.showExpired.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Attendance Register", fontWeight = FontWeight.Bold) },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "Show Expired",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Switch(
                            checked = showExpired,
                            onCheckedChange = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.toggleShowExpired()
                            },
                            modifier = Modifier.scale(0.8f).testTag("show_expired_switch")
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        if (membersWithAttendance.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues), 
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No active members found.",
                        color = Color.Gray,
                        modifier = Modifier.testTag("empty_state_text")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Add some members or toggle \"Show Expired\" above.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Info bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val presentCount = membersWithAttendance.count { it.todayStatus == "Present" }
                    val absentCount = membersWithAttendance.count { it.todayStatus == "Absent" }
                    val unmarkedCount = membersWithAttendance.count { it.todayStatus == null }
                    
                    Text(
                        text = "Present: $presentCount",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Absent: $absentCount",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Unmarked: $unmarkedCount",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = membersWithAttendance,
                        key = { it.member.id }
                    ) { state ->
                        AttendanceItemRedesigned(
                            state = state,
                            onMarkPresent = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.markAttendance(state.member.id, "Present")
                            },
                            onMarkAbsent = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.markAttendance(state.member.id, "Absent")
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
            }
        }
    }
}

@Composable
fun AttendanceItemRedesigned(
    state: MemberAttendanceState,
    onMarkPresent: () -> Unit,
    onMarkAbsent: () -> Unit
) {
    val isPresent = state.todayStatus == "Present"
    val isAbsent = state.todayStatus == "Absent"

    // Dynamic coloring based on status
    val cardBgColor by animateColorAsState(
        targetValue = when {
            isPresent -> Color(0xFF10B981).copy(alpha = 0.08f)
            isAbsent -> Color(0xFFEF4444).copy(alpha = 0.08f)
            else -> MaterialTheme.colorScheme.surface
        },
        label = "bgColor"
    )

    val cardBorderColor by animateColorAsState(
        targetValue = when {
            isPresent -> Color(0xFF10B981).copy(alpha = 0.4f)
            isAbsent -> Color(0xFFEF4444).copy(alpha = 0.4f)
            else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        },
        label = "borderColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("attendance_card_${state.member.id}")
            .clickable {
                // Quick toggle: clicking the row itself defaults to marking present if not present, otherwise clears/absents
                if (isPresent) onMarkAbsent() else onMarkPresent()
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        border = BorderStroke(1.dp, cardBorderColor)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Initials Circle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (state.member.status == "Expired") Color(0xFFEF4444).copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.primaryContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.member.name.take(2).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (state.member.status == "Expired") Color(0xFFEF4444)
                            else MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Member Info Column
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = state.member.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    
                    // Simple inline badge for Expired
                    if (state.member.status == "Expired") {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFEF4444).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "EXPIRED",
                                color = Color(0xFFEF4444),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                color = Color(0xFF10B981),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = "Expires: ${FormatUtils.formatDate(state.member.expiryDate)} • Fee: ${FormatUtils.formatCurrencyNoDecimals(state.member.membershipFeePaid)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Attendance Action Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Present Button
                IconButton(
                    onClick = onMarkPresent,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("present_button_${state.member.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Mark Present",
                        tint = if (isPresent) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Absent Button
                IconButton(
                    onClick = onMarkAbsent,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("absent_button_${state.member.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Cancel,
                        contentDescription = "Mark Absent",
                        tint = if (isAbsent) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
