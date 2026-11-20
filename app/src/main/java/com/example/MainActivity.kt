package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.BonusCelebrationOverlay
import com.example.ui.components.BonusSummaryOverlay
import com.example.ui.components.CollectTicketDialog
import com.example.ui.components.ControlDeck
import com.example.ui.components.MarqueeHeader
import com.example.ui.components.PrizeViewerDialog
import com.example.ui.components.ReelGrid
import com.example.ui.components.SettingsDialog
import com.example.ui.components.ShampooPickBonusOverlay
import com.example.ui.theme.BathDeepNavy
import com.example.ui.theme.BathtimeBucksTheme
import com.example.viewmodel.BathtimeViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      BathtimeBucksTheme {
        BathtimeBucksScreen()
      }
    }
  }
}

@Composable
fun BathtimeBucksScreen(viewModel: BathtimeViewModel = viewModel()) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val haptics = LocalHapticFeedback.current

  LaunchedEffect(state.winCents) {
    if (state.winCents > 0L && state.hapticsEnabled) {
      haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }
  }

  Box(modifier = Modifier.fillMaxSize().background(BathDeepNavy)) {
    // Glossy bathroom tile wallpaper
    Image(
      painter = painterResource(id = R.drawable.img_tile_backdrop_1794592673647),
      contentDescription = null,
      modifier = Modifier.fillMaxSize(),
      contentScale = ContentScale.Crop,
    )

    // Subtle dark cabinet depth vignette
    Box(
      modifier =
        Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              listOf(
                Color.Black.copy(alpha = 0.25f),
                Color.Transparent,
                Color.Black.copy(alpha = 0.45f),
              )
            )
          )
    )

    BoxWithConstraints(
      modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars),
      contentAlignment = Alignment.TopCenter,
    ) {
      val availableHeight = maxHeight
      val isWideLandscape = maxWidth > 700.dp && maxWidth > availableHeight

      if (isWideLandscape) {
        // Tablet / wide landscape canonical arcade cabinet split
        Row(
          modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          MarqueeHeader(
            progressiveCents = state.progressiveCents,
            isProgressiveEligible = state.isProgressiveEligible,
            bonusSpinsRemaining = state.bonusSpinsRemaining,
            modifier = Modifier.weight(0.75f).fillMaxHeight(),
          )
          Column(
            modifier = Modifier.weight(1.35f).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            ReelGrid(
              grid = state.grid,
              reelSpinning = state.reelSpinning,
              winningLines = state.winningLines,
              bonusTriggerCells = state.bonusTriggerCells,
              hotSwapSpots = state.hotSwapSpots,
              skillPrompt = state.skillPrompt,
              instantWinBadgeCents = state.instantWinBadgeCents,
              spinId = state.spinId,
              reelTargetDurationMs = state.reelTargetDurationMs,
              bonusSpinsRemaining = state.bonusSpinsRemaining,
              freeSpinsTotalWonCents = state.freeSpinsTotalWonCents,
              onSkillChoose = {
                if (state.hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                viewModel.onSkillChoose(it)
              },
              onSkillNudge = {
                if (state.hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                viewModel.onSkillNudge(it)
              },
              modifier = Modifier.weight(1f),
            )
            ControlDeck(
              state = state,
              onCollectClick = { viewModel.setShowCollectDialog(true) },
              onPrizeViewerClick = { viewModel.setShowPrizeViewer(true) },
              onSettingsClick = { viewModel.setShowSettingsDialog(true) },
              onStepWager = { viewModel.onStepWager(it) },
              onMaxClick = {
                if (state.hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                viewModel.onMaxPlayClick()
              },
              onPlayClick = {
                if (state.hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                viewModel.onPlayClick()
              },
            )
          }
        }
      } else {
        // Portrait vertical Banilla cabinet layout (matching Screenshots 1, 2, 4)
        Column(
          modifier =
            Modifier
              .fillMaxSize()
              .widthIn(max = 600.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          MarqueeHeader(
            progressiveCents = state.progressiveCents,
            isProgressiveEligible = state.isProgressiveEligible,
            bonusSpinsRemaining = state.bonusSpinsRemaining,
            modifier =
              Modifier
                .padding(horizontal = 10.dp, vertical = 4.dp)
                .height((availableHeight * 0.25f).coerceIn(145.dp, 220.dp)),
          )

          ReelGrid(
            grid = state.grid,
            reelSpinning = state.reelSpinning,
            winningLines = state.winningLines,
            bonusTriggerCells = state.bonusTriggerCells,
            hotSwapSpots = state.hotSwapSpots,
            skillPrompt = state.skillPrompt,
            instantWinBadgeCents = state.instantWinBadgeCents,
            spinId = state.spinId,
            reelTargetDurationMs = state.reelTargetDurationMs,
            bonusSpinsRemaining = state.bonusSpinsRemaining,
            freeSpinsTotalWonCents = state.freeSpinsTotalWonCents,
            onSkillChoose = {
              if (state.hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              viewModel.onSkillChoose(it)
            },
            onSkillNudge = {
              if (state.hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              viewModel.onSkillNudge(it)
            },
            modifier = Modifier.weight(1f).padding(horizontal = 10.dp),
          )

          ControlDeck(
            state = state,
            onCollectClick = { viewModel.setShowCollectDialog(true) },
            onPrizeViewerClick = { viewModel.setShowPrizeViewer(true) },
            onSettingsClick = { viewModel.setShowSettingsDialog(true) },
            onStepWager = { viewModel.onStepWager(it) },
            onMaxClick = {
              if (state.hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              viewModel.onMaxPlayClick()
            },
            onPlayClick = {
              if (state.hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              viewModel.onPlayClick()
            },
          )
        }
      }
    }

    if (state.showPrizeViewer) {
      PrizeViewerDialog(
        wagerCents = state.wagerCents,
        progressiveCents = state.progressiveCents,
        onDismiss = { viewModel.setShowPrizeViewer(false) },
      )
    }

    if (state.showCollectDialog) {
      CollectTicketDialog(
        state = state,
        onRefillCredits = { viewModel.refillVirtualCredits() },
        onResetSession = { viewModel.resetVirtualSession() },
        onDismiss = { viewModel.setShowCollectDialog(false) },
      )
    }

    if (state.showSettingsDialog) {
      SettingsDialog(
        soundEnabled = state.soundEnabled,
        hapticsEnabled = state.hapticsEnabled,
        skillModeEnabled = state.skillModeEnabled,
        onToggleSound = { viewModel.toggleSound(it) },
        onToggleHaptics = { viewModel.toggleHaptics(it) },
        onToggleSkillMode = { viewModel.toggleSkillMode(it) },
        onDismiss = { viewModel.setShowSettingsDialog(false) },
      )
    }

    BonusCelebrationOverlay(
      celebration = state.bonusCelebration,
      onDismiss = { viewModel.dismissBonusCelebration() },
      modifier = Modifier.fillMaxSize(),
    )

    ShampooPickBonusOverlay(
      bonus = state.shampooPickBonus,
      onPickTile = { viewModel.onPickShampooTile(it) },
      onCollectBonus = { viewModel.onCollectShampooPickBonus() },
      modifier = Modifier.fillMaxSize(),
    )

    BonusSummaryOverlay(
      summary = state.bonusSummary,
      onDismiss = { viewModel.dismissBonusSummary() },
      modifier = Modifier.fillMaxSize(),
    )
  }
}
