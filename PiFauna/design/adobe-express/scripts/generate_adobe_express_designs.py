import os
import subprocess

OUT_SVG_DIR = "/home/swarna-sekhar-dhar/projects/smallandroidapp/PiFauna/design/adobe-express/svg"
OUT_PNG_DIR = "/home/swarna-sekhar-dhar/projects/smallandroidapp/PiFauna/design/adobe-express/png"
os.makedirs(OUT_SVG_DIR, exist_ok=True)
os.makedirs(OUT_PNG_DIR, exist_ok=True)

# Common SVG frame template 1080 x 1920
def make_svg(title, content):
    return f"""<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1080 1920" width="1080" height="1920">
  <defs>
    <style>
      @import url('https://fonts.googleapis.com/css2?family=Cinzel:wght@600;700&amp;family=Inter:wght@400;500;600;700&amp;display=swap');
      text {{ font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; }}
      .title {{ font-family: 'Cinzel', serif; font-weight: 700; fill: #F4EEFF; letter-spacing: 1.5px; }}
      .heading {{ font-weight: 700; fill: #F4EEFF; }}
      .subhead {{ font-weight: 400; fill: #BEB6CA; }}
      .label {{ font-weight: 600; fill: #DEC588; letter-spacing: 1.2px; font-size: 26px; }}
      .card-title {{ font-weight: 600; fill: #F4EEFF; font-size: 34px; }}
      .card-sub {{ font-weight: 400; fill: #BEB6CA; font-size: 26px; }}
      .btn-text {{ font-weight: 700; fill: #0B0716; font-size: 30px; }}
      .btn-text-outline {{ font-weight: 600; fill: #E8C872; font-size: 30px; }}
    </style>
  </defs>

  <!-- Canvas Background -->
  <rect width="1080" height="1920" fill="#0B0716"/>
  
  <!-- Subtle Header ambient glow -->
  <circle cx="540" cy="180" r="300" fill="#241B35" opacity="0.4" filter="blur(60px)"/>

  <!-- Top App Bar -->
  <g transform="translate(60, 100)">
    <text x="0" y="40" class="label">LAYASWARNA</text>
    <text x="0" y="110" class="title" font-size="52">{title}</text>
  </g>

  <!-- Screen Content -->
  {content}

  <!-- Bottom Navigation Bar (Persistent 3 tabs) -->
  <g transform="translate(0, 1780)">
    <rect width="1080" height="140" fill="#140E24" opacity="0.95"/>
    <line x1="0" y1="0" x2="1080" y2="0" stroke="#2B213F" stroke-width="2"/>
    
    <!-- Home Tab -->
    <g transform="translate(180, 50)" text-anchor="middle">
      <circle cx="0" cy="15" r="8" fill="#E8C872"/>
      <text x="0" y="55" fill="#E8C872" font-size="24" font-weight="600">Home</text>
    </g>

    <!-- Profiles Tab -->
    <g transform="translate(540, 50)" text-anchor="middle">
      <circle cx="0" cy="15" r="8" fill="#BEB6CA"/>
      <text x="0" y="55" fill="#BEB6CA" font-size="24" font-weight="500">Profiles</text>
    </g>

    <!-- Settings Tab -->
    <g transform="translate(900, 50)" text-anchor="middle">
      <circle cx="0" cy="15" r="8" fill="#BEB6CA"/>
      <text x="0" y="55" fill="#BEB6CA" font-size="24" font-weight="500">Settings</text>
    </g>
  </g>
</svg>"""

screens = {}

