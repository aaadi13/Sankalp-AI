package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Profile
import com.example.ui.viewmodel.AppViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumSettingsScreen(
    viewModel: AppViewModel,
    profile: Profile
) {
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var selectedTierName by remember { mutableStateOf("") }
    var selectedTierPrice by remember { mutableStateOf("") }
    
    // Simulating Secure Checkout Flow States
    var paymentInProgress by remember { mutableStateOf(false) }
    var paymentSuccess by remember { mutableStateOf(false) }
    var cardName by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCVV by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Core Header introducing premium plans
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "🏆 Sankalp Pro 🏆",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Unlock the ultimate self-study power tool for Indian government competitive exams.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp, bottom = 8.dp)
                )

                // Main Subscription state card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (profile.isPremium) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Current Subscription State",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (profile.isPremium) "SANKALP PRO ENROLLED" else "FREE STARTER PLAN",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = if (profile.isPremium) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            if (profile.isPremium) {
                                Text(
                                    text = "Active Billing Model: ${profile.subscriptionTier}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Switch toggle for developers to easily simulate unlocking for verification testing
                        Button(
                            onClick = { viewModel.togglePremiumStatus() },
                            modifier = Modifier.testTag("admin_premium_bypass"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (profile.isPremium) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text(text = if (profile.isPremium) "Reset Free" else "Unlock Pro")
                        }
                    }
                }
            }
        }

        // Comparative features grid
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Why Upgrade to Sankalp Pro?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Custom grid of benefits
                    val features = listOf(
                        "Unlimited Gemini study plan generations" to "Free plan limits you to 2 daily planner AI updates",
                        "Unlimited AI text-to-revision notes" to "Free plan is capped at 2 text summaries",
                        "Smart Analytics & Weak Topic tracker" to "Basic progress chart logging",
                        "Offline flashcard practice slates" to "Standard view limits"
                    )

                    features.forEachIndexed { i, pair ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "check", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = pair.first,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = pair.second,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (i < features.size - 1) {
                            Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }

        // Tiers selections details
        item {
            Text(
                text = "Choose Your Preparation Advantage",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Tier Monthly
        item {
            PlanCard(
                tierName = "Monthly Starter Advantage",
                price = "₹149",
                validity = "per month",
                tagline = "Perfect to try out before a specific exam chapter test.",
                featuresList = listOf("Unlimited AI schedule generations", "Unlimited AI revision summaries", "Mock progress diagnostics metrics"),
                onSelect = {
                    selectedTierName = "Monthly Starter Advantage"
                    selectedTierPrice = "₹149"
                    paymentSuccess = false
                    showCheckoutDialog = true
                },
                isCurrent = profile.isPremium && profile.subscriptionTier.contains("Monthly")
            )
        }

        // Tier Yearly
        item {
            PlanCard(
                tierName = "Yearly Pro Topper Plan",
                price = "₹399",
                validity = "per year (Mega Value)",
                tagline = "Absolute favorite for serious aspirants looking to crush exams.",
                featuresList = listOf("All Monthly features included", "Advanced diagnostic weak topic cloud tagging", "VIP priority response speeds"),
                onSelect = {
                    selectedTierName = "Yearly Pro Topper Plan"
                    selectedTierPrice = "₹399"
                    paymentSuccess = false
                    showCheckoutDialog = true
                },
                isCurrent = profile.isPremium && profile.subscriptionTier.contains("Yearly")
            )
        }
    }

    // Interactive Checkout Dialog simulating payment gateway!
    if (showCheckoutDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!paymentInProgress) showCheckoutDialog = false
            },
            title = {
                Text(
                    text = if (paymentSuccess) "Payment Completed 🎉" else "Simulated Secure Checkout",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                if (paymentSuccess) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            shape = CircleShape,
                            modifier = Modifier.size(60.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "✓", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Welcome to Sankalp Pro!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Your account has been successfully upgraded to the $selectedTierName. Unlimited AI-powered notes and smart daily planners are now completely unlocked.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Upgrading to: $selectedTierName",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Amount Due: $selectedTierPrice (INR)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )

                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        Text(
                            text = "Simulated Sandbox Cards: Use any dummy details to complete checkout.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = cardName,
                            onValueChange = { cardName = it },
                            label = { Text("Aspirant Cardholder Name") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = cardNumber,
                            onValueChange = { cardNumber = it },
                            label = { Text("16-Digit Card Number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = cardExpiry,
                                onValueChange = { cardExpiry = it },
                                label = { Text("Expiry (MM/YY)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = cardCVV,
                                onValueChange = { cardCVV = it },
                                label = { Text("CVV") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (paymentInProgress) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Verifying Secure Bank gateway...", fontSize = 12.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (paymentSuccess) {
                    Button(
                        onClick = {
                            viewModel.upgradeToPremium(selectedTierName)
                            showCheckoutDialog = false
                            paymentSuccess = false
                            cardName = ""
                            cardNumber = ""
                            cardExpiry = ""
                            cardCVV = ""
                        },
                        modifier = Modifier.testTag("checkout_success_close")
                    ) {
                        Text("Let's Get Started!")
                    }
                } else {
                    Button(
                        onClick = {
                            paymentInProgress = true
                            // Simulate gateway delay in coroutine
                            coroutineScope.launch {
                                delay(1800L)
                                paymentInProgress = false
                                paymentSuccess = true
                            }
                        },
                        enabled = !paymentInProgress && cardName.isNotBlank() && cardNumber.isNotBlank(),
                        modifier = Modifier.testTag("checkout_pay_button")
                    ) {
                        Text("Pay $selectedTierPrice")
                    }
                }
            },
            dismissButton = {
                if (!paymentSuccess) {
                    TextButton(
                        onClick = { showCheckoutDialog = false },
                        enabled = !paymentInProgress
                    ) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}

@Composable
fun PlanCard(
    tierName: String,
    price: String,
    validity: String,
    tagline: String,
    featuresList: List<String>,
    onSelect: () -> Unit,
    isCurrent: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(28.dp),
        border = if (isCurrent) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tierName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (isCurrent) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    ) {
                        Text(
                            text = "Current Plan",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(vertical = 4.dp)) {
                Text(
                    text = price,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = " / $validity",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
                )
            }

            Text(
                text = tagline,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            featuresList.forEach { f ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = "feature supported icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = f, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onSelect,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isCurrent,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCurrent) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(text = if (isCurrent) "Plan Active" else "Upgrade Now")
            }
        }
    }
}
