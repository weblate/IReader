package org.ireader.presentation.feature_library.presentation.viewmodel


import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.ireader.core_ui.viewmodel.BaseViewModel
import org.ireader.domain.models.DisplayMode
import org.ireader.domain.models.FilterType
import org.ireader.domain.models.SortType
import org.ireader.domain.use_cases.history.HistoryUseCase
import org.ireader.domain.use_cases.local.LocalGetBookUseCases
import org.ireader.domain.use_cases.preferences.reader_preferences.LibraryLayoutTypeUseCase
import org.ireader.domain.use_cases.preferences.reader_preferences.SortersUseCase
import javax.inject.Inject


@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val localGetBookUseCases: LocalGetBookUseCases,
    private val libraryLayoutUseCase: LibraryLayoutTypeUseCase,
    private val sortersUseCase: SortersUseCase,
    private val historyUseCase: HistoryUseCase,
    private val libraryState: LibraryStateImpl,
) : BaseViewModel(), LibraryState by libraryState {


    init {
        readLayoutTypeAndFilterTypeAndSortType()
        getLibraryBooks()
    }

    suspend fun getHistories() {
        historyUseCase.findHistories()
    }

    fun onEvent(event: LibraryEvents) {
        when (event) {
            is LibraryEvents.OnLayoutTypeChange -> {
                onLayoutTypeChange(event.layoutType)
            }
            is LibraryEvents.ToggleSearchMode -> {
                toggleSearchMode(event.inSearchMode)
                getLibraryDataIfSearchModeIsOff()
            }
            is LibraryEvents.UpdateSearchInput -> {
                onQueryChange(event.query)
            }
            is LibraryEvents.SearchBook -> {
                getLibraryBooks()
            }
            is LibraryEvents.EnableFilter -> {
                when (event.filterType) {
                    is FilterType.Unread -> {
                        addFilters(event.filterType)
                    }
                    else -> {

                    }
                }
            }
        }

    }


    private var getBooksJob: Job? = null
    fun getLibraryBooks() {
        getBooksJob?.cancel()
        getBooksJob = viewModelScope.launch {
            localGetBookUseCases.SubscribeInLibraryBooks(
                query = searchQuery,
                sortType,
                isAsc = isSortAcs,
                filters
            ).collect {
                books = it
            }
        }
    }


    private fun onQueryChange(query: String) {
        searchQuery = query
    }

    private fun toggleSearchMode(inSearchMode: Boolean) {
        this.inSearchMode = inSearchMode
    }


    private fun onLayoutTypeChange(layoutType: DisplayMode) {
        libraryLayoutUseCase.save(layoutType.layoutIndex)
        this.layout = layoutType.layout
    }


    private fun readLayoutTypeAndFilterTypeAndSortType() {
        val sortType = sortersUseCase.read()
        val layoutType = libraryLayoutUseCase.read().layout
        this.layout = layoutType
        this.sortType = sortType

    }

    fun changeSortIndex(sortType: SortType) {
        this.sortType = sortType
        if (sortType == sortType) {
            this.isSortAcs = !isSortAcs
        }
        saveSortType(sortType)
        getLibraryBooks()
    }

    private fun saveSortType(sortType: SortType) {
        sortersUseCase.save(sortType.index)
    }

    fun addFilters(filterType: FilterType) {
        this.filters.add(filterType)

        getLibraryBooks()
    }

    fun removeFilters(filterType: FilterType) {
        this.filters.remove(filterType)

        getLibraryBooks()
    }

    private fun getLibraryDataIfSearchModeIsOff() {
        if (inSearchMode) return
        this.searchedBook = emptyList()
        this.searchQuery = ""
        getLibraryBooks()
    }
}