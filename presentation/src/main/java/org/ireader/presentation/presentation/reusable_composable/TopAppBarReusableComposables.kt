package org.ireader.presentation.presentation.reusable_composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun TopAppBarTitle(
    modifier: Modifier = Modifier,
    title: String,
    color: Color? = null,
    style: TextStyle? = null,
    fontWeight: FontWeight? = null,
    overflow: TextOverflow? = null,
    align: TextAlign? = null,
) {
    Text(
        modifier = modifier,
        text = title,
        color = color ?: MaterialTheme.colors.onBackground,
        style = style ?: MaterialTheme.typography.subtitle1,
        fontWeight = fontWeight ?: FontWeight.Bold,
        overflow = overflow ?: TextOverflow.Ellipsis,
        textAlign = align ?: TextAlign.Start,
    )
}

@Composable
fun MidSizeTextComposable(
    modifier: Modifier = Modifier,
    text: String,
    color: Color? = null,
    style: TextStyle? = null,
    fontWeight: FontWeight? = null,
    overflow: TextOverflow? = null,
    maxLine: Int = Int.MAX_VALUE,
    align: TextAlign? = null,
) {
    Text(
        modifier = modifier,
        text = text,
        color = color ?: MaterialTheme.colors.onBackground,
        style = style ?: MaterialTheme.typography.subtitle2,
        fontWeight = fontWeight ?: FontWeight.SemiBold,
        overflow = overflow ?: TextOverflow.Ellipsis,
        textAlign = align ?: TextAlign.Start,
        maxLines = maxLine
    )
}

@Composable
fun SmallTextComposable(
    modifier: Modifier = Modifier,
    text: String,
    color: Color? = null,
    style: TextStyle? = null,
    fontWeight: FontWeight? = null,
    overflow: TextOverflow? = null,
    maxLine: Int = Int.MAX_VALUE,
    align: TextAlign? = null,
) {
    Text(
        modifier = modifier,
        text = text,
        color = color ?: MaterialTheme.colors.onBackground,
        style = style ?: MaterialTheme.typography.caption,
        fontWeight = fontWeight ?: FontWeight.SemiBold,
        overflow = overflow ?: TextOverflow.Ellipsis,
        textAlign = align ?: TextAlign.Start,
        maxLines = maxLine
    )
}

@Composable
fun SuperSmallTextComposable(
    modifier: Modifier = Modifier,
    title: String,
    color: Color? = null,
    style: TextStyle? = null,
    fontWeight: FontWeight? = null,
    overflow: TextOverflow? = null,
    maxLine: Int = Int.MAX_VALUE,
    align: TextAlign? = null,

    ) {
    Text(
        modifier = modifier,
        text = title,
        color = color ?: MaterialTheme.colors.onBackground,
        style = style ?: MaterialTheme.typography.caption,
        fontWeight = fontWeight ?: FontWeight.Normal,
        fontSize = 12.sp,
        overflow = overflow ?: TextOverflow.Ellipsis,
        maxLines = maxLine,
        textAlign = align ?: TextAlign.Start,
    )
}

@Composable
fun TopAppBarActionButton(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    title: String,
    onClick: () -> Unit,
    tint: Color? = null,
) {
    IconButton(
        onClick = {
            onClick()
        },
    ) {
        Icon(
            modifier = modifier,
            imageVector = imageVector,
            contentDescription = "$title Icon",
            tint = tint ?: MaterialTheme.colors.onBackground
        )
    }
}

@Composable
fun TopAppBarBackButton(navController: NavController, onClick: (() -> Unit?)? = null) {
    IconButton(onClick = {
        navController.popBackStack()
        if (onClick != null) {
            onClick()
        }
    }) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "ArrowBack Icon",
            tint = MaterialTheme.colors.onBackground,
        )
    }
}

@Composable
fun TopAppBarSearch(
    query: String,
    onValueChange: (value: String) -> Unit,
    onSearch: () -> Unit,
    isSearchModeEnable: Boolean = false,
) {
    val focusManager = LocalFocusManager.current
    Box {
        if (!isSearchModeEnable) {
            Text(
                text = "Search...",
                style = MaterialTheme.typography.subtitle1,
                color = MaterialTheme.colors.onBackground.copy(alpha = .7F)
            )
        }
        BasicTextField(
            modifier = Modifier.fillMaxWidth(),
            value = query,
            onValueChange = {
                onValueChange(it)
            },
            maxLines = 1,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                onSearch()
            }),
            singleLine = true,
            textStyle = TextStyle(color = MaterialTheme.colors.onBackground),
        )
    }
}