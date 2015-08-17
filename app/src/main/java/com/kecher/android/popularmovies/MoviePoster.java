package com.kecher.android.popularmovies;

import android.graphics.Bitmap;

import java.util.Date;

/**
 * Created by kevin on 8/8/15.
 */
public class MoviePoster {
    public static String DATE_FORMAT = "yyyy-MM-dd";
    String movieTitle;
    Date releaseDate;
    int moviePosterDrawableId; // drawable reference id
    String posterPath;
    Bitmap moviePosterBitmap;
    Double voteAverage;
    String plotSynopsis;
    int popularity; // 1 is low

    public MoviePoster(String movieTitle, Date releaseDate, int moviePosterDrawableId, Double voteAverage, String plotSynopsis, int popularity) {
        this.movieTitle = movieTitle;
        this.releaseDate = releaseDate;
        this.moviePosterDrawableId = moviePosterDrawableId;
        this.voteAverage = voteAverage;
        this.plotSynopsis = plotSynopsis;
        this.popularity = popularity;
    }

    public MoviePoster(String movieTitle, Date releaseDate, Bitmap moviePosterBitmap, Double voteAverage, String plotSynopsis, int popularity) {
        this.movieTitle = movieTitle;
        this.releaseDate = releaseDate;
        this.moviePosterBitmap = moviePosterBitmap;
        this.voteAverage = voteAverage;
        this.plotSynopsis = plotSynopsis;
        this.popularity = popularity;
    }

    public MoviePoster(String movieTitle, Date releaseDate, String posterPath, Double voteAverage, String plotSynopsis, int popularity) {
        this.movieTitle = movieTitle;
        this.releaseDate = releaseDate;
        this.posterPath = posterPath;
        this.voteAverage = voteAverage;
        this.plotSynopsis = plotSynopsis;
        this.popularity = popularity;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
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

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public Bitmap getMoviePosterBitmap() {
        return moviePosterBitmap;
    }

    public void setMoviePosterBitmap(Bitmap moviePosterBitmap) {
        this.moviePosterBitmap = moviePosterBitmap;
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
        this.plotSynopsis = plotSynopsis;
    }

    public int getPopularity() {
        return popularity;
    }

    public void setPopularity(int popularity) {
        this.popularity = popularity;
    }
}
