package com.pifauna.app.mantra

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class MantraStoreTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun createStore(): Pair<MantraStore, File> {
        val dir = tempFolder.newFolder("mantras_test")
        val store = MantraStore(baseDir = dir)
        return store to dir
    }

    private fun createDummyTake(store: MantraStore, bytes: Int = 1024): File {
        val file = store.newTake()
        file.writeBytes(ByteArray(bytes) { 1 })
        return file
    }

    @Test
    fun `save and list round trip`() {
        val (store, _) = createStore()
        val take = createDummyTake(store)

        val saved = store.save(take, "Gayatri Mantra", 5_000L)
        assertEquals("Gayatri Mantra", saved.title)
        assertEquals(5_000L, saved.durationMs)
        assertTrue(saved.id.matches(Regex("[a-f0-9-]{36}")))
        assertFalse("Temporary take should be deleted after save", take.exists())

        val list = store.list()
        assertEquals(1, list.size)
        assertEquals(saved.id, list.first().id)
        assertEquals(saved.title, list.first().title)
        assertEquals(saved.durationMs, list.first().durationMs)
    }

    @Test
    fun `pruneOrphanTakes removes uncommitted take files on initialization`() {
        val dir = tempFolder.newFolder("orphan_test")
        val orphan1 = File(dir, "take-123.m4a").apply { writeText("dummy1") }
        val orphan2 = File(dir, "take-456.m4a").apply { writeText("dummy2") }
        val legitFile = File(dir, "legit.m4a").apply { writeText("keep me") }

        assertTrue(orphan1.exists())
        assertTrue(orphan2.exists())
        assertTrue(legitFile.exists())

        // Initializing store will trigger pruneOrphanTakes()
        val store = MantraStore(baseDir = dir)
        assertFalse("Orphan 1 must be pruned", orphan1.exists())
        assertFalse("Orphan 2 must be pruned", orphan2.exists())
        assertTrue("Legit file must remain", legitFile.exists())
    }

    @Test
    fun `save rejects duplicate titles case-insensitively`() {
        val (store, _) = createStore()
        val take1 = createDummyTake(store)
        store.save(take1, "Maha Mrityunjaya", 8_000L)

        val take2 = createDummyTake(store)
        val error = assertThrows(IllegalArgumentException::class.java) {
            store.save(take2, "maha mrityunjaya", 8_000L)
        }
        assertTrue(error.message!!.contains("already exists"))
        assertTrue("Failed save should not keep uncommitted take as saved", take2.exists())
    }

    @Test
    fun `save rejects invalid titles`() {
        val (store, _) = createStore()

        // Empty
        val take1 = createDummyTake(store)
        assertThrows(IllegalArgumentException::class.java) {
            store.save(take1, "   ", 2_000L)
        }

        // Over 80 chars
        val take2 = createDummyTake(store)
        assertThrows(IllegalArgumentException::class.java) {
            store.save(take2, "A".repeat(81), 2_000L)
        }

        // Control characters
        val take3 = createDummyTake(store)
        assertThrows(IllegalArgumentException::class.java) {
            store.save(take3, "Mantra\nTitle", 2_000L)
        }
    }

    @Test
    fun `save rejects invalid duration or empty file`() {
        val (store, _) = createStore()

        // Too short (< 300 ms)
        val take1 = createDummyTake(store)
        assertThrows(IllegalArgumentException::class.java) {
            store.save(take1, "Om", 200L)
        }

        // Too long (> 90 seconds)
        val take2 = createDummyTake(store)
        assertThrows(IllegalArgumentException::class.java) {
            store.save(take2, "Long Chant", 95_000L)
        }

        // Empty / tiny file (<= 100 bytes)
        val tinyTake = store.newTake().apply { writeBytes(ByteArray(50)) }
        assertThrows(IllegalArgumentException::class.java) {
            store.save(tinyTake, "Empty", 2_000L)
        }
    }

    @Test
    fun `delete removes recording from catalog and disk`() {
        val (store, _) = createStore()
        val take = createDummyTake(store)
        val saved = store.save(take, "Om Shanti", 3_000L)
        val audioFile = store.recordingFile(saved)
        assertTrue(audioFile.exists())

        store.delete(saved)
        assertTrue(store.list().isEmpty())
        assertFalse(audioFile.exists())
    }

    @Test
    fun `corrupted index gracefully recovers to empty list`() {
        val (store, dir) = createStore()
        val index = File(dir, "recordings.json")
        index.writeText("{ corrupted json [[")

        val list = store.list()
        assertTrue(list.isEmpty())
    }

    @Test
    fun `checkpoint save and get round trip`() {
        val (store, _) = createStore()
        val take = createDummyTake(store)
        val saved = store.save(take, "Daily Japa", 4_000L)

        val cp = store.saveCheckpoint(saved.id, selectedSets = 11, gapMs = 1000L, completed = 42)
        assertTrue(cp != null)
        assertEquals(42, cp!!.completed)
        assertEquals(11, cp.selectedSets)
        assertEquals(1000L, cp.gapMs)

        val retrieved = store.getCheckpoint(saved.id)
        assertTrue(retrieved != null)
        assertEquals(42, retrieved!!.completed)
        assertEquals(11, retrieved.selectedSets)
        assertEquals(1000L, retrieved.gapMs)
    }

    @Test
    fun `delete recording invalidates checkpoint`() {
        val (store, _) = createStore()
        val take = createDummyTake(store)
        val saved = store.save(take, "Temporary Japa", 3_000L)

        store.saveCheckpoint(saved.id, selectedSets = 1, gapMs = 500L, completed = 50)
        assertTrue(store.getCheckpoint(saved.id) != null)

        store.delete(saved)
        assertTrue(store.getCheckpoint(saved.id) == null)
    }

    @Test
    fun `checkpoint fails closed on invalid parameters or out-of-range completion`() {
        val (store, _) = createStore()
        val take = createDummyTake(store)
        val saved = store.save(take, "Safe Japa", 3_000L)

        // Invalid sets (e.g. 5 sets, not in 1, 11, 21, 58, 108)
        val invalidSets = store.saveCheckpoint(saved.id, selectedSets = 5, gapMs = 1000L, completed = 10)
        assertTrue(invalidSets == null)

        // Invalid gap (e.g. 1500L, not supported)
        val invalidGap = store.saveCheckpoint(saved.id, selectedSets = 1, gapMs = 1500L, completed = 10)
        assertTrue(invalidGap == null)

        // Out of range completion (> 108 for 1 set)
        val outOfRange = store.saveCheckpoint(saved.id, selectedSets = 1, gapMs = 1000L, completed = 109)
        assertTrue(outOfRange == null)

        // Negative completion
        val negative = store.saveCheckpoint(saved.id, selectedSets = 1, gapMs = 1000L, completed = -1)
        assertTrue(negative == null)
    }
}
