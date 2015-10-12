package com.kecher.android.popularmovies.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import com.kecher.android.popularmovies.utils.PollingCheck;

import java.util.Map;
import java.util.Set;

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
public class TestUtilities extends AndroidTestCase {
    private static final String LOG_TAG = TestUtilities.class.getSimpleName();
    static final long TEST_DATE = 1419033600L;  // December 20th, 2014

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            if (entry.getValue() != null) {
                String expectedValue = entry.getValue().toString();
                assertEquals("Value '" + entry.getValue().toString() +
                        "' did not match the expected value '" +
                        expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
            } else if (columnName.equals("image")){
                assertNull(valueCursor.getBlob(idx)); // handle the null Image obj.
            } else {
                fail("This is not what was expected.");
            }
        }
    }

    static ContentValues createReviewValues(long movieRowId) {
        ContentValues reviewValues = new ContentValues();
        reviewValues.put(MovieContract.ReviewEntry.COLUMN_MOVIE_ID, movieRowId);
        reviewValues.put(MovieContract.ReviewEntry.COLUMN_REVIEW, "This movie is Awesome!");
        reviewValues.put(MovieContract.ReviewEntry.COLUMN_AUTHOR, "Everybody");

        return reviewValues;
    }

    static ContentValues createTrailerValues(long movieRowId) {
        ContentValues trailerValues = new ContentValues();
        trailerValues.put(MovieContract.TrailerEntry.COLUMN_MOVIE_ID, movieRowId);
        trailerValues.put(MovieContract.TrailerEntry.COLUMN_NAME, "Big Hero 6 Theatrical Trailer 1");
        trailerValues.put(MovieContract.TrailerEntry.COLUMN_SITE, "Youtube");
        trailerValues.put(MovieContract.TrailerEntry.COLUMN_KEY, "x3yq4k49z");

        return trailerValues;
    }

    static ContentValues createMovieValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(MovieContract.MovieEntry.COLUMN_TMDB_MOVIE_ID, 12345);
        testValues.put(MovieContract.MovieEntry.COLUMN_FAVORITE, 1);
        testValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE, "Big Hero 6");
        testValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, TEST_DATE);
        testValues.put(MovieContract.MovieEntry.COLUMN_POSTER_URL, "/7SGGUiTE6oc2fh9MjIk5M00dsQd.jpg");
        testValues.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, 7.6);
        testValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, "Hiro kicks some bad guys butt.");
        testValues.put(MovieContract.MovieEntry.COLUMN_POPULARITY, 50.9484);
        testValues.put(MovieContract.MovieEntry.COLUMN_IMAGE, (byte[]) null);

        return testValues;
    }

    static long insertMovieValues(Context context) {
        // insert our test records into the database
        MovieDbHelper dbHelper = new MovieDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = TestUtilities.createMovieValues();

        long movieRowId;
        movieRowId = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue("Error: Failure inserting Movie Values", movieRowId != -1);

        return movieRowId;
    }

    /*
        Students: The functions we provide inside of TestProvider use this utility class to test
        the ContentObserver callbacks using the PollingCheck class that we grabbed from the Android
        CTS tests.
        Note that this only tests that the onChange function is called; it does not test that the
        correct Uri is returned.
     */
    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}
