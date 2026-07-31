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

                    if (isLocked) {
                        AppLockOverlayScreen(
                            onUnlockSubmit = { pin -> viewModel.unlockApp(pin) }
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
        NavNavItem("notes", "الملاحظات", Icons.Default.EditNote),
        NavNavItem("reminders", "التذكيرات", Icons.Default.Notifications),
        NavNavItem("debts", "الديون", Icons.Default.AccountBalanceWallet),
        NavNavItem("savings", "التحويش", Icons.Default.Savings)
    )

    val secondaryNavItems = listOf(
        NavNavItem("ai_consultant", "المستشار الصوتي 🎙️", Icons.Default.RecordVoiceOver),
        NavNavItem("links", "الروابط المهمة", Icons.Default.Link),
        NavNavItem("documents", "الملفات والوثائق", Icons.Default.Folder),
        NavNavItem("receipts", "الصور والفواتير", Icons.Default.ReceiptLong),
        NavNavItem("backup", "النسخ الاحتياطي", Icons.Default.CloudSync),
        NavNavItem("security", "الأمان والقفل", Icons.Default.Security)
    )

    val currentTitle = when (currentRoute) {
        "dashboard" -> "الرئيسية والإحصائيات"
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
        else -> "منظم حياتي"
    }

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
                    Text(
                        text = "منظم حياتي 🌟",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(12.dp)
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

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
                                navController.navigate(item.route) {
                                    popUpTo("dashboard") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
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
                                navController.navigate(item.route) {
                                    popUpTo("dashboard") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
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
                        IconButton(onClick = { navController.navigate("security") }) {
                            Icon(Icons.Default.Lock, contentDescription = "الأمان")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate("ai_consultant") },
                    icon = { Icon(Icons.Default.RecordVoiceOver, contentDescription = "المستشار الصوتي") },
                    text = { Text("المستشار الصوتي 🎙️") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
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
                                navController.navigate(item.route) {
                                    popUpTo("dashboard") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
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
}
