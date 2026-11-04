package com.pifauna.app.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
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
}
