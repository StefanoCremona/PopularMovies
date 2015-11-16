package ch.scremona.android.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import ch.scremona.android.popularmovies.data.MoviesContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment
        extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    private static final String SELECTED_KEY = "SK";

    private MovieAdapter    myMovieAdapter;
    private GridView        mGridView;
    private int             mPosition;


    //For implementing Loadermanager
    private static final int MOVIES_LOADER = 0;

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    public static final int COL_MOVIE_ID           = 0;
    public static final int COL_MOVIEDB_ID         = 1;
    public static final int COL_MOVIE_SORTBY       = 2;
    public static final int COL_MOVIE_TITLE        = 3;
    public static final int COL_MOVIE_POSTER       = 4;
    public static final int COL_MOVIE_OVERVIEW     = 5;
    public static final int COL_MOVIE_VOTEAVERAGE  = 6;
    public static final int COL_MOVIE_RELEASEDATE  = 7;

    /**
      * A callback interface that all activities containing this fragment must
      * implement. This mechanism allows activities to be notified of item
      * selections.
      */
    public interface Callback {
    /**
      * DetailFragmentCallback for when an item has been selected.
      */
            public void onItemSelected(MovieItem selectedMovieItem);
    }

    public MainActivityFragment() {
    }

    @Override
    public void onCreate (Bundle SavedInstanceState){
        // I created this method. It happens before onCreateView.
        super.onCreate(SavedInstanceState);
        // Comunicate that the Fragment has a menu
        setHasOptionsMenu(true);
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(LOG_TAG, "onSaveInstanceState");
        if(mPosition!= GridView.INVALID_POSITION){
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStart(){
        super.onStart();
        /*Log.d(LOG_TAG, "onStart init");
        Moved on MainActivity.onResume();
        callTheMovieDb();
        Log.d(LOG_TAG, "onStart end");*/
    }

    //Update the Adapter
    //public void fillTheGrid(List<MovieItem> myItems) {
        //Log.d(LOG_TAG, "setAdapter init size " + myItems.size());
        //To reimplement after Cursor loader implementation
        /*mPostersAdapter.clear();
        mPostersAdapter.addAll(myItems);
        mPostersAdapter.notifyDataSetChanged();*/
        //Log.d(LOG_TAG, "setAdapter end");
    //}

    public void onSortOrderChanged(){
        callTheMovieDb();
        getLoaderManager().restartLoader(MOVIES_LOADER, null, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        //inflater.inflate(R.menu.themoviedbfragment, menu);
    }

    private void callTheMovieDb(){
        Log.d(LOG_TAG, "callTheMoviedb init");
        boolean isNetworkAvailable = Utility.isNetworkAvailable(getActivity());

        if (!isNetworkAvailable) {
            Context context = getContext();
            CharSequence text = getString(R.string.noInternetConnection);
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.setGravity(Gravity.TOP, 0, 16);
            toast.show();
            return;
        }

        String sortMethodByPref = Utility.getPreferredSortOrder(getActivity());

        //This task will only fetch the data into the db
        FetchMoviesTask fmTask = new FetchMoviesTask(getContext());

        //Calls the task that perform the http connection to the MovieDb service
        if(sortMethodByPref.equals(getString(R.string.pref_sortmethods_mostpopular))) {
            Log.d(LOG_TAG, "callTheMoviedb Sort by popularity");
            fmTask.execute(getString(R.string.theMovieDb_sortByPopularityParameterValue));
        }else if (sortMethodByPref.equals(getString(R.string.pref_sortmethods_highestrated))) {
            Log.d(LOG_TAG, "callTheMoviedb Sort by rating");
            fmTask.execute(getString(R.string.theMovieDb_sortByRatingParameterValue));
        } else{
            Log.d(LOG_TAG, "callTheMoviedb Sort by favorite ... no operation performed");
        }
        Log.d(LOG_TAG, "callTheMoviedb end");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
        int id = menuItem.getItemId();
        if (id==R.id.action_refresh) {
            callTheMovieDb();
            return true;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreateView init");
        myMovieAdapter = new MovieAdapter(getActivity(), null, 0);

        View rootView =  inflater.inflate(R.layout.grid_layout, container, false);
        mGridView = (GridView) rootView.findViewById(R.id.gridview);
        mGridView.setAdapter(myMovieAdapter);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //SquareImageView myView = (SquareImageView) view;
                Log.d(LOG_TAG, "MovieItem clicked at position: " + position);
                mPosition = position;
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) mGridView.getItemAtPosition(position);
                if (cursor != null) {
                    MovieItem myMovieItem = movieCursorToItem(cursor);

                    FetchTrailersAndReviewsTask myFetchTrailersAndReviewsTask = new FetchTrailersAndReviewsTask(getContext());
                    myFetchTrailersAndReviewsTask.execute(myMovieItem);

                    ((Callback) getActivity()).onItemSelected(myMovieItem);
                    /*
                    Intent myIntent = new Intent(getActivity(), DetailActivity.class);
                    myIntent.putExtra(Intent.EXTRA_TEXT, myMovieItem);
                    startActivity(myIntent);
                    */
                }
            }
        });

        if(savedInstanceState!=null && savedInstanceState.containsKey(SELECTED_KEY)){
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }
        Log.d(LOG_TAG, "onCreateView end");
        return rootView;
    }

    //Here I manage the Loader
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onActivityCreated init");
        getLoaderManager().initLoader(MOVIES_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(LOG_TAG, "onCreateLoader init");
        String sortMethodByPref = Utility.getPreferredSortOrder(getActivity());
        String sortMethodOnDb;

        //TO_DO da rivedere la chiave di sort.
        if(sortMethodByPref.equals(getString(R.string.pref_sortmethods_mostpopular))) {
            Log.d(LOG_TAG, "onCreateLoader Sort by popularity");
            sortMethodOnDb=getString(R.string.theMovieDb_sortByPopularityParameterValue);
        }else if (sortMethodByPref.equals(getString(R.string.pref_sortmethods_highestrated))) {
            Log.d(LOG_TAG, "onCreateLoader Sort by rating");
            sortMethodOnDb = getString(R.string.theMovieDb_sortByRatingParameterValue);
        }else {
            //Favorites
            //}else if (sortMethodByPref.equals(getString(R.string.pref_sortmethods_favorite))) {
            Log.d(LOG_TAG, "onCreateLoader Sort by favorite");
            sortMethodOnDb=getString(R.string.theMovieDb_sortByFavoriteParameterValue);
        }

        Uri moviesUri = MoviesContract.MovieEntry.CONTENT_URI;
        String selection = MoviesContract.MovieEntry.COLUMN_SORTBY_SETTING+ " = ? ";

        Loader<Cursor> myLoader = new CursorLoader(getActivity(),
                moviesUri,
                null,
                selection,
                new String[] {sortMethodOnDb},
                null);

        return myLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.d(LOG_TAG, "onLoadFinished init cursorGetCount: " + cursor.getCount());
        myMovieAdapter.swapCursor(cursor);
        mGridView.setSelection(mPosition);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        myMovieAdapter.swapCursor(null);
    }

    public static MovieItem movieCursorToItem(Cursor movieCursor){
        return new MovieItem(
                movieCursor.getLong(COL_MOVIE_ID),
                movieCursor.getLong(COL_MOVIEDB_ID),
                movieCursor.getString(COL_MOVIE_SORTBY),
                movieCursor.getString(COL_MOVIE_TITLE),
                movieCursor.getString(COL_MOVIE_POSTER),
                movieCursor.getString(COL_MOVIE_OVERVIEW),
                movieCursor.getString(COL_MOVIE_VOTEAVERAGE),
                movieCursor.getString(COL_MOVIE_RELEASEDATE)
        );
    }

    //Extracts trailers and reviews from theMovieDb by a given movie ID and insert into the db
    public class FetchTrailersAndReviewsTask extends AsyncTask<MovieItem, Void, Void> {
        private final String LOG_TAG = FetchTrailersAndReviewsTask.class.getSimpleName();
        private final Context mContext;

        public FetchTrailersAndReviewsTask(Context context) {
            mContext = context;
        }

        @Override
        protected void onPostExecute(Void v) {

        }

        @Override
        protected Void doInBackground(MovieItem... myMovieItem) {
            Log.d(LOG_TAG, "doInBackground init");
            MovieItem myMovie = ((MovieItem) myMovieItem[0]);

            //If I'm coming from the Favorites list, I don't fetch the reviews and Trailers. They are for shure on the db.
            if(Utility.getPreferredSortOrder(getContext()).equals(getContext().getString(R.string.pref_sortmethods_favorite))) {
                Log.d(LOG_TAG, "doInBackground the movie is a favorite, nothing to do for reviews and trailers");
                return null;
            }

            long idAsFavorite = Utility.checkIsFavorite(myMovie, getContext());

            //I'm not coming from the favorites list but the movie is in the favorites list,so I transform it in a fovorite MovieItem
            //In this way the detailActivity will be able to recognize it.
            if(idAsFavorite!=myMovie.getId()){
                Log.d(LOG_TAG, "doInBackground found the movie in the favorite list with id "+idAsFavorite);

                return null;
            }

            Log.d(LOG_TAG, "doInBackground try to retrive the trailers from the db");

            Cursor trailersCursor = mContext.getContentResolver().
                    query(MoviesContract.TrailerEntry.CONTENT_URI,
                            null,
                            MoviesContract.TrailerEntry.COLUMN_MOVIE_KEY + " = ?",
                            new String[]{ ""+myMovie.getId()},
                            null);

            Log.d(LOG_TAG, "doInBackground found "+trailersCursor.getCount()+" trailers");
            if(trailersCursor.getCount()>0) {
                //Means that I've already inserted trailers for this item
                Log.d(LOG_TAG, "doInBackground found trailers, non further operation needed");
                return null;
            }

            //Extract the json string depending on the sort order in params[0]
            String theMovieDbTrailers_jsonStr    = getTrailersFromJson(myMovie.getMovieId());
            if(theMovieDbTrailers_jsonStr==null){
                Log.d(LOG_TAG, "The MovieDb service is not available.");
                return null;
            }
            try {
                JSONObject theMovieDbTrailers_jsonObj   = new JSONObject(theMovieDbTrailers_jsonStr);
                JSONArray theMovieDbTrailers_jsonArray  = theMovieDbTrailers_jsonObj.getJSONArray("results");

                ContentValues[] mTrailers = new ContentValues[theMovieDbTrailers_jsonArray.length()];
                for (int i=0;i<theMovieDbTrailers_jsonArray.length();i++){
                    ContentValues mTrailersValues = new ContentValues();
                    mTrailersValues.put(MoviesContract.TrailerEntry.COLUMN_MOVIE_KEY, myMovie.getId());
                    mTrailersValues.put(MoviesContract.TrailerEntry.COLUMN_NAME,        ((JSONObject) theMovieDbTrailers_jsonArray.get(i)).getString("name"));
                    mTrailersValues.put(MoviesContract.TrailerEntry.COLUMN_YOUTUBE_KEY, ((JSONObject) theMovieDbTrailers_jsonArray.get(i)).getString("key"));
                    mTrailers[i]=mTrailersValues;
                }
                int insertedTrailers = mContext.getContentResolver().
                        bulkInsert(MoviesContract.TrailerEntry.CONTENT_URI, mTrailers);
                Log.d(LOG_TAG, "doInBackground insertes " + insertedTrailers+ " trailers for movie "+myMovie.getId());
            } catch (JSONException e){
                Log.d(LOG_TAG, "doInBackground error "+e.getMessage());
                e.printStackTrace();
            }

            //Extract the json string depending on the sort order in params[0]
            String theMovieDbReviews_jsonStr    = getReviewsFromJson(myMovie.getMovieId());
            if(theMovieDbReviews_jsonStr==null){
                Log.d(LOG_TAG, "The MovieDb service is not available.");
                return null;
            }
            try {
                JSONObject theMovieDbReviews_jsonObj   = new JSONObject(theMovieDbReviews_jsonStr);
                JSONArray theMovieDbReviews_jsonArray  = theMovieDbReviews_jsonObj.getJSONArray("results");

                ContentValues[] mReviews = new ContentValues[theMovieDbReviews_jsonArray.length()];
                for (int i=0;i<theMovieDbReviews_jsonArray.length();i++){
                    ContentValues mReviewsValues = new ContentValues();
                    mReviewsValues.put(MoviesContract.ReviewEntry.COLUMN_MOVIE_KEY, myMovie.getId());
                    mReviewsValues.put(MoviesContract.ReviewEntry.COLUMN_CONTENT,   ((JSONObject) theMovieDbReviews_jsonArray.get(i)).getString("content"));
                    mReviewsValues.put(MoviesContract.ReviewEntry.COLUMN_AUTHOR,    ((JSONObject) theMovieDbReviews_jsonArray.get(i)).getString("author"));
                    mReviews[i]=mReviewsValues;
                }
                int insertedReviews = mContext.getContentResolver().
                        bulkInsert(MoviesContract.ReviewEntry.CONTENT_URI, mReviews);
                Log.d(LOG_TAG, "doInBackground insertes " + insertedReviews+ " reviews for movie "+myMovie.getId());
            } catch (JSONException e){
                Log.d(LOG_TAG, "doInBackground error "+e.getMessage());
                e.printStackTrace();
            }


            Log.d(LOG_TAG, "doInBackground end");
            return null;
        }

        public String getTrailersFromJson(long idMovie){
            return getDataFromJson(idMovie, getString(R.string.theMovieDb_trailersKey));
        }
        public String getReviewsFromJson(long idMovie){
            return getDataFromJson(idMovie, getString(R.string.theMovieDb_reviewsKey));
        }


        public String getDataFromJson(long idMovie, String paramKey){
            Log.d(LOG_TAG, "getDataFromJson init");
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.


            String theMovieDb_jsonStr = null;


            try {
                // Construct the URL for the TheMovieDb query
                // Possible parameters are avaiable at TheMovieDb API page, at
                // http://docs.themoviedb.apiary.io/#reference/movies
                final String BASE_URL       = getString(R.string.theMovieDb_reviewsUrl);
                //final String PARAM_KEY      = getString(R.string.theMovieDb_reviewsKey);
                final String KEY_PARAM_KEY  = getString(R.string.theMovieDb_apiKeyParameterKey);

                //final String theMovieDb_keyStr = getString(R.string.theMovieDb_apiKeyParameterValue);
                final String theMovieDb_keyStr = BuildConfig.THEMOVIEDB_API_KEY;

                Uri buildURI = Uri.parse(BASE_URL).buildUpon()
                        .appendPath(""+idMovie)
                        .appendPath(paramKey)
                        .appendQueryParameter(KEY_PARAM_KEY, theMovieDb_keyStr)
                        .build();

                URL url = new URL(buildURI.toString());
                Log.d(LOG_TAG, buildURI.toString());

                //URL url = new URL("http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=f3a6a11dbe64df59cf688908043e6863");

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                theMovieDb_jsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "getDataFromJson Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "getDataFromJson Error closing stream", e);
                    }
                }
            }
            if (theMovieDb_jsonStr.length()>100)
                Log.d(LOG_TAG, "getDataFromJson end: "+theMovieDb_jsonStr.substring(0, 100)+"...");
            else
                Log.d(LOG_TAG, "getDataFromJson end: "+theMovieDb_jsonStr);
            return theMovieDb_jsonStr;
        }
    }
}
