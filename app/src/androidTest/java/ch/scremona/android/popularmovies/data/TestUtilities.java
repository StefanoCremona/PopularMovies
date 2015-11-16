package ch.scremona.android.popularmovies.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;

import java.util.Map;
import java.util.Set;

/**
 * Created by stefanocremona on 17/10/15.
 */
public class TestUtilities extends AndroidTestCase {
    static final String TEST_SORTBY = "popularity.desc";

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    static ContentValues createJurassicWorldMovieValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(MoviesContract.MovieEntry.COLUMN_THEMOVIEDB_ID  , 135397);
        testValues.put(MoviesContract.MovieEntry.COLUMN_SORTBY_SETTING , TEST_SORTBY);
        testValues.put(MoviesContract.MovieEntry.COLUMN_ORIGINAL_TITLE , "Jurassic World");
        testValues.put(MoviesContract.MovieEntry.COLUMN_POSTER_PATH    , "/jjBgi2r5cRt36xF6iNUEhzscEcb.jpg");
        testValues.put(MoviesContract.MovieEntry.COLUMN_OVERVIEW       , "Twenty-two years after the events of Jurassic Park, Isla Nublar now features a fully functioning dinosaur theme park, Jurassic World, as originally envisioned by John Hammond.");
        testValues.put(MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE   , 6.9);
        testValues.put(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE   , "2015-06-12");

        return testValues;
    }

    static ContentValues[] createReviewValues(long movieRowId){
        ContentValues rev1 = new ContentValues();
        rev1.put(MoviesContract.ReviewEntry.COLUMN_MOVIE_KEY, movieRowId);
        rev1.put(MoviesContract.ReviewEntry.COLUMN_AUTHOR, "jonlikesmoviesthatdontsuck");
        rev1.put(MoviesContract.ReviewEntry.COLUMN_CONTENT, "I was a huge fan of the original 3 movies, they were out when I was younger, and I grew up loving dinosaurs because of them. This movie was awesome, and I think it can stand as a testimonial piece towards the capabilities that Christopher Pratt has. He nailed it. The graphics were awesome, the supporting cast did great and the t rex saved the child in me. 10\\5 stars, four thumbs up, and I hope that star wars episode VII doesn't disappoint,");

        ContentValues rev2 = new ContentValues();
        rev2.put(MoviesContract.ReviewEntry.COLUMN_MOVIE_KEY, movieRowId);
        rev2.put(MoviesContract.ReviewEntry.COLUMN_AUTHOR, "Ganesan");
        rev2.put(MoviesContract.ReviewEntry.COLUMN_CONTENT, "Overall action packed movie... But there should be more puzzles in the climax... But I really love the movie.... Excellent...");

        ContentValues rev3 = new ContentValues();
        rev3.put(MoviesContract.ReviewEntry.COLUMN_MOVIE_KEY, movieRowId);
        rev3.put(MoviesContract.ReviewEntry.COLUMN_AUTHOR, "Stefano");
        rev3.put(MoviesContract.ReviewEntry.COLUMN_CONTENT, "Facciamo una prova...");

        return new ContentValues[]{rev1, rev2, rev3};
    }

    static ContentValues[] createTrailerValues(long movieRowId){
        ContentValues rev1 = new ContentValues();
        rev1.put(MoviesContract.TrailerEntry.COLUMN_MOVIE_KEY, movieRowId);
        rev1.put(MoviesContract.TrailerEntry.COLUMN_YOUTUBE_KEY, "lP-sUUUfamw");
        rev1.put(MoviesContract.TrailerEntry.COLUMN_NAME, "Official Trailer 3");

        ContentValues rev2 = new ContentValues();
        rev2.put(MoviesContract.TrailerEntry.COLUMN_MOVIE_KEY, movieRowId);
        rev2.put(MoviesContract.TrailerEntry.COLUMN_YOUTUBE_KEY, "bvu-zlR5A8Q");
        rev2.put(MoviesContract.TrailerEntry.COLUMN_NAME, "Teaser");

        ContentValues rev3 = new ContentValues();
        rev3.put(MoviesContract.TrailerEntry.COLUMN_MOVIE_KEY, movieRowId);
        rev3.put(MoviesContract.TrailerEntry.COLUMN_YOUTUBE_KEY, "RFinNxS5KN4");
        rev3.put(MoviesContract.TrailerEntry.COLUMN_NAME, "Official Trailer");

        return new ContentValues[]{rev1, rev2, rev3};
    }
}
