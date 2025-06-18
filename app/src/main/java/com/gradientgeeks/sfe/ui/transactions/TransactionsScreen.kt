package com.gradientgeeks.sfe.ui.transactions

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.gradientgeeks.csfe.transaction.TransactionFilter
import com.gradientgeeks.csfe.transaction.TransactionHistoryResult
import com.gradientgeeks.csfe.transaction.TransactionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Screen showing transaction history.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    onBackClick: () -> Unit
) {
    val sfeSDK = SFEClientSDK.getInstance()
    var transactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Filter state
    var selectedFilter by remember { mutableStateOf<TransactionType?>(null) }
    
    // Load transactions
    LaunchedEffect(key1 = selectedFilter) {
        isLoading = true
        
        val filter = TransactionFilter.Builder().apply {
            setPageSize(30)
            selectedFilter?.let { setTransactionTypes(listOf(it)) }
        }.build()
        
        sfeSDK.transactions().getHistory(filter) { result ->
            isLoading = false
            when (result) {
                is TransactionHistoryResult.Success -> {
                    transactions = result.transactions
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
                title = { Text("Transaction History") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Show filter options */ }) {
                        Icon(Icons.Default.Filter, contentDescription = "Filter")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Filter chips
            FilterChips(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
            )
            
            // Transactions
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No transactions found")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Group transactions by date
                    val grouped = transactions.groupBy { it.timestamp.toFormattedDateString() }
                    
                    grouped.forEach { (date, transactionsForDate) ->
                        // Date header
                        item {
                            Text(
                                text = date,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        
                        // Transactions for this date
                        items(transactionsForDate) { transaction ->
                            TransactionItem(transaction)
                        }
                        
                        // Divider between dates
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Filter chips for transaction types.
 */
@Composable
fun FilterChips(
    selectedFilter: TransactionType?,
    onFilterSelected: (TransactionType?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            text = "All",
            isSelected = selectedFilter == null,
            onClick = { onFilterSelected(null) }
        )
        
        FilterChip(
            text = "UPI",
            isSelected = selectedFilter == TransactionType.UPI,
            onClick = { onFilterSelected(TransactionType.UPI) }
        )
        
        FilterChip(
            text = "Wallet",
            isSelected = selectedFilter == TransactionType.WALLET,
            onClick = { onFilterSelected(TransactionType.WALLET) }
        )
        
        FilterChip(
            text = "Bank",
            isSelected = selectedFilter == TransactionType.BANK_TRANSFER,
            onClick = { onFilterSelected(TransactionType.BANK_TRANSFER) }
        )
    }
}

/**
 * Individual filter chip component.
 */
@Composable
fun FilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                   else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

/**
 * Individual transaction item.
 */
@Composable
fun TransactionItem(transaction: Transaction) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Transaction icon
            Box(
                modifier = Modifier
                    .size(48.dp)
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
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Transaction details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.recipientName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                
                Text(
                    text = transaction.timestamp.toFormattedTimeString(),
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                
                if (!transaction.description.isNullOrEmpty()) {
                    Text(
                        text = transaction.description,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
            
            // Amount
            Text(
                text = "₹ ${transaction.amount}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (transaction.recipientId != "demouser@sfe") {
                    Color.Red
                } else {
                    Color.Green
                }
            )
        }
    }
}

/**
 * Format Date to date string.
 */
fun Date.toFormattedDateString(): String {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return formatter.format(this)
}

/**
 * Format Date to time string.
 */
fun Date.toFormattedTimeString(): String {
    val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return formatter.format(this)
}
