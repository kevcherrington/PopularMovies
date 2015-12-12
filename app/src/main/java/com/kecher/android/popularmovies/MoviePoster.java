package com.kecher.android.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.kecher.android.popularmovies.data.MovieContract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * (C) Copyright 2015 Kevin Cherrington (kevcherrington@gmail.com).
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 * Kevin Cherrington
 *
 */
public class MoviePoster implements Parcelable {
    private static String LOG_TAG = MoviePoster.class.getSimpleName();

    public static final String POSTER = "moviePoster";

    public static String DATE_FORMAT = "yyyy-MM-dd";
    private String tmdbMovieId;
    private String movieTitle;
    private Date releaseDate;
    private String posterUrl;
    private List<MovieTrailer> trailers = new ArrayList<>();
    private List<MovieReview> reviews = new ArrayList<>();
    private Double voteAverage;
    private String overview;
    private int popularity; // 1 is low

    static String[] PosterProjection = {
            MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_TMDB_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_MOVIE_TITLE,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_POSTER_URL,
            MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_POPULARITY
    };

    /**
     * Constructor that accepts a Long for the release date.
     * @param tmdbMovieId The movie id assigned by TMDB
     * @param movieTitle The title of the movie
     * @param releaseDate The date the movie was released represented a the milliseconds since epoch.
     * @param posterUrl The URI to the poster image (on web or local storage)
     * @param voteAverage The vote average returned by TMDB
     * @param overview Overview for the movie
     * @param popularity Popularity of the movie
     */
    public MoviePoster (String tmdbMovieId, String movieTitle, Long releaseDate, String posterUrl, Double voteAverage, String overview, int popularity) {
        this.tmdbMovieId = tmdbMovieId;

        if (!movieTitle.equals("null")) {
            this.movieTitle = movieTitle;
        }
        if (releaseDate != null && releaseDate != 0L) {
            this.releaseDate = new Date(releaseDate);
        }
        if (!posterUrl.equals("null")) {
            this.posterUrl = posterUrl;
        }
        this.voteAverage = voteAverage;
        if (!overview.equals("null")) {
            this.overview = overview;
        }
        this.popularity = popularity;
    }

    /**
     * Constructor that accepts a date object for the release date.
     * @param tmdbMovieId The movie id assigned by TMDB
     * @param movieTitle The title of the movie
     * @param releaseDate The date the movie was released
     * @param posterUrl The URI to the poster image (on web or local storage)
     * @param voteAverage The vote average returned by TMDB
     * @param overview Overview for the movie
     * @param popularity Popularity of the movie
     */
    public MoviePoster (String tmdbMovieId, String movieTitle, Date releaseDate, String posterUrl, Double voteAverage, String overview, int popularity) {
        this.tmdbMovieId = tmdbMovieId;

        if (!movieTitle.equals("null")) {
            this.movieTitle = movieTitle;
        }
        this.releaseDate = releaseDate;
        if (!posterUrl.equals("null")) {
            this.posterUrl = posterUrl;
        }
        this.voteAverage = voteAverage;
        if (!overview.equals("null")) {
            this.overview = overview;
        }
        this.popularity = popularity;
    }

    public MoviePoster(Parcel in) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        movieTitle = in.readString();
        try {
            releaseDate = sdf.parse(in.readString());
        } catch (ParseException e) {
            Log.e(LOG_TAG, "unable to parse date: ", e);
        }
        tmdbMovieId = in.readString();
        posterUrl = in.readString();
        in.readTypedList(trailers, MovieTrailer.CREATOR);
        in.readTypedList(reviews, MovieReview.CREATOR);
        voteAverage = in.readDouble();
        overview = in.readString();
        popularity = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        dest.writeString(movieTitle);
        if (releaseDate != null) {
            dest.writeString(sdf.format(releaseDate));
        } else {
            dest.writeString(null);
        }
        dest.writeString(tmdbMovieId);
        dest.writeString(posterUrl);
        dest.writeTypedList(trailers);
        dest.writeTypedList(reviews);
        dest.writeDouble(voteAverage);
        dest.writeString(overview);
        dest.writeInt(popularity);
    }

    public static final Parcelable.Creator<MoviePoster> CREATOR = new Parcelable.Creator<MoviePoster>() {

        @Override
        public MoviePoster createFromParcel(Parcel source) {
            return new MoviePoster(source);
        }

        @Override
        public MoviePoster[] newArray(int size) {
            return new MoviePoster[size];
        }
    };

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

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        if (!posterUrl.equals("null")) {
            this.posterUrl = posterUrl;
        }
    }

    public List<MovieTrailer> getTrailers() {
        return trailers;
    }

    public void setTrailers(List<MovieTrailer> trailers) {
        this.trailers = trailers;
    }

    public Double getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(Double voteAverage) {
        this.voteAverage = voteAverage;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        if (!overview.equals("null")) {
            this.overview = overview;
        }
    }

    public int getPopularity() {
        return popularity;
    }

    public void setPopularity(int popularity) {
        this.popularity = popularity;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public List<MovieReview> getReviews() {
        return reviews;
    }

    public void setReviews(List<MovieReview> reviews) {
        this.reviews = reviews;
    }

    public String getTmdbMovieId() {
        return tmdbMovieId;
    }

    public void setTmdbMovieId(String tmdbMovieId) {
        this.tmdbMovieId = tmdbMovieId;
    }
}
