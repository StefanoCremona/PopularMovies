package ch.scremona.android.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by stefanocremona on 17/10/15.
 */
public class MoviesContract {
    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "ch.scremona.android.popularmovies.app";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://com.example.android.sunshine.app/weather/ is a valid path for
    // looking at weather data. content://com.example.android.sunshine.app/givemeroot/ will fail,
    // as the ContentProvider hasn't been given any information on what to do with "givemeroot".
    // At least, let's hope not.  Don't be that dev, reader.  Don't be that dev.
    public static final String PATH_MOVIE      = "movies";
    public static final String PATH_REVIEW     = "reviews";
    public static final String PATH_TRAILER    = "trailers";

    public static final class MovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        // Table name
        public static final String TABLE_NAME = "movie";

        // The location setting string is what will be sent to movieDb
        // as the sort order query.
        public static final String COLUMN_THEMOVIEDB_ID     = "themoviedb_id";
        public static final String COLUMN_SORTBY_SETTING    = "sortby_setting";

        public static final String COLUMN_ORIGINAL_TITLE    = "original_title";
        public static final String COLUMN_POSTER_PATH       = "poster_path";
        public static final String COLUMN_OVERVIEW          = "overview";
        public static final String COLUMN_VOTE_AVERAGE      = "vote_average";
        public static final String COLUMN_RELEASE_DATE      = "release_date";

        public static Uri buildMoviesSorted(String sortbySetting){
            //content://ch.scremona.android.popularmovies.app/movies/sortbySetting
            return CONTENT_URI.buildUpon().appendPath(sortbySetting).build();
        }

        public static String getSortBySettingFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
        public static String getIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class ReviewEntry implements  BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_REVIEW).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEW;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEW;

        public static final String TABLE_NAME = "review";

        // Column with the foreign key into the movie table.
        public static final String COLUMN_MOVIE_KEY = "movie_id";

        public static final String COLUMN_AUTHOR  = "author";
        public static final String COLUMN_CONTENT = "content";

        public static Uri buildReviewsByMovie(String movieId){
            //TO_DO make the url as similar as the original one
            //content://ch.scremona.android.popularmovies.app/reviews/movieId
            return MovieEntry.CONTENT_URI.buildUpon().appendPath(movieId).appendPath(PATH_REVIEW).build();
        }

        public static String getMovieIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
        public static String getIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
        public static Uri buildReviewUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class TrailerEntry implements  BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRAILER).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRAILER;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRAILER;

        public static final String TABLE_NAME = "trailer";

        // Column with the foreign key into the movie table.
        public static final String COLUMN_MOVIE_KEY     = "movie_id";
        public static final String COLUMN_YOUTUBE_KEY   = "youtube_key";
        public static final String COLUMN_NAME          = "name";

        public static Uri buildTrailersByMovie(String movieId){
            //TO_DO make the url as similar as the original one
            //content://ch.scremona.android.popularmovies.app/reviews/movieId
            return MovieEntry.CONTENT_URI.buildUpon().appendPath(movieId).appendPath(PATH_TRAILER).build();
        }

        public static String getMovieIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
        public static String getIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
        public static Uri buildTrailerUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

}
