package ch.scremona.android.popularmovies.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import java.util.HashSet;

/**
 * Created by stefanocremona on 17/10/15.
 */
public class TestDb extends AndroidTestCase{

    public static final String LOG_TAG = TestDb.class.getSimpleName();

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

    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(MoviesContract.MovieEntry.TABLE_NAME);
        //Add all the remaining tables

        //mContext.deleteDatabase(MoviesDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new MoviesDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        // if this fails, it means that your database doesn't contain both the movie entry
        // and weather entry tables
        assertTrue("Error: Your database was created without the movie entry tables",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + MoviesContract.MovieEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> movieColumnHashSet = new HashSet<String>();
        movieColumnHashSet.add(MoviesContract.MovieEntry._ID);
        movieColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_THEMOVIEDB_ID);
        movieColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_SORTBY_SETTING);
        movieColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_ORIGINAL_TITLE);
        movieColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_POSTER_PATH);
        movieColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_OVERVIEW);
        movieColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE);
        movieColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            movieColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required movie
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required movie entry columns",
                movieColumnHashSet.isEmpty());
        db.close();
    }

    public void testMovieTable() {
        long movieRowId = insertMovie();

        // Make sure we have a valid row ID.
        assertFalse("Error: Location Not Inserted Correctly", movieRowId == -1L);

        insertTrailers(movieRowId);
        insertReviews(movieRowId);
        deleteMovies();
        int reviews = countReviews();
        assertTrue("Error: Too many Records returned from Reviews query", reviews == 0 );

    }

    public void deleteMovies(){
        MoviesDbHelper dbHelper = new MoviesDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int deletedMovies = db.delete(
                MoviesContract.MovieEntry.TABLE_NAME,
                "1",
                null);
        //db.close();
        assertTrue("deleteMovies No movies deleted!!!", deletedMovies > 0);
        Log.d(LOG_TAG, "deleteMovies Deleted " + deletedMovies + " Movies.");
        db.close();
    }

    public int countReviews(){
        MoviesDbHelper dbHelper = new MoviesDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor reviews = db.query(MoviesContract.ReviewEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);
        int reviewsCount = reviews.getCount();
        reviews.close();
        db.close();
        Log.d(LOG_TAG, "deleteMovies Reviews selected " + reviewsCount);
        return reviewsCount;
    }

    public long insertMovie() {
        // First step: Get reference to writable database
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        MoviesDbHelper dbHelper = new MoviesDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second Step: Create ContentValues of what you want to insert
        ContentValues testValues = TestUtilities.createJurassicWorldMovieValues();

        // Third Step: Insert ContentValues into database and get a row ID back
        long movieRowId;

        movieRowId = db.insert(MoviesContract.MovieEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue(movieRowId != -1);


        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                MoviesContract.MovieEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // Move the cursor to a valid database row and check to see if we got any records back
        // from the query
        assertTrue( "Error: No Records returned from Movie query", cursor.moveToFirst() );

        // Fifth Step: Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        TestUtilities.validateCurrentRecord("Error: Movie Query Validation Failed",
                cursor, testValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse("Error: More than one record returned from Movie query",
                cursor.moveToNext());

        cursor.moveToFirst();
        for(int i=0; i<cursor.getCount();i++){
            Log.d(LOG_TAG,  "insertMovie Movie: "+
                            cursor.getLong(0) + "-" +
                            cursor.getLong(1) + "-" +
                            cursor.getString(2) + "-" +
                            cursor.getString(3) + "-" +
                            cursor.getString(4) + "-" +
                            cursor.getString(5) + "-" +
                            cursor.getString(6));
            cursor.moveToNext();
        }

        // Sixth Step: Close Cursor and Database
        cursor.close();
        db.close();
        return movieRowId;
    }

    public void insertTrailers(long movieRowId) {
        // First step: Get reference to writable database
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        MoviesDbHelper dbHelper = new MoviesDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second Step: Create ContentValues of what you want to insert
        ContentValues[] testValues = TestUtilities.createTrailerValues(movieRowId);

        for(int i=0; i<testValues.length;i++) {
            // Third Step: Insert ContentValues into database and get a row ID back
            long rowId;
            rowId = db.insert(MoviesContract.TrailerEntry.TABLE_NAME, null, testValues[i]);

            // Verify we got a row back.
            assertTrue(rowId != -1);
        }
        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                MoviesContract.TrailerEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // Move the cursor to a valid database row and check to see if we got any records back
        // from the query
        assertTrue( "Error: No Records returned from Trailer query", cursor.moveToFirst() );

        // Fifth Step: Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        for(int i=0; i<cursor.getCount();i++) {
            TestUtilities.validateCurrentRecord("Error: Trailer Query Validation Failed",
                    cursor, testValues[i]);
            cursor.moveToNext();
        }
        // Sixth Step: Close Cursor and Database
        cursor.close();
        db.close();
    }

    public void insertReviews(long movieRowId){
        // First step: Get reference to writable database
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        MoviesDbHelper dbHelper = new MoviesDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second Step (Review): Create review values
        ContentValues[] reviewValues = TestUtilities.createReviewValues(movieRowId);

        // Third Step (Review): Insert ContentValues into database and get a row ID back
        for (int i=0; i<reviewValues.length;i++) {
            ContentValues currValue = reviewValues[i];
            long weatherRowId = db.insert(
                    MoviesContract.ReviewEntry.TABLE_NAME,
                    null,
                    currValue);
            assertTrue(weatherRowId != -1);
            Log.d(LOG_TAG, "insertReviews inserting "+i+" row");
        }


        // Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor reviewCursor = db.query(
                MoviesContract.ReviewEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        Log.d(LOG_TAG, "insertReviews inserted "+reviewCursor.getCount()+" reviews.");
        // Move the cursor to the first valid database row and check to see if we have any rows
        assertTrue("Error: No Records returned from location query", reviewCursor.moveToFirst());

        assertTrue("Error: Records number ("+reviewCursor.getCount()+") differs from expected (3)", reviewCursor.getCount()==3);

        for(int i=0;i<reviewCursor.getCount();i++){
            // Fifth Step: Validate the location Query
            TestUtilities.validateCurrentRecord("testInsertReadDb weatherEntry failed to validate",
                    reviewCursor, reviewValues[i]);

            // Move the cursor to demonstrate that there is only one record in the database
//            assertFalse("Error: More than one record returned from weather query",
            reviewCursor.moveToNext();
        }


        // Sixth Step: Close cursor and database
        reviewCursor.close();
        dbHelper.close();
    }
}
