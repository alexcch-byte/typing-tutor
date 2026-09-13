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
import com.alexchee.typingtutor.Level
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

/**
 * Custom-drawn game surface for the "Falling Letters" typing game. Letters spawn at
 * the top and drift down; the player types the character on a keyboard to pop it
 * before it reaches the bottom. Driven by a Choreographer frame loop rather than a
 * separate render thread since the letter count is small and this keeps input
 * handling (which lives on the Activity) trivially thread-safe.
 */
class FallingLettersView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs), Choreographer.FrameCallback {

    enum class TypeResult { HIT, MISS_NO_MATCH, IGNORED }

    interface Listener {
        fun onScoreChanged(score: Int)
        fun onLivesChanged(lives: Int)
        fun onComboChanged(combo: Int)
        fun onGameOver(finalScore: Int)
        fun onLetterExploded() {}
        fun onLetterSpawned() {}
        fun onLetterHit(combo: Int) {}
    }

    var listener: Listener? = null

    private val letters = mutableListOf<FallingLetter>()
    private val popups = mutableListOf<Popup>()
    private val letterColors = intArrayOf(
        Color.parseColor("#FF6B6B"),
        Color.parseColor("#4ECDC4"),
        Color.parseColor("#FFD93D"),
        Color.parseColor("#95E06C"),
        Color.parseColor("#A78BFA"),
        Color.parseColor("#FF9F45"),
    )

    private var level: Level = Level.LEFT_HAND_HOME
    private var score = 0
    private var lives = 5
    private var combo = 0
    private var running = false
    private var spawnAccumulatorMs = 0L
    private var lastFrameNanos = 0L

    private val startingLives = 5

    private val tilePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val popupPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
        textSize = 42f
    }
    private val bgPaint = Paint()
    private var bgShader: Shader? = null

    private val tileSize = 96f
    private val tileRect = RectF()

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

    fun configure(newLevel: Level) {
        level = newLevel
        reset()
    }

    fun reset() {
        letters.clear()
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

    fun resume() {
        start()
    }

    /** Called by the Activity for every printable key the child types. */
    fun handleTypedChar(c: Char): TypeResult {
        if (!running) return TypeResult.IGNORED
        val matchIndex = letters.indices
            .filter { i ->
                val letter = letters[i]
                if (level.requireExactCase) letter.char == c else letter.char.equals(c, ignoreCase = true)
            }
            .maxByOrNull { letters[it].y } ?: -1
        if (matchIndex == -1) {
            if (letters.isNotEmpty()) {
                combo = 0
                listener?.onComboChanged(combo)
                return TypeResult.MISS_NO_MATCH
            }
            return TypeResult.IGNORED
        }
        val hit = letters.removeAt(matchIndex)
        combo++
        val gained = 10 + (combo - 1) * 2
        score += gained
        listener?.onScoreChanged(score)
        listener?.onComboChanged(combo)
        listener?.onLetterHit(combo)
        popups.add(Popup("+$gained", hit.x, hit.y, hit.color))
        return TypeResult.HIT
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        bgShader = LinearGradient(
            0f, 0f, w.toFloat(), h.toFloat(),
            Color.parseColor("#1B1F3B"), Color.parseColor("#3A2C5C"),
            Shader.TileMode.CLAMP,
        )
        bgPaint.shader = bgShader
        textPaint.textSize = tileSize * 0.5f
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (!running) return
        if (lastFrameNanos == 0L) lastFrameNanos = frameTimeNanos
        val dtSeconds = ((frameTimeNanos - lastFrameNanos) / 1_000_000_000.0).toFloat().coerceIn(0f, 0.05f)
        lastFrameNanos = frameTimeNanos

        update(dtSeconds)
        invalidate()

        Choreographer.getInstance().postFrameCallback(this)
    }

    private fun update(dt: Float) {
        if (width == 0 || height == 0) return

        spawnAccumulatorMs += (dt * 1000).toLong()
        val spawnIntervalMs = max(450L, level.baseSpawnMs - score * 4L)
        if (spawnAccumulatorMs >= spawnIntervalMs) {
            spawnAccumulatorMs = 0L
            spawnLetter()
        }

        val iterator = letters.iterator()
        var missed = false
        while (iterator.hasNext()) {
            val letter = iterator.next()
            letter.y += letter.fallSpeedPxPerSec * dt
            if (letter.y + tileSize / 2 >= height) {
                iterator.remove()
                triggerExplosion(letter.x, height.toFloat())
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
            exp.life -= dt / 0.45f
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

    private fun triggerExplosion(x: Float, groundY: Float) {
        shakeIntensity = 9f
        explosions.add(Explosion(x = x, groundY = groundY, life = 1f, maxRadius = tileSize * 1.5f))

        val particleColors = intArrayOf(
            Color.parseColor("#FFFFFF"),
            Color.parseColor("#FFEB3B"),
            Color.parseColor("#FF9800"),
            Color.parseColor("#FF5722"),
            Color.parseColor("#D50000"),
            Color.parseColor("#757575"),
        )

        for (i in 0 until 18) {
            val speed = Random.nextFloat() * 320f + 140f
            val angleDeg = Random.nextFloat() * 120f + 210f
            val angleRad = Math.toRadians(angleDeg.toDouble())
            val vx = (speed * cos(angleRad)).toFloat()
            val vy = (speed * sin(angleRad)).toFloat()
            val color = particleColors.random(Random)
            val size = Random.nextFloat() * 8f + 4f
            val decay = Random.nextFloat() * 0.8f + 1.4f
            particles.add(
                ExplosionParticle(
                    x = x + (Random.nextFloat() - 0.5f) * (tileSize * 0.4f),
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
        listener?.onLetterExploded()
    }

    private fun spawnLetter() {
        val speedMultiplier = (1f + score / 150f).coerceAtMost(2.4f)
        val fallSpeed = (height / level.baseFallSeconds) * speedMultiplier
        val char = level.chars.random(Random)
        val margin = tileSize
        val x = if (width > margin * 2) Random.nextFloat() * (width - margin * 2) + margin else width / 2f
        val color = letterColors.random(Random)
        letters.add(FallingLetter(char, x, -tileSize, fallSpeed, color))
        listener?.onLetterSpawned()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save()
        if (shakeIntensity > 0f) {
            val dx = (Random.nextFloat() * 2f - 1f) * shakeIntensity
            val dy = (Random.nextFloat() * 2f - 1f) * shakeIntensity
            canvas.translate(dx, dy)
        }

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        for (letter in letters) {
            tilePaint.color = letter.color
            val half = tileSize / 2f
            tileRect.set(letter.x - half, letter.y - half, letter.x + half, letter.y + half)
            canvas.drawRoundRect(tileRect, 20f, 20f, tilePaint)
            canvas.drawText(
                letter.char.toString(),
                letter.x,
                letter.y + textPaint.textSize * 0.35f,
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
            val flashHeight = tileSize * 0.35f * (1f - progress)
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
