package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SlotSymbol
import com.example.ui.theme.BarBlue
import com.example.ui.theme.BarGreen
import com.example.ui.theme.BarTan
import com.example.ui.theme.DuckBillOrange
import com.example.ui.theme.DuckGold
import com.example.ui.theme.LuckiestGuyFont

@Composable
fun SymbolArtwork(symbol: SlotSymbol, modifier: Modifier = Modifier) {
  val textMeasurer = rememberTextMeasurer()
  Box(modifier = modifier.padding(2.dp), contentAlignment = Alignment.Center) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      when (symbol) {
        SlotSymbol.DUCK -> drawRubberDuck()
        SlotSymbol.WILD -> drawWildBubble(textMeasurer)
        SlotSymbol.BOAT -> drawToyBoat()
        SlotSymbol.SOAP -> drawSoapBar(textMeasurer)
        SlotSymbol.SHAMPOO -> drawShampooBottle(textMeasurer)
        SlotSymbol.BELL -> drawGoldBell()
        SlotSymbol.TRIPLE_BAR -> drawBarPlaque(textMeasurer, 3, BarGreen)
        SlotSymbol.DOUBLE_BAR -> drawBarPlaque(textMeasurer, 2, BarBlue)
        SlotSymbol.SINGLE_BAR -> drawBarPlaque(textMeasurer, 1, BarTan)
        SlotSymbol.WATERMELON -> drawWatermelon()
        SlotSymbol.PLUM -> drawPlum()
        SlotSymbol.ORANGE -> drawOrange()
        SlotSymbol.CHERRY -> drawCherries()
      }
    }
  }
}

