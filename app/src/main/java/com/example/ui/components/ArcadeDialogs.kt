package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.CloudSyncState
import com.example.model.GameState
import com.example.model.Paylines
import com.example.model.SlotSymbol
import com.example.model.formatCents
import com.example.ui.theme.ArcadeGreen
import com.example.ui.theme.ArcadeGreenBright
import com.example.ui.theme.BathDeepNavy
import com.example.ui.theme.BathRoyalBlue
import com.example.ui.theme.BathTileCyan
import com.example.ui.theme.ChakraPetchFont
import com.example.ui.theme.DuckBillOrange
import com.example.ui.theme.DuckGold
import com.example.ui.theme.FredokaFont
import com.example.ui.theme.LuckiestGuyFont

@Composable
fun PrizeViewerDialog(
  wagerCents: Long,
  progressiveCents: Long,
  onDismiss: () -> Unit,
) {
  val lineUnitCents = (wagerCents / 5L).coerceAtLeast(5L)

  Dialog(onDismissRequest = onDismiss) {
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .heightIn(max = 600.dp)
          .clip(RoundedCornerShape(18.dp))
          .background(Brush.verticalGradient(listOf(BathRoyalBlue, BathDeepNavy)))
          .border(2.5.dp, DuckGold, RoundedCornerShape(18.dp))
          .padding(14.dp)
    ) {
      Text(
        text = "PRIZE CHART & RULES",
        fontFamily = LuckiestGuyFont,
        fontSize = 22.sp,
        color = DuckGold,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
      )
      Text(
        text = "Current Play Amount: ${formatCents(wagerCents)} (9 Paylines)",
        fontFamily = ChakraPetchFont,
        fontSize = 12.sp,
        color = Color(0xFFBAE6FD),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
      )

      Column(
        modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        // Orange/Gold Prize Chart table inspired by Bathtime Bucks Prize Chart
        Column(
          modifier =
            Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0xFFEA580C))
              .border(1.5.dp, DuckGold, RoundedCornerShape(12.dp))
              .padding(8.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("WAGER", fontFamily = ChakraPetchFont, fontSize = 11.sp, color = Color.White)
            Text("$0.25", fontFamily = ChakraPetchFont, fontSize = 11.sp, color = DuckGold)
            Text("$0.50", fontFamily = ChakraPetchFont, fontSize = 11.sp, color = DuckGold)
            Text("$1.00", fontFamily = ChakraPetchFont, fontSize = 11.sp, color = DuckGold)
            Text("$2.00*", fontFamily = ChakraPetchFont, fontSize = 11.sp, color = Color.White)
          }
          HorizontalDivider(color = Color.White.copy(alpha = 0.35f))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("TOP PRIZE", fontFamily = ChakraPetchFont, fontSize = 11.sp, color = Color.White)
            Text("$125+", fontFamily = ChakraPetchFont, fontSize = 11.sp, color = Color.White)
            Text("$250+", fontFamily = ChakraPetchFont, fontSize = 11.sp, color = Color.White)
            Text("$500+", fontFamily = ChakraPetchFont, fontSize = 11.sp, color = Color.White)
            Text(formatCents(progressiveCents), fontFamily = ChakraPetchFont, fontSize = 11.sp, color = DuckGold)
          }
          Text(
            text = "*$2.00 Max Play activates the Progressive Top Prize for 5 WILD bubbles!",
            fontFamily = FredokaFont,
            fontSize = 10.sp,
            color = Color(0xFFFFFBEB),
          )
        }

        // Symbol Payouts
        Text(
          text = "SYMBOL PAYOUTS (AT ${formatCents(wagerCents)})",
          fontFamily = ChakraPetchFont,
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp,
          color = DuckGold,
        )

        SlotSymbol.entries.forEach { symbol ->
          Row(
            modifier =
              Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0A2540))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.weight(1.3f),
            ) {
              SymbolArtwork(symbol = symbol, modifier = Modifier.size(34.dp))
              Text(
                text = symbol.displayName,
                fontFamily = FredokaFont,
                fontSize = 12.sp,
                color = Color.White,
              )
            }
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
              Text(
                text = "3x: ${formatCents(symbol.pay3 * lineUnitCents)}",
                fontFamily = ChakraPetchFont,
                fontSize = 10.sp,
                color = Color(0xFFBAE6FD),
              )
              Text(
                text = "4x: ${formatCents(symbol.pay4 * lineUnitCents)}  •  5x: ${formatCents(symbol.pay5 * lineUnitCents)}",
                fontFamily = ChakraPetchFont,
                fontSize = 10.sp,
                color = DuckGold,
              )
            }
          }
        }

        // 9 Paylines visual grid
        Text(
          text = "9 ACTIVE PAYLINES",
          fontFamily = ChakraPetchFont,
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp,
          color = DuckGold,
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Paylines.ALL.take(5).forEach { pl ->
            PaylineMiniDiagram(pl.index, pl.rows, pl.color, Modifier.weight(1f))
          }
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Paylines.ALL.drop(5).forEach { pl ->
            PaylineMiniDiagram(pl.index, pl.rows, pl.color, Modifier.weight(1f))
          }
          Spacer(modifier = Modifier.weight(1f))
        }

        // Feature Rules
        Column(
          modifier =
            Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(Color(0xFF0A2540))
              .padding(10.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Text("SPECIAL FEATURES", fontFamily = ChakraPetchFont, fontSize = 11.sp, color = DuckGold)
          Text(
            "• BUBBLE POP: Duck & Wild spins can bubble-pop adjacent symbols into WILD Ducks.",
            fontFamily = FredokaFont,
            fontSize = 11.sp,
            color = Color.White,
          )
          Text(
            "• SKILL NUDGE / CHOOSE: On near-misses, pick the winning icon or tap Nudge Up/Down to claim an Instant Win!",
            fontFamily = FredokaFont,
            fontSize = 11.sp,
            color = Color.White,
          )
          Text(
            "• SPLASH BONUS SPINS: 3, 4, or 5+ Rubber Ducks anywhere award 5, 8, or 12 Bonus Spins with 2X wins & extra Wilds!",
            fontFamily = FredokaFont,
            fontSize = 11.sp,
            color = Color.White,
          )
          Text(
            "• FOAM BOTTLE PICK BONUS: 3, 4, or 5+ Sudsy Shampoo bottles anywhere award 3, 4, or 5 interactive Bottle Picks with multipliers & Golden 2X boosts!",
            fontFamily = FredokaFont,
            fontSize = 11.sp,
            color = Color.White,
          )
          Text(
            "Virtual amusement simulator only. Credits have no cash value.",
            fontFamily = FredokaFont,
            fontSize = 10.sp,
            color = Color(0xFF94A3B8),
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))
      Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(containerColor = ArcadeGreen),
        modifier = Modifier.fillMaxWidth().testTag("close_prize_viewer"),
      ) {
        Text("BACK TO GAME", fontFamily = LuckiestGuyFont, fontSize = 15.sp, color = Color.White)
      }
    }
  }
}

