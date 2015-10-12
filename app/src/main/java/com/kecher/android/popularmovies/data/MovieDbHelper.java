package com.kecher.android.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.kecher.android.popularmovies.data.MovieContract.MovieEntry;
import com.kecher.android.popularmovies.data.MovieContract.ReviewEntry;
import com.kecher.android.popularmovies.data.MovieContract.TrailerEntry;
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
public class MovieDbHelper extends SQLiteOpenHelper {
    private static final String LOG_TAG = MovieDbHelper.class.getSimpleName();
    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 2;

    static final String DATABASE_NAME = "movie.db";

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
                MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                // the ID of the location entry associated with this movie data
                MovieEntry.COLUMN_TMDB_MOVIE_ID + " INTEGER NOT NULL, " +
                MovieEntry.COLUMN_FAVORITE + " Integer NOT NULL, " +
                MovieEntry.COLUMN_MOVIE_TITLE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_RELEASE_DATE + " INTEGER, " +
                MovieEntry.COLUMN_POSTER_URL + " TEXT, " +
                MovieEntry.COLUMN_VOTE_AVERAGE + " REAL NOT NULL, " +
                MovieEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_POPULARITY + " REAL NOT NULL, " +
                MovieEntry.COLUMN_IMAGE + " BLOB, " +

                // To assure the application has just one movie entry
                // it's created with a UNIQUE constraint with the REPLACE strategy
                " UNIQUE (" + MovieEntry.COLUMN_TMDB_MOVIE_ID + ") ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_TABLE);

        final String SQL_CREATE_REVIEW_TABLE = "CREATE TABLE " + ReviewEntry.TABLE_NAME + " (" +
                ReviewEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                ReviewEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                ReviewEntry.COLUMN_REVIEW + " TEXT NOT NULL, " +
                ReviewEntry.COLUMN_AUTHOR + " TEXT NOT NULL" +
                " FOREIGN KEY(" + ReviewEntry.COLUMN_MOVIE_ID +
                ") REFERENCES " + MovieEntry.TABLE_NAME +
                "(" + MovieEntry._ID + "));";

        sqLiteDatabase.execSQL(SQL_CREATE_REVIEW_TABLE);

        final String SQL_CREATE_TRAILER_TABLE = "CREATE TABLE " + TrailerEntry.TABLE_NAME + " (" +
                TrailerEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                TrailerEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                TrailerEntry.COLUMN_NAME + " TEXT, " +
                TrailerEntry.COLUMN_SITE + " TEXT, " +
                TrailerEntry.COLUMN_KEY + " TEXT UNIQUE NOT NULL" +
                " FOREIGN KEY (" + TrailerEntry.COLUMN_MOVIE_ID +
                ") REFERENCES " + MovieEntry.TABLE_NAME +
                "(" + MovieEntry._ID + "));";

        sqLiteDatabase.execSQL(SQL_CREATE_TRAILER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ReviewEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TrailerEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME); // DROP This table last because of the Foreign Keys.
        onCreate(sqLiteDatabase);
    }
}
