package com.kecher.android.popularmovies;

import java.util.Date;

/**
 * Created by kevin on 8/8/15.
 */
public class MoviePoster {
    String movieTitle;
    Date releaseDate;
    int moviePosterImage; // drawable reference id
    Double voteAverage;
    String plotSynopsis;
    int popularity; // 1 is low
    int rating; // 1 is low

    public MoviePoster(String movieTitle, Date releaseDate, int moviePosterImage, Double voteAverage, String plotSynopsis, int popularity, int rating) {
        this.movieTitle = movieTitle;
        this.releaseDate = releaseDate;
        this.moviePosterImage = moviePosterImage;
        this.voteAverage = voteAverage;
        this.plotSynopsis = plotSynopsis;
        this.popularity = popularity;
        this.rating = rating;
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

    public int getMoviePosterImage() {
        return moviePosterImage;
    }

    public void setMoviePosterImage(int moviePosterImage) {
        this.moviePosterImage = moviePosterImage;
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

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}
