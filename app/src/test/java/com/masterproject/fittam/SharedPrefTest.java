package com.masterproject.fittam;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.InstrumentationRegistry;



import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertTrue;

/**
 * Similar to the previous issue, there is a problem with language level, API level and robolectric. ("java.lang.IllegalArgumentException: API level 29 is not available")
 *
 * Sources:http://robolectric.org/migrating/#migrating-to-40
 *         https://medium.com/@yair.kukielka/android-unit-tests-explained-219b04dc55b5
 *
 *
 */

@RunWith(RobolectricTestRunner.class)
public class SharedPrefTest {

    private SharedPreferences testPref;
    Context context = InstrumentationRegistry.getTargetContext();
    private static final int DEFAULT_STEPS = 0;

    @Before
    public void before() {
        testPref = context.getSharedPreferences(context.getString(R.string.step_preferences_file_key), Context.MODE_PRIVATE);
    }

    @Test
    public void getPref() throws Exception {
       int steps= testPref.getInt(context.getString(R.string.saved_step_count),DEFAULT_STEPS);
        assertTrue(steps!=0);
    }


}
