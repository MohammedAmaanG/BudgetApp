package com.prog7313.budgetapp.ui.components



import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.prog7313.budgetapp.ui.theme.AccentRed


@Composable
fun BudgetProgressBar(
    progress: Float,
    budgetLabel: String,
    spentLabel: String,
    availableLabel: String,
    isOverBudget: Boolean = false,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(600),
        label = "progress"
    )
    val barColor = when {
        isOverBudget        -> AccentRed
        progress > 0.85f    -> MaterialTheme.colorScheme.tertiary
        else                -> MaterialTheme.colorScheme.primary
    }

    Column(modifier) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(spentLabel, style = MaterialTheme.typography.bodySmall)
            Text(availableLabel, style = MaterialTheme.typography.bodySmall,
                color = if (isOverBudget) AccentRed else MaterialTheme.colorScheme.secondary)
        }
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(barColor)
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = budgetLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}


@Composable
fun SavingsProgressCircle(
    progress: Float,
    label: String,
    subLabel: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(800),
        label = "circleProgress"
    )
    Box(
        modifier.size(80.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxSize(),
            color = color,
            trackColor = color.copy(alpha = 0.15f),
            strokeWidth = 6.dp
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label,    fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(subLabel, fontSize = 9.sp,  color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
        }
    }
}


@Composable
fun CategoryChip(name: String, icon: String, colorHex: String) {
    val bgColor = try { Color(colorHex.toColorInt()) } catch (e: Exception) { MaterialTheme.colorScheme.primary }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(icon, fontSize = 14.sp)
        Spacer(Modifier.width(4.dp))
        Text(name, style = MaterialTheme.typography.labelSmall, color = bgColor)
    }
}

@Composable
fun AmountCard(label: String, amount: String, color: Color = MaterialTheme.colorScheme.primary) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(amount, style = MaterialTheme.typography.headlineMedium.copy(color = color))
        Text(label,  style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}


@Composable
fun SectionHeader(title: String, action: @Composable (() -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        action?.invoke()
    }
}


@Composable
fun ErrorSnackbar(error: String?, onDismiss: () -> Unit) {
    if (error != null) {
        Snackbar(
            action = { TextButton(onClick = onDismiss) { Text("Dismiss") } },
            modifier = Modifier.padding(8.dp)
        ) {
            Text(error)
        }
    }
}


@Composable
fun BadgeCard(icon: String, title: String, isEarned: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isEarned) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .padding(12.dp)
            .width(72.dp)
    ) {
        Text(
            text = icon,
            fontSize = 28.sp,
            color = if (isEarned) Color.Unspecified else Color.Gray.copy(alpha = 0.4f)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = if (isEarned) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurface.copy(0.4f)
        )
    }
}

