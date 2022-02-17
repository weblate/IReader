package org.ireader.presentation.feature_library.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.ireader.domain.models.layouts
import org.ireader.domain.view_models.library.LibraryEvents
import org.ireader.domain.view_models.library.LibraryViewModel


@Composable
fun DisplayScreen(viewModel: LibraryViewModel) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        layouts.forEach { layout ->
            RadioButtonWithTitleComposable(
                text = layout.title,
                selected = viewModel.state.layout == layout.layout,
                onClick = {
                    viewModel.onEvent(LibraryEvents.OnLayoutTypeChange(layout))
                }
            )
        }
    }
}