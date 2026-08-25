package com.example.ui.screens.members

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.data.local.entity.MemberEntity
import com.example.data.local.entity.RenewalEntity
import com.example.utils.FormatUtils
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembersScreen(
    navController: NavController,
    viewModel: MembersViewModel = viewModel()
) {
    val members by viewModel.members.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val filterOption by viewModel.filterOption.collectAsStateWithLifecycle()
    
    var selectedMemberForDetails by remember { mutableStateOf<MemberEntity?>(null) }
    var selectedMemberForRenewal by remember { mutableStateOf<MemberEntity?>(null) }
    var selectedMemberForEdit by remember { mutableStateOf<MemberEntity?>(null) }
    var expandedMemberId by remember { mutableStateOf<Int?>(null) }
    var memberToDelete by remember { mutableStateOf<MemberEntity?>(null) }

    val haptic = LocalHapticFeedback.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gym Members", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    navController.navigate("add_member") 
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_member_fab")
            ) {
                Icon(Icons.Filled.Add, "Add Member")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Instant Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("member_search_input"),
                placeholder = { Text("Search by name or phone...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Sorting Chip Row
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sort by:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(54.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(SortOption.values()) { option ->
                            FilterChip(
                                selected = sortOption == option,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.onSortOptionChange(option)
                                },
                                label = { Text(option.displayName, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Filtering Chip Row
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filter:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(54.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(FilterOption.values()) { option ->
                            FilterChip(
                                selected = filterOption == option,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.onFilterOptionChange(option)
                                },
                                label = { Text(option.displayName, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (members.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No members match current filters.", color = Color.Gray, modifier = Modifier.testTag("empty_members_text"))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = members,
                        key = { it.id }
                    ) { member ->
                        val isExpanded = expandedMemberId == member.id
                        MemberItem(
                            member = member,
                            isExpanded = isExpanded,
                            onExpandToggle = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                expandedMemberId = if (isExpanded) null else member.id
                            },
                            onDetailsClick = {
                                viewModel.selectMember(member.id)
                                selectedMemberForDetails = member
                            },
                            onEditClick = {
                                selectedMemberForEdit = member
                            },
                            onRenewClick = {
                                selectedMemberForRenewal = member
                            },
                            onDeleteClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                memberToDelete = member
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
            }
        }
    }

    // Detail Dialog
    selectedMemberForDetails?.let { member ->
        val renewals by viewModel.selectedMemberRenewals.collectAsStateWithLifecycle()
        val attendanceCount by viewModel.selectedMemberAttendanceThisMonth.collectAsStateWithLifecycle()
        
        MemberDetailsDialog(
            member = member,
            renewals = renewals,
            attendanceThisMonth = attendanceCount,
            onDismiss = { 
                viewModel.selectMember(null)
                selectedMemberForDetails = null 
            },
            onRenewClick = {
                selectedMemberForRenewal = member
                selectedMemberForDetails = null
            },
            onEditClick = {
                selectedMemberForEdit = member
                selectedMemberForDetails = null
            },
            onDelete = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                memberToDelete = member
                viewModel.selectMember(null)
                selectedMemberForDetails = null
            }
        )
    }

    // Renewal Dialog
    selectedMemberForRenewal?.let { member ->
        RenewMembershipDialog(
            member = member,
            onDismiss = { selectedMemberForRenewal = null },
            onRenew = { months, planName, amount, date ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.renewMembership(member, months, planName, amount, date)
                selectedMemberForRenewal = null
            }
        )
    }

    // Edit Dialog
    selectedMemberForEdit?.let { member ->
        EditMemberDialog(
            member = member,
            onDismiss = { selectedMemberForEdit = null },
            onSave = { name, phone, age, gender, height, weight, status ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.updateMemberDetails(member, name, phone, age, gender, height, weight, status)
                selectedMemberForEdit = null
            }
        )
    }

    // Delete Confirmation Dialog
    memberToDelete?.let { member ->
        AlertDialog(
            onDismissRequest = { memberToDelete = null },
            title = { Text("Delete Member?", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
            text = {
                Text(
                    "Are you sure you want to delete ${member.name}?\n\nThis will also permanently delete all of their attendance records and renewal history. This action cannot be undone."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.deleteMember(member)
                        memberToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { memberToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MemberItem(
    member: MemberEntity,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onDetailsClick: () -> Unit,
    onEditClick: () -> Unit,
    onRenewClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isExpanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) 
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onExpandToggle() }
            .testTag("member_item_${member.id}")
            .animateContentSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f) 
                             else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .padding(14.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            if (member.status == "Expired") Color(0xFFEF4444).copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.primaryContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = member.name.take(2).uppercase(),
                        color = if (member.status == "Expired") Color(0xFFEF4444)
                                else MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                Spacer(modifier = Modifier.width(14.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = member.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = member.phone,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Expires: ${FormatUtils.formatDate(member.expiryDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (member.status == "Expired") Color(0xFFEF4444) else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                StatusBadge(status = member.status)
            }
            
            if (isExpanded) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    modifier = Modifier.padding(horizontal = 14.dp)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDetailsClick,
                        modifier = Modifier.testTag("quick_details_${member.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "Details",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Profile", fontSize = 12.sp)
                    }
                    
                    TextButton(
                        onClick = onEditClick,
                        modifier = Modifier.testTag("quick_edit_${member.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit", fontSize = 12.sp)
                    }
                    
                    TextButton(
                        onClick = onRenewClick,
                        modifier = Modifier.testTag("quick_renew_${member.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Renew",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Renew", fontSize = 12.sp)
                    }
                    
                    var showConfirmDelete by remember { mutableStateOf(false) }
                    
                    if (showConfirmDelete) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Sure?", fontSize = 11.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                            IconButton(
                                onClick = { 
                                    showConfirmDelete = false
                                    onDeleteClick() 
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Confirm Delete",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = { showConfirmDelete = false },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Cancel Delete",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    } else {
                        TextButton(
                            onClick = { showConfirmDelete = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444)),
                            modifier = Modifier.testTag("quick_delete_${member.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when (status) {
        "Active" -> Color(0xFF10B981)
        "Expired" -> Color(0xFFEF4444)
        else -> Color(0xFFFFB300)
    }
    
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.uppercase(),
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun MemberDetailsDialog(
    member: MemberEntity,
    renewals: List<RenewalEntity>,
    attendanceThisMonth: Int,
    onDismiss: () -> Unit,
    onRenewClick: () -> Unit,
    onEditClick: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val totalPaid = renewals.sumOf { it.amountPaid }
    val lastRenewal = renewals.firstOrNull()?.let {
        FormatUtils.formatDate(it.renewalDate)
    } ?: "Initial Fee"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Member Profile",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header card
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = member.name.take(2).uppercase(),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = member.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        StatusBadge(status = member.status)
                    }
                }

                // Personal Info
                Text("Personal Information", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                DetailRow(label = "Phone", value = member.phone)
                DetailRow(label = "Age", value = "${member.age} years")
                DetailRow(label = "Gender", value = member.gender)
                DetailRow(label = "Height", value = "${member.height} cm")
                DetailRow(label = "Weight", value = "${member.weight} kg")
                
                DetailRow(label = "Joining Date", value = FormatUtils.formatDate(member.joiningDate))
                DetailRow(label = "Expiry Date", value = FormatUtils.formatDate(member.expiryDate))

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // Subscription Info
                Text("Subscription & Payments", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                DetailRow(
                    label = "Total Amount Paid",
                    value = FormatUtils.formatCurrency(totalPaid),
                    isBold = true
                )
                DetailRow(label = "Last Renewal", value = lastRenewal)
                DetailRow(label = "Attendance This Month", value = "$attendanceThisMonth days")

                Spacer(modifier = Modifier.height(4.dp))

                // Dial Call Button
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${member.phone}"))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Handler in case of tablet with no dialer
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Phone, contentDescription = "Call")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Call Member")
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Delete Button on bottom left
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("dialog_delete_button")
                ) {
                    Text("Delete")
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onEditClick) {
                        Text("Edit")
                    }
                    Button(onClick = onRenewClick, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                        Text("Renew")
                    }
                }
            }
        }
    )
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun RenewMembershipDialog(
    member: MemberEntity,
    onDismiss: () -> Unit,
    onRenew: (durationMonths: Int, planName: String, amountPaid: Double, renewalDate: Long) -> Unit
) {
    var selectedPlanIndex by remember { mutableStateOf(0) }
    val plans = listOf(
        Triple("1 Month", 1, 1000.0),
        Triple("3 Months", 3, 2500.0),
        Triple("6 Months", 6, 4500.0),
        Triple("12 Months", 12, 8000.0),
        Triple("Custom", 0, 0.0)
    )
    
    var customMonths by remember { mutableStateOf("") }
    var amountPaid by remember { mutableStateOf("") }
    
    LaunchedEffect(selectedPlanIndex) {
        val selected = plans[selectedPlanIndex]
        if (selected.first != "Custom") {
            amountPaid = selected.third.toString()
        } else {
            amountPaid = ""
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Renew Membership", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Select plan for ${member.name}:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Horizontal chips for plans
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(plans.size) { index ->
                        FilterChip(
                            selected = selectedPlanIndex == index,
                            onClick = { selectedPlanIndex = index },
                            label = { Text(plans[index].first) }
                        )
                    }
                }
                
                if (plans[selectedPlanIndex].first == "Custom") {
                    OutlinedTextField(
                        value = customMonths,
                        onValueChange = { customMonths = it },
                        label = { Text("Duration (Months)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                OutlinedTextField(
                    value = amountPaid,
                    onValueChange = { amountPaid = it },
                    label = { Text("Amount Paid (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val selected = plans[selectedPlanIndex]
                    val months = if (selected.first == "Custom") customMonths.toIntOrNull() ?: 1 else selected.second
                    val amount = amountPaid.toDoubleOrNull() ?: 0.0
                    onRenew(months, selected.first, amount, System.currentTimeMillis())
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Confirm Renewal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditMemberDialog(
    member: MemberEntity,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, age: Int, gender: String, height: Float, weight: Float, status: String) -> Unit
) {
    var name by remember { mutableStateOf(member.name) }
    var phone by remember { mutableStateOf(member.phone) }
    var age by remember { mutableStateOf(member.age.toString()) }
    var gender by remember { mutableStateOf(member.gender) }
    var height by remember { mutableStateOf(member.height.toString()) }
    var weight by remember { mutableStateOf(member.weight.toString()) }
    var status by remember { mutableStateOf(member.status) }
    
    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile Details", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { 
                        name = it 
                        nameError = null
                    },
                    label = { Text("Name") },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { 
                        phone = it 
                        phoneError = null
                    },
                    label = { Text("Phone") },
                    isError = phoneError != null,
                    supportingText = phoneError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = age,
                        onValueChange = { age = it },
                        label = { Text("Age") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                    )
                    OutlinedTextField(
                        value = height,
                        onValueChange = { height = it },
                        label = { Text("Height (cm)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text("Weight (kg)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Gender", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("Male", "Female", "Other").forEach { g ->
                                FilterChip(
                                    selected = gender == g,
                                    onClick = { gender = g },
                                    label = { Text(g, fontSize = 10.sp) }
                                )
                            }
                        }
                    }
                }
                
                Text("Membership Status:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Active", "Expired").forEach { s ->
                        FilterChip(
                            selected = status == s,
                            onClick = { status = s },
                            label = { Text(s) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = "Name cannot be blank"
                        return@Button
                    }
                    val cleanPhone = phone.filter { it.isDigit() }
                    val finalPhone = if (cleanPhone.length > 10 && cleanPhone.startsWith("91")) cleanPhone.substring(cleanPhone.length - 10) else cleanPhone
                    if (finalPhone.length != 10) {
                        phoneError = "Enter valid 10-digit Indian number"
                        return@Button
                    }
                    onSave(
                        name,
                        "+91 $finalPhone",
                        age.toIntOrNull() ?: 0,
                        gender,
                        height.toFloatOrNull() ?: 0f,
                        weight.toFloatOrNull() ?: 0f,
                        status
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
