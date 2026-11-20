package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BonusCelebration
import com.example.model.BonusType
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class CelebrationParticle(
  val angleRad: Float,
  val speedFactor: Float,
  val radiusDp: Float,
  val isGoldCoin: Boolean,
  val phaseOffset: Float,
)

@Composable
fun BonusCelebrationOverlay(
  celebration: BonusCelebration?,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AnimatedVisibility(
    visible = celebration != null,
    enter = fadeIn(tween(220)) + scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy), initialScale = 0.7f),
    exit = fadeOut(tween(220)) + scaleOut(targetScale = 0.85f),
    modifier = modifier,
  ) {
    celebration?.let { active ->
      val infinite = rememberInfiniteTransition(label = "bonus_celebration")
      val rayRotation by
        infinite.animateFloat(
          initialValue = 0f,
          targetValue = 360f,
          animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing)),
          label = "ray_rotation",
        )
      val mascotBounce by
        infinite.animateFloat(
          initialValue = -6f,
          targetValue = 6f,
          animationSpec = infiniteRepeatable(tween(380), RepeatMode.Reverse),
          label = "mascot_bounce",
        )
      val cardPulse by
        infinite.animateFloat(
          initialValue = 0.98f,
          targetValue = 1.04f,
          animationSpec = infiniteRepeatable(tween(460), RepeatMode.Reverse),
          label = "card_pulse",
        )

      val particles = remember(active) {
        List(42) { idx ->
          CelebrationParticle(
            angleRad = (idx.toFloat() / 42f) * (2f * PI.toFloat()) + Random.nextFloat() * 0.25f,
            speedFactor = 0.45f + Random.nextFloat() * 0.9f,
            radiusDp = 6f + Random.nextFloat() * 14f,
            isGoldCoin = idx % 2 == 0,
            phaseOffset = Random.nextFloat() * 0.35f,
          )
        }
      }

      var particleTime by remember(active) { mutableFloatStateOf(0f) }
      LaunchedEffect(active) {
        var startNanos = 0L
        while (true) {
          withFrameNanos { now ->
            if (startNanos == 0L) startNanos = now
            particleTime = (now - startNanos) / 1_000_000_000f
          }
        }
      }

      val mascotSymbol =
        when (active.bonusType) {
          BonusType.DUCK_SPINS -> SlotSymbol.DUCK
          BonusType.PROGRESSIVE -> SlotSymbol.WILD
          BonusType.SHAMPOO_PICK -> SlotSymbol.SHAMPOO
        }

      Box(
        modifier =
          Modifier
            .fillMaxSize()
            .background(BathDeepNavy.copy(alpha = 0.78f))
            .clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = null,
              onClick = onDismiss,
            ),
        contentAlignment = Alignment.Center,
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val center = Offset(size.width / 2f, size.height / 2f)
          val maxRadius = maxOf(size.width, size.height) * 0.95f

          rotate(degrees = rayRotation, pivot = center) {
            val rayCount = 14
            val stepAngle = (2.0 * PI / rayCount).toFloat()
            for (i in 0 until rayCount) {
              if (i % 2 == 0) {
                val a1 = i * stepAngle
                val a2 = a1 + stepAngle * 0.55f
                val path = Path().apply {
                  moveTo(center.x, center.y)
                  lineTo(center.x + cos(a1) * maxRadius, center.y + sin(a1) * maxRadius)
                  lineTo(center.x + cos(a2) * maxRadius, center.y + sin(a2) * maxRadius)
                  close()
                }
                drawPath(
                  path = path,
                  brush =
                    Brush.radialGradient(
                      colors = listOf(DuckGold.copy(alpha = 0.38f), Color.Transparent),
                      center = center,
                      radius = maxRadius,
                    ),
                )
              }
            }
          }

          for (p in particles) {
            val t = ((particleTime * p.speedFactor + p.phaseOffset) % 1.8f)
            val dist = t * minOf(size.width, size.height) * 0.48f
            val liftY = -t * 95f
            val px = center.x + cos(p.angleRad) * dist
            val py = center.y + sin(p.angleRad) * dist + liftY
            val alpha = (1f - (t / 1.8f)).coerceIn(0f, 1f)
            val rPx = p.radiusDp.dp.toPx()

            if (p.isGoldCoin) {
              drawCircle(color = DuckGold.copy(alpha = alpha), radius = rPx, center = Offset(px, py))
              drawCircle(
                color = Color.White.copy(alpha = alpha * 0.85f),
                radius = rPx,
                center = Offset(px, py),
                style = Stroke(width = 2.5f),
              )
            } else {
              drawCircle(
                brush =
                  Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = alpha * 0.8f), BathTileCyan.copy(alpha = alpha * 0.45f)),
                    center = Offset(px - rPx * 0.3f, py - rPx * 0.3f),
                    radius = rPx * 1.4f,
                  ),
                radius = rPx * 1.15f,
                center = Offset(px, py),
              )
              drawCircle(
                color = Color.White.copy(alpha = alpha * 0.9f),
                radius = rPx * 1.15f,
                center = Offset(px, py),
                style = Stroke(width = 2f),
              )
            }
          }
        }

        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier =
            Modifier
              .padding(horizontal = 20.dp)
              .widthIn(max = 420.dp)
              .fillMaxWidth()
              .scale(cardPulse)
              .clip(RoundedCornerShape(24.dp))
              .background(
                Brush.verticalGradient(
                  listOf(Color(0xFF0E7490), Color(0xFF0A3D62), BathDeepNavy)
                )
              )
              .border(
                width = 4.dp,
                brush = Brush.linearGradient(listOf(Color(0xFFFEF08A), DuckGold, DuckBillOrange, DuckGold)),
                shape = RoundedCornerShape(24.dp),
              )
              .padding(horizontal = 18.dp, vertical = 20.dp),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            SymbolArtwork(
              symbol = mascotSymbol,
              modifier = Modifier.size(56.dp).offset(y = mascotBounce.dp),
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
              val title =
                when (active.bonusType) {
                  BonusType.PROGRESSIVE -> "PROGRESSIVE!"
                  BonusType.DUCK_SPINS -> "SPLASH BONUS SPINS!"
                  BonusType.SHAMPOO_PICK -> "FOAM BOTTLE PICK!"
                }
              OutlinedCelebrationText(text = title, fontSize = 24)
              val sub =
                when (active.bonusType) {
                  BonusType.DUCK_SPINS -> "${active.triggerCount} RUBBER DUCKS LANDED!"
                  BonusType.SHAMPOO_PICK -> "${active.triggerCount} SUDSY SHAMPOO BOTTLES!"
                  BonusType.PROGRESSIVE -> "5 WILD BUBBLES!"
                }
              Text(
                text = sub,
                fontFamily = ChakraPetchFont,
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp,
                color = Color(0xFFBAE6FD),
                textAlign = TextAlign.Center,
              )
            }
            SymbolArtwork(
              symbol = mascotSymbol,
              modifier = Modifier.size(56.dp).offset(y = (-mascotBounce).dp),
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          if (active.bonusType == BonusType.SHAMPOO_PICK && active.picksAwarded > 0) {
            Box(
              modifier =
                Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(14.dp))
                  .background(Brush.horizontalGradient(listOf(DuckBillOrange, DuckGold, DuckBillOrange)))
                  .border(2.dp, Color.White, RoundedCornerShape(14.dp))
                  .padding(vertical = 10.dp, horizontal = 12.dp),
              contentAlignment = Alignment.Center,
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                  text = "${active.picksAwarded} FOAM BOTTLE PICKS!",
                  fontFamily = LuckiestGuyFont,
                  fontSize = 21.sp,
                  color = BathDeepNavy,
                  textAlign = TextAlign.Center,
                )
                Text(
                  text = "PICK BOTTLES TO REVEAL MULTIPLIERS & 2X BOOSTS",
                  fontFamily = ChakraPetchFont,
                  fontWeight = FontWeight.Bold,
                  fontSize = 10.sp,
                  color = BathDeepNavy,
                )
              }
            }
          } else if (active.spinsAwarded > 0) {
            Box(
              modifier =
                Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(14.dp))
                  .background(Brush.horizontalGradient(listOf(ArcadeGreen, ArcadeGreenBright, ArcadeGreen)))
                  .border(2.dp, Color.White, RoundedCornerShape(14.dp))
                  .padding(vertical = 10.dp, horizontal = 12.dp),
              contentAlignment = Alignment.Center,
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                  text = "+${active.spinsAwarded} SPLASH BONUS SPINS!",
                  fontFamily = LuckiestGuyFont,
                  fontSize = 21.sp,
                  color = Color.White,
                  textAlign = TextAlign.Center,
                )
                Text(
                  text = "ALL LINE WINS DOUBLED (2X MULTIPLIER)",
                  fontFamily = ChakraPetchFont,
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp,
                  color = Color(0xFFFEF08A),
                )
              }
            }
          }

          if (active.scatterPayoutCents > 0L) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = if (active.bonusType == BonusType.PROGRESSIVE) "TOP PRIZE WON" else "SCATTER AWARD",
              fontFamily = ChakraPetchFont,
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp,
              color = Color(0xFFBAE6FD),
            )
            OutlinedCelebrationText(
              text = formatCents(active.scatterPayoutCents),
              fontSize = 32,
            )
          }

          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = "TAP TO CONTINUE",
            fontFamily = FredokaFont,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.78f),
          )
        }
      }
    }
  }
}

@Composable
private fun OutlinedCelebrationText(text: String, fontSize: Int) {
  Box(contentAlignment = Alignment.Center) {
    Text(
      text = text,
      style =
        TextStyle(
          fontFamily = LuckiestGuyFont,
          fontSize = fontSize.sp,
          color = Color.Black,
          drawStyle = Stroke(width = 8f, join = StrokeJoin.Round),
        ),
      textAlign = TextAlign.Center,
    )
    Text(
      text = text,
      style =
        TextStyle(
          fontFamily = LuckiestGuyFont,
          fontSize = fontSize.sp,
          brush = Brush.verticalGradient(listOf(Color(0xFFFFF59D), DuckGold, DuckBillOrange)),
        ),
      textAlign = TextAlign.Center,
    )
  }
}
