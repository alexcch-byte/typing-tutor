package com.alexchee.typingtutor.game

import android.content.Context
import android.content.Intent
import android.media.ToneGenerator
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import com.alexchee.typingtutor.Level
import com.alexchee.typingtutor.R
import com.alexchee.typingtutor.ScoreStore
import com.alexchee.typingtutor.databinding.ActivityFallingLettersBinding

class FallingLettersActivity : BaseGameActivity(), FallingLettersView.Listener {

    private lateinit var binding: ActivityFallingLettersBinding
    private lateinit var level: Level
    private lateinit var scoreStore: ScoreStore
    private lateinit var soundPlayer: GameSoundPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFallingLettersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        level = Level.fromId(intent.getStringExtra(EXTRA_LEVEL_ID))
        scoreStore = ScoreStore(this)
        soundPlayer = GameSoundPlayer(this)

        binding.levelLabel.text = getString(level.displayNameRes)
        binding.gameView.listener = this
        binding.gameView.configure(level)

        binding.pauseButton.setOnClickListener { showPause() }
        binding.resumeButton.setOnClickListener { hidePause() }
        binding.pauseHomeButton.setOnClickListener { finish() }
        binding.retryButton.setOnClickListener { retry() }
        binding.gameOverHomeButton.setOnClickListener { finish() }

        onScoreChanged(0)
        onLivesChanged(5)
        onComboChanged(0)
    }

    override fun onGameResume() {
        soundPlayer.resumeAll()
        binding.gameView.resume()
    }

    override fun onGamePause() {
        soundPlayer.pauseAll()
        binding.gameView.pause()
    }

    override fun onDestroy() {
        soundPlayer.release()
        super.onDestroy()
    }

    override fun onPauseVisibilityChanged(visible: Boolean) {
        binding.pauseOverlay.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun handleGameKeyEvent(event: KeyEvent): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN) return false
        val c = event.unicodeChar.toChar()
        if (c.code == 0 || !(c.isLetterOrDigit() || c in ";,./")) return false
        when (binding.gameView.handleTypedChar(c)) {
            FallingLettersView.TypeResult.HIT -> {
                // Popping sound handled in onLetterHit
            }
            FallingLettersView.TypeResult.MISS_NO_MATCH -> playTone(ToneGenerator.TONE_PROP_NACK)
            FallingLettersView.TypeResult.IGNORED -> return false
        }
        return true
    }

    private fun retry() {
        soundPlayer.stopAllWhistles()
        resetSession()
        binding.gameOverOverlay.visibility = View.GONE
        binding.gameView.configure(level)
        binding.gameView.start()
    }

    override fun onScoreChanged(score: Int) {
        binding.scoreLabel.text = getString(R.string.score_format, score)
    }

    override fun onLivesChanged(lives: Int) {
        binding.livesLabel.text = getString(R.string.lives_format, lives)
    }

    override fun onComboChanged(combo: Int) {
        binding.comboLabel.text = if (combo > 1) getString(R.string.combo_format, combo) else ""
    }

    override fun onLetterSpawned() {
        soundPlayer.playFallingWhistle()
    }

    override fun onLetterHit(combo: Int) {
        soundPlayer.playPop(combo)
    }

    override fun onLetterExploded() {
        soundPlayer.playExplosion()
    }

    override fun onGameOver(finalScore: Int) {
        soundPlayer.stopAllWhistles()
        markSessionOver()
        val isNewBest = scoreStore.submitValue("ft_${level.id}", finalScore)
        binding.finalScoreLabel.text = getString(R.string.final_score_format, finalScore)
        binding.newBestLabel.visibility = if (isNewBest) View.VISIBLE else View.GONE
        binding.gameOverOverlay.visibility = View.VISIBLE
    }

    companion object {
        private const val EXTRA_LEVEL_ID = "extra_level_id"

        fun start(context: Context, level: Level) {
            context.startActivity(
                Intent(context, FallingLettersActivity::class.java)
                    .putExtra(EXTRA_LEVEL_ID, level.id),
            )
        }
    }
}
