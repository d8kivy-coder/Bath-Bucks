package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HotSwapSpot
import com.example.model.SkillPrompt
import com.example.model.SlotSymbol
import com.example.model.WinningLine
import com.example.model.formatCents
import com.example.ui.theme.ArcadeGreenBright
import com.example.ui.theme.BathDeepNavy
import com.example.ui.theme.BathTileCyan
import com.example.ui.theme.CabinetBronze
import com.example.ui.theme.CabinetCopper
import com.example.ui.theme.ChakraPetchFont
import com.example.ui.theme.DuckBillOrange
import com.example.ui.theme.DuckGold
import com.example.ui.theme.LuckiestGuyFont
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun ReelGrid(
  grid: List<List<SlotSymbol>>,
  reelSpinning: List<Boolean>,
  winningLines: List<WinningLine>,
  bonusTriggerCells: Set<Pair<Int, Int>>,
  hotSwapSpots: List<HotSwapSpot>,
  skillPrompt: SkillPrompt?,
  instantWinBadgeCents: Long?,
  onSkillChoose: (SlotSymbol) -> Unit,
  onSkillNudge: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  val infinite = rememberInfiniteTransition(label = "reel_fx")
  val pulse by
    infinite.animateFloat(
      initialValue = 0.45f,
      targetValue = 1.0f,
      animationSpec = infiniteRepeatable(tween(480), RepeatMode.Reverse),
      label = "pulse",
    )
  val winPhase by
    infinite.animateFloat(
      initialValue = 0f,
      targetValue = 1f,
      animationSpec = infiniteRepeatable(tween(1150, easing = LinearEasing), RepeatMode.Restart),
      label = "win_phase",
    )
  val bubblePhase by
    infinite.animateFloat(
      initialValue = 0f,
      targetValue = 1f,
      animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Restart),
      label = "bubble_phase",
    )

  val winningCells: Set<Pair<Int, Int>> =
    winningLines
      .flatMap { wl ->
        (0 until wl.matchCount).map { col -> col to wl.payline.rows[col] }
      }
      .toSet()

  // Outer metallic orange-gold Banilla cabinet bezel with soap bubbles & payline tabs
  Box(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .background(
          Brush.verticalGradient(
            listOf(Color(0xFFFBBF24), CabinetBronze, CabinetCopper, Color(0xFF7C2D12))
          )
        )
        .border(
          width = 3.dp,
          brush = Brush.linearGradient(listOf(Color(0xFFFEF08A), DuckBillOrange, Color(0xFFFEF08A))),
          shape = RoundedCornerShape(16.dp),
        )
        .padding(horizontal = 16.dp, vertical = 8.dp)
  ) {
    // Decorative soap bubbles + payline number markers on left & right pillars
    BubbleRailOverlay(bubblePhase = bubblePhase, modifier = Modifier.fillMaxSize())
    PaylineSideMarkers(
      winningLines = winningLines,
      pulse = pulse,
      modifier = Modifier.fillMaxSize(),
    )

    // Inner glowing cyan-blue reel well (original Bathtime Bucks look)
    Box(
      modifier =
        Modifier
          .fillMaxSize()
          .padding(horizontal = 6.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(
            Brush.verticalGradient(
              listOf(
                Color(0xFF075985),
                Color(0xFF0EA5E9),
                Color(0xFF7DD3FC),
                Color(0xFF0EA5E9),
                Color(0xFF075985),
              )
            )
          )
          .border(2.5.dp, Color(0xFF0C4A6E), RoundedCornerShape(10.dp))
    ) {
      // 5 Reel Columns x 3 Rows with crisp chrome dividers
      Row(
        modifier = Modifier.fillMaxSize().padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        for (col in 0 until 5) {
          val isColumnSpinning = reelSpinning.getOrElse(col) { false }
          val activeSkill = skillPrompt
          val isSkillTarget = activeSkill?.targetColumn == col
          val dimForSkill = activeSkill != null && !isSkillTarget
          val colSymbols =
            grid.getOrElse(col) { listOf(SlotSymbol.DUCK, SlotSymbol.DUCK, SlotSymbol.DUCK) }

          // Excitement anticipation glow when stopped earlier reels already show 2+ bonus triggers
          val stoppedEarlierDucks =
            (0 until col).sumOf { prevCol ->
              if (!reelSpinning.getOrElse(prevCol) { false }) {
                grid.getOrElse(prevCol) { emptyList() }.count {
                  it == SlotSymbol.DUCK || it == SlotSymbol.WILD
                }
              } else {
                0
              }
            }
          val stoppedEarlierShampoo =
            (0 until col).sumOf { prevCol ->
              if (!reelSpinning.getOrElse(prevCol) { false }) {
                grid.getOrElse(prevCol) { emptyList() }.count { it == SlotSymbol.SHAMPOO }
              } else {
                0
              }
            }
          val isAnticipating =
            isColumnSpinning && col >= 2 && (stoppedEarlierDucks >= 2 || stoppedEarlierShampoo >= 2)

          Box(
            modifier =
              Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp))
                .background(
                  Brush.verticalGradient(
                    listOf(
                      Color(0xFF0369A1),
                      Color(0xFF38BDF8),
                      Color(0xFFBAE6FD),
                      Color(0xFF38BDF8),
                      Color(0xFF0369A1),
                    )
                  )
                )
                .border(
                  width =
                    when {
                      isSkillTarget -> 2.5.dp
                      isAnticipating -> 2.5.dp
                      else -> 1.5.dp
                    },
                  color =
                    when {
                      isSkillTarget -> DuckGold
                      isAnticipating -> DuckGold.copy(alpha = pulse)
                      else -> Color.White.copy(alpha = 0.65f)
                    },
                  shape = RoundedCornerShape(8.dp),
                )
                .alpha(if (dimForSkill) 0.55f else 1f)
          ) {
            SpinningReelColumn(
              colIndex = col,
              finalSymbols = colSymbols,
              isSpinning = isColumnSpinning,
              isAnticipating = isAnticipating,
              winningCells = winningCells,
              bonusTriggerCells = bonusTriggerCells,
              hotSwapSpots = hotSwapSpots,
              pulse = pulse,
              winPhase = winPhase,
              bubblePhase = bubblePhase,
              modifier = Modifier.fillMaxSize(),
            )

            // Glossy top/bottom cylinder shading
            Box(
              modifier =
                Modifier
                  .fillMaxSize()
                  .background(
                    Brush.verticalGradient(
                      0.0f to Color(0xFF082F49).copy(alpha = 0.46f),
                      0.14f to Color.Transparent,
                      0.86f to Color.Transparent,
                      1.0f to Color(0xFF082F49).copy(alpha = 0.46f),
                    )
                  )
            )

            // Interactive Skill Choose / Nudge overlay on target column
            if (activeSkill != null && activeSkill.targetColumn == col && !activeSkill.isSolved) {
              SkillColumnOverlay(
                skillPrompt = activeSkill,
                pulse = pulse,
                onSkillChoose = onSkillChoose,
                onSkillNudge = onSkillNudge,
                modifier = Modifier.fillMaxSize(),
              )
            }
          }
        }
      }

      // Winning Paylines Vector Overlay
      if (winningLines.isNotEmpty() && reelSpinning.none { it }) {
        PaylineCanvasOverlay(
          winningLines = winningLines,
          pulse = pulse,
          winPhase = winPhase,
          modifier = Modifier.fillMaxSize().padding(4.dp),
        )
      }

      // Floating "5 of a Kind" or "Instant Win $XX.XX" Banilla arcade overlay
      val hasFiveOfAKind =
        winningLines.any { it.matchCount == 5 && !it.isProgressive } && reelSpinning.none { it }
      AnimatedVisibility(
        visible = (instantWinBadgeCents != null || hasFiveOfAKind) && skillPrompt == null,
        enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
        exit = scaleOut() + fadeOut(),
        modifier = Modifier.align(Alignment.Center),
      ) {
        if (hasFiveOfAKind) {
          FiveOfAKindPlaque(amountCents = instantWinBadgeCents, winPhase = winPhase)
        } else {
          instantWinBadgeCents?.let { amount ->
            InstantWinPlaque(amountCents = amount, winPhase = winPhase)
          }
        }
      }
    }
  }
}

