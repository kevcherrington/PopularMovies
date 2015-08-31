package com.kecher.android.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    public static String DATE_FORMAT = "yyyy-MM-dd";
    String movieTitle;
    Date releaseDate;
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

    public MoviePoster(Parcel in) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        movieTitle = in.readString();
        try {
            releaseDate = sdf.parse(in.readString());
        } catch (ParseException e) {
            Log.e(LOG_TAG, "unable to parse date: ", e);
        }
        posterUrl = in.readString();
        voteAverage = in.readDouble();
        plotSynopsis = in.readString();
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
        dest.writeString(posterUrl);
        dest.writeDouble(voteAverage);
        dest.writeString(plotSynopsis);
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

    @Override
    public int describeContents() {
        return 0;
    }

}