# 1. Welcome
screens["01_welcome.svg"] = make_svg("Welcome to Sanctuary", """
  <g transform="translate(60, 320)">
    <rect width="960" height="320" rx="28" fill="#241B35" stroke="#3A2E50" stroke-width="2"/>
    <text x="50" y="75" class="card-title">Acoustic neuromodulation &amp; meditation</text>
    <text x="50" y="130" class="card-sub">Layaswarna produces quiet, bounded acoustic tones</text>
    <text x="50" y="170" class="card-sub">designed for mindful relaxation, experimental pest inquiry,</text>
    <text x="50" y="210" class="card-sub">and personal vocal mantra repetition.</text>
    <text x="50" y="270" fill="#E8C872" font-size="24" font-weight="600">Built-in phone speaker only · Fully offline</text>
  </g>

  <!-- Lotus Emblem -->
  <g transform="translate(540, 840)">
    <circle cx="0" cy="0" r="160" fill="#1C142B" stroke="#DEC588" stroke-width="3"/>
    <path d="M 0 -100 C 40 -40, 90 -20, 0 80 C -90 -20, -40 -40, 0 -100 Z" fill="#DEC588" opacity="0.85"/>
    <circle cx="0" cy="0" r="28" fill="#0B0716"/>
  </g>

  <g transform="translate(60, 1140)">
    <rect width="960" height="240" rx="24" fill="#1B1428" stroke="#312547" stroke-width="1.5"/>
    <text x="50" y="65" class="card-title" font-size="30">Verified hearing safety</text>
    <text x="50" y="115" class="card-sub">Continuous 30-minute hard session limit.</text>
    <text x="50" y="155" class="card-sub">Digital amplitude locked under 8% with 60% media gate.</text>
    <text x="50" y="195" class="card-sub">Headphones, earbuds, and hearing aids are strictly blocked.</text>
  </g>

  <g transform="translate(60, 1480)">
    <rect width="960" height="110" rx="30" fill="#E8C872"/>
    <text x="480" y="70" text-anchor="middle" class="btn-text">Get started</text>
  </g>
""")

# 2. Home Radial
screens["02_home_radial.svg"] = make_svg("Sound Profile Regulator", """
  <g transform="translate(60, 260)">
    <text x="0" y="30" class="subhead" font-size="30">Tap or rotate to select an acoustic profile</text>
  </g>

  <!-- Central Radial Selector -->
  <g transform="translate(540, 680)">
    <!-- Outer ring -->
    <circle cx="0" cy="0" r="320" fill="#171026" stroke="#2F2347" stroke-width="3"/>
    <circle cx="0" cy="0" r="230" fill="#1F1633" stroke="#DEC588" stroke-width="4" stroke-dasharray="12 12"/>
    
    <!-- Hub readout -->
    <circle cx="0" cy="0" r="140" fill="#241B35" stroke="#E8C872" stroke-width="3"/>
    <text x="0" y="-15" text-anchor="middle" class="label" font-size="22">ACTIVE BAND</text>
    <text x="0" y="35" text-anchor="middle" class="heading" font-size="44">Mosquito</text>
    <text x="0" y="75" text-anchor="middle" fill="#BEB6CA" font-size="22">16–20 kHz</text>

    <!-- Profile Nodes -->
    <circle cx="0" cy="-230" r="36" fill="#E8C872"/>
    <circle cx="218" cy="-71" r="32" fill="#352B49" stroke="#52436D"/>
    <circle cx="135" cy="186" r="32" fill="#352B49" stroke="#52436D"/>
    <circle cx="-135" cy="186" r="32" fill="#352B49" stroke="#52436D"/>
    <circle cx="-218" cy="-71" r="32" fill="#352B49" stroke="#52436D"/>
  </g>

  <!-- Action button -->
  <g transform="translate(60, 1100)">
    <rect width="960" height="96" rx="26" fill="#E8C872"/>
    <text x="480" y="60" text-anchor="middle" class="btn-text">Open Mosquito Profile</text>
  </g>

  <!-- Meditation cards -->
  <g transform="translate(60, 1240)">
    <text x="0" y="30" class="label">MEDITATION &amp; CHANTS</text>

    <g transform="translate(0, 60)">
      <rect width="960" height="120" rx="22" fill="#241B35" stroke="#3A2E50"/>
      <text x="40" y="55" class="card-title">Seven Chakra Library</text>
      <text x="40" y="95" class="card-sub">Symbolic tone associations and listening insights</text>
    </g>

    <g transform="translate(0, 205)">
      <rect width="960" height="120" rx="22" fill="#241B35" stroke="#3A2E50"/>
      <text x="40" y="55" class="card-title">Anahata Nada</text>
      <text x="40" y="95" class="card-sub">Experimental relaxation sound studio</text>
    </g>

    <g transform="translate(0, 350)">
      <rect width="960" height="120" rx="22" fill="#2D2145" stroke="#E8C872" stroke-width="2"/>
      <text x="40" y="55" class="card-title" fill="#F4EEFF">Mantra Studio</text>
      <text x="40" y="95" class="card-sub">Record, retry and save your chant · repeat 11, 21, 58, 108 times</text>
    </g>
  </g>
""")

