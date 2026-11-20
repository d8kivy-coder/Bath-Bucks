package com.example.model

import androidx.compose.runtime.Immutable

/**
 * Interactive Banilla-style Skill Nudge / Choose Icon prompt.
 */
@Immutable
data class SkillPrompt(
  val targetColumn: Int,
  val winningRow: Int,
  val targetSymbol: SlotSymbol,
  val candidateSymbols: List<SlotSymbol>,
  val potentialWinCents: Long,
  val isSolved: Boolean = false,
)

@Immutable
data class HotSwapSpot(val col: Int, val row: Int)

enum class BonusType {
  DUCK_SPINS,
  SHAMPOO_PICK,
  PROGRESSIVE,
}

@Immutable
data class BonusCelebration(
  val bonusType: BonusType,
  val picksAwarded: Int = 0,
  val spinsAwarded: Int = 0,
  val scatterPayoutCents: Long = 0L,
  val triggerCount: Int = 0,
)

@Immutable
data class ShampooPickTile(
  val id: Int,
  val multiplier: Int,
  val isGoldenBoost: Boolean = false,
  val isExtraPick: Boolean = false,
  val isRevealed: Boolean = false,
)

@Immutable
data class ShampooPickBonus(
  val picksRemaining: Int,
  val totalPicksGranted: Int,
  val wagerCents: Long,
  val tiles: List<ShampooPickTile>,
  val totalBonusWonCents: Long = 0L,
  val globalMultiplier: Int = 1,
  val lastRevealText: String = "TAP A SUDSY FOAM BOTTLE TO REVEAL A PRIZE!",
  val isComplete: Boolean = false,
)

@Immutable
data class BonusSummary(
  val title: String,
  val subtitle: String,
  val totalWonCents: Long,
  val triggerSymbol: SlotSymbol,
  val roundsLabel: String,
)

@Immutable
data class GameState(
  val balanceCents: Long = 100_000L, // $1,000.00 starting virtual amusement credits
  val wagerCents: Long = 100L, // $1.00 default play amount
  val progressiveCents: Long = 253_759L, // $2,537.59 starting Progressive Top Prize
  val winCents: Long = 0L,
  val displayedWinCents: Long = 0L,
  val grid: List<List<SlotSymbol>> = defaultInitialGrid(),
  val spinId: Long = 0L,
  val reelSpinning: List<Boolean> = listOf(false, false, false, false, false),
  val reelTargetDurationMs: List<Long> = listOf(920L, 1240L, 1560L, 1880L, 2220L),
  val winningLines: List<WinningLine> = emptyList(),
  val bonusTriggerCells: Set<Pair<Int, Int>> = emptySet(),
  val bonusCelebration: BonusCelebration? = null,
  val shampooPickBonus: ShampooPickBonus? = null,
  val bonusSummary: BonusSummary? = null,
  val skillPrompt: SkillPrompt? = null,
  val hotSwapSpots: List<HotSwapSpot> = emptyList(),
  val bonusSpinsRemaining: Int = 0,
  val freeSpinsTotalWonCents: Long = 0L,
  val freeSpinsTotalGranted: Int = 0,
  val pendingShampooTriggerCents: Long = 0L,
  val statusBanner: String = "GOOD LUCK!",
  val instantWinBadgeCents: Long? = null,
  val showPrizeViewer: Boolean = false,
  val showCollectDialog: Boolean = false,
  val showSettingsDialog: Boolean = false,
  val soundEnabled: Boolean = true,
  val hapticsEnabled: Boolean = true,
  val skillModeEnabled: Boolean = true,
  val totalSpins: Int = 0,
  val totalWonCents: Long = 0L,
  val biggestWinCents: Long = 0L,
  val playerDisplayName: String = "Bath Player",
  val cloudSyncState: CloudSyncState = CloudSyncState.Syncing,
  val winHistory: List<WinHistoryEntry> = emptyList(),
) {
  val isSpinning: Boolean
    get() = reelSpinning.any { it }

  val isProgressiveEligible: Boolean
    get() = wagerCents >= MAX_WAGER_CENTS

  companion object {
    val WAGER_OPTIONS_CENTS = listOf(25L, 50L, 100L, 200L)
    const val MAX_WAGER_CENTS = 200L
    const val PROGRESSIVE_SEED_CENTS = 250_000L

    fun defaultInitialGrid(): List<List<SlotSymbol>> =
      listOf(
        listOf(SlotSymbol.SINGLE_BAR, SlotSymbol.DUCK, SlotSymbol.TRIPLE_BAR),
        listOf(SlotSymbol.BELL, SlotSymbol.CHERRY, SlotSymbol.BOAT),
        listOf(SlotSymbol.CHERRY, SlotSymbol.WILD, SlotSymbol.SOAP),
        listOf(SlotSymbol.PLUM, SlotSymbol.TRIPLE_BAR, SlotSymbol.BOAT),
        listOf(SlotSymbol.TRIPLE_BAR, SlotSymbol.DUCK, SlotSymbol.WATERMELON),
      )
  }
}

fun formatCents(cents: Long): String {
  val negative = cents < 0
  val abs = kotlin.math.abs(cents)
  val dollars = abs / 100
  val rem = abs % 100
  val withCommas = "%,d".format(dollars)
  return "${if (negative) "-" else ""}$$withCommas.${"%02d".format(rem)}"
}
