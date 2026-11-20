package com.example.data

import android.content.Context
import com.example.R
import com.example.model.GameState
import com.example.model.PlayerProfile
import com.example.model.WinCategory
import com.example.model.WinHistoryEntry
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreRepository(
  private val db: FirebaseFirestore,
  private val auth: FirebaseAuth = Firebase.auth,
) {
  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    )
  )

  private fun requireUserId(): String {
    return auth.currentUser?.uid?.takeIf { it.isNotBlank() }
      ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
  }

  private fun sanitizeDisplayName(raw: String?): String {
    val cleaned = raw?.trim().orEmpty()
    return if (cleaned.isEmpty()) "Bath Player" else cleaned.take(60)
  }

  private fun sanitizeWager(wagerCents: Long): Long =
    if (wagerCents in GameState.WAGER_OPTIONS_CENTS) wagerCents else 100L

  fun observePlayerProfile(userId: String): Flow<PlayerProfile?> = flow {
    val uid = requireUserId()
    check(uid == userId) { "Signed-in user changed; refusing cross-user profile read." }
    val docRef = db.collection("users").document(uid)
    emitAll(
      docRef
        .snapshots()
        .map { snapshot ->
          if (snapshot.exists()) {
            snapshot.toObject(
              PlayerProfile::class.java,
              DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
            )
          } else {
            null
          }
        }
        .catch { error ->
          val firestoreError =
            generateSequence(error) { it.cause }
              .filterIsInstance<FirebaseFirestoreException>()
              .firstOrNull()
          if (firestoreError == null && error is CancellationException) throw error
          val exceptionToLog = firestoreError ?: (error as? Exception)
          if (exceptionToLog != null && auth.currentUser?.uid == uid) {
            handleFirestoreError(exceptionToLog, OperationType.GET, docRef.path)
          }
          throw error
        }
    )
  }

  fun observeWinHistory(userId: String): Flow<List<WinHistoryEntry>> = flow {
    val uid = requireUserId()
    check(uid == userId) { "Signed-in user changed; refusing cross-user win history read." }
    val colRef = db.collection("users").document(uid).collection("winHistory")
    val path = colRef.path
    emitAll(
      colRef
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .limit(20)
        .snapshots()
        .map { snapshot ->
          snapshot.toObjects(
            WinHistoryEntry::class.java,
            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
          )
        }
        .catch { error ->
          val firestoreError =
            generateSequence(error) { it.cause }
              .filterIsInstance<FirebaseFirestoreException>()
              .firstOrNull()
          if (firestoreError == null && error is CancellationException) throw error
          val exceptionToLog = firestoreError ?: (error as? Exception)
          if (exceptionToLog != null && auth.currentUser?.uid == uid) {
            handleFirestoreError(exceptionToLog, OperationType.LIST, path)
          }
          throw error
        }
    )
  }

  suspend fun ensureProfileExists(
    userId: String,
    displayName: String?,
    initialLocal: PersistedArcadeData,
  ): Result<PlayerProfile> {
    val docPath = "users/$userId"
    return try {
      val uid = requireUserId()
      check(uid == userId) { "Signed-in user changed; refusing cross-user profile creation." }
      val docRef = db.collection("users").document(uid)
      val snap = docRef.get().await()
      if (snap.exists()) {
        val existing =
          snap.toObject(
            PlayerProfile::class.java,
            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
          )
        if (existing != null) {
          return Result.success(existing)
        }
      }

      val safeName = sanitizeDisplayName(displayName)
      val safeBalance = initialLocal.balanceCents.coerceIn(0L, MAX_CENTS)
      val safeWager = sanitizeWager(initialLocal.wagerCents)
      val safeProgressive =
        initialLocal.progressiveCents.coerceIn(GameState.PROGRESSIVE_SEED_CENTS, MAX_CENTS)
      val safeSpins = initialLocal.totalSpins.toLong().coerceIn(0L, MAX_SPINS)
      val safeTotalWon = initialLocal.totalWonCents.coerceIn(0L, MAX_CENTS)
      val safeBiggestWin = initialLocal.biggestWinCents.coerceIn(0L, MAX_CENTS)

      val payload =
        mapOf(
          "userId" to uid,
          "displayName" to safeName,
          "balanceCents" to safeBalance,
          "wagerCents" to safeWager,
          "progressiveCents" to safeProgressive,
          "totalSpins" to safeSpins,
          "totalWonCents" to safeTotalWon,
          "biggestWinCents" to safeBiggestWin,
          "soundEnabled" to initialLocal.soundEnabled,
          "hapticsEnabled" to initialLocal.hapticsEnabled,
          "skillModeEnabled" to initialLocal.skillModeEnabled,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      docRef.set(payload).await()

      Result.success(
        PlayerProfile(
          id = uid,
          userId = uid,
          displayName = safeName,
          balanceCents = safeBalance,
          wagerCents = safeWager,
          progressiveCents = safeProgressive,
          totalSpins = safeSpins,
          totalWonCents = safeTotalWon,
          biggestWinCents = safeBiggestWin,
          soundEnabled = initialLocal.soundEnabled,
          hapticsEnabled = initialLocal.hapticsEnabled,
          skillModeEnabled = initialLocal.skillModeEnabled,
        )
      )
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docPath)
      Result.failure(e)
    }
  }

  suspend fun saveGameplayStats(
    userId: String,
    displayName: String?,
    balanceCents: Long,
    wagerCents: Long,
    progressiveCents: Long,
    totalSpins: Int,
    totalWonCents: Long,
    biggestWinCents: Long,
  ): Result<Unit> {
    val docPath = "users/$userId"
    return try {
      val uid = requireUserId()
      check(uid == userId) { "Signed-in user changed; refusing cross-user stats write." }
      val docRef = db.collection("users").document(uid)
      val updatePayload =
        mapOf(
          "displayName" to sanitizeDisplayName(displayName),
          "balanceCents" to balanceCents.coerceIn(0L, MAX_CENTS),
          "wagerCents" to sanitizeWager(wagerCents),
          "progressiveCents" to
            progressiveCents.coerceIn(GameState.PROGRESSIVE_SEED_CENTS, MAX_CENTS),
          "totalSpins" to totalSpins.toLong().coerceIn(0L, MAX_SPINS),
          "totalWonCents" to totalWonCents.coerceIn(0L, MAX_CENTS),
          "biggestWinCents" to biggestWinCents.coerceIn(0L, MAX_CENTS),
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      docRef.update(updatePayload).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docPath)
      Result.failure(e)
    }
  }

  suspend fun saveSettings(
    userId: String,
    soundEnabled: Boolean,
    hapticsEnabled: Boolean,
    skillModeEnabled: Boolean,
  ): Result<Unit> {
    val docPath = "users/$userId"
    return try {
      val uid = requireUserId()
      check(uid == userId) { "Signed-in user changed; refusing cross-user settings write." }
      val docRef = db.collection("users").document(uid)
      val updatePayload =
        mapOf(
          "soundEnabled" to soundEnabled,
          "hapticsEnabled" to hapticsEnabled,
          "skillModeEnabled" to skillModeEnabled,
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      docRef.update(updatePayload).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docPath)
      Result.failure(e)
    }
  }

  suspend fun recordWinHistory(
    userId: String,
    label: String,
    payoutCents: Long,
    wagerCents: Long,
    category: WinCategory,
  ): Result<Unit> {
    var docPath = "users/$userId/winHistory"
    return try {
      val uid = requireUserId()
      check(uid == userId) { "Signed-in user changed; refusing cross-user win history write." }
      val safePayout = payoutCents.coerceIn(1L, MAX_CENTS)
      val safeLabel = label.trim().ifEmpty { "ARCADE WIN" }.take(80)
      val safeWager = sanitizeWager(wagerCents)

      val docRef = db.collection("users").document(uid).collection("winHistory").document()
      docPath = docRef.path
      val payload =
        mapOf(
          "userId" to uid,
          "label" to safeLabel,
          "payoutCents" to safePayout,
          "wagerCents" to safeWager,
          "category" to category.name,
          "createdAt" to FieldValue.serverTimestamp(),
        )
      docRef.set(payload).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docPath)
      Result.failure(e)
    }
  }

  suspend fun clearWinHistory(userId: String): Result<Unit> {
    val colPath = "users/$userId/winHistory"
    return try {
      val uid = requireUserId()
      check(uid == userId) { "Signed-in user changed; refusing cross-user win history clear." }
      val colRef = db.collection("users").document(uid).collection("winHistory")
      val snapshot = colRef.limit(50).get().await()
      if (!snapshot.isEmpty) {
        val batch = db.batch()
        snapshot.documents.forEach { doc -> batch.delete(doc.reference) }
        batch.commit().await()
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, colPath)
      Result.failure(e)
    }
  }

  suspend fun deleteUserAccountData(userId: String): Result<Unit> {
    val userPath = "users/$userId"
    return try {
      val uid = requireUserId()
      check(uid == userId) { "Signed-in user changed; refusing cross-user account deletion." }
      val userDoc = db.collection("users").document(uid)
      val historyCol = userDoc.collection("winHistory")
      while (true) {
        val snapshot = historyCol.limit(50).get().await()
        if (snapshot.isEmpty) break
        val batch = db.batch()
        snapshot.documents.forEach { doc -> batch.delete(doc.reference) }
        batch.commit().await()
        if (snapshot.size() < 50) break
      }
      userDoc.delete().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, userPath)
      Result.failure(e)
    }
  }

  private companion object {
    const val MAX_CENTS = 100_000_000_000L
    const val MAX_SPINS = 10_000_000L
  }
}