# 3. Mantra Studio Entry & Library
screens["03_mantra_studio_entry.svg"] = make_svg("Mantra Studio", """
  <g transform="translate(60, 260)">
    <text x="0" y="30" class="subhead" font-size="30">Record your own chant · replay in counted sets</text>
  </g>

  <!-- Explainer Hero Card -->
  <g transform="translate(60, 340)">
    <rect width="960" height="190" rx="24" fill="#241B35" stroke="#3A2E50"/>
    <text x="40" y="60" class="card-title">Your voice, your practice</text>
    <text x="40" y="110" class="card-sub">Record as many attempts as you like. Only your chosen take is saved.</text>
    <text x="40" y="150" class="card-sub">Recordings remain strictly private on this device; nothing is uploaded.</text>
  </g>

  <!-- Record Prompt -->
  <g transform="translate(60, 580)">
    <text x="0" y="30" class="label">RECORD</text>
    <rect y="50" width="960" height="150" rx="24" fill="#1F172E" stroke="#312547"/>
    <text x="40" y="110" class="card-title">New recording</text>
    <text x="40" y="155" class="card-sub">Use a quiet room and speak at a comfortable level.</text>

    <rect y="225" width="960" height="96" rx="24" fill="#E8C872"/>
    <text x="480" y="285" text-anchor="middle" class="btn-text">Record a mantra</text>
  </g>

  <!-- My Recordings Section -->
  <g transform="translate(60, 970)">
    <text x="0" y="30" class="label">MY RECORDINGS (1)</text>

    <!-- Recording Item -->
    <g transform="translate(0, 60)">
      <rect width="960" height="140" rx="24" fill="#241B35" stroke="#E8C872" stroke-width="2"/>
      <text x="40" y="60" class="card-title">Daily Gayatri Mantra</text>
      <text x="40" y="105" class="card-sub">15s · Tap to select for counted japa</text>

      <!-- Action mini buttons -->
      <g transform="translate(640, 35)">
        <rect width="130" height="70" rx="18" fill="#352B49" stroke="#4A3C64"/>
        <text x="65" y="44" text-anchor="middle" fill="#FFA9B2" font-size="24" font-weight="600">Delete</text>
      </g>
      <g transform="translate(790, 35)">
        <rect width="130" height="70" rx="18" fill="#352B49" stroke="#4A3C64"/>
        <text x="65" y="44" text-anchor="middle" fill="#DEC588" font-size="24" font-weight="600">Preview</text>
      </g>
    </g>
  </g>

  <!-- Safety note -->
  <g transform="translate(60, 1420)">
    <rect width="960" height="180" rx="24" fill="#171024" stroke="#2B1F3F"/>
    <text x="40" y="55" class="card-title" font-size="28">Listening safety</text>
    <text x="40" y="100" class="card-sub">Playback is foreground-only and uses the built-in phone speaker.</text>
    <text x="40" y="140" class="card-sub">Microphone is active only during an explicit capture session.</text>
  </g>
""")

