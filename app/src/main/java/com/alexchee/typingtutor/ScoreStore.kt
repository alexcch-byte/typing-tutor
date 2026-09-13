package com.alexchee.typingtutor

import android.content.Context

/**
 * Persists each practice mode's best result locally so kids can see themselves improve.
 * "Best" means best score for the arcade games and best words-per-minute for Typing Race;
 * callers pick a unique [key] per game+level so results never collide across modes.
 */
class ScoreStore(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun bestValue(key: String): Int = prefs.getInt(prefKey(key), 0)

    /** Returns true if [value] beat the previous best stored under [key]. */
    fun submitValue(key: String, value: Int): Boolean {
        val previousBest = bestValue(key)
        if (value > previousBest) {
            prefs.edit().putInt(prefKey(key), value).apply()
            return true
        }
        return false
    }

    private fun prefKey(key: String) = "best_$key"

    companion object {
        private const val PREFS_NAME = "typing_tutor_scores"
    }
}
