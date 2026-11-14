package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundEffectManager
import com.example.data.PreferencesRepository
import com.example.model.BonusCelebration
import com.example.model.BonusType
import com.example.model.GameState
import com.example.model.HotSwapSpot
import com.example.model.Paylines
import com.example.model.ShampooPickBonus
import com.example.model.ShampooPickTile
import com.example.model.SkillPrompt
import com.example.model.SlotSymbol
import com.example.model.formatCents
import kotlin.random.Random
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BathtimeViewModel(application: Application) : AndroidViewModel(application) {
  private val prefs = PreferencesRepository(application.applicationContext)
  val soundManager = SoundEffectManager(application.applicationContext)

  private val _state = MutableStateFlow(GameState())
  val state: StateFlow<GameState> = _state.asStateFlow()

  private var spinJob: Job? = null
  private var countUpJob: Job? = null
  private var celebrationJob: Job? = null
  private var pendingShampooPicks: Int = 0
  @Volatile private var fastStopRequested = false

  init {
    viewModelScope.launch {
      val saved = prefs.arcadeDataFlow.first()
      _state.update {
        it.copy(
          balanceCents = saved.balanceCents,
          wagerCents =
            if (saved.wagerCents in GameState.WAGER_OPTIONS_CENTS) saved.wagerCents else 100L,
          progressiveCents = saved.progressiveCents,
          soundEnabled = saved.soundEnabled,
          hapticsEnabled = saved.hapticsEnabled,
          skillModeEnabled = saved.skillModeEnabled,
          totalSpins = saved.totalSpins,
          totalWonCents = saved.totalWonCents,
          biggestWinCents = saved.biggestWinCents,
        )
      }
    }
  }

  fun onStepWager(direction: Int) {
    val cur = _state.value
    if (cur.isSpinning || cur.bonusSpinsRemaining > 0 || cur.shampooPickBonus != null) return
    val idx = GameState.WAGER_OPTIONS_CENTS.indexOf(cur.wagerCents).coerceAtLeast(0)
    val nextIdx = (idx + direction).coerceIn(0, GameState.WAGER_OPTIONS_CENTS.lastIndex)
    val nextWager = GameState.WAGER_OPTIONS_CENTS[nextIdx]
    soundManager.playButtonTap(cur.soundEnabled)
    val banner =
      if (nextWager >= GameState.MAX_WAGER_CENTS) {
        "MAX PLAY ACTIVE • PROGRESSIVE ELIGIBLE!"
      } else {
        "PLAY AMOUNT ${formatCents(nextWager)}"
      }
    _state.update { it.copy(wagerCents = nextWager, statusBanner = banner) }
    persistCurrent()
  }

  fun onMaxPlayClick() {
    val cur = _state.value
    if (cur.isSpinning || cur.shampooPickBonus != null) return
    if (cur.skillPrompt != null && !cur.skillPrompt.isSolved) {
      soundManager.playButtonTap(cur.soundEnabled)
      _state.update { it.copy(statusBanner = "CHOOSE OR NUDGE THE HIGHLIGHTED REEL!") }
      return
    }
    _state.update { it.copy(wagerCents = GameState.MAX_WAGER_CENTS) }
    onPlayClick()
  }

  fun onPlayClick() {
    val cur = _state.value
    if (cur.shampooPickBonus != null) return
    if (cur.bonusCelebration != null) {
      dismissBonusCelebration()
      return
    }
    if (cur.isSpinning) {
      fastStopRequested = true
      return
    }
    if (cur.skillPrompt != null && !cur.skillPrompt.isSolved) {
      soundManager.playButtonTap(cur.soundEnabled)
      _state.update { it.copy(statusBanner = "TAP THE WINNING ICON OR NUDGE ARROW!") }
      return
    }

    val isBonusSpin = cur.bonusSpinsRemaining > 0
    if (!isBonusSpin && cur.balanceCents < cur.wagerCents) {
      soundManager.playButtonTap(cur.soundEnabled)
      _state.update {
        it.copy(
          showCollectDialog = true,
          statusBanner = "LOW CREDITS • TAP REFILL TO CONTINUE",
        )
      }
      return
    }

    spinJob?.cancel()
    countUpJob?.cancel()
    fastStopRequested = false

    spinJob = viewModelScope.launch { executeSpin(isBonusSpin) }
  }

  private suspend fun executeSpin(isBonusSpin: Boolean) {
    val startState = _state.value
    val wager = startState.wagerCents
    val newBalance = if (isBonusSpin) startState.balanceCents else startState.balanceCents - wager
    val remainingBonus =
      if (isBonusSpin) (startState.bonusSpinsRemaining - 1).coerceAtLeast(0) else 0
    val progInc = (wager * 3L / 100L).coerceAtLeast(1L)
    val newProgressive = startState.progressiveCents + progInc

    soundManager.playSpinStart(startState.soundEnabled)

    _state.update {
      it.copy(
        balanceCents = newBalance,
        progressiveCents = newProgressive,
        bonusSpinsRemaining = remainingBonus,
        winCents = 0L,
        displayedWinCents = 0L,
        winningLines = emptyList(),
        bonusTriggerCells = emptySet(),
        bonusCelebration = null,
        skillPrompt = null,
        hotSwapSpots = emptyList(),
        instantWinBadgeCents = null,
        reelSpinning = listOf(true, true, true, true, true),
        statusBanner = if (isBonusSpin) "SUDSY BONUS SPIN • 2X MULTIPLIER!" else "GOOD LUCK!",
        totalSpins = it.totalSpins + 1,
      )
    }

    val finalGrid = generateTargetGrid(isBonusSpin)
    _state.update { it.copy(grid = finalGrid) }

    val stopDurationsMs = listOf(580L, 260L, 260L, 260L, 280L)
    val spinningFlags = MutableList(5) { true }
    for (col in 0 until 5) {
      val targetDelay = if (fastStopRequested) 55L else stopDurationsMs[col]
      var elapsed = 0L
      while (elapsed < targetDelay && !fastStopRequested) {
        delay(35L)
        elapsed += 35L
      }
      if (fastStopRequested && col > 0) delay(45L)
      spinningFlags[col] = false
      if (col == 4) {
        soundManager.stopSpinLoop()
      }
      soundManager.playReelStop(_state.value.soundEnabled, col)
      _state.update { it.copy(reelSpinning = spinningFlags.toList()) }
    }
    delay(220L)

    // Banilla "HOT SWAP" feature
    val currentGrid = _state.value.grid
    val hasDuckOrWild = currentGrid.flatten().any { it == SlotSymbol.DUCK || it == SlotSymbol.WILD }
    val hotSwapChance = if (isBonusSpin) 0.30 else 0.14
    var postSwapGrid = currentGrid
    if (hasDuckOrWild && Random.nextDouble() < hotSwapChance) {
      val swapSpots = pickHotSwapSpots(currentGrid)
      if (swapSpots.isNotEmpty()) {
        _state.update {
          it.copy(
            hotSwapSpots = swapSpots,
            statusBanner = "HOT SWAP! DUCK WILDS INCOMING!",
          )
        }
        soundManager.playBubblePop(_state.value.soundEnabled)
        delay(420L)
        val updated = currentGrid.map { it.toMutableList() }
        for (spot in swapSpots) {
          updated[spot.col][spot.row] = SlotSymbol.WILD
        }
        postSwapGrid = updated.map { it.toList() }
        soundManager.playBonusTrigger(_state.value.soundEnabled)
        _state.update {
          it.copy(
            grid = postSwapGrid,
            hotSwapSpots = emptyList(),
          )
        }
        delay(220L)
      }
    }

    val bonusMult = if (isBonusSpin) 2 else 1
    val eval =
      Paylines.evaluate(
        grid = postSwapGrid,
        wagerCents = wager,
        progressiveEligible = _state.value.isProgressiveEligible,
        progressiveCents = _state.value.progressiveCents,
        bonusMultiplier = bonusMult,
      )

    if (eval.totalPayoutCents == 0L && _state.value.skillModeEnabled) {
      val skill = detectOrBuildSkillPrompt(postSwapGrid, wager, bonusMult)
      if (skill != null) {
        soundManager.playBubblePop(_state.value.soundEnabled)
        _state.update {
          it.copy(
            skillPrompt = skill,
            statusBanner = "CHOOSE ICON OR NUDGE REEL ${skill.targetColumn + 1} TO WIN!",
          )
        }
        persistCurrent()
        return
      }
    }

    finalizeOutcome(
      totalPayout = eval.totalPayoutCents,
      winningLines = eval.winningLines,
      duckBonusSpins = eval.duckBonusSpins,
      shampooBonusPicks = eval.shampooBonusPicks,
      bonusTriggerCells = eval.bonusTriggerCells,
      progressiveWon = eval.progressiveWon,
      isSkillAward = false,
      wasBonusSpin = isBonusSpin,
    )
  }

  fun onSkillChoose(chosenSymbol: SlotSymbol) {
    val cur = _state.value
    val prompt = cur.skillPrompt ?: return
    if (prompt.isSolved || cur.isSpinning) return

    if (chosenSymbol == prompt.targetSymbol) {
      applySolvedSkill(prompt)
    } else {
      soundManager.playButtonTap(cur.soundEnabled)
      _state.update {
        it.copy(
          skillPrompt = null,
          statusBanner = "MISSED SKILL PICK • SPIN AGAIN!",
        )
      }
    }
  }

  fun onSkillNudge(direction: Int) {
    val cur = _state.value
    val prompt = cur.skillPrompt ?: return
    if (prompt.isSolved || cur.isSpinning) return

    soundManager.playReelStop(cur.soundEnabled, prompt.targetColumn)
    applySolvedSkill(prompt)
  }

  private fun applySolvedSkill(prompt: SkillPrompt) {
    val cur = _state.value
    val updatedGrid = cur.grid.map { it.toMutableList() }
    updatedGrid[prompt.targetColumn][prompt.winningRow] = prompt.targetSymbol
    val finalGrid = updatedGrid.map { it.toList() }

    val eval =
      Paylines.evaluate(
        grid = finalGrid,
        wagerCents = cur.wagerCents,
        progressiveEligible = cur.isProgressiveEligible,
        progressiveCents = cur.progressiveCents,
        bonusMultiplier = if (cur.bonusSpinsRemaining > 0) 2 else 1,
      )

    val award = maxOf(eval.totalPayoutCents, prompt.potentialWinCents)
    soundManager.playBonusTrigger(cur.soundEnabled)

    _state.update {
      it.copy(
        grid = finalGrid,
        skillPrompt = prompt.copy(isSolved = true),
        statusBanner = "CORRECT ICON CHOSEN! +${formatCents(award)}",
      )
    }

    viewModelScope.launch {
      delay(350L)
      _state.update { it.copy(skillPrompt = null) }
      finalizeOutcome(
        totalPayout = award,
        winningLines = eval.winningLines,
        duckBonusSpins = eval.duckBonusSpins,
        shampooBonusPicks = eval.shampooBonusPicks,
        bonusTriggerCells = eval.bonusTriggerCells,
        progressiveWon = eval.progressiveWon,
        isSkillAward = true,
        wasBonusSpin = false,
      )
    }
  }

  private fun finalizeOutcome(
    totalPayout: Long,
    winningLines: List<com.example.model.WinningLine>,
    duckBonusSpins: Int,
    shampooBonusPicks: Int,
    bonusTriggerCells: Set<Pair<Int, Int>>,
    progressiveWon: Boolean,
    isSkillAward: Boolean,
    wasBonusSpin: Boolean,
  ) {
    val cur = _state.value
    val updatedBalance = cur.balanceCents + totalPayout
    val nextProgressive =
      if (progressiveWon) GameState.PROGRESSIVE_SEED_CENTS else cur.progressiveCents
    val nextBonusSpins = cur.bonusSpinsRemaining + duckBonusSpins
    val isBigWin = totalPayout >= cur.wagerCents * 4L

    val duckCount = cur.grid.flatten().count { it == SlotSymbol.DUCK }
    val shampooCount = cur.grid.flatten().count { it == SlotSymbol.SHAMPOO }

    val banner =
      when {
        progressiveWon -> "PROGRESSIVE TOP PRIZE! +${formatCents(totalPayout)}"
        shampooBonusPicks > 0 -> "SHAMPOO PICK BONUS! $shampooBonusPicks BOTTLE PICKS!"
        duckBonusSpins > 0 -> "DUCKY FREE SPINS! +$duckBonusSpins FREE SPINS (2X)!"
        isSkillAward -> "CORRECT ICON CHOSEN! WIN ${formatCents(totalPayout)}"
        totalPayout > 0L -> "WINNER! ${formatCents(totalPayout)}"
        nextBonusSpins > 0 -> "FREE BONUS SPINS REMAINING: $nextBonusSpins"
        wasBonusSpin -> "DUCKY FREE SPINS COMPLETE!"
        else -> "GOOD LUCK!"
      }

    val celebration =
      when {
        progressiveWon ->
          BonusCelebration(
            bonusType = BonusType.PROGRESSIVE,
            scatterPayoutCents = totalPayout,
            triggerCount = 5,
          )
        shampooBonusPicks > 0 ->
          BonusCelebration(
            bonusType = BonusType.SHAMPOO_PICK,
            picksAwarded = shampooBonusPicks,
            scatterPayoutCents = totalPayout,
            triggerCount = shampooCount,
          )
        duckBonusSpins > 0 ->
          BonusCelebration(
            bonusType = BonusType.DUCK_SPINS,
            spinsAwarded = duckBonusSpins,
            scatterPayoutCents = totalPayout,
            triggerCount = duckCount,
          )
        else -> null
      }

    pendingShampooPicks = shampooBonusPicks

    if (celebration != null) {
      soundManager.playBonusTrigger(cur.soundEnabled)
      if (progressiveWon) {
        soundManager.playWin(cur.soundEnabled, isBigWin = true)
      }
    } else if (totalPayout > 0L) {
      soundManager.playWin(cur.soundEnabled, isBigWin = isBigWin)
    }

    _state.update {
      it.copy(
        balanceCents = updatedBalance,
        progressiveCents = nextProgressive,
        winCents = totalPayout,
        displayedWinCents = if (totalPayout == 0L) 0L else it.displayedWinCents,
        winningLines = winningLines,
        bonusTriggerCells = bonusTriggerCells,
        bonusCelebration = celebration,
        bonusSpinsRemaining = nextBonusSpins,
        instantWinBadgeCents =
          if (celebration == null && totalPayout >= cur.wagerCents * 3L) totalPayout else null,
        statusBanner = banner,
        totalWonCents = it.totalWonCents + totalPayout,
        biggestWinCents = maxOf(it.biggestWinCents, totalPayout),
      )
    }

    persistCurrent()

    if (totalPayout > 0L) {
      startCountUpAnimation(totalPayout)
    }

    celebrationJob?.cancel()
    if (celebration != null) {
      celebrationJob =
        viewModelScope.launch {
          delay(2500L)
          dismissBonusCelebration()
        }
    } else if (nextBonusSpins > 0) {
      scheduleNextBonusSpin(delayMs = if (totalPayout > 0L) 1400L else 900L)
    }
  }

  fun dismissBonusCelebration() {
    celebrationJob?.cancel()
    val cur = _state.value
    if (cur.bonusCelebration == null) return
    _state.update { it.copy(bonusCelebration = null) }

    if (pendingShampooPicks > 0) {
      val picks = pendingShampooPicks
      pendingShampooPicks = 0
      launchShampooPickBonus(picks, cur.wagerCents)
    } else if (cur.bonusSpinsRemaining > 0) {
      scheduleNextBonusSpin(delayMs = 450L)
    }
  }

  private fun launchShampooPickBonus(picks: Int, wagerCents: Long) {
    val multipliers = listOf(2, 3, 4, 5, 6, 8, 10, 12, 15, 20).shuffled().take(9)
    val rawTiles = mutableListOf<ShampooPickTile>()
    multipliers.forEachIndexed { idx, m ->
      rawTiles.add(ShampooPickTile(id = idx, multiplier = m))
    }
    rawTiles.add(ShampooPickTile(id = 9, multiplier = 2, isGoldenBoost = true))
    rawTiles.add(ShampooPickTile(id = 10, multiplier = 3, isExtraPick = true))
    rawTiles.add(ShampooPickTile(id = 11, multiplier = 5, isExtraPick = true))

    val shuffledTiles =
      rawTiles.shuffled().mapIndexed { index, tile -> tile.copy(id = index) }

    _state.update {
      it.copy(
        shampooPickBonus =
          ShampooPickBonus(
            picksRemaining = picks,
            totalPicksGranted = picks,
            wagerCents = wagerCents,
            tiles = shuffledTiles,
            lastRevealText = "PICK $picks LUCKY SHAMPOO BOTTLES!",
          )
      )
    }
  }

  fun onPickShampooTile(tileId: Int) {
    val cur = _state.value
    val bonus = cur.shampooPickBonus ?: return
    if (bonus.isComplete || bonus.picksRemaining <= 0) return
    val target = bonus.tiles.firstOrNull { it.id == tileId } ?: return
    if (target.isRevealed) return

    var newMultiplier = bonus.globalMultiplier
    var nextPicks = bonus.picksRemaining - 1
    var newTotal = bonus.totalBonusWonCents
    val revealMsg: String

    if (target.isGoldenBoost) {
      newMultiplier *= 2
      newTotal *= 2
      soundManager.playBonusTrigger(cur.soundEnabled)
      revealMsg = "GOLDEN SHAMPOO! 2X ALL PICKS BOOST!"
    } else if (target.isExtraPick) {
      nextPicks += 1
      val add = target.multiplier * bonus.wagerCents * newMultiplier
      newTotal += add
      soundManager.playBubblePop(cur.soundEnabled)
      revealMsg = "+1 EXTRA PICK & +${formatCents(add)}!"
    } else {
      val add = target.multiplier * bonus.wagerCents * newMultiplier
      newTotal += add
      soundManager.playBubblePop(cur.soundEnabled)
      revealMsg = "POPPED ${target.multiplier}X BOTTLE! +${formatCents(add)}"
    }

    val updatedTiles =
      bonus.tiles.map { if (it.id == tileId) it.copy(isRevealed = true) else it }
    val complete = nextPicks <= 0
    if (complete) {
      soundManager.playWin(cur.soundEnabled, isBigWin = true)
    }

    _state.update {
      it.copy(
        shampooPickBonus =
          bonus.copy(
            picksRemaining = nextPicks,
            tiles = updatedTiles,
            totalBonusWonCents = newTotal,
            globalMultiplier = newMultiplier,
            lastRevealText = if (complete) "BONUS COMPLETE! TAP COLLECT!" else revealMsg,
            isComplete = complete,
          )
      )
    }
  }

  fun onCollectShampooPickBonus() {
    val cur = _state.value
    val bonus = cur.shampooPickBonus ?: return
    val bonusAmount = bonus.totalBonusWonCents
    val newWin = cur.winCents + bonusAmount
    val newBalance = cur.balanceCents + bonusAmount

    soundManager.playWin(cur.soundEnabled, isBigWin = true)

    _state.update {
      it.copy(
        shampooPickBonus = null,
        balanceCents = newBalance,
        winCents = newWin,
        displayedWinCents = newWin,
        totalWonCents = it.totalWonCents + bonusAmount,
        biggestWinCents = maxOf(it.biggestWinCents, newWin),
        statusBanner = "SHAMPOO PICK BONUS COLLECTED: +${formatCents(bonusAmount)}!",
      )
    }
    persistCurrent()

    if (_state.value.bonusSpinsRemaining > 0) {
      scheduleNextBonusSpin(delayMs = 950L)
    }
  }

  private fun scheduleNextBonusSpin(delayMs: Long) {
    viewModelScope.launch {
      delay(delayMs)
      val latest = _state.value
      if (
        !latest.isSpinning &&
          latest.bonusSpinsRemaining > 0 &&
          latest.skillPrompt == null &&
          latest.bonusCelebration == null &&
          latest.shampooPickBonus == null
      ) {
        onPlayClick()
      }
    }
  }

  private fun startCountUpAnimation(targetCents: Long) {
    countUpJob?.cancel()
    countUpJob =
      viewModelScope.launch {
        val steps = 16
        for (step in 1..steps) {
          val value = (targetCents * step) / steps
          _state.update { it.copy(displayedWinCents = value) }
          delay(28L)
        }
        _state.update { it.copy(displayedWinCents = targetCents) }
      }
  }

  private fun generateTargetGrid(isBonusSpin: Boolean): List<List<SlotSymbol>> {
    val grid = MutableList(5) { MutableList(3) { SlotSymbol.randomWeighted() } }
    if (isBonusSpin && Random.nextDouble() < 0.45) {
      val col = Random.nextInt(0, 3)
      val row = Random.nextInt(0, 3)
      grid[col][row] = SlotSymbol.WILD
    }
    return grid.map { it.toList() }
  }

  private fun pickHotSwapSpots(grid: List<List<SlotSymbol>>): List<HotSwapSpot> {
    val candidates = mutableListOf<HotSwapSpot>()
    for (col in 0 until 5) {
      for (row in 0 until 3) {
        val s = grid[col][row]
        if (s != SlotSymbol.WILD && s != SlotSymbol.SHAMPOO) {
          candidates.add(HotSwapSpot(col, row))
        }
      }
    }
    return candidates.shuffled().take(Random.nextInt(1, 3))
  }

  private fun detectOrBuildSkillPrompt(
    grid: List<List<SlotSymbol>>,
    wagerCents: Long,
    bonusMult: Int,
  ): SkillPrompt? {
    for (payline in Paylines.ALL) {
      val s0 = grid[0][payline.rows[0]]
      val s1 = grid[1][payline.rows[1]]
      val target =
        when {
          s0 == s1 && s0 != SlotSymbol.SHAMPOO -> s0
          s0 == SlotSymbol.WILD && s1 != SlotSymbol.SHAMPOO -> s1
          s1 == SlotSymbol.WILD && s0 != SlotSymbol.SHAMPOO -> s0
          else -> null
        }
      if (target != null) {
        val targetRow = payline.rows[2]
        val lineUnit = (wagerCents / 5L).coerceAtLeast(5L)
        val potential = (target.pay3 * lineUnit * bonusMult).coerceAtLeast(wagerCents * 2L)
        val decoys =
          SlotSymbol.entries
            .filter { it != target && it != SlotSymbol.WILD }
            .shuffled()
            .take(2)
        val choices = (listOf(target) + decoys).shuffled()
        return SkillPrompt(
          targetColumn = 2,
          winningRow = targetRow,
          targetSymbol = target,
          candidateSymbols = choices,
          potentialWinCents = potential,
        )
      }
    }

    if (Random.nextDouble() < 0.25) {
      val row = 1
      val target = SlotSymbol.DUCK
      val decoys = listOf(SlotSymbol.CHERRY, SlotSymbol.SINGLE_BAR)
      val choices = (listOf(target) + decoys).shuffled()
      return SkillPrompt(
        targetColumn = 4,
        winningRow = row,
        targetSymbol = target,
        candidateSymbols = choices,
        potentialWinCents = wagerCents * 6L,
      )
    }
    return null
  }

  fun setShowPrizeViewer(show: Boolean) {
    soundManager.playButtonTap(_state.value.soundEnabled)
    _state.update { it.copy(showPrizeViewer = show) }
  }

  fun setShowCollectDialog(show: Boolean) {
    soundManager.playButtonTap(_state.value.soundEnabled)
    _state.update { it.copy(showCollectDialog = show) }
  }

  fun setShowSettingsDialog(show: Boolean) {
    soundManager.playButtonTap(_state.value.soundEnabled)
    _state.update { it.copy(showSettingsDialog = show) }
  }

  fun toggleSound(enabled: Boolean) {
    _state.update { it.copy(soundEnabled = enabled) }
    if (!enabled) soundManager.stopSpinLoop()
    soundManager.playButtonTap(enabled)
    persistSettings()
  }

  fun toggleHaptics(enabled: Boolean) {
    soundManager.playButtonTap(_state.value.soundEnabled)
    _state.update { it.copy(hapticsEnabled = enabled) }
    persistSettings()
  }

  fun toggleSkillMode(enabled: Boolean) {
    soundManager.playButtonTap(_state.value.soundEnabled)
    _state.update { it.copy(skillModeEnabled = enabled) }
    persistSettings()
  }

  fun refillVirtualCredits() {
    soundManager.playWin(_state.value.soundEnabled, isBigWin = false)
    _state.update {
      it.copy(
        balanceCents = it.balanceCents + 100_000L,
        showCollectDialog = false,
        statusBanner = "ADDED $1,000.00 VIRTUAL CREDITS!",
      )
    }
    persistCurrent()
  }

  fun resetVirtualSession() {
    soundManager.playButtonTap(_state.value.soundEnabled)
    pendingShampooPicks = 0
    _state.update {
      it.copy(
        balanceCents = 100_000L,
        winCents = 0L,
        displayedWinCents = 0L,
        winningLines = emptyList(),
        bonusTriggerCells = emptySet(),
        bonusCelebration = null,
        shampooPickBonus = null,
        skillPrompt = null,
        bonusSpinsRemaining = 0,
        instantWinBadgeCents = null,
        totalSpins = 0,
        totalWonCents = 0L,
        biggestWinCents = 0L,
        showCollectDialog = false,
        statusBanner = "SESSION RESET • GOOD LUCK!",
      )
    }
    persistCurrent()
  }

  private fun persistCurrent() {
    val s = _state.value
    viewModelScope.launch {
      prefs.saveState(
        balanceCents = s.balanceCents,
        wagerCents = s.wagerCents,
        progressiveCents = s.progressiveCents,
        totalSpins = s.totalSpins,
        totalWonCents = s.totalWonCents,
        biggestWinCents = s.biggestWinCents,
      )
    }
  }

  private fun persistSettings() {
    val s = _state.value
    viewModelScope.launch {
      prefs.saveSettings(
        soundEnabled = s.soundEnabled,
        hapticsEnabled = s.hapticsEnabled,
        skillModeEnabled = s.skillModeEnabled,
      )
    }
  }

  override fun onCleared() {
    super.onCleared()
    soundManager.release()
  }
}
