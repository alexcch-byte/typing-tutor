package com.alexchee.typingtutor.game

/** A single word tile currently falling down the screen. */
data class FallingWord(
    val word: String,
    var x: Float,
    var y: Float,
    val tileWidth: Float,
    val fallSpeedPxPerSec: Float,
    val color: Int,
    var typedCount: Int = 0,
)
