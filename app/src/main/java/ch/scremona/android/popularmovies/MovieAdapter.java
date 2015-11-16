package ch.scremona.android.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class MovieAdapter extends CursorAdapter {
    private final String LOG_TAG = MovieAdapter.class.getSimpleName();

    public static class ViewHolder {
        public final SquareImageView posterView;

        public ViewHolder(View view) {
            this.posterView = (SquareImageView)view.findViewById(R.id.picture);
        }
    }

    public MovieAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.grid_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder)view.getTag();
        String imageUrl = context.getString(R.string.theMovieDb_posterPath)+context.getString(R.string.theMovieDb_posterLowDimensionPath) + cursor.getString(MainActivityFragment.COL_MOVIE_POSTER);
        Picasso.with(context).
                load(context.getString(R.string.theMovieDb_posterPath)+context.getString(R.string.theMovieDb_posterLowDimensionPath) + cursor.getString(MainActivityFragment.COL_MOVIE_POSTER)).
                networkPolicy(NetworkPolicy.OFFLINE).
                into(viewHolder.posterView, Utility.getPicassoCallback(context, viewHolder.posterView, imageUrl));
    }
}