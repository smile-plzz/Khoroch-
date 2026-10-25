package app.khoroch

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import app.khoroch.data.local.TagEntity
import app.khoroch.data.local.TransactionEntity
import app.khoroch.data.repository.PreferencesRepository
import app.khoroch.data.repository.TransactionRepository
import app.khoroch.data.repository.UserPreferences
import app.khoroch.ui.screens.ActivityScreen
import app.khoroch.ui.screens.BudgetsScreen
import app.khoroch.ui.screens.HomeScreen
import app.khoroch.ui.screens.OnboardingScreen
import app.khoroch.ui.screens.SettingsScreen
import app.khoroch.ui.screens.TransactionDetailDialog
import app.khoroch.ui.screens.UnparsedScreen
import app.khoroch.ui.theme.KhorochTheme
import app.khoroch.worker.HistoryImportWorker
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var transactionRepo: TransactionRepository
    private lateinit var preferencesRepo: PreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        transactionRepo = TransactionRepository(applicationContext)
        preferencesRepo = PreferencesRepository(applicationContext)

        setContent {
            val preferences by preferencesRepo.userPreferencesFlow.collectAsState(initial = UserPreferences())

            KhorochTheme(themeSetting = preferences.theme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    KhorochMainContent(
                        preferences = preferences,
                        transactionRepo = transactionRepo,
                        preferencesRepo = preferencesRepo,
                        onExportCsv = { csvData -> shareCsv(csvData) },
                        onApplyLanguage = { lang ->
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(lang))
                        }
                    )
                }
            }
        }
    }

    private fun shareCsv(csvData: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, csvData)
            type = "text/csv"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Export Khoroch CSV")
        startActivity(shareIntent)
    }
}

