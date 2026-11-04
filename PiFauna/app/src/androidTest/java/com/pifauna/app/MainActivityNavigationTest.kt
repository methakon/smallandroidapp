package com.pifauna.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented smoke tests for navigation only.
 *
 * They deliberately do not start playback: running a real tone unattended on a
 * physical device is exactly what this app exists to prevent. Playback behaviour
 * is covered by unit tests of the synthesiser and safety policy plus the manual
 * device matrix in docs/TESTING.md.
 */
@RunWith(AndroidJUnit4::class)
class MainActivityNavigationTest {

    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private fun dismissWelcomeIfShown() {
        val started = compose.onAllNodesWithText("Get started").fetchSemanticsNodes().isNotEmpty()
        if (started) {
            compose.onNodeWithText("Get started").performClick()
            compose.waitForIdle()
        }
    }

    @Test
    fun appLaunchesToAUsableScreen() {
        compose.onNodeWithText("PiFauna", substring = true).assertIsDisplayed()
    }

    @Test
    fun homeListsProfilesAndOpensThePlayer() {
        dismissWelcomeIfShown()
        compose.onNodeWithText("Choose a sound profile").assertIsDisplayed()

        compose.onNodeWithText("Mosquito / insect").performClick()
        compose.waitForIdle()

        compose.onNodeWithText("Start timed playback").assertIsDisplayed()
        compose.onNodeWithText("Stop playback").assertDoesNotExist()
    }

    @Test
    fun bottomBarReachesProfilesAndSettings() {
        dismissWelcomeIfShown()

        compose.onNodeWithText("Profiles").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Explore profiles").assertIsDisplayed()

        compose.onNodeWithText("Settings").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Preferences").assertIsDisplayed()
        compose.onNodeWithText("Background playback").assertIsDisplayed()
    }

    @Test
    fun safetyScreenIsReachableFromSettings() {
        dismissWelcomeIfShown()
        compose.onNodeWithText("Settings").performClick()
        compose.waitForIdle()

        compose.onNodeWithText("Sound & hearing safety").performClick()
        compose.waitForIdle()

        compose.onNodeWithText("PLEASE READ").assertIsDisplayed()
        compose.onNodeWithText("Effectiveness is not established").assertIsDisplayed()
    }

    @Test
    fun timerScreenOffersNoContinuousMode() {
        dismissWelcomeIfShown()
        compose.onNodeWithText("Mosquito / insect").performClick()
        compose.waitForIdle()

        compose.onNodeWithText("Duration • 10 minutes").performClick()
        compose.waitForIdle()

        compose.onNodeWithText("Playback schedule").assertIsDisplayed()
        compose.onNodeWithText("Continuous").assertIsDisplayed()
        compose.onNodeWithText("Not offered").assertIsDisplayed()
    }
}
