package org.ireader.domain.models.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ChapterUpdate(
    @PrimaryKey
    val id: Long = 0,
    val bookId: Long? = null,
    val link: String? = null,
    val name: String? = null,
    val read: Boolean? = null,
    val bookmark: Boolean? = null,
    val progress: Int? = null,
    val dateUpload: Long? = null,
    val dateFetch: Long? = null,
    val sourceOrder: Int? = null,
    val number: Float? = null,
    val translator: String? = null,
)
