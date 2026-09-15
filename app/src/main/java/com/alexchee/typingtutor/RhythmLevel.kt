package com.alexchee.typingtutor

/**
 * A Key Hero difficulty tier. Unlike the other games, the 8 lanes (mapped to
 * the home-row keys) never change — only note speed, spawn rate, and how often
 * multiple lanes fire together as a "chord" ramp up with difficulty.
 */
enum class RhythmLevel(
    val id: String,
    val displayNameRes: Int,
    val descriptionRes: Int,
    val baseFallSeconds: Float,
    val baseSpawnMs: Long,
    val chordChance: Float,
    val maxChordSize: Int,
) {
    EASY(
        id = "keyhero_easy",
        displayNameRes = R.string.keyhero_easy_name,
        descriptionRes = R.string.keyhero_easy_desc,
        baseFallSeconds = 4.5f,
        baseSpawnMs = 1250L,
        chordChance = 0f,
        maxChordSize = 1,
    ),
    MEDIUM(
        id = "keyhero_medium",
        displayNameRes = R.string.keyhero_medium_name,
        descriptionRes = R.string.keyhero_medium_desc,
        baseFallSeconds = 3.5f,
        baseSpawnMs = 900L,
        chordChance = 0.2f,
        maxChordSize = 2,
    ),
    HARD(
        id = "keyhero_hard",
        displayNameRes = R.string.keyhero_hard_name,
        descriptionRes = R.string.keyhero_hard_desc,
        baseFallSeconds = 2.6f,
        baseSpawnMs = 700L,
        chordChance = 0.35f,
        maxChordSize = 3,
    ),
    ;

    companion object {
        fun fromId(id: String?): RhythmLevel = entries.firstOrNull { it.id == id } ?: EASY
    }
}
