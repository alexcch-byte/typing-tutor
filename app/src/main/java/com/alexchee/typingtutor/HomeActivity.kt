package com.alexchee.typingtutor

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.alexchee.typingtutor.databinding.ActivityHomeBinding
import com.alexchee.typingtutor.databinding.ItemLevelBinding
import com.alexchee.typingtutor.game.FallingLettersActivity
import com.alexchee.typingtutor.game.TypingRaceActivity
import com.alexchee.typingtutor.game.WordRainActivity

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var scoreStore: ScoreStore
    private var selectedGame = GameType.FALLING_LETTERS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        scoreStore = ScoreStore(this)

        buildTabs()
        buildLevelList()
    }

    override fun onResume() {
        super.onResume()
        // Best scores may have changed after playing a level; rebuild to refresh them.
        buildLevelList()
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
                ) { FallingLettersActivity.start(this, level) }
            }
            GameType.WORD_RAIN -> for (level in WordLevel.entries) {
                addLevelCard(
                    inflater,
                    getString(level.displayNameRes),
                    getString(level.descriptionRes),
                    getString(R.string.best_score_format, scoreStore.bestValue("wr_${level.id}")),
                ) { WordRainActivity.start(this, level) }
            }
            GameType.TYPING_RACE -> for (level in SentenceLevel.entries) {
                addLevelCard(
                    inflater,
                    getString(level.displayNameRes),
                    getString(level.descriptionRes),
                    getString(R.string.best_wpm_format, scoreStore.bestValue("tr_${level.id}")),
                ) { TypingRaceActivity.start(this, level) }
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
