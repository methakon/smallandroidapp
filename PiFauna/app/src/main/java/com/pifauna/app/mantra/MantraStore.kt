package com.pifauna.app.mantra

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

/** Each recording is private app data. Nothing is shared, uploaded or indexed. */
data class MantraRecording(
    val id: String,
    val title: String,
    val durationMs: Long,
    val createdAt: Long
)

/** Durable checkpoint for long japa sessions. Keyed and validated by recording UUID and selection. */
data class JapaCheckpoint(
    val recordingId: String,
    val selectedSets: Int,
    val gapMs: Long,
    val completed: Int,
    val updatedAt: Long
)

class MantraStore(
    context: Context? = null,
    baseDir: File? = null
) {
    private val root = (baseDir ?: File(requireNotNull(context) { "Either context or baseDir is required" }.filesDir, "mantras")).apply { mkdirs() }
    private val index = File(root, "recordings.json")
    private val checkpointFile = File(root, "checkpoint.json")

    init {
        pruneOrphanTakes()
    }

    /** Cleans up uncommitted temporary takes from previous crashes and invalid checkpoints. */
    fun pruneOrphanTakes() {
        runCatching {
            root.listFiles { file -> file.name.startsWith("take-") && file.name.endsWith(".m4a") }
                ?.forEach { it.delete() }
        }
        runCatching {
            if (checkpointFile.isFile) {
                val obj = JSONObject(checkpointFile.readText())
                val id = obj.optString("recordingId")
                if (!file(id).isFile) {
                    checkpointFile.delete()
                }
            }
        }
    }

    fun list(): List<MantraRecording> = runCatching {
        val arr = JSONArray(index.takeIf { it.exists() }?.readText() ?: "[]")
        buildList {
            for (i in 0 until minOf(arr.length(), 200)) {
                val entry = arr.optJSONObject(i) ?: continue
                val id = entry.optString("id")
                if (!id.matches(Regex("[a-f0-9-]{36}"))) continue
                if (!file(id).isFile) continue
                add(MantraRecording(id, entry.optString("title").take(80),
                    entry.optLong("durationMs"), entry.optLong("createdAt")))
            }
        }
    }.getOrDefault(emptyList())

    /** Private, temporary recording until the user explicitly accepts a take. */
    fun newTake(): File = File.createTempFile("take-", ".m4a", root)

    fun save(take: File, rawTitle: String, durationMs: Long): MantraRecording {
        val title = rawTitle.trim()
        require(title.isNotEmpty() && title.length <= 80 && title.none { it.isISOControl() }) {
            "Please enter a title of 1–80 characters."
        }
        require(durationMs in 300L..90_000L && take.isFile && take.length() > 100L) {
            "Recording is missing, too short or too long."
        }
        val current = list()
        require(current.size < 200) { "Maximum 200 recordings reached." }
        if (current.any { it.title.equals(title, ignoreCase = true) }) {
            throw IllegalArgumentException("A recording named \"$title\" already exists.")
        }
        val id = UUID.randomUUID().toString()
        val target = file(id)
        take.copyTo(target, overwrite = false)
        val item = MantraRecording(id, title, durationMs, System.currentTimeMillis())
        try {
            writeIndex(current + item)
        } catch (error: Exception) {
            target.delete()
            throw error
        }
        take.delete()
        return item
    }

    fun delete(item: MantraRecording) {
        clearCheckpoint(item.id)
        writeIndex(list().filterNot { it.id == item.id })
        file(item.id).delete()
    }

    /** Retrieves the saved checkpoint for [recordingId], failing closed if missing or invalid. */
    fun getCheckpoint(recordingId: String): JapaCheckpoint? {
        if (!checkpointFile.isFile) return null
        return runCatching {
            val obj = JSONObject(checkpointFile.readText())
            val id = obj.optString("recordingId")
            if (id != recordingId) return null
            if (!file(id).isFile) {
                clearCheckpoint()
                return null
            }
            val sets = obj.optInt("selectedSets")
            val gap = obj.optLong("gapMs")
            val completed = obj.optInt("completed")
            if (!MantraRepetitionPolicy.permitted(sets, gap)) return null
            val total = MantraRepetitionPolicy.totalRepetitions(sets)
            if (completed !in 0..total) return null
            JapaCheckpoint(id, sets, gap, completed, obj.optLong("updatedAt"))
        }.getOrNull()
    }

    /** Saves an ongoing japa practice checkpoint atomically. */
    fun saveCheckpoint(recordingId: String, selectedSets: Int, gapMs: Long, completed: Int): JapaCheckpoint? {
        if (!file(recordingId).isFile) {
            clearCheckpoint(recordingId)
            return null
        }
        if (!MantraRepetitionPolicy.permitted(selectedSets, gapMs)) return null
        val total = MantraRepetitionPolicy.totalRepetitions(selectedSets)
        if (completed !in 0..total) return null
        return runCatching {
            val now = System.currentTimeMillis()
            val obj = JSONObject().apply {
                put("recordingId", recordingId)
                put("selectedSets", selectedSets)
                put("gapMs", gapMs)
                put("completed", completed)
                put("updatedAt", now)
            }
            val tmp = File(root, "checkpoint.json.tmp")
            tmp.writeText(obj.toString())
            check(tmp.renameTo(checkpointFile)) { "Could not write checkpoint" }
            JapaCheckpoint(recordingId, selectedSets, gapMs, completed, now)
        }.getOrNull()
    }

    /** Clears checkpoint for a specific recording, or unconditionally if [recordingId] is null. */
    fun clearCheckpoint(recordingId: String? = null) {
        runCatching {
            if (checkpointFile.isFile) {
                if (recordingId == null) {
                    checkpointFile.delete()
                } else {
                    val obj = JSONObject(checkpointFile.readText())
                    if (obj.optString("recordingId") == recordingId) {
                        checkpointFile.delete()
                    }
                }
            }
        }
    }

    fun recordingFile(item: MantraRecording): File {
        require(item.id.matches(Regex("[a-f0-9-]{36}")))
        return file(item.id)
    }

    private fun file(id: String): File = File(root, "$id.m4a")

    private fun writeIndex(items: List<MantraRecording>) {
        val arr = JSONArray()
        items.forEach { item ->
            arr.put(JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("durationMs", item.durationMs)
                put("createdAt", item.createdAt)
            })
        }
        val staging = File(root, "recordings.json.tmp")
        staging.writeText(arr.toString())
        check(staging.renameTo(index)) { "Could not save recording catalog." }
    }
}

