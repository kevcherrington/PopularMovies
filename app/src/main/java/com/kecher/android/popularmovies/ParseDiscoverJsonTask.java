package com.kecher.android.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.kecher.android.popularmovies.data.MovieContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

/**
 * (C) Copyright 2015 Kevin Cherrington (kevcherrington@gmail.com).
 * <p>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * <p>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * <p>
 * Contributors:
 * Kevin Cherrington
 */
public class ParseDiscoverJsonTask extends AsyncTask<String, Void, MoviePoster[]> {
    private static final String LOG_TAG = ParseDiscoverJsonTask.class.getSimpleName();

    private Context context;
    private MoviePosterAdapter posterAdapter;

    public ParseDiscoverJsonTask(Context context, MoviePosterAdapter posterAdapter) {
        this.context = context;
        this.posterAdapter = posterAdapter;
    }

    @Override
    protected MoviePoster[] doInBackground(String... params) {
        final String RESULTS = "results";
        final String TMDB_ID = "id";
        final String OVERVIEW = "overview"; // overview
        final String RELEASE_DATE = "release_date";
        final String POSTER_PATH = "poster_path";
        final String POPULARITY = "popularity";
        final String TITLE = "title";
        final String VOTE_AVERAGE = "vote_average";

        final String API_KEY_PARAM = "api_key";

        if (params.length == 0) {
            Log.w(LOG_TAG, "No JSON string passed into ParseDiscoverJsonTask");
            return null;
        }

        MoviePoster[] posters = null;

        try {
            JSONObject posterJson = new JSONObject(params[0]);
            JSONArray results = posterJson.getJSONArray(RESULTS);

            posters = new MoviePoster[results.length()];

            SimpleDateFormat sdf = new SimpleDateFormat(MoviePoster.DATE_FORMAT);
            Vector<ContentValues> cVVector = new Vector<>();

            for (int i = 0; i < results.length(); i++) {
                JSONObject poster = results.getJSONObject(i);
                Date releaseDate = null;
                try {
                    if (poster.getString(RELEASE_DATE) != null && !poster.getString(RELEASE_DATE).equals("null")) {
                        releaseDate = sdf.parse(poster.getString(RELEASE_DATE));
                    }
                } catch (ParseException e) {
                    Log.e(LOG_TAG, "Unable to parse date ", e);
                }

                Uri builtUri = Uri.parse(Utility.getImageUrl(context)).buildUpon()
                        .appendPath(Utility.getPosterSize(context))
                        .appendPath(poster.getString(POSTER_PATH).replaceAll("/", ""))
                        .appendQueryParameter(API_KEY_PARAM, Utility.getApiKey(context)).build();

                ContentValues posterValues = new ContentValues();

                posterValues.put(MovieContract.MovieEntry.COLUMN_TMDB_MOVIE_ID, poster.getInt(TMDB_ID));
//                    posterValues.put(MovieContract.MovieEntry.COLUMN_FAVORITE, 0); // not favorite yet.
                posterValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE, poster.getString(TITLE));
                posterValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
                        MovieContract.normalizeDate(releaseDate.getTime()));
                posterValues.put(MovieContract.MovieEntry.COLUMN_POSTER_URL, builtUri.toString());
                posterValues.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, poster.getDouble(VOTE_AVERAGE));
                posterValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, poster.getString(OVERVIEW));
                posterValues.put(MovieContract.MovieEntry.COLUMN_POPULARITY, poster.getDouble(POPULARITY));
//                    posterValues.put(MovieContract.MovieEntry.COLUMN_IMAGE, new byte[0]);

                cVVector.add(posterValues);
            }

            // add to database
            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                context.getContentResolver().bulkInsert(MovieContract.MovieEntry.CONTENT_URI, cvArray);

//                notifyMovies();
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Unable to parse Discover JSON", e);
        }
        return posters;
    }

//    private void notifyMovies() {
//            Context context = getContext();
//            //checking the last update and notify if it's the first of the day
//            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//            String lastNotificationKey = context.getString(R.string.pref_last_notification);
//            long lastSync = prefs.getLong(lastNotificationKey, 0);
//
//            if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS && Utility.getNotificationsPref(context)) {
//                // Last sync was more than 1 day ago, let's send a notification with the weather.
//                String locationQuery = Utility.getPreferredLocation(context);
//
//                Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationQuery, System.currentTimeMillis());
//
//                // we'll query our contentProvider, as always
//                Cursor cursor = context.getContentResolver().query(weatherUri, NOTIFY_WEATHER_PROJECTION, null, null, null);
//
//                if (cursor.moveToFirst()) {
//                    int weatherId = cursor.getInt(INDEX_WEATHER_ID);
//                    double high = cursor.getDouble(INDEX_MAX_TEMP);
//                    double low = cursor.getDouble(INDEX_MIN_TEMP);
//                    String desc = cursor.getString(INDEX_SHORT_DESC);
//
//                    int iconId = Utility.getIconResourceForWeatherCondition(weatherId);
//                    String title = context.getString(R.string.app_name);
//
//                    // Define the text of the forecast.
//                    String contentText = String.format(context.getString(R.string.format_notification),
//                            desc,
//                            Utility.formatTemperature(context, high),
//                            Utility.formatTemperature(context, low));
//
//                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
//                            .setSmallIcon(iconId)
//                            .setContentTitle(title)
//                            .setContentText(contentText);
//
//                    Intent intent = new Intent(context, MainActivity.class);
//
//                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
//                    stackBuilder.addNextIntent(intent);
//
//                    PendingIntent pi = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
//                    mBuilder.setContentIntent(pi);
//
//                    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//                    notificationManager.notify(WEATHER_NOTIFICATION_ID, mBuilder.build());
//
//                    //refreshing last sync
//                    SharedPreferences.Editor editor = prefs.edit();
//                    editor.putLong(lastNotificationKey, System.currentTimeMillis());
//                    editor.commit();
//                }
//            }
//
//    }

    protected void onPostExecute(MoviePoster[] posters) {
        if (posters != null) {

            if (posterAdapter != null) { // update poster adapter. Will be null if view has not yet been drawn.
                posterAdapter.clear();
                for (MoviePoster poster : posters) {
                    posterAdapter.add(poster);
                }
            }
        }
    }
}