private fun DrawScope.drawRubberDuck() {
  val w = size.width
  val h = size.height

  // Soft tub water reflection shadow underneath
  drawOval(
    color = Color(0xFF082F49).copy(alpha = 0.28f),
    topLeft = Offset(w * 0.16f, h * 0.79f),
    size = Size(w * 0.70f, h * 0.13f),
  )

  // Seamless 3/4-right 3D vinyl body + upturned left tail + round dome head
  val bodyAndHead = Path().apply {
    // Start at front-right neck fold under cheek
    moveTo(w * 0.75f, h * 0.46f)
    // Broad plump chest sweeping down & forward-right
    cubicTo(w * 0.85f, h * 0.56f, w * 0.84f, h * 0.80f, w * 0.59f, h * 0.85f)
    // Smooth rounded belly sweeping back to the left
    cubicTo(w * 0.35f, h * 0.87f, w * 0.19f, h * 0.76f, w * 0.17f, h * 0.57f)
    // Upturned left duck tail tip
    cubicTo(w * 0.12f, h * 0.47f, w * 0.12f, h * 0.39f, w * 0.18f, h * 0.42f)
    cubicTo(w * 0.23f, h * 0.44f, w * 0.29f, h * 0.50f, w * 0.36f, h * 0.49f)
    // Sweep up back of neck into large spherical head dome
    cubicTo(w * 0.33f, h * 0.41f, w * 0.31f, h * 0.26f, w * 0.39f, h * 0.16f)
    cubicTo(w * 0.48f, h * 0.06f, w * 0.67f, h * 0.07f, w * 0.75f, h * 0.18f)
    // Right side of face down to cheek/neck junction
    cubicTo(w * 0.81f, h * 0.26f, w * 0.80f, h * 0.39f, w * 0.75f, h * 0.46f)
    close()
  }

  val bodyBrush = Brush.radialGradient(
    colors = listOf(
      Color(0xFFFFF59D),
      Color(0xFFFFEB00),
      Color(0xFFFFD000),
      Color(0xFFF59E0B),
      Color(0xFFD97706),
    ),
    center = Offset(w * 0.53f, h * 0.38f),
    radius = w * 0.56f,
  )
  drawPath(bodyAndHead, brush = bodyBrush)

  // Warm molded 3D chin/neck crease separating head sphere from plump chest
  val neckShadow = Path().apply {
    moveTo(w * 0.36f, h * 0.48f)
    cubicTo(w * 0.48f, h * 0.55f, w * 0.64f, h * 0.54f, w * 0.75f, h * 0.47f)
    cubicTo(w * 0.64f, h * 0.58f, w * 0.47f, h * 0.59f, w * 0.35f, h * 0.50f)
    close()
  }
  drawPath(
    neckShadow,
    brush = Brush.verticalGradient(
      colors = listOf(Color(0xFFB45309).copy(alpha = 0.55f), Color.Transparent),
      startY = h * 0.46f,
      endY = h * 0.58f,
    ),
  )

  // Molded left-side wing contour
  val wingPath = Path().apply {
    moveTo(w * 0.21f, h * 0.59f)
    cubicTo(w * 0.24f, h * 0.51f, w * 0.47f, h * 0.53f, w * 0.50f, h * 0.65f)
    cubicTo(w * 0.49f, h * 0.76f, w * 0.25f, h * 0.76f, w * 0.21f, h * 0.59f)
    close()
  }
  drawPath(
    wingPath,
    brush = Brush.linearGradient(
      colors = listOf(Color(0xFFFFF176), Color(0xFFFFD000), Color(0xFFE67E00)),
      start = Offset(w * 0.24f, h * 0.54f),
      end = Offset(w * 0.48f, h * 0.74f),
    ),
  )
  drawPath(
    wingPath,
    color = Color(0xFFD97706).copy(alpha = 0.48f),
    style = Stroke(width = w * 0.014f, join = StrokeJoin.Round),
  )

  // Subtle warm amber vinyl edge rim
  drawPath(
    bodyAndHead,
    color = Color(0xFFB45309).copy(alpha = 0.45f),
    style = Stroke(width = w * 0.017f, join = StrokeJoin.Round),
  )

  // Signature glossy white crown spot & chest vinyl sheen
  drawOval(
    color = Color.White.copy(alpha = 0.85f),
    topLeft = Offset(w * 0.45f, h * 0.105f),
    size = Size(w * 0.12f, h * 0.055f),
  )
  drawOval(
    color = Color(0xFFFFFDE7).copy(alpha = 0.52f),
    topLeft = Offset(w * 0.54f, h * 0.57f),
    size = Size(w * 0.16f, h * 0.13f),
  )

  // 3/4-right main glossy dark eye (large dark oval + espresso iris rim + bright white dot)
  val eyeCx = w * 0.525f
  val eyeCy = h * 0.295f
  drawOval(
    color = Color(0xFFB45309).copy(alpha = 0.28f),
    topLeft = Offset(eyeCx - w * 0.062f, eyeCy - h * 0.078f),
    size = Size(w * 0.124f, h * 0.156f),
  )
  drawOval(
    color = Color(0xFF1F1610),
    topLeft = Offset(eyeCx - w * 0.052f, eyeCy - h * 0.068f),
    size = Size(w * 0.104f, h * 0.136f),
  )
  drawOval(
    color = Color(0xFF050505),
    topLeft = Offset(eyeCx - w * 0.038f, eyeCy - h * 0.052f),
    size = Size(w * 0.076f, h * 0.104f),
  )
  drawCircle(
    color = Color.White,
    radius = w * 0.022f,
    center = Offset(eyeCx + w * 0.010f, eyeCy - h * 0.008f),
  )

  // Far-right peeking eye sliver along the 3/4 head contour
  drawOval(
    color = Color(0xFF1A120B),
    topLeft = Offset(w * 0.745f, h * 0.235f),
    size = Size(w * 0.036f, h * 0.090f),
  )
  drawCircle(
    color = Color.White.copy(alpha = 0.9f),
    radius = w * 0.009f,
    center = Offset(w * 0.766f, h * 0.275f),
  )

  // Iconic wide crimson-red 3D sculpted pouty duck bill (protruding right-forward)
  val lowerLipPath = Path().apply {
    moveTo(w * 0.62f, h * 0.42f)
    cubicTo(w * 0.67f, h * 0.51f, w * 0.80f, h * 0.53f, w * 0.87f, h * 0.44f)
    cubicTo(w * 0.89f, h * 0.40f, w * 0.85f, h * 0.37f, w * 0.77f, h * 0.39f)
    close()
  }
  drawPath(
    lowerLipPath,
    brush = Brush.verticalGradient(
      colors = listOf(Color(0xFF991B1B), Color(0xFFDC2626), Color(0xFF7F1D1D)),
      startY = h * 0.39f,
      endY = h * 0.53f,
    ),
  )

  val upperBillPath = Path().apply {
    // High rounded bridge between the eyes
    moveTo(w * 0.67f, h * 0.305f)
    cubicTo(w * 0.72f, h * 0.295f, w * 0.76f, h * 0.335f, w * 0.84f, h * 0.355f)
    // Flared wide front bill tip curving right
    cubicTo(w * 0.91f, h * 0.375f, w * 0.91f, h * 0.425f, w * 0.82f, h * 0.440f)
    // Sculpted smile arch curving down-left into wide red cheek corner
    cubicTo(w * 0.73f, h * 0.455f, w * 0.63f, h * 0.485f, w * 0.56f, h * 0.445f)
    cubicTo(w * 0.53f, h * 0.415f, w * 0.62f, h * 0.375f, w * 0.67f, h * 0.305f)
    close()
  }
  drawPath(
    upperBillPath,
    brush = Brush.linearGradient(
      colors = listOf(Color(0xFFEF2316), Color(0xFFFF4D35), Color(0xFFB91C1C)),
      start = Offset(w * 0.64f, h * 0.31f),
      end = Offset(w * 0.86f, h * 0.48f),
    ),
  )
  drawPath(
    upperBillPath,
    color = Color(0xFF7F1D1D).copy(alpha = 0.6f),
    style = Stroke(width = w * 0.013f, join = StrokeJoin.Round),
  )

  // Deep pouty mouth recess shadow + glossy highlight along upper bill bridge
  val mouthCrease = Path().apply {
    moveTo(w * 0.68f, h * 0.445f)
    cubicTo(w * 0.75f, h * 0.435f, w * 0.82f, h * 0.445f, w * 0.865f, h * 0.415f)
  }
  drawPath(
    mouthCrease,
    color = Color(0xFF450A0A).copy(alpha = 0.88f),
    style = Stroke(width = w * 0.018f, cap = StrokeCap.Round),
  )
  drawOval(
    color = Color.White.copy(alpha = 0.55f),
    topLeft = Offset(w * 0.745f, h * 0.342f),
    size = Size(w * 0.085f, h * 0.026f),
  )
}