@Composable
private fun SpinningReelColumn(
  colIndex: Int,
  finalSymbols: List<SlotSymbol>,
  isSpinning: Boolean,
  isAnticipating: Boolean,
  winningCells: Set<Pair<Int, Int>>,
  bonusTriggerCells: Set<Pair<Int, Int>>,
  hotSwapSpots: List<HotSwapSpot>,
  pulse: Float,
  winPhase: Float,
  bubblePhase: Float,
  modifier: Modifier = Modifier,
) {
  // Deterministic 20-symbol strip per column for smooth continuous scrolling
  val strip = remember(colIndex) {
    val all = SlotSymbol.entries
    List(20) { i -> all[(i * 3 + colIndex * 5) % all.size] }
  }

  var spinProgress by remember { mutableFloatStateOf(0f) }
  val settleOffset = remember { Animatable(0f) }
  val lockFlash = remember { Animatable(0f) }
  var hadFirstSpin by remember { mutableFloatStateOf(0f) }

  LaunchedEffect(isSpinning) {
    if (isSpinning) {
      hadFirstSpin = 1f
      // Brief physical upward reel anticipation kick (~65ms) before dropping down fast
      var kickElapsed = 0f
      var lastNanos = 0L
      val symbolsPerSec = 19.0f + colIndex * 1.25f
      while (true) {
        withFrameNanos { frameNanos ->
          if (lastNanos != 0L) {
            val dt = ((frameNanos - lastNanos) / 1_000_000_000f).coerceAtMost(0.05f)
            if (kickElapsed < 0.065f) {
              kickElapsed += dt
              spinProgress -= dt * 3.2f
            } else {
              val speedMult = if (isAnticipating) 1.22f else 1f
              spinProgress += dt * symbolsPerSec * speedMult
            }
          }
          lastNanos = frameNanos
        }
      }
    } else if (hadFirstSpin > 0f) {
      // Heavy physical drum overshoot & spring bounce + lock-in flash sheen
      lockFlash.snapTo(1f)
      settleOffset.snapTo(-0.68f)
      settleOffset.animateTo(
        targetValue = 0f,
        animationSpec =
          spring(
            dampingRatio = 0.42f,
            stiffness = 360f,
          ),
      )
      lockFlash.animateTo(
        targetValue = 0f,
        animationSpec = tween(durationMillis = 220),
      )
    }
  }

  BoxWithConstraints(modifier = modifier.clipToBounds()) {
    val density = LocalDensity.current
    val cellHeightDp = maxHeight / 3f
    val cellHeightPx = with(density) { cellHeightDp.toPx() }

    if (isSpinning) {
      val baseIndex = kotlin.math.floor(spinProgress).toInt()
      val frac = spinProgress - baseIndex // 0f..1f moving downward

      // Render 4 consecutive symbols (-1..2) scrolling smoothly with vertical motion stretch
      for (slot in -1..2) {
        val stripIdx = Math.floorMod(baseIndex - slot, strip.size)
        val symbol = strip[stripIdx]
        val yPx = ((slot + frac) * cellHeightPx).roundToInt()

        Box(
          modifier =
            Modifier
              .fillMaxWidth()
              .height(cellHeightDp)
              .offset { IntOffset(0, yPx) }
              .graphicsLayer {
                scaleY = 1.07f
                scaleX = 0.96f
              }
              .padding(3.dp),
          contentAlignment = Alignment.Center,
        ) {
          SymbolArtwork(
            symbol = symbol,
            modifier = Modifier.fillMaxSize().alpha(0.90f),
          )
        }
      }

      // Vertical glossy motion-blur streaks + optional excitement anticipation glow
      Canvas(modifier = Modifier.fillMaxSize()) {
        val streakAlpha = 0.14f
        drawLine(
          color = Color.White.copy(alpha = streakAlpha),
          start = Offset(size.width * 0.22f, 0f),
          end = Offset(size.width * 0.22f, size.height),
          strokeWidth = 3f,
        )
        drawLine(
          color = Color.White.copy(alpha = streakAlpha * 0.8f),
          start = Offset(size.width * 0.78f, 0f),
          end = Offset(size.width * 0.78f, size.height),
          strokeWidth = 2.5f,
        )
        if (isAnticipating) {
          drawRect(
            brush =
              Brush.verticalGradient(
                listOf(
                  DuckGold.copy(alpha = 0.28f * pulse),
                  Color.Transparent,
                  DuckGold.copy(alpha = 0.28f * pulse),
                )
              )
          )
        }
      }
    } else {
      val bouncePx = (settleOffset.value * cellHeightPx).roundToInt()

      for (row in 0 until 3) {
        val symbol = finalSymbols.getOrElse(row) { SlotSymbol.DUCK }
        val isWinningCell = (colIndex to row) in winningCells
        val isBonusCell = (colIndex to row) in bonusTriggerCells
        val isHotSwapCell = hotSwapSpots.any { it.col == colIndex && it.row == row }
        val yPx = (row * cellHeightPx).roundToInt() + bouncePx

        val phaseAngle = (winPhase * 2f * PI.toFloat()) + (colIndex * 0.45f)
        val winPopScale =
          when {
            isHotSwapCell -> 1.0f + 0.16f * pulse
            isBonusCell -> 1.0f + 0.13f * (0.5f + 0.5f * sin(phaseAngle))
            isWinningCell -> 1.0f + 0.10f * (0.5f + 0.5f * sin(phaseAngle))
            else -> 1.0f
          }
        val winRockDeg =
          when {
            isBonusCell -> sin(phaseAngle) * 5.5f
            isWinningCell -> sin(phaseAngle) * 3.8f
            else -> 0f
          }

        Box(
          modifier =
            Modifier
              .fillMaxWidth()
              .height(cellHeightDp)
              .offset { IntOffset(0, yPx) }
              .padding(2.dp)
              .clip(RoundedCornerShape(6.dp))
              .background(
                when {
                  isHotSwapCell -> BathTileCyan.copy(alpha = 0.52f)
                  isBonusCell -> ArcadeGreenBright.copy(alpha = 0.28f * pulse)
                  isWinningCell -> DuckGold.copy(alpha = 0.24f * pulse)
                  else -> Color.Transparent
                }
              )
              .border(
                width = if (isWinningCell || isHotSwapCell || isBonusCell) 2.dp else 0.dp,
                color =
                  when {
                    isHotSwapCell -> Color.White
                    isBonusCell -> ArcadeGreenBright.copy(alpha = pulse)
                    isWinningCell -> DuckGold.copy(alpha = pulse)
                    else -> Color.Transparent
                  },
                shape = RoundedCornerShape(6.dp),
              ),
          contentAlignment = Alignment.Center,
        ) {
          SymbolArtwork(
            symbol = symbol,
            modifier =
              Modifier
                .fillMaxSize()
                .padding(2.dp)
                .graphicsLayer {
                  scaleX = winPopScale
                  scaleY = winPopScale
                  rotationZ = winRockDeg
                },
          )

          // Rising animated soap bubbles on winning, bonus, or Hot Swap cells
          if (isWinningCell || isBonusCell || isHotSwapCell) {
            CellBubbleBurstOverlay(
              bubblePhase = bubblePhase,
              isHotSwap = isHotSwapCell,
              pulse = pulse,
              seedOffset = colIndex * 3 + row,
              modifier = Modifier.fillMaxSize(),
            )
          }

          if (isBonusCell) {
            Text(
              text = "BONUS",
              fontFamily = ChakraPetchFont,
              fontWeight = FontWeight.Bold,
              fontSize = 8.sp,
              color = Color.White,
              modifier =
                Modifier
                  .align(Alignment.BottomCenter)
                  .clip(RoundedCornerShape(4.dp))
                  .background(ArcadeGreenBright.copy(alpha = 0.94f))
                  .padding(horizontal = 4.dp, vertical = 1.dp),
            )
          } else if (isHotSwapCell) {
            Text(
              text = "SWAP!",
              fontFamily = LuckiestGuyFont,
              fontSize = 9.sp,
              color = DuckGold,
              modifier =
                Modifier
                  .align(Alignment.TopCenter)
                  .clip(RoundedCornerShape(4.dp))
                  .background(Color(0xFFDC2626).copy(alpha = 0.92f))
                  .padding(horizontal = 5.dp, vertical = 1.dp),
            )
          }
        }
      }

      // Crisp lock-in flash sheen right as the reel settles
      if (lockFlash.value > 0.01f) {
        Box(
          modifier =
            Modifier
              .fillMaxSize()
              .background(
                Brush.verticalGradient(
                  listOf(
                    Color.White.copy(alpha = 0.28f * lockFlash.value),
                    Color.Transparent,
                    Color.White.copy(alpha = 0.16f * lockFlash.value),
                  )
                )
              )
        )
      }
    }
  }
}

