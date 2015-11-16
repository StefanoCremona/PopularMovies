package ch.scremona.android.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ch.scremona.android.popularmovies.data.MoviesContract;

/**
 * Created by stefanocremona on 20/10/15.
 */
public class Utility {
    private final static String LOG_TAG = Utility.class.getSimpleName();

    public static String getPreferredSortOrder(Context context) {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            return prefs.getString(
                    context.getString(R.string.pref_sortmethods_key),
                    context.getString(R.string.pref_sortmethods_mostpopular));
        }catch (Exception e){
            Log.e(LOG_TAG, "getPreferredSortOrder", e);
            return context.getString(R.string.pref_sortmethods_mostpopular);
        }
    }

    public static long checkIsFavorite(MovieItem mMovie, Context mContext){
        long oldId = mMovie.getId();
        Cursor favoriteMovieCursor = mContext.getContentResolver().
                query(MoviesContract.MovieEntry.CONTENT_URI,
                        null,
                        MoviesContract.MovieEntry.COLUMN_SORTBY_SETTING + " = ? and " +
                                MoviesContract.MovieEntry.COLUMN_THEMOVIEDB_ID + " = ?",
                        new String[]{mContext.getString(R.string.theMovieDb_sortByFavoriteParameterValue), "" + mMovie.getMovieId()},
                        null);
        if (favoriteMovieCursor.getCount()>0) {
            favoriteMovieCursor.moveToFirst();
            return favoriteMovieCursor.getInt(0);
        } else {
            return oldId;
        }
    }

    public static String[] fromJsonArray2StrArray(JSONArray jsonMovieArray, String field) {
        String[] resultStrs = null;
        try {
            if (jsonMovieArray.length() == 0) {
                Log.d(LOG_TAG, "fromJsonStr2Array: No Objects Found.");
                return null;
            }

            resultStrs = new String[jsonMovieArray.length()];

            for (int i = 0; i < jsonMovieArray.length(); i++) {
                resultStrs[i] = ((JSONObject) jsonMovieArray.get(i)).getString(field);
            }
            Log.d(LOG_TAG, "fromJsonStr2Array: end");


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultStrs;
    }

    public static boolean isNetworkAvailable(Activity myActivity) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) myActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(),
                View.MeasureSpec.AT_MOST);
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    public static Callback getPicassoCallback(final Context context, final ImageView imageView, final String imageUrl) {
        return new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {
                //Try again online if cache failed
                Picasso.with(context)
                        .load(imageUrl)
                        .error(R.drawable.movie)
                        .into(imageView, new Callback() {
                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onError() {
                                Log.v("Picasso", "Could not fetch image");
                            }
                        });
            }
        };
    }
}
