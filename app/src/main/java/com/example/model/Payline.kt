package com.example.model

import androidx.compose.ui.graphics.Color

/**
 * 9 classic Bathtime Bucks paylines across a 5-column x 3-row reel grid.
 * Each array holds the row index (0 = Top, 1 = Middle, 2 = Bottom) for columns 0..4.
 */
data class Payline(val index: Int, val name: String, val rows: List<Int>, val color: Color)

data class WinningLine(
  val payline: Payline,
  val symbol: SlotSymbol,
  val matchCount: Int,
  val payoutCents: Long,
  val isProgressive: Boolean = false,
)

data class EvaluationResult(
  val totalPayoutCents: Long,
  val winningLines: List<WinningLine>,
  val duckBonusSpins: Int = 0,
  val shampooBonusPicks: Int = 0,
  val bonusTriggerCells: Set<Pair<Int, Int>> = emptySet(),
  val progressiveWon: Boolean = false,
)

object Paylines {
  val ALL: List<Payline> =
    listOf(
      Payline(1, "Center Line", listOf(1, 1, 1, 1, 1), Color(0xFFFFD400)),
      Payline(2, "Top Line", listOf(0, 0, 0, 0, 0), Color(0xFF38BDF8)),
      Payline(3, "Bottom Line", listOf(2, 2, 2, 2, 2), Color(0xFF22C55E)),
      Payline(4, "V-Shape", listOf(0, 1, 2, 1, 0), Color(0xFFF97316)),
      Payline(5, "Inverted V", listOf(2, 1, 0, 1, 2), Color(0xFFE879F9)),
      Payline(6, "Top Dip", listOf(0, 0, 1, 0, 0), Color(0xFFF43F5E)),
      Payline(7, "Bottom Rise", listOf(2, 2, 1, 2, 2), Color(0xFF14B8A6)),
      Payline(8, "Step Down", listOf(0, 1, 1, 1, 2), Color(0xFFFACC15)),
      Payline(9, "Step Up", listOf(2, 1, 1, 1, 0), Color(0xFF60A5FA)),
    )

  /**
   * Evaluates a 5-column x 3-row grid (`grid[col][row]`).
   * - 3+ RUBBER DUCKS anywhere trigger Ducky Free Spins (5, 8, or 12 spins at 2x multiplier).
   * - 3+ LUCKY SHAMPOO anywhere trigger the interactive Shampoo Pick Bonus (3, 4, or 5 picks).
   */
  fun evaluate(
    grid: List<List<SlotSymbol>>,
    wagerCents: Long,
    progressiveEligible: Boolean,
    progressiveCents: Long,
    bonusMultiplier: Int = 1,
  ): EvaluationResult {
    val lineUnitCents = (wagerCents / 5L).coerceAtLeast(5L)
    val winningLines = mutableListOf<WinningLine>()
    var totalPayout = 0L
    var progressiveWon = false

    for (payline in ALL) {
      val lineSymbols = List(5) { col -> grid[col][payline.rows[col]] }

      // Check 5 WILDs first (Progressive Top Prize when wager == $2.00!)
      if (lineSymbols.all { it == SlotSymbol.WILD }) {
        val payout =
          if (progressiveEligible) {
            progressiveWon = true
            progressiveCents
          } else {
            SlotSymbol.WILD.pay5 * lineUnitCents * bonusMultiplier
          }
        winningLines.add(
          WinningLine(
            payline = payline,
            symbol = SlotSymbol.WILD,
            matchCount = 5,
            payoutCents = payout,
            isProgressive = progressiveEligible,
          )
        )
        totalPayout += payout
        continue
      }

      // Determine target non-wild symbol from left-to-right
      val firstNonWild = lineSymbols.firstOrNull { it != SlotSymbol.WILD && it != SlotSymbol.SHAMPOO }
      if (firstNonWild != null) {
        var matchCount = 0
        for (sym in lineSymbols) {
          if (sym == firstNonWild || sym == SlotSymbol.WILD) {
            matchCount++
          } else {
            break
          }
        }
        val mult =
          when (matchCount) {
            2 -> firstNonWild.pay2
            3 -> firstNonWild.pay3
            4 -> firstNonWild.pay4
            5 -> firstNonWild.pay5
            else -> 0
          }
        if (mult > 0) {
          val payout = mult * lineUnitCents * bonusMultiplier
          winningLines.add(WinningLine(payline, firstNonWild, matchCount, payout))
          totalPayout += payout
          continue
        }
      }

      // Fallback: check mixed BAR combinations (3+ BARs of any type or WILD)
      var barCount = 0
      for (sym in lineSymbols) {
        if (sym.isBar || sym == SlotSymbol.WILD) barCount++ else break
      }
      if (barCount >= 3) {
        val barMult =
          when (barCount) {
            3 -> 2
            4 -> 5
            5 -> 15
            else -> 0
          }
        val payout = barMult * lineUnitCents * bonusMultiplier
        winningLines.add(WinningLine(payline, SlotSymbol.SINGLE_BAR, barCount, payout))
        totalPayout += payout
      }
    }

    // Distinct Bonus Scatters:
    // 1) 3+ DUCKS anywhere -> Ducky Free Spins (5, 8, or 12 Free Spins at 2x!)
    // 2) 3+ SHAMPOO anywhere -> Shampoo Pick Bonus (3, 4, or 5 bottle picks!)
    val duckCells = mutableSetOf<Pair<Int, Int>>()
    val shampooCells = mutableSetOf<Pair<Int, Int>>()
    for (col in grid.indices) {
      for (row in grid[col].indices) {
        when (grid[col][row]) {
          SlotSymbol.DUCK -> duckCells.add(col to row)
          SlotSymbol.SHAMPOO -> shampooCells.add(col to row)
          else -> Unit
        }
      }
    }

    val duckCount = duckCells.size
    val duckSpins =
      when {
        duckCount >= 5 -> 12
        duckCount == 4 -> 8
        duckCount == 3 -> 5
        else -> 0
      }

    val shampooCount = shampooCells.size
    val shampooPicks =
      when {
        shampooCount >= 5 -> 5
        shampooCount == 4 -> 4
        shampooCount == 3 -> 3
        else -> 0
      }

    val bonusCells = buildSet {
      if (duckCount >= 3) addAll(duckCells)
      if (shampooCount >= 3) addAll(shampooCells)
    }

    if (duckCount >= 3) {
      val duckScatterMult =
        when {
          duckCount >= 5 -> 25
          duckCount == 4 -> 10
          else -> 4
        }
      totalPayout += duckScatterMult * lineUnitCents * bonusMultiplier
    }

    if (shampooCount >= 3) {
      val scatterMult =
        when {
          shampooCount >= 5 -> SlotSymbol.SHAMPOO.pay5
          shampooCount == 4 -> SlotSymbol.SHAMPOO.pay4
          else -> SlotSymbol.SHAMPOO.pay3
        }
      totalPayout += scatterMult * lineUnitCents * bonusMultiplier
    }

    if (progressiveWon) {
      val progLines = winningLines.filter { it.isProgressive }
      return EvaluationResult(
        totalPayoutCents = progressiveCents,
        winningLines = progLines,
        duckBonusSpins = 0,
        shampooBonusPicks = 0,
        bonusTriggerCells = emptySet(),
        progressiveWon = true,
      )
    }

    return EvaluationResult(
      totalPayoutCents = totalPayout,
      winningLines = winningLines,
      duckBonusSpins = duckSpins,
      shampooBonusPicks = shampooPicks,
      bonusTriggerCells = bonusCells,
      progressiveWon = false,
    )
  }
}
