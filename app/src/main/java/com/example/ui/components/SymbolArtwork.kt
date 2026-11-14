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
    topLeft = Offset(w * 0.19f, h * 0.78f),
    size = Size(w * 0.68f, h * 0.13f),
  )

  // Seamless 3D vinyl rubber duck silhouette (head, plump breast, upturned tail — matching Banilla original)
  val duckSilhouette = Path().apply {
    // Start at front neck curve below bill
    moveTo(w * 0.28f, h * 0.43f)
    // Plump rounded chest sweeping down to belly
    cubicTo(w * 0.18f, h * 0.54f, w * 0.21f, h * 0.77f, w * 0.43f, h * 0.81f)
    // Belly across to rear base
    cubicTo(w * 0.68f, h * 0.83f, w * 0.86f, h * 0.70f, w * 0.89f, h * 0.49f)
    // Characteristic perky upturned rubber duck tail point
    cubicTo(w * 0.90f, h * 0.42f, w * 0.86f, h * 0.41f, w * 0.77f, h * 0.47f)
    // Concave back sweeping up into back of neck/head
    cubicTo(w * 0.69f, h * 0.52f, w * 0.63f, h * 0.46f, w * 0.63f, h * 0.34f)
    // Large round smooth head dome
    cubicTo(w * 0.65f, h * 0.14f, w * 0.46f, h * 0.08f, w * 0.33f, h * 0.15f)
    // Forehead down to upper bill base
    cubicTo(w * 0.23f, h * 0.20f, w * 0.23f, h * 0.33f, w * 0.28f, h * 0.43f)
    close()
  }

  val bodyBrush = Brush.radialGradient(
    colors = listOf(Color(0xFFFFFDE7), Color(0xFFFFEE32), Color(0xFFFFD600), Color(0xFFF59E0B), Color(0xFFD97706)),
    center = Offset(w * 0.47f, h * 0.35f),
    radius = w * 0.55f,
  )
  drawPath(duckSilhouette, brush = bodyBrush)
  // Soft warm amber vinyl rim (no harsh cartoon black stroke)
  drawPath(duckSilhouette, color = Color(0xFFB45309).copy(alpha = 0.48f), style = Stroke(w * 0.018f, join = StrokeJoin.Round))

  // Smooth glossy highlights on crown and breast
  drawOval(
    color = Color.White.copy(alpha = 0.65f),
    topLeft = Offset(w * 0.38f, h * 0.13f),
    size = Size(w * 0.16f, h * 0.07f),
  )
  drawOval(
    color = Color(0xFFFFF9C4).copy(alpha = 0.55f),
    topLeft = Offset(w * 0.26f, h * 0.51f),
    size = Size(w * 0.12f, h * 0.16f),
  )

  // Molded 3D side wing contour (soft warm shading instead of hard outline)
  val wingBrush = Brush.linearGradient(
    colors = listOf(Color(0xFFFFF59D), Color(0xFFFFD600), Color(0xFFE67E00)),
    start = Offset(w * 0.44f, h * 0.52f),
    end = Offset(w * 0.77f, h * 0.75f),
  )
  val wingPath = Path().apply {
    moveTo(w * 0.44f, h * 0.57f)
    cubicTo(w * 0.54f, h * 0.50f, w * 0.76f, h * 0.54f, w * 0.77f, h * 0.65f)
    cubicTo(w * 0.75f, h * 0.75f, w * 0.50f, h * 0.74f, w * 0.44f, h * 0.57f)
    close()
  }
  drawPath(wingPath, brush = wingBrush)
  drawPath(wingPath, color = Color(0xFFD97706).copy(alpha = 0.55f), style = Stroke(w * 0.016f, join = StrokeJoin.Round))

  // Signature Banilla pouty glossy crimson-red 3D duck bill
  val billBrush = Brush.verticalGradient(
    colors = listOf(Color(0xFFFF6E40), Color(0xFFEF2316), Color(0xFFB71C1C)),
    startY = h * 0.27f,
    endY = h * 0.44f,
  )
  val billPath = Path().apply {
    moveTo(w * 0.27f, h * 0.30f)
    cubicTo(w * 0.16f, h * 0.27f, w * 0.06f, h * 0.31f, w * 0.08f, h * 0.36f)
    cubicTo(w * 0.10f, h * 0.40f, w * 0.16f, h * 0.44f, w * 0.25f, h * 0.42f)
    cubicTo(w * 0.29f, h * 0.40f, w * 0.31f, h * 0.34f, w * 0.27f, h * 0.30f)
    close()
  }
  drawPath(billPath, brush = billBrush)
  drawPath(billPath, color = Color(0xFF7F1D1D).copy(alpha = 0.55f), style = Stroke(w * 0.015f, join = StrokeJoin.Round))
  // Pouty lip crease + bright specular sheen on upper bill
  drawLine(
    color = Color(0xFF7F1D1D).copy(alpha = 0.8f),
    start = Offset(w * 0.09f, h * 0.365f),
    end = Offset(w * 0.25f, h * 0.375f),
    strokeWidth = w * 0.014f,
    cap = StrokeCap.Round,
  )
  drawOval(
    color = Color.White.copy(alpha = 0.72f),
    topLeft = Offset(w * 0.12f, h * 0.30f),
    size = Size(w * 0.09f, h * 0.028f),
  )

  // Glossy black Banilla eye with subtle cheek shadow & double catchlight
  drawCircle(Color(0xFFD97706).copy(alpha = 0.32f), radius = w * 0.060f, center = Offset(w * 0.345f, h * 0.265f))
  drawCircle(Color(0xFF0B0F19), radius = w * 0.048f, center = Offset(w * 0.345f, h * 0.263f))
  drawCircle(Color.White, radius = w * 0.018f, center = Offset(w * 0.332f, h * 0.249f))
  drawCircle(Color.White.copy(alpha = 0.75f), radius = w * 0.008f, center = Offset(w * 0.358f, h * 0.274f))
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
  // Foamy white splash cushion matching maxresdefault screenshot
  drawOval(
    brush = Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.90f), Color(0xFF7DD3FC).copy(alpha = 0.75f))),
    topLeft = Offset(w * 0.08f, h * 0.64f),
    size = Size(w * 0.84f, h * 0.22f),
  )

  // Mint-green & white 3D cabin cruiser / toy boat angled in 3/4 view
  // Upper white wheelhouse cabin
  drawRoundRect(
    brush = Brush.verticalGradient(listOf(Color.White, Color(0xFFD1FAE5))),
    topLeft = Offset(w * 0.30f, h * 0.25f),
    size = Size(w * 0.40f, h * 0.25f),
    cornerRadius = CornerRadius(w * 0.08f, w * 0.08f),
  )
  // Mint roof & radar nub
  drawRoundRect(
    brush = Brush.horizontalGradient(listOf(Color(0xFFA7F3D0), Color(0xFF34D399), Color(0xFF059669))),
    topLeft = Offset(w * 0.28f, h * 0.22f),
    size = Size(w * 0.44f, h * 0.09f),
    cornerRadius = CornerRadius(w * 0.05f, w * 0.05f),
  )
  drawRoundRect(
    color = Color(0xFFE2E8F0),
    topLeft = Offset(w * 0.46f, h * 0.14f),
    size = Size(w * 0.08f, h * 0.09f),
    cornerRadius = CornerRadius(w * 0.02f, w * 0.02f),
  )
  // Tinted panoramic windshield
  drawRoundRect(
    brush = Brush.verticalGradient(listOf(Color(0xFF38BDF8), Color(0xFF0369A1))),
    topLeft = Offset(w * 0.34f, h * 0.33f),
    size = Size(w * 0.32f, h * 0.11f),
    cornerRadius = CornerRadius(w * 0.04f, w * 0.04f),
  )

  // Glossy mint-green boat hull
  val hull = Path().apply {
    moveTo(w * 0.11f, h * 0.48f)
    quadraticTo(w * 0.50f, h * 0.43f, w * 0.89f, h * 0.48f)
    lineTo(w * 0.77f, h * 0.74f)
    quadraticTo(w * 0.50f, h * 0.80f, w * 0.21f, h * 0.74f)
    close()
  }
  drawPath(
    hull,
    brush = Brush.verticalGradient(listOf(Color(0xFFA7F3D0), Color(0xFF34D399), Color(0xFF047857))),
  )
  drawPath(hull, color = Color(0xFF065F46).copy(alpha = 0.55f), style = Stroke(width = w * 0.020f, join = StrokeJoin.Round))
  // White stripe along hull
  drawLine(
    color = Color.White.copy(alpha = 0.85f),
    start = Offset(w * 0.17f, h * 0.55f),
    end = Offset(w * 0.83f, h * 0.55f),
    strokeWidth = w * 0.030f,
    cap = StrokeCap.Round,
  )
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
  val rindPath = Path().apply {
    moveTo(w * 0.10f, h * 0.34f)
    quadraticTo(w * 0.50f, h * 0.96f, w * 0.90f, h * 0.34f)
    close()
  }
  drawPath(rindPath, brush = Brush.verticalGradient(listOf(Color(0xFF22C55E), Color(0xFF14532D))))
  val whiteRind = Path().apply {
    moveTo(w * 0.14f, h * 0.35f)
    quadraticTo(w * 0.50f, h * 0.89f, w * 0.86f, h * 0.35f)
    close()
  }
  drawPath(whiteRind, color = Color(0xFFDCFCE7))
  val fleshPath = Path().apply {
    moveTo(w * 0.18f, h * 0.37f)
    quadraticTo(w * 0.50f, h * 0.84f, w * 0.82f, h * 0.37f)
    close()
  }
  drawPath(
    fleshPath,
    brush = Brush.verticalGradient(listOf(Color(0xFFF87171), Color(0xFFEF4444), Color(0xFF991B1B))),
  )
  val seedOffsets = listOf(0.35f to 0.46f, 0.50f to 0.51f, 0.65f to 0.46f, 0.42f to 0.59f, 0.58f to 0.59f)
  for ((sx, sy) in seedOffsets) {
    drawCircle(Color(0xFF111827), radius = w * 0.027f, center = Offset(w * sx, h * sy))
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
  drawOval(color = Color(0xFF22C55E), topLeft = Offset(w * 0.52f, h * 0.11f), size = Size(w * 0.22f, h * 0.12f))
  val orangeBrush = Brush.radialGradient(
    colors = listOf(Color(0xFFFEF08A), Color(0xFFFB923C), Color(0xFFEA580C), Color(0xFF9A3412)),
    center = Offset(w * 0.40f, h * 0.42f),
    radius = w * 0.40f,
  )
  drawCircle(brush = orangeBrush, radius = w * 0.32f, center = Offset(w * 0.50f, h * 0.54f))
  drawOval(
    color = Color.White.copy(alpha = 0.65f),
    topLeft = Offset(w * 0.30f, h * 0.34f),
    size = Size(w * 0.14f, h * 0.10f),
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
