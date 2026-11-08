package com.adoptme.core.tutorial

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.tutorialDataStore by preferencesDataStore(name = "adoptme_tutorial")

/**
 * Preferencias de onboarding que no dependen de la cuenta.
 * El tutorial de swipe se muestra una sola vez por instalación.
 */
class TutorialStore(context: Context) {

    private val appContext = context.applicationContext

    val hasSeenSwipeTutorial: Flow<Boolean> = appContext.tutorialDataStore.data.map { prefs ->
        prefs[KEY_SWIPE_TUTORIAL_SEEN] ?: false
    }

    suspend fun markSwipeTutorialSeen() {
        appContext.tutorialDataStore.edit { prefs ->
            prefs[KEY_SWIPE_TUTORIAL_SEEN] = true
        }
    }

    private companion object {
        val KEY_SWIPE_TUTORIAL_SEEN = booleanPreferencesKey("swipe_tutorial_seen")
    }
}
