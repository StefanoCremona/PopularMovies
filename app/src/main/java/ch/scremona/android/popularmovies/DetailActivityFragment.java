package ch.scremona.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import ch.scremona.android.popularmovies.data.MoviesContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>{
    public final static String  DETAIL_ITEM = "detailItem";
    private final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    private final String PARCELABLE_SELECTED_MOVIE_KEY = "selectedMovie";
    private SimpleCursorAdapter myReviewsAdapter;
    private SimpleCursorAdapter myTrailersAdapter;
    private static final int REVIEWS_LOADER     = 0;
    private static final int TRAILERS_LOADER    = 1;
    private MovieItem selectedMovie;
    private View rootView;
    private int mYoutubeKeyIndexInCursor = 2;
    private int mYoutubeTitleIndexInCursor = 3;
    private boolean isWelcome = true;
    private ShareActionProvider mShareActionProvider;
    private boolean mIsShareIntentUpdated = false;
    private String mFirstYoutubeVideo;

    public DetailActivityFragment() {
    }

    public boolean isWelcome(){
        return isWelcome;
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(LOG_TAG, "onSaveInstanceState save the selected movie");
        outState.putParcelable(PARCELABLE_SELECTED_MOVIE_KEY, selectedMovie);
        //outState.putParcelable("reviewsCursor", myReviewsAdapter.getCursor());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        Log.d(LOG_TAG, "onCreateOptionsMenu");
        MenuItem menuItem = menu.findItem(R.id.menu_item_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        //Why this if:
        //On my mobile device, this onCreateOptionMenu, is called after the call to the ListView DataSetObserver,
        //Then its mShareOptionProveder is not initialized and fails to create the share intent.
        if(!mIsShareIntentUpdated && mShareActionProvider!=null){
            Intent shareIntent = createShareMovieIntent();
            if(shareIntent!=null) {
                mShareActionProvider.setShareIntent(createShareMovieIntent());
                Log.d(LOG_TAG, "onCreateOptionsMenu shareIntent Updated");
                mIsShareIntentUpdated = true;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreateView init");

        if(savedInstanceState == null || (savedInstanceState!=null && !savedInstanceState.containsKey(PARCELABLE_SELECTED_MOVIE_KEY))) {
            Bundle arguments = getArguments();
            if (arguments != null) {
                Log.d(LOG_TAG, "onCreateView selectedMovie got from arguments Bundle");
                selectedMovie = (MovieItem) arguments.getParcelable(DetailActivityFragment.DETAIL_ITEM);
            } else {
                Intent myIntent = getActivity().getIntent();
                if(myIntent.hasExtra(Intent.EXTRA_TEXT)) {
                    Log.d(LOG_TAG, "onCreateView selectedMovie got from the Intent.");
                    selectedMovie = (MovieItem) myIntent.getParcelableExtra(Intent.EXTRA_TEXT);
                }
            }
        } else {
            Log.d(LOG_TAG, "onCreateView selectedMovie got from savedInstance");
            selectedMovie = savedInstanceState.getParcelable(PARCELABLE_SELECTED_MOVIE_KEY);
        }

        if(selectedMovie !=null) {
            Log.d(LOG_TAG, "onCreateView in input: " + selectedMovie.getOriginalTitle());

            rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            ((TextView) rootView.findViewById(R.id.originalTitle_details)).setText(selectedMovie.getOriginalTitle());
            ImageView myImage = (ImageView) rootView.findViewById(R.id.poster_details);
            String imageUrl = getString(R.string.theMovieDb_posterPath)+getString(R.string.theMovieDb_posterLowDimensionPath) + selectedMovie.getPoster();

            Picasso.with(getContext()).
                    load(imageUrl).
                    networkPolicy(NetworkPolicy.OFFLINE).
                    into(myImage, Utility.getPicassoCallback(getContext(), myImage, imageUrl));

            boolean comingFromFavoritesList = selectedMovie.getSortBy().equals(getString(R.string.pref_sortmethods_favorite));
            if(!comingFromFavoritesList) {
                long idAsFavorite = Utility.checkIsFavorite(selectedMovie, getContext());
                if (idAsFavorite != selectedMovie.getId()) {
                    selectedMovie.setId(idAsFavorite);
                    selectedMovie.setSortBy(getContext().getString(R.string.theMovieDb_sortByFavoriteParameterValue));
                }
            }

            //Fraw the Favorite StarButton
            Log.d(LOG_TAG, "onCreateView selectedMovie.getSortBy: "+ selectedMovie.getSortBy());
            Log.d(LOG_TAG, "onCreateView compare to: "+getString(R.string.theMovieDb_sortByFavoriteParameterValue));
            Picasso.with(getContext()).
                    load((selectedMovie.getSortBy().equals(getString(R.string.theMovieDb_sortByFavoriteParameterValue))) ? R.drawable.favorite : R.drawable.notfavorite).
                    into((ImageButton) rootView.findViewById(R.id.markAsFavoriteButton_details));

            ((TextView) rootView.findViewById(R.id.overview_details)).setText(selectedMovie.getOverview());
            ((TextView) rootView.findViewById(R.id.releaseData_details)).setText(selectedMovie.getReleaseDate().substring(0, 4));
            //Format the rate like this 6.5/10
            Resources res = getResources();
            String decimalRate = String.format(res.getString(R.string.decimalRate), selectedMovie.getVoteAverage());
            ((TextView) rootView.findViewById(R.id.voteAverage_details)).setText(decimalRate);

            //Becouse it was impossible to manage the ListView inside a ScrollView,
            //I decided to populate dinamically a LinearLayout in the onLoadFinish event

            //Define the Reviews cursor adapter and bind it to the relative listview
            /*myReviewsAdapter = new SimpleCursorAdapter(
                    getContext(),
                    R.layout.list_item_reviews,
                    null,
                    new String[]{MoviesContract.ReviewEntry.COLUMN_CONTENT},
                    new int[] {R.id.listview_reviews_text},
                    0);

            final ListView reviewsListView = (ListView) rootView.findViewById(R.id.listview_reviews);
            reviewsListView.setAdapter(myReviewsAdapter);*/
            getLoaderManager().initLoader(REVIEWS_LOADER, null, this);

            //When data changes, I may modify the shareintent argument
            /*myReviewsAdapter.registerDataSetObserver(new DataSetObserver() {
                @Override
                public void onChanged() {
                    //trailersListView.requestLayout();// setLayoutParams(params);
                    Utility.setListViewHeightBasedOnChildren(reviewsListView);
                }
            });*/

            //Define the Trailers cursor adapter and bind it to the relative listview
            /*myTrailersAdapter = new SimpleCursorAdapter(
                    getContext(),
                    R.layout.list_item_trailers,
                    null,
                    new String[]{MoviesContract.TrailerEntry.COLUMN_NAME},
                    new int[] {R.id.listview_trailers_text},
                    0);

            final ListView trailersListView = (ListView) rootView.findViewById(R.id.listview_trailers);

            trailersListView.setAdapter(myTrailersAdapter);*/
            getLoaderManager().initLoader(TRAILERS_LOADER, null, this);

            /*trailersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Cursor cursor = (Cursor) trailersListView.getItemAtPosition(position);
                    if (cursor != null) {
                        String youtube_key = getString(R.string.movieProvidersUrl_youTube) + cursor.getString(mYoutubeKeyIndexInCursor);
                        Log.d(LOG_TAG, "setOnItemClickListener intent on URI: " + youtube_key);
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(youtube_key)));
                    }
                }
            });*/

            //When data changes, I may modify the shareintent argument
            /*myTrailersAdapter.registerDataSetObserver(new DataSetObserver() {
                @Override
                public void onChanged() {
                    //As the trailers are updated, I change the shareIntent message with the Trailer url
                    if (mShareActionProvider != null) {
                        mShareActionProvider.setShareIntent(createShareMovieIntent());
                        Log.d(LOG_TAG, "registerDataSetObserver shareIntent Updated");
                        mIsShareIntentUpdated = true;
                    } else {
                        Log.d(LOG_TAG, "registerDataSetObserver mShareActionProvider null?");
                        mIsShareIntentUpdated = false;
                    }
                    //trailersListView.requestLayout();// setLayoutParams(params);
                    //Utility.setListViewHeightBasedOnChildren(trailersListView);
                }
            });*/

/*            FetchReviewsTask myReviewsTask = new FetchReviewsTask(getContext());
            myReviewsTask.execute(selectedMovie);*/

            /*FetchTrailersAndReviewsTask myTrailerTask = new FetchTrailersAndReviewsTask(getContext());
            myTrailerTask.execute(selectedMovie);*/
            isWelcome=false;
        }else{
            rootView = inflater.inflate(R.layout.fragment_welcome, container, false);
            Log.d(LOG_TAG, "onCreateView unable to get a Movie");
            isWelcome=true;
        }

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //Next if is Useful when no intent call is performed. es 2 panel test
        /*Intent myIntent = getActivity().getIntent();
        if (myIntent==null || myIntent.getData()==null)
            return null;*/
        if(selectedMovie ==null)
            return null;

        Uri myUri               = null;
        String mySelection[]    = null;
        String myProjection     = null;
        String mySelectionArgs[] = new String[]{""+ selectedMovie.getId()};

        Log.d(LOG_TAG, "onCreateLoader id " + id);
        switch (id) {
            case REVIEWS_LOADER:
                myUri           = MoviesContract.ReviewEntry.CONTENT_URI;
                myProjection    = MoviesContract.ReviewEntry.COLUMN_MOVIE_KEY+"= ?";
                break;
            case TRAILERS_LOADER:
                myUri           = MoviesContract.TrailerEntry.CONTENT_URI;
                myProjection    = MoviesContract.TrailerEntry.COLUMN_MOVIE_KEY+"= ?";
                break;
        }
        return new CursorLoader (getContext(),
                        myUri,
                        mySelection,
                        myProjection,
                        mySelectionArgs,
                        null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()){
            case REVIEWS_LOADER:
                //myReviewsAdapter.swapCursor(data);
                if (data.getCount()>0) {
                    Picasso.with(getContext()).
                            load(R.drawable.yellowreviews).
                            into((ImageButton) rootView.findViewById(R.id.reviewsButton_details));
                    //Create new Views in the Linear Layout
                    LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    LinearLayout list = (LinearLayout) rootView.findViewById(R.id.reviewsLinearLayout);
                    list.removeAllViews();
                    data.moveToLast();
                    for(int i=data.getCount()-1;i>=0;i--) {
                        View myRowView = vi.inflate(R.layout.list_item_reviews, null);
                        // fill in any details dynamically here
                        TextView textView = (TextView) myRowView.findViewById(R.id.listview_reviews_text);
                        textView.setText(data.getString(3));
                        //list.addView(myRowView, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        list.addView(myRowView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 0, 1));
                        data.moveToPrevious();
                    }
                }
                break;
            case TRAILERS_LOADER:
                //myTrailersAdapter.swapCursor(data);
                //Create new Views in the Linear Layout
                if (data.getCount()>0) {
                    Picasso.with(getContext()).
                            load(R.drawable.yellowtrailers).
                            into((ImageButton) rootView.findViewById(R.id.trailersButton_details));
                    LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    LinearLayout list = (LinearLayout) rootView.findViewById(R.id.trailersLinearLayout);
                    list.removeAllViews();
                    data.moveToLast();
                    for(int i=data.getCount()-1;i>=0;i--) {
                        View myRowView = vi.inflate(R.layout.list_item_trailers, null);
                        // fill in any details dynamically here
                        TextView textView = (TextView) myRowView.findViewById(R.id.listview_trailers_text);
                        textView.setText(data.getString(mYoutubeTitleIndexInCursor));
                        //list.addView(myRowView, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        list.addView(myRowView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 0, 1));

                        mFirstYoutubeVideo = data.getString(mYoutubeKeyIndexInCursor);
                        final String uriStr = getString(R.string.movieProvidersUrl_youTube) + mFirstYoutubeVideo;
                        myRowView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.d(LOG_TAG, "setOnClickListener intent on URI: " + uriStr);
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uriStr)));
                            }
                        });
                        data.moveToPrevious();
                    }
                    if (mShareActionProvider != null) {
                        mShareActionProvider.setShareIntent(createShareMovieIntent());
                        Log.d(LOG_TAG, "registerDataSetObserver shareIntent Updated");
                        mIsShareIntentUpdated = true;
                    } else {
                        Log.d(LOG_TAG, "registerDataSetObserver mShareActionProvider null?");
                        mIsShareIntentUpdated = false;
                    }
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()){
            case REVIEWS_LOADER:
                //myReviewsAdapter.swapCursor(null);
                break;
            case TRAILERS_LOADER:
                //myTrailersAdapter.swapCursor(null);
                break;
        }
    }

    //Called by pressing the AddToFavorites Button
    public void setMovieAsFavorite(){
        FetchFavoritesTask mTask = new FetchFavoritesTask(getContext(), (ImageButton) rootView.findViewById(R.id.markAsFavoriteButton_details));
        mTask.execute(selectedMovie);
    }

    public void showOverview(){
        Log.d(LOG_TAG, "showOverview View");
        //View rootView = view.getRootView();
        TextView overviewTextView   = (TextView) rootView.findViewById(R.id.overview_details);
        //ListView reviewsListView    = (ListView) rootView.findViewById(R.id.listview_reviews);
        LinearLayout reviewsListView    = (LinearLayout) rootView.findViewById(R.id.reviewsLinearLayout);
        LinearLayout trailersListView   = (LinearLayout) rootView.findViewById(R.id.trailersLinearLayout);
        overviewTextView.setVisibility(View.VISIBLE);
        reviewsListView.setVisibility(View.GONE);
        trailersListView.setVisibility(View.GONE);
        ((TextView) rootView.findViewById(R.id.bottomDetailsTitle)).setText(getString(R.string.overviewTitle));
    }
    public void showReviews(){
        Log.d(LOG_TAG, "showReviews View");
        TextView overviewTextView   = (TextView) rootView.findViewById(R.id.overview_details);
        LinearLayout reviewsListView    = (LinearLayout) rootView.findViewById(R.id.reviewsLinearLayout);
        LinearLayout trailersListView   = (LinearLayout) rootView.findViewById(R.id.trailersLinearLayout);
        overviewTextView.setVisibility(View.GONE);
        reviewsListView.setVisibility(View.VISIBLE);
        trailersListView.setVisibility(View.GONE);
        ((TextView) rootView.findViewById(R.id.bottomDetailsTitle)).setText(getString(R.string.reviewTitle));
    }
    public void showMovies(){
        Log.d(LOG_TAG, "showMovies View");
        TextView overviewTextView   = (TextView) rootView.findViewById(R.id.overview_details);
        LinearLayout reviewsListView    = (LinearLayout) rootView.findViewById(R.id.reviewsLinearLayout);
        LinearLayout trailersListView   = (LinearLayout) rootView.findViewById(R.id.trailersLinearLayout);
        overviewTextView.setVisibility(View.GONE);
        reviewsListView.setVisibility(View.GONE);
        trailersListView.setVisibility(View.VISIBLE);
        ((TextView) rootView.findViewById(R.id.bottomDetailsTitle)).setText(getString(R.string.trailerTitle));
    }

    public void onSortOrderChanged() {
        //TODO probably here we have to update the fragment with the first movie data...
    }

    public Intent createShareMovieIntent(){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        //It prevents to end up in another application (FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET deprecated)
        //shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

        //ListView trailersListView = (ListView) rootView.findViewById(R.id.listview_trailers);
        //if(trailersListView!=null && trailersListView.getCount()!=0) {
        if(mFirstYoutubeVideo!=null){

            //String youtube_key = ((Cursor) trailersListView.getItemAtPosition(0)).getString(mYoutubeKeyIndexInCursor);

            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.movieProvidersUrl_youTube) + mFirstYoutubeVideo);
        } else{
            Log.d(LOG_TAG, "createShareMovieIntent etrailers listview not jet populated");
            return null;
        }

        //LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
        //        AbsListView.LayoutParams.MATCH_PARENT, );

        //trailersListView.requestLayout();// setLayoutParams(params);
        //setListViewHeightBasedOnChildren(listView)
        return shareIntent;
    }

}