# 4. Mantra Record / Capture in Progress
screens["04_mantra_record_capture.svg"] = make_svg("Mantra Recording", """
  <g transform="translate(60, 260)">
    <text x="0" y="30" class="subhead" font-size="30">Speak clearly · Up to 90 seconds per chant</text>
  </g>

  <!-- Active Recording Card -->
  <g transform="translate(60, 360)">
    <rect width="960" height="420" rx="30" fill="#241B35" stroke="#C8483E" stroke-width="3"/>
    
    <circle cx="100" cy="90" r="18" fill="#C8483E"/>
    <text x="140" y="98" class="card-title" fill="#FFA9B2">Recording in progress…</text>
    
    <!-- Live Timer readout -->
    <text x="480" y="240" text-anchor="middle" class="heading" font-size="96" fill="#F4EEFF">00:15</text>
    <text x="480" y="300" text-anchor="middle" class="subhead" font-size="28">Max duration: 90 seconds</text>

    <!-- Audio waveform indicator (illustrative) -->
    <g transform="translate(180, 340)">
      <line x1="0" y1="20" x2="0" y2="40" stroke="#DEC588" stroke-width="6" stroke-linecap="round"/>
      <line x1="40" y1="10" x2="40" y2="50" stroke="#DEC588" stroke-width="6" stroke-linecap="round"/>
      <line x1="80" y1="0" x2="80" y2="60" stroke="#DEC588" stroke-width="6" stroke-linecap="round"/>
      <line x1="120" y1="15" x2="120" y2="45" stroke="#DEC588" stroke-width="6" stroke-linecap="round"/>
      <line x1="160" y1="5" x2="160" y2="55" stroke="#DEC588" stroke-width="6" stroke-linecap="round"/>
      <line x1="200" y1="22" x2="200" y2="38" stroke="#DEC588" stroke-width="6" stroke-linecap="round"/>
      <line x1="240" y1="12" x2="240" y2="48" stroke="#DEC588" stroke-width="6" stroke-linecap="round"/>
      <line x1="280" y1="0" x2="280" y2="60" stroke="#DEC588" stroke-width="6" stroke-linecap="round"/>
      <line x1="320" y1="18" x2="320" y2="42" stroke="#DEC588" stroke-width="6" stroke-linecap="round"/>
      <line x1="360" y1="8" x2="360" y2="52" stroke="#DEC588" stroke-width="6" stroke-linecap="round"/>
      <line x1="400" y1="20" x2="400" y2="40" stroke="#DEC588" stroke-width="6" stroke-linecap="round"/>
      <line x1="440" y1="5" x2="440" y2="55" stroke="#DEC588" stroke-width="6" stroke-linecap="round"/>
      <line x1="480" y1="15" x2="480" y2="45" stroke="#DEC588" stroke-width="6" stroke-linecap="round"/>
      <line x1="520" y1="0" x2="520" y2="60" stroke="#DEC588" stroke-width="6" stroke-linecap="round"/>
      <line x1="560" y1="20" x2="560" y2="40" stroke="#DEC588" stroke-width="6" stroke-linecap="round"/>
      <line x1="600" y1="10" x2="600" y2="50" stroke="#DEC588" stroke-width="6" stroke-linecap="round"/>
    </g>
  </g>

  <!-- Big Stop Recording Button -->
  <g transform="translate(60, 840)">
    <rect width="960" height="110" rx="28" fill="#C8483E"/>
    <text x="480" y="70" text-anchor="middle" class="btn-text" fill="#FFFFFF">■ Stop recording</text>
  </g>

  <!-- Privacy Callout -->
  <g transform="translate(60, 1020)">
    <rect width="960" height="160" rx="24" fill="#1C1428" stroke="#312547"/>
    <text x="40" y="55" class="card-title" font-size="28">Private microphone use</text>
    <text x="40" y="100" class="card-sub">Recorded voice stays in private storage. No background capture.</text>
  </g>
""")

# 5. Review, Retry & Save Take
screens["05_mantra_review_retry.svg"] = make_svg("Review Mantra Take", """
  <g transform="translate(60, 260)">
    <text x="0" y="30" class="subhead" font-size="30">Take completed · Listen, retry or save</text>
  </g>

  <!-- Take Summary Card -->
  <g transform="translate(60, 340)">
    <rect width="960" height="200" rx="26" fill="#241B35" stroke="#3A2E50"/>
    <text x="40" y="60" class="card-title">Review take · 15.2s</text>
    <text x="40" y="105" class="card-sub">Audio format: AAC 96 kbps / 44.1 kHz</text>
    <text x="40" y="150" class="card-sub">Preview before committing to local library.</text>
  </g>

  <!-- Preview Action & Retry -->
  <g transform="translate(60, 580)">
    <rect width="460" height="90" rx="22" fill="#352B49" stroke="#E8C872"/>
    <text x="230" y="58" text-anchor="middle" class="btn-text-outline">▶ Preview take</text>

    <g transform="translate(500, 0)">
      <rect width="460" height="90" rx="22" fill="#241B35" stroke="#3A2E50"/>
      <text x="230" y="58" text-anchor="middle" fill="#BEB6CA" font-size="28" font-weight="600">Try another take</text>
    </g>
  </g>

  <!-- Title Input Form -->
  <g transform="translate(60, 720)">
    <text x="0" y="30" class="label">RECORDING TITLE</text>
    
    <rect y="50" width="960" height="90" rx="20" fill="#1A1326" stroke="#DEC588" stroke-width="2"/>
    <text x="40" y="108" fill="#F4EEFF" font-size="32" font-weight="500">Morning Gayatri Mantra</text>

    <text x="0" y="180" class="subhead" font-size="24">1–80 characters · Used for display in your personal library</text>
  </g>

  <!-- Save Button -->
  <g transform="translate(60, 960)">
    <rect width="960" height="100" rx="26" fill="#E8C872"/>
    <text x="480" y="64" text-anchor="middle" class="btn-text">Save selected take</text>
  </g>
""")

