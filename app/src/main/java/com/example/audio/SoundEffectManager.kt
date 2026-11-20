package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.R

/**
 * Standard Android SoundPool manager for low-latency polyphonic arcade audio cues:
 * continuous reel spin loop, staggered reel stops, winning chimes, duck squeak bonus triggers,
 * bubble pops, and tactile button clicks.
 */
class SoundEffectManager(context: Context) {

  private val soundPool: SoundPool? =
    try {
      val attrs =
        AudioAttributes.Builder()
          .setUsage(AudioAttributes.USAGE_GAME)
          .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
          .build()
      SoundPool.Builder().setMaxStreams(8).setAudioAttributes(attrs).build()
    } catch (_: Throwable) {
      null
    }

  private val loadedIds = mutableSetOf<Int>()

  private val spinStartId: Int
  private val reelSpinLoopId: Int
  private val reelStopId: Int
  private val winSmallId: Int
  private val winBigId: Int
  private val bonusTriggerId: Int
  private val bubblePopId: Int
  private val buttonTapId: Int

  private var activeSpinStreamId: Int = 0

  init {
    soundPool?.setOnLoadCompleteListener { _, sampleId, status ->
      if (status == 0) {
        loadedIds.add(sampleId)
      }
    }
    spinStartId = loadRaw(context, R.raw.sfx_spin_start)
    reelSpinLoopId = loadRaw(context, R.raw.sfx_reel_spin_loop)
    reelStopId = loadRaw(context, R.raw.sfx_reel_stop)
    winSmallId = loadRaw(context, R.raw.sfx_win_small)
    winBigId = loadRaw(context, R.raw.sfx_win_big)
    bonusTriggerId = loadRaw(context, R.raw.sfx_bonus_trigger)
    bubblePopId = loadRaw(context, R.raw.sfx_bubble_pop)
    buttonTapId = loadRaw(context, R.raw.sfx_button_tap)
  }

  private fun loadRaw(context: Context, resId: Int): Int =
    try {
      soundPool?.load(context, resId, 1) ?: 0
    } catch (_: Throwable) {
      0
    }

  fun playSpinStart(enabled: Boolean) {
    if (!enabled) return
    playCue(spinStartId, volume = 0.75f, rate = 1.0f)
    stopSpinLoop()
    val pool = soundPool ?: return
    if (reelSpinLoopId != 0) {
      activeSpinStreamId = pool.play(reelSpinLoopId, 0.35f, 0.35f, 1, -1, 1.05f)
    }
  }

  fun setSpinLoopRate(rate: Float) {
    if (activeSpinStreamId != 0) {
      try {
        soundPool?.setRate(activeSpinStreamId, rate.coerceIn(0.55f, 1.5f))
      } catch (_: Throwable) {
      }
    }
  }

  fun playAnticipationCue(enabled: Boolean) {
    if (!enabled) return
    playCue(bubblePopId, volume = 0.90f, rate = 1.18f)
  }

  fun stopSpinLoop() {
    if (activeSpinStreamId != 0) {
      try {
        soundPool?.stop(activeSpinStreamId)
      } catch (_: Throwable) {
      }
      activeSpinStreamId = 0
    }
  }

  fun playReelStop(enabled: Boolean, reelIndex: Int) {
    if (!enabled) return
    val pitch = 0.94f + reelIndex * 0.04f
    playCue(reelStopId, volume = 0.85f, rate = pitch)
  }

  fun playWin(enabled: Boolean, isBigWin: Boolean) {
    if (!enabled) return
    val id = if (isBigWin) winBigId else winSmallId
    playCue(id, volume = 0.92f, rate = 1.0f)
  }

  fun playBonusTrigger(enabled: Boolean) {
    if (!enabled) return
    playCue(bonusTriggerId, volume = 1.0f, rate = 1.0f)
  }

  fun playBubblePop(enabled: Boolean) {
    if (!enabled) return
    playCue(bubblePopId, volume = 0.82f, rate = 1.0f)
  }

  fun playButtonTap(enabled: Boolean) {
    if (!enabled) return
    playCue(buttonTapId, volume = 0.65f, rate = 1.0f)
  }

  private fun playCue(soundId: Int, volume: Float, rate: Float) {
    val pool = soundPool ?: return
    if (soundId == 0) return
    try {
      pool.play(soundId, volume, volume, 1, 0, rate)
    } catch (_: Throwable) {
    }
  }

  fun release() {
    stopSpinLoop()
    try {
      soundPool?.release()
    } catch (_: Throwable) {
    }
  }
}