private fun DrawScope.drawWildBubble(textMeasurer: TextMeasurer) {
  val w = size.width
  val h = size.height
  val r = minOf(w, h) * 0.44f
  val center = Offset(w / 2f, h / 2f)

  drawCircle(Color(0xFF082F49).copy(alpha = 0.32f), radius = r, center = center + Offset(2f, 4f))

  val bubbleBrush = Brush.radialGradient(
    colors = listOf(Color(0xFFFFFFFF), Color(0xFF67E8F9), Color(0xFF0284C7), Color(0xFF1E3A8A)),
    center = Offset(center.x - r * 0.28f, center.y - r * 0.28f),
    radius = r * 1.35f,
  )
  drawCircle(brush = bubbleBrush, radius = r, center = center)
  drawCircle(color = Color.White, radius = r, center = center, style = Stroke(width = w * 0.042f))
  drawCircle(color = Color(0xFF0C4A6E).copy(alpha = 0.65f), radius = r * 1.01f, center = center, style = Stroke(width = w * 0.015f))

  drawOval(
    color = Color.White.copy(alpha = 0.85f),
    topLeft = Offset(center.x - r * 0.56f, center.y - r * 0.62f),
    size = Size(r * 0.46f, r * 0.26f),
  )
  drawCircle(
    color = Color(0xFFF472B6).copy(alpha = 0.45f),
    radius = r * 0.16f,
    center = Offset(center.x + r * 0.45f, center.y + r * 0.42f),
  )

  val label = "WILD"
  val fontSize = (minOf(w, h) * 0.25f).toSp()
  val shadowMeasured =
    textMeasurer.measure(
      text = label,
      style = TextStyle(fontFamily = LuckiestGuyFont, fontSize = fontSize, color = Color(0xFF082F49)),
    )
  val topLeft =
    Offset((w - shadowMeasured.size.width) / 2f, (h - shadowMeasured.size.height) / 2f + h * 0.03f)
  drawText(shadowMeasured, topLeft = topLeft + Offset(2.5f, 3f))
  val mainMeasured =
    textMeasurer.measure(
      text = label,
      style =
        TextStyle(
          fontFamily = LuckiestGuyFont,
          fontSize = fontSize,
          brush = Brush.verticalGradient(listOf(Color(0xFFFFF59D), DuckGold, DuckBillOrange)),
        ),
    )
  drawText(mainMeasured, topLeft = topLeft)
}

