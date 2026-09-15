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
import com.alexchee.typingtutor.RhythmLevel
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/**
 * Guitar-Hero-style rhythm mode: 8 fixed lanes, one per home-row key, each with
 * a note tile scrolling down toward a strike line near the bottom. Pressing a
 * lane's key while a note is near the line pops it (graded Perfect/Good by how
 * close); letting one pass the line detonates it, just like a missed bomb in
 * the other games.
 */
class KeyHeroView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs), Choreographer.FrameCallback {

    enum class TypeResult { HIT, MISS_NO_MATCH, IGNORED }

    interface Listener {
        fun onScoreChanged(score: Int)
        fun onLivesChanged(lives: Int)
        fun onComboChanged(combo: Int)
        fun onGameOver(finalScore: Int)
        fun onNoteHit(combo: Int) {}
        fun onNoteMissed() {}
    }

    var listener: Listener? = null

    private val laneKeys = charArrayOf('a', 's', 'd', 'f', 'j', 'k', 'l', ';')
    private val laneColors = intArrayOf(
        Color.parseColor("#FF6B6B"),
        Color.parseColor("#FF9F45"),
        Color.parseColor("#FFD93D"),
        Color.parseColor("#95E06C"),
        Color.parseColor("#4ECDC4"),
        Color.parseColor("#4FC3F7"),
        Color.parseColor("#A78BFA"),
        Color.parseColor("#F06292"),
    )
    private val laneCount get() = laneKeys.size

    private data class Note(val lane: Int, var y: Float, val fallSpeed: Float)
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

    private val notes = mutableListOf<Note>()
    private val popups = mutableListOf<Popup>()
    private val explosions = mutableListOf<Explosion>()
    private val particles = mutableListOf<ExplosionParticle>()
    private var shakeIntensity = 0f
    private val laneFlash = FloatArray(8)

    private var level: RhythmLevel = RhythmLevel.EASY
    private var score = 0
    private var lives = 5
    private var combo = 0
    private var running = false
    private var spawnAccumulatorMs = 0L
    private var lastFrameNanos = 0L

    private val startingLives = 5
    private val hitLineFraction = 0.90f
    private val perfectWindowMs = 80f
    private val goodWindowMs = 160f

