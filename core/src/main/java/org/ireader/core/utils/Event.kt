package org.ireader.core.utils

import android.content.Context
import org.ireader.core.R

abstract class Event

sealed class UiEvent : Event() {
    data class ShowSnackbar(val uiText: UiText) : UiEvent()
    data class Navigate(val route: String) : UiEvent()
    object NavigateUp : UiEvent()
    object OnLogin : UiEvent()
}

sealed class UiText {
    data class DynamicString(val text: String) : UiText()
    data class StringResource(val resId: Int) : UiText()
    data class ExceptionString(val e: Throwable) : UiText()

    fun asString(context: Context): String {
        return when (this) {
            is DynamicString -> text
            is StringResource -> context.getString(resId)
            is ExceptionString -> e.localizedMessage ?: context.getString(R.string.error_unknown)
        }
    }
}

