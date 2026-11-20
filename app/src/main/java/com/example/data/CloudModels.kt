package com.example.data

import androidx.annotation.Keep
import androidx.compose.runtime.Immutable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
@Immutable
data class ArcadeProfile(
  @DocumentId val id: String = "",
  val userId: String = "",
  val displayName: String = "Player",
  val balanceCents: Long = 100_000L,
  val wagerCents: Long = 100L,
  val progressiveCents: Long = 253_759L,
  val soundEnabled: Boolean = true,
  val hapticsEnabled: Boolean = true,
  val skillModeEnabled: Boolean = true,
  val totalSpins: Long = 0L,
  val totalWonCents: Long = 0L,
  val biggestWinCents: Long = 0L,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

enum class WinHistoryType {
  LINE_WIN,
  SKILL_WIN,
  DUCK_FREE_SPINS,
  SHAMPOO_PICK,
  PROGRESSIVE;

  companion object {
    fun fromRaw(raw: String): WinHistoryType =
      entries.firstOrNull { it.name == raw } ?: LINE_WIN
  }
}

@Keep
@IgnoreExtraProperties
@Immutable
data class WinHistoryEntry(
  @DocumentId val id: String = "",
  val userId: String = "",
  val winType: String = WinHistoryType.LINE_WIN.name,
  val label: String = "",
  val wagerCents: Long = 100L,
  val payoutCents: Long = 0L,
  @ServerTimestamp val createdAt: Timestamp? = null,
) {
  val parsedType: WinHistoryType
    get() = WinHistoryType.fromRaw(winType)
}

sealed interface CloudUiState<out T> {
  data object Loading : CloudUiState<Nothing>
  data class Success<T>(val data: T) : CloudUiState<T>
  data class Error(val message: String) : CloudUiState<Nothing>
}
