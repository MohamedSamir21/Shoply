package com.shopping.shoply.categories.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopping.shoply.catalog.domain.CatalogRepository
import com.shopping.shoply.common.domain.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Backs the Categories screen. Loads the categories once when created; [CategoriesIntent.Retry]
 * loads them again. An empty list is [CategoriesState.Empty]; any error is [CategoriesState.Offline].
 */
@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val catalogRepository: CatalogRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<CategoriesState>(CategoriesState.Loading)
    val state: StateFlow<CategoriesState> = _state.asStateFlow()

    init {
        load()
    }

    fun onIntent(intent: CategoriesIntent) {
        when (intent) {
            CategoriesIntent.Retry -> load()
        }
    }

    private fun load() {
        _state.value = CategoriesState.Loading
        viewModelScope.launch {
            _state.value = when (val result = catalogRepository.getCategories()) {
                is Result.Success ->
                    if (result.data.isEmpty()) CategoriesState.Empty
                    else CategoriesState.Content(result.data)
                is Result.Error -> CategoriesState.Offline
            }
        }
    }
}