private fun DrawScope.drawToyBoat() {
  val w = size.width
  val h = size.height
  // Blue & white frothy tub splash beneath boat (matching Superior Skill Lightning screenshot)
  drawOval(
    brush = Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.92f), Color(0xFF38BDF8).copy(alpha = 0.85f))),
    topLeft = Offset(w * 0.08f, h * 0.65f),
    size = Size(w * 0.84f, h * 0.22f),
  )

  // Smokestack with red cap
  drawRoundRect(
    brush = Brush.horizontalGradient(listOf(Color(0xFFFEF08A), DuckGold, Color(0xFFD97706))),
    topLeft = Offset(w * 0.44f, h * 0.13f),
    size = Size(w * 0.12f, h * 0.15f),
    cornerRadius = CornerRadius(w * 0.03f, w * 0.03f),
  )
  drawRoundRect(
    color = Color(0xFFDC2626),
    topLeft = Offset(w * 0.43f, h * 0.12f),
    size = Size(w * 0.14f, h * 0.055f),
    cornerRadius = CornerRadius(w * 0.025f, w * 0.025f),
  )

  // Sunny yellow upper cabin with crimson red roof trim & blue panoramic windows
  drawRoundRect(
    brush = Brush.verticalGradient(listOf(Color(0xFFFFF59D), DuckGold, Color(0xFFF59E0B))),
    topLeft = Offset(w * 0.29f, h * 0.26f),
    size = Size(w * 0.42f, h * 0.24f),
    cornerRadius = CornerRadius(w * 0.07f, w * 0.07f),
  )
  drawRoundRect(
    brush = Brush.horizontalGradient(listOf(Color(0xFFEF4444), Color(0xFFB91C1C))),
    topLeft = Offset(w * 0.26f, h * 0.23f),
    size = Size(w * 0.48f, h * 0.07f),
    cornerRadius = CornerRadius(w * 0.04f, w * 0.04f),
  )
  // Blue-tinted cabin windows
  drawRoundRect(
    brush = Brush.verticalGradient(listOf(Color(0xFF7DD3FC), Color(0xFF0284C7))),
    topLeft = Offset(w * 0.34f, h * 0.33f),
    size = Size(w * 0.32f, h * 0.10f),
    cornerRadius = CornerRadius(w * 0.03f, w * 0.03f),
  )

  // Golden-yellow 3D boat hull with crimson waterline trim
  val hull = Path().apply {
    moveTo(w * 0.10f, h * 0.48f)
    quadraticTo(w * 0.50f, h * 0.43f, w * 0.90f, h * 0.48f)
    lineTo(w * 0.78f, h * 0.74f)
    quadraticTo(w * 0.50f, h * 0.80f, w * 0.20f, h * 0.74f)
    close()
  }
  drawPath(
    hull,
    brush = Brush.verticalGradient(listOf(Color(0xFFFFFDE7), Color(0xFFFFD600), Color(0xFFD97706))),
  )
  drawPath(hull, color = Color(0xFF92400E).copy(alpha = 0.50f), style = Stroke(width = w * 0.018f, join = StrokeJoin.Round))

  // Bold red deck stripe & round portholes
  drawLine(
    color = Color(0xFFDC2626),
    start = Offset(w * 0.15f, h * 0.54f),
    end = Offset(w * 0.85f, h * 0.54f),
    strokeWidth = w * 0.042f,
    cap = StrokeCap.Round,
  )
  drawCircle(Color(0xFF0284C7), radius = w * 0.042f, center = Offset(w * 0.37f, h * 0.63f))
  drawCircle(Color(0xFF0284C7), radius = w * 0.042f, center = Offset(w * 0.63f, h * 0.63f))
  drawCircle(Color.White.copy(alpha = 0.75f), radius = w * 0.015f, center = Offset(w * 0.36f, h * 0.615f))
  drawCircle(Color.White.copy(alpha = 0.75f), radius = w * 0.015f, center = Offset(w * 0.62f, h * 0.615f))
}

private fun DrawScope.drawSoapBar(textMeasurer: TextMeasurer) {
  val w = size.width
  val h = size.height
  drawRoundRect(
    color = Color(0xFF082F49).copy(alpha = 0.32f),
    topLeft = Offset(w * 0.13f, h * 0.27f),
    size = Size(w * 0.76f, h * 0.53f),
    cornerRadius = CornerRadius(w * 0.22f, w * 0.22f),
  )
  val soapBrush = Brush.linearGradient(
    colors = listOf(Color(0xFFE0F2FE), Color(0xFF7DD3FC), Color(0xFF0284C7)),
    start = Offset(w * 0.1f, h * 0.2f),
    end = Offset(w * 0.9f, h * 0.8f),
  )
  drawRoundRect(
    brush = soapBrush,
    topLeft = Offset(w * 0.11f, h * 0.23f),
    size = Size(w * 0.78f, h * 0.54f),
    cornerRadius = CornerRadius(w * 0.22f, w * 0.22f),
  )
  drawRoundRect(
    color = Color(0xFF0369A1).copy(alpha = 0.65f),
    topLeft = Offset(w * 0.11f, h * 0.23f),
    size = Size(w * 0.78f, h * 0.54f),
    cornerRadius = CornerRadius(w * 0.22f, w * 0.22f),
    style = Stroke(width = w * 0.022f),
  )
  drawRoundRect(
    color = Color.White.copy(alpha = 0.72f),
    topLeft = Offset(w * 0.19f, h * 0.31f),
    size = Size(w * 0.62f, h * 0.38f),
    cornerRadius = CornerRadius(w * 0.15f, w * 0.15f),
    style = Stroke(width = w * 0.020f),
  )
  val measured =
    textMeasurer.measure(
      text = "SOAP",
      style = TextStyle(
        fontFamily = LuckiestGuyFont,
        fontSize = (minOf(w, h) * 0.22f).toSp(),
        color = Color(0xFF0369A1),
      ),
    )
  val pos = Offset((w - measured.size.width) / 2f, (h - measured.size.height) / 2f + h * 0.02f)
  drawText(
    textMeasurer.measure(
      text = "SOAP",
      style = TextStyle(fontFamily = LuckiestGuyFont, fontSize = (minOf(w, h) * 0.22f).toSp(), color = Color.White),
    ),
    topLeft = pos + Offset(1.5f, 1.5f),
  )
  drawText(measured, topLeft = pos)

  drawCircle(Color.White.copy(alpha = 0.90f), radius = w * 0.08f, center = Offset(w * 0.18f, h * 0.22f))
  drawCircle(Color.White.copy(alpha = 0.90f), radius = w * 0.09f, center = Offset(w * 0.80f, h * 0.75f))
  drawCircle(Color.White.copy(alpha = 0.85f), radius = w * 0.055f, center = Offset(w * 0.71f, h * 0.79f))
}

