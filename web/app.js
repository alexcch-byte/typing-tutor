'use strict';

/* =====================================================================
 * Level data — mirrors the Android app's Level.kt / WordLevel.kt /
 * SentenceLevel.kt exactly (same ids, names, timing constants, word
 * and sentence pools) so the web version plays identically.
 * ===================================================================== */

const FALLING_LEVELS = [
  { id: 'starter_fj', name: 'First Keys: F & J', desc: 'f · j index keys, adds a new key every 100 pts',
    chars: 'fj', exactCase: false, baseFallSeconds: 13.0, baseSpawnMs: 3500, progressive: true },
  { id: 'left_hand_home', name: 'Left Hand (Home)', desc: 'a s d f · left hand',
    chars: 'asdf', exactCase: false, baseFallSeconds: 8.5, baseSpawnMs: 2300 },
  { id: 'right_hand_home', name: 'Right Hand (Home)', desc: 'j k l ; · right hand',
    chars: 'jkl;', exactCase: false, baseFallSeconds: 8.5, baseSpawnMs: 2300 },
  { id: 'home_row', name: 'Home Row', desc: 'a s d f · j k l ;',
    chars: 'asdfjkl;', exactCase: false, baseFallSeconds: 7.5, baseSpawnMs: 1900 },
  { id: 'top_row', name: 'Top Row', desc: 'q w e r t · y u i o p',
    chars: 'qwertyuiop', exactCase: false, baseFallSeconds: 7, baseSpawnMs: 1800 },
  { id: 'bottom_row', name: 'Bottom Row', desc: 'z x c v b · n m , . /',
    chars: 'zxcvbnm,./', exactCase: false, baseFallSeconds: 7, baseSpawnMs: 1800 },
  { id: 'numbers', name: 'Numbers', desc: '0 1 2 3 4 5 6 7 8 9',
    chars: '0123456789', exactCase: false, baseFallSeconds: 6.5, baseSpawnMs: 1700 },
  { id: 'all_letters', name: 'All Letters', desc: 'a through z',
    chars: 'abcdefghijklmnopqrstuvwxyz', exactCase: false, baseFallSeconds: 6, baseSpawnMs: 1500 },
  { id: 'uppercase', name: 'Capital Letters', desc: 'A through Z — practice Shift',
    chars: 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', exactCase: true, baseFallSeconds: 6.5, baseSpawnMs: 1700 },
  { id: 'mixed', name: 'Mixed Challenge', desc: 'letters + numbers',
    chars: 'abcdefghijklmnopqrstuvwxyz0123456789', exactCase: false, baseFallSeconds: 5.5, baseSpawnMs: 1300 },
];

function getAvailableChars(level, score) {
  if (!level.progressive) return level.chars.split('');
  if (score >= 600) return 'fjdskla;'.split('');
  if (score >= 500) return 'fjdskla'.split('');
  if (score >= 400) return 'fjdskl'.split('');
  if (score >= 300) return 'fjdsk'.split('');
  if (score >= 200) return 'fjdk'.split('');
  if (score >= 100) return 'fjd'.split('');
  return 'fj'.split('');
}
function getTier(level, score) {
  if (!level.progressive) return 0;
  return Math.min(6, Math.floor(score / 100));
}
function getTierAnnouncement(tier) {
  return {
    1: '★ New Key: D! ★',
    2: '★ New Key: K! ★',
    3: '★ New Key: S! ★',
    4: '★ New Key: L! ★',
    5: '★ New Key: A! ★',
    6: '★ New Key: ; (All Home Row Unlocked!) ★',
  }[tier] || null;
}

const WORD_LEVELS = [
  { id: 'short_words', name: 'Short Words', desc: '3–4 letters: cat, frog, star…', baseFallSeconds: 9, baseSpawnMs: 2600,
    words: ['cat', 'dog', 'sun', 'run', 'big', 'red', 'box', 'fun', 'jam', 'bee',
      'ant', 'cow', 'pig', 'hat', 'cup', 'pen', 'bag', 'map', 'fan', 'jet',
      'kite', 'frog', 'lamp', 'milk', 'nest', 'pond', 'star', 'tree', 'fish', 'bird'] },
  { id: 'medium_words', name: 'Medium Words', desc: '5–6 letters: apple, dragon…', baseFallSeconds: 10, baseSpawnMs: 3000,
    words: ['apple', 'tiger', 'happy', 'cloud', 'dream', 'magic', 'ocean',
      'pencil', 'rocket', 'garden', 'planet', 'silver', 'wonder', 'purple',
      'monkey', 'rabbit', 'dragon', 'castle', 'forest', 'guitar', 'orange',
      'yellow', 'basket', 'bottle', 'camera', 'dinner', 'flower', 'hammer'] },
  { id: 'long_words', name: 'Long Words', desc: '7+ letters: elephant, computer…', baseFallSeconds: 12, baseSpawnMs: 3500,
    words: ['elephant', 'dinosaur', 'mountain', 'treasure', 'sandwich', 'umbrella',
      'keyboard', 'computer', 'adventure', 'butterfly', 'chocolate', 'dangerous',
      'fantastic', 'furniture', 'hamburger', 'important', 'invisible', 'jellyfish',
      'kangaroo', 'telephone', 'vegetable', 'waterfall', 'wonderful', 'xylophone', 'yesterday'] },
];

const SENTENCE_LEVELS = [
  { id: 'race_easy', name: 'Easy Sentences', desc: 'lowercase, no punctuation',
    sentences: ['the cat sat on the mat', 'she sells sea shells by the sea', 'we like to play in the park',
      'my dog can run very fast', 'the sun is big and bright', 'birds fly high in the sky',
      'we read a fun book today', 'the frog jumped into the pond'] },
  { id: 'race_medium', name: 'Medium Sentences', desc: 'capitals and punctuation',
    sentences: ['The quick fox jumps over the lazy dog.', 'Sam and Alex went to the park, then home.',
      'My favorite color is blue, but I also like green.', 'The rain fell softly on the quiet town.',
      'We packed our bags and left early in the morning.', 'Grandma baked cookies, and the kitchen smelled amazing.',
      'The little robot rolled across the wooden floor.', 'Every summer, we visit the lake near the mountains.'] },
  { id: 'race_hard', name: 'Hard Sentences', desc: 'numbers, symbols, mixed case',
    sentences: ['Wow! Did you see that amazing trick?', "It's a beautiful day, isn't it?",
      'There are 7 continents and 5 oceans on Earth.', 'Careful! Watch out for the falling branch.',
      "Can't you see we're already 10 minutes late?", 'The rocket launched at 3:45, right on schedule!',
      "Don't forget: practice makes perfect, even on day 30.", 'Why do owls sleep by day but hunt at night?'] },
];

const RHYTHM_LEVELS = [
  { id: 'keyhero_easy', name: 'Easy Beat', desc: 'slow, one note at a time',
    baseFallSeconds: 3.2, baseSpawnMs: 900, chordChance: 0, maxChordSize: 1 },
  { id: 'keyhero_medium', name: 'Medium Beat', desc: 'faster, occasional chords',
    baseFallSeconds: 2.5, baseSpawnMs: 650, chordChance: 0.2, maxChordSize: 2 },
  { id: 'keyhero_hard', name: 'Hard Beat', desc: 'fast, frequent chords',
    baseFallSeconds: 1.8, baseSpawnMs: 480, chordChance: 0.35, maxChordSize: 3 },
];

const GAME_TYPES = [
  { id: 'falling_letters', title: 'Falling Letters' },
  { id: 'word_rain', title: 'Word Rain' },
  { id: 'typing_race', title: 'Typing Race' },
  { id: 'key_hero', title: 'Key Hero' },
];

/* =====================================================================
 * Score storage — localStorage stand-in for the Android app's
 * SharedPreferences-backed ScoreStore.
 * ===================================================================== */

const ScoreStore = {
  bestValue(key) {
    const v = localStorage.getItem('tt_best_' + key);
    return v ? parseInt(v, 10) : 0;
  },
  submitValue(key, value) {
    const previous = ScoreStore.bestValue(key);
    if (value > previous) {
      localStorage.setItem('tt_best_' + key, String(value));
      return true;
    }
    return false;
  },
};

/* =====================================================================
 * Sound — Web Audio API port of GameSoundPlayer.kt: falling whistles
 * (capped at 2 concurrent), a rewarding pop on hit (pitch rises with
 * combo), and an explosion on miss.
 * ===================================================================== */

class SoundPlayer {
  constructor() {
    this.ctx = null;
    this.buffers = {};
    this.activeWhistles = [];
    this.loaded = false;
    this._loadPromise = null;
  }

  _ensureContext() {
    if (!this.ctx) {
      const Ctx = window.AudioContext || window.webkitAudioContext;
      this.ctx = new Ctx();
    }
    if (this.ctx.state === 'suspended') this.ctx.resume();
    return this.ctx;
  }

  async load() {
    if (this._loadPromise) return this._loadPromise;
    this._loadPromise = (async () => {
      const ctx = this._ensureContext();
      const files = { whistle: 'assets/falling_whistle.wav', explosion: 'assets/explosion_boom.wav', pop: 'assets/letter_pop.wav' };
      await Promise.all(Object.entries(files).map(async ([key, url]) => {
        try {
          const res = await fetch(url);
          const arr = await res.arrayBuffer();
          this.buffers[key] = await ctx.decodeAudioData(arr);
        } catch (e) {
          console.warn('Sound failed to load:', url, e);
        }
      }));
      this.loaded = true;
    })();
    return this._loadPromise;
  }

  _play(bufferKey, { rate = 1, gain = 1 } = {}) {
    if (!this.loaded || !this.buffers[bufferKey]) return null;
    const ctx = this._ensureContext();
    const source = ctx.createBufferSource();
    source.buffer = this.buffers[bufferKey];
    source.playbackRate.value = rate;
    const gainNode = ctx.createGain();
    gainNode.gain.value = gain;
    source.connect(gainNode).connect(ctx.destination);
    source.start();
    return source;
  }

  playFallingWhistle() {
    while (this.activeWhistles.length >= 2) {
      const old = this.activeWhistles.shift();
      try { old.stop(); } catch (e) { /* already stopped */ }
    }
    const rate = Math.random() * 0.08 + 0.96;
    const source = this._play('whistle', { rate, gain: 0.55 });
    if (source) this.activeWhistles.push(source);
  }

  stopOneWhistle() {
    const source = this.activeWhistles.shift();
    if (source) { try { source.stop(); } catch (e) { /* already stopped */ } }
  }

  stopAllWhistles() {
    for (const s of this.activeWhistles) { try { s.stop(); } catch (e) { /* already stopped */ } }
    this.activeWhistles = [];
  }

  playExplosion() {
    this.stopOneWhistle();
    const rate = Math.random() * 0.12 + 0.94;
    this._play('explosion', { rate, gain: 1.0 });
  }

  playPop(combo = 0) {
    this.stopOneWhistle();
    const rate = 1.0 + Math.min(combo, 10) * 0.04;
    this._play('pop', { rate, gain: 0.85 });
  }

  /** Short synthesized buzz for a mistyped key with no matching target (no dedicated asset for this). */
  playBuzz() {
    const ctx = this._ensureContext();
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();
    osc.type = 'square';
    osc.frequency.value = 160;
    gain.gain.value = 0.08;
    osc.connect(gain).connect(ctx.destination);
    osc.start();
    osc.stop(ctx.currentTime + 0.09);
  }

  pauseAll() { if (this.ctx) this.ctx.suspend(); }
  resumeAll() { if (this.ctx) this.ctx.resume(); }
}

const soundPlayer = new SoundPlayer();

/* =====================================================================
 * Small canvas helpers
 * ===================================================================== */

function roundRectPath(ctx, x, y, w, h, r) {
  ctx.beginPath();
  ctx.moveTo(x + r, y);
  ctx.arcTo(x + w, y, x + w, y + h, r);
  ctx.arcTo(x + w, y + h, x, y + h, r);
  ctx.arcTo(x, y + h, x, y, r);
  ctx.arcTo(x, y, x + w, y, r);
  ctx.closePath();
}
function clamp(v, lo, hi) { return Math.max(lo, Math.min(hi, v)); }
function pick(arr) { return arr[Math.floor(Math.random() * arr.length)]; }
function randRange(lo, hi) { return Math.random() * (hi - lo) + lo; }

const TILE_COLORS = ['#FF6B6B', '#4ECDC4', '#FFD93D', '#95E06C', '#A78BFA', '#FF9F45'];

/* =====================================================================
 * Explosion / particle system shared by Falling Letters and Word Rain.
 * ===================================================================== */

class ExplosionSystem {
  constructor() {
    this.explosions = [];
    this.particles = [];
    this.shakeIntensity = 0;
  }

  clear() {
    this.explosions = [];
    this.particles = [];
    this.shakeIntensity = 0;
  }

  trigger(x, groundY, maxRadius, spreadWidth, particleCount, shake, lifeDivisor) {
    this.shakeIntensity = shake;
    this.explosions.push({ x, groundY, life: 1, maxRadius, lifeDivisor });

    const particleColors = ['#FFFFFF', '#FFEB3B', '#FF9800', '#FF5722', '#D50000', '#757575'];
    for (let i = 0; i < particleCount; i++) {
      const speed = randRange(140, 140 + 320 * (particleCount / 22));
      const angleDeg = randRange(210, 330);
      const angleRad = angleDeg * Math.PI / 180;
      this.particles.push({
        x: x + (Math.random() - 0.5) * (spreadWidth * 0.6),
        y: groundY - Math.random() * 6,
        vx: speed * Math.cos(angleRad),
        vy: speed * Math.sin(angleRad),
        color: pick(particleColors),
        size: randRange(4, 13),
        life: 1,
        decayRate: randRange(1.3, 2.1),
      });
    }
  }

  update(dt) {
    if (this.shakeIntensity > 0) this.shakeIntensity = Math.max(0, this.shakeIntensity - dt * 32);

    this.explosions = this.explosions.filter(exp => {
      exp.life -= dt / exp.lifeDivisor;
      return exp.life > 0;
    });

    this.particles = this.particles.filter(p => {
      p.x += p.vx * dt;
      p.y += p.vy * dt;
      p.vy += 650 * dt;
      p.life -= p.decayRate * dt;
      return p.life > 0;
    });
  }

  draw(ctx, tileHeight) {
    for (const exp of this.explosions) {
      const progress = 1 - clamp(exp.life, 0, 1);
      const eased = 1 - (1 - progress) * (1 - progress);
      const currentRadius = exp.maxRadius * eased;
      const alpha = clamp(exp.life, 0, 1);

      ctx.fillStyle = `rgba(255,160,0,${(alpha * 0.7).toFixed(3)})`;
      const flashW = currentRadius * 1.8;
      const flashH = tileHeight * 0.35 * (1 - progress);
      ctx.beginPath();
      ctx.ellipse(exp.x, exp.groundY, Math.max(flashW, 0.01), Math.max(flashH, 0.01), 0, 0, Math.PI * 2);
      ctx.fill();

      ctx.fillStyle = `rgba(255,87,34,${alpha.toFixed(3)})`;
      ctx.beginPath();
      ctx.arc(exp.x, exp.groundY, Math.max(currentRadius, 0.01), Math.PI, 2 * Math.PI);
      ctx.fill();

      const innerR = currentRadius * 0.55;
      ctx.fillStyle = `rgba(255,235,59,${(alpha * 0.9).toFixed(3)})`;
      ctx.beginPath();
      ctx.arc(exp.x, exp.groundY, Math.max(innerR, 0.01), Math.PI, 2 * Math.PI);
      ctx.fill();

      const shockR = currentRadius * 1.3;
      ctx.strokeStyle = `rgba(255,224,130,${(alpha * 0.85).toFixed(3)})`;
      ctx.lineWidth = Math.max(7 * exp.life, 1);
      ctx.beginPath();
      ctx.arc(exp.x, exp.groundY, Math.max(shockR, 0.01), Math.PI, 2 * Math.PI);
      ctx.stroke();
    }

    for (const p of this.particles) {
      ctx.fillStyle = p.color;
      ctx.globalAlpha = clamp(p.life, 0, 1);
      const r = p.size * clamp(p.life, 0.2, 1);
      ctx.beginPath();
      ctx.arc(p.x, p.y, Math.max(r, 0.01), 0, Math.PI * 2);
      ctx.fill();
      ctx.globalAlpha = 1;
    }
  }
}

/* =====================================================================
 * Falling Letters game
 * ===================================================================== */

class FallingLettersGame {
  constructor(canvas) {
    this.canvas = canvas;
    this.ctx = canvas.getContext('2d');
    this.explosionSystem = new ExplosionSystem();
    this.listener = null;
    this.reset();
  }

  configure(level) {
    this.level = level;
    this.reset();
  }

  reset() {
    this.letters = [];
    this.popups = [];
    this.explosionSystem.clear();
    this.score = 0;
    this.lives = 5;
    this.combo = 0;
    this.spawnAccumulatorMs = 0;
    this.running = false;
    this.lastFrameTime = 0;
    this._fire('onScoreChanged', this.score);
    this._fire('onLivesChanged', this.lives);
    this._fire('onComboChanged', this.combo);
  }

  start() {
    if (this.running) return;
    this.running = true;
    this.lastFrameTime = 0;
    this._raf = requestAnimationFrame(this._frame.bind(this));
  }

  pause() {
    this.running = false;
    if (this._raf) cancelAnimationFrame(this._raf);
  }

  resume() { this.start(); }

  handleTypedChar(c) {
    if (!this.running) return 'IGNORED';
    let matchIndex = -1;
    let bestY = -Infinity;
    for (let i = 0; i < this.letters.length; i++) {
      const letter = this.letters[i];
      const matches = this.level.exactCase ? letter.char === c : letter.char.toLowerCase() === c.toLowerCase();
      if (matches && letter.y > bestY) { bestY = letter.y; matchIndex = i; }
    }
    if (matchIndex === -1) {
      if (this.letters.length > 0) {
        this.combo = 0;
        this._fire('onComboChanged', this.combo);
        return 'MISS_NO_MATCH';
      }
      return 'IGNORED';
    }
    const hit = this.letters.splice(matchIndex, 1)[0];
    this.combo++;
    const gained = 10 + (this.combo - 1) * 2;
    const oldTier = getTier(this.level, this.score);
    this.score += gained;
    const newTier = getTier(this.level, this.score);
    if (newTier > oldTier) {
      const announcement = getTierAnnouncement(newTier);
      if (announcement) {
        this.popups.push({ text: announcement, x: this.canvas._cssWidth / 2, y: this.canvas._cssHeight * 0.38, color: '#FFD93D', life: 1 });
      }
    }
    this._fire('onScoreChanged', this.score);
    this._fire('onComboChanged', this.combo);
    this._fire('onLetterHit', this.combo);
    this.popups.push({ text: `+${gained}`, x: hit.x, y: hit.y, color: hit.color, life: 1 });
    return 'HIT';
  }

  _frame(t) {
    if (!this.running) return;
    if (this.lastFrameTime === 0) this.lastFrameTime = t;
    const dt = clamp((t - this.lastFrameTime) / 1000, 0, 0.05);
    this.lastFrameTime = t;
    this._update(dt);
    this._draw();
    this._raf = requestAnimationFrame(this._frame.bind(this));
  }

  _update(dt) {
    const width = this.canvas._cssWidth, height = this.canvas._cssHeight;
    if (!width || !height) return;
    const tileSize = clamp(height * 0.13, 56, 100);
    this._tileSize = tileSize;

    this.spawnAccumulatorMs += dt * 1000;
    const spawnIntervalMs = this.level.progressive
      ? Math.max(2400, this.level.baseSpawnMs - Math.floor(this.score / 100) * 120)
      : Math.max(450, this.level.baseSpawnMs - this.score * 4);
    if (this.spawnAccumulatorMs >= spawnIntervalMs) {
      this.spawnAccumulatorMs = 0;
      this._spawnLetter(width, height, tileSize);
    }

    let missed = false;
    this.letters = this.letters.filter(letter => {
      letter.y += letter.fallSpeed * dt;
      if (letter.y + tileSize / 2 >= height) {
        this._triggerExplosion(letter.x, height, tileSize);
        missed = true;
        return false;
      }
      return true;
    });
    if (missed) {
      this.combo = 0;
      this.lives--;
      this._fire('onComboChanged', this.combo);
      this._fire('onLivesChanged', this.lives);
      if (this.lives <= 0) {
        this.running = false;
        if (this._raf) cancelAnimationFrame(this._raf);
        this._fire('onGameOver', this.score);
        return;
      }
    }

    this.explosionSystem.update(dt);

    this.popups = this.popups.filter(p => {
      p.y -= 60 * dt;
      p.life -= dt / 0.6;
      return p.life > 0;
    });
  }

  _triggerExplosion(x, groundY, tileSize) {
    this.explosionSystem.trigger(x, groundY, tileSize * 1.5, tileSize * 0.4, 18, 9, 0.45);
    this._fire('onLetterExploded');
  }

  _spawnLetter(width, height, tileSize) {
    const speedMultiplier = this.level.progressive
      ? Math.min(1.4, 1 + this.score / 600)
      : Math.min(2.4, 1 + this.score / 150);
    const fallSpeed = (height / this.level.baseFallSeconds) * speedMultiplier;
    const char = pick(getAvailableChars(this.level, this.score));
    const margin = tileSize;
    const x = width > margin * 2 ? randRange(margin, width - margin) : width / 2;
    const color = pick(TILE_COLORS);
    this.letters.push({ char, x, y: -tileSize, fallSpeed, color });
    this._fire('onLetterSpawned');
  }

  _draw() {
    const ctx = this.ctx, width = this.canvas._cssWidth, height = this.canvas._cssHeight;
    const tileSize = this._tileSize || 90;

    ctx.save();
    const shake = this.explosionSystem.shakeIntensity;
    if (shake > 0) {
      ctx.translate((Math.random() * 2 - 1) * shake, (Math.random() * 2 - 1) * shake);
    }

    const grad = ctx.createLinearGradient(0, 0, width, height);
    grad.addColorStop(0, '#1B1F3B');
    grad.addColorStop(1, '#3A2C5C');
    ctx.fillStyle = grad;
    ctx.fillRect(0, 0, width, height);

    ctx.textAlign = 'center';
    ctx.textBaseline = 'alphabetic';
    ctx.font = `bold ${tileSize * 0.5}px -apple-system, "Segoe UI", Roboto, sans-serif`;

    for (const letter of this.letters) {
      const half = tileSize / 2;
      ctx.fillStyle = letter.color;
      roundRectPath(ctx, letter.x - half, letter.y - half, tileSize, tileSize, 16);
      ctx.fill();
      ctx.fillStyle = '#FFFFFF';
      ctx.fillText(letter.char, letter.x, letter.y + tileSize * 0.5 * 0.35);
    }

    this.explosionSystem.draw(ctx, tileSize);

    ctx.font = 'bold 22px -apple-system, "Segoe UI", Roboto, sans-serif';
    for (const popup of this.popups) {
      ctx.fillStyle = popup.color;
      ctx.globalAlpha = clamp(popup.life, 0, 1);
      ctx.fillText(popup.text, popup.x, popup.y);
      ctx.globalAlpha = 1;
    }

    ctx.restore();
  }

  _fire(name, ...args) {
    if (this.listener && this.listener[name]) this.listener[name](...args);
  }
}

/* =====================================================================
 * Word Rain game — whole words fall; typing the first letter locks
 * that word as the target until it's finished or falls off.
 * ===================================================================== */

class WordRainGame {
  constructor(canvas) {
    this.canvas = canvas;
    this.ctx = canvas.getContext('2d');
    this.explosionSystem = new ExplosionSystem();
    this.listener = null;
    this.tileHeight = 90;
    this.horizontalPadding = 28;
    this.reset();
  }

  configure(level) {
    this.level = level;
    this.reset();
  }

  reset() {
    this.words = [];
    this.targetWord = null;
    this.popups = [];
    this.explosionSystem.clear();
    this.score = 0;
    this.lives = 5;
    this.combo = 0;
    this.spawnAccumulatorMs = 0;
    this.running = false;
    this.lastFrameTime = 0;
    this._fire('onScoreChanged', this.score);
    this._fire('onLivesChanged', this.lives);
    this._fire('onComboChanged', this.combo);
  }

  start() {
    if (this.running) return;
    this.running = true;
    this.lastFrameTime = 0;
    this._raf = requestAnimationFrame(this._frame.bind(this));
  }

  pause() {
    this.running = false;
    if (this._raf) cancelAnimationFrame(this._raf);
  }

  resume() { this.start(); }

  handleTypedChar(c) {
    if (!this.running) return 'IGNORED';

    const active = this.targetWord;
    if (active) {
      const expected = active.word[active.typedCount];
      if (expected.toLowerCase() !== c.toLowerCase()) {
        this.combo = 0;
        this._fire('onComboChanged', this.combo);
        return 'MISS_NO_MATCH';
      }
      active.typedCount++;
      if (active.typedCount >= active.word.length) this._completeWord(active);
      return 'HIT';
    }

    const match = this.words.find(w => w.word[0].toLowerCase() === c.toLowerCase());
    if (!match) {
      if (this.words.length > 0) {
        this.combo = 0;
        this._fire('onComboChanged', this.combo);
        return 'MISS_NO_MATCH';
      }
      return 'IGNORED';
    }
    match.typedCount = 1;
    if (match.typedCount >= match.word.length) this._completeWord(match);
    else this.targetWord = match;
    return 'HIT';
  }

  _completeWord(wordTile) {
    this.words = this.words.filter(w => w !== wordTile);
    if (this.targetWord === wordTile) this.targetWord = null;
    this.combo++;
    const gained = 10 + wordTile.word.length * 2 + (this.combo - 1) * 2;
    this.score += gained;
    this._fire('onScoreChanged', this.score);
    this._fire('onComboChanged', this.combo);
    this._fire('onWordCompleted', this.combo);
    this.popups.push({ text: `+${gained}`, x: wordTile.x + wordTile.tileWidth / 2, y: wordTile.y, color: wordTile.color, life: 1 });
  }

  _frame(t) {
    if (!this.running) return;
    if (this.lastFrameTime === 0) this.lastFrameTime = t;
    const dt = clamp((t - this.lastFrameTime) / 1000, 0, 0.05);
    this.lastFrameTime = t;
    this._update(dt);
    this._draw();
    this._raf = requestAnimationFrame(this._frame.bind(this));
  }

  _update(dt) {
    const width = this.canvas._cssWidth, height = this.canvas._cssHeight;
    if (!width || !height) return;

    this.spawnAccumulatorMs += dt * 1000;
    const spawnIntervalMs = Math.max(900, this.level.baseSpawnMs - this.score * 6);
    if (this.spawnAccumulatorMs >= spawnIntervalMs) {
      this.spawnAccumulatorMs = 0;
      this._spawnWord(width, height);
    }

    let missed = false;
    this.words = this.words.filter(w => {
      w.y += w.fallSpeed * dt;
      if (w.y + this.tileHeight / 2 >= height) {
        if (this.targetWord === w) this.targetWord = null;
        const centerX = w.x + w.tileWidth / 2;
        const blastRadius = clamp(w.tileWidth * 0.7, 120, 220);
        this.explosionSystem.trigger(centerX, height, blastRadius, w.tileWidth, 22, 11, 0.48);
        this._fire('onWordExploded');
        missed = true;
        return false;
      }
      return true;
    });
    if (missed) {
      this.combo = 0;
      this.lives--;
      this._fire('onComboChanged', this.combo);
      this._fire('onLivesChanged', this.lives);
      if (this.lives <= 0) {
        this.running = false;
        if (this._raf) cancelAnimationFrame(this._raf);
        this._fire('onGameOver', this.score);
        return;
      }
    }

    this.explosionSystem.update(dt);

    this.popups = this.popups.filter(p => {
      p.y -= 60 * dt;
      p.life -= dt / 0.6;
      return p.life > 0;
    });
  }

  _spawnWord(width, height) {
    const speedMultiplier = Math.min(2.2, 1 + this.score / 200);
    const fallSpeed = (height / this.level.baseFallSeconds) * speedMultiplier;
    const word = pick(this.level.words);
    this.ctx.font = 'bold 24px -apple-system, "Segoe UI", Roboto, sans-serif';
    const tileWidth = this.ctx.measureText(word).width + this.horizontalPadding * 2;
    const margin = tileWidth;
    const x = width > margin ? randRange(0, width - margin) : 0;
    const color = pick(TILE_COLORS);
    this.words.push({ word, x, y: -this.tileHeight, tileWidth, fallSpeed, color, typedCount: 0 });
    this._fire('onWordSpawned');
  }

  _draw() {
    const ctx = this.ctx, width = this.canvas._cssWidth, height = this.canvas._cssHeight;

    ctx.save();
    const shake = this.explosionSystem.shakeIntensity;
    if (shake > 0) {
      ctx.translate((Math.random() * 2 - 1) * shake, (Math.random() * 2 - 1) * shake);
    }

    const grad = ctx.createLinearGradient(0, 0, width, height);
    grad.addColorStop(0, '#1B1F3B');
    grad.addColorStop(1, '#3A2C5C');
    ctx.fillStyle = grad;
    ctx.fillRect(0, 0, width, height);

    ctx.textAlign = 'left';
    ctx.textBaseline = 'alphabetic';
    ctx.font = 'bold 24px -apple-system, "Segoe UI", Roboto, sans-serif';

    for (const w of this.words) {
      const half = this.tileHeight / 2;
      const top = w.y - half;

      ctx.fillStyle = w.color;
      roundRectPath(ctx, w.x, top, w.tileWidth, this.tileHeight, 16);
      ctx.fill();

      if (w.typedCount > 0) {
        const typedWidth = ctx.measureText(w.word.slice(0, w.typedCount)).width + this.horizontalPadding;
        ctx.fillStyle = 'rgba(255,255,255,0.35)';
        roundRectPath(ctx, w.x, top, typedWidth, this.tileHeight, 16);
        ctx.fill();
      }

      if (w === this.targetWord) {
        ctx.strokeStyle = '#FFFFFF';
        ctx.lineWidth = 3;
        roundRectPath(ctx, w.x, top, w.tileWidth, this.tileHeight, 16);
        ctx.stroke();
      }

      ctx.fillStyle = '#FFFFFF';
      ctx.fillText(w.word, w.x + this.horizontalPadding, w.y + 24 * 0.35);
    }

    this.explosionSystem.draw(ctx, this.tileHeight);

    ctx.textAlign = 'center';
    ctx.font = 'bold 22px -apple-system, "Segoe UI", Roboto, sans-serif';
    for (const popup of this.popups) {
      ctx.fillStyle = popup.color;
      ctx.globalAlpha = clamp(popup.life, 0, 1);
      ctx.fillText(popup.text, popup.x, popup.y);
      ctx.globalAlpha = 1;
    }

    ctx.restore();
  }

  _fire(name, ...args) {
    if (this.listener && this.listener[name]) this.listener[name](...args);
  }
}

/* =====================================================================
 * Key Hero — Guitar-Hero-style rhythm mode: 8 fixed lanes (one per
 * home-row key), notes scroll down toward a strike line near the
 * bottom. Hitting a lane's key while a note is near the line pops it
 * (graded Perfect/Good by timing); letting one pass detonates it.
 * ===================================================================== */

const LANE_KEYS = ['a', 's', 'd', 'f', 'j', 'k', 'l', ';'];
const LANE_COLORS = ['#FF6B6B', '#FF9F45', '#FFD93D', '#95E06C', '#4ECDC4', '#4FC3F7', '#A78BFA', '#F06292'];

class KeyHeroGame {
  constructor(canvas) {
    this.canvas = canvas;
    this.ctx = canvas.getContext('2d');
    this.explosionSystem = new ExplosionSystem();
    this.listener = null;
    this.hitLineFraction = 0.82;
    this.perfectWindowMs = 80;
    this.goodWindowMs = 160;
    this.laneFlash = new Array(LANE_KEYS.length).fill(0);
    this.reset();
  }

  configure(level) {
    this.level = level;
    this.reset();
  }

  reset() {
    this.notes = [];
    this.popups = [];
    this.explosionSystem.clear();
    this.laneFlash.fill(0);
    this.score = 0;
    this.lives = 5;
    this.combo = 0;
    this.spawnAccumulatorMs = 0;
    this.running = false;
    this.lastFrameTime = 0;
    this._fire('onScoreChanged', this.score);
    this._fire('onLivesChanged', this.lives);
    this._fire('onComboChanged', this.combo);
  }

  start() {
    if (this.running) return;
    this.running = true;
    this.lastFrameTime = 0;
    this._raf = requestAnimationFrame(this._frame.bind(this));
  }

  pause() {
    this.running = false;
    if (this._raf) cancelAnimationFrame(this._raf);
  }

  resume() { this.start(); }

  _laneForKey(c) {
    return LANE_KEYS.indexOf(c.toLowerCase());
  }

  _laneX(lane) {
    const width = this.canvas._cssWidth;
    return (width / LANE_KEYS.length) * (lane + 0.5);
  }

  handleTypedChar(c) {
    const lane = this._laneForKey(c);
    if (lane === -1 || !this.running) return 'IGNORED';

    this.laneFlash[lane] = 1;
    const hitLineY = this.canvas._cssHeight * this.hitLineFraction;
    const candidates = this.notes.filter(n => n.lane === lane);
    let best = null, bestDist = Infinity;
    for (const n of candidates) {
      const d = Math.abs(n.y - hitLineY);
      if (d < bestDist) { bestDist = d; best = n; }
    }
    if (!best) {
      this.combo = 0;
      this._fire('onComboChanged', this.combo);
      return 'MISS_NO_MATCH';
    }

    const goodWindowPx = best.fallSpeed * (this.goodWindowMs / 1000);
    if (bestDist > goodWindowPx) {
      this.combo = 0;
      this._fire('onComboChanged', this.combo);
      return 'MISS_NO_MATCH';
    }

    this.notes = this.notes.filter(n => n !== best);
    const perfectWindowPx = best.fallSpeed * (this.perfectWindowMs / 1000);
    const isPerfect = bestDist <= perfectWindowPx;
    this.combo++;
    const gained = isPerfect ? 100 + (this.combo - 1) * 5 : 50 + (this.combo - 1) * 3;
    this.score += gained;
    this._fire('onScoreChanged', this.score);
    this._fire('onComboChanged', this.combo);
    this._fire('onNoteHit', this.combo);
    this.popups.push({
      text: (isPerfect ? 'PERFECT +' : 'GOOD +') + gained,
      x: this._laneX(lane), y: hitLineY - 40, color: LANE_COLORS[lane], life: 1,
    });
    return 'HIT';
  }

  _frame(t) {
    if (!this.running) return;
    if (this.lastFrameTime === 0) this.lastFrameTime = t;
    const dt = clamp((t - this.lastFrameTime) / 1000, 0, 0.05);
    this.lastFrameTime = t;
    this._update(dt);
    this._draw();
    this._raf = requestAnimationFrame(this._frame.bind(this));
  }

  _tileSize() {
    const width = this.canvas._cssWidth, height = this.canvas._cssHeight;
    const laneWidth = width / LANE_KEYS.length;
    return clamp(Math.min(laneWidth * 0.82, height * 0.09), 32, 200);
  }

  _update(dt) {
    const width = this.canvas._cssWidth, height = this.canvas._cssHeight;
    if (!width || !height) return;
    const hitLineY = height * this.hitLineFraction;
    const tileSize = this._tileSize();

    this.spawnAccumulatorMs += dt * 1000;
    const spawnIntervalMs = Math.max(300, this.level.baseSpawnMs - this.score * 1.2);
    if (this.spawnAccumulatorMs >= spawnIntervalMs) {
      this.spawnAccumulatorMs = 0;
      this._spawnNotes(height, tileSize);
    }

    let missed = false;
    this.notes = this.notes.filter(note => {
      note.y += note.fallSpeed * dt;
      const goodWindowPx = note.fallSpeed * (this.goodWindowMs / 1000);
      if (note.y > hitLineY + goodWindowPx) {
        this.explosionSystem.trigger(this._laneX(note.lane), hitLineY, tileSize * 1.4, tileSize * 0.4, 16, 8, 0.45);
        this._fire('onNoteMissed');
        missed = true;
        return false;
      }
      return true;
    });
    if (missed) {
      this.combo = 0;
      this.lives--;
      this._fire('onComboChanged', this.combo);
      this._fire('onLivesChanged', this.lives);
      if (this.lives <= 0) {
        this.running = false;
        if (this._raf) cancelAnimationFrame(this._raf);
        this._fire('onGameOver', this.score);
        return;
      }
    }

    this.explosionSystem.update(dt);
    for (let i = 0; i < this.laneFlash.length; i++) {
      if (this.laneFlash[i] > 0) this.laneFlash[i] = Math.max(0, this.laneFlash[i] - dt * 4);
    }

    this.popups = this.popups.filter(p => {
      p.y -= 50 * dt;
      p.life -= dt / 0.6;
      return p.life > 0;
    });
  }

  _spawnNotes(height, tileSize) {
    const speedMultiplier = Math.min(1.5, 1 + this.score / 500);
    const fallSpeed = (height / this.level.baseFallSeconds) * speedMultiplier;

    const eligibleLanes = [];
    for (let lane = 0; lane < LANE_KEYS.length; lane++) {
      const blocked = this.notes.some(n => n.lane === lane && n.y < tileSize * 2.2);
      if (!blocked) eligibleLanes.push(lane);
    }
    if (eligibleLanes.length === 0) return;

    let chordSize = 1;
    if (this.level.maxChordSize > 1 && Math.random() < this.level.chordChance) {
      chordSize = 2 + Math.floor(Math.random() * (this.level.maxChordSize - 1));
    }
    chordSize = Math.min(chordSize, eligibleLanes.length);

    const shuffled = eligibleLanes.sort(() => Math.random() - 0.5).slice(0, chordSize);
    for (const lane of shuffled) {
      this.notes.push({ lane, y: -tileSize, fallSpeed });
    }
  }

  _draw() {
    const ctx = this.ctx, width = this.canvas._cssWidth, height = this.canvas._cssHeight;
    const hitLineY = height * this.hitLineFraction;
    const tileSize = this._tileSize();
    const laneWidth = width / LANE_KEYS.length;

    ctx.save();
    const shake = this.explosionSystem.shakeIntensity;
    if (shake > 0) ctx.translate((Math.random() * 2 - 1) * shake, (Math.random() * 2 - 1) * shake);

    const grad = ctx.createLinearGradient(0, 0, width, height);
    grad.addColorStop(0, '#1B1F3B');
    grad.addColorStop(1, '#3A2C5C');
    ctx.fillStyle = grad;
    ctx.fillRect(0, 0, width, height);

    ctx.strokeStyle = 'rgba(255,255,255,0.16)';
    ctx.lineWidth = 2;
    for (let i = 1; i < LANE_KEYS.length; i++) {
      const x = laneWidth * i;
      ctx.beginPath();
      ctx.moveTo(x, 0);
      ctx.lineTo(x, height);
      ctx.stroke();
    }

    ctx.strokeStyle = '#FFFFFF';
    ctx.lineWidth = 5;
    ctx.beginPath();
    ctx.moveTo(0, hitLineY);
    ctx.lineTo(width, hitLineY);
    ctx.stroke();

    ctx.textAlign = 'center';
    ctx.textBaseline = 'alphabetic';
    const fontSize = tileSize * 0.42;
    ctx.font = `bold ${fontSize}px -apple-system, "Segoe UI", Roboto, sans-serif`;

    for (let lane = 0; lane < LANE_KEYS.length; lane++) {
      const cx = this._laneX(lane);
      const half = tileSize / 2;
      const flash = this.laneFlash[lane];
      ctx.globalAlpha = clamp((70 + flash * 140) / 255, 0, 1);
      ctx.fillStyle = LANE_COLORS[lane];
      roundRectPath(ctx, cx - half, hitLineY - half, tileSize, tileSize, 10);
      ctx.fill();
      ctx.globalAlpha = 1;
      ctx.fillStyle = '#FFFFFF';
      ctx.fillText(LANE_KEYS[lane], cx, hitLineY + fontSize * 0.35);
    }

    for (const note of this.notes) {
      const cx = this._laneX(note.lane);
      const half = tileSize / 2;
      ctx.fillStyle = LANE_COLORS[note.lane];
      roundRectPath(ctx, cx - half, note.y - half, tileSize, tileSize, 12);
      ctx.fill();
      ctx.fillStyle = '#FFFFFF';
      ctx.fillText(LANE_KEYS[note.lane], cx, note.y + fontSize * 0.35);
    }

    this.explosionSystem.draw(ctx, tileSize);

    ctx.font = 'bold 20px -apple-system, "Segoe UI", Roboto, sans-serif';
    for (const popup of this.popups) {
      ctx.fillStyle = popup.color;
      ctx.globalAlpha = clamp(popup.life, 0, 1);
      ctx.fillText(popup.text, popup.x, popup.y);
      ctx.globalAlpha = 1;
    }

    ctx.restore();
  }

  _fire(name, ...args) {
    if (this.listener && this.listener[name]) this.listener[name](...args);
  }
}

/* =====================================================================
 * Typing Race — DOM-based sentence typing with WPM/accuracy tracking.
 * A wrong key does not advance the cursor; the child must correct it,
 * which builds accuracy rather than just speed.
 * ===================================================================== */

class TypingRaceGame {
  constructor(els) {
    this.els = els;
    this.sentencesPerSession = 4;
    this.hasStarted = false;
    this.accumulatedMs = 0;
    this.runStart = null;
    this._tickInterval = null;
  }

  startNewSession(level) {
    this.level = level;
    this.sentences = [...level.sentences].sort(() => Math.random() - 0.5).slice(0, this.sentencesPerSession);
    this.sentenceIndex = 0;
    this.typedIndex = 0;
    this.totalCorrect = 0;
    this.totalMistakes = 0;
    this.accumulatedMs = 0;
    this.runStart = null;
    this.hasStarted = false;
    this.els.progress.textContent = `Sentence 1 of ${this.sentences.length}`;
    this._renderSentence('cursor');
    this._updateHud();
  }

  onResume() {
    if (this.hasStarted) {
      this.runStart = performance.now();
      this._startTicking();
    }
  }

  onPause() {
    if (this.runStart !== null) {
      this.accumulatedMs += performance.now() - this.runStart;
      this.runStart = null;
    }
    this._stopTicking();
  }

  _startTicking() {
    this._stopTicking();
    this._tickInterval = setInterval(() => this._updateHud(), 100);
  }
  _stopTicking() {
    if (this._tickInterval) clearInterval(this._tickInterval);
    this._tickInterval = null;
  }

  handleChar(c) {
    if (!this.hasStarted) {
      this.hasStarted = true;
      this.onResume();
    }
    const sentence = this.sentences[this.sentenceIndex];
    if (this.typedIndex < sentence.length && sentence[this.typedIndex] === c) {
      this.typedIndex++;
      this.totalCorrect++;
      soundPlayer.playPop(0);
      if (this.typedIndex >= sentence.length) {
        this._advanceSentence();
      } else {
        this._renderSentence('cursor');
      }
    } else {
      this.totalMistakes++;
      soundPlayer.playBuzz();
      this._flashMistake();
    }
    this._updateHud();
  }

  _advanceSentence() {
    this.sentenceIndex++;
    this.typedIndex = 0;
    if (this.sentenceIndex >= this.sentences.length) {
      this._finishSession();
    } else {
      this.els.progress.textContent = `Sentence ${this.sentenceIndex + 1} of ${this.sentences.length}`;
      this._renderSentence('cursor');
    }
  }

  _finishSession() {
    this.onPause();
    const wpm = this.currentWpm();
    const accuracy = this.currentAccuracy();
    const isNewBest = ScoreStore.submitValue('tr_' + this.level.id, wpm);
    if (this.onSessionComplete) this.onSessionComplete(wpm, accuracy, isNewBest);
  }

  _flashMistake() {
    this._renderSentence('mistake');
    setTimeout(() => {
      if (this.sentenceIndex < this.sentences.length) this._renderSentence('cursor');
    }, 150);
  }

  _renderSentence(cursorMode) {
    const sentence = this.sentences[this.sentenceIndex];
    const typed = sentence.slice(0, this.typedIndex);
    const cursorChar = sentence[this.typedIndex] || '';
    const rest = sentence.slice(this.typedIndex + 1);
    this.els.sentence.innerHTML =
      `<span class="typed">${escapeHtml(typed)}</span>` +
      (cursorChar ? `<span class="cursor${cursorMode === 'mistake' ? ' mistake' : ''}">${escapeHtml(cursorChar)}</span>` : '') +
      `${escapeHtml(rest)}`;
  }

  currentElapsedMs() {
    return this.accumulatedMs + (this.runStart !== null ? performance.now() - this.runStart : 0);
  }
  currentWpm() {
    if (!this.hasStarted) return 0;
    const minutes = Math.max(this.currentElapsedMs(), 1) / 60000;
    return Math.round((this.totalCorrect / 5) / minutes);
  }
  currentAccuracy() {
    const total = this.totalCorrect + this.totalMistakes;
    return total > 0 ? Math.round((this.totalCorrect / total) * 100) : 100;
  }

  _updateHud() {
    this.els.wpm.textContent = `${this.currentWpm()} WPM`;
    this.els.accuracy.textContent = `${this.currentAccuracy()}% acc`;
    this.els.time.textContent = `${(this.currentElapsedMs() / 1000).toFixed(1)}s`;
  }
}

function escapeHtml(str) {
  return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

/* =====================================================================
 * App shell — navigation, home screen, canvas sizing, keyboard routing,
 * and pause/game-over state machine (mirrors BaseGameActivity.kt).
 * ===================================================================== */

const App = {
  selectedGame: 'falling_letters',
  activeScreen: 'home', // 'home' | 'falling_letters' | 'word_rain' | 'typing_race'
  isPaused: false,
  isSessionOver: false,

  els: {},

  init() {
    this.els = {
      homeScreen: document.getElementById('homeScreen'),
      gameTabs: document.getElementById('gameTabs'),
      levelList: document.getElementById('levelList'),

      gameScreen: document.getElementById('gameScreen'),
      canvas: document.getElementById('gameCanvas'),
      hudLevel: document.getElementById('hudLevel'),
      hudCombo: document.getElementById('hudCombo'),
      hudScore: document.getElementById('hudScore'),
      hudLives: document.getElementById('hudLives'),
      pauseButton: document.getElementById('pauseButton'),
      pauseOverlay: document.getElementById('pauseOverlay'),
      resumeButton: document.getElementById('resumeButton'),
      pauseHomeButton: document.getElementById('pauseHomeButton'),
      gameOverOverlay: document.getElementById('gameOverOverlay'),
      finalScoreLabel: document.getElementById('finalScoreLabel'),
      newBestLabel: document.getElementById('newBestLabel'),
      retryButton: document.getElementById('retryButton'),
      gameOverHomeButton: document.getElementById('gameOverHomeButton'),

      raceScreen: document.getElementById('raceScreen'),
      raceHudLevel: document.getElementById('raceHudLevel'),
      raceHudTime: document.getElementById('raceHudTime'),
      raceHudAccuracy: document.getElementById('raceHudAccuracy'),
      raceHudWpm: document.getElementById('raceHudWpm'),
      racePauseButton: document.getElementById('racePauseButton'),
      raceProgressLabel: document.getElementById('raceProgressLabel'),
      sentenceText: document.getElementById('sentenceText'),
      racePauseOverlay: document.getElementById('racePauseOverlay'),
      raceResumeButton: document.getElementById('raceResumeButton'),
      racePauseHomeButton: document.getElementById('racePauseHomeButton'),
      sessionCompleteOverlay: document.getElementById('sessionCompleteOverlay'),
      sessionSummaryLabel: document.getElementById('sessionSummaryLabel'),
      raceNewBestLabel: document.getElementById('raceNewBestLabel'),
      raceRetryButton: document.getElementById('raceRetryButton'),
      sessionCompleteHomeButton: document.getElementById('sessionCompleteHomeButton'),
    };

    this.canvasGame = new FallingLettersGame(this.els.canvas);
    this.wordRainGame = new WordRainGame(this.els.canvas);
    this.keyHeroGame = new KeyHeroGame(this.els.canvas);
    this.raceGame = new TypingRaceGame({
      progress: this.els.raceProgressLabel,
      sentence: this.els.sentenceText,
      wpm: this.els.raceHudWpm,
      accuracy: this.els.raceHudAccuracy,
      time: this.els.raceHudTime,
    });

    this._wireHomeScreen();
    this._wireCanvasGameControls();
    this._wireRaceControls();
    this._wireResize();
    window.addEventListener('keydown', this._onKeyDown.bind(this));

    this.showHome();
  },

  /* ---------- Home screen ---------- */

  _wireHomeScreen() {
    this.els.gameTabs.innerHTML = '';
    for (const game of GAME_TYPES) {
      const tab = document.createElement('button');
      tab.className = 'tab' + (game.id === this.selectedGame ? ' selected' : '');
      tab.textContent = game.title;
      tab.addEventListener('click', () => {
        if (this.selectedGame !== game.id) {
          this.selectedGame = game.id;
          this._wireHomeScreen();
          this._buildLevelList();
        }
      });
      this.els.gameTabs.appendChild(tab);
    }
    this._buildLevelList();
  },

  _buildLevelList() {
    this.els.levelList.innerHTML = '';
    let levels, prefix, bestFormat, onSelect;
    if (this.selectedGame === 'falling_letters') {
      levels = FALLING_LEVELS; prefix = 'ft_';
      bestFormat = v => `Best: ${v}`;
      onSelect = level => this.startFallingLetters(level);
    } else if (this.selectedGame === 'word_rain') {
      levels = WORD_LEVELS; prefix = 'wr_';
      bestFormat = v => `Best: ${v}`;
      onSelect = level => this.startWordRain(level);
    } else if (this.selectedGame === 'typing_race') {
      levels = SENTENCE_LEVELS; prefix = 'tr_';
      bestFormat = v => `Best: ${v} WPM`;
      onSelect = level => this.startTypingRace(level);
    } else {
      levels = RHYTHM_LEVELS; prefix = 'kh_';
      bestFormat = v => `Best: ${v}`;
      onSelect = level => this.startKeyHero(level);
    }

    for (const level of levels) {
      const card = document.createElement('button');
      card.className = 'level-card';
      const best = ScoreStore.bestValue(prefix + level.id);
      card.innerHTML = `
        <div class="level-card-text">
          <p class="level-name">${escapeHtml(level.name)}</p>
          <p class="level-desc">${escapeHtml(level.desc)}</p>
        </div>
        <span class="level-best">${bestFormat(best)}</span>
        <span class="level-play-icon"></span>
      `;
      card.addEventListener('click', () => { soundPlayer.load(); onSelect(level); });
      this.els.levelList.appendChild(card);
    }
  },

  showHome() {
    this.canvasGame.pause();
    this.wordRainGame.pause();
    this.keyHeroGame.pause();
    this.raceGame.onPause();
    this.activeScreen = 'home';
    this.els.homeScreen.hidden = false;
    this.els.gameScreen.hidden = true;
    this.els.raceScreen.hidden = true;
    this._buildLevelList();
  },

  /* ---------- Falling Letters / Word Rain shared plumbing ---------- */

  _wireCanvasGameControls() {
    const listener = {
      onScoreChanged: s => { this.els.hudScore.textContent = `Score: ${s}`; },
      onLivesChanged: l => { this.els.hudLives.textContent = `Lives: ${l}`; },
      onComboChanged: c => { this.els.hudCombo.textContent = c > 1 ? `Combo x${c}` : ''; },
      onGameOver: score => this._onCanvasGameOver(score),
      onLetterSpawned: () => soundPlayer.playFallingWhistle(),
      onLetterHit: combo => soundPlayer.playPop(combo),
      onLetterExploded: () => soundPlayer.playExplosion(),
      onWordSpawned: () => soundPlayer.playFallingWhistle(),
      onWordCompleted: combo => soundPlayer.playPop(combo),
      onWordExploded: () => soundPlayer.playExplosion(),
      onNoteHit: combo => soundPlayer.playPop(combo),
      onNoteMissed: () => soundPlayer.playExplosion(),
    };
    this.canvasGame.listener = listener;
    this.wordRainGame.listener = listener;
    this.keyHeroGame.listener = listener;

    this.els.pauseButton.addEventListener('click', () => this.showPause());
    this.els.resumeButton.addEventListener('click', () => this.hidePause());
    this.els.pauseHomeButton.addEventListener('click', () => this.showHome());
    this.els.gameOverHomeButton.addEventListener('click', () => this.showHome());
    this.els.retryButton.addEventListener('click', () => this._retryCanvasGame());
  },

  _activeCanvasGame() {
    if (this.activeScreen === 'word_rain') return this.wordRainGame;
    if (this.activeScreen === 'key_hero') return this.keyHeroGame;
    return this.canvasGame;
  },

  startFallingLetters(level) {
    soundPlayer.stopAllWhistles();
    this.activeScreen = 'falling_letters';
    this.isPaused = false;
    this.isSessionOver = false;
    this.els.homeScreen.hidden = true;
    this.els.raceScreen.hidden = true;
    this.els.gameScreen.hidden = false;
    this.els.pauseOverlay.hidden = true;
    this.els.gameOverOverlay.hidden = true;
    this.els.hudLevel.textContent = level.name;
    this._resizeCanvas();
    this.canvasGame.configure(level);
    this.canvasGame.start();
  },

  startWordRain(level) {
    soundPlayer.stopAllWhistles();
    this.activeScreen = 'word_rain';
    this.isPaused = false;
    this.isSessionOver = false;
    this.els.homeScreen.hidden = true;
    this.els.raceScreen.hidden = true;
    this.els.gameScreen.hidden = false;
    this.els.pauseOverlay.hidden = true;
    this.els.gameOverOverlay.hidden = true;
    this.els.hudLevel.textContent = level.name;
    this._resizeCanvas();
    this.wordRainGame.configure(level);
    this.wordRainGame.start();
  },

  startKeyHero(level) {
    soundPlayer.stopAllWhistles();
    this.activeScreen = 'key_hero';
    this.isPaused = false;
    this.isSessionOver = false;
    this.els.homeScreen.hidden = true;
    this.els.raceScreen.hidden = true;
    this.els.gameScreen.hidden = false;
    this.els.pauseOverlay.hidden = true;
    this.els.gameOverOverlay.hidden = true;
    this.els.hudLevel.textContent = level.name;
    this._resizeCanvas();
    this.keyHeroGame.configure(level);
    this.keyHeroGame.start();
  },

  _retryCanvasGame() {
    soundPlayer.stopAllWhistles();
    this.isSessionOver = false;
    this.isPaused = false;
    this.els.gameOverOverlay.hidden = true;
    const game = this._activeCanvasGame();
    game.reset();
    game.start();
  },

  _onCanvasGameOver(score) {
    soundPlayer.stopAllWhistles();
    this.isSessionOver = true;
    const prefix = { word_rain: 'wr_', key_hero: 'kh_', falling_letters: 'ft_' }[this.activeScreen];
    const level = this._activeCanvasGame().level;
    const isNewBest = ScoreStore.submitValue(prefix + level.id, score);
    this.els.finalScoreLabel.textContent = `Final score: ${score}`;
    this.els.newBestLabel.hidden = !isNewBest;
    this.els.gameOverOverlay.hidden = false;
  },

  showPause() {
    if (this.isSessionOver || this.isPaused) return;
    this.isPaused = true;
    if (this.activeScreen === 'typing_race') {
      this.raceGame.onPause();
      this.els.racePauseOverlay.hidden = false;
    } else {
      this._activeCanvasGame().pause();
      soundPlayer.pauseAll();
      this.els.pauseOverlay.hidden = false;
    }
  },

  hidePause() {
    if (!this.isPaused) return;
    this.isPaused = false;
    if (this.activeScreen === 'typing_race') {
      this.raceGame.onResume();
      this.els.racePauseOverlay.hidden = true;
    } else {
      this._activeCanvasGame().resume();
      soundPlayer.resumeAll();
      this.els.pauseOverlay.hidden = true;
    }
  },

  /* ---------- Typing Race ---------- */

  _wireRaceControls() {
    this.els.racePauseButton.addEventListener('click', () => this.showPause());
    this.els.raceResumeButton.addEventListener('click', () => this.hidePause());
    this.els.racePauseHomeButton.addEventListener('click', () => this.showHome());
    this.els.sessionCompleteHomeButton.addEventListener('click', () => this.showHome());
    this.els.raceRetryButton.addEventListener('click', () => this._retryRace());

    this.raceGame.onSessionComplete = (wpm, accuracy, isNewBest) => {
      this.isSessionOver = true;
      this.els.sessionSummaryLabel.textContent = `${wpm} WPM · ${accuracy}% accuracy`;
      this.els.raceNewBestLabel.hidden = !isNewBest;
      this.els.sessionCompleteOverlay.hidden = false;
    };
  },

  startTypingRace(level) {
    this.activeScreen = 'typing_race';
    this.isPaused = false;
    this.isSessionOver = false;
    this.els.homeScreen.hidden = true;
    this.els.gameScreen.hidden = true;
    this.els.raceScreen.hidden = false;
    this.els.racePauseOverlay.hidden = true;
    this.els.sessionCompleteOverlay.hidden = true;
    this.els.raceHudLevel.textContent = level.name;
    this.raceGame.startNewSession(level);
  },

  _retryRace() {
    this.isSessionOver = false;
    this.isPaused = false;
    this.els.sessionCompleteOverlay.hidden = true;
    this.raceGame.startNewSession(this.raceGame.level);
  },

  /* ---------- Keyboard routing ---------- */

  _onKeyDown(e) {
    if (this.activeScreen === 'home') return;

    if (e.key === 'Escape') {
      if (this.isSessionOver) this.showHome();
      else if (this.isPaused) this.hidePause();
      else this.showPause();
      e.preventDefault();
      return;
    }

    if (this.isPaused || this.isSessionOver) return;
    if (e.key.length !== 1) return; // ignore modifier/navigation keys

    if (this.activeScreen === 'typing_race') {
      this.raceGame.handleChar(e.key);
      e.preventDefault();
      return;
    }

    const game = this._activeCanvasGame();
    const c = e.key;
    let allowed;
    if (this.activeScreen === 'word_rain') allowed = /^[a-zA-Z]$/.test(c);
    else if (this.activeScreen === 'key_hero') allowed = /^[a-zA-Z;]$/.test(c);
    else allowed = /^[a-zA-Z0-9;,./]$/.test(c);
    if (!allowed) return;

    const result = game.handleTypedChar(c);
    if (result === 'MISS_NO_MATCH') soundPlayer.playBuzz();
    if (result !== 'IGNORED') e.preventDefault();
  },

  /* ---------- Canvas sizing ---------- */

  _wireResize() {
    window.addEventListener('resize', () => this._resizeCanvas());
  },

  _resizeCanvas() {
    const canvas = this.els.canvas;
    const rect = canvas.parentElement.getBoundingClientRect();
    const dpr = window.devicePixelRatio || 1;
    canvas.width = Math.round(rect.width * dpr);
    canvas.height = Math.round(rect.height * dpr);
    canvas.style.width = rect.width + 'px';
    canvas.style.height = rect.height + 'px';
    const ctx = canvas.getContext('2d');
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
    canvas._cssWidth = rect.width;
    canvas._cssHeight = rect.height;
  },
};

document.addEventListener('DOMContentLoaded', () => App.init());
