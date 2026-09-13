package com.alexchee.typingtutor.game

import android.content.Context
import android.content.Intent
import android.media.ToneGenerator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.view.KeyEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.alexchee.typingtutor.R
import com.alexchee.typingtutor.ScoreStore
import com.alexchee.typingtutor.SentenceLevel
import com.alexchee.typingtutor.databinding.ActivityTypingRaceBinding
import kotlin.math.roundToInt

/**
 * A calmer companion to the two arcade games: type full sentences left-to-right,
 * exact character and case, for accuracy/WPM practice rather than reflex practice.
 * A wrong key does not advance the cursor — the child must correct it before moving
 * on, which is how most typing tutors build accurate habits rather than just speed.
 */
class TypingRaceActivity : BaseGameActivity() {

    private lateinit var binding: ActivityTypingRaceBinding
    private lateinit var level: SentenceLevel
    private lateinit var scoreStore: ScoreStore

    private lateinit var sentences: List<String>
    private var sentenceIndex = 0
    private var typedIndex = 0
    private var totalCorrectChars = 0
    private var totalMistakes = 0

    private var hasStarted = false
    private var accumulatedElapsedMs = 0L
    private var runStartElapsedRealtime: Long? = null

    private val tickHandler = Handler(Looper.getMainLooper())
    private val tickRunnable = object : Runnable {
        override fun run() {
            updateHud()
            tickHandler.postDelayed(this, 100)
        }
    }

    private var correctColor = 0
    private var cursorColor = 0
    private var mistakeColor = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTypingRaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        level = SentenceLevel.fromId(intent.getStringExtra(EXTRA_LEVEL_ID))
        scoreStore = ScoreStore(this)
        correctColor = ContextCompat.getColor(this, R.color.correct_green)
        cursorColor = ContextCompat.getColor(this, R.color.accent_yellow)
        mistakeColor = ContextCompat.getColor(this, R.color.wrong_red)

        binding.levelLabel.text = getString(level.displayNameRes)
        binding.pauseButton.setOnClickListener { showPause() }
        binding.resumeButton.setOnClickListener { hidePause() }
        binding.pauseHomeButton.setOnClickListener { finish() }
        binding.retryButton.setOnClickListener { retry() }
        binding.sessionCompleteHomeButton.setOnClickListener { finish() }

        startNewSession()
    }

    override fun onDestroy() {
        tickHandler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    override fun onGameResume() {
        if (hasStarted) {
            runStartElapsedRealtime = SystemClock.elapsedRealtime()
            tickHandler.post(tickRunnable)
        }
    }

    override fun onGamePause() {
        runStartElapsedRealtime?.let {
            accumulatedElapsedMs += SystemClock.elapsedRealtime() - it
            runStartElapsedRealtime = null
        }
        tickHandler.removeCallbacks(tickRunnable)
    }

    override fun onPauseVisibilityChanged(visible: Boolean) {
        binding.pauseOverlay.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun handleGameKeyEvent(event: KeyEvent): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN) return false
        val c = event.unicodeChar.toChar()
        if (c.code == 0 || c.isISOControl()) return false
        onCharTyped(c)
        return true
    }

    private fun startNewSession() {
        sentences = level.sentences.shuffled().take(SENTENCES_PER_SESSION)
        sentenceIndex = 0
        typedIndex = 0
        totalCorrectChars = 0
        totalMistakes = 0
        accumulatedElapsedMs = 0L
        runStartElapsedRealtime = null
        hasStarted = false
        binding.progressLabel.text = getString(R.string.sentence_progress_format, 1, sentences.size)
        updateSentenceSpan(cursorColor)
        updateHud()
    }

    private fun onCharTyped(c: Char) {
        if (!hasStarted) {
            hasStarted = true
            onGameResume()
        }
        val sentence = sentences[sentenceIndex]
        if (typedIndex < sentence.length && sentence[typedIndex] == c) {
            typedIndex++
            totalCorrectChars++
            playTone(ToneGenerator.TONE_PROP_BEEP, 40)
            if (typedIndex >= sentence.length) {
                advanceSentence()
            } else {
                updateSentenceSpan(cursorColor)
            }
        } else {
            totalMistakes++
            playTone(ToneGenerator.TONE_PROP_NACK, 80)
            flashMistake()
        }
        updateHud()
    }

    private fun advanceSentence() {
        sentenceIndex++
        typedIndex = 0
        if (sentenceIndex >= sentences.size) {
            finishSession()
        } else {
            binding.progressLabel.text = getString(R.string.sentence_progress_format, sentenceIndex + 1, sentences.size)
            updateSentenceSpan(cursorColor)
        }
    }

    private fun finishSession() {
        onGamePause()
        markSessionOver()
        val wpm = currentWpm()
        val accuracy = currentAccuracy()
        val isNewBest = scoreStore.submitValue("tr_${level.id}", wpm)
        binding.sessionSummaryLabel.text = getString(R.string.session_summary_format, wpm, accuracy)
        binding.newBestLabel.visibility = if (isNewBest) View.VISIBLE else View.GONE
        binding.sessionCompleteOverlay.visibility = View.VISIBLE
    }

    private fun retry() {
        resetSession()
        binding.sessionCompleteOverlay.visibility = View.GONE
        startNewSession()
    }

    private fun flashMistake() {
        updateSentenceSpan(mistakeColor)
        tickHandler.postDelayed({ if (!isSessionOver) updateSentenceSpan(cursorColor) }, 150)
    }

    private fun updateSentenceSpan(cursorBg: Int) {
        val sentence = sentences[sentenceIndex]
        val spannable = SpannableString(sentence)
        if (typedIndex > 0) {
            spannable.setSpan(ForegroundColorSpan(correctColor), 0, typedIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        if (typedIndex < sentence.length) {
            spannable.setSpan(BackgroundColorSpan(cursorBg), typedIndex, typedIndex + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        binding.sentenceText.text = spannable
    }

    private fun currentElapsedMs(): Long =
        accumulatedElapsedMs + (runStartElapsedRealtime?.let { SystemClock.elapsedRealtime() - it } ?: 0L)

    private fun currentWpm(): Int {
        if (!hasStarted) return 0
        val minutes = currentElapsedMs().coerceAtLeast(1L) / 60000.0
        return ((totalCorrectChars / 5.0) / minutes).roundToInt()
    }

    private fun currentAccuracy(): Int {
        val totalKeystrokes = totalCorrectChars + totalMistakes
        return if (totalKeystrokes > 0) ((totalCorrectChars.toDouble() / totalKeystrokes) * 100).roundToInt() else 100
    }

    private fun updateHud() {
        binding.wpmLabel.text = getString(R.string.wpm_format, currentWpm())
        binding.accuracyLabel.text = getString(R.string.accuracy_format, currentAccuracy())
        binding.timeLabel.text = getString(R.string.time_format, currentElapsedMs() / 1000f)
    }

    companion object {
        private const val EXTRA_LEVEL_ID = "extra_level_id"
        private const val SENTENCES_PER_SESSION = 4

        fun start(context: Context, level: SentenceLevel) {
            context.startActivity(
                Intent(context, TypingRaceActivity::class.java)
                    .putExtra(EXTRA_LEVEL_ID, level.id),
            )
        }
    }
}