private fun DrawScope.drawShampooBottle(textMeasurer: TextMeasurer) {
  val w = size.width
  val h = size.height
  // Soft shadow
  drawRoundRect(
    color = Color(0xFF082F49).copy(alpha = 0.30f),
    topLeft = Offset(w * 0.24f, h * 0.24f),
    size = Size(w * 0.56f, h * 0.68f),
    cornerRadius = CornerRadius(w * 0.18f, w * 0.18f),
  )
  // Royal-blue ribbed flip cap (matching LUCKY bottle in NCG_II logo!)
  drawRoundRect(
    brush = Brush.verticalGradient(listOf(Color(0xFF60A5FA), Color(0xFF2563EB), Color(0xFF1E3A8A))),
    topLeft = Offset(w * 0.35f, h * 0.08f),
    size = Size(w * 0.30f, h * 0.16f),
    cornerRadius = CornerRadius(w * 0.05f, w * 0.05f),
  )
  drawRoundRect(
    color = Color(0xFF1E3A8A),
    topLeft = Offset(w * 0.35f, h * 0.08f),
    size = Size(w * 0.30f, h * 0.16f),
    cornerRadius = CornerRadius(w * 0.05f, w * 0.05f),
    style = Stroke(w * 0.016f),
  )

  // Smooth glossy 3D orange bottle
  val bottleBrush = Brush.horizontalGradient(
    listOf(Color(0xFFFFCC80), Color(0xFFF97316), Color(0xFFEA580C), Color(0xFFC2410C)),
  )
  drawRoundRect(
    brush = bottleBrush,
    topLeft = Offset(w * 0.21f, h * 0.22f),
    size = Size(w * 0.58f, h * 0.67f),
    cornerRadius = CornerRadius(w * 0.18f, w * 0.18f),
  )
  drawRoundRect(
    color = Color(0xFF9A3412).copy(alpha = 0.55f),
    topLeft = Offset(w * 0.21f, h * 0.22f),
    size = Size(w * 0.58f, h * 0.67f),
    cornerRadius = CornerRadius(w * 0.18f, w * 0.18f),
    style = Stroke(width = w * 0.020f),
  )

  // Signature purple & gold "LUCKY" curved label
  drawOval(
    brush = Brush.verticalGradient(listOf(Color(0xFF7E22CE), Color(0xFF4C1D95))),
    topLeft = Offset(w * 0.25f, h * 0.37f),
    size = Size(w * 0.50f, h * 0.39f),
  )
  drawOval(
    color = DuckGold,
    topLeft = Offset(w * 0.25f, h * 0.37f),
    size = Size(w * 0.50f, h * 0.39f),
    style = Stroke(width = w * 0.024f),
  )
  val measured =
    textMeasurer.measure(
      text = "LUCKY",
      style = TextStyle(
        fontFamily = LuckiestGuyFont,
        fontSize = (minOf(w, h) * 0.145f).toSp(),
        color = DuckGold,
      ),
    )
  drawText(
    measured,
    topLeft = Offset((w - measured.size.width) / 2f, h * 0.49f - measured.size.height / 2f),
  )
  // Tiny duck silhouette accent on label
  drawCircle(DuckGold, radius = w * 0.048f, center = Offset(w * 0.50f, h * 0.64f))
  drawLine(
    color = Color.White.copy(alpha = 0.58f),
    start = Offset(w * 0.28f, h * 0.29f),
    end = Offset(w * 0.28f, h * 0.78f),
    strokeWidth = w * 0.034f,
    cap = StrokeCap.Round,
  )
}

