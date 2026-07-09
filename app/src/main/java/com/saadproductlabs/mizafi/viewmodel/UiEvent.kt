package com.saadproductlabs.mizafi.viewmodel

import androidx.annotation.StringRes

sealed interface UiEvent {
    data class Message(
        @StringRes val textRes: Int,
        @StringRes val actionLabelRes: Int? = null
    ) : UiEvent

    data class Error(@StringRes val textRes: Int) : UiEvent
}
