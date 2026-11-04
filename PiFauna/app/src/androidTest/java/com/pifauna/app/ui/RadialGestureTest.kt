package com.pifauna.app.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.geometry.Offset
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Gesture tests avoid audio: activation is just a counted callback. */
@RunWith(AndroidJUnit4::class)
class RadialGestureTest {
    @get:Rule val compose = createComposeRule()

    private val options = listOf(
        RadialOption("first", "First", "first profile", RadialSymbol.SINE),
        RadialOption("second", "Second", "second profile", RadialSymbol.SWEEP),
        RadialOption("third", "Third", "third profile", RadialSymbol.HOP)
    )

    private fun show(activated: AtomicInteger) {
        compose.setContent {
            var selected by remember { mutableStateOf("first") }
            PiFaunaTheme {
                LayaswarnaRadialSelector(
                    options = options,
                    selectedId = selected,
                    onSelect = { selected = it },
                    onActivate = { activated.incrementAndGet() },
                    actionLabel = "Open selected profile"
                )
            }
        }
    }

    @Test fun simpleTapOnlyChangesHighlight() {
        val counter = AtomicInteger()
        show(counter)
        compose.onNodeWithContentDescription("Second, second profile", substring = true).performClick()
        compose.onNodeWithContentDescription("Second, second profile", substring = true).assertIsSelected()
        assertEquals(0, counter.get())
    }

    /**
     * Regression guard for selection latency: with onDoubleClick set,
     * combinedClickable cannot report a single tap until the double-tap window
     * expires. The highlight must therefore be applied on press, without the test
     * clock advancing.
     */
    @Test fun tapHighlightsImmediatelyWithoutWaitingForDoubleTapWindow() {
        val counter = AtomicInteger()
        show(counter)
        compose.onNodeWithContentDescription("Second, second profile", substring = true).performClick()
        compose.onNodeWithContentDescription("Second, second profile", substring = true)
            .assertIsSelected()
        assertEquals("a tap must not activate", 0, counter.get())
    }

    @Test fun stationaryLongPressActivatesOnce() {
        val counter = AtomicInteger()
        show(counter)
        compose.onNodeWithContentDescription("Second, second profile", substring = true)
            .performTouchInput { longClick() }
        compose.waitForIdle()
        assertEquals(1, counter.get())
    }

    @Test fun doubleTapActivatesOnce() {
        val counter = AtomicInteger()
        show(counter)
        compose.onNodeWithContentDescription("Second, second profile", substring = true)
            .performTouchInput { doubleClick() }
        compose.waitForIdle()
        assertEquals(1, counter.get())
    }

    /**
     * Rotating past the wheel must select a different node and must never open the
     * one the finger started on.
     */
    @Test fun dragRotationChangesSelectionWithoutActivating() {
        val counter = AtomicInteger()
        show(counter)
        compose.onNodeWithContentDescription("First, first profile", substring = true)
            .performTouchInput {
                down(center)
                advanceEventTime(16)
                moveTo(center + Offset(260f, 260f))
                advanceEventTime(16)
                up()
            }
        compose.waitForIdle()
        compose.onNodeWithContentDescription("First, first profile", substring = true)
            .assertIsNotSelected()
        assertEquals("rotation must not open anything", 0, counter.get())
    }

    /**
     * A long press that turns into a rotation is a selection gesture, not an
     * activation. Moving past touch slop before the long-press timeout must cancel it.
     */
    @Test fun rotationCancelsPendingLongPress() {
        val counter = AtomicInteger()
        show(counter)
        compose.onNodeWithContentDescription("Second, second profile", substring = true)
            .performTouchInput {
                down(center)
                advanceEventTime(80)
                moveTo(center + Offset(220f, 0f))
                advanceEventTime(900)   // well past the long-press timeout
                up()
            }
        compose.waitForIdle()
        assertEquals("long press should have been cancelled by rotation", 0, counter.get())
    }

    /** The centre is a readout; tapping it must not open anything. */
    @Test fun centreReadoutIsNotAnAction() {
        val counter = AtomicInteger()
        show(counter)
        compose.onNodeWithText("SELECTED").performClick()
        compose.waitForIdle()
        assertEquals(0, counter.get())
    }
}
