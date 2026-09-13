package com.alexchee.typingtutor.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.Choreographer
import android.view.View
import com.alexchee.typingtutor.WordLevel
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

/**
 * Falling-word variant of [FallingLettersView]: whole words drift down and the
 * player types them letter by letter. Only one word can be "targeted" at a time —
 * typing its first letter locks it in, and every falling word not yet targeted is
 * ignored until that one is finished or missed, mirroring classic word-typing games.
 */
class WordRainView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs), Choreographer.FrameCallback {

    enum class TypeResult { HIT, MISS_NO_MATCH, IGNORED }

    interface Listener {
        fun onScoreChanged(score: Int)
        fun onLivesChanged(lives: Int)
        fun onComboChanged(combo: Int)
        fun onGameOver(finalScore: Int)
        fun onWordExploded() {}
        fun onWordSpawned() {}
        fun onWordCompleted(combo: Int) {}
    }

    var listener: Listener? = null

    private val words = mutableListOf<FallingWord>()
    private var targetWord: FallingWord? = null
    private val popups = mutableListOf<Popup>()
    private val wordColors = intArrayOf(
        Color.parseColor("#FF6B6B"),
        Color.parseColor("#4ECDC4"),
        Color.parseColor("#FFD93D"),
        Color.parseColor("#95E06C"),
        Color.parseColor("#A78BFA"),
        Color.parseColor("#FF9F45"),
    )

    private var level: WordLevel = WordLevel.SHORT_WORDS
    private var score = 0
    private var lives = 5
    private var combo = 0
    private var running = false
    private var spawnAccumulatorMs = 0L
    private var lastFrameNanos = 0L

    private val startingLives = 5
    private val tileHeight = 90f
    private val horizontalPadding = 28f

