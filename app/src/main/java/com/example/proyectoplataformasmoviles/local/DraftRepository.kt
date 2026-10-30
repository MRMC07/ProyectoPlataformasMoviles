
package com.example.proyectoplataformasmoviles.local

import kotlinx.coroutines.flow.Flow

class DraftRepository(
    private val draftDao: DraftDao
) {

    fun observeDrafts(): Flow<List<DraftEntity>> {
        return draftDao.observeDrafts()
    }

    suspend fun getDraft(
        draftId: String
    ): DraftEntity? {
        return draftDao.getDraft(draftId)
    }

    suspend fun saveDraft(
        draft: DraftEntity
    ) {
        draftDao.saveDraft(draft)
    }

    suspend fun deleteDraft(
        draftId: String
    ) {
        draftDao.deleteDraft(draftId)
    }
}
