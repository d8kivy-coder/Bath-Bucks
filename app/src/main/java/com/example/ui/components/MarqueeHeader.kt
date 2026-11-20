package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.formatCents
import com.example.ui.theme.ArcadeGreenBright
import com.example.ui.theme.BathDeepNavy
import com.example.ui.theme.BathTileCyan
import com.example.ui.theme.ChakraPetchFont
import com.example.ui.theme.DuckBillOrange
import com.example.ui.theme.DuckGold
import com.example.ui.theme.LuckiestGuyFont
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun MarqueeHeader(
  progressiveCents: Long,
  isProgressiveEligible: Boolean,
  bonusSpinsRemaining: Int,
  modifier: Modifier = Modifier,
) {
  val infiniteTransition = rememberInfiniteTransition(label = "marquee_glow")
  val pulseAlpha by
    infiniteTransition.animateFloat(
      initialValue = 0.65f,
      targetValue = 1.0f,
      animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
      label = "pulse_alpha",
    )
  val bubbleFlow by
    infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = 1f,
      animationSpec = infiniteRepeatable(tween(4200, easing = LinearEasing), RepeatMode.Restart),
      label = "bubble_flow",
    )

  Box(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(14.dp))
        .border(
          width = 3.dp,
          brush =
            Brush.linearGradient(
              listOf(Color(0xFFFDE047), Color(0xFFD97706), Color(0xFF38BDF8), Color(0xFFFDE047))
            ),
          shape = RoundedCornerShape(14.dp),
        )
  ) {
    Image(
      painter = painterResource(id = R.drawable.img_marquee_backdrop_1794592673673),
      contentDescription = stringResource(id = R.string.marquee_title),
      modifier = Modifier.fillMaxSize(),
      contentScale = ContentScale.Crop,
    )

    // Subtle dark bottom scrim for crisp logo legibility
    Box(
      modifier =
        Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              listOf(
                Color.Black.copy(alpha = 0.15f),
                Color.Black.copy(alpha = 0.10f),
                BathDeepNavy.copy(alpha = 0.55f),
              )
            )
          )
    )

    // Floating soap bubbles rising gently across the marquee tub backdrop
    Canvas(modifier = Modifier.fillMaxSize()) {
      val seeds = listOf(0.08f, 0.21f, 0.34f, 0.48f, 0.63f, 0.77f, 0.90f)
      seeds.forEachIndexed { idx, baseX ->
        val p = (bubbleFlow + idx * 0.16f) % 1f
        val sway = sin((p + idx * 0.2f) * 2f * PI.toFloat()) * 8f
        val cx = size.width * baseX + sway
        val cy = size.height * (1.08f - p * 1.16f)
        val radius = 5f + (idx % 3) * 3.5f
        val alpha = sin(p * PI.toFloat()).coerceIn(0f, 1f) * 0.52f
        drawCircle(
          color = Color.White.copy(alpha = alpha * 0.4f),
          radius = radius,
          center = Offset(cx, cy),
        )
        drawCircle(
          color = Color(0xFFE0F2FE).copy(alpha = alpha),
          radius = radius,
          center = Offset(cx, cy),
          style = Stroke(width = 1.6f),
        )
        drawCircle(
          color = Color.White.copy(alpha = alpha * 0.9f),
          radius = radius * 0.25f,
          center = Offset(cx - radius * 0.32f, cy - radius * 0.32f),
        )
      }
    }

    // Top badges row: HOT SWAP badge (left) & PROGRESSIVE TOP PRIZE plaque (right)
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Top,
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
          modifier =
            Modifier
              .graphicsLayer {
                val badgeScale = 0.98f + 0.04f * pulseAlpha
                scaleX = badgeScale
                scaleY = badgeScale
              }
              .clip(RoundedCornerShape(8.dp))
              .background(
                Brush.linearGradient(listOf(Color(0xFFEF4444), DuckBillOrange))
              )
              .border(1.5.dp, DuckGold.copy(alpha = pulseAlpha), RoundedCornerShape(8.dp))
              .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
          Text(
            text = "BUBBLE POP",
            fontFamily = LuckiestGuyFont,
            fontSize = 11.sp,
            color = DuckGold,
          )
        }

        AnimatedVisibility(visible = bonusSpinsRemaining > 0) {
          Box(
            modifier =
              Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(ArcadeGreenBright.copy(alpha = pulseAlpha))
                .border(1.5.dp, Color.White, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
          ) {
            Text(
              text = "BONUS SPINS: $bonusSpinsRemaining",
              fontFamily = ChakraPetchFont,
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp,
              color = Color.White,
            )
          }
        }
      }

      // Progressive Top Prize LED plaque
      Column(
        horizontalAlignment = Alignment.End,
        modifier =
          Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(BathDeepNavy.copy(alpha = 0.88f))
            .border(
              width = 1.5.dp,
              color = if (isProgressiveEligible) ArcadeGreenBright else BathTileCyan,
              shape = RoundedCornerShape(10.dp),
            )
            .padding(horizontal = 9.dp, vertical = 4.dp),
      ) {
        Text(
          text = if (isProgressiveEligible) "PROGRESSIVE ACTIVE" else "PROGRESSIVE ($2 MAX)",
          fontFamily = ChakraPetchFont,
          fontSize = 8.5.sp,
          fontWeight = FontWeight.Bold,
          color = if (isProgressiveEligible) ArcadeGreenBright else Color(0xFFBAE6FD),
        )
        Text(
          text = formatCents(progressiveCents),
          fontFamily = ChakraPetchFont,
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = DuckGold,
        )
      }
    }

    // Center-bottom 3D outlined "SUDSY DUCK REELS" title with warm shimmer
    Box(
      modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp),
      contentAlignment = Alignment.Center,
    ) {
      OutlinedMarqueeTitle(text = stringResource(id = R.string.marquee_title), pulseAlpha = pulseAlpha)
    }
  }
}

@Composable
private fun OutlinedMarqueeTitle(
  text: String,
  pulseAlpha: Float,
) {
  Box(
    contentAlignment = Alignment.Center,
    modifier =
      Modifier.graphicsLayer {
        val s = 0.99f + 0.025f * pulseAlpha
        scaleX = s
        scaleY = s
      },
  ) {
    // Drop shadow
    Text(
      text = text,
      fontFamily = LuckiestGuyFont,
      fontSize = 27.sp,
      textAlign = TextAlign.Center,
      color = Color.Black.copy(alpha = 0.78f),
      modifier = Modifier.offset(x = 2.dp, y = 3.dp),
    )
    // Thick navy stroke
    Text(
      text = text,
      style =
        TextStyle(
          fontFamily = LuckiestGuyFont,
          fontSize = 27.sp,
          textAlign = TextAlign.Center,
          color = BathDeepNavy,
          drawStyle = Stroke(width = 10f, join = StrokeJoin.Round),
        ),
    )
    // Bright golden yellow fill
    Text(
      text = text,
      style =
        TextStyle(
          fontFamily = LuckiestGuyFont,
          fontSize = 27.sp,
          textAlign = TextAlign.Center,
          brush =
            Brush.verticalGradient(
              listOf(Color(0xFFFFF59D), DuckGold, Color(0xFFF59E0B))
            ),
        ),
    )
  }
}