@Composable
private fun PaylineMiniDiagram(
  index: Int,
  rows: List<Int>,
  color: Color,
  modifier: Modifier = Modifier,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier =
      modifier
        .clip(RoundedCornerShape(6.dp))
        .background(Color(0xFF0A2540))
        .padding(4.dp),
  ) {
    Text("L$index", fontFamily = ChakraPetchFont, fontSize = 9.sp, color = color)
    Canvas(modifier = Modifier.fillMaxWidth().height(24.dp)) {
      val cw = size.width / 5f
      val rh = size.height / 3f
      for (c in 0 until 5) {
        for (r in 0 until 3) {
          val active = rows[c] == r
          drawCircle(
            color = if (active) color else Color.White.copy(alpha = 0.2f),
            radius = if (active) 3.5f else 2f,
            center = Offset(c * cw + cw / 2f, r * rh + rh / 2f),
          )
        }
      }
    }
  }
}

@Composable
fun CollectTicketDialog(
  state: GameState,
  onRefillCredits: () -> Unit,
  onResetSession: () -> Unit,
  onDismiss: () -> Unit,
) {
  Dialog(onDismissRequest = onDismiss) {
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .heightIn(max = 620.dp)
          .clip(RoundedCornerShape(18.dp))
          .background(Brush.verticalGradient(listOf(BathRoyalBlue, BathDeepNavy)))
          .border(2.5.dp, ArcadeGreenBright, RoundedCornerShape(18.dp))
          .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
        text = "AMUSEMENT TICKET",
        fontFamily = LuckiestGuyFont,
        fontSize = 22.sp,
        color = DuckGold,
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = state.playerDisplayName,
          fontFamily = FredokaFont,
          fontSize = 13.sp,
          color = Color.White,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f).padding(end = 8.dp),
        )
        CloudSyncBadge(cloudSyncState = state.cloudSyncState)
      }

      Column(
        modifier =
          Modifier
            .fillMaxWidth()
            .weight(1f, fill = false)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Column(
          modifier =
            Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0xFFF8FAFC))
              .padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Text(
            text = "SUDSY DUCK REELS • CLOUD VOUCHER",
            fontFamily = ChakraPetchFont,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = BathDeepNavy,
          )
          HorizontalDivider(color = Color(0xFFCBD5E1))
          TicketRow("CURRENT BALANCE", formatCents(state.balanceCents))
          TicketRow("TOTAL SPINS", state.totalSpins.toString())
          TicketRow("TOTAL WON", formatCents(state.totalWonCents))
          TicketRow("BIGGEST WIN", formatCents(state.biggestWinCents))
          Text(
            text = "FOR AMUSEMENT ONLY • NO CASH VALUE",
            fontFamily = ChakraPetchFont,
            fontSize = 9.sp,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
          )
        }

        Column(
          modifier =
            Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0xFF0A2540))
              .border(1.dp, BathTileCyan.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
              .padding(10.dp)
              .testTag("cloud_win_history_section"),
          verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
          Text(
            text = "RECENT CLOUD WINS",
            fontFamily = ChakraPetchFont,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = DuckGold,
          )
          if (state.winHistory.isEmpty()) {
            Text(
              text = "No cloud wins logged yet. Spin the reels to save your big hits!",
              fontFamily = FredokaFont,
              fontSize = 11.sp,
              color = Color(0xFF94A3B8),
            )
          } else {
            state.winHistory.take(8).forEach { entry ->
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = entry.label,
                  fontFamily = FredokaFont,
                  fontSize = 12.sp,
                  color = Color.White,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.weight(1f).padding(end = 8.dp),
                )
                Text(
                  text = "+${formatCents(entry.payoutCents)}",
                  fontFamily = ChakraPetchFont,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp,
                  color = ArcadeGreenBright,
                )
              }
            }
          }
        }
      }

      Button(
        onClick = onRefillCredits,
        colors = ButtonDefaults.buttonColors(containerColor = ArcadeGreen),
        modifier =
          Modifier.fillMaxWidth().minimumInteractiveComponentSize().testTag("refill_credits_button"),
      ) {
        Text("REFILL +$1,000.00 CREDITS", fontFamily = LuckiestGuyFont, fontSize = 14.sp, color = Color.White)
      }

      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(
          onClick = onResetSession,
          modifier =
            Modifier.weight(1f).minimumInteractiveComponentSize().testTag("reset_session_button"),
        ) {
          Text("RESET", fontFamily = ChakraPetchFont, color = DuckBillOrange)
        }
        Button(
          onClick = onDismiss,
          colors = ButtonDefaults.buttonColors(containerColor = BathTileCyan),
          modifier =
            Modifier.weight(1f).minimumInteractiveComponentSize().testTag("close_collect_dialog"),
        ) {
          Text("PLAY ON", fontFamily = ChakraPetchFont, color = BathDeepNavy)
        }
      }
    }
  }
}

