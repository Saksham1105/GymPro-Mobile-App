package com.example.ui.screens.members

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.data.local.entity.MemberEntity
import com.example.data.local.entity.RenewalEntity
import com.example.di.Graph
import com.example.utils.FormatUtils
import kotlinx.coroutines.launch

class AddMemberViewModel : ViewModel() {
    private val memberRepository = Graph.memberRepository

    var name by mutableStateOf("")
    var phone by mutableStateOf("")
    var age by mutableStateOf("")
    var gender by mutableStateOf("Male")
    var weight by mutableStateOf("")
    var height by mutableStateOf("")
    var status by mutableStateOf("Active")
    var membershipFeePaid by mutableStateOf("")

    var nameError by mutableStateOf<String?>(null)
    var phoneError by mutableStateOf<String?>(null)
    var membershipFeePaidError by mutableStateOf<String?>(null)

    var selectedPlan by mutableStateOf("Monthly")

    fun saveMember(onSuccess: () -> Unit) {
        if (name.isBlank()) {
            nameError = "Full name is required"
            return
        }
        nameError = null
        
        if (!FormatUtils.isValidPhone(phone)) {
            phoneError = "Enter valid 10-digit Indian number"
            return
        }
        phoneError = null

        val fee = membershipFeePaid.toDoubleOrNull()
        if (fee == null) {
            membershipFeePaidError = "Please enter membership fee paid"
            return
        }
        if (fee < 0.0) {
            membershipFeePaidError = "Membership fee cannot be negative"
            return
        }
        membershipFeePaidError = null

        val durationMonths = when (selectedPlan) {
            "3 Months" -> 3
            "6 Months" -> 6
            "12 Months" -> 12
            else -> 1
        }

        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.MONTH, durationMonths)
        val computedExpiry = cal.timeInMillis

        val member = MemberEntity(
            name = name,
            phone = FormatUtils.sanitizeAndFormatPhone(phone),
            age = age.toIntOrNull() ?: 0,
            gender = gender,
            height = height.toFloatOrNull() ?: 0f,
            weight = weight.toFloatOrNull() ?: 0f,
            planId = null,
            joiningDate = System.currentTimeMillis(),
            expiryDate = computedExpiry,
            medicalNotes = "",
            emergencyContact = "",
            status = status,
            membershipFeePaid = fee
        )

        viewModelScope.launch {
            val memberId = memberRepository.insertMember(member).toInt()
            val renewal = RenewalEntity(
                memberId = memberId,
                planDuration = selectedPlan,
                amountPaid = fee,
                renewalDate = System.currentTimeMillis()
            )
            Graph.renewalRepository.insertRenewal(renewal)
            onSuccess()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemberScreen(
    navController: NavController,
    viewModel: AddMemberViewModel = viewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Member", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = viewModel.name,
                onValueChange = { 
                    viewModel.name = it 
                    viewModel.nameError = null
                },
                label = { Text("Full Name *") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth().testTag("add_member_name_input"),
                isError = viewModel.nameError != null,
                supportingText = viewModel.nameError?.let { { Text(it) } }
            )

            OutlinedTextField(
                value = viewModel.phone,
                onValueChange = { 
                    viewModel.phone = it
                    viewModel.phoneError = null
                },
                label = { Text("Phone Number *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth().testTag("add_member_phone_input"),
                isError = viewModel.phoneError != null,
                supportingText = viewModel.phoneError?.let { { Text(it) } }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = viewModel.age,
                    onValueChange = { viewModel.age = it },
                    label = { Text("Age") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    modifier = Modifier.weight(1f)
                )
                
                Column(modifier = Modifier.weight(1f)) {
                    Text("Gender", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        listOf("Male", "Female", "Other").forEach { g ->
                            FilterChip(
                                selected = viewModel.gender == g,
                                onClick = { viewModel.gender = g },
                                label = { Text(g, fontSize = 10.sp) }
                            )
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = viewModel.weight,
                    onValueChange = { viewModel.weight = it },
                    label = { Text("Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = viewModel.height,
                    onValueChange = { viewModel.height = it },
                    label = { Text("Height (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    modifier = Modifier.weight(1f)
                )
            }

            var expanded by remember { mutableStateOf(false) }
            val plans = listOf("Monthly", "3 Months", "6 Months", "12 Months")
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = viewModel.selectedPlan,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Membership Plan") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    plans.forEach { plan ->
                        DropdownMenuItem(
                            text = { Text(plan) },
                            onClick = {
                                viewModel.selectedPlan = plan
                                expanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = viewModel.membershipFeePaid,
                onValueChange = { 
                    viewModel.membershipFeePaid = it
                    viewModel.membershipFeePaidError = null
                },
                label = { Text("Membership Fee Paid (₹) *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth().testTag("membership_fee_input"),
                isError = viewModel.membershipFeePaidError != null,
                supportingText = viewModel.membershipFeePaidError?.let { { Text(it) } }
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.saveMember {
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Save Member", fontSize = 16.sp)
            }
        }
    }
}
