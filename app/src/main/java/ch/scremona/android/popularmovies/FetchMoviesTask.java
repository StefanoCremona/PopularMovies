package ch.scremona.android.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

import ch.scremona.android.popularmovies.data.MoviesContract;

/**
 * Created by stefanocremona on 19/10/15.
 */
public class FetchMoviesTask extends AsyncTask<String, Void, Void> {
    private final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    private final Context mContext;

    public FetchMoviesTask(Context context) {
        mContext = context;
    }

    /*@Override
    protected void onPostExecute(List<MovieItem> results) {
        super.onPostExecute(results);
        Log.d(LOG_TAG, "onPostExecute init");
        //Manage the String[] with the Film titles
        if(results != null){
            Log.d(LOG_TAG, "onPostExecute - Extract Elements n.: " + results.size());
            //fillTheGrid(results);
            insertMovies(results);
        }
        Log.d(LOG_TAG, "onPostExecute end");
        //super.onPostExecute(results);
        //super.onPostExecute(mItems);
    }*/



    @Override
    protected Void doInBackground(String... params) {
        Log.d(LOG_TAG, "doInBackground init");
        //Extract he json string depending on the sort order in params[0]
        String theMovieDb_jsonStr = getMoviesFromJson(params[0]);

        if (theMovieDb_jsonStr==null) {
            Log.d(LOG_TAG, "doInBackground the MovieDb internet service is not available.");
            return null;
        }
        //Extract a String[] with the Film titles.
        try {
            JSONObject jsonStr          = new JSONObject(theMovieDb_jsonStr);
            JSONArray jsonMovieArray    = jsonStr.getJSONArray("results");

            JSONObject obj              = (JSONObject) jsonMovieArray.get(0);
            Log.d(LOG_TAG, "The first video: "+obj.getString("original_title"));

            //List<MovieItem> myItems = new ArrayList<>();
            Vector<ContentValues> cVVector = new Vector<ContentValues>(jsonMovieArray.length());
            //String[] resultStrs = new String[jsonMovieArray.length()];

            for(int i=0; i<jsonMovieArray.length(); i++){
                ContentValues movieValues = new ContentValues();
                JSONObject jsonMovie = (JSONObject) jsonMovieArray.get(i);

                movieValues.put(MoviesContract.MovieEntry.COLUMN_ORIGINAL_TITLE,  jsonMovie.getString("original_title"));
                movieValues.put(MoviesContract.MovieEntry.COLUMN_OVERVIEW,        jsonMovie.getString("overview"));
                movieValues.put(MoviesContract.MovieEntry.COLUMN_POSTER_PATH,     jsonMovie.getString("poster_path"));
                movieValues.put(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE,    jsonMovie.getString("release_date"));
                movieValues.put(MoviesContract.MovieEntry.COLUMN_SORTBY_SETTING,  params[0]);
                movieValues.put(MoviesContract.MovieEntry.COLUMN_THEMOVIEDB_ID,   jsonMovie.getLong("id"));
                movieValues.put(MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE,    jsonMovie.getString("vote_average"));

                cVVector.add(movieValues);
            }

            int deleted = mContext.getContentResolver().
                    delete(
                            MoviesContract.MovieEntry.CONTENT_URI,
                            MoviesContract.MovieEntry.COLUMN_SORTBY_SETTING + " = ? OR " +
                            MoviesContract.MovieEntry.COLUMN_SORTBY_SETTING + " = ?",
                            new String[] {params[0], mContext.getString(R.string.theMovieDb_sortByToBeDeleteParameterValue)});

            Log.d(LOG_TAG, "doInBackground end: "+ deleted + " Movies deleted by SortParam: "+params[0]);

            int inserted = 0;
            // adds the movies to database
            if ( cVVector.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                inserted = mContext.getContentResolver().bulkInsert(MoviesContract.MovieEntry.CONTENT_URI, cvArray);
            }

            Log.d(LOG_TAG, "doInBackground end: "+ inserted + " Movies inserted by SortParam: "+params[0]);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String getMoviesFromJson(String sortMethod){
        Log.d(LOG_TAG, "getMoviesFromJson init");
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
            final String BASE_URL       = mContext.getString(R.string.theMovieDb_baseUrl);
            final String SORT_PARAM_KEY = mContext.getString(R.string.theMovieDb_sortByParameterKey);
            final String KEY_PARAM_KEY  = mContext.getString(R.string.theMovieDb_apiKeyParameterKey);

            //final String theMovieDb_keyStr = mContext.getString( R.string.theMovieDb_apiKeyParameterValue);
            final String theMovieDb_keyStr = BuildConfig.THEMOVIEDB_API_KEY;

            Uri buildURI = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(SORT_PARAM_KEY, sortMethod)
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
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            return null;
        } finally{
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        Log.d(LOG_TAG, "getMoviesFromJson end: "+theMovieDb_jsonStr.substring(0, 100)+"...");
        return theMovieDb_jsonStr;
    }

    private void insertMovies(ContentValues[] mMovies){
        //Insert some Reviews
        //ContentValues[] mTrailers = TestUtilities.createTrailerValues(movieId);
        int nRows = mContext.getContentResolver().
                bulkInsert(MoviesContract.TrailerEntry.CONTENT_URI, mMovies);
        Log.d(LOG_TAG, "insertMovies inserted " + nRows + " Trailers");
        //assertTrue(nRows != -1);
    }
}
