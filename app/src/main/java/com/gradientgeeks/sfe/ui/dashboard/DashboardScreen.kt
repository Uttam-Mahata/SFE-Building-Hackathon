package com.gradientgeeks.sfe.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gradientgeeks.csfe.SFEClientSDK
import com.gradientgeeks.csfe.transaction.Transaction
import com.gradientgeeks.csfe.transaction.TransactionHistoryResult
import com.gradientgeeks.csfe.transaction.TransactionType
import com.gradientgeeks.csfe.wallet.WalletResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Dashboard screen showing balance and recent transactions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onSendMoneyClick: () -> Unit,
    onTransactionsClick: () -> Unit
) {
    val sfeSDK = SFEClientSDK.getInstance()
    var balance by remember { mutableStateOf<Double?>(null) }
    var isLoadingBalance by remember { mutableStateOf(true) }
    var recentTransactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var isLoadingTransactions by remember { mutableStateOf(true) }
    
    // Load wallet balance
    LaunchedEffect(key1 = Unit) {
        sfeSDK.wallet().getBalance { result ->
            isLoadingBalance = false
            when (result) {
                is WalletResult.Success -> {
                    balance = result.walletData.balance
                }
                is WalletResult.Error -> {
                    // Handle error
                }
            }
        }
        
        // Load recent transactions
        sfeSDK.transactions().getHistory(
            com.gradientgeeks.csfe.transaction.TransactionFilter.Builder()
                .setPageSize(5)
                .build()
        ) { result ->
            isLoadingTransactions = false
            when (result) {
                is TransactionHistoryResult.Success -> {
                    recentTransactions = result.transactions
                }
                is TransactionHistoryResult.Error -> {
                    // Handle error
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SFE Payment App") },
                actions = {
                    IconButton(onClick = { /* Refresh data */ }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onSendMoneyClick() }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Send Money")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Balance card
            item {
                BalanceCard(
                    balance = balance,
                    isLoading = isLoadingBalance
                )
            }
            
            // Quick Actions
            item {
                QuickActions(
                    onSendMoneyClick = onSendMoneyClick,
                    onTransactionsClick = onTransactionsClick
                )
            }
            
            // Recent Transactions Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Text(
                        text = "See All",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onTransactionsClick() }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Recent Transactions
            if (isLoadingTransactions) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (recentTransactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No transactions yet")
                    }
                }
            } else {
                items(recentTransactions) { transaction ->
                    TransactionItem(transaction)
                }
            }
        }
    }
}

/**
 * Card showing wallet balance.
 */
@Composable
fun BalanceCard(
    balance: Double?,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Your Balance",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "₹ ${balance ?: 0.0}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ActionButton(
                    text = "Add Money",
                    icon = Icons.Default.Add,
                    onClick = { /* TODO */ }
                )
                
                ActionButton(
                    text = "Transfer",
                    icon = Icons.Default.ArrowForward,
                    onClick = { /* TODO */ }
                )
            }
        }
    }
}

/**
 * Action button for balance card.
 */
@Composable
private fun ActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

/**
 * Quick action buttons.
 */
@Composable
fun QuickActions(
    onSendMoneyClick: () -> Unit,
    onTransactionsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        QuickActionItem(
            icon = Icons.Default.CreditCard,
            text = "Send Money",
            onClick = onSendMoneyClick
        )
        
        QuickActionItem(
            icon = Icons.Default.QrCode,
            text = "Scan QR",
            onClick = { /* TODO */ }
        )
        
        QuickActionItem(
            icon = Icons.Default.History,
            text = "History",
            onClick = onTransactionsClick
        )
    }
}

/**
 * Individual quick action item.
 */
@Composable
fun QuickActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(8.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(28.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Single transaction item in the list.
 */
@Composable
fun TransactionItem(transaction: Transaction) {
    val dateFormatter = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Transaction type icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    when (transaction.type) {
                        TransactionType.UPI -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        TransactionType.WALLET -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                        else -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = transaction.recipientName.first().toString(),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Transaction details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.recipientName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            
            Text(
                text = dateFormatter.format(transaction.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
        
        // Amount
        Text(
            text = "₹ ${transaction.amount}",
            style = MaterialTheme.typography.bodyLarge,
            color = if (transaction.recipientId != "demouser@sfe") {
                Color.Red
            } else {
                Color.Green
            },
            fontWeight = FontWeight.Bold
        )
    }
}
