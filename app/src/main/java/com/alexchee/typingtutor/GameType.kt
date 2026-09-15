package com.alexchee.typingtutor

/** The four practice modes on the home screen, each with its own level list. */
enum class GameType(val id: String, val titleRes: Int, val descriptionRes: Int) {
    FALLING_LETTERS(
        id = "falling_letters",
        titleRes = R.string.game_falling_letters,
        descriptionRes = R.string.game_falling_letters_desc,
    ),
    WORD_RAIN(
        id = "word_rain",
        titleRes = R.string.game_word_rain,
        descriptionRes = R.string.game_word_rain_desc,
    ),
    TYPING_RACE(
        id = "typing_race",
        titleRes = R.string.game_typing_race,
        descriptionRes = R.string.game_typing_race_desc,
    ),
    KEY_HERO(
        id = "key_hero",
        titleRes = R.string.game_key_hero,
        descriptionRes = R.string.game_key_hero_desc,
    ),
}