# 6. Counted Japa Setup (11, 21, 58, 108)
screens["06_mantra_counted_setup.svg"] = make_svg("Counted Japa Setup", """
  <g transform="translate(60, 260)">
    <text x="0" y="30" class="subhead" font-size="30">Selected: Morning Gayatri Mantra (15s)</text>
  </g>

  <!-- Repetitions Card -->
  <g transform="translate(60, 340)">
    <rect width="960" height="260" rx="26" fill="#241B35" stroke="#3A2E50"/>
    <text x="40" y="60" class="card-title">Repetition target: 108</text>
    <text x="40" y="105" class="card-sub">Session terminates automatically at target or 30-minute safety cap.</text>

    <!-- Count Chips -->
    <g transform="translate(40, 140)">
      <rect x="0" y="0" width="180" height="70" rx="18" fill="#352B49" stroke="#4A3C64"/>
      <text x="90" y="44" text-anchor="middle" fill="#BEB6CA" font-size="28" font-weight="600">11</text>

      <rect x="220" y="0" width="180" height="70" rx="18" fill="#352B49" stroke="#4A3C64"/>
      <text x="310" y="44" text-anchor="middle" fill="#BEB6CA" font-size="28" font-weight="600">21</text>

      <rect x="440" y="0" width="180" height="70" rx="18" fill="#352B49" stroke="#4A3C64"/>
      <text x="530" y="44" text-anchor="middle" fill="#BEB6CA" font-size="28" font-weight="600">58</text>

      <rect x="660" y="0" width="220" height="70" rx="18" fill="#E8C872"/>
      <text x="770" y="44" text-anchor="middle" fill="#0B0716" font-size="28" font-weight="700">108</text>
    </g>
  </g>

  <!-- Gap between chants -->
  <g transform="translate(60, 640)">
    <rect width="960" height="240" rx="26" fill="#241B35" stroke="#3A2E50"/>
    <text x="40" y="60" class="card-title">Silence between chants: 1.0s</text>
    <text x="40" y="105" class="card-sub">Permits quiet breathing space between vocal repetitions.</text>

    <!-- Gap Chips -->
    <g transform="translate(40, 135)">
      <rect x="0" y="0" width="190" height="65" rx="16" fill="#352B49" stroke="#4A3C64"/>
      <text x="95" y="42" text-anchor="middle" fill="#BEB6CA" font-size="26" font-weight="600">0.5s</text>

      <rect x="230" y="0" width="190" height="65" rx="16" fill="#E8C872"/>
      <text x="325" y="42" text-anchor="middle" fill="#0B0716" font-size="26" font-weight="700">1.0s</text>

      <rect x="460" y="0" width="190" height="65" rx="16" fill="#352B49" stroke="#4A3C64"/>
      <text x="555" y="42" text-anchor="middle" fill="#BEB6CA" font-size="26" font-weight="600">2.0s</text>

      <rect x="690" y="0" width="190" height="65" rx="16" fill="#352B49" stroke="#4A3C64"/>
      <text x="785" y="42" text-anchor="middle" fill="#BEB6CA" font-size="26" font-weight="600">3.0s</text>
    </g>
  </g>

  <!-- Start Button -->
  <g transform="translate(60, 940)">
    <rect width="960" height="100" rx="26" fill="#E8C872"/>
    <text x="480" y="64" text-anchor="middle" class="btn-text">Start 108 repetitions</text>
  </g>
""")

