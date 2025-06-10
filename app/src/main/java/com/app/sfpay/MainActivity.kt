package com.app.sfpay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings

import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.sfpay.ui.MainViewModel
import com.app.sfpay.ui.theme.SfpayTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SfpayTheme {
                PaymentApp()
            }
        }
    }
}

enum class AppScreen {
    HOME, PROFILE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentApp() {
    val viewModel: MainViewModel = viewModel()
    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
    
    // Run SFE checks silently in background on app start
    LaunchedEffect(Unit) {
        viewModel.initializeSfeChecks()
    }
    
    when (currentScreen) {
        AppScreen.HOME -> {
            HomeScreen(
                viewModel = viewModel,
                onProfileClick = { currentScreen = AppScreen.PROFILE }
            )
        }
        AppScreen.PROFILE -> {
            ProfileScreen(
                viewModel = viewModel,
                onBackClick = { currentScreen = AppScreen.HOME }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onProfileClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Home,
                        contentDescription = "SFPay",
                        tint = Color(0xFF6200EE)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "SFPay",
                        color = Color(0xFF6200EE),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            actions = {
                IconButton(onClick = onProfileClick) {
                    Icon(Icons.Default.Person, contentDescription = "Profile")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Security Status Indicator (subtle)
            item {
                SecurityStatusBanner(viewModel)
            }
            
            // Balance Card
            item {
                BalanceCard()
            }
            
            // Quick Actions
            item {
                QuickActionsSection(viewModel)
            }
            
            // Recent Transactions
            item {
                RecentTransactionsSection()
            }
        }
    }
}

@Composable
fun SecurityStatusBanner(viewModel: MainViewModel) {
    val securityStatus by viewModel.securityStatus.collectAsState()
    
    if (securityStatus.showBanner) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = when (securityStatus.overallStatus) {
                    "secure" -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                    "warning" -> Color(0xFFFF9800).copy(alpha = 0.1f)
                    else -> Color(0xFFF44336).copy(alpha = 0.1f)
                }
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        when (securityStatus.overallStatus) {
                            "secure" -> Icons.Default.CheckCircle
                            "warning" -> Icons.Default.Warning
                            else -> Icons.Default.Close
                        },
                        contentDescription = "Security Status",
                        tint = when (securityStatus.overallStatus) {
                            "secure" -> Color(0xFF4CAF50)
                            "warning" -> Color(0xFFFF9800)
                            else -> Color(0xFFF44336)
                        },
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        securityStatus.message,
                        fontSize = 12.sp,
                        color = Color.Black.copy(alpha = 0.8f)
                    )
                }
                
                Icon(
                    Icons.Default.Info,
                    contentDescription = "Info",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit
) {
    val securityStatus by viewModel.securityStatus.collectAsState()
    val detailedSecurityInfo by viewModel.detailedSecurityInfo.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top App Bar with Back Button
        TopAppBar(
            title = {
                Text(
                    "Profile & Settings",
                    color = Color(0xFF6200EE),
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Profile Section
            item {
                UserProfileCard()
            }
            
            // SFE Security Status Section
            item {
                SfeSecurityStatusCard(securityStatus, detailedSecurityInfo, viewModel)
            }
            
            // Settings Section
            item {
                SettingsSection()
            }
        }
    }
}

@Composable
fun UserProfileCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF6200EE)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "John Doe",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                "john.doe@example.com",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
            
            Text(
                "SFPay Premium Member",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SfeSecurityStatusCard(
    securityStatus: MainViewModel.SecurityStatus,
    detailedInfo: MainViewModel.DetailedSecurityInfo,
    viewModel: MainViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Security",
                    tint = Color(0xFF6200EE),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "SFE Security Status",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Overall Status
            SecurityStatusItem(
                label = "Overall Security",
                status = securityStatus.overallStatus,
                description = securityStatus.message
            )
            
            Divider(modifier = Modifier.padding(vertical = 12.dp))
            
            // Detailed Security Checks
            Text(
                "Security Checks",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            SecurityCheckItem(
                label = "Device Binding",
                status = detailedInfo.deviceBinding,
                description = "SIM card and network verification"
            )
            
            SecurityCheckItem(
                label = "Root Detection",
                status = detailedInfo.rootDetection,
                description = "System integrity and root access check"
            )
            
            SecurityCheckItem(
                label = "Debug Detection",
                status = detailedInfo.debugDetection,
                description = "Development tools and debugging check"
            )
            
            SecurityCheckItem(
                label = "Tamper Detection",
                status = detailedInfo.tamperDetection,
                description = "App modification and signature verification"
            )
            
            SecurityCheckItem(
                label = "Play Integrity",
                status = detailedInfo.playIntegrity,
                description = "Google Play Store app authenticity"
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.refreshSecurityStatus() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Refresh Status")
                }
                
                OutlinedButton(
                    onClick = { viewModel.checkBackendStatus() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Check Backend")
                }
            }
        }
    }
}

@Composable
fun SecurityStatusItem(
    label: String,
    status: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Text(
                description,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                when (status) {
                    "secure", "passed" -> Icons.Default.CheckCircle
                    "warning" -> Icons.Default.Warning
                    else -> Icons.Default.Close
                },
                contentDescription = status,
                tint = when (status) {
                    "secure", "passed" -> Color(0xFF4CAF50)
                    "warning" -> Color(0xFFFF9800)
                    else -> Color(0xFFF44336)
                },
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                status.capitalize(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = when (status) {
                    "secure", "passed" -> Color(0xFF4CAF50)
                    "warning" -> Color(0xFFFF9800)
                    else -> Color(0xFFF44336)
                }
            )
        }
    }
}

@Composable
fun SecurityCheckItem(
    label: String,
    status: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Text(
                description,
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                when (status) {
                    "passed", "secure" -> Icons.Default.CheckCircle
                    "warning" -> Icons.Default.Warning
                    "failed", "insecure" -> Icons.Default.Close
                    else -> Icons.Default.Info
                },
                contentDescription = status,
                tint = when (status) {
                    "passed", "secure" -> Color(0xFF4CAF50)
                    "warning" -> Color(0xFFFF9800)
                    "failed", "insecure" -> Color(0xFFF44336)
                    else -> Color.Gray
                },
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                when (status) {
                    "passed" -> "Yes"
                    "failed" -> "No"
                    "pending" -> "..."
                    else -> status.capitalize()
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = when (status) {
                    "passed", "secure" -> Color(0xFF4CAF50)
                    "warning" -> Color(0xFFFF9800)
                    "failed", "insecure" -> Color(0xFFF44336)
                    else -> Color.Gray
                }
            )
        }
    }
}

@Composable
fun SettingsSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color(0xFF6200EE),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Settings",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SettingsItem("Privacy & Security", "Manage your privacy preferences")
            SettingsItem("Notifications", "Configure app notifications")
            SettingsItem("Payment Methods", "Manage cards and accounts")
            SettingsItem("Transaction History", "View all transactions")
            SettingsItem("Help & Support", "Get help and contact support")
            SettingsItem("About SFPay", "App version and information")
        }
    }
}