@Composable
private fun CellBubbleBurstOverlay(
  bubblePhase: Float,
  isHotSwap: Boolean,
  pulse: Float,
  seedOffset: Int,
  modifier: Modifier = Modifier,
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    if (isHotSwap) {
      val ringR = (w * 0.25f) + (w * 0.26f * pulse)
      drawCircle(
        color = Color.White.copy(alpha = 0.85f * (1.1f - pulse).coerceIn(0.2f, 1f)),
        radius = ringR,
        center = Offset(w * 0.5f, h * 0.5f),
        style = Stroke(width = 3.5f),
      )
    }
    for (i in 0 until 4) {
      val p = (bubblePhase + i * 0.25f + seedOffset * 0.13f) % 1f
      val bx = w * (0.18f + ((i * 29 + seedOffset * 17) % 64) / 100f) + sin(p * 6.28f) * 4f
      val by = h * (0.92f - p * 0.84f)
      val r = (3.5f + (i % 3) * 1.8f) * (1f - p * 0.35f)
      val alpha = (1f - p).coerceIn(0f, 1f) * 0.8f
      drawCircle(
        color = Color.White.copy(alpha = alpha * 0.45f),
        radius = r,
        center = Offset(bx, by),
      )
      drawCircle(
        color = Color(0xFFE0F2FE).copy(alpha = alpha),
        radius = r,
        center = Offset(bx, by),
        style = Stroke(width = 1.4f),
      )
    }
  }
}

