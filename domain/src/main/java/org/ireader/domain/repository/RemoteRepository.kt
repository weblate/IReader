package org.ireader.domain.repository

import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.ExploreType
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.models.source.ContentPage
import org.ireader.domain.models.source.Source
import org.ireader.domain.utils.Resource

interface RemoteRepository {


    suspend fun getRemoteBookDetail(
        book: Book,
        source: Source,
    ): Book


    fun getAllExploreBookByPaging(
        source: Source,
        exploreType: ExploreType,
        query: String? = null,
    ): PagingSource<Int, Book>


    fun getRemoteReadingContentUseCase(
        chapter: Chapter,
        source: Source,
    ): Flow<Resource<ContentPage>>

}