package ch.scremona.android.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageButton;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import ch.scremona.android.popularmovies.data.MoviesContract;

/**
 * Created by stefanocremona on 19/10/15.
 */
public class FetchFavoritesTask extends AsyncTask<MovieItem, Void, Boolean> {
    private final String LOG_TAG = FetchFavoritesTask.class.getSimpleName();
    private final Context mContext;
    private final ImageButton mButtonView;

    public FetchFavoritesTask(Context context, ImageButton buttonView) {
        mButtonView = buttonView;
        mContext = context;
    }

    @Override
    protected void onPostExecute(Boolean retVal) {
        //Change the favorite button icon
        super.onPostExecute(retVal);
        Log.d(LOG_TAG, "onPostExecute init");
        Picasso.with(mContext).
                load((retVal) ? R.drawable.favorite : R.drawable.notfavorite).
                into(mButtonView);
        String text = (retVal) ? mContext.getString(R.string.movieAddedToFavorites) : mContext.getString(R.string.movieRemoveToFavorites);
        Toast myToast = Toast.makeText(mContext, text, Toast.LENGTH_SHORT);
        myToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        myToast.show();
        Log.d(LOG_TAG, "onPostExecute end");
        //super.onPostExecute(results);
        //super.onPostExecute(mItems);
    }



    @Override
    protected Boolean doInBackground(MovieItem... params) {
        Log.d(LOG_TAG, "doInBackground init");
        MovieItem selectedMovie = params[0];
        if(selectedMovie.getSortBy()!=null && selectedMovie.getSortBy().equals(mContext.getString(R.string.theMovieDb_sortByFavoriteParameterValue))) {
            deleteFromFavorite(selectedMovie);
            return false;
        } else {
            insertMovieAsFavorite(selectedMovie);
            return true;
        }
    }

    /*
    //Moved to Utility
    private boolean checkfavoriteExists(MovieItem mMovie){
        Cursor favoriteMovie = mContext.getContentResolver().
                query(MoviesContract.MovieEntry.CONTENT_URI,
                        null,
                        MoviesContract.MovieEntry.COLUMN_SORTBY_SETTING+" = ? and "+
                                MoviesContract.MovieEntry.COLUMN_THEMOVIEDB_ID +" = ?" ,
                        new String[]{mContext.getString( R.string.theMovieDb_sortByFavoriteParameterValue), ""+mMovie.getMovieId()},
                        null);
        if (favoriteMovie.getCount()>0) {
            return true;
        } else {
            return false;
        }
    }
*/
    private void deleteFromFavorite (MovieItem mMovie){
        Log.d(LOG_TAG, "deleteFromFavorite try to delete itme id: "+mMovie.getId());
        ContentValues mMovieValues = new ContentValues();
        mMovieValues.put(
                MoviesContract.MovieEntry.COLUMN_SORTBY_SETTING,
                mContext.getString(R.string.theMovieDb_sortByToBeDeleteParameterValue));

        int deletedMovies = mContext.getContentResolver().
                update(MoviesContract.MovieEntry.CONTENT_URI,
                        mMovieValues,
                        MoviesContract.MovieEntry._ID + " = ?",
                        new String[]{""+mMovie.getId()} );

        /*int deletedMovies = mContext.getContentResolver().
                delete(MoviesContract.MovieEntry.CONTENT_URI,
                        MoviesContract.MovieEntry.COLUMN_SORTBY_SETTING + " = ? and " +
                        MoviesContract.MovieEntry.COLUMN_THEMOVIEDB_ID + " = ?",
                        new String[]{mContext.getString(R.string.theMovieDb_sortByFavoriteParameterValue), "" + mMovie.getMovieId()} );*/
        mMovie.setSortBy(mContext.getString(R.string.theMovieDb_sortByToBeDeleteParameterValue));
        Log.d(LOG_TAG, "deleteFromFavorite Logically deleted "+deletedMovies+" Movies");

        //The related Trailers and Reviews were delete by the trigger
    }