@Composable
private fun SkillColumnOverlay(
  skillPrompt: SkillPrompt,
  pulse: Float,
  onSkillChoose: (SlotSymbol) -> Unit,
  onSkillNudge: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier =
      modifier
        .background(BathDeepNavy.copy(alpha = 0.72f))
        .padding(2.dp)
  ) {
    Column(
      modifier = Modifier.fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      IconButton(
        onClick = { onSkillNudge(-1) },
        modifier =
          Modifier
            .minimumInteractiveComponentSize()
            .size(36.dp)
            .clip(CircleShape)
            .background(ArcadeGreenBright.copy(alpha = 0.9f))
            .border(1.5.dp, Color.White, CircleShape)
            .testTag("skill_nudge_up"),
      ) {
        Icon(
          imageVector = Icons.Default.KeyboardArrowUp,
          contentDescription = "Nudge reel up",
          tint = Color.White,
        )
      }

      Column(
        modifier = Modifier.weight(1f).fillMaxWidth(),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        skillPrompt.candidateSymbols.forEachIndexed { idx, candidate ->
          Box(
            modifier =
              Modifier
                .minimumInteractiveComponentSize()
                .size(48.dp)
                .clip(CircleShape)
                .background(BathDeepNavy.copy(alpha = 0.92f))
                .border(2.dp, DuckGold.copy(alpha = pulse), CircleShape)
                .clickable { onSkillChoose(candidate) }
                .padding(4.dp)
                .testTag("skill_choose_$idx"),
            contentAlignment = Alignment.Center,
          ) {
            SymbolArtwork(symbol = candidate, modifier = Modifier.fillMaxSize())
          }
        }
      }

      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = "CHOOSE",
          fontFamily = ChakraPetchFont,
          fontWeight = FontWeight.Bold,
          fontSize = 9.sp,
          color = DuckGold,
        )
        IconButton(
          onClick = { onSkillNudge(1) },
          modifier =
            Modifier
              .minimumInteractiveComponentSize()
              .size(36.dp)
              .clip(CircleShape)
              .background(ArcadeGreenBright.copy(alpha = 0.9f))
              .border(1.5.dp, Color.White, CircleShape)
              .testTag("skill_nudge_down"),
        ) {
          Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = "Nudge reel down",
            tint = Color.White,
          )
        }
      }
    }
  }
}

