# CampusFind+ Code Examples

**Version:** 1.0  
**Last Updated:** February 2026

This document contains code snippets and examples demonstrating the preferred patterns, conventions, and implementation approaches for the CampusFind+ project.

---

## Table of Contents

1. [Room Database Examples](#room-database-examples)
2. [Repository Pattern](#repository-pattern)
3. [ViewModel & UI State](#viewmodel--ui-state)
4. [Jetpack Compose UI](#jetpack-compose-ui)
5. [Navigation](#navigation)
6. [WorkManager Sync](#workmanager-sync)
7. [API Integration](#api-integration)
8. [Sample Data & Responses](#sample-data--responses)

---

## Room Database Examples

### Entity Definition

```kotlin
// data/local/database/LostItemEntity.kt
package com.campusfind.data.local.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "lost_items")
data class LostItemEntity(
    @PrimaryKey 
    val id: String = UUID.randomUUID().toString(),
    
    @ColumnInfo(name = "title") 
    val title: String,
    
    @ColumnInfo(name = "description") 
    val description: String,
    
    @ColumnInfo(name = "status") 
    val status: String, // "LOST", "FOUND", "RESOLVED"
    
    @ColumnInfo(name = "reported_at") 
    val reportedAt: Long,
    
    @ColumnInfo(name = "last_modified_at") 
    val lastModifiedAt: Long,
    
    @ColumnInfo(name = "sync_status") 
    val syncStatus: String = "SYNCED", // "SYNCED", "PENDING_SYNC", "SYNC_FAILED"
    
    @ColumnInfo(name = "last_synced_at") 
    val lastSyncedAt: Long? = null
)
```

### DAO Interface

```kotlin
// data/local/database/LostItemDao.kt
package com.campusfind.data.local.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LostItemDao {
    
    @Query("SELECT * FROM lost_items ORDER BY reported_at DESC")
    fun getAllItems(): Flow<List<LostItemEntity>>
    
    @Query("SELECT * FROM lost_items WHERE status = :status ORDER BY reported_at DESC")
    fun getItemsByStatus(status: String): Flow<List<LostItemEntity>>
    
    @Query("SELECT * FROM lost_items WHERE id = :id")
    suspend fun getItemById(id: String): LostItemEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: LostItemEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<LostItemEntity>)
    
    @Update
    suspend fun updateItem(item: LostItemEntity)
    
    @Query("""
        UPDATE lost_items 
        SET status = :status, 
            last_modified_at = :timestamp,
            sync_status = 'PENDING_SYNC'
        WHERE id = :id
    """)
    suspend fun updateItemStatus(id: String, status: String, timestamp: Long)
    
    @Query("SELECT * FROM lost_items WHERE sync_status = 'PENDING_SYNC'")
    suspend fun getPendingSyncItems(): List<LostItemEntity>
    
    @Delete
    suspend fun deleteItem(item: LostItemEntity)
}
```

### Database Class

```kotlin
// data/local/database/AppDatabase.kt
package com.campusfind.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [LostItemEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun lostItemDao(): LostItemDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "campusfind_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

---

## Repository Pattern

### Domain Model

```kotlin
// domain/model/LostItem.kt
package com.campusfind.domain.model

data class LostItem(
    val id: String,
    val title: String,
    val description: String,
    val status: ItemStatus,
    val reportedAt: Long,
    val lastModifiedAt: Long,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)

enum class ItemStatus {
    LOST,
    FOUND,
    RESOLVED;
    
    companion object {
        fun fromString(value: String): ItemStatus {
            return valueOf(value.uppercase())
        }
    }
}

enum class SyncStatus {
    SYNCED,
    PENDING_SYNC,
    SYNC_FAILED;
    
    companion object {
        fun fromString(value: String): SyncStatus {
            return valueOf(value.uppercase())
        }
    }
}
```

### Repository Interface

```kotlin
// domain/repository/LostItemRepository.kt
package com.campusfind.domain.repository

import com.campusfind.domain.model.LostItem
import com.campusfind.domain.model.ItemStatus
import kotlinx.coroutines.flow.Flow

interface LostItemRepository {
    fun getAllItems(): Flow<List<LostItem>>
    fun getItemsByStatus(status: ItemStatus): Flow<List<LostItem>>
    suspend fun getItemById(id: String): LostItem?
    suspend fun insertItem(item: LostItem)
    suspend fun updateItemStatus(id: String, status: ItemStatus)
    suspend fun syncWithCloud(): Result<Unit>
}
```

### Repository Implementation

```kotlin
// data/repository/LostItemRepositoryImpl.kt
package com.campusfind.data.repository

import com.campusfind.data.local.database.LostItemDao
import com.campusfind.data.local.database.LostItemEntity
import com.campusfind.domain.model.LostItem
import com.campusfind.domain.model.ItemStatus
import com.campusfind.domain.model.SyncStatus
import com.campusfind.domain.repository.LostItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LostItemRepositoryImpl @Inject constructor(
    private val localDataSource: LostItemDao
) : LostItemRepository {
    
    override fun getAllItems(): Flow<List<LostItem>> {
        return localDataSource.getAllItems().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override fun getItemsByStatus(status: ItemStatus): Flow<List<LostItem>> {
        return localDataSource.getItemsByStatus(status.name).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override suspend fun getItemById(id: String): LostItem? {
        return localDataSource.getItemById(id)?.toDomainModel()
    }
    
    override suspend fun insertItem(item: LostItem) {
        localDataSource.insertItem(item.toEntity())
    }
    
    override suspend fun updateItemStatus(id: String, status: ItemStatus) {
        val timestamp = System.currentTimeMillis()
        localDataSource.updateItemStatus(id, status.name, timestamp)
    }
    
    override suspend fun syncWithCloud(): Result<Unit> {
        return try {
            // TODO: Implement cloud sync logic in MCO 2
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Extension functions for mapping
private fun LostItemEntity.toDomainModel(): LostItem {
    return LostItem(
        id = id,
        title = title,
        description = description,
        status = ItemStatus.fromString(status),
        reportedAt = reportedAt,
        lastModifiedAt = lastModifiedAt,
        syncStatus = SyncStatus.fromString(syncStatus)
    )
}

private fun LostItem.toEntity(): LostItemEntity {
    return LostItemEntity(
        id = id,
        title = title,
        description = description,
        status = status.name,
        reportedAt = reportedAt,
        lastModifiedAt = lastModifiedAt,
        syncStatus = syncStatus.name,
        lastSyncedAt = null
    )
}
```

---

## ViewModel & UI State

### UI State Definition

```kotlin
// ui/screens/home/HomeUiState.kt
package com.campusfind.ui.screens.home

import com.campusfind.domain.model.LostItem
import com.campusfind.domain.model.ItemStatus

data class HomeUiState(
    val items: List<LostItem> = emptyList(),
    val selectedFilter: ItemStatus? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
```

### ViewModel Implementation

```kotlin
// ui/screens/home/HomeViewModel.kt
package com.campusfind.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusfind.domain.model.ItemStatus
import com.campusfind.domain.repository.LostItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: LostItemRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    private val _selectedFilter = MutableStateFlow<ItemStatus?>(null)
    
    init {
        observeItems()
    }
    
    private fun observeItems() {
        viewModelScope.launch {
            _selectedFilter
                .flatMapLatest { filter ->
                    _uiState.update { it.copy(isLoading = true) }
                    if (filter == null) {
                        repository.getAllItems()
                    } else {
                        repository.getItemsByStatus(filter)
                    }
                }
                .catch { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = exception.message
                        )
                    }
                }
                .collect { items ->
                    _uiState.update { 
                        it.copy(
                            items = items,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }
    
    fun onFilterChanged(filter: ItemStatus?) {
        _selectedFilter.value = filter
        _uiState.update { it.copy(selectedFilter = filter) }
    }
    
    fun onStatusUpdated(itemId: String, newStatus: ItemStatus) {
        viewModelScope.launch {
            try {
                repository.updateItemStatus(itemId, newStatus)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
```

### Add Item ViewModel

```kotlin
// ui/screens/additem/AddItemViewModel.kt
package com.campusfind.ui.screens.additem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusfind.domain.model.LostItem
import com.campusfind.domain.model.ItemStatus
import com.campusfind.domain.model.SyncStatus
import com.campusfind.domain.repository.LostItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddItemViewModel @Inject constructor(
    private val repository: LostItemRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AddItemUiState())
    val uiState: StateFlow<AddItemUiState> = _uiState.asStateFlow()
    
    fun onTitleChanged(title: String) {
        _uiState.update { it.copy(title = title) }
    }
    
    fun onDescriptionChanged(description: String) {
        _uiState.update { it.copy(description = description) }
    }
    
    fun onSubmit(onSuccess: () -> Unit) {
        val currentState = _uiState.value
        
        // Validation
        if (currentState.title.isBlank()) {
            _uiState.update { it.copy(error = "Title is required") }
            return
        }
        
        if (currentState.description.isBlank()) {
            _uiState.update { it.copy(error = "Description is required") }
            return
        }
        
        _uiState.update { it.copy(isSubmitting = true, error = null) }
        
        viewModelScope.launch {
            try {
                val item = LostItem(
                    id = UUID.randomUUID().toString(),
                    title = currentState.title,
                    description = currentState.description,
                    status = ItemStatus.LOST,
                    reportedAt = System.currentTimeMillis(),
                    lastModifiedAt = System.currentTimeMillis(),
                    syncStatus = SyncStatus.PENDING_SYNC
                )
                
                repository.insertItem(item)
                _uiState.update { it.copy(isSubmitting = false) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isSubmitting = false,
                        error = "Failed to save item: ${e.message}"
                    )
                }
            }
        }
    }
}

data class AddItemUiState(
    val title: String = "",
    val description: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null
)
```

---

## Jetpack Compose UI

### Home Screen

```kotlin
// ui/screens/home/HomeScreen.kt
package com.campusfind.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.campusfind.domain.model.ItemStatus
import com.campusfind.ui.components.FilterChips
import com.campusfind.ui.components.ItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAddItem: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CampusFind+") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddItem,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter Chips
            FilterChips(
                selectedFilter = uiState.selectedFilter,
                onFilterSelected = { filter ->
                    viewModel.onFilterChanged(filter)
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            // Content
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.error ?: "Unknown error",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                uiState.items.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No items found")
                    }
                }
                
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.items,
                            key = { it.id }
                        ) { item ->
                            ItemCard(
                                item = item,
                                onClick = { onNavigateToDetail(item.id) },
                                onStatusUpdate = { newStatus ->
                                    viewModel.onStatusUpdated(item.id, newStatus)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
```

### Item Card Component

```kotlin
// ui/components/ItemCard.kt
package com.campusfind.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.campusfind.domain.model.ItemStatus
import com.campusfind.domain.model.LostItem
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ItemCard(
    item: LostItem,
    onClick: () -> Unit,
    onStatusUpdate: (ItemStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                StatusBadge(status = item.status)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTimestamp(item.reportedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (item.status == ItemStatus.LOST) {
                    TextButton(
                        onClick = { onStatusUpdate(ItemStatus.FOUND) }
                    ) {
                        Text("Mark as Found")
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
```

### Status Badge Component

```kotlin
// ui/components/StatusBadge.kt
package com.campusfind.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.campusfind.domain.model.ItemStatus

@Composable
fun StatusBadge(
    status: ItemStatus,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (status) {
        ItemStatus.LOST -> Color(0xFFFFEBEE) to Color(0xFFC62828)
        ItemStatus.FOUND -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        ItemStatus.RESOLVED -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
    }
    
    Text(
        text = status.name,
        style = MaterialTheme.typography.labelSmall,
        color = textColor,
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 4.dp)
    )
}
```

### Filter Chips Component

```kotlin
// ui/components/FilterChips.kt
package com.campusfind.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.campusfind.domain.model.ItemStatus

@Composable
fun FilterChips(
    selectedFilter: ItemStatus?,
    onFilterSelected: (ItemStatus?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedFilter == null,
            onClick = { onFilterSelected(null) },
            label = { Text("All") }
        )
        
        FilterChip(
            selected = selectedFilter == ItemStatus.LOST,
            onClick = { 
                onFilterSelected(
                    if (selectedFilter == ItemStatus.LOST) null 
                    else ItemStatus.LOST
                )
            },
            label = { Text("Lost") }
        )
        
        FilterChip(
            selected = selectedFilter == ItemStatus.FOUND,
            onClick = { 
                onFilterSelected(
                    if (selectedFilter == ItemStatus.FOUND) null 
                    else ItemStatus.FOUND
                )
            },
            label = { Text("Found") }
        )
    }
}
```

### Add Item Screen

```kotlin
// ui/screens/additem/AddItemScreen.kt
package com.campusfind.ui.screens.additem

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddItemViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report Lost Item") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.onTitleChanged(it) },
                label = { Text("Item Title") },
                placeholder = { Text("e.g., Black Wallet") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.error?.contains("title", ignoreCase = true) == true
            )
            
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.onDescriptionChanged(it) },
                label = { Text("Description") },
                placeholder = { Text("e.g., Lost near library, contains student ID") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 5,
                isError = uiState.error?.contains("description", ignoreCase = true) == true
            )
            
            if (uiState.error != null) {
                Text(
                    text = uiState.error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = { 
                    viewModel.onSubmit(onSuccess = onNavigateBack)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSubmitting
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Submit Report")
                }
            }
        }
    }
}
```

---

## Navigation

### Navigation Graph

```kotlin
// ui/navigation/NavGraph.kt
package com.campusfind.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.campusfind.ui.screens.additem.AddItemScreen
import com.campusfind.ui.screens.detail.DetailScreen
import com.campusfind.ui.screens.home.HomeScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddItem : Screen("add_item")
    object Detail : Screen("detail/{itemId}") {
        fun createRoute(itemId: String) = "detail/$itemId"
    }
}

@Composable
fun CampusFindNavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToAddItem = {
                    navController.navigate(Screen.AddItem.route)
                },
                onNavigateToDetail = { itemId ->
                    navController.navigate(Screen.Detail.createRoute(itemId))
                }
            )
        }
        
        composable(Screen.AddItem.route) {
            AddItemScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument("itemId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
            DetailScreen(
                itemId = itemId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
```

---

## WorkManager Sync

### Sync Worker

```kotlin
// data/sync/SyncWorker.kt
package com.campusfind.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.campusfind.domain.repository.LostItemRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: LostItemRepository
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            val syncResult = repository.syncWithCloud()
            
            if (syncResult.isSuccess) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
    
    companion object {
        const val WORK_NAME = "campusfind_sync_worker"
    }
}
```

### Sync Manager

```kotlin
// data/sync/SyncManager.kt
package com.campusfind.data.sync

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SyncManager @Inject constructor(
    private val context: Context
) {
    
    fun schedulePeriodic Sync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
        
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            repeatInterval = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
    
    fun cancelSync() {
        WorkManager.getInstance(context).cancelUniqueWork(SyncWorker.WORK_NAME)
    }
}
```

---

## API Integration

### API Service (Retrofit)

```kotlin
// data/remote/api/LostItemApi.kt
package com.campusfind.data.remote.api

import com.campusfind.data.remote.dto.LostItemDto
import com.campusfind.data.remote.dto.SyncResponse
import retrofit2.http.*

interface LostItemApi {
    
    @GET("items")
    suspend fun getItems(
        @Query("status") status: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): List<LostItemDto>
    
    @GET("items/{id}")
    suspend fun getItemById(@Path("id") id: String): LostItemDto
    
    @POST("items")
    suspend fun createItem(@Body item: LostItemDto): LostItemDto
    
    @PATCH("items/{id}/status")
    suspend fun updateItemStatus(
        @Path("id") id: String,
        @Body status: Map<String, String>
    ): LostItemDto
    
    @GET("sync/items")
    suspend fun getSyncUpdates(
        @Query("since") timestamp: Long
    ): SyncResponse
    
    @POST("sync/items")
    suspend fun uploadChanges(
        @Body items: List<LostItemDto>
    ): SyncResponse
}
```

### DTOs

```kotlin
// data/remote/dto/LostItemDto.kt
package com.campusfind.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LostItemDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("reportedAt")
    val reportedAt: Long,
    
    @SerializedName("lastModifiedAt")
    val lastModifiedAt: Long
)

data class SyncResponse(
    @SerializedName("items")
    val items: List<LostItemDto>,
    
    @SerializedName("timestamp")
    val timestamp: Long,
    
    @SerializedName("hasMore")
    val hasMore: Boolean
)
```

---

## Sample Data & Responses

### Sample Lost Items (JSON)

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "title": "Black Wallet",
    "description": "Lost near library entrance. Contains student ID and credit cards.",
    "status": "LOST",
    "reportedAt": 1707523200000,
    "lastModifiedAt": 1707523200000
  },
  {
    "id": "550e8400-e29b-41d4-a716-446655440002",
    "title": "iPhone 13 Pro",
    "description": "Blue case with cracked screen. Lost in cafeteria.",
    "status": "LOST",
    "reportedAt": 1707536800000,
    "lastModifiedAt": 1707536800000
  },
  {
    "id": "550e8400-e29b-41d4-a716-446655440003",
    "title": "Red Backpack",
    "description": "Found in gym locker room. Contains textbooks.",
    "status": "FOUND",
    "reportedAt": 1707550400000,
    "lastModifiedAt": 1707564000000
  }
]
```

### API Request/Response Examples

**POST /items** (Create Item)
```http
POST https://api.campusfind.com/v1/items
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...

{
  "title": "Black Wallet",
  "description": "Lost near library entrance. Contains student ID.",
  "status": "LOST",
  "reportedAt": 1707523200000
}
```

**Response (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "title": "Black Wallet",
  "description": "Lost near library entrance. Contains student ID.",
  "status": "LOST",
  "reportedAt": 1707523200000,
  "lastModifiedAt": 1707523200000
}
```

**PATCH /items/:id/status** (Update Status)
```http
PATCH https://api.campusfind.com/v1/items/550e8400-e29b-41d4-a716-446655440001/status
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...

{
  "status": "FOUND",
  "lastModifiedAt": 1707609600000
}
```

**Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "title": "Black Wallet",
  "description": "Lost near library entrance. Contains student ID.",
  "status": "FOUND",
  "reportedAt": 1707523200000,
  "lastModifiedAt": 1707609600000
}
```

**GET /sync/items** (Sync Updates)
```http
GET https://api.campusfind.com/v1/sync/items?since=1707523200000
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

**Response (200 OK):**
```json
{
  "items": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440002",
      "title": "iPhone 13 Pro",
      "description": "Blue case with cracked screen. Lost in cafeteria.",
      "status": "LOST",
      "reportedAt": 1707536800000,
      "lastModifiedAt": 1707536800000
    }
  ],
  "timestamp": 1707609600000,
  "hasMore": false
}
```

---

## Testing Examples

### Repository Test

```kotlin
// test/repository/LostItemRepositoryTest.kt
package com.campusfind.repository

import com.campusfind.data.local.database.LostItemDao
import com.campusfind.data.local.database.LostItemEntity
import com.campusfind.data.repository.LostItemRepositoryImpl
import com.campusfind.domain.model.ItemStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class LostItemRepositoryTest {
    
    @Mock
    private lateinit var dao: LostItemDao
    
    private lateinit var repository: LostItemRepositoryImpl
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = LostItemRepositoryImpl(dao)
    }
    
    @Test
    fun `getAllItems returns mapped domain models`() = runTest {
        // Given
        val entities = listOf(
            LostItemEntity(
                id = "1",
                title = "Test Item",
                description = "Test Description",
                status = "LOST",
                reportedAt = 1707523200000,
                lastModifiedAt = 1707523200000,
                syncStatus = "SYNCED"
            )
        )
        `when`(dao.getAllItems()).thenReturn(flowOf(entities))
        
        // When
        val result = repository.getAllItems().first()
        
        // Then
        assertEquals(1, result.size)
        assertEquals("Test Item", result[0].title)
        assertEquals(ItemStatus.LOST, result[0].status)
    }
}
```

---

**Document Status:** Living document - add more examples as patterns emerge  
**Last Updated:** February 9, 2026
