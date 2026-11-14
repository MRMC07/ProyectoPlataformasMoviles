
package com.example.proyectoplataformasmoviles.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DraftDao {

    @Query(
        "SELECT * FROM publication_drafts " +
                "ORDER BY updatedAt DESC"
    )
    fun observeDrafts(): Flow<List<DraftEntity>>

    @Query(
        "SELECT * FROM publication_drafts " +
                "WHERE id = :draftId LIMIT 1"
    )
    suspend fun getDraft(
        draftId: String
    ): DraftEntity?

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun saveDraft(
        draft: DraftEntity
    )

    @Query(
        "DELETE FROM publication_drafts " +
                "WHERE id = :draftId"
    )
    suspend fun deleteDraft(
        draftId: String
    )

    @Query(
        "SELECT COUNT(*) FROM publication_drafts"
    )
    suspend fun countDrafts(): Int
}
