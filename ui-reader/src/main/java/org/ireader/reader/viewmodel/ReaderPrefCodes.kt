package org.ireader.reader.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.view.WindowManager
import androidx.compose.ui.graphics.Color
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.ireader.common_extensions.brightness
import org.ireader.common_extensions.findComponentActivity
import org.ireader.common_extensions.hideSystemUI
import org.ireader.common_extensions.isImmersiveModeEnabled
import org.ireader.common_extensions.showSystemUI
import org.ireader.core_ui.theme.readerThemes
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

interface ReaderPrefFunctions {
    fun ReaderScreenViewModel.toggleReaderMode(enable: Boolean? = null)
    fun ReaderScreenViewModel.saveBrightness(brightness: Float, context: Context)

    fun ReaderScreenViewModel.toggleAutoScrollMode()
    fun ReaderScreenViewModel.changeBackgroundColor(colorIndex: Int)
    fun ReaderScreenViewModel.setReaderBackgroundColor(color: Color)
    suspend fun ReaderScreenViewModel.readBrightness(context: Context)
    suspend fun ReaderScreenViewModel.readOrientation(context: Context)
    fun ReaderScreenViewModel.setReaderTextColor(color: Color)
    suspend fun ReaderScreenViewModel.readImmersiveMode(context: Context, onHideNav: (Boolean) -> Unit, onHideStatus: (Boolean) -> Unit)
    fun ReaderScreenViewModel.showSystemBars(context: Context)
    fun ReaderScreenViewModel.hideSystemBars(context: Context)
}

class ReaderPrefFunctionsImpl @Inject constructor() : ReaderPrefFunctions {

    override fun ReaderScreenViewModel.toggleReaderMode(enable: Boolean?) {
        isReaderModeEnable = enable ?: !state.isReaderModeEnable
        isMainBottomModeEnable = true
        isSettingModeEnable = false
    }

    override fun ReaderScreenViewModel.saveBrightness(brightness: Float, context: Context) {
        this.brightness.value = brightness
        val activity = context.findComponentActivity()
        if (activity != null) {
            activity.brightness(brightness)
            readerUseCases.brightnessStateUseCase.saveBrightness(brightness)
        }
    }

    override suspend fun ReaderScreenViewModel.readImmersiveMode(
        context: Context,
        onHideNav: (Boolean) -> Unit,
        onHideStatus: (Boolean) -> Unit
    ) {
        context.findComponentActivity()?.let { activity ->

            if (immersiveMode.value) {
                onHideNav(true)
                onHideStatus(true)
                hideSystemBars(context = context)
            } else if (activity.isImmersiveModeEnabled) {
                onHideNav(false)
                onHideStatus(false)
                showSystemBars(context)
            }
        }
    }

    override fun ReaderScreenViewModel.toggleAutoScrollMode() {
        autoScrollMode = !autoScrollMode
    }

    override suspend fun ReaderScreenViewModel.readBrightness(context: Context) {
        val activity = context.findComponentActivity()
        if (activity != null) {
            val window = activity.window
            if (!autoBrightnessMode.value) {
                val layoutParams: WindowManager.LayoutParams = window.attributes
                layoutParams.screenBrightness = brightness.value
                window.attributes = layoutParams
                // this.brightness = brightness
            } else {
                val layoutParams: WindowManager.LayoutParams = window.attributes
                showSystemBars(context = context)
                layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                window.attributes = layoutParams
            }
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override suspend fun ReaderScreenViewModel.readOrientation(context: Context) {
        val activity = context.findComponentActivity()
        val lastCheck = Instant.fromEpochMilliseconds(lastOrientationChangedTime.value)
        val now = Clock.System.now()
        if (activity != null && (now - lastCheck) > 1.seconds) {
            activity.requestedOrientation = orientation.value
            lastOrientationChangedTime.value = Clock.System.now().toEpochMilliseconds()
        } else {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    override fun ReaderScreenViewModel.changeBackgroundColor(colorIndex: Int) {
        val bgColor = readerThemes[colorIndex].backgroundColor
        val textColor = readerThemes[colorIndex].onTextColor
        backgroundColor.value = bgColor
        this.textColor.value = textColor
        setReaderBackgroundColor(bgColor)
        setReaderTextColor(textColor)
    }

    override fun ReaderScreenViewModel.setReaderBackgroundColor(color: Color) {
        readerUseCases.backgroundColorUseCase.save(color)
    }

    override fun ReaderScreenViewModel.setReaderTextColor(color: Color) {
        readerUseCases.textColorUseCase.save(color)
    }

    override fun ReaderScreenViewModel.showSystemBars(context: Context) {
        context.findComponentActivity()?.showSystemUI()
    }

    override fun ReaderScreenViewModel.hideSystemBars(context: Context) {
        context.findComponentActivity()?.hideSystemUI()
    }
}
