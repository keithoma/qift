package com.example.qift.issue

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Calendar
import java.util.Date
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueCardScreen() {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var customAmount by remember { mutableStateOf("") }
    var selectedPreset by remember { mutableStateOf<Int?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    val presetValues = listOf(20, 30, 50, 80, 100)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Issue Gift Card") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Customer Email Input
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Customer Email") },
                placeholder = { Text("customer@example.com") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text(
                text = "Select Amount",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
            )

            // Preset Values Grid/Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                presetValues.take(3).forEach { amount ->
                    PresetButton(
                        amount = amount,
                        isSelected = selectedPreset == amount,
                        onClick = {
                            selectedPreset = amount
                            customAmount = ""
                        }
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                presetValues.drop(3).forEach { amount ->
                    PresetButton(
                        amount = amount,
                        isSelected = selectedPreset == amount,
                        onClick = {
                            selectedPreset = amount
                            customAmount = ""
                        }
                    )
                }
            }

            // Custom Amount Input
            OutlinedTextField(
                value = customAmount,
                onValueChange = { input ->
                    // Allow only digits and a single decimal separator (comma or dot)
                    if (input.matches(Regex("^\\d*[.,]?\\d{0,2}$"))) {
                        customAmount = input.replace(',', '.')
                        if (customAmount.isNotEmpty()) {
                            selectedPreset = null
                        }
                    }
                },
                label = { Text("Custom Amount") },
                placeholder = { Text("0.00") },
                trailingIcon = { Text("€", modifier = Modifier.padding(end = 12.dp)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.weight(1f))

            // Submit Button
            Button(
                onClick = {
                    val finalAmountStr = selectedPreset?.toString() ?: customAmount
                    val amountDouble = finalAmountStr.toDoubleOrNull()
                    
                    if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        Toast.makeText(context, "Please enter a valid email address.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (amountDouble == null || amountDouble <= 0) {
                        Toast.makeText(context, "Please select or enter a valid amount.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    // Convert to integer cents
                    val amountCents = (amountDouble * 100).toInt()
                    
                    submitGiftCard(
                        email = email,
                        amountCents = amountCents,
                        onLoading = { isSubmitting = true },
                        onSuccess = { token, expiryDate ->
                            isSubmitting = false
                            Toast.makeText(context, "Gift Card Issued Successfully!", Toast.LENGTH_LONG).show()
                            
                            // Launch Email Intent
                            val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                            val expiryStr = dateFormat.format(expiryDate)
                            val valueStr = String.format(java.util.Locale.getDefault(), "%.2f", amountCents / 100.0)

                            val emailIntent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                                data = android.net.Uri.parse("mailto:")
                                putExtra(android.content.Intent.EXTRA_EMAIL, arrayOf(email))
                                putExtra(android.content.Intent.EXTRA_SUBJECT, "Your Restaurant Gift Card is Here!")
                                putExtra(
                                    android.content.Intent.EXTRA_TEXT,
                                    "Hello!\n\nHere is your restaurant gift card for $valueStr €.\n" +
                                            "This gift card is valid until $expiryStr.\n\n" +
                                            "You can view and scan your QR code by clicking this link:\n" +
                                            "https://quickchart.io/qr?text=$token&size=300\n\n" +
                                            "Enjoy your meal!"
                                )
                            }
                            try {
                                context.startActivity(emailIntent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "No email app found.", Toast.LENGTH_SHORT).show()
                            }

                            // Reset form
                            email = ""
                            customAmount = ""
                            selectedPreset = null
                        },
                        onFailure = { errorMsg ->
                            isSubmitting = false
                            Toast.makeText(context, "Failed: $errorMsg", Toast.LENGTH_LONG).show()
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Generate & Issue Card", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
fun PresetButton(amount: Int, isSelected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text("$amount €", style = MaterialTheme.typography.bodyLarge) },
        modifier = Modifier.padding(4.dp)
    )
}

private fun submitGiftCard(
    email: String,
    amountCents: Int,
    onLoading: () -> Unit,
    onSuccess: (String, Date) -> Unit,
    onFailure: (String) -> Unit
) {
    onLoading()

    val firestore = Firebase.firestore
    
    // Generate an alphanumeric Token (UUID without dashes)
    val token = UUID.randomUUID().toString().replace("-", "").uppercase()
    
    // Calculate Issue & Expiry Dates
    val calendar = Calendar.getInstance()
    val issueDate = calendar.time
    
    calendar.add(Calendar.DAY_OF_YEAR, 90)
    val expiryDate = calendar.time

    // Prepare exactly as per NoSQL Schema
    val giftCardData = mapOf(
        "token" to token,
        "email" to email,
        "initialAmount" to amountCents,
        "remainingBalance" to amountCents,
        "issueDate" to Timestamp(issueDate),
        "expiryDate" to Timestamp(expiryDate),
        "status" to "active"
    )

    firestore.collection("GiftCards").document(token)
        .set(giftCardData)
        .addOnSuccessListener {
            // Also log the creation in AuditLogs
            val auditLogData = mapOf(
                "cardId" to token,
                "action" to "ISSUED",
                "deductedAmount" to 0,
                "previousBalance" to 0,
                "newBalance" to amountCents,
                "deviceId" to "DEVICE_ID_PLACEHOLDER",
                "userId" to "DEVICE_ID_PLACEHOLDER",
                "timestamp" to Timestamp(Date())
            )
            
            firestore.collection("AuditLogs").add(auditLogData)
                .addOnSuccessListener { onSuccess(token, expiryDate) }
                .addOnFailureListener { e -> onFailure(e.message ?: "Failed to write audit log") }
        }
        .addOnFailureListener { e ->
            onFailure(e.message ?: "Unknown database error occurred")
        }
}