    private val tilePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        alpha = 90
    }
    private val targetOutlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        color = Color.WHITE
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.LEFT
        isFakeBoldText = true
        textSize = 46f
    }
    private val popupPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
        textSize = 42f
    }
    private val bgPaint = Paint()
    private val tileRect = RectF()
    private val progressRect = RectF()

    private val shockwavePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val fireballOuterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#FF5722")
    }
    private val fireballInnerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#FFEB3B")
    }
    private val groundFlashPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#FFA000")
    }
    private val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val explosionRect = RectF()

    private val explosions = mutableListOf<Explosion>()
    private val particles = mutableListOf<ExplosionParticle>()
    private var shakeIntensity = 0f

    private data class Popup(val text: String, var x: Float, var y: Float, val color: Int, var life: Float = 1f)
    private data class Explosion(val x: Float, val groundY: Float, var life: Float = 1f, val maxRadius: Float)
    private data class ExplosionParticle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        val color: Int,
        val size: Float,
        var life: Float = 1f,
        val decayRate: Float,
    )

    fun configure(newLevel: WordLevel) {
        level = newLevel
        reset()
    }

    fun reset() {
        words.clear()
        targetWord = null
        popups.clear()
        explosions.clear()
        particles.clear()
        shakeIntensity = 0f
        score = 0
        lives = startingLives
        combo = 0
        spawnAccumulatorMs = 0L
        listener?.onScoreChanged(score)
        listener?.onLivesChanged(lives)
        listener?.onComboChanged(combo)
        invalidate()
    }

    fun start() {
        if (running) return
        running = true
        lastFrameNanos = 0L
        Choreographer.getInstance().postFrameCallback(this)
    }

    fun pause() {
        running = false
        Choreographer.getInstance().removeFrameCallback(this)
    }

    fun resume() = start()

    fun handleTypedChar(c: Char): TypeResult {
        if (!running) return TypeResult.IGNORED

        val active = targetWord
        if (active != null) {
            val expected = active.word[active.typedCount]
            if (!expected.equals(c, ignoreCase = true)) {
                combo = 0
                listener?.onComboChanged(combo)
                return TypeResult.MISS_NO_MATCH
            }
            active.typedCount++
            if (active.typedCount >= active.word.length) {
                completeWord(active)
            }
            return TypeResult.HIT
        }

        val match = words.firstOrNull { it.word[0].equals(c, ignoreCase = true) }
        if (match == null) {
            if (words.isNotEmpty()) {
                combo = 0
                listener?.onComboChanged(combo)
                return TypeResult.MISS_NO_MATCH
            }
            return TypeResult.IGNORED
        }
        match.typedCount = 1
        if (match.typedCount >= match.word.length) {
            completeWord(match)
        } else {
            targetWord = match
        }
        return TypeResult.HIT
    }

    private fun completeWord(wordTile: FallingWord) {
        words.remove(wordTile)
        if (targetWord === wordTile) targetWord = null
        combo++
        val gained = 10 + wordTile.word.length * 2 + (combo - 1) * 2
        score += gained
        listener?.onScoreChanged(score)
        listener?.onComboChanged(combo)
        listener?.onWordCompleted(combo)
        popups.add(Popup("+$gained", wordTile.x + wordTile.tileWidth / 2, wordTile.y, wordTile.color))
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        bgPaint.shader = LinearGradient(
            0f, 0f, w.toFloat(), h.toFloat(),
            Color.parseColor("#1B1F3B"), Color.parseColor("#3A2C5C"),
            Shader.TileMode.CLAMP,
        )
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (!running) return
        if (lastFrameNanos == 0L) lastFrameNanos = frameTimeNanos
        val dt = ((frameTimeNanos - lastFrameNanos) / 1_000_000_000.0).toFloat().coerceIn(0f, 0.05f)
        lastFrameNanos = frameTimeNanos

        update(dt)
        invalidate()

        Choreographer.getInstance().postFrameCallback(this)
    }

    private fun update(dt: Float) {
        if (width == 0 || height == 0) return

        spawnAccumulatorMs += (dt * 1000).toLong()
        val spawnIntervalMs = max(900L, level.baseSpawnMs - score * 6L)
        if (spawnAccumulatorMs >= spawnIntervalMs) {
            spawnAccumulatorMs = 0L
            spawnWord()
        }

        val iterator = words.iterator()
        var missed = false
        while (iterator.hasNext()) {
            val wordTile = iterator.next()
            wordTile.y += wordTile.fallSpeedPxPerSec * dt
            if (wordTile.y + tileHeight / 2 >= height) {
                if (targetWord === wordTile) targetWord = null
                val centerX = wordTile.x + wordTile.tileWidth / 2f
                val blastRadius = (wordTile.tileWidth * 0.7f).coerceIn(120f, 220f)
                iterator.remove()
                triggerExplosion(centerX, height.toFloat(), blastRadius, wordTile.tileWidth)
                missed = true
            }
        }
        if (missed) {
            combo = 0
            lives--
            listener?.onComboChanged(combo)
            listener?.onLivesChanged(lives)
            if (lives <= 0) {
                running = false
                Choreographer.getInstance().removeFrameCallback(this)
                listener?.onGameOver(score)
                return
            }
        }

        if (shakeIntensity > 0f) {
            shakeIntensity = (shakeIntensity - dt * 32f).coerceAtLeast(0f)
        }

        val expIter = explosions.iterator()
        while (expIter.hasNext()) {
            val exp = expIter.next()
            exp.life -= dt / 0.48f
            if (exp.life <= 0f) expIter.remove()
        }

        val partIter = particles.iterator()
        while (partIter.hasNext()) {
            val p = partIter.next()
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.vy += 650f * dt
            p.life -= p.decayRate * dt
            if (p.life <= 0f) partIter.remove()
        }

        val popupIterator = popups.iterator()
        while (popupIterator.hasNext()) {
            val popup = popupIterator.next()
            popup.y -= 60f * dt
            popup.life -= dt / 0.6f
            if (popup.life <= 0f) popupIterator.remove()
        }
    }

    private fun triggerExplosion(x: Float, groundY: Float, maxRadius: Float, spreadWidth: Float) {
        shakeIntensity = 11f
        explosions.add(Explosion(x = x, groundY = groundY, life = 1f, maxRadius = maxRadius))

        val particleColors = intArrayOf(
            Color.parseColor("#FFFFFF"),
            Color.parseColor("#FFEB3B"),
            Color.parseColor("#FF9800"),
            Color.parseColor("#FF5722"),
            Color.parseColor("#D50000"),
            Color.parseColor("#757575"),
        )

        for (i in 0 until 22) {
            val speed = Random.nextFloat() * 340f + 150f
            val angleDeg = Random.nextFloat() * 120f + 210f
            val angleRad = Math.toRadians(angleDeg.toDouble())
            val vx = (speed * cos(angleRad)).toFloat()
            val vy = (speed * sin(angleRad)).toFloat()
            val color = particleColors.random(Random)
            val size = Random.nextFloat() * 9f + 5f
            val decay = Random.nextFloat() * 0.8f + 1.3f
            particles.add(
                ExplosionParticle(
                    x = x + (Random.nextFloat() - 0.5f) * (spreadWidth * 0.6f),
                    y = groundY - Random.nextFloat() * 6f,
                    vx = vx,
                    vy = vy,
                    color = color,
                    size = size,
                    life = 1f,
                    decayRate = decay,
                ),
            )
        }
        listener?.onWordExploded()
    }

    private fun spawnWord() {
        val speedMultiplier = (1f + score / 200f).coerceAtMost(2.2f)
        val fallSpeed = (height / level.baseFallSeconds) * speedMultiplier
        val word = level.words.random(Random)
        val tileWidth = textPaint.measureText(word) + horizontalPadding * 2
        val margin = tileWidth
        val x = if (width > margin) Random.nextFloat() * (width - margin) else 0f
        val color = wordColors.random(Random)
        words.add(FallingWord(word, x, -tileHeight, tileWidth, fallSpeed, color))
        listener?.onWordSpawned()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save()
        if (shakeIntensity > 0f) {
            val dx = (Random.nextFloat() * 2f - 1f) * shakeIntensity
            val dy = (Random.nextFloat() * 2f - 1f) * shakeIntensity
            canvas.translate(dx, dy)
        }

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        for (wordTile in words) {
            tilePaint.color = wordTile.color
            val half = tileHeight / 2f
            tileRect.set(wordTile.x, wordTile.y - half, wordTile.x + wordTile.tileWidth, wordTile.y + half)
            canvas.drawRoundRect(tileRect, 20f, 20f, tilePaint)

            if (wordTile.typedCount > 0) {
                val typedWidth = textPaint.measureText(wordTile.word.take(wordTile.typedCount)) + horizontalPadding
                progressRect.set(tileRect.left, tileRect.top, tileRect.left + typedWidth, tileRect.bottom)
                canvas.drawRoundRect(progressRect, 20f, 20f, progressPaint)
            }

            if (wordTile === targetWord) {
                canvas.drawRoundRect(tileRect, 20f, 20f, targetOutlinePaint)
            }

            canvas.drawText(
                wordTile.word,
                wordTile.x + horizontalPadding,
                wordTile.y + textPaint.textSize * 0.35f,
                textPaint,
            )
        }

        for (exp in explosions) {
            val progress = 1f - exp.life.coerceIn(0f, 1f)
            val eased = 1f - (1f - progress) * (1f - progress)
            val currentRadius = exp.maxRadius * eased
            val alpha = (exp.life.coerceIn(0f, 1f) * 255).toInt()

            groundFlashPaint.alpha = (alpha * 0.7f).toInt()
            val flashWidth = currentRadius * 1.8f
            val flashHeight = tileHeight * 0.4f * (1f - progress)
            explosionRect.set(exp.x - flashWidth, exp.groundY - flashHeight, exp.x + flashWidth, exp.groundY + flashHeight)
            canvas.drawOval(explosionRect, groundFlashPaint)

            fireballOuterPaint.alpha = alpha
            explosionRect.set(exp.x - currentRadius, exp.groundY - currentRadius, exp.x + currentRadius, exp.groundY + currentRadius)
            canvas.drawArc(explosionRect, 180f, 180f, true, fireballOuterPaint)

            val innerRadius = currentRadius * 0.55f
            fireballInnerPaint.alpha = (alpha * 0.9f).toInt()
            explosionRect.set(exp.x - innerRadius, exp.groundY - innerRadius, exp.x + innerRadius, exp.groundY + innerRadius)
            canvas.drawArc(explosionRect, 180f, 180f, true, fireballInnerPaint)

            val shockRadius = currentRadius * 1.3f
            shockwavePaint.strokeWidth = (7f * exp.life).coerceAtLeast(1f)
            shockwavePaint.color = Color.parseColor("#FFE082")
            shockwavePaint.alpha = (alpha * 0.85f).toInt()
            explosionRect.set(exp.x - shockRadius, exp.groundY - shockRadius, exp.x + shockRadius, exp.groundY + shockRadius)
            canvas.drawArc(explosionRect, 180f, 180f, false, shockwavePaint)
        }

        for (p in particles) {
            particlePaint.color = p.color
            particlePaint.alpha = (p.life.coerceIn(0f, 1f) * 255).toInt()
            val r = p.size * p.life.coerceIn(0.2f, 1f)
            canvas.drawCircle(p.x, p.y, r, particlePaint)
        }

        for (popup in popups) {
            popupPaint.color = popup.color
            popupPaint.alpha = (popup.life.coerceIn(0f, 1f) * 255).toInt()
            canvas.drawText(popup.text, popup.x, popup.y, popupPaint)
        }

        canvas.restore()
    }
}
