package org.ireader.presentation.feature_ttl

import androidx.activity.OnBackPressedDispatcher
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.ireader.core.R
import org.ireader.domain.feature_services.io.BookCover
import org.ireader.domain.models.entities.Chapter
import org.ireader.presentation.feature_reader.presentation.reader.ReaderScreenDrawer
import org.ireader.presentation.feature_reader.presentation.reader.components.SettingItemComposable
import org.ireader.presentation.feature_reader.presentation.reader.components.SettingItemToggleComposable
import org.ireader.presentation.feature_reader.presentation.reader.viewmodel.ReaderScreenViewModel
import org.ireader.presentation.presentation.Toolbar
import org.ireader.presentation.presentation.components.BookImageComposable
import org.ireader.presentation.presentation.components.showLoading
import org.ireader.presentation.presentation.reusable_composable.AppIconButton
import org.ireader.presentation.presentation.reusable_composable.BigSizeTextComposable
import org.ireader.presentation.presentation.reusable_composable.MidSizeTextComposable
import org.ireader.presentation.presentation.reusable_composable.SuperSmallTextComposable
import tachiyomi.source.Source
import java.math.RoundingMode


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TTSScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    vm: ReaderScreenViewModel,
    onPrev: () -> Unit,
    onNextPar: () -> Unit,
    onPrevPar: () -> Unit,
    onPlay: () -> Unit,
    onNext: () -> Unit,
    source: Source,
    onChapter: (Chapter) -> Unit,
) {
    navController.setOnBackPressedDispatcher(OnBackPressedDispatcher {

    })
    val context = LocalContext.current

    val drawerScrollState = rememberLazyListState()
    val scrollState = rememberLazyListState()
    val chapter = vm.stateChapter
    val chapters = vm.stateChapters
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()
    val bottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    LaunchedEffect(key1 = scaffoldState.drawerState.targetValue) {
        if (chapter != null && scaffoldState.drawerState.targetValue == DrawerValue.Open && vm.stateChapters.isNotEmpty()) {
            drawerScrollState.scrollToItem(vm.getCurrentIndexOfChapter(chapter))
        }
    }
    ModalBottomSheetLayout(
        modifier = Modifier.systemBarsPadding(),
        sheetContent = {
            Box(modifier.defaultMinSize(minHeight = 1.dp)) {
                Column(modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)) {
                    VoiceChip(viewModel = vm, modifier = Modifier.height(32.dp))
                    LanguageChip(viewModel = vm, modifier = Modifier.height(32.dp))
                    SettingItemToggleComposable(text = "Auto Next Chapter",
                        value = vm.autoNextChapter,
                        onToggle = { vm.autoNextChapter = !vm.autoNextChapter })
                    SettingItemComposable(text = "Speech Rate",
                        value = vm.speechSpeed.toString(),
                        onAdd = {
                            vm.speechSpeed += .1f
                            vm.speechSpeed =
                                vm.speechSpeed.toBigDecimal().setScale(1, RoundingMode.FLOOR)
                                    .toFloat()
                            vm.speechPrefUseCases.saveRate(vm.speechSpeed)
                        },
                        onMinus = {
                            vm.speechSpeed -= .1f
                            vm.speechSpeed =
                                vm.speechSpeed.toBigDecimal().setScale(1, RoundingMode.FLOOR)
                                    .toFloat()
                            vm.speechPrefUseCases.saveRate(vm.speechSpeed)
                        })

                    SettingItemComposable(text = "Pitch",
                        value = vm.pitch.toString(),
                        onAdd = {
                            vm.pitch += .1f
                            vm.pitch =
                                vm.pitch.toBigDecimal().setScale(1, RoundingMode.FLOOR).toFloat()
                            vm.speechPrefUseCases.savePitch(vm.speechSpeed)
                        },
                        onMinus = {
                            vm.pitch -= .1f
                            vm.pitch =
                                vm.pitch.toBigDecimal().setScale(1, RoundingMode.FLOOR).toFloat()
                            vm.speechPrefUseCases.savePitch(vm.speechSpeed)

                        })

                }
            }
        },
        sheetState = bottomSheetState,
        sheetBackgroundColor = MaterialTheme.colors.background,
        sheetContentColor = MaterialTheme.colors.onBackground,
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize(),

            scaffoldState = scaffoldState,
            topBar = {
                Toolbar(
                    title = {},
                    navigationIcon = {
                        AppIconButton(imageVector = Icons.Default.ArrowBack,
                            title = "Return to Reader Screen",
                            onClick = {
                                vm.voiceMode = false
                            })
                    },
                )
            },
            drawerGesturesEnabled = true,
            drawerBackgroundColor = MaterialTheme.colors.background,
            drawerContent = {
                ReaderScreenDrawer(
                    modifier = Modifier.statusBarsPadding(),
                    onReverseIcon = {
                        if (chapter != null) {
                            vm.reverseChapters()
                            scope.launch {
                                vm.getLocalChaptersByPaging(chapter.bookId)
                            }
                        }
                    },
                    onChapter = onChapter,
                    chapter = chapter,
                    source = source,
                    chapters = chapters,
                    drawerScrollState = drawerScrollState
                )

            }
        ) {
            Column(modifier = Modifier
                .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                vm.book?.let { book ->
                    vm.stateChapter?.let { chapter ->
                        Column(modifier = Modifier
                            .fillMaxWidth(),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            BookImageComposable(
                                image = BookCover.from(book),
                                modifier = Modifier
                                    .padding(8.dp)
                                    .height(150.dp)
                                    .width(120.dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .border(2.dp,
                                        MaterialTheme.colors.onBackground.copy(alpha = .2f)),
                                contentScale = ContentScale.Crop,
                            )

                            BigSizeTextComposable(text = chapter.title, align = TextAlign.Center)
                            MidSizeTextComposable(text = book.title, align = TextAlign.Center)
                            vm.stateChapter?.let { chapter ->
                                SuperSmallTextComposable(text = "${vm.currentReadingParagraph}/${chapter.content.size - 1}")

                            }
                        }
                        Text(
                            modifier = modifier

                                .fillMaxWidth()
                                .align(Alignment.CenterHorizontally)
                                .padding(horizontal = vm.paragraphsIndent.dp,
                                    vertical = 4.dp),
                            text = if (chapter.content.isNotEmpty() && vm.currentReadingParagraph != chapter.content.size) chapter.content[vm.currentReadingParagraph] else "",
                            fontSize = vm.fontSize.sp,
                            fontFamily = vm.font.fontFamily,
                            textAlign = TextAlign.Start,
                            color = MaterialTheme.colors.onBackground,
                            lineHeight = vm.lineHeight.sp,
                            maxLines = 12,
                        )
                        Column(modifier = Modifier
                            .fillMaxWidth(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            TTLScreenSetting(
                                onSetting = {
                                    scope.launch {
                                        bottomSheetState.show()
                                    }
                                },
                                onContent = {
                                    scope.launch {
                                        scaffoldState.drawerState.animateTo(DrawerValue.Open,
                                            TweenSpec())
                                    }
                                }
                            )
                            TTLScreenPlay(
                                modifier = Modifier.padding(bottom = 46.dp, top = 32.dp),
                                onPlay = onPlay,
                                onNext = onNext,
                                onPrev = onPrev,
                                vm = vm,
                                onNextPar = onNextPar,
                                onPrevPar = onPrevPar
                            )
                        }


                    }
                }

            }


        }

    }
}