# 7. Live Counted Japa Playback
screens["07_mantra_live_japa_playback.svg"] = make_svg("Counted Japa Replay", """
  <g transform="translate(60, 260)">
    <text x="0" y="30" class="subhead" font-size="30">Morning Gayatri Mantra · Built-in phone speaker</text>
  </g>

  <!-- Live Status Hero -->
  <g transform="translate(60, 340)">
    <rect width="960" height="480" rx="32" fill="#241B35" stroke="#E8C872" stroke-width="2"/>
    
    <text x="480" y="100" text-anchor="middle" class="label" font-size="24">COMPLETED REPETITIONS</text>
    <text x="480" y="230" text-anchor="middle" class="heading" font-size="110" fill="#E8C872">14 <tspan font-size="48" fill="#BEB6CA">/ 108</tspan></text>
    
    <!-- Progress Bar -->
    <rect x="100" y="290" width="760" height="16" rx="8" fill="#181226"/>
    <rect x="100" y="290" width="100" height="16" rx="8" fill="#E8C872"/>

    <text x="480" y="380" text-anchor="middle" fill="#F4EEFF" font-size="32" font-weight="600">Chant 14 of 108 complete.</text>
    <text x="480" y="430" text-anchor="middle" fill="#BEB6CA" font-size="24">1.0s breath gap between repetitions</text>
  </g>

  <!-- Stop Button -->
  <g transform="translate(60, 870)">
    <rect width="960" height="110" rx="28" fill="#3D3054" stroke="#DEC588" stroke-width="2"/>
    <text x="480" y="70" text-anchor="middle" fill="#DEC588" font-size="32" font-weight="700">■ Stop playback</text>
  </g>

  <!-- Active Safety Guardrails -->
  <g transform="translate(60, 1030)">
    <rect width="960" height="240" rx="24" fill="#1C1428" stroke="#312547"/>
    <text x="40" y="55" class="card-title" font-size="28">Live safety enforcement</text>
    <text x="40" y="105" class="card-sub">• Route verified: Built-in phone speaker (changes stop instantly)</text>
    <text x="40" y="145" class="card-sub">• Media volume: under 60% threshold monitored continuously</text>
    <text x="40" y="185" class="card-sub">• Finite duration: Hard 30-minute cutoff stops long sessions safely</text>
  </g>
""")

# 8. Sound Player (Synthesizer)
screens["08_sound_player.svg"] = make_svg("Sound Player &amp; Timer", """
  <g transform="translate(60, 260)">
    <text x="0" y="30" class="subhead" font-size="30">Natural acoustic modulation session</text>
  </g>

  <g transform="translate(60, 340)">
    <rect width="960" height="420" rx="30" fill="#241B35" stroke="#3A2E50"/>
    
    <text x="50" y="80" class="card-title">Mosquito profile</text>
    <text x="50" y="130" class="subhead" font-size="32">16,000 Hz – 20,000 Hz Sweep</text>

    <!-- Circular Timer Progress -->
    <g transform="translate(480, 260)">
      <circle cx="0" cy="0" r="110" fill="#1B132B" stroke="#362952" stroke-width="12"/>
      <circle cx="0" cy="0" r="110" fill="none" stroke="#E8C872" stroke-width="12" stroke-dasharray="691" stroke-dashoffset="230"/>
      <text x="0" y="16" text-anchor="middle" class="heading" font-size="52">07:24</text>
      <text x="0" y="54" text-anchor="middle" fill="#BEB6CA" font-size="20">REMAINING</text>
    </g>
  </g>

  <!-- Start / Stop Button -->
  <g transform="translate(60, 810)">
    <rect width="960" height="110" rx="28" fill="#E8C872"/>
    <text x="480" y="70" text-anchor="middle" class="btn-text">▶ Start (10 min)</text>
  </g>

  <!-- Safety Snapshot Box -->
  <g transform="translate(60, 970)">
    <rect width="960" height="260" rx="24" fill="#1C1428" stroke="#312547"/>
    <text x="40" y="55" class="card-title" font-size="28">Device safety status</text>
    <text x="40" y="105" class="card-sub">Output: Built-in phone speaker (verified)</text>
    <text x="40" y="145" class="card-sub">Volume: 42% (below 60% cutoff limit)</text>
    <text x="40" y="185" class="card-sub">Battery: 84% (above 15% minimum)</text>
    <text x="40" y="225" class="card-sub">Thermal state: Normal (0)</text>
  </g>
""")

# Write all SVGs
for filename, content in screens.items():
    svg_path = os.path.join(OUT_SVG_DIR, filename)
    with open(svg_path, "w", encoding="utf-8") as f:
        f.write(content)
    print(f"Wrote {svg_path}")

print("All SVGs successfully written.")
