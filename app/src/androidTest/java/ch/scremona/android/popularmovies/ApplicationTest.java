package ch.scremona.android.popularmovies;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;


/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */

public class ApplicationTest extends ApplicationTestCase<Application> {
    final static String LOG_TAG = ApplicationTest.class.getSimpleName();

    //private MyFirstTestActivity mFirstTestActivity;
    //private TextView mFirstTestText;

    public ApplicationTest() {
        super(Application.class);
    }

    public void testPreconditions(){
        Log.d(LOG_TAG, "init testPreconditions");
        /*
        assertNotNull(“mFirstTestActivity is null”, mFirstTestActivity);
        assertNotNull(“mFirstTestText is null”, mFirstTestText);
        */
    }

    @LargeTest
    public void testA(){
        Log.d(LOG_TAG, "init testA");
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Log.d(LOG_TAG, "init setUp");

/*
        // Tipical behaviour of the setUp() method
        mFirstTestActivity = getActivity();
        mFirstTestText =
                (TextView) mFirstTestActivity
                        .findViewById(R.id.my_first_test_text_view);
*/
    }

    /*
    public void testMyFirstTestTextView_labelText() {
        final String expected =
                mFirstTestActivity.getString(R.string.my_first_test);
        final String actual = mFirstTestText.getText().toString();
        assertEquals(expected, actual);
    }
    */

/*    public void testGetReviewsFromJson (){
        Log.d(LOG_TAG, "Init testGetReviewsFromJson");

        final String BASE_URL           = getContext().getString(R.string.theMovieDb_reviewsUrl);
        final String REVIEWS_PARAM_KEY  = getContext().getString(R.string.theMovieDb_reviewsKey);
        final String KEY_PARAM_KEY      = getContext().getString(R.string.theMovieDb_apiKeyParameterKey);

        final String theMovieDb_keyStr  = getContext().getString(R.string.theMovieDb_apiKeyParameterValue);

        Uri buildURI = Uri.parse(BASE_URL).buildUpon()
                .appendPath("135397")
                .appendPath(REVIEWS_PARAM_KEY)
                .appendQueryParameter(KEY_PARAM_KEY, theMovieDb_keyStr)
                .build();

        //URL url = new URL(buildURI.toString());
        Log.d(LOG_TAG, buildURI.toString());
    }*/


    @Override
    protected void tearDown() throws Exception {
        Log.d(LOG_TAG, "init tearDown");
    }

}