    private val tilePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val chipPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val laneLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(40, 255, 255, 255)
        strokeWidth = 2f
    }
    private val hitLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFFFFF")
        strokeWidth = 5f
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val popupPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
        textSize = 36f
    }
    private val bgPaint = Paint()
    private val tileRect = RectF()

    private val shockwavePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
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
    private val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val explosionRect = RectF()

    fun configure(newLevel: RhythmLevel) {
        level = newLevel
        reset()
    }

    fun reset() {
        notes.clear()
        popups.clear()
        explosions.clear()
        particles.clear()
        shakeIntensity = 0f
        for (i in laneFlash.indices) laneFlash[i] = 0f
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

    private fun laneForKey(c: Char): Int = laneKeys.indexOfFirst { it.equals(c, ignoreCase = true) }

    /** Called by the Activity for every printable key the child types. */
    fun handleTypedChar(c: Char): TypeResult {
        val lane = laneForKey(c)
        if (lane == -1) return TypeResult.IGNORED
        if (!running) return TypeResult.IGNORED

        laneFlash[lane] = 1f
        val hitLineY = height * hitLineFraction
        val candidate = notes.filter { it.lane == lane }.minByOrNull { abs(it.y - hitLineY) }
        if (candidate == null) {
            combo = 0
            listener?.onComboChanged(combo)
            return TypeResult.MISS_NO_MATCH
        }

        val distPx = abs(candidate.y - hitLineY)
        val goodWindowPx = candidate.fallSpeed * (goodWindowMs / 1000f)
        if (distPx > goodWindowPx) {
            combo = 0
            listener?.onComboChanged(combo)
            return TypeResult.MISS_NO_MATCH
        }

        notes.remove(candidate)
        val perfectWindowPx = candidate.fallSpeed * (perfectWindowMs / 1000f)
        val isPerfect = distPx <= perfectWindowPx
        combo++
        val gained = if (isPerfect) 100 + (combo - 1) * 5 else 50 + (combo - 1) * 3
        score += gained
        listener?.onScoreChanged(score)
        listener?.onComboChanged(combo)
        listener?.onNoteHit(combo)
        val label = if (isPerfect) "PERFECT +$gained" else "GOOD +$gained"
        popups.add(Popup(label, laneX(lane), hitLineY - 40f, laneColors[lane]))
        return TypeResult.HIT
    }

    private fun laneX(lane: Int): Float = (width.toFloat() / laneCount) * (lane + 0.5f)

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
        val hitLineY = height * hitLineFraction
        val tileSize = tileSize()

        spawnAccumulatorMs += (dt * 1000).toLong()
        val spawnIntervalMs = max(300L, level.baseSpawnMs - (score * 1.2f).toLong())
        if (spawnAccumulatorMs >= spawnIntervalMs) {
            spawnAccumulatorMs = 0L
            spawnNotes(tileSize)
        }

        var missed = false
        val iterator = notes.iterator()
        while (iterator.hasNext()) {
            val note = iterator.next()
            note.y += note.fallSpeed * dt
            val goodWindowPx = note.fallSpeed * (goodWindowMs / 1000f)
            if (note.y > hitLineY + goodWindowPx) {
                iterator.remove()
                triggerExplosion(laneX(note.lane), hitLineY, tileSize)
                listener?.onNoteMissed()
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

        if (shakeIntensity > 0f) shakeIntensity = (shakeIntensity - dt * 32f).coerceAtLeast(0f)
        for (i in laneFlash.indices) if (laneFlash[i] > 0f) laneFlash[i] = (laneFlash[i] - dt * 4f).coerceAtLeast(0f)

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
            popup.y -= 50f * dt
            popup.life -= dt / 0.6f
            if (popup.life <= 0f) popupIterator.remove()
        }
    }

    private fun tileSize(): Float {
        val laneWidth = width / laneCount.toFloat()
        return min(laneWidth * 0.82f, height * 0.09f).coerceAtLeast(32f)
    }

    private fun spawnNotes(tileSize: Float) {
        val speedMultiplier = min(1.3f, 1f + score / 800f)
        val fallSpeed = (height / level.baseFallSeconds) * speedMultiplier

        val eligibleLanes = (0 until laneCount).filter { lane ->
            notes.none { it.lane == lane && it.y < tileSize * 2.2f }
        }
        if (eligibleLanes.isEmpty()) return

        var chordSize = 1
        if (level.maxChordSize > 1 && Random.nextFloat() < level.chordChance) {
            chordSize = Random.nextInt(2, level.maxChordSize + 1)
        }
        chordSize = min(chordSize, eligibleLanes.size)

        val chosen = eligibleLanes.shuffled(Random).take(chordSize)
        for (lane in chosen) {
            notes.add(Note(lane, -tileSize, fallSpeed))
        }
    }

    private fun triggerExplosion(x: Float, groundY: Float, tileSize: Float) {
        shakeIntensity = 8f
        explosions.add(Explosion(x = x, groundY = groundY, life = 1f, maxRadius = tileSize * 1.4f))

        val particleColors = intArrayOf(
            Color.parseColor("#FFFFFF"), Color.parseColor("#FFEB3B"), Color.parseColor("#FF9800"),
            Color.parseColor("#FF5722"), Color.parseColor("#D50000"), Color.parseColor("#757575"),
        )
        for (i in 0 until 16) {
            val speed = Random.nextFloat() * 300f + 130f
            val angleDeg = Random.nextFloat() * 120f + 210f
            val angleRad = Math.toRadians(angleDeg.toDouble())
            particles.add(
                ExplosionParticle(
                    x = x + (Random.nextFloat() - 0.5f) * (tileSize * 0.4f),
                    y = groundY - Random.nextFloat() * 6f,
                    vx = (speed * cos(angleRad)).toFloat(),
                    vy = (speed * sin(angleRad)).toFloat(),
                    color = particleColors.random(Random),
                    size = Random.nextFloat() * 7f + 4f,
                    life = 1f,
                    decayRate = Random.nextFloat() * 0.8f + 1.4f,
                ),
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save()
        if (shakeIntensity > 0f) {
            canvas.translate(
                (Random.nextFloat() * 2f - 1f) * shakeIntensity,
                (Random.nextFloat() * 2f - 1f) * shakeIntensity,
            )
        }

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val hitLineY = height * hitLineFraction
        val tileSize = tileSize()
        val laneWidth = width / laneCount.toFloat()

        for (i in 1 until laneCount) {
            val x = laneWidth * i
            canvas.drawLine(x, 0f, x, height.toFloat(), laneLinePaint)
        }
        canvas.drawLine(0f, hitLineY, width.toFloat(), hitLineY, hitLinePaint)

        textPaint.textSize = tileSize * 0.42f
        for (lane in 0 until laneCount) {
            val cx = laneX(lane)
            val half = tileSize / 2f
            val flash = laneFlash[lane]
            chipPaint.color = laneColors[lane]
            chipPaint.alpha = (70 + flash * 140).toInt().coerceIn(0, 255)
            tileRect.set(cx - half, hitLineY - half, cx + half, hitLineY + half)
            canvas.drawRoundRect(tileRect, 12f, 12f, chipPaint)
            textPaint.alpha = 255
            canvas.drawText(laneKeys[lane].toString(), cx, hitLineY + textPaint.textSize * 0.35f, textPaint)
        }

        textPaint.alpha = 255
        for (note in notes) {
            val cx = laneX(note.lane)
            val half = tileSize / 2f
            tilePaint.color = laneColors[note.lane]
            tileRect.set(cx - half, note.y - half, cx + half, note.y + half)
            canvas.drawRoundRect(tileRect, 14f, 14f, tilePaint)
            canvas.drawText(laneKeys[note.lane].toString(), cx, note.y + textPaint.textSize * 0.35f, textPaint)
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
            canvas.drawCircle(p.x, p.y, p.size * p.life.coerceIn(0.2f, 1f), particlePaint)
        }

        for (popup in popups) {
            popupPaint.color = popup.color
            popupPaint.alpha = (popup.life.coerceIn(0f, 1f) * 255).toInt()
            canvas.drawText(popup.text, popup.x, popup.y, popupPaint)
        }

        canvas.restore()
    }
}
