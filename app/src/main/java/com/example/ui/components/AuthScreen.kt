package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Login
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.auth.attemptAutoSignIn
import com.example.auth.onGoogleSignInClicked
import com.example.model.SlotSymbol
import com.example.ui.theme.ArcadeGreen
import com.example.ui.theme.BathDeepNavy
import com.example.ui.theme.BathRoyalBlue
import com.example.ui.theme.BathTileCyan
import com.example.ui.theme.ChakraPetchFont
import com.example.ui.theme.DuckBillOrange
import com.example.ui.theme.DuckGold
import com.example.ui.theme.FredokaFont
import com.example.ui.theme.LuckiestGuyFont

@Composable
fun AuthScreen(
  onAuthSuccess: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember(context) { CredentialManager.create(context) }

  var isCheckingAutoSignIn by remember { mutableStateOf(true) }
  var isSigningIn by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(Unit) {
    attemptAutoSignIn(
      context = context,
      credentialManager = credentialManager,
      onAuthSuccess = {
        isCheckingAutoSignIn = false
        onAuthSuccess()
      },
      onUnauthenticated = { isCheckingAutoSignIn = false },
      scope = scope,
    )
  }

  Box(modifier = modifier.fillMaxSize().background(BathDeepNavy)) {
    Image(
      painter = painterResource(id = R.drawable.img_tile_backdrop_1794592673647),
      contentDescription = null,
      modifier = Modifier.fillMaxSize(),
      contentScale = ContentScale.Crop,
    )
    Box(
      modifier =
        Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              listOf(
                BathDeepNavy.copy(alpha = 0.55f),
                BathDeepNavy.copy(alpha = 0.72f),
                BathDeepNavy.copy(alpha = 0.88f),
              )
            )
          )
    )

    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .windowInsetsPadding(WindowInsets.systemBars)
          .padding(20.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Column(
        modifier =
          Modifier
            .fillMaxWidth()
            .widthIn(max = 420.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.verticalGradient(listOf(BathRoyalBlue, BathDeepNavy)))
            .border(
              width = 3.dp,
              brush = Brush.linearGradient(listOf(DuckGold, DuckBillOrange, BathTileCyan, DuckGold)),
              shape = RoundedCornerShape(24.dp),
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        Box(
          modifier =
            Modifier
              .size(96.dp)
              .clip(CircleShape)
              .background(
                Brush.radialGradient(
                  listOf(Color(0xFF7DD3FC), Color(0xFF0284C7), BathDeepNavy)
                )
              )
              .border(3.dp, DuckGold, CircleShape)
              .padding(8.dp),
          contentAlignment = Alignment.Center,
        ) {
          SymbolArtwork(symbol = SlotSymbol.DUCK, modifier = Modifier.fillMaxSize())
        }

        Text(
          text = "SUDSY DUCK REELS",
          fontFamily = LuckiestGuyFont,
          fontSize = 30.sp,
          color = DuckGold,
          textAlign = TextAlign.Center,
        )

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier =
            Modifier
              .clip(RoundedCornerShape(50))
              .background(Color(0xFF0A2540))
              .border(1.dp, BathTileCyan.copy(alpha = 0.7f), RoundedCornerShape(50))
              .padding(horizontal = 12.dp, vertical = 5.dp),
        ) {
          Icon(
            imageVector = Icons.Default.CloudDone,
            contentDescription = "Cloud Sync Enabled",
            tint = BathTileCyan,
            modifier = Modifier.size(16.dp),
          )
          Text(
            text = "CLOUD PROFILE & WIN LEDGER",
            fontFamily = ChakraPetchFont,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = Color(0xFFBAE6FD),
          )
        }

        Text(
          text = "Sign in with Google to save your virtual amusement credits, progressive prize meter, and biggest wins in the cloud.",
          fontFamily = FredokaFont,
          fontSize = 14.sp,
          color = Color(0xFFE2E8F0),
          textAlign = TextAlign.Center,
        )

        if (errorMessage != null) {
          Text(
            text = errorMessage ?: "",
            fontFamily = FredokaFont,
            fontSize = 12.sp,
            color = Color(0xFFFCA5A5),
            textAlign = TextAlign.Center,
            modifier =
              Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF7F1D1D).copy(alpha = 0.55f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
          )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Button(
          onClick = {
            errorMessage = null
            isSigningIn = true
            onGoogleSignInClicked(
              context = context,
              credentialManager = credentialManager,
              onAuthSuccess = {
                isSigningIn = false
                onAuthSuccess()
              },
              onAuthError = { msg ->
                isSigningIn = false
                errorMessage = msg
              },
              scope = scope,
              onAuthCancelled = {
                isSigningIn = false
              },
            )
          },
          enabled = !isSigningIn && !isCheckingAutoSignIn,
          colors = ButtonDefaults.buttonColors(containerColor = ArcadeGreen),
          shape = RoundedCornerShape(14.dp),
          modifier =
            Modifier
              .fillMaxWidth()
              .minimumInteractiveComponentSize()
              .height(54.dp)
              .testTag("google_sign_in_button"),
        ) {
          if (isSigningIn || isCheckingAutoSignIn) {
            CircularProgressIndicator(
              modifier = Modifier.size(22.dp),
              color = Color.White,
              strokeWidth = 2.5.dp,
            )
          } else {
            Icon(
              imageVector = Icons.Default.Login,
              contentDescription = "Sign in with Google",
              tint = Color.White,
              modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.size(10.dp))
            Text(
              text = "SIGN IN WITH GOOGLE",
              fontFamily = LuckiestGuyFont,
              fontSize = 17.sp,
              color = Color.White,
            )
          }
        }

        Text(
          text = "Virtual amusement simulator only • Credits have no cash value",
          fontFamily = FredokaFont,
          fontSize = 11.sp,
          color = Color(0xFF94A3B8),
          textAlign = TextAlign.Center,
        )
      }
    }
  }
}
