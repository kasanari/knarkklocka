package se.jakob.knarkklocka

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.junit.Rule
import androidx.test.rule.ActivityTestRule
import org.junit.Test

class TimerActivityTest {
    @Rule
    @JvmField
    var activityTestRule = ActivityTestRule(TimerActivity::class.java)

    @Test fun clickOnOpenSettings() {
        onView(withId(R.id.action_settings)).perform(click())
    }

    @Test fun clickOnHistory() {
        onView(withId(R.id.action_history)).perform(click())
        onView(withId(R.id.action_history)).perform(click())
    }

    @Test fun pressFABAndThenSleep() {
        onView(withId(R.id.fab_start_timer)).perform(click())
        onView(withId(R.id.fab_start_timer)).perform(longClick())
        onView(withId(R.id.button_remove_timer)).perform(click())
        onView(withId(R.id.button_remove_timer)).perform(longClick())
    }
}