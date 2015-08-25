package com.kecher.android.popularmovies;

import java.util.Date;

/**
 * Created by kevin on 8/8/15.
 */
public class MoviePoster {
    public static String DATE_FORMAT = "yyyy-MM-dd";
    String movieTitle;
    Date releaseDate;
    int moviePosterDrawableId; // drawable reference id
    String posterUrl;
    Double voteAverage;
    String plotSynopsis;
    int popularity; // 1 is low

    public MoviePoster(String movieTitle, Date releaseDate, String posterUrl, Double voteAverage, String plotSynopsis, int popularity) {
        if (!movieTitle.equals("null")) {
            this.movieTitle = movieTitle;
        }
        this.releaseDate = releaseDate;
        if (!posterUrl.equals("null")) {
            this.posterUrl = posterUrl;
        }
        this.voteAverage = voteAverage;
        if (!plotSynopsis.equals("null")) {
            this.plotSynopsis = plotSynopsis;
        }
        this.popularity = popularity;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        if (!movieTitle.equals("null")) {
            this.movieTitle = movieTitle;
        }
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public int getMoviePosterDrawableId() {
        return moviePosterDrawableId;
    }

    public void setMoviePosterDrawableId(int moviePosterDrawableId) {
        this.moviePosterDrawableId = moviePosterDrawableId;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        if (!posterUrl.equals("null")) {
            this.posterUrl = posterUrl;
        }
    }

    public Double getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(Double voteAverage) {
        this.voteAverage = voteAverage;
    }

    public String getPlotSynopsis() {
        return plotSynopsis;
    }

    public void setPlotSynopsis(String plotSynopsis) {
        if (!plotSynopsis.equals("null")) {
            this.plotSynopsis = plotSynopsis;
        }
    }

    public int getPopularity() {
        return popularity;
    }

    public void setPopularity(int popularity) {
        this.popularity = popularity;
    }
}
