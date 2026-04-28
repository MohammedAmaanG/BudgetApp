package com.prog7313.budgetapp.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prog7313.budgetapp.R
import com.prog7313.budgetapp.ui.theme.AccentBlue
import com.prog7313.budgetapp.ui.theme.DarkNavy
import com.prog7313.budgetapp.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) onLoginSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(DarkNavy, AccentBlue.copy(alpha = 0.85f)))
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp)
                .padding(vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter            = painterResource(id = R.drawable.ic_app_logo),
                contentDescription = "FinWise Logo",
                modifier           = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(24.dp))
            )

            /*
            Title: Compose — Image composable with painterResource
            Author(s): Android Developers
            Date: 2024
            Version: Compose BOM 2024.06.00
            Type: Documentation
            Availability: https://developer.android.com/develop/ui/compose/graphics/images/loading
            */

            Spacer(Modifier.height(14.dp))

            Text(
                text       = "Budget Tracker",
                style      = MaterialTheme.typography.displayLarge,
                color      = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text  = "Smart budget tracking",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
            )

            Spacer(Modifier.height(32.dp))

            Card(
                shape     = RoundedCornerShape(24.dp),
                colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(Modifier.padding(24.dp)) {

                    Text(
                        text       = "Welcome back",
                        style      = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text  = "Sign in to your account",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.height(20.dp))

                    OutlinedTextField(
                        value           = uiState.email,
                        onValueChange   = viewModel::onEmailChange,
                        label           = { Text("Email address") },
                        leadingIcon     = { Icon(Icons.Default.Email, null) },
                        singleLine      = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier        = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value           = uiState.password,
                        onValueChange   = viewModel::onPasswordChange,
                        label           = { Text("Password") },
                        leadingIcon     = { Icon(Icons.Default.Lock, null) },
                        trailingIcon    = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.VisibilityOff
                                    else Icons.Default.Visibility,
                                    contentDescription = "Toggle password"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        singleLine      = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier        = Modifier.fillMaxWidth()
                    )

                    uiState.error?.let { error ->
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text      = error,
                            color     = MaterialTheme.colorScheme.error,
                            style     = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier  = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick  = viewModel::login,
                        enabled  = !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape    = RoundedCornerShape(12.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Sign In", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    TextButton(
                        onClick  = onNavigateToRegister,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Don't have an account? Register", textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

/*
Title: Compose — VisualTransformation for password masking
Author(s): Android Developers
Date: 2024
Version: Compose BOM 2024.06.00
Type: Documentation
Availability: https://developer.android.com/reference/kotlin/androidx/compose/ui/text/input/PasswordVisualTransformation
*/

/*
Title: Compose — Brush.verticalGradient for background
Author(s): Android Developers
Date: 2024
Version: Compose BOM 2024.06.00
Type: Documentation
Availability: https://developer.android.com/develop/ui/compose/graphics/draw/brush
*/