@Composable
private fun CloudSyncBadge(cloudSyncState: CloudSyncState) {
  val (label, bgColor, textColor) =
    when (cloudSyncState) {
      CloudSyncState.Synced -> Triple("CLOUD SYNCED", Color(0xFF14532D), ArcadeGreenBright)
      CloudSyncState.Syncing -> Triple("SYNCING...", Color(0xFF0C4A6E), Color(0xFFBAE6FD))
      is CloudSyncState.Error -> Triple("SYNC ISSUE", Color(0xFF7C2D12), Color(0xFFFDE68A))
    }
  Box(
    modifier =
      Modifier
        .clip(RoundedCornerShape(999.dp))
        .background(bgColor)
        .border(1.dp, textColor.copy(alpha = 0.6f), RoundedCornerShape(999.dp))
        .padding(horizontal = 8.dp, vertical = 3.dp)
        .testTag("cloud_sync_badge"),
  ) {
    Text(
      text = label,
      fontFamily = ChakraPetchFont,
      fontWeight = FontWeight.Bold,
      fontSize = 10.sp,
      color = textColor,
    )
  }
}

@Composable
private fun TicketRow(label: String, value: String) {
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
    Text(label, fontFamily = ChakraPetchFont, fontSize = 12.sp, color = Color(0xFF475569))
    Text(value, fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BathDeepNavy)
  }
}