private fun DrawScope.drawGoldBell() {
  val w = size.width
  val h = size.height
  // Tilted diagonal 3D polished gold bell with bronze mouth cavity & clapper (matching both Banilla screenshots!)
  rotate(degrees = -22f, pivot = Offset(w * 0.50f, h * 0.50f)) {
    val bellBrush = Brush.linearGradient(
      colors = listOf(Color(0xFFFFFDE7), Color(0xFFFFEB3B), DuckGold, Color(0xFFD97706)),
      start = Offset(w * 0.22f, h * 0.16f),
      end = Offset(w * 0.84f, h * 0.72f),
    )
    // Crown ring
    drawCircle(
      color = Color(0xFFD97706),
      radius = w * 0.06f,
      center = Offset(w * 0.50f, h * 0.13f),
      style = Stroke(width = w * 0.026f),
    )
    // Bell dome & flared rim
    val bellPath = Path().apply {
      moveTo(w * 0.50f, h * 0.15f)
      cubicTo(w * 0.26f, h * 0.18f, w * 0.27f, h * 0.52f, w * 0.15f, h * 0.69f)
      quadraticTo(w * 0.50f, h * 0.79f, w * 0.85f, h * 0.69f)
      cubicTo(w * 0.73f, h * 0.52f, w * 0.74f, h * 0.18f, w * 0.50f, h * 0.15f)
      close()
    }
    drawPath(bellPath, brush = bellBrush)
    drawPath(bellPath, color = Color(0xFF92400E).copy(alpha = 0.55f), style = Stroke(width = w * 0.020f, join = StrokeJoin.Round))

    // Dark bronze inner mouth oval + shiny gold clapper ball
    drawOval(
      brush = Brush.verticalGradient(listOf(Color(0xFF451A03), Color(0xFF9A3412))),
      topLeft = Offset(w * 0.22f, h * 0.63f),
      size = Size(w * 0.56f, h * 0.14f),
    )
    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(Color(0xFFFFFDE7), DuckGold, Color(0xFFB45309)),
        center = Offset(w * 0.48f, h * 0.70f),
        radius = w * 0.11f,
      ),
      radius = w * 0.085f,
      center = Offset(w * 0.50f, h * 0.73f),
    )

    // Bright specular crescent along left shoulder
    drawLine(
      color = Color.White.copy(alpha = 0.88f),
      start = Offset(w * 0.40f, h * 0.24f),
      end = Offset(w * 0.28f, h * 0.58f),
      strokeWidth = w * 0.048f,
      cap = StrokeCap.Round,
    )
  }
}

private fun DrawScope.drawBarPlaque(textMeasurer: TextMeasurer, count: Int, baseColor: Color) {
  val w = size.width
  val h = size.height
  val barHeight = h * 0.245f
  val gap = h * 0.042f
  val totalHeight = count * barHeight + (count - 1) * gap
  val startY = (h - totalHeight) / 2f
  val lightColor = baseColor.copy(
    red = (baseColor.red + 0.34f).coerceAtMost(1f),
    green = (baseColor.green + 0.34f).coerceAtMost(1f),
    blue = (baseColor.blue + 0.34f).coerceAtMost(1f),
  )
  val darkColor = baseColor.copy(
    red = (baseColor.red * 0.45f),
    green = (baseColor.green * 0.45f),
    blue = (baseColor.blue * 0.45f),
  )
  val barBrush = Brush.verticalGradient(listOf(lightColor, baseColor, darkColor))

  for (i in 0 until count) {
    val top = startY + i * (barHeight + gap)
    // Soft drop shadow
    drawRoundRect(
      color = Color(0xFF082F49).copy(alpha = 0.38f),
      topLeft = Offset(w * 0.08f, top + 2.5f),
      size = Size(w * 0.84f, barHeight),
      cornerRadius = CornerRadius(w * 0.05f, w * 0.05f),
    )
    drawRoundRect(
      brush = barBrush,
      topLeft = Offset(w * 0.07f, top),
      size = Size(w * 0.86f, barHeight),
      cornerRadius = CornerRadius(w * 0.05f, w * 0.05f),
    )
    // Frosted glass upper sheen matching Banilla BAR tiles
    drawRoundRect(
      color = Color.White.copy(alpha = 0.32f),
      topLeft = Offset(w * 0.09f, top + 1.5f),
      size = Size(w * 0.82f, barHeight * 0.40f),
      cornerRadius = CornerRadius(w * 0.035f, w * 0.035f),
    )
    drawRoundRect(
      color = Color.White.copy(alpha = 0.90f),
      topLeft = Offset(w * 0.07f, top),
      size = Size(w * 0.86f, barHeight),
      cornerRadius = CornerRadius(w * 0.05f, w * 0.05f),
      style = Stroke(width = w * 0.022f),
    )
    val measured =
      textMeasurer.measure(
        text = "BAR",
        style = TextStyle(
          fontFamily = LuckiestGuyFont,
          fontSize = (barHeight * 0.72f).toSp(),
          fontWeight = FontWeight.Bold,
          color = Color.White,
        ),
      )
    val textPos = Offset((w - measured.size.width) / 2f, top + (barHeight - measured.size.height) / 2f + 1f)
    drawText(measured, topLeft = textPos)
  }
}

