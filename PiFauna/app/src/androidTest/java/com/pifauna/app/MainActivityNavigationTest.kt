package com.pifauna.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented smoke tests for navigation only.
 *
 * They deliberately do not start playback: running a real tone unattended on a
 * physical device is exactly what this app exists to prevent. Playback behaviour
 * is covered by unit tests of the synthesiser and safety policy plus the device
 * matrix in docs/TESTING.md.
 *
 * Screens are tall scrollable columns, so content below the fold is scrolled
 * into view before a display assertion.
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
        // Exact brand match; avoids accidental matches in explanatory copy.
        compose.onNodeWithText("Layaswarna").assertIsDisplayed()
        compose.onNodeWithText("Profiles").assertIsDisplayed()
    }

    @Test
    fun homeListsProfilesAndOpensThePlayer() {
        dismissWelcomeIfShown()
        compose.onNodeWithText("Sound profile regulator").assertIsDisplayed()

        compose.onNodeWithText("Open Mosquito").performScrollTo().performClick()
        compose.waitForIdle()

        // The Figma artboard is 390x844; on this 640 dp screen the primary action
        // sits below the fold, so it is scrolled into view before asserting.
        compose.onNodeWithText("▶ Start").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("■ Stop").assertDoesNotExist()
    }

    @Test
    fun bottomBarReachesProfilesAndSettings() {
        dismissWelcomeIfShown()

        compose.onNodeWithText("Profiles").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Profile library regulator").assertIsDisplayed()

        compose.onNodeWithText("Settings").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Preferences").assertIsDisplayed()
        compose.onNodeWithText("Continue timed playback with screen locked")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun safetyScreenIsReachableFromSettings() {
        dismissWelcomeIfShown()
        compose.onNodeWithText("Settings").performClick()
        compose.waitForIdle()

        compose.onNodeWithText("Sound & hearing safety").performScrollTo().performClick()
        compose.waitForIdle()

        compose.onNodeWithText("PLEASE READ").assertIsDisplayed()
        compose.onNodeWithText("Effectiveness is not established")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun timerScreenOffersNoContinuousMode() {
        dismissWelcomeIfShown()
        compose.onNodeWithText("Open Mosquito").performScrollTo().performClick()
        compose.waitForIdle()

        compose.onNodeWithText("Duration • 10 minutes").performClick()
        compose.waitForIdle()

        compose.onNodeWithText("Session timer regulator").assertIsDisplayed()
        // The continuous option must not be offered as an action.
        compose.onNodeWithText("Continuous and overnight listening are not available.")
            .performScrollTo().assertIsDisplayed()
    }
    @Test
    fun chakraLibraryIsReachableAndDoesNotStartSound() {
        dismissWelcomeIfShown()
        compose.onNodeWithText("Seven Chakra Library").performScrollTo().performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Open Anahata").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("CHAKRA INSIGHTS · ANAHATA").performScrollTo().assertIsDisplayed()
    }

    /**
     * The privacy entry used to be a disabled placeholder. It now opens the
     * policy bundled inside the APK, so the document must be readable with no
     * network and no account.
     */
    @Test
    fun privacyPolicyIsReachableFromSettings() {
        dismissWelcomeIfShown()
        compose.onNodeWithText("Settings").performClick()
        compose.waitForIdle()

        compose.onNodeWithText("About & privacy").performScrollTo().performClick()
        compose.waitForIdle()

        compose.onNodeWithText("OFFLINE COPY").assertIsDisplayed()
        // This screen is a fixed layout (the WebView takes the free space), so
        // the action sits at the bottom and needs no scrolling.
        compose.onNodeWithText("Open the published copy").assertIsDisplayed()
    }

}
