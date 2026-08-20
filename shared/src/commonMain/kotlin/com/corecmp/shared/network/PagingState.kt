package com.corecmp.shared.network

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

data class PagingState<T>(
    val items: List<T> = emptyList(),
    val page: Int = 1,
    val pageSize: Int = 20,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val endReached: Boolean = false,
    val error: String? = null,
) {
    val canLoadMore: Boolean
        get() = !isLoading && !isLoadingMore && !endReached && error == null
}

class PagingController<T>(
    private val scope: CoroutineScope,
    private val pageSize: Int,
    private val loadPage: suspend (page: Int, pageSize: Int) -> List<T>,
) {
    var state by mutableStateOf(PagingState<T>(pageSize = pageSize))
        private set

    fun refresh() {
        scope.launch { refreshInternal() }
    }

    fun loadMore() {
        scope.launch { loadMoreInternal() }
    }

    suspend fun refreshInternal() {
        state = state.copy(
            isLoading = true,
            error = null,
            page = 1,
            endReached = false,
        )
        runCatching {
            val pageItems = loadPage(1, pageSize)
            state = state.copy(
                items = pageItems,
                page = 1,
                isLoading = false,
                endReached = pageItems.size < pageSize,
            )
        }.onFailure { error ->
            state = state.copy(
                isLoading = false,
                error = error.message ?: "Failed to load page",
            )
        }
    }

    private suspend fun loadMoreInternal() {
        if (!state.canLoadMore) return
        val nextPage = state.page + 1
        state = state.copy(isLoadingMore = true, error = null)
        runCatching {
            val pageItems = loadPage(nextPage, pageSize)
            state = state.copy(
                items = state.items + pageItems,
                page = nextPage,
                isLoadingMore = false,
                endReached = pageItems.size < pageSize,
            )
        }.onFailure { error ->
            state = state.copy(
                isLoadingMore = false,
                error = error.message ?: "Failed to load more",
            )
        }
    }
}

@Composable
fun <T> rememberPagingState(
    pageSize: Int = 20,
    loadPage: suspend (page: Int, pageSize: Int) -> List<T>,
): Pair<PagingState<T>, PagingController<T>> {
    val scope = rememberCoroutineScope()
    val controller = remember(pageSize, loadPage) {
        PagingController(
            scope = scope,
            pageSize = pageSize,
            loadPage = loadPage,
        )
    }

    LaunchedEffect(controller, pageSize) {
        controller.refreshInternal()
    }

    return controller.state to controller
}
