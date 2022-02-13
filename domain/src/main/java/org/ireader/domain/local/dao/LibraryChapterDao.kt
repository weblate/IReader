package org.ireader.domain.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.entities.Chapter


@Dao
interface LibraryChapterDao {

    @Query("SELECT * FROM chapter_table WHERE id = :chapterId Limit 1")
    fun getChapterById(
        chapterId: Long,
    ): Flow<Chapter?>


    @Query("""SELECT * FROM chapter_table WHERE bookId= :bookId ORDER BY
        CASE WHEN :isAsc = 1 THEN id END ASC,
        CASE WHEN :isAsc = 0 THEN  id END DESC
    """)
    fun getChaptersByBookId(bookId: Long, isAsc: Boolean): Flow<List<Chapter>>

    @Query("""SELECT * FROM chapter_table WHERE bookId = :bookId ORDER BY 
        CASE WHEN :isAsc = 1 THEN id END ASC,
        CASE WHEN :isAsc = 0 THEN  id END DESC""")
    fun getChaptersForPaging(bookId: Long, isAsc: Boolean): PagingSource<Int, Chapter>


    @Query("SELECT * FROM chapter_table WHERE id = :chapterId AND bookId = :bookId Limit 1")
    fun getOneChapterForPaging(
        chapterId: Int,
        bookId: Int,
    ): PagingSource<Int, Chapter>


    @Query("SELECT * from chapter_table WHERE bookId = :bookId AND lastRead = 1 LIMIT 1")
    fun getLastReadChapter(bookId: Long): Flow<Chapter?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<Chapter>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: Chapter)


    @Query("DELETE FROM chapter_table WHERE bookId = :bookId")
    suspend fun deleteChaptersById(bookId: Long)

    @Delete
    suspend fun deleteChaptersById(chapters: List<Chapter>)

    @Delete
    suspend fun deleteChapter(chapter: Chapter)

    @Query("DELETE FROM chapter_table ")
    suspend fun deleteAllChapters()

    @Query("DELETE FROM chapter_table WHERE inLibrary = 0")
    suspend fun deleteNotInLibraryChapters()
}
