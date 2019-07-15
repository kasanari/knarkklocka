package se.jakob.knarkklocka

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Test

class TimerActivityTest {
    @Rule
    @JvmField
    var activityTestRule = ActivityTestRule(TimerActivity::class.java)

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test fun clickOnOpenSettings() {
        onView(withId(R.id.action_settings)).perform(click())
    }

    @Test fun clickOnHistory() {
        onView(withId(R.id.action_history)).perform(click())
        onView(withId(R.id.action_history)).perform(click())
    }

    @Test fun pressFABAndThenSleep() {
        onView(withId(R.id.button_start_timer)).perform(click())
        onView(withId(R.id.button_start_timer)).perform(longClick())
        onView(withId(R.id.button_remove_timer)).perform(click())
        onView(withId(R.id.button_remove_timer)).perform(longClick())
    }
}