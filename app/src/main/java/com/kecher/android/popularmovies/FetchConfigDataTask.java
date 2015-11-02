package com.kecher.android.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
public class FetchConfigDataTask extends AsyncTask<String, Void, Map<String, String[]>> { // AsyncTask<params, progress, result>
    private final String LOG_TAG = FetchConfigDataTask.class.getSimpleName();

    Map<String, String[]> configData;

    final String TMDB_IMAGES = "images";
    final String TMDB_SECURE_BASE_URL = "secure_base_url";
    final String TMDB_BACKDROP_SIZES = "backdrop_sizes";
    final String TMDB_POSTER_SIZES = "poster_sizes";
    final String TMDB_CHANGE_KEYS = "change_keys";
    private boolean fetchPosterImages;
    private Context context;
    private MoviePosterAdapter posterAdapter;

    public FetchConfigDataTask(Context context, MoviePosterAdapter posterAdapter) {
        this.context = context;
        this.posterAdapter = posterAdapter;
    }

    private Map<String, String[]> getConfigurationJson(String configurationJsonStr) throws JSONException {
        // the names of the JSON objects to be extracted.

        JSONObject configurationJson = new JSONObject(configurationJsonStr);
        JSONObject imagesJson = configurationJson.getJSONObject(TMDB_IMAGES);
        JSONArray changeKeys = configurationJson.getJSONArray(TMDB_CHANGE_KEYS);

        String secureBaseUrl = imagesJson.getString(TMDB_SECURE_BASE_URL);
        JSONArray backdropSizes = imagesJson.getJSONArray(TMDB_BACKDROP_SIZES);
        JSONArray posterSizes = imagesJson.getJSONArray(TMDB_POSTER_SIZES);

        Map<String, String[]> results = new HashMap<>();
        results.put(TMDB_SECURE_BASE_URL, new String[]{secureBaseUrl});

        String[] backdropSizesAry = new String[backdropSizes.length()];
        for (int i = 0; i < backdropSizes.length(); i++) {
            backdropSizesAry[i] = backdropSizes.getString(i);
        }

        String[] posterSizesAry = new String[posterSizes.length()];
        for (int i = 0; i < posterSizes.length(); i++) {
            posterSizesAry[i] = posterSizes.getString(i);
        }

        String[] changeKeysAry = new String[changeKeys.length()];
        for (int i = 0; i < changeKeys.length(); i++) {
            changeKeysAry[i] = changeKeys.getString(i);
        }

        results.put(TMDB_BACKDROP_SIZES, backdropSizesAry);
        results.put(TMDB_POSTER_SIZES, posterSizesAry);
        results.put(TMDB_CHANGE_KEYS, changeKeysAry);

        return results;
    }

    @Override
    protected Map<String, String[]> doInBackground(String... params) {

        if (params.length == 0) {
            return null;
        }

        fetchPosterImages = "true".equals(params[1]);

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String configurationJsonStr = null;

        try {
            final String CONFIGURATION_BASE_URL = "https://api.themoviedb.org/3/configuration";
            final String API_KEY_PARAM = "api_key";

            Uri builtUri = Uri.parse(CONFIGURATION_BASE_URL).buildUpon()
                    .appendQueryParameter(API_KEY_PARAM, params[0]).build();

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
            configurationJsonStr = builder.toString();
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

        try {
            return getConfigurationJson(configurationJsonStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Map<String, String[]> result) {
        if (result != null) {
            configData = result;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(context.getString(R.string.config_image_url), configData.get(TMDB_SECURE_BASE_URL)[0]);
        editor.putString(context.getString(R.string.config_poster_size), configData.get(TMDB_POSTER_SIZES)[3]);
        editor.putLong(context.getString(R.string.config_last_config_refresh), new Date().getTime());
        editor.commit();

        String sortOrder = prefs.getString(context.getString(R.string.pref_sort_key),
                context.getString(R.string.pref_sort_popular));

        if (fetchPosterImages) {
            FetchMovieTask posterTask = new FetchMovieTask(context, posterAdapter);
            posterTask.execute(sortOrder);
        }
    }
}
