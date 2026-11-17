package com.example.nexora.local

import com.example.nexora.data.monitoring.CrashlyticsErrorReporter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

class DraftRepository(
    private val draftDao: DraftDao,
    private val errorReporter: CrashlyticsErrorReporter
) {
    fun observeDrafts(): Flow<List<DraftEntity>> {
        return flow {
            emitAll(draftDao.observeDrafts())
        }.catch { error ->
            errorReporter.record(
                error = error,
                operation = "drafts.observe"
            )

            throw error
        }
    }

    suspend fun getDraft(
        draftId: String
    ): DraftEntity? {
        return runDraftOperation("drafts.get") {
            draftDao.getDraft(draftId)
        }
    }

    suspend fun saveDraft(
        draft: DraftEntity
    ) {
        runDraftOperation("drafts.save") {
            draftDao.saveDraft(draft)
        }
    }

    suspend fun deleteDraft(
        draftId: String
    ) {
        runDraftOperation("drafts.delete") {
            draftDao.deleteDraft(draftId)
        }
    }

    private suspend fun <T> runDraftOperation(
        operationName: String,
        operation: suspend () -> T
    ): T {
        return try {
            operation()
        } catch (timeout: TimeoutCancellationException) {
            errorReporter.record(
                error = timeout,
                operation = operationName
            )

            throw timeout
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            errorReporter.record(
                error = error,
                operation = operationName
            )

            throw error
        }
    }
}