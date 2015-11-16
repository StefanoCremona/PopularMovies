package ch.scremona.android.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by stefanocremona on 17/10/15.
 */
public class MoviesProvider extends ContentProvider {

    public static final String LOG_TAG = MoviesProvider.class.getSimpleName();
    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MoviesDbHelper mMoviesHelper;

    static final int CODE_MOVIE          = 100;
    static final int CODE_MOVIE_BYID     = 101;
    static final int CODE_SORTED_MOVIES  = 110;
    static final int CODE_MOVIE_REVIEWS  = 120;
    static final int CODE_MOVIE_TRAILERS = 130;

    static final int CODE_REVIEW         = 200;
    static final int CODE_REVIEW_BYID    = 201;

    static final int CODE_TRAILER        = 300;
    static final int CODE_TRAILER_BYID   = 301;

    private static final SQLiteQueryBuilder sMovieQueryBuilder;

    static{
        sMovieQueryBuilder = new SQLiteQueryBuilder();

        sMovieQueryBuilder.setTables(
                MoviesContract.MovieEntry.TABLE_NAME + ", "+
                MoviesContract.ReviewEntry.TABLE_NAME+ " INNER JOIN "+
                        MoviesContract.MovieEntry.TABLE_NAME + " ON "+
                        MoviesContract.ReviewEntry.TABLE_NAME+"."+MoviesContract.ReviewEntry.COLUMN_MOVIE_KEY+" = "+
                        MoviesContract.MovieEntry.TABLE_NAME+"."+ MoviesContract.MovieEntry._ID+ ", "+
                MoviesContract.TrailerEntry.TABLE_NAME+ " INNER JOIN "+
                        MoviesContract.MovieEntry.TABLE_NAME + " ON "+
                        MoviesContract.TrailerEntry.TABLE_NAME+"."+MoviesContract.TrailerEntry.COLUMN_MOVIE_KEY+" = "+
                        MoviesContract.MovieEntry.TABLE_NAME+"."+ MoviesContract.MovieEntry._ID
        );
    }

    static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MoviesContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        //matcher.addURI(authority, MoviesContract.PATH_MOVIE,            CODE_MOVIE);
        //matcher.addURI(authority, MoviesContract.PATH_MOVIE + "/*",     CODE_MOVIE_SORTED);
        //matcher.addURI(authority, MoviesContract.PATH_REVIEW + "/#",    CODE_REVIEW);
        //matcher.addURI(authority, MoviesContract.PATH_TRAILER + "/#",   CODE_TRAILER);

        //matcher.addURI(authority, MoviesContract.PATH_MOVIE,            CODE_MOVIE);

        // Remember *=String #=Number
        /*
        TO_DO
        Notify to google that changing the order in adding process between # and * make impossibile to check che numbers!!!
        >>it works
        matcher.addURI(authority, "people/", 777);
        matcher.addURI(authority, "people/#", 888);
        matcher.addURI(authority, "people*//*", 999);
        <<it doesn't work
        matcher.addURI(authority, "people/", 777);
        matcher.addURI(authority, "people*//*", 999);
        matcher.addURI(authority, "people/#", 888);

        */

        matcher.addURI(authority, MoviesContract.PATH_MOVIE     + "/",                                 CODE_MOVIE);
        matcher.addURI(authority, MoviesContract.PATH_MOVIE     + "/#",                                CODE_MOVIE_BYID);
        matcher.addURI(authority, MoviesContract.PATH_MOVIE     + "/*",                                CODE_SORTED_MOVIES);

        matcher.addURI(authority, MoviesContract.PATH_MOVIE     + "/#/" + MoviesContract.PATH_REVIEW,   CODE_MOVIE_REVIEWS);
        matcher.addURI(authority, MoviesContract.PATH_MOVIE     + "/#/" + MoviesContract.PATH_TRAILER,  CODE_MOVIE_TRAILERS);