@Composable
private fun TTLScreenSetting(
    onSetting: () -> Unit,
    onContent: () -> Unit,
) {
    Row(modifier = Modifier
        .fillMaxWidth()
        //   .padding(16.dp)
        .height(80.dp)
        .border(width = 1.dp, color = MaterialTheme.colors.onBackground.copy(.1f)),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically) {
        Row(modifier = Modifier
            .clickable { onContent() }
            .weight(1f)
            .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center) {
            Icon(modifier = Modifier.size(40.dp), imageVector = Icons.Default.List,
                contentDescription = stringResource(id = R.string.content).uppercase())
            Spacer(modifier = Modifier.width(16.dp))
            MidSizeTextComposable(text = stringResource(id = R.string.content).uppercase())
        }
        Divider(modifier = Modifier
            .width(1.dp)
            .fillMaxHeight())
        Row(modifier = Modifier
            .clickable { onSetting() }
            .weight(1f)
            .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center) {
            Icon(modifier = Modifier.size(40.dp), imageVector = Icons.Default.Settings,
                contentDescription = stringResource(id = R.string.settings).uppercase())
            Spacer(modifier = Modifier.width(16.dp))
            MidSizeTextComposable(text = stringResource(id = R.string.settings).uppercase())
        }
    }
}

@Composable
private fun TTLScreenPlay(
    modifier: Modifier = Modifier,
    vm: ReaderScreenViewModel,
    onPrev: () -> Unit,
    onPlay: () -> Unit,
    onNext: () -> Unit,
    onNextPar: () -> Unit,
    onPrevPar: () -> Unit,
) {
    Row(modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround) {
            AppIconButton(modifier = Modifier.size(50.dp),
                imageVector = Icons.Filled.SkipPrevious,
                title = "Previous Paragraph",
                onClick = onPrev,
                tint = MaterialTheme.colors.onBackground)
            AppIconButton(modifier = Modifier.size(50.dp),
                imageVector = Icons.Filled.FastRewind,
                title = "Previous",
                onClick = onPrevPar,
                tint = MaterialTheme.colors.onBackground)
            Box(modifier = Modifier
                .size(100.dp)
                .border(1.dp, MaterialTheme.colors.onBackground.copy(.4f), CircleShape),
                contentAlignment = Alignment.Center) {
                when {
                    vm.isLoading || vm.isRemoteLoading -> {
                        showLoading()
                    }
                    vm.isPlaying -> {
                        AppIconButton(modifier = Modifier.size(80.dp),
                            imageVector = Icons.Filled.Pause,
                            title = "Play",
                            onClick = onPlay,
                            tint = MaterialTheme.colors.onBackground)
                    }
                    else -> {
                        AppIconButton(modifier = Modifier.size(80.dp),
                            imageVector = Icons.Filled.PlayArrow,
                            title = "Play",
                            onClick = onPlay,
                            tint = MaterialTheme.colors.onBackground)
                    }
                }

            }

            AppIconButton(modifier = Modifier.size(50.dp),
                imageVector = Icons.Filled.FastForward,
                title = "Next Paragraph",
                onClick = onNextPar,
                tint = MaterialTheme.colors.onBackground)
            AppIconButton(modifier = Modifier.size(50.dp),
                imageVector = Icons.Filled.SkipNext,
                title = "Next",
                onClick = onNext,
                tint = MaterialTheme.colors.onBackground)


        }
    }
}