/** A set consists of exactly 108 chants. Set selections are NOT raw repetitions. */
object MantraRepetitionPolicy {
    const val REPS_PER_SET = 108
    val setChoices = listOf(1, 11, 21, 58, 108)
    val gapsMs = listOf(100L, 250L, 500L, 750L, 1000L, 2000L, 3000L)

    /** Per explicitly initiated foreground session — never silently extend or restart. */
    const val MAX_SESSION_MS = 30L * 60L * 1000L

    /** UI labels without floating-point rounding artifacts. */
    fun gapLabel(gapMs: Long): String = when (gapMs) {
        100L -> "0.1"
        250L -> "0.25"
        500L -> "0.5"
        750L -> "0.75"
        1000L -> "1"
        2000L -> "2"
        3000L -> "3"
        else -> throw IllegalArgumentException("Unsupported silence duration")
    }

    fun permitted(sets: Int, gapMs: Long): Boolean =
        sets in setChoices && gapMs in gapsMs

    fun totalRepetitions(sets: Int): Int {
        require(sets in setChoices)
        return Math.multiplyExact(sets, REPS_PER_SET)
    }

    fun estimatedMs(durationMs: Long, sets: Int, gapMs: Long): Long {
        require(durationMs in 300L..90_000L && permitted(sets, gapMs))
        val repetitions = totalRepetitions(sets).toLong()
        return Math.addExact(
            Math.multiplyExact(durationMs, repetitions),
            Math.multiplyExact(gapMs, repetitions - 1)
        )
    }

    /** Completed chants only; the first chant is set 1, repetition 1. */
    fun progress(completed: Int, sets: Int): MantraProgress {
        val total = totalRepetitions(sets)
        require(completed in 0..total)
        return if (completed == total) {
            MantraProgress(sets, REPS_PER_SET, completed, total)
        } else {
            MantraProgress(completed / REPS_PER_SET + 1,
                completed % REPS_PER_SET, completed, total)
        }
    }

    fun canFinishInOneSession(durationMs: Long, sets: Int, gapMs: Long): Boolean =
        estimatedMs(durationMs, sets, gapMs) <= MAX_SESSION_MS
}

data class MantraProgress(
    val currentSet: Int,
    val completedWithinSet: Int,
    val completedOverall: Int,
    val totalOverall: Int
)
