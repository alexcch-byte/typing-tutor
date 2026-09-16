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

    /** Remembers the most recently played game+level so the home screen can offer to resume it. */
    fun saveLastPlayed(gameTypeId: String, levelId: String) {
        prefs.edit()
            .putString(KEY_LAST_GAME_TYPE, gameTypeId)
            .putString(KEY_LAST_LEVEL_ID, levelId)
            .apply()
    }

    /** Returns (gameTypeId, levelId) of the last-played level, or null if none yet. */
    fun lastPlayed(): Pair<String, String>? {
        val gameTypeId = prefs.getString(KEY_LAST_GAME_TYPE, null) ?: return null
        val levelId = prefs.getString(KEY_LAST_LEVEL_ID, null) ?: return null
        return gameTypeId to levelId
    }

    companion object {
        private const val PREFS_NAME = "typing_tutor_scores"
        private const val KEY_LAST_GAME_TYPE = "last_played_game_type"
        private const val KEY_LAST_LEVEL_ID = "last_played_level_id"
    }
}
