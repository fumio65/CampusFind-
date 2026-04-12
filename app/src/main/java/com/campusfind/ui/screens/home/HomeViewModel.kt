package com.campusfind.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusfind.data.sync.SyncManager
import com.campusfind.domain.model.ItemStatus
import com.campusfind.domain.repository.LostItemRepository
import com.campusfind.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "SYNC_DEBUG"

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: LostItemRepository,
    private val userRepository: UserRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow<ItemStatus?>(null)

    val uiState: StateFlow<HomeUiState> = _selectedFilter
        .flatMapLatest { filter ->
            Log.d(TAG, "HomeViewModel: filter=$filter subscribing to Room")
            if (filter == null) repository.getAllItems()
            else repository.getItemsByStatus(filter)
        }
        .flatMapLatest { items ->
            Log.d(TAG, "HomeViewModel: Room emitted ${items.size} items")
            flow {
                val names = mutableMapOf<String, String>()
                items.map { it.reportedBy }.distinct().forEach { userId ->
                    names[userId] = userRepository.getUserById(userId)?.fullName ?: "Unknown"
                }
                emit(Pair(items, names.toMap()))
            }
        }
        .combine(_selectedFilter) { (items, names), filter ->
            Log.d(TAG, "HomeViewModel: building UiState ${items.size} items")
            HomeUiState(
                items          = items,
                reporterNames  = names,
                selectedFilter = filter,
                isLoading      = false,
                error          = null
            )
        }
        .catch { exception ->
            Log.e(TAG, "HomeViewModel Flow ERROR: ${exception.message}")
            exception.printStackTrace()
            emit(HomeUiState(error = exception.message ?: "Failed to load items"))
        }
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.Eagerly,
            initialValue = HomeUiState(isLoading = true)
        )

    init {
        // Poll Supabase every 10 seconds while the ViewModel is alive.
        // This keeps the HomeScreen data fresh without any user action —
        // new items posted by other users appear automatically.
        // The coroutine is cancelled when the ViewModel is cleared (user
        // logs out or the app process is killed).
        viewModelScope.launch {
            while (true) {
                delay(5_000)
                Log.d(TAG, "HomeViewModel: polling sync")
                syncManager.triggerNow()
            }
        }
    }

    fun refresh() {
        syncManager.triggerNow()
    }

    fun onFilterChanged(filter: ItemStatus?) {
        Log.d(TAG, "HomeViewModel: onFilterChanged($filter)")
        _selectedFilter.value = filter
    }
}