@Composable
private fun PaylineCanvasOverlay(
  winningLines: List<WinningLine>,
  pulse: Float,
  winPhase: Float,
  modifier: Modifier = Modifier,
) {
  Canvas(modifier = modifier) {
    val colW = size.width / 5f
    val rowH = size.height / 3f

    for (wl in winningLines) {
      val path = Path()
      for (col in 0 until 5) {
        val x = col * colW + colW / 2f
        val y = wl.payline.rows[col] * rowH + rowH / 2f
        if (col == 0) path.moveTo(x, y) else path.lineTo(x, y)
      }
      drawPath(
        path = path,
        color = wl.payline.color.copy(alpha = 0.40f * pulse),
        style = Stroke(width = 16f, cap = StrokeCap.Round, join = StrokeJoin.Round),
      )
      drawPath(
        path = path,
        color = wl.payline.color,
        style = Stroke(width = 5.5f, cap = StrokeCap.Round, join = StrokeJoin.Round),
      )
      // Traveling sparkle bead along the active winning payline
      val travel = (winPhase * 4f).coerceIn(0f, 3.999f)
      val seg = travel.toInt()
      val t = travel - seg
      val x0 = seg * colW + colW / 2f
      val y0 = wl.payline.rows[seg] * rowH + rowH / 2f
      val x1 = (seg + 1) * colW + colW / 2f
      val y1 = wl.payline.rows[seg + 1] * rowH + rowH / 2f
      val cx = x0 + (x1 - x0) * t
      val cy = y0 + (y1 - y0) * t
      drawCircle(color = Color.White, radius = 6.5f, center = Offset(cx, cy))
      drawCircle(color = wl.payline.color, radius = 10f, center = Offset(cx, cy), style = Stroke(2.5f))
    }
  }
}

