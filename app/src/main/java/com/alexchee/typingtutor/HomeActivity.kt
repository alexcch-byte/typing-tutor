package com.alexchee.typingtutor

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import com.alexchee.typingtutor.databinding.ActivityHomeBinding
import com.alexchee.typingtutor.databinding.ItemLevelBinding
import com.alexchee.typingtutor.game.FallingLettersActivity
import com.alexchee.typingtutor.game.KeyHeroActivity
import com.alexchee.typingtutor.game.TypingRaceActivity
import com.alexchee.typingtutor.game.WordRainActivity
import java.util.Calendar

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var scoreStore: ScoreStore
    private var selectedGame = GameType.FALLING_LETTERS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        scoreStore = ScoreStore(this)

        renderGreeting()
        startMascotWave()
        buildTabs()
        refreshHomeState()
    }

    override fun onResume() {
        super.onResume()
        // Best scores and last-played may have changed after playing a level; refresh.
        refreshHomeState()
    }

    private fun refreshHomeState() {
        renderContinueCard()
        renderStatsStrip()
        buildLevelList()
    }

    private fun renderGreeting() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val textRes = when {
            hour < 5 -> R.string.greeting_early
            hour < 12 -> R.string.greeting_morning
            hour < 17 -> R.string.greeting_afternoon
            hour < 21 -> R.string.greeting_evening
            else -> R.string.greeting_night
        }
        binding.greetingLabel.text = getString(textRes)
    }

    /** Gently waves the mascot's arm on a loop; purely decorative. */
    private fun startMascotWave() {
        binding.mascotArm.doOnLayout { arm ->
            arm.pivotX = arm.width * 0.9f
            arm.pivotY = arm.height * 0.38f
            ObjectAnimator.ofFloat(arm, View.ROTATION, 0f, 18f, 0f).apply {
                duration = 1800
                repeatCount = ValueAnimator.INFINITE
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
        }
    }

    private fun renderContinueCard() {
        val last = scoreStore.lastPlayed()
        val gameType = last?.let { (gameTypeId, _) -> GameType.entries.firstOrNull { it.id == gameTypeId } }
        val levelId = last?.second
        val levelName = if (gameType != null && levelId != null) levelDisplayName(gameType, levelId) else null

        if (gameType == null || levelId == null || levelName == null) {
            binding.continueCard.visibility = View.GONE
            binding.continueCard.setOnClickListener(null)
            return
        }

        binding.continueCard.visibility = View.VISIBLE
        binding.continueTitleLabel.text = getString(R.string.continue_title_format, getString(gameType.titleRes), levelName)
        binding.continueCard.setOnClickListener {
            scoreStore.saveLastPlayed(gameType.id, levelId)
            startLevel(gameType, levelId)
        }
    }

    private fun renderStatsStrip() {
        val count = countPersonalBests()
        if (count == 0) {
            binding.statsStripLabel.visibility = View.GONE
            return
        }
        binding.statsStripLabel.visibility = View.VISIBLE
        binding.statsStripLabel.text = resources.getQuantityString(R.plurals.personal_bests, count, count)
    }

    private fun countPersonalBests(): Int {
        var count = 0
        for (level in Level.entries) if (scoreStore.bestValue("ft_${level.id}") > 0) count++
        for (level in WordLevel.entries) if (scoreStore.bestValue("wr_${level.id}") > 0) count++
        for (level in SentenceLevel.entries) if (scoreStore.bestValue("tr_${level.id}") > 0) count++
        for (level in RhythmLevel.entries) if (scoreStore.bestValue("kh_${level.id}") > 0) count++
        return count
    }

    private fun levelDisplayName(gameType: GameType, levelId: String): String? = when (gameType) {
        GameType.FALLING_LETTERS -> Level.entries.firstOrNull { it.id == levelId }?.let { getString(it.displayNameRes) }
        GameType.WORD_RAIN -> WordLevel.entries.firstOrNull { it.id == levelId }?.let { getString(it.displayNameRes) }
        GameType.TYPING_RACE -> SentenceLevel.entries.firstOrNull { it.id == levelId }?.let { getString(it.displayNameRes) }
        GameType.KEY_HERO -> RhythmLevel.entries.firstOrNull { it.id == levelId }?.let { getString(it.displayNameRes) }
    }

    private fun startLevel(gameType: GameType, levelId: String) {
        when (gameType) {
            GameType.FALLING_LETTERS -> Level.entries.firstOrNull { it.id == levelId }?.let { FallingLettersActivity.start(this, it) }
            GameType.WORD_RAIN -> WordLevel.entries.firstOrNull { it.id == levelId }?.let { WordRainActivity.start(this, it) }
            GameType.TYPING_RACE -> SentenceLevel.entries.firstOrNull { it.id == levelId }?.let { TypingRaceActivity.start(this, it) }
            GameType.KEY_HERO -> RhythmLevel.entries.firstOrNull { it.id == levelId }?.let { KeyHeroActivity.start(this, it) }
        }
    }

    private fun buildTabs() {
        binding.gameTabContainer.removeAllViews()
        val inflater = LayoutInflater.from(this)
        for (game in GameType.entries) {
            val tab = inflater.inflate(R.layout.item_game_tab, binding.gameTabContainer, false) as TextView
            tab.text = getString(game.titleRes)
            styleTab(tab, game == selectedGame)
            tab.setOnClickListener {
                if (selectedGame != game) {
                    selectedGame = game
                    buildTabs()
                    buildLevelList()
                }
            }
            binding.gameTabContainer.addView(tab)
        }
    }

    private fun styleTab(tab: TextView, selected: Boolean) {
        if (selected) {
            tab.setBackgroundResource(R.drawable.bg_tab_selected)
            tab.setTextColor(ContextCompat.getColor(this, R.color.card_text))
        } else {
            tab.setBackgroundResource(0)
            tab.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
        }
    }

    private fun buildLevelList() {
        binding.levelListContainer.removeAllViews()
        val inflater = LayoutInflater.from(this)
        when (selectedGame) {
            GameType.FALLING_LETTERS -> for (level in Level.entries) {
                addLevelCard(
                    inflater,
                    getString(level.displayNameRes),
                    getString(level.descriptionRes),
                    getString(R.string.best_score_format, scoreStore.bestValue("ft_${level.id}")),
                ) {
                    scoreStore.saveLastPlayed(GameType.FALLING_LETTERS.id, level.id)
                    FallingLettersActivity.start(this, level)
                }
            }
            GameType.WORD_RAIN -> for (level in WordLevel.entries) {
                addLevelCard(
                    inflater,
                    getString(level.displayNameRes),
                    getString(level.descriptionRes),
                    getString(R.string.best_score_format, scoreStore.bestValue("wr_${level.id}")),
                ) {
                    scoreStore.saveLastPlayed(GameType.WORD_RAIN.id, level.id)
                    WordRainActivity.start(this, level)
                }
            }
            GameType.TYPING_RACE -> for (level in SentenceLevel.entries) {
                addLevelCard(
                    inflater,
                    getString(level.displayNameRes),
                    getString(level.descriptionRes),
                    getString(R.string.best_wpm_format, scoreStore.bestValue("tr_${level.id}")),
                ) {
                    scoreStore.saveLastPlayed(GameType.TYPING_RACE.id, level.id)
                    TypingRaceActivity.start(this, level)
                }
            }
            GameType.KEY_HERO -> for (level in RhythmLevel.entries) {
                addLevelCard(
                    inflater,
                    getString(level.displayNameRes),
                    getString(level.descriptionRes),
                    getString(R.string.best_score_format, scoreStore.bestValue("kh_${level.id}")),
                ) {
                    scoreStore.saveLastPlayed(GameType.KEY_HERO.id, level.id)
                    KeyHeroActivity.start(this, level)
                }
            }
        }
    }

    private fun addLevelCard(
        inflater: LayoutInflater,
        name: String,
        description: String,
        best: String,
        onClick: () -> Unit,
    ) {
        val itemBinding = ItemLevelBinding.inflate(inflater, binding.levelListContainer, false)
        itemBinding.levelName.text = name
        itemBinding.levelDesc.text = description
        itemBinding.levelBest.text = best
        itemBinding.root.setOnClickListener { onClick() }
        binding.levelListContainer.addView(itemBinding.root)
    }
}
