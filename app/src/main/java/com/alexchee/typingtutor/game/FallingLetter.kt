package com.alexchee.typingtutor.game

/** A single letter tile currently falling down the screen. */
data class FallingLetter(
    val char: Char,
    var x: Float,
    var y: Float,
    val fallSpeedPxPerSec: Float,
    val color: Int,
    var scale: Float = 1f,
)