@Composable
fun SettingsItem(title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle settings item click */ }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Text(
                description,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        Icon(
            Icons.Default.ArrowForward,
            contentDescription = "Navigate",
            tint = Color.Gray,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun BalanceCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF6200EE)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        "Total Balance",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                    Text(
                        "₹ 25,420.50",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    Icons.Default.Star,
                    contentDescription = "Show/Hide Balance",
                    tint = Color.White,
                    modifier = Modifier.clickable { /* Toggle balance visibility */ }
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "•••• •••• •••• 4829",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp
                )
                Text(
                    "SFPay Premium",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun QuickActionsSection(viewModel: MainViewModel) {
    Column {
        Text(
            "Quick Actions",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickActionItem(
                icon = Icons.Default.Send,
                label = "Send Money",
                onClick = { viewModel.initiatePayment(1500.0, "John Doe") }
            )
            QuickActionItem(
                icon = Icons.Default.Search,
                label = "Scan QR",
                onClick = { viewModel.scanQrCode() }
            )
            QuickActionItem(
                icon = Icons.Default.Call,
                label = "Pay Bills",
                onClick = { viewModel.openBillPayment() }
            )
            QuickActionItem(
                icon = Icons.Default.Person,
                label = "Cards",
                onClick = { viewModel.openCardManagement() }
            )
        }
    }
}

@Composable
fun QuickActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Color(0xFFF3F3F3)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = Color(0xFF6200EE),
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            label,
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun RecentTransactionsSection() {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Recent Transactions",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            TextButton(onClick = { /* View all transactions */ }) {
                Text("View All", color = Color(0xFF6200EE))
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        val transactions = listOf(
            Transaction("Grocery Store", "Today", "-₹ 1,250", TransactionType.DEBIT),
            Transaction("John Doe", "Yesterday", "+₹ 2,500", TransactionType.CREDIT),
            Transaction("Electric Bill", "2 days ago", "-₹ 890", TransactionType.DEBIT),
            Transaction("Salary Credit", "5 days ago", "+₹ 45,000", TransactionType.CREDIT)
        )
        
        transactions.forEach { transaction ->
            TransactionItem(transaction)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (transaction.type == TransactionType.CREDIT) 
                                Color(0xFF4CAF50).copy(alpha = 0.1f)
                            else 
                                Color(0xFFF44336).copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (transaction.type == TransactionType.CREDIT) 
                            Icons.Default.ArrowForward 
                        else 
                            Icons.Default.ArrowBack,
                        contentDescription = transaction.type.name,
                        tint = if (transaction.type == TransactionType.CREDIT) 
                            Color(0xFF4CAF50) 
                        else 
                            Color(0xFFF44336),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        transaction.title,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Text(
                        transaction.date,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            
            Text(
                transaction.amount,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = if (transaction.type == TransactionType.CREDIT) 
                    Color(0xFF4CAF50) 
                else 
                    Color(0xFFF44336)
            )
        }
    }
}

data class Transaction(
    val title: String,
    val date: String,
    val amount: String,
    val type: TransactionType
)

enum class TransactionType {
    CREDIT, DEBIT
}