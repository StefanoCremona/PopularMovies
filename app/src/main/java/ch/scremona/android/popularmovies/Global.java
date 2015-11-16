package ch.scremona.android.popularmovies;

/**
 * Created by stefanocremona on 14/11/15.
 */
import android.app.Application;

import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

public class Global extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this,Integer.MAX_VALUE));
        Picasso built = builder.build();
 //       built.setIndicatorsEnabled(true);
 //       built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);

    }
}