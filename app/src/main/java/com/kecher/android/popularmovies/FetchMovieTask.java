package com.kecher.android.popularmovies;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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
public class FetchMovieTask extends AsyncTask<String, Void, String> {
    private static final String LOG_TAG = FetchMovieTask.class.getSimpleName();

    public static String DISCOVER_URL = "https://api.themoviedb.org/3/discover/movie";

    // parameters in the order they were passed.
    private String sortOrder;
    private String discoverJson;

    private MoviePosterAdapter movieAdapter;
    private final Context context;

    public FetchMovieTask(Context context, MoviePosterAdapter movieAdapter) {
        this.context = context;
        this.movieAdapter = movieAdapter;
    }

    private boolean DEBUG = true;

    /**
     * Retrieves the movie poster discover information
     *
     * @param params First param is the discovery url, Second param is the API_KEY,
     *               third param is the configuration url, fourth posterSize, fifth sortOrder
     * @return an array of MoviePoster obj
     */
    @Override
    protected String doInBackground(String... params) {

        if (params.length > 4) {
            return null;
        }

        sortOrder = params[1];

        return downloadDiscoverJsonString();
    }

    @Override
    protected void onPostExecute(String json) {
        if (json != null) {
            discoverJson = json;
            ParseDiscoverJsonTask parseJsonTask = new ParseDiscoverJsonTask(context, movieAdapter);
            parseJsonTask.execute(discoverJson);
        }
    }

    private String downloadDiscoverJsonString() {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String jsonStr = null;

        try {
            final String API_KEY_PARAM = "api_key";
            final String SORT_BY_PARAM = "sort_by";

            Uri builtUri = Uri.parse(DISCOVER_URL).buildUpon()
                    .appendQueryParameter(SORT_BY_PARAM, sortOrder)
                    .appendQueryParameter(API_KEY_PARAM, Utility.getApiKey(context)).build();

            Log.d(LOG_TAG, "CONNECTION URL: " + builtUri.toString());
            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder builder = new StringBuilder();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }

            if (builder.length() == 0) {
                return null;
            }
            jsonStr = builder.toString();

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return jsonStr;
    }

}