        matcher.addURI(authority, MoviesContract.PATH_REVIEW    + "/",                                  CODE_REVIEW);
        matcher.addURI(authority, MoviesContract.PATH_REVIEW    + "/#",                                 CODE_REVIEW_BYID);

        matcher.addURI(authority, MoviesContract.PATH_TRAILER   + "/",                                  CODE_TRAILER);
        matcher.addURI(authority, MoviesContract.PATH_TRAILER   + "/#",                                 CODE_TRAILER_BYID);

        return matcher;
    }


    @Override
    public boolean onCreate() {
        mMoviesHelper = new MoviesDbHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.d(LOG_TAG, "query uri: "+uri);

        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "Movies/*/*"
            case CODE_MOVIE_BYID: {
                retCursor = getMovieById(uri, projection);
                break;
            }
            case CODE_MOVIE: {
                retCursor = mMoviesHelper.getReadableDatabase().query(
                        MoviesContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case CODE_SORTED_MOVIES: {
                retCursor = getMoviesBySortOrder(uri, projection);
                break;
            }
            // "Reviews/*"
            case CODE_REVIEW_BYID: {
                retCursor = getReviewById(uri, projection);
                break;
            }
            case CODE_REVIEW: {
                retCursor = mMoviesHelper.getReadableDatabase().query(
                        MoviesContract.ReviewEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case CODE_MOVIE_REVIEWS: {
                retCursor = getReviewsByMovieId(uri, projection);
                break;
            }
            // "Trailers"
            case CODE_TRAILER_BYID: {
                retCursor = getTrailerById(uri, projection);
                break;
            }
            case CODE_TRAILER: {
                retCursor = mMoviesHelper.getReadableDatabase().query(
                        MoviesContract.TrailerEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case CODE_MOVIE_TRAILERS: {
                retCursor = getTrailersByMovieId(uri, projection);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        Log.d(LOG_TAG, "query cursor count: "+retCursor.getCount());
        return retCursor;
    }

    private Cursor getMovieById(Uri uri, String[] projection) {
        String id = MoviesContract.MovieEntry.getIdFromUri(uri);

        return sMovieQueryBuilder.query(
                mMoviesHelper.getReadableDatabase(),
                projection,
                MoviesContract.MovieEntry.TABLE_NAME+"."+ MoviesContract.MovieEntry._ID+" = ?",
                new String[]{id},
                null,
                null,
                null
        );
    }
    private Cursor getMoviesBySortOrder(Uri uri, String[] projection) {
        String sortBySetting = MoviesContract.MovieEntry.getSortBySettingFromUri(uri);

        return sMovieQueryBuilder.query(
                mMoviesHelper.getReadableDatabase(),
                projection,
                MoviesContract.MovieEntry.TABLE_NAME+"."+ MoviesContract.MovieEntry.COLUMN_SORTBY_SETTING+" = ?",
                new String[]{sortBySetting},
                null,
                null,
                null
        );
    }

    private Cursor getReviewById(Uri uri, String[] projection) {
        String id = MoviesContract.ReviewEntry.getIdFromUri(uri);

        return sMovieQueryBuilder.query(
                mMoviesHelper.getReadableDatabase(),
                projection,
                MoviesContract.ReviewEntry.TABLE_NAME+"."+ MoviesContract.ReviewEntry._ID+" = ?",
                new String[]{id},
                null,
                null,
                null
        );
    }
    private Cursor getReviewsByMovieId(Uri uri, String[] projection) {
        String movieId = MoviesContract.ReviewEntry.getMovieIdFromUri(uri);

        return sMovieQueryBuilder.query(
                mMoviesHelper.getReadableDatabase(),
                projection,
                MoviesContract.ReviewEntry.TABLE_NAME+"."+ MoviesContract.ReviewEntry.COLUMN_MOVIE_KEY+" = ?",
                new String[]{movieId},
                null,
                null,
                null
        );
    }

    private Cursor getTrailerById(Uri uri, String[] projection) {
        String id = MoviesContract.TrailerEntry.getIdFromUri(uri);

        return sMovieQueryBuilder.query(
                mMoviesHelper.getReadableDatabase(),
                projection,
                MoviesContract.TrailerEntry.TABLE_NAME+"."+ MoviesContract.TrailerEntry._ID+" = ?",
                new String[]{id},
                null,
                null,
                null
        );
    }
    private Cursor getTrailersByMovieId(Uri uri, String[] projection) {
        String movieId = MoviesContract.TrailerEntry.getMovieIdFromUri(uri);

        return sMovieQueryBuilder.query(
                mMoviesHelper.getReadableDatabase(),
                projection,
                MoviesContract.TrailerEntry.TABLE_NAME+"."+ MoviesContract.TrailerEntry.COLUMN_MOVIE_KEY+" = ?",
                new String[]{movieId},
                null,
                null,
                null
        );
    }

    //@Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        Log.d(LOG_TAG, "GetType: "+ match);

        switch (match){
            case CODE_MOVIE:
                return MoviesContract.MovieEntry.CONTENT_TYPE;
            case CODE_MOVIE_BYID:
                return MoviesContract.MovieEntry.CONTENT_ITEM_TYPE;
            case CODE_SORTED_MOVIES:
                return MoviesContract.MovieEntry.CONTENT_TYPE;
            case CODE_REVIEW:
                return MoviesContract.ReviewEntry.CONTENT_TYPE;
            case CODE_REVIEW_BYID:
                return MoviesContract.ReviewEntry.CONTENT_ITEM_TYPE;
            case CODE_MOVIE_REVIEWS:
                return MoviesContract.ReviewEntry.CONTENT_TYPE;
            case CODE_TRAILER:
                return MoviesContract.TrailerEntry.CONTENT_TYPE;
            case CODE_TRAILER_BYID:
                return MoviesContract.TrailerEntry.CONTENT_ITEM_TYPE;
            case CODE_MOVIE_TRAILERS:
                return MoviesContract.TrailerEntry.CONTENT_TYPE;
            default:
                throw  new UnsupportedOperationException("Unknown uri: "+uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mMoviesHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case CODE_MOVIE: {
                long _id = db.insert(MoviesContract.MovieEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MoviesContract.MovieEntry.buildMovieUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case CODE_REVIEW: {
                long _id = db.insert(MoviesContract.ReviewEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MoviesContract.ReviewEntry.buildReviewUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case CODE_TRAILER: {
                long _id = db.insert(MoviesContract.TrailerEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MoviesContract.TrailerEntry.buildTrailerUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mMoviesHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case CODE_MOVIE:
                rowsDeleted = db.delete(
                        MoviesContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case CODE_REVIEW:
                rowsDeleted = db.delete(
                        MoviesContract.ReviewEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case CODE_TRAILER:
                rowsDeleted = db.delete(
                        MoviesContract.TrailerEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mMoviesHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case CODE_MOVIE:
                rowsUpdated = db.update(MoviesContract.MovieEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case CODE_REVIEW:
                rowsUpdated = db.update(MoviesContract.ReviewEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case CODE_TRAILER:
                rowsUpdated = db.update(MoviesContract.TrailerEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        Log.d(LOG_TAG, "bulkInsert URI "+uri);
        final SQLiteDatabase db = mMoviesHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount = 0;
        switch (match) {
            case CODE_MOVIE:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {

                        long _id = db.insert(MoviesContract.MovieEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                Log.d(LOG_TAG, "bulkInsert CODE_MOVIE inserted " + returnCount + " elements");
                return returnCount;
            case CODE_REVIEW:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MoviesContract.ReviewEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                Log.d(LOG_TAG, "bulkInsert CODE_REVIEW inserted " + returnCount + " elements");
                return returnCount;
            case CODE_TRAILER:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MoviesContract.TrailerEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                Log.d(LOG_TAG, "bulkInsert CODE_TRAILER inserted " + returnCount + " elements");
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
