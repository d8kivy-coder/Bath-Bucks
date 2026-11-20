package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.ArcadeGreenBright
import com.example.ui.theme.BarBlue
import com.example.ui.theme.BarGreen
import com.example.ui.theme.BarTan
import com.example.ui.theme.BathTileCyan
import com.example.ui.theme.DuckBillOrange
import com.example.ui.theme.DuckGold

enum class SlotSymbol(
  val displayName: String,
  val weight: Int,
  val pay2: Int,
  val pay3: Int,
  val pay4: Int,
  val pay5: Int,
  val accentColor: Color,
  val isBar: Boolean = false,
) {
  WILD("WILD", weight = 4, pay2 = 0, pay3 = 25, pay4 = 100, pay5 = 500, accentColor = BathTileCyan),
  DUCK("RUBBER DUCK", weight = 6, pay2 = 0, pay3 = 15, pay4 = 60, pay5 = 250, accentColor = DuckGold),
  BOAT("TOY BOAT", weight = 7, pay2 = 0, pay3 = 10, pay4 = 40, pay5 = 150, accentColor = DuckBillOrange),
  SOAP("SOAP BAR", weight = 8, pay2 = 0, pay3 = 8, pay4 = 30, pay5 = 100, accentColor = Color(0xFF38BDF8)),
  SHAMPOO("FOAM BONUS", weight = 6, pay2 = 0, pay3 = 5, pay4 = 15, pay5 = 50, accentColor = ArcadeGreenBright),
  BELL("GOLD BELL", weight = 9, pay2 = 0, pay3 = 6, pay4 = 20, pay5 = 75, accentColor = DuckGold),
  TRIPLE_BAR("3X BAR", weight = 10, pay2 = 0, pay3 = 5, pay4 = 15, pay5 = 50, accentColor = BarGreen, isBar = true),
  DOUBLE_BAR("2X BAR", weight = 11, pay2 = 0, pay3 = 4, pay4 = 12, pay5 = 35, accentColor = BarBlue, isBar = true),
  SINGLE_BAR("1X BAR", weight = 12, pay2 = 0, pay3 = 3, pay4 = 8, pay5 = 25, accentColor = BarTan, isBar = true),
  WATERMELON("MELON", weight = 13, pay2 = 0, pay3 = 3, pay4 = 7, pay5 = 20, accentColor = Color(0xFFEF4444)),
  PLUM("PLUM", weight = 14, pay2 = 0, pay3 = 2, pay4 = 5, pay5 = 15, accentColor = Color(0xFFA855F7)),
  ORANGE("ORANGE", weight = 15, pay2 = 0, pay3 = 2, pay4 = 4, pay5 = 12, accentColor = Color(0xFFF97316)),
  CHERRY("CHERRIES", weight = 16, pay2 = 1, pay3 = 2, pay4 = 5, pay5 = 12, accentColor = Color(0xFFDC2626));

  companion object {
    private val weightedPool: List<SlotSymbol> by lazy {
      entries.flatMap { symbol -> List(symbol.weight) { symbol } }
    }

    fun randomWeighted(random: kotlin.random.Random = kotlin.random.Random): SlotSymbol =
      weightedPool[random.nextInt(weightedPool.size)]
  }
}
