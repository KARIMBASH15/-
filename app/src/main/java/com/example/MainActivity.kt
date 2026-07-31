package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.MainViewModel
import com.example.ui.screens.*
import com.example.ui.theme.LifeOrganizerTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LifeOrganizerTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val isLocked by viewModel.isAppLocked.collectAsState()
                    val isLoggedIn by viewModel.isUserLoggedIn.collectAsState()

                    if (isLocked) {
                        AppLockOverlayScreen(
                            onUnlockSubmit = { pin -> viewModel.unlockApp(pin) }
                        )
                    } else if (!isLoggedIn) {
                        AuthScreen(
                            viewModel = viewModel,
                            onLoginSuccess = {}
                        )
                    } else {
                        MainAppContent(viewModel = viewModel)
                    }

                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.lockApp()
    }
}

data class NavNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "dashboard"

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val primaryNavItems = listOf(
        NavNavItem("dashboard", "الرئيسية", Icons.Default.Dashboard),
        NavNavItem("quran", "الوردي والمصحف", Icons.Default.MenuBook),
        NavNavItem("azkar", "الأذكار والفوائد", Icons.Default.Mosque),
        NavNavItem("ai_consultant", "المستشار", Icons.Default.RecordVoiceOver),
        NavNavItem("notes", "الملاحظات", Icons.Default.EditNote),
        NavNavItem("reminders", "التذكيرات", Icons.Default.Notifications)
    )

    val secondaryNavItems = listOf(
        NavNavItem("debts", "الديون", Icons.Default.AccountBalanceWallet),
        NavNavItem("savings", "التحويش", Icons.Default.Savings),
        NavNavItem("links", "الروابط المهمة", Icons.Default.Link),
        NavNavItem("documents", "الملفات والوثائق", Icons.Default.Folder),
        NavNavItem("receipts", "الصور والفواتير", Icons.Default.ReceiptLong),
        NavNavItem("backup", "النسخ الاحتياطي", Icons.Default.CloudSync),
        NavNavItem("security", "الأمان والقفل", Icons.Default.Security),
        NavNavItem("about", "عن التطبيق والمطور", Icons.Default.Info)
    )

    val currentTitle = when (currentRoute) {
        "dashboard" -> "الرئيسية والإحصائيات"
        "quran" -> "الوردي اليومي والمصحف الشريف 📖"
        "azkar" -> "حصن المسلم والأذكار والفوائد 📿"
        "ai_consultant" -> "المستشار الصوتي 🎙️ (كريم الفردي)"
        "notes" -> "الملاحظات والمدونات"
        "reminders" -> "التذكيرات والمهام"
        "debts" -> "إدارة الديون"
        "savings" -> "صندوق التحويش"
        "links" -> "الروابط المهمة"
        "documents" -> "الملفات والوثائق"
        "receipts" -> "الصور والفواتير"
        "backup" -> "النسخ الاحتياطي"
        "security" -> "إعدادات الأمان والقفل"
        "about" -> "عن التطبيق والاتصال بالمطور ℹ️"
        else -> "منظم حياتي"
    }

    val unreadCount by viewModel.unreadNotificationCount.collectAsState()
    val notificationsList by viewModel.allNotifications.collectAsState()
    var showNotificationsDialog by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .width(280.dp)
                        .fillMaxHeight()
                        .padding(16.dp)
                ) {
                    val currentUsername by viewModel.currentUsername.collectAsState()
                    val currentUserRole by viewModel.currentUserRole.collectAsState()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "منظم حياتي 🌟",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Text(
                                text = "الحساب: $currentUsername",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        IconButton(onClick = { viewModel.logoutUser() }) {
                            Icon(Icons.Default.Logout, contentDescription = "خروج", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))


                    Text(
                        text = "الأقسام الرئيسية",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )

                    primaryNavItems.forEach { item ->
                        NavigationDrawerItem(
                            label = { Text(item.title) },
                            icon = { Icon(item.icon, contentDescription = null) },
                            selected = currentRoute == item.route,
                            onClick = {
                                if (item.route == "dashboard") {
                                    navController.navigate("dashboard") {
                                        popUpTo(navController.graph.startDestinationId) { inclusive = false }
                                        launchSingleTop = true
                                    }
                                } else if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "الأدوات والخدمات",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )

                    secondaryNavItems.forEach { item ->
                        NavigationDrawerItem(
                            label = { Text(item.title) },
                            icon = { Icon(item.icon, contentDescription = null) },
                            selected = currentRoute == item.route,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = currentTitle,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "القائمة")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            showNotificationsDialog = true
                            viewModel.markAllNotificationsAsRead()
                        }) {
                            BadgedBox(
                                badge = {
                                    if (unreadCount > 0) {
                                        Badge(containerColor = MaterialTheme.colorScheme.error) {
                                            Text("$unreadCount")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (unreadCount > 0) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                    contentDescription = "الإشعارات",
                                    tint = if (unreadCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        IconButton(onClick = { navController.navigate("security") }) {
                            Icon(Icons.Default.Lock, contentDescription = "الأمان")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    primaryNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                if (item.route == "dashboard") {
                                    navController.navigate("dashboard") {
                                        popUpTo(navController.graph.startDestinationId) { inclusive = false }
                                        launchSingleTop = true
                                    }
                                } else if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = "dashboard"
                ) {
                    composable("dashboard") {
                        DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToSection = { route -> navController.navigate(route) }
                        )
                    }
                    composable("quran") { QuranScreen(viewModel = viewModel) }
                    composable("azkar") { AzkarScreen(viewModel = viewModel) }
                    composable("about") { AboutScreen(viewModel = viewModel) }
                    composable("ai_consultant") { AiConsultantScreen(viewModel = viewModel) }
                    composable("notes") { NotesScreen(viewModel = viewModel) }
                    composable("reminders") { RemindersScreen(viewModel = viewModel) }
                    composable("debts") { DebtsScreen(viewModel = viewModel) }
                    composable("savings") { SavingsScreen(viewModel = viewModel) }
                    composable("links") { LinksScreen(viewModel = viewModel) }
                    composable("documents") { DocumentsScreen(viewModel = viewModel) }
                    composable("receipts") { ReceiptsScreen(viewModel = viewModel) }
                    composable("backup") { BackupRestoreScreen(viewModel = viewModel) }
                    composable("security") { SecurityLockScreen(viewModel = viewModel) }
                }
            }
        }
    }

    if (showNotificationsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("مركز الإشعارات والتنبيهات 🔔", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            },
            text = {
                if (notificationsList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("لا توجد إشعارات حالياً 📭", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 350.dp)
                    ) {
                        notificationsList.forEach { notification ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (notification.isRead) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = notification.title,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = notification.message,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "من: ${notification.sender}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Gray
                                        )
                                    }

                                    IconButton(onClick = { viewModel.deleteNotification(notification.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotificationsDialog = false }) {
                    Text("إغلاق")
                }
            }
        )
    }
}
