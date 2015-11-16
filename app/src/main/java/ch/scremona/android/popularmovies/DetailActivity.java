package ch.scremona.android.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class DetailActivity extends AppCompatActivity {

    private final String LOG_TAG = DetailActivity.class.getSimpleName();
    private ShareActionProvider mShareActionProvider;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);


        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putParcelable(DetailActivityFragment.DETAIL_ITEM, getIntent().getParcelableExtra(Intent.EXTRA_TEXT));

            DetailActivityFragment fragment = new DetailActivityFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, new DetailActivityFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.menu_detail, menu);

        /*Log.d(LOG_TAG, "onCreateOptionsMenu");
        MenuItem menuItem = menu.findItem(R.id.menu_item_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if (mShareActionProvider!=null){
            Log.d(LOG_TAG, "ShareActionprovider correctly created.");
            DetailActivityFragment df = (DetailActivityFragment)getSupportFragmentManager().findFragmentById(R.id.movie_detail_container);
            mShareActionProvider.setShareIntent(df.createShareMovieIntent());
        } else{
            Log.d(LOG_TAG, "ShareActionprovider si null?");
        }*/
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

    public void setMovieAsFavorite(View view){
        DetailActivityFragment df = (DetailActivityFragment)getSupportFragmentManager().findFragmentById(R.id.movie_detail_container);
        df.setMovieAsFavorite();
    }
    public void showOverview(View view){
        DetailActivityFragment df = (DetailActivityFragment)getSupportFragmentManager().findFragmentById(R.id.movie_detail_container);
        df.showOverview();
    }
    public void showReviews(View view){
        DetailActivityFragment df = (DetailActivityFragment)getSupportFragmentManager().findFragmentById(R.id.movie_detail_container);
        df.showReviews();
    }
    public void showMovies(View view){
        DetailActivityFragment df = (DetailActivityFragment)getSupportFragmentManager().findFragmentById(R.id.movie_detail_container);
        df.showMovies();
    }

}
