package com.prog7313.budgetapp.ui.theme

import androidx.compose.ui.graphics.Color

// ── Brand palette (inspired by the FinWise screenshots) ──────────────────────
val DarkNavy       = Color(0xFF1A2332)   // Sidebar background
val DarkNavyLight  = Color(0xFF243447)   // Selected nav item
val AccentBlue     = Color(0xFF4A90D9)   // Primary action colour
val AccentGreen    = Color(0xFF4CAF50)   // Income / positive
val AccentOrange   = Color(0xFFFF9800)   // Warning / near limit
val AccentRed      = Color(0xFFF44336)   // Over budget
val AccentPurple   = Color(0xFF9C27B0)   // Savings goals
val SurfaceCard    = Color(0xFF1E2D3E)   // Card backgrounds (dark)
val SurfaceLight   = Color(0xFFF5F7FA)   // Light mode background
val TextPrimary    = Color(0xFF1A2332)
val TextSecondary  = Color(0xFF6B7A8D)
val White          = Color(0xFFFFFFFF)
val Divider        = Color(0xFFE8EDF2)

// Category colour presets exposed to the UI
val CategoryColors = listOf(
    "#4A90D9", "#4CAF50", "#FF9800", "#F44336",
    "#9C27B0", "#00BCD4", "#FF5722", "#607D8B",
    "#E91E63", "#3F51B5", "#009688", "#FFC107"
)