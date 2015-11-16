package ch.scremona.android.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * Created by stefanocremona on 12/10/15.
 */
public class MovieItem implements Parcelable {
    //private static final long serialVersionUID = -7060210544600464481L;
    private final String LOG_TAG = MovieItem.class.getSimpleName();

    /*
    * The item sould contains:
    *   &#9675; original title
        &#9675; movie poster image thumbnail
        &#9675; A plot synopsis (called overview in the api)
        &#9675; user rating (called vote_average in the api)
        &#9675; release date
    * */
    private long   id;
    private long   movieId;
    private String sortBy;
    private String originalTitle;
    private String poster;
    private String overview;
    private String voteAverage;
    private String releaseDate;
    /*private JSONArray reviews;
    private JSONArray trailers;
*/
    public long getMovieId() {
        return movieId;
    }

    public void setMovieId(long movieId) {
        this.movieId = movieId;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

/*    public JSONArray getReviews() {
        return reviews;
    }

    public void setReviews(JSONArray reviews) {
        this.reviews = reviews;
    }

    public JSONArray getTrailers() {
        return trailers;
    }

    public void setTrailers(JSONArray trailers) {
        this.trailers = trailers;
    }*/

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(String voteAverage) {
        this.voteAverage = voteAverage;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }
    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public String getPoster() {
        return poster;
    }
    public void setPoster(String poster) {
        this.poster = poster;
    }

    public MovieItem(long id, long movieId, String sortBy, String originalTitle, String poster, String overview, String voteAverage, String releaseDate) {
        setId(id);
        setMovieId(movieId);
        setSortBy(sortBy);
        setOriginalTitle(originalTitle);
        setPoster(poster);
        setOverview(overview);
        setVoteAverage(voteAverage);
        setReleaseDate(releaseDate);
    }

    @Override
    public String toString(){
       /* return  "{"+getId()+"-"+
                getOriginalTitle()+"-"+
                getPoster()+"-"+
                getOverview()+"-"+
                getVoteAverage().substring(0, 30)+"-"+
                getReleaseDate()+"}";*/
        try {
            String retVal = "{" +
                    getId() + "-" +
                    getMovieId() + "-" +
                    getOriginalTitle() + "-" +
                    getPoster() + "-" +
                    ((getOverview().length() < 30) ? getOverview() : getOverview().substring(0, 30)) + "...-" +
                    getVoteAverage() + "-" +
                    getReleaseDate() +
                    "}";
            return retVal;
        } catch (Exception e){
            Log.e(LOG_TAG, "Error parsing data: ", e);
            return e.getMessage();
        }

    }

    protected MovieItem(Parcel in) {
        id          = in.readLong();
        movieId     = in.readLong();
        sortBy      = in.readString();
        originalTitle = in.readString();
        poster      = in.readString();
        overview    = in.readString();
        voteAverage = in.readString();
        releaseDate = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong  (id);
        dest.writeLong  (movieId);
        dest.writeString(sortBy);
        dest.writeString(originalTitle);
        dest.writeString(poster);
        dest.writeString(overview);
        dest.writeString(voteAverage);
        dest.writeString(releaseDate);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<MovieItem> CREATOR = new Parcelable.Creator<MovieItem>() {
        @Override
        public MovieItem createFromParcel(Parcel in) {
            return new MovieItem(in);
        }

        @Override
        public MovieItem[] newArray(int size) {
            return new MovieItem[size];
        }
    };
}