@Composable
fun KhorochMainContent(
    preferences: UserPreferences,
    transactionRepo: TransactionRepository,
    preferencesRepo: PreferencesRepository,
    onExportCsv: (String) -> Unit,
    onApplyLanguage: (String) -> Unit
) {
    val scope = rememberCoroutineScope()

    // Query month start and end epochs
    val startOfMonth = remember {
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis
    }
    val endOfMonth = remember {
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis
    }

    val transactions by transactionRepo.getAllTransactions().collectAsState(initial = emptyList())
    val categorySpendings by transactionRepo.getCategorySpending(startOfMonth, endOfMonth).collectAsState(initial = emptyList())
    val totalExpense by transactionRepo.getTotalExpense(startOfMonth, endOfMonth).collectAsState(initial = 0L)
    val totalIncome by transactionRepo.getTotalIncome(startOfMonth, endOfMonth).collectAsState(initial = 0L)
    val budgets by transactionRepo.getAllBudgets().collectAsState(initial = emptyList())
    val unparsedCount by transactionRepo.getUnparsedCount().collectAsState(initial = 0)
    val unparsedMessages by transactionRepo.getUnparsedMessages().collectAsState(initial = emptyList())
    val merchantMemories by transactionRepo.getAllMerchantMemories().collectAsState(initial = emptyList())

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedTxnForDetail by remember { mutableStateOf<TransactionEntity?>(null) }
    var viewingUnparsedScreen by remember { mutableStateOf(false) }

    var rawSmsText by remember { mutableStateOf<String?>(null) }
    var tagsForDetail by remember { mutableStateOf<List<TagEntity>>(emptyList()) }

    LaunchedEffect(selectedTxnForDetail) {
        val txn = selectedTxnForDetail
        if (txn != null) {
            val rawId = txn.rawMessageId
            rawSmsText = if (rawId != null) transactionRepo.getRawMessage(rawId)?.body else null
            transactionRepo.getTagsForTransaction(txn.id).collect {
                tagsForDetail = it
            }
        } else {
            rawSmsText = null
            tagsForDetail = emptyList()
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    // SMS Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.READ_SMS] == true ||
            permissions[Manifest.permission.RECEIVE_SMS] == true
        if (granted) {
            val importRequest = OneTimeWorkRequestBuilder<HistoryImportWorker>().build()
            WorkManager.getInstance(context).enqueue(importRequest)
        }
    }

    if (!preferences.onboardingCompleted) {
        OnboardingScreen(
            onRequestSmsPermission = {
                permissionLauncher.launch(
                    arrayOf(Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS)
                )
            },
            onFinishOnboarding = { lang, theme ->
                scope.launch {
                    preferencesRepo.setLanguage(lang)
                    preferencesRepo.setTheme(theme)
                    preferencesRepo.setOnboardingCompleted(true)
                    onApplyLanguage(lang)
                }
            }
        )
        return
    }

    if (viewingUnparsedScreen) {
        UnparsedScreen(
            unparsedMessages = unparsedMessages,
            onBackClick = { viewingUnparsedScreen = false },
            onDismissMessage = { /* Dismiss unparsed */ }
        )
        return
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) },
                    label = { Text("Activity") }
                )
                NavigationBarItem(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null) },
                    label = { Text("Budgets") }
                )
                NavigationBarItem(
                    selected = selectedTabIndex == 3,
                    onClick = { selectedTabIndex = 3 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Settings") }
                )
            }
        }
    ) { padding ->
        Surface(modifier = Modifier.padding(padding)) {
            when (selectedTabIndex) {
                0 -> HomeScreen(
                    transactions = transactions,
                    categorySpendings = categorySpendings,
                    totalExpensePaisa = totalExpense ?: 0L,
                    totalIncomePaisa = totalIncome ?: 0L,
                    unparsedCount = unparsedCount,
                    useBanglaNumerals = preferences.banglaNumerals,
                    currencySymbol = preferences.currencyDisplay,
                    hideAmountsOnHome = preferences.hideAmountsOnHome,
                    onSeeAllClick = { selectedTabIndex = 1 },
                    onUnparsedClick = { viewingUnparsedScreen = true },
                    onTransactionClick = { txnId ->
                        selectedTxnForDetail = transactions.firstOrNull { it.id == txnId }
                    },
                    onAddTransaction = { amount, direction, catId, note ->
                        scope.launch {
                            transactionRepo.logManualTransaction(
                                amountPaisa = amount,
                                direction = direction,
                                categoryId = catId,
                                counterparty = "Cash",
                                note = note
                            )
                        }
                    }
                )
                1 -> ActivityScreen(
                    transactions = transactions,
                    useBanglaNumerals = preferences.banglaNumerals,
                    currencySymbol = preferences.currencyDisplay,
                    onTransactionClick = { txnId ->
                        selectedTxnForDetail = transactions.firstOrNull { it.id == txnId }
                    },
                    onDeleteTransaction = { txnId ->
                        scope.launch { transactionRepo.deleteTransaction(txnId) }
                    },
                    onRestoreTransaction = { txnId ->
                        scope.launch { transactionRepo.restoreTransaction(txnId) }
                    }
                )
                2 -> BudgetsScreen(
                    budgets = budgets,
                    categorySpendings = categorySpendings,
                    totalExpensePaisa = totalExpense ?: 0L,
                    useBanglaNumerals = preferences.banglaNumerals,
                    currencySymbol = preferences.currencyDisplay
                )
                3 -> SettingsScreen(
                    preferences = preferences,
                    unparsedCount = unparsedCount,
                    merchantMemories = merchantMemories,
                    onLanguageChange = { lang ->
                        scope.launch {
                            preferencesRepo.setLanguage(lang)
                            onApplyLanguage(lang)
                        }
                    },
                    onThemeChange = { theme ->
                        scope.launch { preferencesRepo.setTheme(theme) }
                    },
                    onBanglaNumeralsChange = { enabled ->
                        scope.launch { preferencesRepo.setBanglaNumerals(enabled) }
                    },
                    onHideAmountsChange = { hide ->
                        scope.launch { preferencesRepo.setHideAmounts(hide) }
                    },
                    onAppLockChange = { enabled ->
                        scope.launch { preferencesRepo.setAppLock(enabled) }
                    },
                    onExportCsv = {
                        scope.launch {
                            val csv = transactionRepo.exportToCsv(transactions)
                            onExportCsv(csv)
                        }
                    },
                    onClearAllData = {
                        scope.launch { transactionRepo.clearAllData() }
                    },
                    onUnparsedClick = { viewingUnparsedScreen = true },
                    onDeleteMerchantMemory = { key ->
                        scope.launch { transactionRepo.deleteMerchantMemory(key) }
                    },
                    onRescanSms = {
                        val importRequest = OneTimeWorkRequestBuilder<HistoryImportWorker>().build()
                        WorkManager.getInstance(context).enqueue(importRequest)
                        Toast.makeText(context, "Scanning SMS from last 90 days...", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        selectedTxnForDetail?.let { txn ->
            TransactionDetailDialog(
                transaction = txn,
                rawSmsText = rawSmsText,
                tags = tagsForDetail,
                useBanglaNumerals = preferences.banglaNumerals,
                currencySymbol = preferences.currencyDisplay,
                onDismiss = { selectedTxnForDetail = null },
                onCategoryChanged = { newCat, applyToAll ->
                    scope.launch {
                        transactionRepo.updateCategory(txn.id, newCat, applyToAll)
                        selectedTxnForDetail = null
                    }
                },
                onAddTag = { tag ->
                    scope.launch { transactionRepo.addTag(txn.id, tag) }
                },
                onRemoveTag = { tagId ->
                    scope.launch { transactionRepo.removeTag(txn.id, tagId) }
                },
                onSplit = { p1, c1, p2, c2 ->
                    scope.launch {
                        transactionRepo.splitTransaction(txn.id, p1, c1, p2, c2)
                        selectedTxnForDetail = null
                    }
                },
                onDelete = {
                    scope.launch {
                        transactionRepo.deleteTransaction(txn.id)
                        selectedTxnForDetail = null
                    }
                }
            )
        }
    }
}
