package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.GameState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "bathtime_bucks_prefs")

data class PersistedArcadeData(
  val balanceCents: Long = 100_000L,
  val wagerCents: Long = 100L,
  val progressiveCents: Long = 253_759L,
  val soundEnabled: Boolean = true,
  val hapticsEnabled: Boolean = true,
  val skillModeEnabled: Boolean = true,
  val totalSpins: Int = 0,
  val totalWonCents: Long = 0L,
  val biggestWinCents: Long = 0L,
)

class PreferencesRepository(private val context: Context) {

  private object Keys {
    val BALANCE_CENTS = longPreferencesKey("balance_cents")
    val WAGER_CENTS = longPreferencesKey("wager_cents")
    val PROGRESSIVE_CENTS = longPreferencesKey("progressive_cents")
    val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
    val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
    val SKILL_MODE_ENABLED = booleanPreferencesKey("skill_mode_enabled")
    val TOTAL_SPINS = intPreferencesKey("total_spins")
    val TOTAL_WON_CENTS = longPreferencesKey("total_won_cents")
    val BIGGEST_WIN_CENTS = longPreferencesKey("biggest_win_cents")
  }

  val arcadeDataFlow: Flow<PersistedArcadeData> =
    context.dataStore.data.map { prefs ->
      PersistedArcadeData(
        balanceCents = prefs[Keys.BALANCE_CENTS] ?: 100_000L,
        wagerCents = prefs[Keys.WAGER_CENTS] ?: 100L,
        progressiveCents =
          (prefs[Keys.PROGRESSIVE_CENTS] ?: 253_759L).coerceAtLeast(GameState.PROGRESSIVE_SEED_CENTS),
        soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
        hapticsEnabled = prefs[Keys.HAPTICS_ENABLED] ?: true,
        skillModeEnabled = prefs[Keys.SKILL_MODE_ENABLED] ?: true,
        totalSpins = prefs[Keys.TOTAL_SPINS] ?: 0,
        totalWonCents = prefs[Keys.TOTAL_WON_CENTS] ?: 0L,
        biggestWinCents = prefs[Keys.BIGGEST_WIN_CENTS] ?: 0L,
      )
    }

  suspend fun saveState(
    balanceCents: Long,
    wagerCents: Long,
    progressiveCents: Long,
    totalSpins: Int,
    totalWonCents: Long,
    biggestWinCents: Long,
  ) {
    context.dataStore.edit { prefs ->
      prefs[Keys.BALANCE_CENTS] = balanceCents
      prefs[Keys.WAGER_CENTS] = wagerCents
      prefs[Keys.PROGRESSIVE_CENTS] = progressiveCents
      prefs[Keys.TOTAL_SPINS] = totalSpins
      prefs[Keys.TOTAL_WON_CENTS] = totalWonCents
      prefs[Keys.BIGGEST_WIN_CENTS] = biggestWinCents
    }
  }

  suspend fun saveSettings(soundEnabled: Boolean, hapticsEnabled: Boolean, skillModeEnabled: Boolean) {
    context.dataStore.edit { prefs ->
      prefs[Keys.SOUND_ENABLED] = soundEnabled
      prefs[Keys.HAPTICS_ENABLED] = hapticsEnabled
      prefs[Keys.SKILL_MODE_ENABLED] = skillModeEnabled
    }
  }

  suspend fun clearAll() {
    context.dataStore.edit { prefs -> prefs.clear() }
  }
}
