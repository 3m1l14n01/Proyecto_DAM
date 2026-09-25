package com.example.sportsgd.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

internal fun <T> seededFlow(
    awaitSeed: suspend () -> Unit,
    source: () -> Flow<T>,
): Flow<T> = flow {
    awaitSeed()
    emitAll(source())
}
