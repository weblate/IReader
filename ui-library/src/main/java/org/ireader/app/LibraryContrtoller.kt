@file:OptIn(ExperimentalPagerApi::class)

package org.ireader.app

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewModelScope
import com.google.accompanist.pager.ExperimentalPagerApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.ireader.Controller
import org.ireader.app.viewmodel.LibraryViewModel
import org.ireader.common_models.entities.BookItem
import org.ireader.common_models.entities.toBookCategory

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable
fun LibraryController(
    modifier: Modifier,
    vm: LibraryViewModel,
    controller: Controller,
    goToReader: (BookItem) -> Unit,
    goToDetail: (BookItem) -> Unit,

) {

    LibraryScreen(
        modifier = modifier,
        onMarkAsRead = {
            vm.markAsRead()
        },
        onDownload = {
            vm.downloadChapters()
        },
        onMarkAsNotRead = {
            vm.markAsNotRead()
        },
        onDelete = {
            vm.deleteBooks()
        },
        goToLatestChapter = { book ->
            goToReader(book)
        },
        onBook = { book ->
            if (vm.selectionMode) {
                if (book.id in vm.selectedBooks) {
                    vm.selectedBooks.remove(book.id)
                } else {
                    vm.selectedBooks.add(book.id)
                }
            } else {
                goToDetail(book)
            }
        },
        onLongBook = {
            if (it.id in vm.selectedBooks) return@LibraryScreen
            vm.selectedBooks.add(it.id)
        },
        vm = vm,
        refreshUpdate = {
            vm.refreshUpdate()
        },
        bottomSheetState = controller.sheetState,
        onClickChangeCategory = {
            vm.showDialog = true
        },
        scaffoldPadding = controller.scaffoldPadding,
        requestHideBottomNav = controller.requestHideNavigator,
        getColumnsForOrientation = { isLandscape ->
            vm.getColumnsForOrientation(isLandscape, this)
        },
        editCategoryDismissDialog = {
            vm.showDialog = false
            vm.selectedBooks.clear()
            vm.addQueues.clear()
            vm.deleteQueues.clear()
        },
        editCategoryOnAddDeleteQueue = { category ->
            vm.deleteQueues.addAll(category.toBookCategory(vm.selectedBooks))
        },
        editCategoryOnAddToInsertQueue = { category ->
            vm.addQueues.addAll(category.toBookCategory(vm.selectedBooks))
        },
        editCategoryOnConfirm = {
            vm.viewModelScope.launch(Dispatchers.IO) {
                vm.getCategory.insertBookCategory(vm.addQueues)
                vm.getCategory.deleteBookCategory(vm.deleteQueues)
                vm.deleteQueues.clear()
                vm.addQueues.clear()
                vm.selectedBooks.clear()
                vm.addQueues.clear()
                vm.deleteQueues.clear()
            }
            vm.showDialog = false
        },
        editCategoryOnRemoteInDeleteQueue = { category ->
            vm.deleteQueues.removeIf { it.categoryId == category.id }
        },
        editCategoryOnRemoteInInsertQueue = { category ->
            vm.addQueues.removeIf { it.categoryId == category.id }
        },
        onPagerPageChange = {
            vm.setSelectedPage(it)
        }
    )
}
