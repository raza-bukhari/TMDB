package com.example.tmdb

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.tmdb.core.designsystem.theme.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeDataStore by preferencesDataStore(name = "tmdb_settings")

/** Persists the user's chosen [AppTheme] across launches via Preferences DataStore. */
class ThemePreferences(private val context: Context) {

    private val key = stringPreferencesKey("app_theme")

    val theme: Flow<AppTheme> = context.themeDataStore.data
        .map { prefs -> AppTheme.fromNameOrDefault(prefs[key]) }

    suspend fun setTheme(theme: AppTheme) {
        context.themeDataStore.edit { prefs -> prefs[key] = theme.name }
    }
}
