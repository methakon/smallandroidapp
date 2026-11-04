package com.pifauna.app

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Launch guard for the oldest supported platform.
 *
 * This exists because of a class-verification failure that no lint rule and no
 * JVM unit test could see: referencing an API 29 type in a field signature makes
 * ART reject the whole class on Android 8 and 9, so the activity died in
 * onCreate before any `Build.VERSION` check could run. Installing and launching
 * on API 26–28 is the only reliable check, and this test performs it.
 */
@RunWith(AndroidJUnit4::class)
class AppLaunchTest {

    @Test
    fun activityReachesResumedWithoutCrashing() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                assertEquals(
                    "MainActivity did not reach RESUMED",
                    Lifecycle.State.RESUMED,
                    activity.lifecycle.currentState
                )
            }
        }
    }

    @Test
    fun activityCanBeRecreated() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.recreate()
            scenario.onActivity { activity ->
                assertEquals(
                    "MainActivity did not survive recreation",
                    Lifecycle.State.RESUMED,
                    activity.lifecycle.currentState
                )
            }
        }
    }
}
