package com.alexchee.typingtutor.game

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity

/**
 * Common plumbing for every full-screen game: immersive layout, a shared tone
 * generator for hit/miss feedback, and the pause / game-over / Escape-key state
 * machine. Centralizing this avoids re-introducing the same "Escape leaks a Pause
 * panel behind an already-finished game" bug in every new game mode.
 */
abstract class BaseGameActivity : AppCompatActivity() {

    private var toneGenerator: ToneGenerator? = null

    var isPaused = false
        private set
    var isSessionOver = false
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
    }

    /**
     * Hooked (rather than called from [onCreate]) because the window has no decor
     * view — and [android.view.Window.getInsetsController] returns null — until a
     * content view is attached, which each subclass does after its own super.onCreate().
     */
    override fun setContentView(view: View) {
        super.setContentView(view)
        hideSystemBars()
    }

    override fun onResume() {
        super.onResume()
        if (!isPaused && !isSessionOver) onGameResume()
    }

    override fun onPause() {
        super.onPause()
        onGamePause()
    }

    override fun onDestroy() {
        super.onDestroy()
        toneGenerator?.release()
        toneGenerator = null
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemBars()
    }

    /**
     * All keyboard input — including from a paired Bluetooth keyboard, which Android
     * delivers as ordinary hardware KeyEvents — is intercepted here rather than via a
     * focused EditText, since none of these games have a text field.
     */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN &&
            (event.keyCode == KeyEvent.KEYCODE_ESCAPE || event.keyCode == KeyEvent.KEYCODE_BACK)
        ) {
            when {
                isSessionOver -> finish()
                isPaused -> hidePause()
                else -> showPause()
            }
            return true
        }
        if (!isPaused && !isSessionOver && handleGameKeyEvent(event)) return true
        return super.dispatchKeyEvent(event)
    }

    fun showPause() {
        if (isSessionOver || isPaused) return
        isPaused = true
        onGamePause()
        onPauseVisibilityChanged(true)
    }

    fun hidePause() {
        if (!isPaused) return
        isPaused = false
        onGameResume()
        onPauseVisibilityChanged(false)
    }

    /** Call when the round ends (lives depleted, session finished, etc). */
    protected fun markSessionOver() {
        isSessionOver = true
        onGamePause()
    }

    /** Call when starting a fresh attempt after showing a results screen. */
    protected fun resetSession() {
        isSessionOver = false
        isPaused = false
    }

    protected fun playTone(tone: Int, durationMs: Int = 100) {
        toneGenerator?.startTone(tone, durationMs)
    }

    /** Resume gameplay (frame loop / timer). Also called on system resume and Resume-button tap. */
    protected abstract fun onGameResume()

    /** Pause gameplay (frame loop / timer). Also called on system pause and Pause-button tap. */
    protected abstract fun onGamePause()

    protected abstract fun onPauseVisibilityChanged(visible: Boolean)

    /** Return true if [event] was consumed as gameplay input (a printable key press). */
    protected abstract fun handleGameKeyEvent(event: KeyEvent): Boolean

    private fun hideSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                )
        }
    }
}
