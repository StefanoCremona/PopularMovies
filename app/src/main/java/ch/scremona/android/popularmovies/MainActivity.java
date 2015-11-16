package ch.scremona.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements MainActivityFragment.Callback{

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private boolean mTwoPane;
    private static final String DETAIL_CONTAINER_TAG = "DFTAG";
    private final static int    DETAIL_CONTAINER_ID = R.id.movie_detail_container;

    private static String mSortOrder;
    @Override
    protected void onResume(){
        super.onResume();
        String sortOrder = Utility.getPreferredSortOrder(this);
        Log.d(LOG_TAG, "onResume init sortOrder " + ((sortOrder == null) ? "noSortOrder" : sortOrder));

        if (mSortOrder==null ||(sortOrder != null && !sortOrder.equals(mSortOrder))) {
            //MainActivityFragment df = (MainActivityFragment)getSupportFragmentManager().findFragmentById(R.id.container);
            MainActivityFragment mf = (MainActivityFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_main);
            if ( null != mf ) {
                Log.d(LOG_TAG, "onResume init Reload Movies");
                mf.onSortOrderChanged();
            }

            //In 2 panels configuration
            DetailActivityFragment df = (DetailActivityFragment)getSupportFragmentManager().findFragmentByTag(DETAIL_CONTAINER_TAG);
            if ( null != df ) {
                df.onSortOrderChanged();
            }

            mSortOrder = sortOrder;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //mSortOrder = Utility.getPreferredSortOrder(this);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        if (findViewById(DETAIL_CONTAINER_ID) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                    .replace(DETAIL_CONTAINER_ID, new DetailActivityFragment(), DETAIL_CONTAINER_TAG)
                    .commit();
            }
        } else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if(mTwoPane){
            getMenuInflater().inflate(R.menu.menu_detail, menu);

            /*MenuItem menuItem = menu.findItem(R.id.menu_item_share);

            // Fetch and store ShareActionProvider
            ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

            DetailActivityFragment df = (DetailActivityFragment)getSupportFragmentManager().findFragmentById(DETAIL_CONTAINER_ID);

            if (mShareActionProvider!=null && df!=null && !df.isWelcome()){
                Intent myIntent = df.createShareMovieIntent();
                if(myIntent!=null)
                    mShareActionProvider.setShareIntent(df.createShareMovieIntent());
            } else{
                Log.d(LOG_TAG, "ShareActionprovider si null?");
            }
            return true;*/
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SimpleSettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Method for the callback
    @Override
    public void onItemSelected(MovieItem selectedMovieItem) {
        if(!Utility.isNetworkAvailable(this) && Utility.getPreferredSortOrder(this)!=getString(R.string.pref_sortmethods_favorite)){
            Context context = this;
            CharSequence text = getString(R.string.noInternetConnection);
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.setGravity(Gravity.TOP, 0, 16);
            toast.show();
            return;
        }

        if(mTwoPane){
            Bundle args = new Bundle();
            args.putParcelable(DetailActivityFragment.DETAIL_ITEM, selectedMovieItem);

            DetailActivityFragment fragment = new DetailActivityFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(DETAIL_CONTAINER_ID, fragment, DETAIL_CONTAINER_TAG)
                    .commit();
        }else{
            Intent myIntent = new Intent(this, DetailActivity.class);
            myIntent.putExtra(Intent.EXTRA_TEXT, selectedMovieItem);
            startActivity(myIntent);
        }
    }

    public void setMovieAsFavorite(View view){
        DetailActivityFragment df = (DetailActivityFragment)getSupportFragmentManager().findFragmentById(DETAIL_CONTAINER_ID);
        df.setMovieAsFavorite();
    }
    public void showOverview(View view){
        DetailActivityFragment df = (DetailActivityFragment)getSupportFragmentManager().findFragmentById(DETAIL_CONTAINER_ID);
        df.showOverview();
    }
    public void showReviews(View view){
        DetailActivityFragment df = (DetailActivityFragment)getSupportFragmentManager().findFragmentById(DETAIL_CONTAINER_ID);
        df.showReviews();
    }
    public void showMovies(View view){
        DetailActivityFragment df = (DetailActivityFragment)getSupportFragmentManager().findFragmentById(DETAIL_CONTAINER_ID);
        df.showMovies();
    }
}