@Composable
private fun InstantWinPlaque(
  amountCents: Long,
  winPhase: Float,
) {
  val bobScale = 1.0f + 0.05f * sin(winPhase * 2f * PI.toFloat())
  Box(
    contentAlignment = Alignment.Center,
    modifier =
      Modifier.graphicsLayer {
        scaleX = bobScale
        scaleY = bobScale
      },
  ) {
    // Rotating golden starburst rays behind the Instant Win badge
    Canvas(modifier = Modifier.size(210.dp, 118.dp)) {
      val cx = size.width / 2f
      val cy = size.height / 2f
      val rayCount = 12
      val rotOffset = winPhase * 2f * PI.toFloat() / rayCount
      for (i in 0 until rayCount) {
        val angle = rotOffset + (i * 2f * PI.toFloat() / rayCount)
        val rInner = 24f
        val rOuter = if (i % 2 == 0) size.width * 0.48f else size.width * 0.36f
        val p =
          Path().apply {
            moveTo(cx + cos(angle - 0.14f) * rInner, cy + sin(angle - 0.14f) * rInner)
            lineTo(cx + cos(angle) * rOuter, cy + sin(angle) * rOuter * 0.62f)
            lineTo(cx + cos(angle + 0.14f) * rInner, cy + sin(angle + 0.14f) * rInner)
            close()
          }
        drawPath(
          path = p,
          color = Color(0xFFFDE047).copy(alpha = if (i % 2 == 0) 0.50f else 0.28f),
        )
      }
    }

    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy((-6).dp),
    ) {
      // Slanted Banilla "Instant Win" 3D gold outlined title
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.graphicsLayer { rotationZ = -4.5f },
      ) {
        Text(
          text = "Instant Win",
          fontFamily = LuckiestGuyFont,
          fontSize = 26.sp,
          color = Color.Black,
          modifier = Modifier.offset(x = 2.dp, y = 2.dp),
        )
        Text(
          text = "Instant Win",
          style =
            TextStyle(
              fontFamily = LuckiestGuyFont,
              fontSize = 26.sp,
              color = Color(0xFF451A03),
              drawStyle = Stroke(width = 8f, join = StrokeJoin.Round),
            ),
        )
        Text(
          text = "Instant Win",
          style =
            TextStyle(
              fontFamily = LuckiestGuyFont,
              fontSize = 26.sp,
              brush =
                Brush.verticalGradient(
                  listOf(Color(0xFFFFFBEB), DuckGold, Color(0xFFF59E0B))
                ),
            ),
        )
      }

      // Bold "$12.00" navy-gold Banilla prize oval
      Box(
        contentAlignment = Alignment.Center,
        modifier =
          Modifier
            .clip(RoundedCornerShape(50))
            .background(
              Brush.verticalGradient(
                listOf(Color(0xFF0C4A6E), BathDeepNavy)
              )
            )
            .border(
              width = 3.dp,
              brush = Brush.linearGradient(listOf(Color(0xFFFEF08A), DuckBillOrange, Color(0xFFFEF08A))),
              shape = RoundedCornerShape(50),
            )
            .padding(horizontal = 24.dp, vertical = 6.dp),
      ) {
        Text(
          text = formatCents(amountCents),
          style =
            TextStyle(
              fontFamily = LuckiestGuyFont,
              fontSize = 30.sp,
              color = Color.Black,
              drawStyle = Stroke(width = 7f, join = StrokeJoin.Round),
            ),
        )
        Text(
          text = formatCents(amountCents),
          style =
            TextStyle(
              fontFamily = LuckiestGuyFont,
              fontSize = 30.sp,
              brush = Brush.verticalGradient(listOf(Color.White, Color(0xFFFEF08A))),
              textAlign = TextAlign.Center,
            ),
        )
      }
    }
  }
}