private fun DrawScope.drawWatermelon() {
  val w = size.width
  val h = size.height
  // Tilted 3D watermelon wedge slice (matching Reel 4 Row 1 of Superior Skill Lightning screenshot)
  rotate(degrees = -16f, pivot = Offset(w * 0.50f, h * 0.52f)) {
    // 3D green striped outer rind belly
    val rindPath = Path().apply {
      moveTo(w * 0.11f, h * 0.58f)
      quadraticTo(w * 0.50f, h * 0.94f, w * 0.89f, h * 0.58f)
      quadraticTo(w * 0.50f, h * 0.76f, w * 0.11f, h * 0.58f)
      close()
    }
    drawPath(rindPath, brush = Brush.verticalGradient(listOf(Color(0xFF22C55E), Color(0xFF14532D))))
    // Pale green inner rind edge
    val innerRind = Path().apply {
      moveTo(w * 0.12f, h * 0.56f)
      quadraticTo(w * 0.50f, h * 0.76f, w * 0.88f, h * 0.56f)
      lineTo(w * 0.85f, h * 0.52f)
      quadraticTo(w * 0.50f, h * 0.69f, w * 0.15f, h * 0.52f)
      close()
    }
    drawPath(innerRind, color = Color(0xFFDCFCE7))

    // Juicy triangular red wedge face
    val fleshPath = Path().apply {
      moveTo(w * 0.50f, h * 0.16f)
      lineTo(w * 0.15f, h * 0.53f)
      quadraticTo(w * 0.50f, h * 0.70f, w * 0.85f, h * 0.53f)
      close()
    }
    drawPath(
      fleshPath,
      brush = Brush.verticalGradient(listOf(Color(0xFFFB7185), Color(0xFFEF4444), Color(0xFFB91C1C))),
    )
    // Glossy edge shine along slice cut
    drawLine(
      color = Color.White.copy(alpha = 0.45f),
      start = Offset(w * 0.50f, h * 0.19f),
      end = Offset(w * 0.21f, h * 0.51f),
      strokeWidth = w * 0.025f,
      cap = StrokeCap.Round,
    )
    val seedOffsets = listOf(0.42f to 0.42f, 0.57f to 0.42f, 0.34f to 0.53f, 0.50f to 0.52f, 0.66f to 0.53f)
    for ((sx, sy) in seedOffsets) {
      drawOval(
        color = Color(0xFF111827),
        topLeft = Offset(w * sx - w * 0.020f, h * sy - h * 0.032f),
        size = Size(w * 0.040f, h * 0.064f),
      )
    }
  }
}

private fun DrawScope.drawPlum() {
  val w = size.width
  val h = size.height
  // Smooth glossy 3D purple plum matching NCG_II & maxresdefault screenshots (no green stem/leaf!)
  drawOval(
    color = Color(0xFF082F49).copy(alpha = 0.32f),
    topLeft = Offset(w * 0.21f, h * 0.23f),
    size = Size(w * 0.62f, h * 0.64f),
  )
  val plumBrush = Brush.radialGradient(
    colors = listOf(Color(0xFFF3E8FF), Color(0xFFC084FC), Color(0xFF7E22CE), Color(0xFF4C1D95), Color(0xFF2E1065)),
    center = Offset(w * 0.38f, h * 0.36f),
    radius = w * 0.44f,
  )
  drawOval(brush = plumBrush, topLeft = Offset(w * 0.18f, h * 0.18f), size = Size(w * 0.64f, h * 0.66f))
  // Characteristic plum cleft crease at top-center
  val crease = Path().apply {
    moveTo(w * 0.50f, h * 0.19f)
    quadraticTo(w * 0.45f, h * 0.42f, w * 0.50f, h * 0.72f)
  }
  drawPath(crease, color = Color(0xFF2E1065).copy(alpha = 0.45f), style = Stroke(w * 0.024f, cap = StrokeCap.Round))
  // Soft specular highlight
  drawOval(
    color = Color.White.copy(alpha = 0.62f),
    topLeft = Offset(w * 0.27f, h * 0.27f),
    size = Size(w * 0.14f, h * 0.19f),
  )
}

