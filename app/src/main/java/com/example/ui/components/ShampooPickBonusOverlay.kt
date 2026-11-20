package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ShampooPickBonus
import com.example.model.ShampooPickTile
import com.example.model.SlotSymbol
import com.example.model.formatCents
import com.example.ui.theme.ArcadeGreen
import com.example.ui.theme.ArcadeGreenBright
import com.example.ui.theme.BathDeepNavy
import com.example.ui.theme.BathTileCyan
import com.example.ui.theme.ChakraPetchFont
import com.example.ui.theme.DuckBillOrange
import com.example.ui.theme.DuckGold
import com.example.ui.theme.FredokaFont
import com.example.ui.theme.LuckiestGuyFont

@Composable
fun ShampooPickBonusOverlay(
  bonus: ShampooPickBonus?,
  onPickTile: (Int) -> Unit,
  onCollectBonus: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AnimatedVisibility(
    visible = bonus != null,
    enter = fadeIn(tween(240)) + scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy), initialScale = 0.85f),
    exit = fadeOut(tween(200)),
    modifier = modifier,
  ) {
    bonus?.let { state ->
      val infinite = rememberInfiniteTransition(label = "shampoo_shelf")
      val bob by
        infinite.animateFloat(
          initialValue = -3.5f,
          targetValue = 3.5f,
          animationSpec = infiniteRepeatable(tween(720), RepeatMode.Reverse),
          label = "bob",
        )

      Box(
        modifier =
          Modifier
            .fillMaxSize()
            .background(BathDeepNavy.copy(alpha = 0.88f))
            .padding(14.dp),
        contentAlignment = Alignment.Center,
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(10.dp),
          modifier =
            Modifier
              .widthIn(max = 460.dp)
              .fillMaxWidth()
              .clip(RoundedCornerShape(22.dp))
              .background(
                Brush.verticalGradient(
                  listOf(Color(0xFF0284C7), Color(0xFF0A4DA2), Color(0xFF062552))
                )
              )
              .border(3.5.dp, DuckGold, RoundedCornerShape(22.dp))
              .padding(14.dp),
        ) {
          // Header
          Box(contentAlignment = Alignment.Center) {
            Text(
              text = "FOAM BOTTLE PICK BONUS!",
              style =
                TextStyle(
                  fontFamily = LuckiestGuyFont,
                  fontSize = 23.sp,
                  color = Color.Black,
                  drawStyle = Stroke(width = 7f, join = StrokeJoin.Round),
                ),
              textAlign = TextAlign.Center,
            )
            Text(
              text = "FOAM BOTTLE PICK BONUS!",
              fontFamily = LuckiestGuyFont,
              fontSize = 23.sp,
              color = DuckGold,
              textAlign = TextAlign.Center,
            )
          }

          // Top Meter Pill: Picks Left | Multiplier | Bonus Won
          Row(
            modifier =
              Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(BathDeepNavy.copy(alpha = 0.9f))
                .border(1.5.dp, BathTileCyan, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(horizontalAlignment = Alignment.Start) {
              Text("PICKS LEFT", fontFamily = ChakraPetchFont, fontSize = 10.sp, color = Color(0xFFBAE6FD))
              Text(
                text = "${state.picksRemaining}",
                fontFamily = LuckiestGuyFont,
                fontSize = 22.sp,
                color = if (state.picksRemaining > 0) ArcadeGreenBright else Color(0xFF94A3B8),
              )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("BOOST", fontFamily = ChakraPetchFont, fontSize = 10.sp, color = Color(0xFFBAE6FD))
              Text(
                text = "${state.globalMultiplier}X",
                fontFamily = LuckiestGuyFont,
                fontSize = 22.sp,
                color = if (state.globalMultiplier > 1) DuckGold else Color.White,
              )
            }
            Column(horizontalAlignment = Alignment.End) {
              Text("BONUS WON", fontFamily = ChakraPetchFont, fontSize = 10.sp, color = Color(0xFFBAE6FD))
              Text(
                text = formatCents(state.totalBonusWonCents),
                fontFamily = LuckiestGuyFont,
                fontSize = 22.sp,
                color = DuckGold,
              )
            }
          }

          // Status Prompt
          Text(
            text = state.lastRevealText,
            fontFamily = ChakraPetchFont,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
          )

          // 4x3 Tile Grid of 12 Interactive Lucky Shampoo Bottles
          Column(
            modifier =
              Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                  Brush.verticalGradient(
                    listOf(Color(0xFF38BDF8).copy(alpha = 0.35f), Color(0xFF0369A1).copy(alpha = 0.75f))
                  )
                )
                .border(2.dp, Color.White.copy(alpha = 0.65f), RoundedCornerShape(16.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            for (rowIdx in 0 until 3) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                for (colIdx in 0 until 4) {
                  val tile = state.tiles[rowIdx * 4 + colIdx]
                  val bobOffset = if ((rowIdx + colIdx) % 2 == 0) bob else -bob
                  ShampooBottleTileButton(
                    tile = tile,
                    wagerCents = state.wagerCents,
                    globalMultiplier = state.globalMultiplier,
                    canPick = !state.isComplete && !tile.isRevealed && state.picksRemaining > 0,
                    bobOffsetDp = bobOffset,
                    onClick = { onPickTile(tile.id) },
                    modifier = Modifier.weight(1f).height(76.dp),
                  )
                }
              }
            }
          }

          if (state.isComplete) {
            Button(
              onClick = onCollectBonus,
              colors = ButtonDefaults.buttonColors(containerColor = ArcadeGreen),
              modifier = Modifier.fillMaxWidth().height(50.dp).testTag("collect_shampoo_bonus_button"),
            ) {
              Text(
                text = "COLLECT ${formatCents(state.totalBonusWonCents)}!",
                fontFamily = LuckiestGuyFont,
                fontSize = 18.sp,
                color = Color.White,
              )
            }
          } else {
            Text(
              text = "Find Golden 2X Boosts & +1 Extra Pick Bottles!",
              fontFamily = FredokaFont,
              fontSize = 11.sp,
              color = Color(0xFFBAE6FD),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ShampooBottleTileButton(
  tile: ShampooPickTile,
  wagerCents: Long,
  globalMultiplier: Int,
  canPick: Boolean,
  bobOffsetDp: Float,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier =
      modifier
        .minimumInteractiveComponentSize()
        .clip(RoundedCornerShape(14.dp))
        .background(
          when {
            !tile.isRevealed -> Brush.verticalGradient(listOf(Color(0xFF0EA5E9), Color(0xFF0369A1)))
            tile.isGoldenBoost -> Brush.verticalGradient(listOf(DuckGold, DuckBillOrange))
            tile.isExtraPick -> Brush.verticalGradient(listOf(ArcadeGreenBright, ArcadeGreen))
            else -> Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E293B)))
          }
        )
        .border(
          width = 2.dp,
          color =
            when {
              !tile.isRevealed -> Color.White.copy(alpha = 0.85f)
              tile.isGoldenBoost -> Color.White
              tile.isExtraPick -> Color(0xFFFEF08A)
              else -> DuckGold
            },
          shape = RoundedCornerShape(14.dp),
        )
        .clickable(enabled = canPick, onClick = onClick)
        .testTag("shampoo_pick_tile_${tile.id}"),
    contentAlignment = Alignment.Center,
  ) {
    if (!tile.isRevealed) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        drawOval(
          color = Color.White.copy(alpha = 0.28f),
          topLeft = Offset(size.width * 0.14f, size.height * 0.68f),
          size = androidx.compose.ui.geometry.Size(size.width * 0.72f, size.height * 0.22f),
        )
      }
      SymbolArtwork(
        symbol = SlotSymbol.SHAMPOO,
        modifier = Modifier.size(54.dp).offset(y = bobOffsetDp.dp),
      )
      Box(
        modifier =
          Modifier
            .align(Alignment.TopEnd)
            .padding(4.dp)
            .size(16.dp)
            .clip(CircleShape)
            .background(DuckGold),
        contentAlignment = Alignment.Center,
      ) {
        Text("?", fontFamily = LuckiestGuyFont, fontSize = 11.sp, color = BathDeepNavy)
      }
    } else {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        when {
          tile.isGoldenBoost -> {
            Text("2X ALL", fontFamily = LuckiestGuyFont, fontSize = 15.sp, color = BathDeepNavy)
            Text("BOOST!", fontFamily = ChakraPetchFont, fontSize = 10.sp, color = Color.White)
          }
          tile.isExtraPick -> {
            Text("+1 PICK", fontFamily = LuckiestGuyFont, fontSize = 15.sp, color = Color.White)
            Text(
              formatCents(tile.multiplier * wagerCents),
              fontFamily = ChakraPetchFont,
              fontSize = 11.sp,
              color = Color(0xFFFEF08A),
            )
          }
          else -> {
            Text(
              text = "${tile.multiplier}X",
              fontFamily = LuckiestGuyFont,
              fontSize = 16.sp,
              color = DuckGold,
            )
            Text(
              text = formatCents(tile.multiplier * wagerCents * globalMultiplier),
              fontFamily = ChakraPetchFont,
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp,
              color = Color.White,
            )
          }
        }
      }
    }
  }
}
