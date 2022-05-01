package org.ireader.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.ireader.components.reusable_composable.AppIconButton
import org.ireader.components.reusable_composable.MidSizeTextComposable

@Composable
fun FilterMenuItem(
    filter: String,
    onSelected: (key: Int) -> Unit,
    currentItem: String? = null,
    items: Array<String>,
) {
    var opened by remember {
        mutableStateOf(false)
    }
    Column(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MidSizeTextComposable(modifier = Modifier.weight(1f), text = filter)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        opened = true
                    }
                    .weight(1f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MidSizeTextComposable(text = currentItem ?: items.first())
                AppIconButton(
                    imageVector = Icons.Default.ArrowDropDown,
                    title = "",
                    onClick = {
                        opened = true
                    }
                )
            }
            DropdownMenu(
                modifier = Modifier
                    .background(MaterialTheme.colors.background)
                    .fillMaxWidth(.5f),
                expanded = opened, // viewModel.state.isMenuExpanded,
                onDismissRequest = {
                    opened = false
                },
            ) {
                items.forEachIndexed { index, s ->
                    DropdownMenuItem(onClick = {
                        opened = false
                        onSelected(index)
                    }) {
                        MidSizeTextComposable(text = s)
                    }
                }
            }
        }
    }
}

@Composable
fun SortedByScreen() {
}

@Preview(showBackground = true)
@Composable
fun FilterItemPrev() {
    FilterMenuItem(
        "Sort By:",
        { s ->
        },
        "Latest",
        arrayOf(
            "Latest",
            "Popular"
        )
    )
}