package com.methakon.pifauna

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.json.JSONArray
import org.json.JSONObject

/** Native single-activity prototype, offline and permission-free, not an animal repellent claim. */
class MainActivity : Activity() {
    private val deepTeal = Color.rgb(0, 91, 88)
    private val ink = Color.rgb(30, 48, 51)
    private val muted = Color.rgb(80, 98, 101)
    private lateinit var profileSpinner: Spinner
    private lateinit var modeSpinner: Spinner
    private lateinit var durationSpinner: Spinner
    private lateinit var levelSpinner: Spinner
    private lateinit var observedSpinner: Spinner
    private lateinit var startField: EditText
    private lateinit var endField: EditText
    private lateinit var profileInfo: TextView
    private lateinit var status: TextView
    private lateinit var observations: TextView
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private var profiles: List<SpeciesProfile> = SpeciesProfiles.list
    private val exportRequestCode = 4207

    @Volatile private var playing = false
    @Volatile private var session = 0
    private var confirmedThisLaunch = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val outer = ScrollView(this).apply {
            fillViewport = true
            setBackgroundColor(Color.rgb(247, 250, 248))
        }
        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(28), dp(20), dp(32))
        }
        outer.addView(body)
        setContentView(outer)

        body.addView(makeText("PIFAUNA", 13f, deepTeal, true))
        body.addView(makeText("Frequency experiments", 26f, ink, true).withTop(4))
        body.addView(makeText(
            "Generate low-level test tones on the phone speaker. These settings do not establish pest-repellent effectiveness.",
            14f, muted
        ).withTop(8))
        body.addView(makeNotice(
            "PHONE MODE  •  100–18,000 Hz  •  48 kHz PCM\n" +
                "An app cannot make a normal phone speaker reliably emit 30–70 kHz ultrasound."
        ).withTop(18))

        body.addView(section("1   Species / observation group"))
        profiles = SpeciesProfiles.list + readSavedProfiles()
        profileSpinner = dropdown(profiles.map { it.name })
        body.addView(profileSpinner)
        profileInfo = makeText("", 13f, muted)
        body.addView(profileInfo.withTop(10))

        body.addView(section("2   Signal configuration"))
        body.addView(makeText("Start / fixed frequency (Hz)", 13f, ink, true))
        startField = numberField("1000")
        body.addView(startField)
        body.addView(makeText("Sweep end frequency (Hz)", 13f, ink, true).withTop(10))
        endField = numberField("4000")
        body.addView(endField)
        body.addView(makeText("Signal pattern", 13f, ink, true).withTop(12))
        modeSpinner = dropdown(listOf(
            "Fixed tone", "Triangle sweep (2.5 s)", "Pulse (250 ms on / 750 ms off)",
            "Random frequency hop (every 0.5 s)"
        ))
        body.addView(modeSpinner)
        body.addView(makeText(
            "Random hop changes frequency every 0.5 s and fades between steps. Choose a range using the two frequency fields.",
            12f, muted
        ).withTop(6))
        body.addView(Button(this).apply {
            text = "Save these settings as a local preset"
            isAllCaps = false
            setOnClickListener { promptSavePreset() }
        }.withTop(8))

        body.addView(section("3   Exposure limits"))
        body.addView(makeText("Automatic stop", 13f, ink, true))
        durationSpinner = dropdown(listOf("5 seconds", "10 seconds", "20 seconds"))
        durationSpinner.setSelection(1)
        body.addView(durationSpinner)
        body.addView(makeText("Digital signal amplitude (NOT calibrated dB SPL)", 13f, ink, true).withTop(12))
        levelSpinner = dropdown(listOf("3% PCM", "6% PCM", "10% PCM"))
        body.addView(levelSpinner)
        body.addView(makeText(
            "Keep your phone's media volume at minimum to begin; digital amplitude is not an acoustic safety measurement.",
            12f, muted
        ).withTop(6))

        val controls = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        startButton = Button(this).apply {
            text = "Start short test"
            isAllCaps = false
            setTextColor(Color.WHITE)
            backgroundTintList = android.content.res.ColorStateList.valueOf(deepTeal)
            setOnClickListener { requestStart() }
        }
        stopButton = Button(this).apply {
            text = "STOP"
            isAllCaps = false
            isEnabled = false
            setOnClickListener { stopPlayback("Stopped manually") }
        }
        controls.addView(startButton, LinearLayout.LayoutParams(0, dp(54), 2f))
        controls.addView(stopButton, LinearLayout.LayoutParams(0, dp(54), 1f).apply { leftMargin = dp(8) })
        body.addView(controls.withTop(20))
        status = makeText("Ready • speaker-only • playback ends when app leaves screen", 13f, deepTeal, true)
        body.addView(status.withTop(8))

        body.addView(section("4   Manual observation log"))
        body.addView(makeText(
            "Log what you actually observed, including 'no change'. This does not prove causation.",
            13f, muted
        ))
        observedSpinner = dropdown(listOf(
            "No change observed", "Moved away", "Moved closer", "Activity changed", "Distress observed — stop testing"
        ))
        body.addView(observedSpinner.withTop(8))
        body.addView(Button(this).apply {
            text = "Save observation locally"
            isAllCaps = false
            setOnClickListener { saveObservation() }
        }.withTop(6))
        body.addView(Button(this).apply {
            text = "Export observation history (CSV)"
            isAllCaps = false
            setOnClickListener { exportCsv() }
        }.withTop(2))
        observations = makeText("", 12f, muted)
        body.addView(observations.withTop(8))
        body.addView(makeText(
            "Safety: Never use headphones. Do not expose children, pets, wildlife or neighbors to disturbing tones. " +
                "Stop at any sign of distress. No unattended playback or background service.",
            12f, muted
        ).withTop(24))

        profileSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val preset = profiles[position]
                startField.setText(preset.firstHz.toString())
                endField.setText(preset.lastHz.toString())
                profileInfo.text = preset.explanation
            }
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
        refreshObservations()
    }

    private fun requestStart() {
        if (playing) return
        val start = startField.text.toString().toDoubleOrNull()
        val end = endField.text.toString().toDoubleOrNull()
        if (start == null || end == null || start !in 100.0..18_000.0 || end !in 100.0..18_000.0) {
            showError("Enter both frequencies as numbers from 100 to 18,000 Hz.")
            return
        }
        val duration = intArrayOf(5, 10, 20)[durationSpinner.selectedItemPosition]
        val gain = doubleArrayOf(0.03, 0.06, 0.10)[levelSpinner.selectedItemPosition]
        val mode = ToneMode.entries[modeSpinner.selectedItemPosition]
        val config = try {
            ToneConfig(start, end, mode, gain, durationSeconds = duration)
        } catch (error: IllegalArgumentException) {
            showError(error.message ?: "Invalid signal configuration")
            return
        }

        val audio = getSystemService(AUDIO_SERVICE) as AudioManager
        val outputs = audio.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        val externalRoute = outputs.any {
            when (it.type) {
                AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                AudioDeviceInfo.TYPE_WIRED_HEADSET,
                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
                AudioDeviceInfo.TYPE_USB_DEVICE,
                AudioDeviceInfo.TYPE_USB_ACCESSORY,
                AudioDeviceInfo.TYPE_USB_HEADSET,
                AudioDeviceInfo.TYPE_HEARING_AID,
                AudioDeviceInfo.TYPE_BLE_HEADSET,
                AudioDeviceInfo.TYPE_BLE_SPEAKER -> true
                else -> false
            }
        }
        if (externalRoute) {
            showError("Disconnect headphones, Bluetooth audio or USB audio before testing. Phone-speaker-only mode is enforced.")
            return
        }
        val speaker = outputs.firstOrNull { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }
        if (speaker == null) {
            showError("Built-in speaker not reported. Playback refused to avoid accidental headset routing.")
            return
        }
        val currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        if (currentVolume.toDouble() / maxVolume > 0.35) {
            showError("Reduce the phone's media volume to 35% or less before this short test.")
            return
        }
        if (!confirmedThisLaunch) {
            AlertDialog.Builder(this)
                .setTitle("Short acoustic experiment only")
                .setMessage(
                    "Playback may be audible or uncomfortable even when you cannot hear it. " +
                        "It is NOT a proven pest repellent. Keep animals and other people away from the speaker; " +
                        "never use headphones, and stop immediately if there is distress. Continue?"
                )
                .setNegativeButton("Cancel", null)
                .setPositiveButton("I understand") { _, _ ->
                    confirmedThisLaunch = true
                    startPlayback(config, speaker)
                }.show()
        } else startPlayback(config, speaker)
    }

    private fun startPlayback(config: ToneConfig, speaker: AudioDeviceInfo) {
        if (playing) return
        playing = true
        session += 1
        val mySession = session
        startButton.isEnabled = false
        stopButton.isEnabled = true
        status.text = "Playing ${config.mode.name.lowercase()} • max ${config.durationSeconds} s • speaker only"
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        Thread({
            var track: AudioTrack? = null
            var result = "Playback completed / auto-stopped"
            try {
                val minBytes = AudioTrack.getMinBufferSize(
                    config.sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT
                )
                require(minBytes > 0) { "PCM output not supported" }
                track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder().setSampleRate(config.sampleRate)
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build()
                    )
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .setBufferSizeInBytes(maxOf(minBytes, 4_096))
                    .build()
                check(track.state == AudioTrack.STATE_INITIALIZED) { "Audio output failed to initialize" }
                check(track.setPreferredDevice(speaker)) { "Could not select built-in speaker" }
                track.play()
                val totalFrames = config.durationSeconds.toLong() * config.sampleRate
                var frame = 0L
                var phase = 0.0
                while (playing && session == mySession && frame < totalFrames) {
                    val count = minOf(480L, totalFrames - frame).toInt() // 10 ms per block
                    val block = ToneKernel.render(config, frame, count, phase)
                    phase = block.endingPhase
                    var written = 0
                    while (written < block.samples.size && playing && session == mySession) {
                        val n = track.write(block.samples, written, block.samples.size - written, AudioTrack.WRITE_BLOCKING)
                        check(n > 0) { "Audio write failed: $n" }
                        written += n
                    }
                    frame += written
                    // Audio routing can change after starting. Refuse non-speaker routes.
                    if (frame > 480L) {
                        val actualRoute = track.routedDevice
                        check(actualRoute == null || actualRoute.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER) {
                            "Audio route changed away from built-in speaker"
                        }
                    }
                }
                if (session != mySession || !playing) result = "Playback stopped"
            } catch (error: Exception) {
                result = "Playback ended: ${error.message ?: "unknown audio error"}"
            } finally {
                try { track?.pause() } catch (_: Exception) { }
                try { track?.flush() } catch (_: Exception) { }
                try { track?.release() } catch (_: Exception) { }
                runOnUiThread {
                    if (session == mySession) {
                        playing = false
                        startButton.isEnabled = true
                        stopButton.isEnabled = false
                        status.text = result
                        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                }
            }
        }, "Acoustic-Synthesis").start()
    }

    private fun stopPlayback(message: String) {
        playing = false
        session += 1
        if (::startButton.isInitialized) startButton.isEnabled = true
        if (::stopButton.isInitialized) stopButton.isEnabled = false
        if (::status.isInitialized) status.text = message
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun promptSavePreset() {
        val start = startField.text.toString().toDoubleOrNull()
        val end = endField.text.toString().toDoubleOrNull()
        if (start == null || end == null || start !in 100.0..18_000.0 || end !in 100.0..18_000.0) {
            showError("Use a start and end frequency between 100 and 18,000 Hz before saving a preset.")
            return
        }
        if (readSavedProfiles().size >= 20) {
            showError("The maximum of 20 custom presets has been reached.")
            return
        }
        val nameInput = EditText(this).apply {
            setSingleLine(true)
            hint = "E.g., Kitchen observation"
            setPadding(dp(16), dp(12), dp(16), dp(12))
        }
        AlertDialog.Builder(this)
            .setTitle("Save local research preset")
            .setMessage("This stores the frequency range only. No frequency is verified to repel a species.")
            .setView(nameInput)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ ->
                val name = nameInput.text.toString().trim().replace(Regex("[\\r\\n\\t]"), " ").take(50)
                if (name.isBlank()) {
                    Toast.makeText(this, "Preset needs a name", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val saved = readSavedProfiles()
                if (saved.any { it.name.equals(name, true) } || SpeciesProfiles.list.any { it.name.equals(name, true) }) {
                    Toast.makeText(this, "A preset already uses that name", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val record = JSONObject().put("name", name).put("startHz", start).put("endHz", end)
                val records = JSONArray(getSharedPreferences("presets", MODE_PRIVATE).getString("items", "[]"))
                records.put(record)
                getSharedPreferences("presets", MODE_PRIVATE).edit().putString("items", records.toString()).apply()
                profiles = SpeciesProfiles.list + readSavedProfiles()
                profileSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, profiles.map { it.name })
                profileSpinner.setSelection(profiles.lastIndex)
                Toast.makeText(this, "Custom preset saved locally", Toast.LENGTH_SHORT).show()
            }.show()
    }

    private fun readSavedProfiles(): List<SpeciesProfile> = try {
        val raw = getSharedPreferences("presets", MODE_PRIVATE).getString("items", "[]") ?: "[]"
        val items = JSONArray(raw)
        (0 until minOf(items.length(), 20)).mapNotNull { i ->
            val item = items.optJSONObject(i) ?: return@mapNotNull null
            val start = item.optDouble("startHz")
            val end = item.optDouble("endHz")
            val name = item.optString("name", "").take(50)
            if (name.isBlank() || start !in 100.0..18_000.0 || end !in 100.0..18_000.0) null
            else SpeciesProfile(name, start.toInt(), end.toInt(),
                "Personal research preset, not an established repellent range.", "")
        }
    } catch (_: Exception) { emptyList() }

    private fun saveObservation() {
        if (playing) stopPlayback("Stopped to log observation")
        val record = JSONObject()
            .put("timestamp", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()))
            .put("group", profiles[profileSpinner.selectedItemPosition].name)
            .put("start_hz", startField.text.toString())
            .put("end_hz", endField.text.toString())
            .put("pattern", modeSpinner.selectedItem.toString())
            .put("duration_s", intArrayOf(5, 10, 20)[durationSpinner.selectedItemPosition])
            .put("gain_percent", intArrayOf(3, 6, 10)[levelSpinner.selectedItemPosition])
            .put("observation", observedSpinner.selectedItem.toString())
        val prefs = getSharedPreferences("observations", MODE_PRIVATE)
        val previous = getRecords()
        val next = JSONArray().put(record)
        for (i in 0 until minOf(previous.length(), 99)) next.put(previous.get(i))
        prefs.edit().putString("records_v2", next.toString()).apply()
        refreshObservations()
        Toast.makeText(this, "Saved privately on this phone", Toast.LENGTH_SHORT).show()
    }

    private fun getRecords(): JSONArray = try {
        JSONArray(getSharedPreferences("observations", MODE_PRIVATE).getString("records_v2", "[]") ?: "[]")
    } catch (_: Exception) { JSONArray() }

    private fun refreshObservations() {
        val records = getRecords()
        observations.text = if (records.length() == 0) "No observations yet."
        else (0 until minOf(5, records.length())).joinToString("\n\n") { i ->
            val record = records.getJSONObject(i)
            "${record.optString("timestamp")} | ${record.optString("group")} | " +
                "${record.optString("start_hz")}–${record.optString("end_hz")} Hz | " +
                "${record.optString("observation")}" 
        }
    }

    private fun exportCsv() {
        if (getRecords().length() == 0) {
            Toast.makeText(this, "Save an observation before exporting", Toast.LENGTH_SHORT).show()
            return
        }
        val date = SimpleDateFormat("yyyyMMdd-HHmm", Locale.US).format(Date())
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv"
            putExtra(Intent.EXTRA_TITLE, "bioacoustic-observations-$date.csv")
        }
        try { startActivityForResult(intent, exportRequestCode) }
        catch (error: Exception) { showError("CSV export unavailable: ${error.message}") }
    }

    @Deprecated("Activity result callback retained for minSdk 26 without AndroidX")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != exportRequestCode || resultCode != RESULT_OK) return
        val uri = data?.data ?: return
        try {
            val keys = listOf("timestamp", "group", "start_hz", "end_hz", "pattern", "duration_s", "gain_percent", "observation")
            val records = getRecords()
            val csv = buildString {
                append(keys.joinToString(",")); append("\r\n")
                for (i in 0 until records.length()) {
                    val record = records.getJSONObject(i)
                    append(keys.joinToString(",") { key ->
                        "\"" + record.optString(key, "").replace("\"", "\"\"") + "\""
                    })
                    append("\r\n")
                }
            }
            contentResolver.openOutputStream(uri)?.use { it.write(csv.toByteArray(Charsets.UTF_8)) }
                ?: error("File output stream unavailable")
            Toast.makeText(this, "Observation CSV exported", Toast.LENGTH_LONG).show()
        } catch (error: Exception) {
            showError("Could not export CSV: ${error.message}")
        }
    }

    private fun showError(message: String) {
        AlertDialog.Builder(this).setTitle("Playback not started").setMessage(message).setPositiveButton("OK", null).show()
    }

    override fun onPause() {
        stopPlayback("Stopped because the app is no longer in the foreground")
        super.onPause()
    }

    private fun section(title: String): TextView = makeText(title, 16f, deepTeal, true).withTop(24)
    private fun makeText(text: String, size: Float, color: Int, bold: Boolean = false): TextView = TextView(this).apply {
        this.text = text
        textSize = size
        setTextColor(color)
        if (bold) setTypeface(typeface, Typeface.BOLD)
        setLineSpacing(dp(2).toFloat(), 1f)
    }
    private fun makeNotice(text: String): TextView = makeText(text, 13f, ink).apply {
        setPadding(dp(13), dp(12), dp(13), dp(12))
        background = GradientDrawable().apply {
            setColor(Color.rgb(228, 241, 239))
            cornerRadius = dp(12).toFloat()
        }
    }
    private fun dropdown(options: List<String>): Spinner = Spinner(this).apply {
        adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, options)
        minimumHeight = dp(48)
    }
    private fun numberField(value: String): EditText = EditText(this).apply {
        setSingleLine(true)
        setText(value)
        inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        textSize = 16f
        setSelectAllOnFocus(true)
        contentDescription = "Frequency in hertz"
    }
    private fun <T : View> T.withTop(margin: Int): T {
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(margin)
        }
        return this
    }
    private fun dp(number: Int): Int = (number * resources.displayMetrics.density + 0.5f).toInt()
}