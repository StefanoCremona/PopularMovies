/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.scremona.android.popularmovies.data;

import android.content.UriMatcher;
import android.test.AndroidTestCase;

import ch.scremona.android.popularmovies.R;

/*
    Uncomment this class when you are ready to test your UriMatcher.  Note that this class utilizes
    constants that are declared with package protection inside of the UriMatcher, which is why
    the test must be in the same data package as the Android app code.  Doing the test this way is
    a nice compromise between data hiding and testability.
 */
public class TestUriMatcher extends AndroidTestCase {

    /*
        This function tests that your UriMatcher returns the correct integer value
        for each of the Uri types that our ContentProvider can handle.  Uncomment this when you are
        ready to test your UriMatcher.
     */
    public void testUriMatcher() {
        UriMatcher testMatcher = MoviesProvider.buildUriMatcher();

        assertEquals("Error: The MOVIE URI was matched incorrectly.",
                testMatcher.match(  MoviesContract.MovieEntry.CONTENT_URI),
                MoviesProvider.CODE_MOVIE);

        assertEquals("Error: The MOVIE URI by id was matched incorrectly.",
                testMatcher.match(  MoviesContract.MovieEntry.buildMovieUri(123L)),
                                    MoviesProvider.CODE_MOVIE_BYID);

        assertEquals("Error: The MOVIE_SORTED URI was matched incorrectly.",
                testMatcher.match(  MoviesContract.MovieEntry.buildMoviesSorted(getContext().getString(R.string.theMovieDb_sortByRatingParameterValue))),
                                    MoviesProvider.CODE_SORTED_MOVIES);

        assertEquals("Error: The REVIEW URI was matched incorrectly.",
                testMatcher.match(  MoviesContract.ReviewEntry.buildReviewUri(123L)),
                MoviesProvider.CODE_REVIEW_BYID);
        assertEquals("Error: The REVIEW_BY_MOVIE URI was matched incorrectly.",
                testMatcher.match(  MoviesContract.ReviewEntry.buildReviewsByMovie("123456")),
                MoviesProvider.CODE_MOVIE_REVIEWS);

        assertEquals("Error: The TRAILERS URI was matched incorrectly.",
                testMatcher.match(  MoviesContract.TrailerEntry.buildTrailerUri(123L)),
                                    MoviesProvider.CODE_TRAILER_BYID);
        assertEquals("Error: The TRAILERS_BY_MOVIE URI was matched incorrectly.",
                testMatcher.match(  MoviesContract.TrailerEntry.buildTrailersByMovie("123456")),
                MoviesProvider.CODE_MOVIE_TRAILERS);

        //TO_DO create test case for the Trailers!!!

        /*assertEquals("Error: The WEATHER WITH LOCATION URI was matched incorrectly.",
                testMatcher.match(TEST_WEATHER_WITH_LOCATION_DIR), WeatherProvider.WEATHER_WITH_LOCATION);
        assertEquals("Error: The WEATHER WITH LOCATION AND DATE URI was matched incorrectly.",
                testMatcher.match(TEST_WEATHER_WITH_LOCATION_AND_DATE_DIR), WeatherProvider.WEATHER_WITH_LOCATION_AND_DATE);
        assertEquals("Error: The LOCATION URI was matched incorrectly.",
                testMatcher.match(TEST_LOCATION_DIR), WeatherProvider.LOCATION);*/
    }
}
