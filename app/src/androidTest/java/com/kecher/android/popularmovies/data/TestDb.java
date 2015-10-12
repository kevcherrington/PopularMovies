package com.kecher.android.popularmovies.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.test.AndroidTestCase;

import java.util.HashSet;

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
public class TestDb extends AndroidTestCase {
    private static final String LOG_TAG = TestDb.class.getSimpleName();

    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(MovieDbHelper.DATABASE_NAME);
    }

    /*
        This function gets called before each test is executed to delete the database.  This makes
        sure that we always have a clean test.
     */
    public void setUp() {
        deleteTheDatabase();
    }

    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<>();
        tableNameHashSet.add(MovieContract.MovieEntry.TABLE_NAME);
        tableNameHashSet.add(MovieContract.ReviewEntry.TABLE_NAME);
        tableNameHashSet.add(MovieContract.TrailerEntry.TABLE_NAME);

        mContext.deleteDatabase(MovieDbHelper.DATABASE_NAME);
        SQLiteDatabase db = getSqLiteDatabase();

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        assertTrue("Error: Your database was created without the tables",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        // Movie Table
        c = db.rawQuery("PRAGMA table_info(" + MovieContract.MovieEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> movieColumnHashSet = new HashSet<>();
        movieColumnHashSet.add(MovieContract.MovieEntry._ID);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_TMDB_MOVIE_ID);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_FAVORITE);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_RELEASE_DATE);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_POSTER_URL);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_OVERVIEW);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_POPULARITY);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_IMAGE);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            movieColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required movie entry columns",
                movieColumnHashSet.isEmpty());

        // Review Table
        c = db.rawQuery("PRAGMA table_info(" + MovieContract.ReviewEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> reviewColumnHashSet = new HashSet<>();
        reviewColumnHashSet.add(MovieContract.ReviewEntry._ID);
        reviewColumnHashSet.add(MovieContract.ReviewEntry.COLUMN_MOVIE_ID);
        reviewColumnHashSet.add(MovieContract.ReviewEntry.COLUMN_REVIEW);
        reviewColumnHashSet.add(MovieContract.ReviewEntry.COLUMN_AUTHOR);

        int reviewColumnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(reviewColumnNameIndex);
            reviewColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required review entry columns",
                reviewColumnHashSet.isEmpty());

        // Trailer Table
        c = db.rawQuery("PRAGMA table_info(" + MovieContract.TrailerEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> trailerColumnHashSet = new HashSet<>();
        trailerColumnHashSet.add(MovieContract.TrailerEntry._ID);
        trailerColumnHashSet.add(MovieContract.TrailerEntry.COLUMN_MOVIE_ID);
        trailerColumnHashSet.add(MovieContract.TrailerEntry.COLUMN_NAME);
        trailerColumnHashSet.add(MovieContract.TrailerEntry.COLUMN_SITE);
        trailerColumnHashSet.add(MovieContract.TrailerEntry.COLUMN_KEY);

        int trailerColumnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(trailerColumnNameIndex);
            trailerColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required trailer entry columns",
                trailerColumnHashSet.isEmpty());
        db.close();
    }

    public void testMovieTable() {
        SQLiteDatabase db = getSqLiteDatabase();

        ContentValues testValues = TestUtilities.createMovieValues();

        assertTrue(insertMovieIntoDb(db, testValues) != -1L);

        Cursor cursor = db.query(MovieContract.MovieEntry.TABLE_NAME, // Table to query
                null, // all columns
                null, // Columns for the "where" clause
                null, // values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null); // sort order

        assertTrue("There are no records... This was unexpected.", cursor.moveToFirst());

        TestUtilities.validateCurrentRecord("Oh Noes... This record isn't what was expected.", cursor, testValues);

        assertFalse("Error: There is more than one record in the movie table", cursor.moveToNext());

        cursor.close();
        db.close();
    }

    @NonNull
    private SQLiteDatabase getSqLiteDatabase() {
        SQLiteDatabase db = new MovieDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        return db;
    }

    private Long insertMovieIntoDb(SQLiteDatabase db, ContentValues testValues) {
        return db.insert(MovieContract.MovieEntry.TABLE_NAME, null, testValues);
    }

    public void testReviewTable() {
        SQLiteDatabase db = getSqLiteDatabase();
        Long movieId = insertMovieIntoDb(db, TestUtilities.createMovieValues());

        ContentValues testValues = TestUtilities.createReviewValues(movieId);
        Long reviewId = db.insert(MovieContract.ReviewEntry.TABLE_NAME, null, testValues);

        assertTrue(reviewId != -1);

        Cursor cursor = db.query(MovieContract.ReviewEntry.TABLE_NAME, // table to query
                null, // all columns
                null, // Columns for the "where" clause
                null, // values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null); // sort order

        assertTrue("The review entry wasn't inserted properly", cursor.moveToFirst());

        TestUtilities.validateCurrentRecord("This isn't the record that we were expecting", cursor, testValues);

        assertFalse("Error: There is more than one record in the review table", cursor.moveToNext());

        cursor.close();
        db.close();
    }

    public void testTrailerTable() {
        SQLiteDatabase db = getSqLiteDatabase();
        Long movieId = insertMovieIntoDb(db, TestUtilities.createMovieValues());

        ContentValues testValues = TestUtilities.createTrailerValues(movieId);
        Long trailerId = db.insert(MovieContract.TrailerEntry.TABLE_NAME, null, testValues);

        assertTrue(trailerId != -1);

        Cursor cursor = db.query(MovieContract.TrailerEntry.TABLE_NAME, // table to query
                null, // all columns
                null, // Columns for the "where" clause
                null, // values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null); // sort order

        assertTrue("The trailer entry wasn't inserted properly", cursor.moveToFirst());

        TestUtilities.validateCurrentRecord("This isn't the record that we were expecting", cursor, testValues);

        assertFalse("Error: There is more than one record in the trailer table", cursor.moveToNext());

        cursor.close();
        db.close();
    }

}
