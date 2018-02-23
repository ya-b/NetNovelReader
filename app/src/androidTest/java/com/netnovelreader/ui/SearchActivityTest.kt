package com.netnovelreader.ui

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.UiController
import android.support.test.espresso.ViewAction
import android.support.test.espresso.action.ViewActions.pressKey
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import android.support.v7.widget.SearchView
import android.view.KeyEvent
import android.view.View
import com.netnovelreader.R
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test


class SearchActivityTest {

    @Rule
    @JvmField
    var searchActivityRule = ActivityTestRule<SearchActivity>(SearchActivity::class.java)

    @Test
    fun onSearchViewEditNotNull() {
        onView(withId(R.id.searchViewBar)).perform(typeSearchViewText("极道天魔"))
        onView(withId(R.id.linearLayout)).check(matches(not(isDisplayed())))
    }

    @Test
    fun onSearchViewEditisNull() {
        onView(withId(R.id.searchViewBar)).perform(typeSearchViewText(""))
        onView(withId(R.id.linearLayout)).check(matches(isDisplayed()))
    }

    @Test
    fun search() {
        onView(withId(R.id.searchViewBar)).perform(
            typeSearchViewText("极道天魔"),
            pressKey(KeyEvent.KEYCODE_SEARCH)
        )
        onView(withId(R.id.searchRecycler)).check(matches(isDisplayed()))
    }

    fun typeSearchViewText(text: String): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                //Ensure that only apply if it is a SearchView and if it is visible.
                return allOf(isDisplayed(), isAssignableFrom(SearchView::class.java))
            }

            override fun getDescription(): String {
                return "Change view text"
            }

            override fun perform(uiController: UiController, view: View) {
                (view as SearchView).setQuery(text, false)
            }
        }
    }
}