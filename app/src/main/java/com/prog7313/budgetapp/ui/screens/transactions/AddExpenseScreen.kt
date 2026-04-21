package com.prog7313.budgetapp.ui.screens.transactions

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.prog7313.budgetapp.viewmodel.AppViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AddExpenseScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val state       by viewModel.state.collectAsStateWithLifecycle()
    val context     = LocalContext.current
    val scrollState = rememberScrollState()

    // Form state
    var amount      by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date        by remember { mutableStateOf(LocalDate.now().toString()) }
    var categoryId  by remember { mutableStateOf("") }
    var expanded    by remember { mutableStateOf(false) }
    var receiptUri  by remember { mutableStateOf<Uri?>(null) }
    var receiptFile by remember { mutableStateOf<File?>(null) }
    var showPhotoOptions by remember { mutableStateOf(false) }

    // Validation
    var amountError    by remember { mutableStateOf(false) }
    var categoryError  by remember { mutableStateOf(false) }

    // Camera permission
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    // Camera launcher (take a photo)
    val cameraUri   = remember { mutableStateOf<Uri?>(null) }
    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            receiptUri  = cameraUri.value
            receiptFile = receiptUri?.toFile(context)
        }
    }

    // Gallery launcher (pick from library)
    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            receiptUri  = uri
            receiptFile = uri.toFile(context)
        }
    }

    // Date picker
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        date = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.systemDefault()).toLocalDate().toString()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Expense", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Amount
            OutlinedTextField(
                value         = amount,
                onValueChange = { amount = it; amountError = false },
                label         = { Text("Amount (R)") },
                leadingIcon   = { Icon(Icons.Default.CurrencyExchange, null) },
                isError       = amountError,
                supportingText = if (amountError) {{ Text("Please enter a valid amount") }} else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth()
            )

            // Description
            OutlinedTextField(
                value         = description,
                onValueChange = { description = it },
                label         = { Text("Description") },
                leadingIcon   = { Icon(Icons.Default.Description, null) },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth()
            )

            // Date picker button
            OutlinedTextField(
                value         = date,
                onValueChange = {},
                label         = { Text("Date") },
                leadingIcon   = { Icon(Icons.Default.CalendarToday, null) },
                readOnly      = true,
                trailingIcon  = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.EditCalendar, null)
                    }
                },
                modifier      = Modifier.fillMaxWidth()
            )

            // Category dropdown
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value         = state.categories.find { it.id == categoryId }
                        ?.let { "${it.icon} ${it.name}" } ?: "",
                    onValueChange = {},
                    readOnly      = true,
                    label         = { Text("Category") },
                    isError       = categoryError,
                    supportingText = if (categoryError) {{ Text("Please select a category") }} else null,
                    trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier      = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    state.categories.forEach { cat ->
                        DropdownMenuItem(
                            text    = { Text("${cat.icon} ${cat.name}") },
                            onClick = { categoryId = cat.id; expanded = false; categoryError = false }
                        )
                    }
                    if (state.categories.isEmpty()) {
                        DropdownMenuItem(
                            text    = { Text("No categories – create one in Budget tab") },
                            onClick = { expanded = false }
                        )
                    }
                }
            }

            // Receipt photo
            Text("Receipt Photo (optional)", style = MaterialTheme.typography.labelMedium)
            if (receiptUri != null) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showPhotoOptions = true }
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(receiptUri),
                        contentDescription = "Receipt",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        Modifier.align(Alignment.TopEnd).padding(8.dp)
                            .clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surface.copy(0.8f))
                            .padding(4.dp)
                    ) {
                        Icon(Icons.Default.Edit, null, Modifier.size(16.dp))
                    }
                }
            } else {
                OutlinedButton(
                    onClick  = { showPhotoOptions = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CameraAlt, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Attach Receipt")
                }
            }

            Spacer(Modifier.height(8.dp))

            // Save button
            Button(
                onClick  = {
                    amountError   = amount.toDoubleOrNull() == null || amount.toDoubleOrNull()!! <= 0
                    categoryError = categoryId.isBlank()
                    if (!amountError && !categoryError) {
                        viewModel.createExpense(
                            amount      = amount.toDouble(),
                            date        = date,
                            description = description,
                            categoryId  = categoryId,
                            receiptFile = receiptFile
                        )
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Save Expense", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Photo source options dialog
    if (showPhotoOptions) {
        AlertDialog(
            onDismissRequest = { showPhotoOptions = false },
            title   = { Text("Add Receipt Photo") },
            text    = {
                Column {
                    TextButton(
                        onClick = {
                            showPhotoOptions = false
                            if (cameraPermission.status.isGranted) {
                                val file = createImageFile(context)
                                receiptFile = file
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                cameraUri.value = uri
                                takePicture.launch(uri)
                            } else {
                                cameraPermission.launchPermissionRequest()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CameraAlt, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Take Photo")
                    }
                    TextButton(
                        onClick = { showPhotoOptions = false; pickImage.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PhotoLibrary, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Choose from Gallery")
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showPhotoOptions = false }) { Text("Cancel") } }
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun createImageFile(context: Context): File {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = context.cacheDir
    return File.createTempFile("RECEIPT_${timestamp}_", ".jpg", storageDir)
}

private fun Uri.toFile(context: Context): File? = try {
    val inputStream = context.contentResolver.openInputStream(this) ?: return null
    val file = createImageFile(context)
    file.outputStream().use { inputStream.copyTo(it) }
    file
} catch (e: Exception) { null }