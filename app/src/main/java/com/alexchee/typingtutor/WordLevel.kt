package com.alexchee.typingtutor

/** A Word Rain difficulty tier: a curated pool of kid-friendly words of similar length. */
enum class WordLevel(
    val id: String,
    val displayNameRes: Int,
    val descriptionRes: Int,
    val words: List<String>,
    val baseFallSeconds: Float,
    val baseSpawnMs: Long,
) {
    SHORT_WORDS(
        id = "short_words",
        displayNameRes = R.string.word_short_name,
        descriptionRes = R.string.word_short_desc,
        words = listOf(
            "cat", "dog", "sun", "run", "big", "red", "box", "fun", "jam", "bee",
            "ant", "cow", "pig", "hat", "cup", "pen", "bag", "map", "fan", "jet",
            "kite", "frog", "lamp", "milk", "nest", "pond", "star", "tree", "fish", "bird",
        ),
        baseFallSeconds = 9f,
        baseSpawnMs = 2600L,
    ),
    MEDIUM_WORDS(
        id = "medium_words",
        displayNameRes = R.string.word_medium_name,
        descriptionRes = R.string.word_medium_desc,
        words = listOf(
            "apple", "tiger", "happy", "cloud", "dream", "magic", "ocean",
            "pencil", "rocket", "garden", "planet", "silver", "wonder", "purple",
            "monkey", "rabbit", "dragon", "castle", "forest", "guitar", "orange",
            "yellow", "basket", "bottle", "camera", "dinner", "flower", "hammer",
        ),
        baseFallSeconds = 10f,
        baseSpawnMs = 3000L,
    ),
    LONG_WORDS(
        id = "long_words",
        displayNameRes = R.string.word_long_name,
        descriptionRes = R.string.word_long_desc,
        words = listOf(
            "elephant", "dinosaur", "mountain", "treasure", "sandwich", "umbrella",
            "keyboard", "computer", "adventure", "butterfly", "chocolate", "dangerous",
            "fantastic", "furniture", "hamburger", "important", "invisible", "jellyfish",
            "kangaroo", "telephone", "vegetable", "waterfall", "wonderful", "xylophone", "yesterday",
        ),
        baseFallSeconds = 12f,
        baseSpawnMs = 3500L,
    ),
    ;

    companion object {
        fun fromId(id: String?): WordLevel = entries.firstOrNull { it.id == id } ?: SHORT_WORDS
    }
}