@Composable
private fun FiveOfAKindPlaque(
  amountCents: Long?,
  winPhase: Float,
) {
  val popScale = 1.0f + 0.06f * sin(winPhase * 2f * PI.toFloat())
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(2.dp),
    modifier =
      Modifier.graphicsLayer {
        scaleX = popScale
        scaleY = popScale
      },
  ) {
    Box(contentAlignment = Alignment.Center) {
      Text(
        text = "5 of a Kind",
        fontFamily = LuckiestGuyFont,
        fontSize = 38.sp,
        color = Color.Black.copy(alpha = 0.85f),
        modifier = Modifier.offset(x = 3.dp, y = 4.dp),
      )
      Text(
        text = "5 of a Kind",
        style =
          TextStyle(
            fontFamily = LuckiestGuyFont,
            fontSize = 38.sp,
            color = Color.White,
            drawStyle = Stroke(width = 12f, join = StrokeJoin.Round),
          ),
      )
      Text(
        text = "5 of a Kind",
        style =
          TextStyle(
            fontFamily = LuckiestGuyFont,
            fontSize = 38.sp,
            brush = Brush.verticalGradient(listOf(Color(0xFFFFF59D), DuckGold, Color(0xFFEA580C))),
          ),
      )
    }
    if (amountCents != null) {
      Box(
        contentAlignment = Alignment.Center,
        modifier =
          Modifier
            .clip(RoundedCornerShape(50))
            .background(BathDeepNavy.copy(alpha = 0.92f))
            .border(2.dp, DuckGold, RoundedCornerShape(50))
            .padding(horizontal = 18.dp, vertical = 3.dp),
      ) {
        Text(
          text = formatCents(amountCents),
          fontFamily = LuckiestGuyFont,
          fontSize = 20.sp,
          color = DuckGold,
        )
      }
    }
  }
}

