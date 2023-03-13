package ireader.domain.di

import android.app.Service
import androidx.compose.ui.text.ExperimentalTextApi
import ireader.domain.preferences.prefs.AndroidUiPreferences
import ireader.domain.preferences.prefs.PlatformUiPreferences
import ireader.domain.services.downloaderService.DefaultNotificationHelper
import ireader.domain.services.downloaderService.DownloaderService
import ireader.domain.services.extensions_insstaller_service.ExtensionManagerService
import ireader.domain.services.library_update_service.LibraryUpdatesService
import ireader.domain.services.tts_service.TTSStateImpl
import ireader.domain.services.tts_service.media_player.TTSService
import ireader.domain.services.update_service.UpdateService
import ireader.domain.usecases.backup.AutomaticBackup
import ireader.domain.usecases.epub.EpubCreator
import ireader.domain.usecases.epub.ImportEpub
import ireader.domain.usecases.file.AndroidFileSaver
import ireader.domain.usecases.preferences.*
import ireader.domain.usecases.reader.ScreenAlwaysOn
import ireader.domain.usecases.reader.ScreenAlwaysOnImpl
import ireader.domain.usecases.services.*
import ireader.i18n.LocalizeHelper
import org.kodein.di.*

@OptIn(ExperimentalTextApi::class)
actual val DomainModule: DI.Module = DI.Module("domainModulePlatform") {
    bindProvider {
        DownloaderService(
                instance(),
                instance(),
                instance()
        )
    }
    bindSingleton {
        ExtensionManagerService(
                instance(),
                instance(),

                )
    }
    bindSingleton {
        UpdateService(instance(), instance())
    }
    bindSingleton {
        LibraryUpdatesService(
                instance(),
                instance(),
        )
    }

    bindProvider<Service>() {
        TTSService()
    }
    bindSingleton {
        AutomaticBackup(
                instance(),
                instance(),
                instance(),
                instance()
        )
    }
    bindProvider { ireader.domain.usecases.history.HistoryPagingUseCase(instance()) }
    bindProvider { ImportEpub(instance(), instance(), instance()) }
    bindProvider<TTSStateImpl> { ireader.domain.services.tts_service.TTSStateImpl() }
    bindProvider { ireader.domain.services.update_service.UpdateApi(instance()) }
    bindProvider {
        ireader.domain.services.extensions_insstaller_service.GetDefaultRepo(
                instance(),
                instance()
        )
    }
    bindProvider { ireader.domain.usecases.services.StartDownloadServicesUseCase(instance()) }
    bindProvider { ireader.domain.usecases.services.StartLibraryUpdateServicesUseCase(instance()) }
    bindProvider { ireader.domain.usecases.services.StartTTSServicesUseCase(instance()) }
    bindProvider { ireader.domain.usecases.services.StopServiceUseCase(instance()) }
    bindProvider {
        TextReaderPrefUseCase(
                instance(),
                instance()
        )
    }
    bindProvider {
        StartExtensionManagerService(
                instance()
        )
    }
    bindSingleton { ireader.domain.usecases.files.GetSimpleStorage(instance(), instance()) }
    bindSingleton<DefaultNotificationHelper> { new(::DefaultNotificationHelper) }
    bindProvider<ScreenAlwaysOn> {2
        ScreenAlwaysOnImpl(instance())
    }
    bindSingleton {
        new(::AndroidFileSaver)
    }
    bindSingleton {
        AndroidReaderPrefUseCases(
                selectedFontStateUseCase = SelectedFontStateUseCase(instance(),instance()),
                backgroundColorUseCase = BackgroundColorUseCase(instance()),
                textAlignmentUseCase = TextAlignmentUseCase(instance()),
                textColorUseCase = TextColorUseCase(instance())
        )
    }
    bindSingleton<PlatformUiPreferences> {
        new(::AndroidUiPreferences)
    }
    bindProvider<EpubCreator> { EpubCreator(instance(), instance(),instance()) }
    bindSingleton<ServiceUseCases> { ServiceUseCases(
            startDownloadServicesUseCase = StartDownloadServicesUseCase(instance()),
            startLibraryUpdateServicesUseCase = StartLibraryUpdateServicesUseCase(instance()),
            startTTSServicesUseCase = StartTTSServicesUseCase(instance()),
            stopServicesUseCase = StopServiceUseCase(instance()),
    ) }

    bindSingleton { LocalizeHelper(instance()) }






}