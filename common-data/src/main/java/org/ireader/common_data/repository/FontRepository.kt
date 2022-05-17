package org.ireader.common_data.repository

import org.ireader.common_models.entities.FontEntity

interface FontRepository {
    suspend fun findFontByName(fontName: String): FontEntity?

    fun subscribeFontEntity(): kotlinx.coroutines.flow.Flow<List<FontEntity>>
    suspend fun findAllFontEntities(): List<FontEntity>


    suspend fun insertFont(fontEntity: FontEntity): Long
    suspend fun insertFonts(fontEntity: List<FontEntity>): List<Long>

    suspend fun deleteFonts(fonts: List<FontEntity>)

    suspend fun deleteAllFonts()
}
