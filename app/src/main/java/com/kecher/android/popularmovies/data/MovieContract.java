package com.kecher.android.popularmovies.data;

import android.provider.BaseColumns;

import java.util.Calendar;

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
public class MovieContract {
    private static final String LOG_TAG = MovieContract.class.getSimpleName();

    public static final String PATH_MOVIE = "movie";
    public static final String PATH_REVIEW = "review";
    public static final String PATH_TRAILER = "trailer";

    // To make it easy to query for the exact date, we normalize all dates that go into
    // the database to the start of the day.
    public static long normalizeDate(long startDate) {
        // normalize the start date to the beginning of the (UTC) day
        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(startDate);
        time.set(Calendar.HOUR_OF_DAY, 0);
        time.set(Calendar.MINUTE, 0);
        time.set(Calendar.SECOND, 0);
        time.set(Calendar.MILLISECOND, 0);
        return time.getTimeInMillis();
    }

    public static final class MovieEntry implements BaseColumns {

        public static final String TABLE_NAME = "movie";

        // Movie id that corresponds to TMDB movie id.
        public static final String COLUMN_TMDB_MOVIE_ID = "tmdb_movie_id";
        public static final String COLUMN_FAVORITE = "favorite";
        public static final String COLUMN_MOVIE_TITLE = "title";
        // Release date, stored as long in milliseconds.
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_POSTER_URL = "poster_url";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        public static final String COLUMN_OVERVIEW = "description";
        public static final String COLUMN_POPULARITY = "popularity";

    }

    public static final class ReviewEntry implements BaseColumns {
        public static final String TABLE_NAME = "review";

        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_REVIEW = "review";
        public static final String COLUMN_AUTHOR = "author";

    }

    public static final class TrailerEntry implements BaseColumns {
        public static final String TABLE_NAME = "trailer";

        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_SITE = "site";
        public static final String COLUMN_KEY = "key";

    }
}
