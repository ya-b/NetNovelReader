package com.netnovelreader.ui.activity


import android.support.test.InstrumentationRegistry.getInstrumentation
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.intent.Intents
import android.support.test.espresso.intent.Intents.intended
import android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.view.View
import android.view.ViewGroup
import com.netnovelreader.R
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.*
import org.hamcrest.TypeSafeMatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class ShelfActivityTest {
    @Before
    fun init() {
        Intents.init()
    }

    @After
    fun destroy() {
        Intents.release()
    }

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(ShelfActivity::class.java)

    @Test
    fun tabRight() {
        val tabView = onView(
            allOf(
                childAtPosition(
                    childAtPosition(
                        withId(R.id.shelfTab),
                        0
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        tabView.perform(click())
        Thread.sleep(100)
        onView(withId(R.id.malelabel)).check(ViewAssertions.matches(isDisplayed()))
        onView(withId(R.id.shelfRecycler)).check(ViewAssertions.matches(not(isDisplayed())))
    }

    @Test
    fun tabLeft() {
        val tabView = onView(
            allOf(
                childAtPosition(
                    childAtPosition(
                        withId(R.id.shelfTab),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        tabView.perform(click())
        Thread.sleep(100)
        onView(withId(R.id.shelfRecycler)).check(ViewAssertions.matches(isDisplayed()))
        onView(withId(R.id.malelabel)).check(ViewAssertions.matches(not(isDisplayed())))
    }

    @Test
    fun startSettingActivity() {
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext())
        val appCompatTextView = onView(
            allOf(
                withId(R.id.title), withText("设置"),
                childAtPosition(
                    childAtPosition(
                        withClassName(`is`("android.support.v7.view.menu.ListMenuItemView")),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        appCompatTextView.perform(click())
        Thread.sleep(500)
        intended(hasComponent(SettingActivity::class.java.getName()))


        val appCompatImageButton = onView(
            allOf(
                withContentDescription("转到上一层级"),
                childAtPosition(
                    allOf(
                        withId(R.id.settingToolbar),
                        childAtPosition(
                            withClassName(`is`("android.support.design.widget.AppBarLayout")),
                            0
                        )
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        appCompatImageButton.perform(click())
    }

    @Test
    fun startSearchActivity() {
        onView(withId(R.id.search_button)).perform(click())
        Thread.sleep(500)
        intended(hasComponent(SearchActivity::class.java.getName()))
        onView(withId(R.id.backButton)).check(ViewAssertions.matches(isDisplayed()))
            .perform(click())
    }


    private fun childAtPosition(
        parentMatcher: Matcher<View>, position: Int
    ): Matcher<View> {

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
