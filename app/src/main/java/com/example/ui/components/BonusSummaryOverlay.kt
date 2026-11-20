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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BonusSummary
import com.example.model.formatCents
import com.example.ui.theme.ArcadeGreen
import com.example.ui.theme.ArcadeGreenBright
import com.example.ui.theme.BathDeepNavy
import com.example.ui.theme.ChakraPetchFont
import com.example.ui.theme.DuckBillOrange
import com.example.ui.theme.DuckGold
import com.example.ui.theme.LuckiestGuyFont
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay

@Composable
fun BonusSummaryOverlay(
  summary: BonusSummary?,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AnimatedVisibility(
    visible = summary != null,
    enter = fadeIn(tween(240)) + scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy), initialScale = 0.78f),
    exit = fadeOut(tween(200)) + scaleOut(targetScale = 0.88f),
    modifier = modifier,
  ) {
    summary?.let { active ->
      val infinite = rememberInfiniteTransition(label = "bonus_summary_fx")
      val rayRotation by
        infinite.animateFloat(
          initialValue = 0f,
          targetValue = 360f,
          animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing)),
          label = "ray_rotation",
        )
      val mascotBob by
        infinite.animateFloat(
          initialValue = -5f,
          targetValue = 5f,
          animationSpec = infiniteRepeatable(tween(420), RepeatMode.Reverse),
          label = "mascot_bob",
        )
      val cardPulse by
        infinite.animateFloat(
          initialValue = 0.985f,
          targetValue = 1.03f,
          animationSpec = infiniteRepeatable(tween(520), RepeatMode.Reverse),
          label = "card_pulse",
        )

      var animatedAmountCents by remember(active) { mutableLongStateOf(0L) }
      LaunchedEffect(active) {
        val target = active.totalWonCents
        if (target <= 0L) {
          animatedAmountCents = 0L
        } else {
          val steps = 18
          for (step in 1..steps) {
            animatedAmountCents = (target * step) / steps
            delay(28L)
          }
          animatedAmountCents = target
        }
      }

      Box(
        modifier =
          Modifier
            .fillMaxSize()
            .background(BathDeepNavy.copy(alpha = 0.84f))
            .clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = null,
              onClick = onDismiss,
            ),
        contentAlignment = Alignment.Center,
      ) {
        // Golden rotating sunburst rays behind summary card
        Canvas(modifier = Modifier.fillMaxSize()) {
          val center = Offset(size.width / 2f, size.height / 2f)
          val maxRadius = maxOf(size.width, size.height) * 0.9f
          rotate(degrees = rayRotation, pivot = center) {
            val rayCount = 12
            val step = (2.0 * PI / rayCount).toFloat()
            for (i in 0 until rayCount) {
              if (i % 2 == 0) {
                val a1 = i * step
                val a2 = a1 + step * 0.55f
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
                      colors = listOf(DuckGold.copy(alpha = 0.36f), Color.Transparent),
                      center = center,
                      radius = maxRadius,
                    ),
                )
              }
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
              symbol = active.triggerSymbol,
              modifier = Modifier.size(54.dp).offset(y = mascotBob.dp),
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
              OutlinedSummaryText(text = active.title, fontSize = 23)
              Text(
                text = active.roundsLabel,
                fontFamily = ChakraPetchFont,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color(0xFFBAE6FD),
                textAlign = TextAlign.Center,
              )
            }
            SymbolArtwork(
              symbol = active.triggerSymbol,
              modifier = Modifier.size(54.dp).offset(y = (-mascotBob).dp),
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Total Bonus Win Plaque
          Box(
            modifier =
              Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                  Brush.verticalGradient(
                    listOf(Color(0xFF0C4A6E), Color(0xFF082F49))
                  )
                )
                .border(2.5.dp, DuckGold, RoundedCornerShape(16.dp))
                .padding(vertical = 12.dp, horizontal = 14.dp),
            contentAlignment = Alignment.Center,
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = active.subtitle,
                fontFamily = ChakraPetchFont,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color(0xFFFEF08A),
              )
              Spacer(modifier = Modifier.height(2.dp))
              OutlinedSummaryText(
                text = formatCents(animatedAmountCents),
                fontSize = 38,
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(containerColor = ArcadeGreen),
            modifier =
              Modifier
                .fillMaxWidth()
                .height(48.dp)
                .border(2.dp, ArcadeGreenBright, RoundedCornerShape(24.dp))
                .testTag("dismiss_bonus_summary_button"),
          ) {
            Text(
              text = "CONTINUE",
              fontFamily = LuckiestGuyFont,
              fontSize = 18.sp,
              color = Color.White,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun OutlinedSummaryText(text: String, fontSize: Int) {
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
