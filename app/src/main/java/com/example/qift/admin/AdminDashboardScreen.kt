package com.example.qift.admin

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

enum class AdminState { PIN_ENTRY, CARD_LIST, CARD_DETAIL }

data class GiftCardItem(
    val token: String = "",
    val customerEmail: String = "",
    val initialAmount: Int = 0,
    val remainingBalance: Int = 0,
    val issueDate: Date? = null,
    val expiryDate: Date? = null,
    val status: String = ""
)

data class AuditLogItem(
    val action: String = "",
    val timestamp: Date? = null,
    val deductedAmount: Int = 0,
    val previousBalance: Int = 0,
    val newBalance: Int = 0,
    val deviceId: String = ""
)

fun formatCentsToEur(cents: Int): String = String.format(Locale.getDefault(), "€%.2f", cents / 100.0)
fun formatEurValue(cents: Int): String = String.format(Locale.US, "%.2f", cents / 100.0)
fun formatDate(date: Date?): String = date?.let { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(it) } ?: "N/A"
fun formatDateOnly(date: Date?): String = date?.let { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it) } ?: ""

@Composable
fun AdminDashboardScreen() {
    var state by remember { mutableStateOf(AdminState.PIN_ENTRY) }
    var selectedToken by remember { mutableStateOf<String?>(null) }

    when (state) {
        AdminState.PIN_ENTRY -> {
            PinEntryScreen(onUnlock = { state = AdminState.CARD_LIST })
        }
        AdminState.CARD_LIST -> {
            CardListScreen(onCardSelected = { token ->
                selectedToken = token
                state = AdminState.CARD_DETAIL
            })
        }
        AdminState.CARD_DETAIL -> {
            CardDetailScreen(
                token = selectedToken ?: "",
                onBack = { state = AdminState.CARD_LIST }
            )
        }
    }
}

