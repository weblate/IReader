package ir.kazemcodes.infinity.core.domain.use_cases.local.chapter_usecases

import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.models.Chapter
import ir.kazemcodes.infinity.core.domain.repository.LocalChapterRepository
import ir.kazemcodes.infinity.core.utils.Constants
import ir.kazemcodes.infinity.core.utils.Resource
import ir.kazemcodes.infinity.core.utils.UiText
import ir.kazemcodes.infinity.core.utils.asString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

/**
 * get one Chapter using a chapterId
 * note: if nothing is found it return a resource of error
 */
class GetOneChapterById(private val localChapterRepository: LocalChapterRepository) {
    operator fun invoke(
        chapterId: Int,
    ): Flow<Resource<Chapter>> = flow {
        try {
            localChapterRepository.getOneChapterById(chapterId = chapterId).first { chapters ->
                if (chapters != null) {
                    emit(Resource.Success<Chapter>(data = chapters))
                    true
                } else {
                    emit(Resource.Error<Chapter>(uiText = UiText.noChapter()))
                    true
                }

            }
        } catch (e: Exception) {
            Resource.Error<Resource<List<Book>>>(
                uiText = UiText.DynamicString(e.localizedMessage ?: Constants.UNKNOWN_ERROR).asString()
            )
        }
    }
}