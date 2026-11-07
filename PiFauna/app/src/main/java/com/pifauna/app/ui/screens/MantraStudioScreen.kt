package com.pifauna.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.pifauna.app.mantra.MantraAudioEngine
import com.pifauna.app.mantra.MantraRecording
import com.pifauna.app.mantra.MantraRepetitionPolicy
import com.pifauna.app.mantra.MantraStore
import com.pifauna.app.playback.PlaybackRuntime
import com.pifauna.app.ui.ConcentricJapaDial
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import com.pifauna.app.ui.PfCard
import com.pifauna.app.ui.PfScreen
import com.pifauna.app.ui.PfTextCard
import com.pifauna.app.ui.PiFaunaColors
import com.pifauna.app.ui.SectionLabel
import kotlinx.coroutines.delay
import java.io.File

/** Mantra recordings never leave app-private storage. No cloud, sharing or upload. */
@Composable
fun MantraStudioScreen() {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current
    val store = remember(context) { MantraStore(context) }
    val engine = remember(context) { MantraAudioEngine(context) }
    var recordings by remember { mutableStateOf(store.list()) }
    var take by remember { mutableStateOf<Pair<File, Long>?>(null) }
    val latestTake by rememberUpdatedState(take)
    var title by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Your voice stays on this phone.") }
    var recording by remember { mutableStateOf(false) }
    var recordingElapsedMs by remember { mutableLongStateOf(0L) }
    var selected by remember { mutableStateOf<MantraRecording?>(null) }
    var selectedSets by remember { mutableIntStateOf(1) }
    var gapMs by remember { mutableLongStateOf(1000L) }
    var completed by remember { mutableIntStateOf(0) }
    var replaying by remember { mutableStateOf(false) }
    var deleteCandidate by remember { mutableStateOf<MantraRecording?>(null) }

    fun finishTake() {
        runCatching { engine.finishCapture() }
            .onSuccess {
                take?.first?.delete()
                take = it
                status = "Review your take, record again, or save it with a title."
            }
            .onFailure {
                status = it.message ?: "Recording could not be completed."
            }
        recording = false
    }

    fun beginTake() {
        engine.stopPlayback()
        replaying = false
        PlaybackRuntime.stop(context, "Stopped before microphone recording.")
        take?.first?.delete()
        take = null
        recordingElapsedMs = 0
        runCatching { engine.beginCapture(store.newTake()) }
            .onSuccess { recording = true; status = "Recording… tap Stop when finished." }
            .onFailure { status = it.message ?: "Microphone unavailable." }
    }

    val requestMicrophone = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { allowed ->
        if (allowed) beginTake() else status = "Microphone permission denied. No recording made."
    }

    fun requestTake() {
        if (recording) return
        if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        ) beginTake()
        else requestMicrophone.launch(Manifest.permission.RECORD_AUDIO)
    }

    fun play(file: File, sets: Int? = null, resume: Boolean = false) {
        if (recording) return
        PlaybackRuntime.stop(context, "Stopped before mantra replay.")
        val startingAt = if (sets != null && resume) completed else 0
        if (sets != null && !resume) completed = 0
        replaying = true
        val target = sets?.let(MantraRepetitionPolicy::totalRepetitions) ?: 1
        val notify: (Int, String?) -> Unit = { done, error ->
            // One-play previews must never mutate a long japa practice checkpoint.
            if (sets != null) completed = done
            if (error != null || done >= target) {
                replaying = false
                status = error ?: if (sets == null) "Preview finished."
                    else "Completed $done of $target chants."
            } else {
                val progress = MantraRepetitionPolicy.progress(done, sets ?: 1)
                status = if (sets == null) "Previewing…"
                    else "Set ${progress.currentSet} of $sets · ${progress.completedWithinSet}/108 chants completed."
            }
        }
        runCatching {
            if (sets == null) engine.startPreview(file, notify)
            else engine.startReplay(file, sets, gapMs, notify, startingAt)
        }.onFailure {
            replaying = false
            status = it.message ?: "Could not play the selected recording."
        }
    }

    LaunchedEffect(recording) {
        if (recording) {
            while (recording) {
                delay(250)
                recordingElapsedMs += 250L
                if (recordingElapsedMs >= 89_500L) {
                    finishTake()
                    break
                }
            }
        }
    }

    DisposableEffect(lifecycle, engine) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                // No recording or voice replay while the app is backgrounded.
                engine.close()
                recording = false
                replaying = false
                status = "Stopped when the app left the foreground."
            }
        }
        lifecycle.lifecycle.addObserver(observer)
        onDispose {
            lifecycle.lifecycle.removeObserver(observer)
            engine.close()
            latestTake?.first?.delete()
        }
    }

    PfScreen(
        title = "Mantra Studio",
        subtitle = "Record your own chant · replay in counted sets"
    ) {
        PfTextCard(
            heading = "Your voice, your practice",
            body = "Record as many attempts as you like. Only your chosen take is saved. " +
                "Recordings remain private on this device; nothing is uploaded."
        )

        SectionLabel("RECORD")
        PfCard(
            title = if (recording) "Recording · ${recordingElapsedMs / 1000}s / 90s" else "New recording",
            subtitle = "Use a quiet place and speak at a comfortable level."
        )
        if (recording) {
            Button(onClick = ::finishTake, modifier = Modifier.fillMaxWidth()) {
                Text("Stop recording")
            }
        } else {
            Button(onClick = ::requestTake, modifier = Modifier.fillMaxWidth(),
                enabled = !replaying) {
                Text(if (take == null) "Record a mantra" else "Try another take")
            }
        }

        take?.let { (file, duration) ->
            PfCard(
                title = "Review take · ${duration / 1000}s",
                subtitle = "You can preview, retry, or save when satisfied."
            )
            OutlinedButton(onClick = { play(file) }, enabled = !recording && !replaying) {
                Text("Preview this take")
            }
            OutlinedTextField(
                value = title,
                onValueChange = { title = it.take(80) },
                label = { Text("Recording title") },
                placeholder = { Text("e.g. Morning Gayatri Mantra") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                enabled = !recording && !replaying && title.isNotBlank(),
                onClick = {
                    runCatching { store.save(file, title, duration) }
                        .onSuccess {
                            take = null
                            title = ""
                            recordings = store.list()
                            selected = it
                            status = "Saved \"${it.title}\" privately on this phone."
                        }
                        .onFailure {
                            status = it.message ?: "Save failed."
                        }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save selected take") }
        }

        SectionLabel("MY RECORDINGS")
        if (recordings.isEmpty()) {
            PfTextCard("No saved recordings", "Make a first recording and give it a title.")
        }
        recordings.forEach { item ->
            PfCard(
                title = item.title,
                subtitle = "${item.durationMs / 1000}s · Tap to select",
                enabled = !recording && !replaying,
                onClick = { selected = item }
            )
            if (selected?.id == item.id) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { deleteCandidate = item },
                        enabled = !recording && !replaying
                    ) { Text("Delete") }
                    OutlinedButton(
                        onClick = { play(store.recordingFile(item)) },
                        enabled = !recording && !replaying
                    ) { Text("Preview") }
                }
            }
        }

        selected?.let { item ->
            val total = MantraRepetitionPolicy.totalRepetitions(selectedSets)
            val estimated = MantraRepetitionPolicy.estimatedMs(item.durationMs, selectedSets, gapMs)
            val estimatedMinutes = estimated / 60_000L
            val estimatedHours = estimatedMinutes / 60
            val remainingMinutes = estimatedMinutes % 60
            val finishable = estimated <= MantraRepetitionPolicy.MAX_SESSION_MS

            SectionLabel("JAPA CYCLE · ${item.title}")
            PfTextCard(
                heading = "Choose your sets of 108",
                body = "Outer dial: number of 108-count sets. Inner dial: quiet pause between chants."
            )
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                ConcentricJapaDial(
                    selectedSets = selectedSets,
                    selectedGapMs = gapMs,
                    enabled = !recording && !replaying,
                    onSetsChanged = { selectedSets = it },
                    onGapChanged = { gapMs = it }
                )
            }
            SectionLabel("OUTER RING · SETS")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MantraRepetitionPolicy.setChoices.forEach { sets ->
                    FilterChip(
                        selected = selectedSets == sets,
                        enabled = !recording && !replaying,
                        onClick = { selectedSets = sets },
                        label = { Text("$sets") }
                    )
                }
            }
            SectionLabel("INNER RING · SILENCE BETWEEN CHANTS")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MantraRepetitionPolicy.gapsMs.forEach { gap ->
                    FilterChip(
                        selected = gapMs == gap,
                        enabled = !recording && !replaying,
                        onClick = { gapMs = gap },
                        label = { Text("${MantraRepetitionPolicy.gapLabel(gap)}s") }
                    )
                }
            }
            PfCard(
                title = "$selectedSets sets × 108 = $total chants",
                subtitle = "Estimated duration: ${estimatedHours}h ${remainingMinutes}m · Gap between each chant: ${MantraRepetitionPolicy.gapLabel(gapMs)}s"
            )
            if (!finishable) {
                PfTextCard(
                    heading = "This practice needs more than one session",
                    body = "Each session stops after 30 minutes. You may explicitly resume from your completed chant count using the Continue action. Playback never restarts automatically."
                )
            }
            if (replaying) {
                val progress = MantraRepetitionPolicy.progress(completed.coerceIn(0, total), selectedSets)
                PfCard(
                    title = "Set ${progress.currentSet} / $selectedSets",
                    subtitle = "Completed within current set: ${progress.completedWithinSet} / 108 · Overall: $completed / $total"
                )
                Button(onClick = {
                    engine.stopPlayback()
                    replaying = false
                    status = "Stopped after $completed of $total repetitions."
                }, modifier = Modifier.fillMaxWidth()) { Text("Stop practice") }
            } else {
                if (completed in 1 until total) {
                    Button(
                        onClick = { play(store.recordingFile(item), selectedSets, resume = true) },
                        enabled = !recording,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Continue from chant ${completed + 1} of $total") }
                    OutlinedButton(
                        onClick = { completed = 0; status = "Practice progress reset." },
                        enabled = !recording,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Start over") }
                } else {
                    Button(
                        onClick = { play(store.recordingFile(item), selectedSets) },
                        enabled = !recording,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Start $selectedSets sets · $total chants") }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(status, color = PiFaunaColors.Muted)

        PfTextCard(
            heading = "Listening safety",
            body = "Playback is foreground-only and uses the phone speaker. " +
                "There is no endless loop or automatic restart. Keep volume low; " +
                "record other people only with their consent. The microphone is used " +
                "only after you tap Record and grant Android permission."
        )
    }

    deleteCandidate?.let { item ->
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            title = { Text("Delete recording?") },
            text = { Text("Delete \"${item.title}\" permanently from this device?") },
            confirmButton = {
                Button(onClick = {
                    store.delete(item)
                    recordings = store.list()
                    if (selected?.id == item.id) selected = null
                    deleteCandidate = null
                }) { Text("Delete") }
            },
            dismissButton = {
                OutlinedButton(onClick = { deleteCandidate = null }) { Text("Cancel") }
            }
        )
    }
}