@Composable
fun PinEntryScreen(onUnlock: () -> Unit) {
    var pin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Filled.Warning, contentDescription = "Admin Area", modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Admin Dashboard", style = MaterialTheme.typography.headlineMedium)
        Text("Restricted Access", color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = pin,
            onValueChange = { 
                pin = it
                isError = false
            },
            label = { Text("Enter 4-Digit PIN") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = PasswordVisualTransformation(),
            isError = isError,
            singleLine = true
        )
        if (isError) {
            Text("Incorrect PIN", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            if (pin == "1234") {
                onUnlock()
            } else {
                isError = true
            }
        }) {
            Text("Unlock")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardListScreen(onCardSelected: (String) -> Unit) {
    val db = Firebase.firestore
    var cards by remember { mutableStateOf<List<GiftCardItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val snapshot = db.collection("GiftCards")
                .orderBy("issueDate", Query.Direction.DESCENDING)
                .get().await()
            val loadedCards = snapshot.documents.mapNotNull { doc ->
                GiftCardItem(
                    token = doc.id,
                    customerEmail = doc.getString("customerEmail") ?: "",
                    initialAmount = doc.getLong("initialAmount")?.toInt() ?: 0,
                    remainingBalance = doc.getLong("remainingBalance")?.toInt() ?: 0,
                    issueDate = doc.getTimestamp("issueDate")?.toDate(),
                    expiryDate = doc.getTimestamp("expiryDate")?.toDate(),
                    status = doc.getString("status") ?: ""
                )
            }
            cards = loadedCards
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Gift Cards Registry") }) }
    ) { innerPadding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (cards.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("No gift cards found.")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                items(cards) { card ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clickable { onCardSelected(card.token) },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Token: ${card.token.take(8)}...", fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text("Email: ${card.customerEmail}")
                            Text("Balance: ${formatCentsToEur(card.remainingBalance)}", color = MaterialTheme.colorScheme.primary)
                            Text("Status: ${card.status}")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailScreen(token: String, onBack: () -> Unit) {
    val db = Firebase.firestore
    val context = LocalContext.current
    var card by remember { mutableStateOf<GiftCardItem?>(null) }
    var logs by remember { mutableStateOf<List<AuditLogItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var refreshKey by remember { mutableStateOf(0) }

    LaunchedEffect(refreshKey) {
        isLoading = true
        try {
            val doc = db.collection("GiftCards").document(token).get().await()
            if (doc.exists()) {
                card = GiftCardItem(
                    token = doc.id,
                    customerEmail = doc.getString("customerEmail") ?: "",
                    initialAmount = doc.getLong("initialAmount")?.toInt() ?: 0,
                    remainingBalance = doc.getLong("remainingBalance")?.toInt() ?: 0,
                    issueDate = doc.getTimestamp("issueDate")?.toDate(),
                    expiryDate = doc.getTimestamp("expiryDate")?.toDate(),
                    status = doc.getString("status") ?: ""
                )
            }

            val logSnap = db.collection("AuditLogs")
                .whereEqualTo("giftCardToken", token)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get().await()
            logs = logSnap.documents.mapNotNull {
                AuditLogItem(
                    action = it.getString("action") ?: "",
                    timestamp = it.getTimestamp("timestamp")?.toDate(),
                    deductedAmount = it.getLong("deductedAmount")?.toInt() ?: 0,
                    previousBalance = it.getLong("previousBalance")?.toInt() ?: 0,
                    newBalance = it.getLong("newBalance")?.toInt() ?: 0,
                    deviceId = it.getString("deviceId") ?: ""
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error fetching details.", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Details & Audit") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            card?.let { currentCard ->
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Card Details
                    Card(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Gift Card Information", style = MaterialTheme.typography.titleLarge)
                            Divider(Modifier.padding(vertical = 8.dp))
                            Text("Token: ${currentCard.token}")
                            Text("Email: ${currentCard.customerEmail}")
                            Text("Initial: ${formatCentsToEur(currentCard.initialAmount)}")
                            Text("Balance: ${formatCentsToEur(currentCard.remainingBalance)}", fontWeight = FontWeight.Bold)
                            Text("Issued: ${formatDate(currentCard.issueDate)}")
                            Text("Expires: ${formatDate(currentCard.expiryDate)}")
                            Text("Status: ${currentCard.status}")
                        }
                    }

                    // Audit Logs
                    Text("Audit Logs", modifier = Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.titleLarge)
                    if (logs.isEmpty()) {
                        Text("No logs found.", modifier = Modifier.padding(16.dp))
                    } else {
                        logs.forEach { log ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text(log.action, fontWeight = FontWeight.Bold, color = if(log.action == "ADMIN_EDIT") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                                        Text(formatDate(log.timestamp), style = MaterialTheme.typography.bodySmall)
                                    }
                                    if (log.action == "REDEEMED") {
                                        Text("Deducted: ${formatCentsToEur(log.deductedAmount)}")
                                    }
                                    Text("Balance: ${formatCentsToEur(log.previousBalance)} -> ${formatCentsToEur(log.newBalance)}", style = MaterialTheme.typography.bodyMedium)
                                    Text("Device/Source: ${log.deviceId}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Danger Zone
                    DangerZone(
                        card = currentCard,
                        onUpdated = { refreshKey++ }
                    )
                }
            } ?: Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("Card details unavailable.")
            }
        }
    }
}

@Composable
fun DangerZone(card: GiftCardItem, onUpdated: () -> Unit) {
    val context = LocalContext.current
    var overrideBalanceStr by remember { mutableStateOf(formatEurValue(card.remainingBalance)) }
    var overrideExpiryStr by remember { mutableStateOf(formatDateOnly(card.expiryDate)) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Warning, contentDescription = "Danger", tint = MaterialTheme.colorScheme.error)
                Spacer(Modifier.width(8.dp))
                Text("DANGER ZONE", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }
            Text("Manually override balance or expiry. This action is irreversible.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
            
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = overrideBalanceStr,
                onValueChange = { overrideBalanceStr = it },
                label = { Text("New Balance (€)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = overrideExpiryStr,
                onValueChange = { overrideExpiryStr = it },
                label = { Text("New Expiry Date (yyyy-MM-dd)") }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { showConfirmDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Save Override")
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Override?") },
            text = { Text("Are you sure you want to forcibly change the stored balance/expiry date of this card? An ADMIN_EDIT audit log will be generated.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        executeOverride(context, card, overrideBalanceStr, overrideExpiryStr, onUpdated)
                    }
                ) {
                    Text("Confirm", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun executeOverride(
    context: android.content.Context,
    card: GiftCardItem,
    balanceStr: String,
    expiryStr: String,
    onSuccess: () -> Unit
) {
    val newBalanceCents = (balanceStr.toFloatOrNull()?.times(100))?.toInt() ?: card.remainingBalance
    val newExpiryDate = try {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(expiryStr)
    } catch (e: Exception) {
        card.expiryDate
    }

    val db = Firebase.firestore
    db.runTransaction { transaction ->
        val cardRef = db.collection("GiftCards").document(card.token)
        val snap = transaction.get(cardRef)
        val prevCents = snap.getLong("remainingBalance")?.toInt() ?: 0
        
        val updates = mutableMapOf<String, Any>(
            "remainingBalance" to newBalanceCents
        )
        if (newExpiryDate != null) {
            updates["expiryDate"] = Timestamp(newExpiryDate)
        }
        
        transaction.update(cardRef, updates)
        
        val logRef = db.collection("AuditLogs").document()
        val auditData = mapOf(
            "giftCardToken" to card.token,
            "action" to "ADMIN_EDIT",
            "previousBalance" to prevCents,
            "newBalance" to newBalanceCents,
            "deductedAmount" to (prevCents - newBalanceCents),
            "deviceId" to Build.MODEL,
            "timestamp" to Timestamp.now()
        )
        transaction.set(logRef, auditData)
    }.addOnSuccessListener {
        Toast.makeText(context, "Override applied successfully.", Toast.LENGTH_SHORT).show()
        onSuccess()
    }.addOnFailureListener { e ->
        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
