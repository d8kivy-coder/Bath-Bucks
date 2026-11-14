package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.GameState
import com.example.model.formatCents
import com.example.ui.theme.ArcadeGreen
import com.example.ui.theme.ArcadeGreenBright
import com.example.ui.theme.ArcadeRed
import com.example.ui.theme.ArcadeYellow
import com.example.ui.theme.BathDeepNavy
import com.example.ui.theme.BathTileCyan
import com.example.ui.theme.CabinetBezelDark
import com.example.ui.theme.CabinetObsidian
import com.example.ui.theme.ChakraPetchFont
import com.example.ui.theme.DuckBillOrange
import com.example.ui.theme.DuckGold
import com.example.ui.theme.FredokaFont
import com.example.ui.theme.LuckiestGuyFont

@Composable
fun ControlDeck(
  state: GameState,
  onCollectClick: () -> Unit,
  onPrizeViewerClick: () -> Unit,
  onSettingsClick: () -> Unit,
  onStepWager: (Int) -> Unit,
  onMaxClick: () -> Unit,
  onPlayClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
        .background(
          Brush.verticalGradient(listOf(CabinetBezelDark, CabinetObsidian))
        )
        .border(
          width = 2.dp,
          color = Color(0xFF334155),
          shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
        )
        .navigationBarsPadding()
        .padding(horizontal = 8.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    // Top Status Strip (Cream Banilla marquee bar like Suite II / Platinum)
    Box(
      modifier =
        Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(
            Brush.verticalGradient(
              when {
                state.winCents > 0L -> listOf(Color(0xFFFEF08A), DuckGold)
                state.skillPrompt != null -> listOf(Color(0xFFBBF7D0), ArcadeGreenBright)
                else -> listOf(Color(0xFFF8FAFC), Color(0xFFE2E8F0))
              }
            )
          )
          .border(1.5.dp, Color(0xFF475569), RoundedCornerShape(8.dp))
          .padding(horizontal = 10.dp, vertical = 5.dp),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = state.statusBanner,
        fontFamily = ChakraPetchFont,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        color = BathDeepNavy,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
      )
    }

    // Meter Strip: Golden PLAY AMOUNT (< $1.00 >)  |  WIN & BALANCE Readout
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      // PLAY AMOUNT stepper (classic Banilla amber-gold box with red arrows)
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
          Modifier
            .weight(1.05f)
            .clip(RoundedCornerShape(10.dp))
            .background(
              Brush.verticalGradient(listOf(Color(0xFFFDE047), Color(0xFFF59E0B)))
            )
            .border(1.5.dp, Color(0xFFFEF08A), RoundedCornerShape(10.dp))
            .padding(horizontal = 4.dp, vertical = 4.dp),
      ) {
        Text(
          text = stringResource(id = R.string.label_play_amount),
          fontFamily = ChakraPetchFont,
          fontSize = 9.sp,
          fontWeight = FontWeight.Bold,
          color = BathDeepNavy,
        )
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth(),
        ) {
          val canLower =
            !state.isSpinning &&
              state.bonusSpinsRemaining == 0 &&
              state.shampooPickBonus == null &&
              state.wagerCents > GameState.WAGER_OPTIONS_CENTS.first()
          val canRaise =
            !state.isSpinning &&
              state.bonusSpinsRemaining == 0 &&
              state.shampooPickBonus == null &&
              state.wagerCents < GameState.WAGER_OPTIONS_CENTS.last()

          Box(
            modifier =
              Modifier
                .minimumInteractiveComponentSize()
                .size(34.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (canLower) ArcadeRed else Color(0xFF78350F).copy(alpha = 0.4f))
                .clickable(enabled = canLower) { onStepWager(-1) }
                .testTag("wager_down_button"),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowLeft,
              contentDescription = "Decrease play amount",
              tint = Color.White,
            )
          }

          Text(
            text = formatCents(state.wagerCents),
            fontFamily = ChakraPetchFont,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = BathDeepNavy,
          )

          Box(
            modifier =
              Modifier
                .minimumInteractiveComponentSize()
                .size(34.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (canRaise) ArcadeRed else Color(0xFF78350F).copy(alpha = 0.4f))
                .clickable(enabled = canRaise) { onStepWager(1) }
                .testTag("wager_up_button"),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowRight,
              contentDescription = "Increase play amount",
              tint = Color.White,
            )
          }
        }
      }

      // WIN & BALANCE LCD Readout
      Column(
        modifier =
          Modifier
            .weight(1.25f)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Black)
            .border(1.5.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = stringResource(id = R.string.label_win),
            fontFamily = ChakraPetchFont,
            fontSize = 11.sp,
            color = Color(0xFF94A3B8),
          )
          Text(
            text = formatCents(state.displayedWinCents),
            fontFamily = ChakraPetchFont,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = if (state.displayedWinCents > 0L) DuckGold else Color.White,
          )
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = stringResource(id = R.string.label_balance),
            fontFamily = ChakraPetchFont,
            fontSize = 11.sp,
            color = Color(0xFF94A3B8),
          )
          Text(
            text = formatCents(state.balanceCents),
            fontFamily = ChakraPetchFont,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = BathTileCyan,
          )
        }
      }
    }

    // Action Buttons Row: COLLECT | PRIZES | SETTINGS | MAX | PLAY
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      // COLLECT / BANK button
      ArcadeGlossButton(
        label = stringResource(id = R.string.btn_collect),
        topColor = Color(0xFF15803D),
        bottomColor = Color(0xFF14532D),
        borderColor = ArcadeGreenBright,
        textColor = Color.White,
        onClick = onCollectClick,
        modifier = Modifier.weight(0.95f).height(50.dp).testTag("collect_button"),
      )

      // PRIZE VIEWER / RULES button
      Box(
        modifier =
          Modifier
            .minimumInteractiveComponentSize()
            .weight(0.75f)
            .height(50.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
              Brush.verticalGradient(listOf(Color(0xFF0284C7), Color(0xFF1E3A8A)))
            )
            .border(1.5.dp, BathTileCyan, RoundedCornerShape(10.dp))
            .clickable(onClick = onPrizeViewerClick)
            .padding(horizontal = 4.dp)
            .testTag("prize_viewer_button"),
        contentAlignment = Alignment.Center,
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = stringResource(id = R.string.btn_prize_viewer),
            tint = DuckGold,
            modifier = Modifier.size(18.dp),
          )
          Text(
            text = "PRIZES",
            fontFamily = ChakraPetchFont,
            fontWeight = FontWeight.Bold,
            fontSize = 9.5.sp,
            color = Color.White,
          )
        }
      }

      // SETTINGS button
      Box(
        modifier =
          Modifier
            .minimumInteractiveComponentSize()
            .weight(0.55f)
            .height(50.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
              Brush.verticalGradient(listOf(Color(0xFF475569), Color(0xFF1E293B)))
            )
            .border(1.5.dp, Color(0xFF94A3B8), RoundedCornerShape(10.dp))
            .clickable(onClick = onSettingsClick)
            .testTag("settings_button"),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Default.Settings,
          contentDescription = stringResource(id = R.string.btn_settings),
          tint = Color.White,
          modifier = Modifier.size(22.dp),
        )
      }

      // MAX button
      ArcadeGlossButton(
        label = stringResource(id = R.string.btn_max),
        topColor = ArcadeYellow,
        bottomColor = DuckBillOrange,
        borderColor = Color(0xFFFEF08A),
        textColor = BathDeepNavy,
        onClick = onMaxClick,
        enabled = !state.isSpinning && state.bonusSpinsRemaining == 0,
        modifier = Modifier.weight(0.8f).height(50.dp).testTag("max_button"),
      )

      // PLAY / STOP button
      ArcadeGlossButton(
        label = if (state.isSpinning) "STOP" else stringResource(id = R.string.btn_play),
        topColor = if (state.isSpinning) Color(0xFFEF4444) else ArcadeGreenBright,
        bottomColor = if (state.isSpinning) Color(0xFF991B1B) else ArcadeGreen,
        borderColor = Color.White,
        textColor = Color.White,
        fontSize = 18,
        onClick = onPlayClick,
        modifier = Modifier.weight(1.25f).height(50.dp).testTag("play_button"),
      )
    }

    // Subtle Amusement Disclaimer
    Text(
      text = stringResource(id = R.string.amusement_notice),
      fontFamily = FredokaFont,
      fontSize = 9.sp,
      color = Color(0xFF64748B),
      textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth(),
    )
  }
}

@Composable
private fun ArcadeGlossButton(
  label: String,
  topColor: Color,
  bottomColor: Color,
  borderColor: Color,
  textColor: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  fontSize: Int = 14,
) {
  Box(
    modifier =
      modifier
        .minimumInteractiveComponentSize()
        .clip(RoundedCornerShape(10.dp))
        .background(
          Brush.verticalGradient(
            if (enabled) listOf(topColor, bottomColor)
            else listOf(Color(0xFF334155), Color(0xFF1E293B))
          )
        )
        .border(
          width = 2.dp,
          color = if (enabled) borderColor else Color(0xFF475569),
          shape = RoundedCornerShape(10.dp),
        )
        .clickable(enabled = enabled, onClick = onClick)
        .padding(horizontal = 4.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = label,
      fontFamily = LuckiestGuyFont,
      fontSize = fontSize.sp,
      color = if (enabled) textColor else Color(0xFF94A3B8),
      maxLines = 1,
    )
  }
}
