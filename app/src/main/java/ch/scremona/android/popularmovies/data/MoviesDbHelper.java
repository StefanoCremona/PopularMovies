package ch.scremona.android.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import ch.scremona.android.popularmovies.data.MoviesContract.MovieEntry;
import ch.scremona.android.popularmovies.data.MoviesContract.ReviewEntry;
import ch.scremona.android.popularmovies.data.MoviesContract.TrailerEntry;

/**
 * Created by stefanocremona on 17/10/15.
 */
public class MoviesDbHelper extends SQLiteOpenHelper {
    public static final String LOG_TAG = MoviesDbHelper.class.getSimpleName();
    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 7;

    static final String DATABASE_NAME = "moview.db";

    public MoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create a table to hold Moviews.
        final String SQL_CREATE_MOVIES_TABLE =
                "CREATE TABLE " +
                        MovieEntry.TABLE_NAME + " (" +
                        MovieEntry._ID                     + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        MovieEntry.COLUMN_THEMOVIEDB_ID    + " INTEGER NOT NULL, " +
                        MovieEntry.COLUMN_SORTBY_SETTING   + " TEXT NOT NULL, " +
                        MovieEntry.COLUMN_ORIGINAL_TITLE   + " TEXT NOT NULL, " +
                        MovieEntry.COLUMN_POSTER_PATH      + " TEXT NOT NULL, " +
                        MovieEntry.COLUMN_OVERVIEW         + " TEXT NOT NULL, " +
                        MovieEntry.COLUMN_VOTE_AVERAGE     + " REAL NOT NULL, " +
                        MovieEntry.COLUMN_RELEASE_DATE     + " TEXT NOT NULL " +
                " );";

        final String SQL_CREATE_MOVIES_TRIGGER =
                "CREATE TRIGGER Delete_ReviewsTrailers_trigger BEFORE DELETE ON " + MovieEntry.TABLE_NAME +
                " FOR EACH ROW BEGIN "+
                "DELETE FROM "+ReviewEntry.TABLE_NAME+" WHERE "+ReviewEntry.COLUMN_MOVIE_KEY+" = OLD._id;" +
                "DELETE FROM "+TrailerEntry.TABLE_NAME+" WHERE "+TrailerEntry.COLUMN_MOVIE_KEY+" = OLD._id;" +
                "END;";

        final String SQL_CREATE_REVIEWS_TABLE =
                "CREATE TABLE " +
                        ReviewEntry.TABLE_NAME + " (" +
                        ReviewEntry._ID              + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        // the ID of the movie entry associated with this Review data
                        ReviewEntry.COLUMN_MOVIE_KEY   + " INTEGER NOT NULL, " +
                        ReviewEntry.COLUMN_AUTHOR      + " TEXT NOT NULL, " +
                        ReviewEntry.COLUMN_CONTENT     + " TEXT NOT NULL, " +

                        // Set up the location column as a foreign key to location table.
                        " FOREIGN KEY (" + ReviewEntry.COLUMN_MOVIE_KEY + ") REFERENCES " +
                        MovieEntry.TABLE_NAME + " (" + MovieEntry._ID + ") ON DELETE CASCADE" +
                " );";


        final String SQL_CREATE_TRAILERS_TABLE =
                "CREATE TABLE " +
                        TrailerEntry.TABLE_NAME + " (" +
                        TrailerEntry._ID              + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        // the ID of the movie entry associated with this Review data
                        TrailerEntry.COLUMN_MOVIE_KEY       + " INTEGER NOT NULL, " +
                        TrailerEntry.COLUMN_YOUTUBE_KEY     + " TEXT NOT NULL, " +
                        TrailerEntry.COLUMN_NAME            + " TEXT NOT NULL, " +

                        // Set up the location column as a foreign key to location table.
                        " FOREIGN KEY (" + TrailerEntry.COLUMN_MOVIE_KEY + ") REFERENCES " +
                        MovieEntry.TABLE_NAME + " (" + MovieEntry._ID + ") ON DELETE CASCADE" +
                        " );";

        Log.d(LOG_TAG, "onCreate "+SQL_CREATE_MOVIES_TABLE);
        Log.d(LOG_TAG, "onCreate "+SQL_CREATE_REVIEWS_TABLE);
        Log.d(LOG_TAG, "onCreate "+SQL_CREATE_TRAILERS_TABLE);
        Log.d(LOG_TAG, "onCreate " + SQL_CREATE_MOVIES_TRIGGER);

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIES_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_REVIEWS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_TRAILERS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_MOVIES_TRIGGER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ReviewEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TrailerEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