    private void insertMovieAsFavorite(MovieItem mMovie){
        Log.d(LOG_TAG, "insertMovieAsFavorite try to insert a favorite for movieDbId: "+mMovie.getMovieId());
        //Create a new Movie as a favorite
        ContentValues mMovieValues = new ContentValues();
        mMovieValues.put(MoviesContract.MovieEntry.COLUMN_THEMOVIEDB_ID,    mMovie.getMovieId());
        mMovieValues.put(MoviesContract.MovieEntry.COLUMN_SORTBY_SETTING,   mContext.getString(R.string.theMovieDb_sortByFavoriteParameterValue));
        mMovieValues.put(MoviesContract.MovieEntry.COLUMN_ORIGINAL_TITLE,   mMovie.getOriginalTitle());
        mMovieValues.put(MoviesContract.MovieEntry.COLUMN_POSTER_PATH,      mMovie.getPoster());
        mMovieValues.put(MoviesContract.MovieEntry.COLUMN_OVERVIEW,         mMovie.getOverview());
        mMovieValues.put(MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE,     mMovie.getVoteAverage());
        mMovieValues.put(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE,     mMovie.getReleaseDate());


        Uri favoriteMovieUri = mContext.getContentResolver().
                insert(MoviesContract.MovieEntry.CONTENT_URI, mMovieValues);

        String newIdMovie = MoviesContract.MovieEntry.getIdFromUri(favoriteMovieUri);
        Log.d(LOG_TAG, "insertMovieAsFavorite inserted Movie " + newIdMovie+ ". Try to insert the reviews.");

        //Insert the reviews with the new movie id
        Cursor mReviewsCursor = mContext.getContentResolver().query(MoviesContract.ReviewEntry.CONTENT_URI,
                new String[]{MoviesContract.ReviewEntry.COLUMN_AUTHOR, MoviesContract.ReviewEntry.COLUMN_CONTENT},
                MoviesContract.ReviewEntry.COLUMN_MOVIE_KEY+" = ?",
                new String[]{""+mMovie.getId()},
                null);

        ContentValues[] mReviusContentArray = new ContentValues[mReviewsCursor.getCount()];
        Log.d(LOG_TAG, "insertMovieAsFavorite found " + mReviewsCursor.getCount()+ " reviews rlated to the original movie.");
        mReviewsCursor.moveToFirst();

        for(int i=0;i<mReviewsCursor.getCount();i++){
            ContentValues mReview = new ContentValues();
            mReview.put(MoviesContract.ReviewEntry.COLUMN_MOVIE_KEY,    newIdMovie);
            mReview.put(MoviesContract.ReviewEntry.COLUMN_AUTHOR,       mReviewsCursor.getString(0));
            mReview.put(MoviesContract.ReviewEntry.COLUMN_CONTENT,      mReviewsCursor.getString(1));
            mReviusContentArray[i]=mReview;
            mReviewsCursor.moveToNext();
        }
        int newReviews = mContext.getContentResolver().bulkInsert(MoviesContract.ReviewEntry.CONTENT_URI, mReviusContentArray);

        Log.d(LOG_TAG, "insertMovieAsFavorite inserted " + newReviews +" Reviews. Try to insert the Trailers.");

        //Update the trailers with the new movie id
        Cursor mTrailersCursor = mContext.getContentResolver().query(
                MoviesContract.TrailerEntry.CONTENT_URI,
                new String[]{MoviesContract.TrailerEntry.COLUMN_NAME, MoviesContract.TrailerEntry.COLUMN_YOUTUBE_KEY},
                MoviesContract.TrailerEntry.COLUMN_MOVIE_KEY+" = ?",
                new String[]{""+mMovie.getId()},
                null);

        ContentValues[] mTrailersContentArray = new ContentValues[mTrailersCursor.getCount()];
        Log.d(LOG_TAG, "insertMovieAsFavorite found " + mTrailersCursor.getCount()+ " Trailers related to the original movie.");
        mTrailersCursor.moveToFirst();

        for(int i=0;i<mTrailersCursor.getCount();i++){
            ContentValues mTrailer = new ContentValues();
            mTrailer.put(MoviesContract.TrailerEntry.COLUMN_MOVIE_KEY,   newIdMovie);
            mTrailer.put(MoviesContract.TrailerEntry.COLUMN_NAME,        mTrailersCursor.getString(0));
            mTrailer.put(MoviesContract.TrailerEntry.COLUMN_YOUTUBE_KEY, mTrailersCursor.getString(1));
            mTrailersContentArray[i]=mTrailer;
            mTrailersCursor.moveToNext();
        }
        int newTrailers = mContext.getContentResolver().bulkInsert(MoviesContract.TrailerEntry.CONTENT_URI, mTrailersContentArray);

        Log.d(LOG_TAG, "insertMovieAsFavorite inserted " + newTrailers + " Trailers");
        mMovie.setId(new Long(newIdMovie));
        mMovie.setSortBy(mContext.getString(R.string.theMovieDb_sortByFavoriteParameterValue));
    }
}
