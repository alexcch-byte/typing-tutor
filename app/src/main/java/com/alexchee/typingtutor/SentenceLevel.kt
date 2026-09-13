package com.alexchee.typingtutor

/**
 * A Typing Race difficulty tier: full sentences typed left-to-right, exact-case,
 * for accuracy/WPM practice rather than reflex practice. Difficulty ramps from plain
 * lowercase text up to capitals, numbers, and punctuation.
 */
enum class SentenceLevel(
    val id: String,
    val displayNameRes: Int,
    val descriptionRes: Int,
    val sentences: List<String>,
) {
    EASY(
        id = "race_easy",
        displayNameRes = R.string.race_easy_name,
        descriptionRes = R.string.race_easy_desc,
        sentences = listOf(
            "the cat sat on the mat",
            "she sells sea shells by the sea",
            "we like to play in the park",
            "my dog can run very fast",
            "the sun is big and bright",
            "birds fly high in the sky",
            "we read a fun book today",
            "the frog jumped into the pond",
        ),
    ),
    MEDIUM(
        id = "race_medium",
        displayNameRes = R.string.race_medium_name,
        descriptionRes = R.string.race_medium_desc,
        sentences = listOf(
            "The quick fox jumps over the lazy dog.",
            "Sam and Alex went to the park, then home.",
            "My favorite color is blue, but I also like green.",
            "The rain fell softly on the quiet town.",
            "We packed our bags and left early in the morning.",
            "Grandma baked cookies, and the kitchen smelled amazing.",
            "The little robot rolled across the wooden floor.",
            "Every summer, we visit the lake near the mountains.",
        ),
    ),
    HARD(
        id = "race_hard",
        displayNameRes = R.string.race_hard_name,
        descriptionRes = R.string.race_hard_desc,
        sentences = listOf(
            "Wow! Did you see that amazing trick?",
            "It's a beautiful day, isn't it?",
            "There are 7 continents and 5 oceans on Earth.",
            "Careful! Watch out for the falling branch.",
            "Can't you see we're already 10 minutes late?",
            "The rocket launched at 3:45, right on schedule!",
            "Don't forget: practice makes perfect, even on day 30.",
            "Why do owls sleep by day but hunt at night?",
        ),
    ),
    ;

    companion object {
        fun fromId(id: String?): SentenceLevel = entries.firstOrNull { it.id == id } ?: EASY
    }
}
