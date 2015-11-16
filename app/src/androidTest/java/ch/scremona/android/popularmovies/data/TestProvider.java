package ch.scremona.android.popularmovies.data;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import ch.scremona.android.popularmovies.R;

/**
 * Created by stefanocremona on 18/10/15.
 */
public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(MoviesDbHelper.DATABASE_NAME);
    }

    /*
        This function gets called before each test is executed to delete the database.  This makes
        sure that we always have a clean test.
     */
    @Override
    public void setUp() {
        deleteTheDatabase();
    }

    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // WeatherProvider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                MoviesProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: MoviesProvider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + MoviesContract.CONTENT_AUTHORITY,
                    providerInfo.authority, MoviesContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: MoviesProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }
/*
    public void testMatcher(){
        Uri mUri = MoviesContract.MovieEntry.CONTENT_URI.buildUpon().appendPath("abcd").build();
        Log.d(LOG_TAG, "testMatcher myUri: "+mUri);
        int mCode= MoviesProvider.buildUriMatcher().match(mUri);
        Log.d(LOG_TAG, "testMatcher match: " + mCode);
        assertTrue(mCode == MoviesProvider.CODE_SORTED_MOVIES);

        mUri= Uri.parse("content://ch.scremona.android.popularmovies.app").buildUpon().appendPath("people").appendPath("123456").build();
        //mUri = MoviesContract.MovieEntry.CONTENT_URI;
        Log.d(LOG_TAG, "testMatcher myUri: "+mUri);
        mCode= MoviesProvider.buildUriMatcher().match(mUri);
        Log.d(LOG_TAG, "testMatcher match: " + mCode);
        assertTrue(mCode == 888);

        //mUri = MoviesContract.MovieEntry.CONTENT_URI.buildUpon().appendPath("123456").build();
        mUri= Uri.parse("content://ch.scremona.android.popularmovies.app").buildUpon().appendPath("movies").appendPath("123456").build();
        Log.d(LOG_TAG, "testMatcher myUri: "+mUri);
        mCode= MoviesProvider.buildUriMatcher().match(mUri);
        Log.d(LOG_TAG, "testMatcher match: " + mCode);
        assertTrue(mCode==101);
    }*/

    public void testGetType() {
        String movieId      = "135397";
        String sortMethod   = "popularity.desc";

        // content://myAuthority/movie/sortMethod
        Uri mUri = MoviesContract.MovieEntry.buildMoviesSorted(sortMethod);
        Log.d(LOG_TAG, "testgetType buildMoviesSorted: "+mUri.toString());
        String type = mContext.getContentResolver().getType(mUri);
        assertEquals("Error: the MovieEntry CONTENT_URI should return MovieEntry.CONTENT_TYPE",
                MoviesContract.MovieEntry.CONTENT_TYPE, type);

        // content://myAuthority/movie/123
        mUri = MoviesContract.MovieEntry.buildMovieUri(123L);
        //mUri = MoviesContract.MovieEntry.CONTENT_URI.buildUpon().appendPath("123").build();
        Log.d(LOG_TAG, "testGetType buildMovieUri: "+mUri.toString());
        type = mContext.getContentResolver().getType(mUri);
        assertEquals("Error: the MovieEntry CONTENT_URI should return MovieEntry.CONTENT_ITEM_TYPE",
                MoviesContract.MovieEntry.CONTENT_ITEM_TYPE, type);

        // content://myAuthority/movie/123/reviews
        mUri=MoviesContract.ReviewEntry.buildReviewsByMovie(movieId);
        Log.d(LOG_TAG, "getType from MyMatcher: "+MoviesProvider.buildUriMatcher().match(mUri));
        Log.d(LOG_TAG, "getType buildReviewsByMovie: "+mUri);
        type = mContext.getContentResolver().getType(mUri);
        assertEquals("Error: the ReviewEntry CONTENT_URI should return ReviewEntry.CONTENT_TYPE",
                MoviesContract.ReviewEntry.CONTENT_TYPE, type);

        // content://myAuthority/review/123456
        type = mContext.getContentResolver().getType(
                MoviesContract.ReviewEntry.buildReviewUri(123L));
        assertEquals("Error: the ReviewEntry CONTENT_URI should return ReviewEntry.CONTENT_ITEM_TYPE",
                MoviesContract.ReviewEntry.CONTENT_ITEM_TYPE, type);

        // content://myAuthority/movie/123/trailers
        type = mContext.getContentResolver().getType(
                MoviesContract.TrailerEntry.buildTrailersByMovie(movieId));
        assertEquals("Error: the TrailerEntry CONTENT_URI should return TrailerEntry.CONTENT_TYPE",
                MoviesContract.TrailerEntry.CONTENT_TYPE, type);

        // content://myAuthority/trailer/123456
        type = mContext.getContentResolver().getType(
                MoviesContract.TrailerEntry.buildTrailerUri(123L));
        assertEquals("Error: the TrailerEntry CONTENT_URI should return TrailerEntry.CONTENT_ITEM_TYPE",
                MoviesContract.TrailerEntry.CONTENT_ITEM_TYPE, type);

    }

    public void testMoviesContractUriParsers(){

        String movieId      = "135397";
        String sortMethod   = "popularity.desc";

        assertEquals("Error: the MovieEntry.getSortBySettingFromUri should return "+sortMethod,
                MoviesContract.MovieEntry.getSortBySettingFromUri(MoviesContract.MovieEntry.buildMoviesSorted(sortMethod)),
                sortMethod);

        assertEquals("Error: the ReviewEntry.getMovieIdFromUri should return "+movieId,
                MoviesContract.ReviewEntry.getMovieIdFromUri(MoviesContract.ReviewEntry.buildReviewsByMovie(movieId)),
                movieId);

        assertEquals("Error: the TrailerEntry.getMovieIdFromUri should return "+movieId,
                MoviesContract.TrailerEntry.getMovieIdFromUri(MoviesContract.TrailerEntry.buildTrailersByMovie(movieId)),
                movieId);
    }

    public void testInsert(){
        //Insert a Moview
        ContentValues mMovie = TestUtilities.createJurassicWorldMovieValues();
        Log.d(LOG_TAG, "testInsert "+mMovie.toString());
        Uri MovieUri = mContext.getContentResolver().
                insert(MoviesContract.MovieEntry.CONTENT_URI, mMovie);
        long movieId = ContentUris.parseId(MovieUri);
        Log.d(LOG_TAG, "testInsert inserted Movie Row id "+movieId);
        assertTrue(movieId != -1);

        //Insert some Reviews
        ContentValues[] mReviews = TestUtilities.createReviewValues(movieId);
        int nRows = mContext.getContentResolver().
                bulkInsert(MoviesContract.ReviewEntry.CONTENT_URI, mReviews);
        Log.d(LOG_TAG, "testInsert inserted "+nRows+" Reviews");
        assertTrue(nRows != -1);

        //Insert some Trailers
        ContentValues[] mTrailers = TestUtilities.createTrailerValues(movieId);
        nRows = mContext.getContentResolver().
                bulkInsert(MoviesContract.TrailerEntry.CONTENT_URI, mTrailers);
        Log.d(LOG_TAG, "testInsert inserted "+nRows+" Trailers");
        assertTrue(nRows != -1);
    }

    public void testSelect (){

        Cursor myCur = mContext.getContentResolver().query(MoviesContract.MovieEntry.CONTENT_URI, null, null, null, null);
        //assertTrue("Error: AllMovie Cursor is empty.", myCur.getCount() > 0);

        Log.d(LOG_TAG, "testSelect getCount: " + myCur.getCount());

        for(int i=0; i<myCur.getCount();i++){
            myCur.moveToNext();
            Log.d(LOG_TAG,
                            myCur.getLong(0)+"-"+
                            myCur.getLong(1)+"-"+
                            myCur.getString(2)+"-"+
                            myCur.getString(3)+"-"+
                            myCur.getString(4)+"-"+
                            myCur.getString(5)+"-"+
                            myCur.getString(6));
        }

        String selection = MoviesContract.MovieEntry.TABLE_NAME+"."+ MoviesContract.MovieEntry.COLUMN_SORTBY_SETTING+" = ?";
        myCur = mContext.getContentResolver().query(MoviesContract.MovieEntry.CONTENT_URI, null, selection, new String[] {getContext().getString(R.string.theMovieDb_sortByPopularityParameterValue)}, null);
        Log.d(LOG_TAG, "testSelect Movie getCount: "+myCur.getCount());
        //assertTrue("Error: Movie Cursor is empty.", myCur.getCount() > 0);

        myCur = mContext.getContentResolver().query(MoviesContract.ReviewEntry.CONTENT_URI, null, null, null, null);
        Log.d(LOG_TAG, "testSelect Review getCount: "+myCur.getCount());
        //assertTrue("Error: AllReview Cursor is empty.", myCur.getCount() > 0);

        myCur = mContext.getContentResolver().query(MoviesContract.TrailerEntry.CONTENT_URI, null, null, null, null);
        Log.d(LOG_TAG, "testSelect Trailer getCount: "+myCur.getCount());
        //assertTrue("Error: AllReview Cursor is empty.", myCur.getCount() > 0);
    }
}