@Composable
private fun BubbleRailOverlay(
  bubblePhase: Float,
  modifier: Modifier = Modifier,
) {
  Canvas(modifier = modifier) {
    val radii = listOf(5f, 7f, 4.5f, 6.5f, 8f, 5f, 6.5f, 4.5f)
    val leftX = 6f
    val rightX = size.width - 6f
    val stepY = size.height / (radii.size + 1)

    radii.forEachIndexed { idx, r ->
      val drift = sin((bubblePhase + idx * 0.14f) * 2f * PI.toFloat()) * 3f
      val y = stepY * (idx + 1) + drift
      drawCircle(Color.White.copy(alpha = 0.58f), radius = r, center = Offset(leftX, y))
      drawCircle(
        Color(0xFFBAE6FD).copy(alpha = 0.88f),
        radius = r,
        center = Offset(leftX, y),
        style = Stroke(1.5f),
      )
      drawCircle(Color.White.copy(alpha = 0.58f), radius = r, center = Offset(rightX, y))
      drawCircle(
        Color(0xFFBAE6FD).copy(alpha = 0.88f),
        radius = r,
        center = Offset(rightX, y),
        style = Stroke(1.5f),
      )
    }
  }
}

@Composable
private fun PaylineSideMarkers(
  winningLines: List<WinningLine>,
  pulse: Float,
  modifier: Modifier = Modifier,
) {
  val activeIndices = winningLines.map { it.payline.index }.toSet()
  Box(modifier = modifier) {
    Column(
      modifier = Modifier.align(Alignment.CenterStart).fillMaxHeight().padding(vertical = 8.dp),
      verticalArrangement = Arrangement.SpaceEvenly,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      listOf(2, 4, 1, 5, 3).forEach { num ->
        val lit = num in activeIndices
        Box(
          modifier =
            Modifier
              .size(14.dp)
              .clip(CircleShape)
              .background(
                if (lit) DuckGold.copy(alpha = 0.7f + 0.3f * pulse) else BathDeepNavy.copy(alpha = 0.8f)
              )
              .border(1.dp, Color.White.copy(alpha = 0.85f), CircleShape),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = "$num",
            fontFamily = ChakraPetchFont,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = if (lit) BathDeepNavy else Color.White,
          )
        }
      }
    }
    Column(
      modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight().padding(vertical = 8.dp),
      verticalArrangement = Arrangement.SpaceEvenly,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      listOf(6, 8, 9, 7).forEach { num ->
        val lit = num in activeIndices
        Box(
          modifier =
            Modifier
              .size(14.dp)
              .clip(CircleShape)
              .background(
                if (lit) DuckGold.copy(alpha = 0.7f + 0.3f * pulse) else BathDeepNavy.copy(alpha = 0.8f)
              )
              .border(1.dp, Color.White.copy(alpha = 0.85f), CircleShape),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = "$num",
            fontFamily = ChakraPetchFont,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = if (lit) BathDeepNavy else Color.White,
          )
        }
      }
    }
  }
}
