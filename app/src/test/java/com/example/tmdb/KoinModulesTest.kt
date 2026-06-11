package com.example.tmdb

import androidx.lifecycle.SavedStateHandle
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.verify
import org.junit.Test

class KoinModulesTest {

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `koin graph is valid`() {
        // SavedStateHandle is provided by the ViewModel factory at runtime, not the graph.
        module { includes(appModules) }.verify(extraTypes = listOf(SavedStateHandle::class))
    }
}