private fun DrawScope.drawOrange() {
  val w = size.width
  val h = size.height
  // Glossy 3D navel orange matching Reels 1-3 of Superior Skill Lightning screenshot (no oversized green leaf)
  drawCircle(
    color = Color(0xFF082F49).copy(alpha = 0.30f),
    radius = w * 0.34f,
    center = Offset(w * 0.52f, h * 0.55f),
  )
  val orangeBrush = Brush.radialGradient(
    colors = listOf(Color(0xFFFEF08A), Color(0xFFFB923C), Color(0xFFEA580C), Color(0xFF9A3412)),
    center = Offset(w * 0.39f, h * 0.39f),
    radius = w * 0.46f,
  )
  drawCircle(brush = orangeBrush, radius = w * 0.35f, center = Offset(w * 0.50f, h * 0.51f))
  // Tiny top green-brown calyx navel dimple
  drawCircle(Color(0xFF3F6212), radius = w * 0.045f, center = Offset(w * 0.50f, h * 0.21f))
  drawCircle(Color(0xFF1A2E05), radius = w * 0.020f, center = Offset(w * 0.50f, h * 0.21f))
  // Subtle peel stipple dimples
  val dimples = listOf(0.36f to 0.54f, 0.44f to 0.66f, 0.60f to 0.44f, 0.66f to 0.58f, 0.54f to 0.69f)
  for ((dx, dy) in dimples) {
    drawCircle(Color(0xFF9A3412).copy(alpha = 0.25f), radius = w * 0.014f, center = Offset(w * dx, h * dy))
  }
  drawOval(
    color = Color.White.copy(alpha = 0.72f),
    topLeft = Offset(w * 0.28f, h * 0.28f),
    size = Size(w * 0.16f, h * 0.11f),
  )
}

private fun DrawScope.drawCherries() {
  val w = size.width
  val h = size.height
  // Lush green twin leaves + three curved stems matching NCG_II & maxresdefault screenshots!
  val leftLeaf = Path().apply {
    moveTo(w * 0.50f, h * 0.17f)
    quadraticTo(w * 0.30f, h * 0.08f, w * 0.21f, h * 0.26f)
    quadraticTo(w * 0.37f, h * 0.28f, w * 0.50f, h * 0.17f)
    close()
  }
  val rightLeaf = Path().apply {
    moveTo(w * 0.50f, h * 0.17f)
    quadraticTo(w * 0.72f, h * 0.07f, w * 0.80f, h * 0.25f)
    quadraticTo(w * 0.64f, h * 0.28f, w * 0.50f, h * 0.17f)
    close()
  }
  drawPath(leftLeaf, brush = Brush.linearGradient(listOf(Color(0xFF86EFAC), Color(0xFF16A34A))))
  drawPath(rightLeaf, brush = Brush.linearGradient(listOf(Color(0xFF86EFAC), Color(0xFF15803D))))

  // Curved green stems to 3 cherries
  drawLine(Color(0xFF4ADE80), Offset(w * 0.31f, h * 0.56f), Offset(w * 0.50f, h * 0.16f), strokeWidth = w * 0.036f, cap = StrokeCap.Round)
  drawLine(Color(0xFF22C55E), Offset(w * 0.53f, h * 0.49f), Offset(w * 0.50f, h * 0.16f), strokeWidth = w * 0.036f, cap = StrokeCap.Round)
  drawLine(Color(0xFF16A34A), Offset(w * 0.69f, h * 0.60f), Offset(w * 0.50f, h * 0.16f), strokeWidth = w * 0.036f, cap = StrokeCap.Round)

  // Back-center cherry
  val backCherry = Brush.radialGradient(
    colors = listOf(Color(0xFFFCA5A5), Color(0xFFDC2626), Color(0xFF7F1D1D)),
    center = Offset(w * 0.49f, h * 0.51f),
    radius = w * 0.18f,
  )
  drawCircle(backCherry, radius = w * 0.155f, center = Offset(w * 0.53f, h * 0.56f))

  // Left cherry
  val leftCherry = Brush.radialGradient(
    colors = listOf(Color(0xFFFEE2E2), Color(0xFFEF4444), Color(0xFF991B1B)),
    center = Offset(w * 0.26f, h * 0.59f),
    radius = w * 0.21f,
  )
  drawCircle(leftCherry, radius = w * 0.17f, center = Offset(w * 0.30f, h * 0.65f))
  drawCircle(Color.White.copy(alpha = 0.85f), radius = w * 0.038f, center = Offset(w * 0.25f, h * 0.58f))

  // Right foreground cherry
  val rightCherry = Brush.radialGradient(
    colors = listOf(Color(0xFFFEE2E2), Color(0xFFEF4444), Color(0xFF991B1B)),
    center = Offset(w * 0.62f, h * 0.64f),
    radius = w * 0.21f,
  )
  drawCircle(rightCherry, radius = w * 0.17f, center = Offset(w * 0.68f, h * 0.69f))
  drawCircle(Color.White.copy(alpha = 0.85f), radius = w * 0.038f, center = Offset(w * 0.62f, h * 0.62f))
}
