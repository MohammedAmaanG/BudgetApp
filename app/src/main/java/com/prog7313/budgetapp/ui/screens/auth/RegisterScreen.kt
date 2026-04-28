package com.prog7313.budgetapp.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prog7313.budgetapp.R
import com.prog7313.budgetapp.ui.theme.AccentBlue
import com.prog7313.budgetapp.ui.theme.DarkNavy
import com.prog7313.budgetapp.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) onRegisterSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DarkNavy, AccentBlue.copy(alpha = 0.85f))))
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
                contentDescription = "BudgetTracker Logo",
                modifier           = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(20.dp))
            )

            /*
            Title: Compose — painterResource for loading drawable images
            Author(s): Android Developers
            Date: 2024
            Version: Compose BOM 2024.06.00
            Type: Documentation
            Availability: https://developer.android.com/develop/ui/compose/graphics/images/loading
            */

            Spacer(Modifier.height(12.dp))

            Text(
                text       = "Budget Tracker",
                style      = MaterialTheme.typography.headlineLarge,
                color      = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text  = "Start your financial journey",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
            )

            Spacer(Modifier.height(28.dp))

            Card(
                shape     = RoundedCornerShape(24.dp),
                colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(Modifier.padding(24.dp)) {

                    Text(
                        "Create account",
                        style      = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Fill in your details below",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
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
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        singleLine      = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier        = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value                = uiState.confirmPassword,
                        onValueChange        = viewModel::onConfirmPasswordChange,
                        label                = { Text("Confirm password") },
                        leadingIcon          = { Icon(Icons.Default.Lock, null) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine           = true,
                        keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier             = Modifier.fillMaxWidth()
                    )

                    uiState.error?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            it,
                            color     = MaterialTheme.colorScheme.error,
                            style     = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier  = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick  = viewModel::register,
                        enabled  = !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape    = RoundedCornerShape(12.dp)
                    ) {
                        if (uiState.isLoading)
                            CircularProgressIndicator(
                                Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        else
                            Text("Create Account", fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(12.dp))
                    TextButton(
                        onClick  = onNavigateToLogin,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Already have an account? Sign in", textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

/*
Title: Compose — LaunchedEffect for navigation side-effects
Author(s): Android Developers
Date: 2024
Version: Compose BOM 2024.06.00
Type: Documentation
Availability: https://developer.android.com/develop/ui/compose/side-effects#launchedeffect
*/