package com.example.tmdb

import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.verify
import org.junit.Test

class KoinModulesTest {

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `koin graph is valid`() {
        module { includes(appModules) }.verify()
    }
}