@Composable
fun SettingsDialog(
  soundEnabled: Boolean,
  hapticsEnabled: Boolean,
  skillModeEnabled: Boolean,
  playerDisplayName: String,
  cloudSyncState: CloudSyncState,
  onToggleSound: (Boolean) -> Unit,
  onToggleHaptics: (Boolean) -> Unit,
  onToggleSkillMode: (Boolean) -> Unit,
  onSignOut: () -> Unit,
  onDeleteAccount: (onComplete: () -> Unit, onError: (String) -> Unit) -> Unit,
  onDismiss: () -> Unit,
) {
  var showDeleteConfirm by remember { mutableStateOf(false) }
  var isDeletingAccount by remember { mutableStateOf(false) }
  var deleteErrorMessage by remember { mutableStateOf<String?>(null) }

  Dialog(onDismissRequest = { if (!isDeletingAccount) onDismiss() }) {
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .heightIn(max = 640.dp)
          .clip(RoundedCornerShape(18.dp))
          .background(Brush.verticalGradient(listOf(BathRoyalBlue, BathDeepNavy)))
          .border(2.dp, BathTileCyan, RoundedCornerShape(18.dp))
          .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Text(
        text = "CABINET SETTINGS",
        fontFamily = LuckiestGuyFont,
        fontSize = 21.sp,
        color = DuckGold,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
      )

      Column(
        modifier =
          Modifier
            .fillMaxWidth()
            .weight(1f, fill = false)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        SettingToggleRow(
          title = "Arcade Sound Effects",
          subtitle = "Reel ticks, duck squeaks & win chimes",
          checked = soundEnabled,
          onCheckedChange = onToggleSound,
        )
        SettingToggleRow(
          title = "Haptic Feedback",
          subtitle = "Tactile vibration on wins & button taps",
          checked = hapticsEnabled,
          onCheckedChange = onToggleHaptics,
        )
        SettingToggleRow(
          title = "Skill Nudge / Choose Mode",
          subtitle = "Interactive skill icon pick on near-misses",
          checked = skillModeEnabled,
          onCheckedChange = onToggleSkillMode,
        )

        Column(
          modifier =
            Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(Color(0xFF0A2540))
              .border(1.dp, BathTileCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
              .padding(horizontal = 12.dp, vertical = 10.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
              Text(
                text = "GOOGLE CLOUD ACCOUNT",
                fontFamily = ChakraPetchFont,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = DuckGold,
              )
              Text(
                text = playerDisplayName,
                fontFamily = FredokaFont,
                fontSize = 14.sp,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
            CloudSyncBadge(cloudSyncState = cloudSyncState)
          }
          Text(
            text =
              "Stores only your display name, virtual credits, settings, and win history in Cloud Firestore. Virtual amusement only; no real-money wagering.",
            fontFamily = FredokaFont,
            fontSize = 11.sp,
            color = Color(0xFF94A3B8),
          )
          if (cloudSyncState is CloudSyncState.Error) {
            Text(
              text = cloudSyncState.message,
              fontFamily = FredokaFont,
              fontSize = 11.sp,
              color = Color(0xFFFDE68A),
            )
          }
          if (deleteErrorMessage != null) {
            Text(
              text = deleteErrorMessage ?: "",
              fontFamily = FredokaFont,
              fontSize = 11.sp,
              color = Color(0xFFFCA5A5),
            )
          }
          OutlinedButton(
            onClick = onSignOut,
            enabled = !isDeletingAccount,
            modifier =
              Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("sign_out_button"),
          ) {
            Text(
              text = "SIGN OUT",
              fontFamily = ChakraPetchFont,
              fontWeight = FontWeight.Bold,
              color = DuckBillOrange,
            )
          }
          TextButton(
            onClick = {
              deleteErrorMessage = null
              showDeleteConfirm = true
            },
            enabled = !isDeletingAccount,
            modifier =
              Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("delete_account_button"),
          ) {
            if (isDeletingAccount) {
              CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = Color(0xFFF87171),
                strokeWidth = 2.dp,
              )
            } else {
              Text(
                text = "DELETE ACCOUNT & CLOUD DATA",
                fontFamily = ChakraPetchFont,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color(0xFFF87171),
              )
            }
          }
        }
      }

      Button(
        onClick = onDismiss,
        enabled = !isDeletingAccount,
        colors = ButtonDefaults.buttonColors(containerColor = ArcadeGreen),
        modifier =
          Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .testTag("close_settings_dialog"),
      ) {
        Text("DONE", fontFamily = LuckiestGuyFont, fontSize = 15.sp, color = Color.White)
      }
    }
  }

  if (showDeleteConfirm) {
    AlertDialog(
      onDismissRequest = { if (!isDeletingAccount) showDeleteConfirm = false },
      containerColor = BathDeepNavy,
      titleContentColor = DuckGold,
      textContentColor = Color.White,
      title = {
        Text(
          text = "DELETE ACCOUNT & DATA?",
          fontFamily = LuckiestGuyFont,
          fontSize = 18.sp,
        )
      },
      text = {
        Text(
          text =
            "This permanently deletes your cloud profile, virtual credit balance, win history, and Firebase sign-in record. This action cannot be undone.",
          fontFamily = FredokaFont,
          fontSize = 13.sp,
          color = Color(0xFFE2E8F0),
        )
      },
      confirmButton = {
        Button(
          onClick = {
            showDeleteConfirm = false
            isDeletingAccount = true
            deleteErrorMessage = null
            onDeleteAccount(
              {
                isDeletingAccount = false
              },
              { err ->
                isDeletingAccount = false
                deleteErrorMessage = err
              },
            )
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
          modifier = Modifier.minimumInteractiveComponentSize().testTag("confirm_delete_account_button"),
        ) {
          Text("DELETE FOREVER", fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold, color = Color.White)
        }
      },
      dismissButton = {
        TextButton(
          onClick = { showDeleteConfirm = false },
          modifier = Modifier.minimumInteractiveComponentSize().testTag("cancel_delete_account_button"),
        ) {
          Text("CANCEL", fontFamily = ChakraPetchFont, color = BathTileCyan)
        }
      },
    )
  }
}

@Composable
private fun SettingToggleRow(
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
) {
  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(10.dp))
        .background(Color(0xFF0A2540))
        .padding(horizontal = 12.dp, vertical = 8.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
      Text(title, fontFamily = FredokaFont, fontSize = 14.sp, color = Color.White)
      Text(subtitle, fontFamily = FredokaFont, fontSize = 11.sp, color = Color(0xFF94A3B8))
    }
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors = SwitchDefaults.colors(checkedThumbColor = DuckGold, checkedTrackColor = ArcadeGreen),
    )
  }
}
