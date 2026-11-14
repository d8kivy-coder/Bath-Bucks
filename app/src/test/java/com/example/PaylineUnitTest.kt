package com.example

import com.example.model.Paylines
import com.example.model.SlotSymbol
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PaylineUnitTest {
  @Test
  fun testFiveWildsTriggersProgressiveAtMaxWager() {
    val grid =
      List(5) {
        listOf(SlotSymbol.CHERRY, SlotSymbol.WILD, SlotSymbol.PLUM)
      }
    val result =
      Paylines.evaluate(
        grid = grid,
        wagerCents = 200L,
        progressiveEligible = true,
        progressiveCents = 253_759L,
      )
    assertTrue(result.progressiveWon)
    assertEquals(253_759L, result.totalPayoutCents)
  }

  @Test
  fun testFourDucksPaysLineAndScatterAndAwardsEightFreeSpins() {
    val grid =
      listOf(
        listOf(SlotSymbol.CHERRY, SlotSymbol.DUCK, SlotSymbol.PLUM),
        listOf(SlotSymbol.ORANGE, SlotSymbol.DUCK, SlotSymbol.BELL),
        listOf(SlotSymbol.PLUM, SlotSymbol.DUCK, SlotSymbol.ORANGE),
        listOf(SlotSymbol.BELL, SlotSymbol.DUCK, SlotSymbol.CHERRY),
        listOf(SlotSymbol.WATERMELON, SlotSymbol.SINGLE_BAR, SlotSymbol.PLUM),
      )
    val result =
      Paylines.evaluate(
        grid = grid,
        wagerCents = 100L,
        progressiveEligible = false,
        progressiveCents = 250_000L,
      )
    // 4 Ducks = 60x * 20c ($12.00 line win) + 10x * 20c ($2.00 4-Duck scatter) = $14.00 (1400 cents) + 8 Free Spins
    assertEquals(1400L, result.totalPayoutCents)
    assertEquals(8, result.duckBonusSpins)
    assertEquals(0, result.shampooBonusPicks)
    assertEquals(1, result.winningLines.size)
  }

  @Test
  fun testThreeShampooScattersAwardThreePickBonusPicksWithoutFreeSpins() {
    val grid =
      listOf(
        listOf(SlotSymbol.SHAMPOO, SlotSymbol.CHERRY, SlotSymbol.PLUM),
        listOf(SlotSymbol.ORANGE, SlotSymbol.BELL, SlotSymbol.WATERMELON),
        listOf(SlotSymbol.PLUM, SlotSymbol.SHAMPOO, SlotSymbol.ORANGE),
        listOf(SlotSymbol.BELL, SlotSymbol.SINGLE_BAR, SlotSymbol.CHERRY),
        listOf(SlotSymbol.WATERMELON, SlotSymbol.PLUM, SlotSymbol.SHAMPOO),
      )
    val result =
      Paylines.evaluate(
        grid = grid,
        wagerCents = 100L,
        progressiveEligible = false,
        progressiveCents = 250_000L,
      )
    assertEquals(3, result.shampooBonusPicks)
    assertEquals(0, result.duckBonusSpins)
    assertTrue(result.totalPayoutCents > 0L)
  }

  @Test
  fun testThreeScatteredDucksTriggerFiveFreeSpinsWithoutPickBonus() {
    val grid =
      listOf(
        listOf(SlotSymbol.DUCK, SlotSymbol.CHERRY, SlotSymbol.PLUM),
        listOf(SlotSymbol.ORANGE, SlotSymbol.BELL, SlotSymbol.WATERMELON),
        listOf(SlotSymbol.PLUM, SlotSymbol.ORANGE, SlotSymbol.DUCK),
        listOf(SlotSymbol.BELL, SlotSymbol.SINGLE_BAR, SlotSymbol.CHERRY),
        listOf(SlotSymbol.DUCK, SlotSymbol.PLUM, SlotSymbol.WATERMELON),
      )
    val result =
      Paylines.evaluate(
        grid = grid,
        wagerCents = 100L,
        progressiveEligible = false,
        progressiveCents = 250_000L,
      )
    assertEquals(5, result.duckBonusSpins)
    assertEquals(0, result.shampooBonusPicks)
    assertEquals(3, result.bonusTriggerCells.size)
    assertTrue(result.totalPayoutCents > 0L)
  }
}
