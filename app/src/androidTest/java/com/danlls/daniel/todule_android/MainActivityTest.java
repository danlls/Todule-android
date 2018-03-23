package com.danlls.daniel.todule_android;


import android.support.design.widget.TextInputLayout;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.web.proto.sugar.WebSugar;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;
import android.view.View;

import com.danlls.daniel.todule_android.activities.MainActivity;
import com.danlls.daniel.todule_android.layout.CheckableLinearLayout;
import com.danlls.daniel.todule_android.provider.ToduleDBContract;
import com.danlls.daniel.todule_android.provider.ToduleProvider;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.hasErrorText;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;

/**
 * Created by danieL on 3/20/2018.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> mainActivityActivityTestRule =
            new ActivityTestRule<MainActivity>(MainActivity.class);


    @Before
    public void setUp() throws Exception{
        Espresso.closeSoftKeyboard();
        mainActivityActivityTestRule.getActivity().getSupportFragmentManager()
                .beginTransaction();
    }

    @Test
    public void clickAddToduleFab_opensAddFragment(){
        onView(withId(R.id.add_fab)).perform(click());
        onView(withId(R.id.edit_title))
                .check(matches(isDisplayed()));
    }

    @Test
    public void addNewTodule_validateRequiredFields() {
        onView(withId(R.id.add_fab)).perform(click());
        onView(withId(R.id.action_save)).perform(click());
        onView(withId(R.id.edit_title_wrapper))
                .check(matches(hasTextInputLayoutErrorText("This field is required.")));
    }

    @Test
    public void ToduleAddFragment_addNewTodule()  {
        onView(withId(R.id.add_fab)).perform(click());
        onView(withId(R.id.edit_title)).perform(typeText("unique"));
        onView(withId(R.id.edit_description)).perform(typeText("testdesc"));
        onView(withId(R.id.action_save)).perform(click());
    }

    @Test
    public void ToduleListFragment_deleteSingleTodule(){
        addNewTodule(1);
        onData(anything())
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0)
                .onChildView(withId(R.id.title_text))
                .perform(longClick());
        onView(withId(R.id.action_soft_delete))
                .perform(click());
        onView(allOf(withId(android.support.design.R.id.snackbar_text), withText("1 Deleted")))
                .check(matches(isDisplayed()));
        onView(allOf(withId(android.support.design.R.id.snackbar_action), withText("Undo")))
                .check(matches(isDisplayed()));
    }

    @Test
    public void ToduleListFragment_deleteMultipleTodule(){
        addNewTodule(3);
        onData(anything())
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0)
                .onChildView(withId(R.id.title_text))
                .perform(longClick());
        onData(anything())
                .inAdapterView(withId(android.R.id.list))
                .atPosition(1)
                .onChildView(withId(R.id.title_text))
                .perform(click());
        onData(anything())
                .inAdapterView(withId(android.R.id.list))
                .atPosition(2)
                .onChildView(withId(R.id.title_text))
                .perform(click());
        onView(withId(R.id.action_soft_delete))
                .perform(click());
        onView(allOf(withId(android.support.design.R.id.snackbar_text), withText("3 Deleted")))
                .check(matches(isDisplayed()));
    }


    public void addNewTodule(int count)  {
        for(int i=0; i<count; i++){
            onView(withId(R.id.add_fab)).perform(click());
            onView(withId(R.id.edit_title)).perform(typeText("test " + i));
            onView(withId(R.id.edit_description)).perform(typeText("testdesc " + i));
            onView(withId(R.id.action_save)).perform(click());
        }
    }

    public static Matcher<View> hasTextInputLayoutErrorText(final String expectedErrorText) {
        return new TypeSafeMatcher<View>() {

            @Override
            public boolean matchesSafely(View view) {
                if (!(view instanceof TextInputLayout)) {
                    return false;
                }

                CharSequence error = ((TextInputLayout) view).getError();

                if (error == null) {
                    return false;
                }

                String hint = error.toString();

                return expectedErrorText.equals(hint);
            }

            @Override
            public void describeTo(Description description) {
            }
        };
    }

}


