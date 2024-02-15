package ireader.presentation.ui.home.library.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.domain.models.DisplayMode
import ireader.domain.models.library.LibraryFilter
import ireader.domain.models.library.LibrarySort
import ireader.i18n.LocalizeHelper
import ireader.i18n.asString
import ireader.i18n.localize

import ireader.presentation.ui.component.reusable_composable.MidSizeTextComposable
import ireader.presentation.ui.component.text_related.TextSection
import ireader.presentation.ui.core.theme.AppColors
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.core.ui.Colour.contentColor
import ireader.presentation.ui.home.library.viewmodel.LibraryViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)

@ExperimentalMaterialApi
@Composable
fun Tabs(libraryTabs: List<TabItem>, pagerState: androidx.compose.foundation.pager.PagerState) {
    val scope = rememberCoroutineScope()
    // OR ScrollableTabRow()
    androidx.compose.material3.TabRow(
        selectedTabIndex = pagerState.currentPage,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.contentColor,
    ) {
        libraryTabs.forEachIndexed { index, tab ->
            Tab(
                text = { MidSizeTextComposable(text = tab.title) },
                selected = pagerState.currentPage == index,
                unselectedContentColor = MaterialTheme.colorScheme.onBackground,
                selectedContentColor = MaterialTheme.colorScheme.primary,
                onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@ExperimentalMaterialApi
@Composable
fun ScrollableTabs(
    modifier: Modifier = Modifier,
    libraryTabs: List<String>,
    pagerState: androidx.compose.foundation.pager.PagerState,
    visible: Boolean = true,
) {
    val scope = rememberCoroutineScope()
    // OR ScrollableTabRow()
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        androidx.compose.material3.ScrollableTabRow(
            modifier = modifier,
            selectedTabIndex = pagerState.currentPage,
            containerColor = AppColors.current.bars,
            contentColor = AppColors.current.onBars,
            edgePadding = 0.dp,
        ) {
            libraryTabs.forEachIndexed { index, tab ->
                Tab(
                    text = { MidSizeTextComposable(text = tab) },
                    selected = pagerState.currentPage == index,
                    unselectedContentColor = MaterialTheme.colorScheme.onBackground,
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TabsContent(
    libraryTabs: List<TabItem>,
    pagerState: androidx.compose.foundation.pager.PagerState,
    filters: List<LibraryFilter>,
    onLayoutSelected: (DisplayMode) -> Unit,
    vm: LibraryViewModel,
    scaffoldPadding: PaddingValues
) {
    val localizeHelper = LocalLocalizeHelper.currentOrThrow
    val layouts = remember {
        listOf(
            DisplayMode.CompactGrid,
            DisplayMode.ComfortableGrid,
            DisplayMode.List,
            DisplayMode.OnlyCover
        )
    }
    HorizontalPager(
        modifier = Modifier.fillMaxSize(),
        state = pagerState,
        pageSpacing = 0.dp,
        userScrollEnabled = true,
        reverseLayout = false,
        contentPadding = PaddingValues(0.dp),
        beyondBoundsPageCount = 0,
        pageSize = PageSize.Fill,
        key = null,
        pageNestedScrollConnection = PagerDefaults.pageNestedScrollConnection(
            state = pagerState,
            orientation = Orientation.Horizontal
        ),
        pageContent = { page ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                contentPadding = scaffoldPadding
            ) {
                when (page) {
                    0 -> FiltersPage(filters, onClick = {
                        vm.toggleFilter(it)
                    })

                    1 -> SortPage(
                        vm.sorting.value,
                        onClick = vm::toggleSort,
                        localizeHelper
                    )

                    2 -> DispalyPage(
                        layouts = layouts,
                        onLayoutSelected = onLayoutSelected,
                        vm = vm
                    )
                }
            }
        }
    )
}

private fun LazyListScope.FiltersPage(
    filters: List<LibraryFilter>,
    onClick: (LibraryFilter.Type) -> Unit
) {
    items(filters) { (filter, state) ->
        ClickableRow(onClick = { onClick(filter) }) {
            TriStateCheckbox(
                modifier = Modifier.padding(horizontal = 16.dp),
                state = state.asToggleableState(),
                onClick = { onClick(filter) }
            )
            Text(filter.name)
        }
    }
}

@Composable
private fun ClickableRow(onClick: () -> Unit, content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .requiredHeight(48.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        content = { content() }
    )
}

private fun LibraryFilter.Value.asToggleableState(): ToggleableState {
    return when (this) {
        LibraryFilter.Value.Included -> ToggleableState.On
        LibraryFilter.Value.Excluded -> ToggleableState.Indeterminate
        LibraryFilter.Value.Missing -> ToggleableState.Off
    }
}

private fun LazyListScope.SortPage(
    sorting: LibrarySort,
    onClick: (LibrarySort.Type) -> Unit,
    localizeHelper: LocalizeHelper
) {

    items(LibrarySort.types) { type ->
        ClickableRow(onClick = { onClick(type) }) {
            val iconModifier = Modifier.requiredWidth(56.dp)
            if (sorting.type == type) {
                val icon = if (sorting.isAscending) {
                    Icons.Default.KeyboardArrowUp
                } else {
                    Icons.Default.KeyboardArrowDown
                }
                Icon(
                    icon,
                    null,
                    iconModifier,
                    MaterialTheme.colorScheme.primary
                )
            } else {
                Spacer(iconModifier)
            }
            Text(LibrarySort.Type.name(type).asString(localizeHelper))
        }
    }
}


private fun LazyListScope.DispalyPage(
        layouts: List<DisplayMode>,
        vm: LibraryViewModel,
        onLayoutSelected: (DisplayMode) -> Unit
) {
    item {
        TextSection(
            text = localize { xml -> xml.displayMode },
            padding = PaddingValues(vertical = 12.dp, horizontal = 20.dp),
            style = MaterialTheme.typography.bodyMedium,
            toUpper = false
        )
    }
    items(layouts) { layout ->
        val localizeHelper = LocalLocalizeHelper.currentOrThrow
        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            ClickableRow(onClick = { onLayoutSelected(layout) }) {
                RadioButton(
                    selected = vm.layout == layout,
                    onClick = { onLayoutSelected(layout) },
                    modifier = Modifier.padding(horizontal = 15.dp)
                )

                when (layout) {
                    DisplayMode.CompactGrid -> {
                        MidSizeTextComposable(text = localizeHelper.localize { xml -> xml.compactLayout })
                    }
                    DisplayMode.ComfortableGrid -> {
                        MidSizeTextComposable(text = localizeHelper.localize { xml -> xml.comfortableLayout })
                    }
                    DisplayMode.List -> {
                        MidSizeTextComposable(text = localizeHelper.localize { xml -> xml.listLayout })
                    }
                    DisplayMode.OnlyCover -> {
                        MidSizeTextComposable(text = localizeHelper.localize { xml -> xml.coverOnlyLayout })
                    }
                }
            }
        }
    }
    item {
        TextSection(
            text = localize { xml -> xml.columns },
            padding = PaddingValues(vertical = 12.dp, horizontal = 20.dp),
            style = MaterialTheme.typography.bodyMedium,
            toUpper = false
        )
    }
    item {
        Slider(modifier = Modifier.padding(horizontal = 20.dp), value = vm.columnInPortrait.lazyValue.toFloat(), onValueChange = {
            vm.columnInPortrait.lazyValue = it.toInt()
        }, valueRange = 0f..10f)
    }
    item {
        TextSection(
            text = localize { xml -> xml.badge },
            padding = PaddingValues(vertical = 12.dp, horizontal = 20.dp),
            style = MaterialTheme.typography.bodyMedium,
            toUpper = false
        )
    }
    item {
        val localizeHelper = LocalLocalizeHelper.currentOrThrow
        ClickableRow(onClick = { vm.readBadge.value = !vm.readBadge.value }) {
            Checkbox(
                modifier = Modifier.padding(horizontal = 16.dp),
                checked = vm.readBadge.value,
                onCheckedChange = {
                    vm.readBadge.value = it
                }
            )
            MidSizeTextComposable(text = localizeHelper.localize { xml -> xml.readChapters })
        }
    }
    item {
        val localizeHelper = LocalLocalizeHelper.currentOrThrow
        ClickableRow(onClick = { vm.unreadBadge.value = !vm.unreadBadge.value }) {
            Checkbox(
                modifier = Modifier.padding(horizontal = 16.dp),
                checked = vm.unreadBadge.value,
                onCheckedChange = {
                    vm.unreadBadge.value = it
                }
            )
            MidSizeTextComposable(text = localizeHelper.localize { xml -> xml.unreadChapters })
        }
    }
    item {
        val localizeHelper = LocalLocalizeHelper.currentOrThrow
        ClickableRow(onClick = { vm.goToLastChapterBadge.value = !vm.goToLastChapterBadge.value }) {
            Checkbox(
                modifier = Modifier.padding(horizontal = 16.dp),
                checked = vm.goToLastChapterBadge.value,
                onCheckedChange = {
                    vm.goToLastChapterBadge.value = it
                }
            )
            MidSizeTextComposable(text = localizeHelper.localize { xml -> xml.goToLastChapter })
        }
    }
    item {
        TextSection(
            text = localize { xml -> xml.tabs },
            padding = PaddingValues(vertical = 12.dp, horizontal = 20.dp),
            style = MaterialTheme.typography.bodyMedium,
            toUpper = false
        )
    }
    item {
        val localizeHelper = LocalLocalizeHelper.currentOrThrow
        ClickableRow(onClick = { vm.showCategoryTabs.value = !vm.showCategoryTabs.value }) {
            Checkbox(
                modifier = Modifier.padding(horizontal = 16.dp),
                checked = vm.showCategoryTabs.value,
                onCheckedChange = {
                    vm.showCategoryTabs.value = it
                }
            )
            MidSizeTextComposable(text = localizeHelper.localize { xml -> xml.showCategoryTabs })
        }
    }
    item {
        val localizeHelper = LocalLocalizeHelper.currentOrThrow
        ClickableRow(onClick = { vm.showAllCategoryTab.value = !vm.showAllCategoryTab.value }) {
            Checkbox(
                modifier = Modifier.padding(horizontal = 16.dp),
                checked = vm.showAllCategoryTab.value,
                onCheckedChange = {
                    vm.showAllCategoryTab.value = it
                }
            )
            MidSizeTextComposable(text = localizeHelper.localize { xml -> xml.showAllCategoryTab })
        }
    }
    item {
        val localizeHelper = LocalLocalizeHelper.currentOrThrow
        ClickableRow(onClick = { vm.showCountInCategory.value = !vm.showCountInCategory.value }) {
            Checkbox(
                modifier = Modifier.padding(horizontal = 16.dp),
                checked = vm.showCountInCategory.value,
                onCheckedChange = {
                    vm.showCountInCategory.value = it
                }
            )
            MidSizeTextComposable(text = localizeHelper.localize { xml -> xml.showCountInCategoryTab })
        }
    }
}
