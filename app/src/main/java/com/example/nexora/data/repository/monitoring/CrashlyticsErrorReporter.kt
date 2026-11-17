package com.example.nexora.data.monitoring

import com.google.firebase.crashlytics.CustomKeysAndValues
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException

class CrashlyticsErrorReporter(
    private val crashlytics: FirebaseCrashlytics
) {
    fun record(
        error: Throwable,
        operation: String
    ) {
        if (
            error is CancellationException &&
            error !is TimeoutCancellationException
        ) {
            return
        }

        val metadata = CustomKeysAndValues.Builder()
            .putString("operation", operation)
            .build()

        crashlytics.recordException(error, metadata)
    }
}