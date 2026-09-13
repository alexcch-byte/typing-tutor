package com.alexchee.typingtutor.game

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.alexchee.typingtutor.R
import kotlin.random.Random

/**
 * Manages low-latency audio playback for gameplay sound effects using Android's [SoundPool].
 * Provides falling bomb whistles, impact explosions, and rewarding letter pop sounds.
 */
class GameSoundPlayer(context: Context) {

    private val soundPool: SoundPool
    private val whistleSoundId: Int
    private val explosionSoundId: Int
    private val popSoundId: Int

    private val activeWhistleStreams = mutableListOf<Int>()
    private var isLoaded = false

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(8)
            .setAudioAttributes(audioAttributes)
            .build()

        soundPool.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) isLoaded = true
        }

        whistleSoundId = soundPool.load(context, R.raw.falling_whistle, 1)
        explosionSoundId = soundPool.load(context, R.raw.explosion_boom, 1)
        popSoundId = soundPool.load(context, R.raw.letter_pop, 1)
    }

    /** Plays a falling bomb whistle descending in pitch. */
    fun playFallingWhistle() {
        if (!isLoaded) return
        // Keep at most 2 simultaneous falling whistles to prevent audio clutter
        while (activeWhistleStreams.size >= 2) {
            val oldStream = activeWhistleStreams.removeAt(0)
            soundPool.stop(oldStream)
        }
        val rate = Random.nextFloat() * 0.08f + 0.96f
        val streamId = soundPool.play(whistleSoundId, 0.55f, 0.55f, 1, 0, rate)
        if (streamId != 0) {
            activeWhistleStreams.add(streamId)
        }
    }

    /** Stops a falling whistle stream when a letter is defused or detonates. */
    fun stopOneWhistle() {
        if (activeWhistleStreams.isNotEmpty()) {
            val streamId = activeWhistleStreams.removeAt(0)
            soundPool.stop(streamId)
        }
    }

    /** Stops all active whistle streams (e.g. on round end, game over, or clear). */
    fun stopAllWhistles() {
        for (streamId in activeWhistleStreams) {
            soundPool.stop(streamId)
        }
        activeWhistleStreams.clear()
    }

    /** Plays the bomb explosion sound on impact. */
    fun playExplosion() {
        if (!isLoaded) return
        stopOneWhistle()
        val rate = Random.nextFloat() * 0.12f + 0.94f
        soundPool.play(explosionSoundId, 1.0f, 1.0f, 2, 0, rate)
    }

    /** Plays a crisp bubble/pop sound when a letter is successfully typed. */
    fun playPop(combo: Int = 0) {
        if (!isLoaded) return
        stopOneWhistle()
        // Pitch rises subtly with combo streak for rewarding audio feedback
        val rate = (1.0f + (combo.coerceAtMost(10) * 0.04f))
        soundPool.play(popSoundId, 0.85f, 0.85f, 1, 0, rate)
    }

    fun pauseAll() {
        soundPool.autoPause()
    }

    fun resumeAll() {
        soundPool.autoResume()
    }

    fun release() {
        stopAllWhistles()
        soundPool.release()
    }
}
