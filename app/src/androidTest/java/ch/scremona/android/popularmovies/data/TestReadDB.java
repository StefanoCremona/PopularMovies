package ch.scremona.android.popularmovies.data;

import android.database.Cursor;
import android.test.AndroidTestCase;
import android.util.Log;

/**
 * Created by stefanocremona on 27/10/15.
 */
public class TestReadDB extends AndroidTestCase {

    public static final String LOG_TAG = TestReadDB.class.getSimpleName();

    public void testReadReviews(){
        Log.d(LOG_TAG, "testReadReviews init");
        Cursor myCur = mContext.getContentResolver().query(MoviesContract.ReviewEntry.CONTENT_URI, null, null, null, null);
        Log.d(LOG_TAG, "testReadReviews xtracted "+myCur.getCount());
        myCur.moveToFirst();
        for (int i=0;i<myCur.getCount();i++){
            Log.d(LOG_TAG, "id=" + myCur.getLong(0) +
                            "movie_id=" + myCur.getString(1) +
                            "author=" + myCur.getString(2) +
                            "content lenght" + myCur.getString(3).length()
            );
            myCur.moveToNext();
        }
    }

    public void testReadTrailers(){
        Log.d(LOG_TAG, "testReadTrailers init");
        Cursor myCur = mContext.getContentResolver().query(MoviesContract.TrailerEntry.CONTENT_URI, null, null, null, null);
        Log.d(LOG_TAG, "testReadTrailers xtracted "+myCur.getCount());
        myCur.moveToFirst();
        for (int i=0;i<myCur.getCount();i++){
            Log.d(LOG_TAG, "id=" + myCur.getLong(0) +
                            "movie_id=" + myCur.getString(1) +
                            "youtube_key=" + myCur.getString(2) +
                            "name" + myCur.getString(3)
            );
            myCur.moveToNext();
        }
    }
}

