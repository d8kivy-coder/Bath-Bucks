package com.example.model

import androidx.annotation.Keep
import androidx.compose.runtime.Immutable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
@Immutable
data class PlayerProfile(
  @DocumentId val id: String = "",
  val userId: String = "",
  val displayName: String = "Bath Player",
  val balanceCents: Long = 100_000L,
  val wagerCents: Long = 100L,
  val progressiveCents: Long = 253_759L,
  val totalSpins: Long = 0L,
  val totalWonCents: Long = 0L,
  val biggestWinCents: Long = 0L,
  val soundEnabled: Boolean = true,
  val hapticsEnabled: Boolean = true,
  val skillModeEnabled: Boolean = true,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

enum class WinCategory {
  LINE_WIN,
  BIG_WIN,
  FREE_SPINS,
  SHAMPOO_BONUS,
  SKILL_WIN,
  PROGRESSIVE;

  companion object {
    fun fromRaw(raw: String?): WinCategory =
      entries.firstOrNull { it.name == raw } ?: LINE_WIN
  }
}

@Keep
@IgnoreExtraProperties
@Immutable
data class WinHistoryEntry(
  @DocumentId val id: String = "",
  val userId: String = "",
  val label: String = "",
  val payoutCents: Long = 0L,
  val wagerCents: Long = 100L,
  val category: String = WinCategory.LINE_WIN.name,
  @ServerTimestamp val createdAt: Timestamp? = null,
) {
  val parsedCategory: WinCategory
    get() = WinCategory.fromRaw(category)
}

@Immutable
sealed interface CloudSyncState {
  data object Syncing : CloudSyncState
  data object Synced : CloudSyncState
  data class Error(val message: String) : CloudSyncState
}
