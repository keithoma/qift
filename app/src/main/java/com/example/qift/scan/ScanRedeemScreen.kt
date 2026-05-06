package com.example.qift.scan

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.*
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanRedeemScreen() {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Card") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (hasCameraPermission) {
                CameraAndRedeemUI()
            } else {
                Text(
                    text = "Camera permission is required to scan QR codes.",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

enum class CardState {
    SCANNING, LOADING, VALID, INVALID, REDEEMED_SUCCESS
}

@Composable
fun CameraAndRedeemUI() {
    var cardState by remember { mutableStateOf(CardState.SCANNING) }
    var scannedToken by remember { mutableStateOf<String?>(null) }
    var remainingBalanceCents by remember { mutableStateOf(0) }
    var invalidReason by remember { mutableStateOf("") }
    
    when (cardState) {
        CardState.SCANNING -> {
            CameraScanner { token ->
                scannedToken = token
                cardState = CardState.LOADING
            }
        }
        CardState.LOADING -> {
            scannedToken?.let { token ->
                ValidateCardEffect(
                    token = token,
                    onValid = { balance ->
                        remainingBalanceCents = balance
                        cardState = CardState.VALID
                    },
                    onInvalid = { reason ->
                        invalidReason = reason
                        cardState = CardState.INVALID
                    }
                )
            }
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        CardState.VALID -> {
            ValidCardScreen(
                token = scannedToken!!,
                remainingBalanceCents = remainingBalanceCents,
                onSuccess = { cardState = CardState.REDEEMED_SUCCESS },
                onCancel = { cardState = CardState.SCANNING }
            )
        }
        CardState.INVALID -> {
            InvalidCardScreen(
                reason = invalidReason,
                onRetry = { cardState = CardState.SCANNING }
            )
        }
        CardState.REDEEMED_SUCCESS -> {
            SuccessScreen { cardState = CardState.SCANNING }
        }
    }
}

@Composable
fun CameraScanner(onBarcodeScanned: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isProcessing by remember { mutableStateOf(false) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val options = BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                    .build()
                val scanner = BarcodeScanning.getClient(options)
                val executor = Executors.newSingleThreadExecutor()

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(executor) { imageProxy ->
                    if (isProcessing) {
                        imageProxy.close()
                        return@setAnalyzer
                    }

                    @SuppressLint("UnsafeOptInUsageError")
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                        scanner.process(image)
                            .addOnSuccessListener { barcodes ->
                                for (barcode in barcodes) {
                                    barcode.rawValue?.let { value ->
                                        isProcessing = true
                                        onBarcodeScanned(value)
                                        break
                                    }
                                }
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    } else {
                        imageProxy.close()
                    }
                }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                } catch (exc: Exception) {
                    Log.e("CameraScanner", "Use case binding failed", exc)
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun ValidateCardEffect(
    token: String,
    onValid: (Int) -> Unit,
    onInvalid: (String) -> Unit
) {
    LaunchedEffect(token) {
        val firestore = Firebase.firestore
        firestore.collection("GiftCards").document(token).get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    onInvalid("Card not found.")
                    return@addOnSuccessListener
                }

                val status = document.getString("status")
                if (status != "ACTIVE") {
                    onInvalid("Card is not active (Status: $status).")
                    return@addOnSuccessListener
                }

                val expiryStamp = document.getTimestamp("expiryDate")
                if (expiryStamp != null && expiryStamp.toDate().before(Date())) {
                    onInvalid("Card has expired.")
                    return@addOnSuccessListener
                }

                val balance = document.getLong("remainingBalance")?.toInt() ?: 0
                if (balance <= 0) {
                    onInvalid("Card has zero balance.")
                    return@addOnSuccessListener
                }

                // Log a scan event in AuditLogs
                firestore.collection("AuditLogs").add(
                    mapOf(
                        "giftCardToken" to token,
                        "action" to "SCAN",
                        "deductedAmount" to 0,
                        "previousBalance" to balance,
                        "newBalance" to balance,
                        "deviceId" to "DEVICE_ID_PLACEHOLDER",
                        "timestamp" to Timestamp(Date())
                    )
                )

                onValid(balance)
            }
            .addOnFailureListener {
                onInvalid("Error fetching card: ${it.message}")
            }
    }
}

@Composable
fun ValidCardScreen(
    token: String,
    remainingBalanceCents: Int,
    onSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var deductAmountStr by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    val formattedBalance = String.format(Locale.getDefault(), "%.2f", remainingBalanceCents / 100.0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE8F5E9)) // Light Green
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.CheckCircle, contentDescription = "Valid", tint = Color(0xFF2E7D32), modifier = Modifier.size(80.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("VERIFIED", color = Color(0xFF2E7D32), fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Remaining Balance", style = MaterialTheme.typography.titleMedium)
                Text("$formattedBalance €", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = deductAmountStr,
            onValueChange = { input ->
                if (input.matches(Regex("^\\d*[.,]?\\d{0,2}$"))) {
                    deductAmountStr = input.replace(',', '.')
                }
            },
            label = { Text("Amount to Deduct") },
            placeholder = { Text("0.00") },
            trailingIcon = { Text("€", modifier = Modifier.padding(end = 12.dp)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val amountDouble = deductAmountStr.toDoubleOrNull()
                if (amountDouble == null || amountDouble <= 0) {
                    Toast.makeText(context, "Enter a valid amount", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                
                val deductCents = (amountDouble * 100).toInt()
                if (deductCents > remainingBalanceCents) {
                    Toast.makeText(context, "Deduction exceeds balance", Toast.LENGTH_LONG).show()
                    return@Button
                }

                isSubmitting = true
                processTransaction(context, token, deductCents, onSuccess, onFailure = { isSubmitting = false })
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = !isSubmitting,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            if (isSubmitting) CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
            else Text("Confirm Deduction", fontSize = 18.sp)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("Cancel", fontSize = 18.sp)
        }
    }
}

private fun processTransaction(
    context: Context,
    token: String,
    deductCents: Int,
    onSuccess: () -> Unit,
    onFailure: () -> Unit
) {
    val db = Firebase.firestore
    val docRef = db.collection("GiftCards").document(token)

    db.runTransaction { transaction ->
        val snapshot = transaction.get(docRef)
        
        // A) Re-read document to ensure balance hasn't changed
        val currentBalance = snapshot.getLong("remainingBalance")?.toInt() ?: throw Exception("Balance fetch failed")
        
        // B) Verify deduction amount
        if (deductCents > currentBalance) {
            throw Exception("Insufficient funds. Current balance: ${currentBalance / 100.0} €")
        }

        val newBalance = currentBalance - deductCents
        val newStatus = if (newBalance == 0) "REDEEMED" else "ACTIVE"

        // C) Update GiftCard balance
        transaction.update(docRef, "remainingBalance", newBalance)
        transaction.update(docRef, "status", newStatus)

        // D) Write to AuditLogs
        val auditRef = db.collection("AuditLogs").document()
        transaction.set(auditRef, mapOf(
            "giftCardToken" to token,
            "action" to "REDEEM",
            "deductedAmount" to deductCents,
            "previousBalance" to currentBalance,
            "newBalance" to newBalance,
            "deviceId" to "DEVICE_ID_PLACEHOLDER",
            "timestamp" to Timestamp(Date())
        ))
        
        null 
    }.addOnSuccessListener {
        onSuccess()
    }.addOnFailureListener { e ->
        Toast.makeText(context, "Transaction Failed: ${e.message}", Toast.LENGTH_LONG).show()
        onFailure()
    }
}

@Composable
fun InvalidCardScreen(reason: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFEBEE)) // Light Red
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.Warning, contentDescription = "Invalid", tint = Color(0xFFC62828), modifier = Modifier.size(80.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("INVALID CARD", color = Color(0xFFC62828), fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text(reason, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onRetry, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))) {
            Text("Scan Again", fontSize = 18.sp)
        }
    }
}

@Composable
fun SuccessScreen(onDone: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.CheckCircle, contentDescription = "Success", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(80.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Transaction Complete", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onDone, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("Ready for Next Customer", fontSize = 18.sp)
        }
    }
}
