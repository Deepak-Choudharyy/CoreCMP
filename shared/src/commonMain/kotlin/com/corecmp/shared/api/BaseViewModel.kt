package com.corecmp.shared.api

import androidx.lifecycle.ViewModel
import com.corecmp.shared.ui.AppSnackbarManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

abstract class BaseViewModel : ViewModel() {
    private val _baseState = MutableStateFlow(BaseUiState())
    val baseState = _baseState.asStateFlow()
    protected fun setLoading(value: Boolean) {
        _baseState.update {
            it.copy(isLoading = value)
        }
    }
    protected fun setError(message: String?) {
        _baseState.update {
            it.copy(error = message)
        }
        AppSnackbarManager.show(message)
    }

    protected fun <T> Flow<Resource<T>>.collectApi(
        scope: CoroutineScope,
        showError : Boolean = true,
        showLoading : Boolean = true,
        onError: (String?) -> Unit = {},
        onSuccess: (T?) -> Unit = {}
    ) {
        onEach { result ->
            when (result) {

                is Resource.Loading -> {
                    if (showLoading) {
                        setLoading(true)
                    }
                }

                is Resource.Error -> {
                    if(showError) {
                        setLoading(false)
                        setError(result.message)
                    }
                    onError(result.message)
                }

                is Resource.Success -> {
                    setLoading(false)
                    onSuccess(result.data)
                }
            }
        }.launchIn(scope)
    }
}

data class BaseUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)