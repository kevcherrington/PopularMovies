package com.kecher.android.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

import com.kecher.android.popularmovies.data.MovieContract;

/**
 * (C) Copyright 2015 Kevin Cherrington (kevcherrington@gmail.com).
 * <p/>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * <p/>
 * Contributors:
 * Kevin Cherrington
 */
public class MovieTrailer implements Parcelable {
    private static final String LOG_TAG = MovieTrailer.class.getSimpleName();

    private String trailerUrl;
    private String trailerTitle;

    public static String[] trailerProjection = {
            MovieContract.TrailerEntry._ID,
            MovieContract.TrailerEntry.COLUMN_KEY,
            MovieContract.TrailerEntry.COLUMN_NAME,
            MovieContract.TrailerEntry.COLUMN_SITE
    };

    public MovieTrailer(Parcel in) {
        trailerUrl = in.readString();
        trailerTitle = in.readString();
    }

    public MovieTrailer() {
    }

    public MovieTrailer(String trailerUrl, String trailerTitle) {
        this.trailerUrl = trailerUrl;
        this.trailerTitle = trailerTitle;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(trailerUrl);
        dest.writeString(trailerTitle);
    }

    public static final Parcelable.Creator<MovieTrailer> CREATOR = new Parcelable.Creator<MovieTrailer>() {

        @Override
        public MovieTrailer createFromParcel(Parcel source) {
            return new MovieTrailer(source);
        }

        @Override
        public MovieTrailer[] newArray(int size) {
            return new MovieTrailer[size];
        }
    };

    public String getTrailerUrl() {
        return trailerUrl;
    }

    public void setTrailerUrl(String trailerUrl) {
        this.trailerUrl = trailerUrl;
    }

    public String getTrailerTitle() {
        return trailerTitle;
    }

    public void setTrailerTitle(String trailerTitle) {
        this.trailerTitle = trailerTitle;
    }

    @Override
    public int describeContents() {
        return 0;
    }

}
