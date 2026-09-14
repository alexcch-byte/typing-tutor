package com.alexchee.typingtutor

/**
 * A typing practice level: which characters can fall, whether the child must
 * match the exact case (used to force Shift practice on the uppercase level),
 * and the starting difficulty curve.
 */
enum class Level(
    val id: String,
    val displayNameRes: Int,
    val descriptionRes: Int,
    val chars: List<Char>,
    val requireExactCase: Boolean,
    val baseFallSeconds: Float,
    val baseSpawnMs: Long,
    val isProgressive: Boolean = false,
) {
    STARTER_FJ(
        id = "starter_fj",
        displayNameRes = R.string.level_starter_name,
        descriptionRes = R.string.level_starter_desc,
        chars = "fj".toList(),
        requireExactCase = false,
        baseFallSeconds = 13.0f,
        baseSpawnMs = 3500L,
        isProgressive = true,
    ),
    LEFT_HAND_HOME(
        id = "left_hand_home",
        displayNameRes = R.string.level_left_hand_home_name,
        descriptionRes = R.string.level_left_hand_home_desc,
        chars = "asdf".toList(),
        requireExactCase = false,
        baseFallSeconds = 8.5f,
        baseSpawnMs = 2300L,
    ),
    RIGHT_HAND_HOME(
        id = "right_hand_home",
        displayNameRes = R.string.level_right_hand_home_name,
        descriptionRes = R.string.level_right_hand_home_desc,
        chars = "jkl;".toList(),
        requireExactCase = false,
        baseFallSeconds = 8.5f,
        baseSpawnMs = 2300L,
    ),
    HOME_ROW(
        id = "home_row",
        displayNameRes = R.string.level_home_row_name,
        descriptionRes = R.string.level_home_row_desc,
        chars = "asdfjkl;".toList(),
        requireExactCase = false,
        baseFallSeconds = 7.5f,
        baseSpawnMs = 1900L,
    ),
    TOP_ROW(
        id = "top_row",
        displayNameRes = R.string.level_top_row_name,
        descriptionRes = R.string.level_top_row_desc,
        chars = "qwertyuiop".toList(),
        requireExactCase = false,
        baseFallSeconds = 7f,
        baseSpawnMs = 1800L,
    ),
    BOTTOM_ROW(
        id = "bottom_row",
        displayNameRes = R.string.level_bottom_row_name,
        descriptionRes = R.string.level_bottom_row_desc,
        chars = "zxcvbnm,./".toList(),
        requireExactCase = false,
        baseFallSeconds = 7f,
        baseSpawnMs = 1800L,
    ),
    NUMBERS(
        id = "numbers",
        displayNameRes = R.string.level_numbers_name,
        descriptionRes = R.string.level_numbers_desc,
        chars = "0123456789".toList(),
        requireExactCase = false,
        baseFallSeconds = 6.5f,
        baseSpawnMs = 1700L,
    ),
    ALL_LETTERS(
        id = "all_letters",
        displayNameRes = R.string.level_all_letters_name,
        descriptionRes = R.string.level_all_letters_desc,
        chars = ('a'..'z').toList(),
        requireExactCase = false,
        baseFallSeconds = 6f,
        baseSpawnMs = 1500L,
    ),
    UPPERCASE(
        id = "uppercase",
        displayNameRes = R.string.level_uppercase_name,
        descriptionRes = R.string.level_uppercase_desc,
        chars = ('A'..'Z').toList(),
        requireExactCase = true,
        baseFallSeconds = 6.5f,
        baseSpawnMs = 1700L,
    ),
    MIXED(
        id = "mixed",
        displayNameRes = R.string.level_mixed_name,
        descriptionRes = R.string.level_mixed_desc,
        chars = (('a'..'z') + ('0'..'9')).toList(),
        requireExactCase = false,
        baseFallSeconds = 5.5f,
        baseSpawnMs = 1300L,
    ),
    ;

    fun getAvailableChars(score: Int): List<Char> {
        if (!isProgressive) return chars
        return when {
            score >= 600 -> "fjdskla;".toList()
            score >= 500 -> "fjdskla".toList()
            score >= 400 -> "fjdskl".toList()
            score >= 300 -> "fjdsk".toList()
            score >= 200 -> "fjdk".toList()
            score >= 100 -> "fjd".toList()
            else         -> "fj".toList()
        }
    }

    fun getTier(score: Int): Int {
        if (!isProgressive) return 0
        return (score / 100).coerceAtMost(6)
    }

    fun getTierAnnouncement(tier: Int): String? = when (tier) {
        1 -> "★ New Key: D! ★"
        2 -> "★ New Key: K! ★"
        3 -> "★ New Key: S! ★"
        4 -> "★ New Key: L! ★"
        5 -> "★ New Key: A! ★"
        6 -> "★ New Key: ; (All Home Row Unlocked!) ★"
        else -> null
    }

    companion object {
        fun fromId(id: String?): Level = entries.firstOrNull { it.id == id } ?: STARTER_FJ
    }
}
