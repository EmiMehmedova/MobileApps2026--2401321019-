package com.emi.bookshelf

import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddBookFlowTest {
    @get:Rule
    val rule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun addBook_appearsInList() {
        val title = "Под игото ${System.currentTimeMillis()}"

        onView(withId(R.id.fab_add)).perform(click())
        onView(withId(R.id.input_title)).perform(replaceText(title))
        onView(withId(R.id.input_author)).perform(replaceText("Иван Вазов"))
        closeSoftKeyboard()
        onView(withId(R.id.btn_save)).perform(scrollTo(), click())

        Thread.sleep(700) // Room работи във фонова нишка; кратко изчакване

        onView(withText(title)).check(matches(isDisplayed()))
    }
}
