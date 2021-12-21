package ir.kazemcodes.infinity.presentation.reader

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.font.FontFamily
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.kazemcodes.infinity.domain.local_feature.domain.use_case.LocalUseCase
import ir.kazemcodes.infinity.domain.models.Book
import ir.kazemcodes.infinity.domain.models.Chapter
import ir.kazemcodes.infinity.domain.models.Resource
import ir.kazemcodes.infinity.domain.network.models.Source
import ir.kazemcodes.infinity.domain.use_cases.remote.RemoteUseCase
import ir.kazemcodes.infinity.presentation.book_detail.PreferenceKeys.SAVED_BRIGHTNESS_PREFERENCES
import ir.kazemcodes.infinity.presentation.book_detail.PreferenceKeys.SAVED_FONT_PREFERENCES
import ir.kazemcodes.infinity.presentation.book_detail.PreferenceKeys.SAVED_FONT_SIZE_PREFERENCES
import ir.kazemcodes.infinity.presentation.theme.poppins
import ir.kazemcodes.infinity.presentation.theme.sourceSansPro
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber


class ReaderScreenViewModel(
    private val localUseCase: LocalUseCase,
    private val remoteUseCase: RemoteUseCase,
    private val dataStore: DataStore<Preferences>,
    private val source: Source,
    private val  chapter: Chapter,
    private val book: Book
) : ViewModel() {

    private val _state = mutableStateOf(ReaderScreenState())
    val state: State<ReaderScreenState> = _state
    private val fontSizeDataStore = intPreferencesKey(SAVED_FONT_SIZE_PREFERENCES)
    private val fontDatastore = stringPreferencesKey(SAVED_FONT_PREFERENCES)
    private val brightnessDatastore = floatPreferencesKey(SAVED_BRIGHTNESS_PREFERENCES)

    init {
        readFromDatastore()
        getContent(chapter = chapter.copy(bookName = book.bookName))

    }


    fun onEvent(event: ReaderEvent) {
        when(event) {
            is ReaderEvent.ChangeBrightness -> {
                changeBrightness(event.brightness)
            }
            is ReaderEvent.ChangeFontSize -> {
                changeFontSize(event.fontSizeEvent)
            }
            is ReaderEvent.ChangeFont -> {
                changeFont(event.fontFamily)
            }
            is ReaderEvent.GetContent -> {
                getContent(event.chapter)
            }
        }
    }

    private fun changeBrightness(brightness : Float) {
        _state.value = state.value.copy(brightness = brightness)
        viewModelScope.launch(Dispatchers.IO) {
            dataStore.edit { preferences ->
                val currentFontSize = preferences[fontSizeDataStore] ?: 18
                preferences[brightnessDatastore] = brightness
            }
        }
    }

    private fun changeFontSize(event: FontSizeEvent) {
        if (event == FontSizeEvent.Increase) {
            _state.value = state.value.copy(fontSize = state.value.fontSize+1)
            viewModelScope.launch(Dispatchers.IO) {
                dataStore.edit { preferences ->
                    val currentFontSize = preferences[fontSizeDataStore] ?: 18
                    preferences[fontSizeDataStore] = currentFontSize + 1
                }
            }
        } else {
            _state.value = state.value.copy(fontSize = state.value.fontSize-1)
            viewModelScope.launch(Dispatchers.IO) {
                dataStore.edit { preferences ->
                    val currentFontSize = preferences[fontSizeDataStore] ?: 18
                    preferences[fontSizeDataStore] = currentFontSize - 1
                }
            }
        }
    }

    private fun changeFont(fontFamily: FontFamily) {
        _state.value = state.value.copy(font = fontFamily)
        viewModelScope.launch(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[fontDatastore] = fontFamily.toString()
            }
        }
    }



    private fun getContent(chapter: Chapter) {
        _state.value = state.value.copy(chapter = chapter)
        if (chapter.content == null) {
            getReadingContentLocally()
        }
    }

    private fun getReadingContentLocally() {
        localUseCase.getLocalChapterReadingContentUseCase(state.value.chapter).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    if (result.data?.content != null) {
                        Timber.d("getReadingContentLocally Copying" + _state.value)
                        _state.value = state.value.copy(
                            chapter = state.value.chapter.copy(content = result.data.content),
                            isLoading = false,
                            error = ""
                        )
                    } else {
                        if (state.value.chapter.content == null) {

                            getReadingContentRemotely()
                        }
                    }
                }
                is Resource.Error -> {
                    _state.value =
                        state.value.copy(
                            error = result.message ?: "An Unknown Error Occurred",
                            isLoading = false
                        )
                }
                is Resource.Loading -> {
                    _state.value = state.value.copy(isLoading = true, error = "")
                }
            }

        }.launchIn(viewModelScope)
    }


    private fun getReadingContentRemotely() {
        Timber.d("getReadingContentRemotely Successfully Triggered")
        remoteUseCase.getRemoteReadingContentUseCase(state.value.chapter,source = source).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    Timber.d("getReadingContentRemotely Successfully Called")
                    Timber.d("TAG "+ result.data)
                    _state.value = state.value
                        .copy(
                            chapter = state.value.chapter.copy(content = result.data),
                            isLoading = false,
                            error = ""
                        )
                    if (!state.value.chapter.content.isNullOrBlank()) {
                        Timber.d("insertChapterContent Successfully Called")
                        updateChapterContent(state.value.chapter)
                    }
                }
                is Resource.Error -> {
                    _state.value =
                        state.value.copy(
                            error = result.message ?: "An Unknown Error Occurred",
                            isLoading = false
                        )
                }
                is Resource.Loading -> {
                    _state.value = state.value.copy(isLoading = true, error = "")
                }
            }
        }.launchIn(viewModelScope)


    }

    private fun updateChapterContent(chapter: Chapter) {
        viewModelScope.launch(Dispatchers.IO) {
            localUseCase.UpdateLocalChapterContentUseCase(chapter)
        }
    }



    private fun readFromDatastore() {
        viewModelScope.launch(Dispatchers.IO) {
            getFontSizeFromDatastore().collectLatest {
                _state.value = state.value.copy(fontSize = it)

            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            getFontFromDatastore().collectLatest {
                _state.value = state.value.copy(font = convertStringToFont(it))
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            getBrightnessFromDatastore().collectLatest {
                _state.value = state.value.copy(brightness = it)
            }
        }
    }


    private fun getFontFromDatastore(): Flow<String> {
        return dataStore.data
            .map { preferences ->
                preferences[fontDatastore] ?: poppins.toString()
            }
    }
    private fun getBrightnessFromDatastore(): Flow<Float> {
        return dataStore.data
            .map { preferences ->
                preferences[brightnessDatastore] ?: 1f
            }
    }

    private fun getFontSizeFromDatastore(): Flow<Int> {
        return dataStore.data
            .map { preferences ->
                preferences[fontSizeDataStore] ?: 18
            }
    }



    private fun convertStringToFont(font: String?): FontFamily {
        return if (font == poppins.toString()) {
            poppins
        } else if (font == sourceSansPro.toString()) {
            sourceSansPro
        } else {
            poppins
        }
    }

    fun convertFontIntoString(fontFamily: FontFamily): String {
        return if (fontFamily == poppins) {
            "Poppins"
        } else if (fontFamily == sourceSansPro) {
            "Source Sans Pro"
        } else {
            "Unknown"
        }
    }

}