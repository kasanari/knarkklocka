package se.jakob.knarkklocka


import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class TimerStartTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(TimerActivity::class.java)

    @Test
    fun timerStartTest() {

        Thread.sleep(700)

        val imageButton = onView(withId(R.id.fab_start_timer))
        imageButton.check(matches(isDisplayed()))

        val floatingActionButton = onView(
                allOf(withId(R.id.fab_start_timer),
                        childAtPosition(
                                allOf(withId(R.id.timer_controls),
                                        childAtPosition(
                                                withId(R.id.timer_content),
                                                1)),
                                1),
                        isDisplayed()))
        floatingActionButton.perform(longClick())

        val button = onView(
                allOf(withId(R.id.button_remove_timer),
                        childAtPosition(
                                allOf(withId(R.id.timer_controls),
                                        childAtPosition(
                                                withId(R.id.timer_content),
                                                1)),
                                0),
                        isDisplayed()))
        button.check(matches(isDisplayed()))

        val chronometer = onView(allOf(withId(R.id.chronometer_main), isDisplayed()))
        chronometer.check(matches(isDisplayed()))
    }

    private fun childAtPosition(
            parentMatcher: Matcher<View>, position: Int): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }
}
